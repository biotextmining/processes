package com.silicolife.textmining.processes.resources.ontology.chebi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.silicolife.textmining.core.datastructures.general.AnoteClass;
import com.silicolife.textmining.core.datastructures.general.ExternalIDImpl;
import com.silicolife.textmining.core.datastructures.general.SourceImpl;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.language.LanguageProperties;
import com.silicolife.textmining.core.datastructures.report.resources.ResourceUpdateReportImpl;
import com.silicolife.textmining.core.datastructures.resources.ResourceElementImpl;
import com.silicolife.textmining.core.datastructures.resources.dictionary.loaders.DictionaryLoaderHelp;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.dataaccess.layer.resources.IResourceManagerReport;
import com.silicolife.textmining.core.interfaces.core.general.IExternalID;
import com.silicolife.textmining.core.interfaces.core.general.classe.IAnoteClass;
import com.silicolife.textmining.core.interfaces.core.general.source.ISource;
import com.silicolife.textmining.core.interfaces.core.report.resources.IResourceUpdateReport;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;
import com.silicolife.textmining.core.interfaces.resource.ontologies.IOntologyLoader;
import com.silicolife.textmining.core.interfaces.resource.ontologies.configuration.IOntologyLoaderConfiguration;

import uk.ac.ebi.chebi.webapps.chebiWS.client.ChebiWebServiceClient;
import uk.ac.ebi.chebi.webapps.chebiWS.model.ChebiWebServiceFault_Exception;
import uk.ac.ebi.chebi.webapps.chebiWS.model.DataItem;
import uk.ac.ebi.chebi.webapps.chebiWS.model.Entity;
import uk.ac.ebi.chebi.webapps.chebiWS.model.LiteEntity;
import uk.ac.ebi.chebi.webapps.chebiWS.model.LiteEntityList;
import uk.ac.ebi.chebi.webapps.chebiWS.model.OntologyDataItem;
import uk.ac.ebi.chebi.webapps.chebiWS.model.OntologyDataItemList;
import uk.ac.ebi.chebi.webapps.chebiWS.model.SearchCategory;
import uk.ac.ebi.chebi.webapps.chebiWS.model.StarsCategory;

public class ChebiOntologyWSLoader extends DictionaryLoaderHelp implements IOntologyLoader{

	private boolean debug = true;
	
	private boolean cancel = false;
	private ChebiWebServiceClient chebiClient;
	
	private static String defaultClass = "Chebi Ontology";
	private static String metaboliteClass = "metabolite";
	private static String drugClass = "drug";

	
	private static String source = "ChEBI";
	private static String chebiMetaboliteId = "CHEBI:25212";
	private static String chebiDrugId = "CHEBI:23888";


	private static int maxStepsInGrapth = 8;

	
	private Set<String> synonymExclusionSources;

	private int startpoint = 0;

	
	public ChebiOntologyWSLoader() {
		super("Chebi Ontology (web-service)");
		chebiClient = new ChebiWebServiceClient();
	}


	@Override
	public IResourceUpdateReport processOntologyFile(IOntologyLoaderConfiguration configuration) throws ANoteException, IOException {
		this.setResource(configuration.getOntology());
		try {
			LiteEntityList allChebiEntities = chebiClient.getLiteEntity("*", SearchCategory.ALL, 200000, StarsCategory.ALL);
			if(debug) { System.out.println("Number Of Elements : "+allChebiEntities.getListElement().size());}
			long startTime = GregorianCalendar.getInstance().getTimeInMillis();
			IResourceUpdateReport report = new ResourceUpdateReportImpl(LanguageProperties.getLanguageStream("pt.uminho.anote2.general.resources.ontology.update.report.title"), configuration.getOntology(), null, "");
			Map<String, Entity> mapChebiIdEntity = new HashMap<>();
			Map<String,IResourceElement> ontologyIDDatabaseIndex = new HashMap<String, IResourceElement>();
			// Add root elemement
			IAnoteClass klass = new AnoteClass(defaultClass);
			getReport().addClassesAdding(2);
			IResourceElement root = new ResourceElementImpl("root",klass,new ArrayList<IExternalID>(),new ArrayList<String>(),0,true);
			this.addElementToBatch(root);
			// Process and Add all Entities to DB
			processLiteEntityList(allChebiEntities,report,mapChebiIdEntity,ontologyIDDatabaseIndex,startTime);
			// Add is_a relationships
			processIsARelations(mapChebiIdEntity,ontologyIDDatabaseIndex,root,startTime);
			long endTime = GregorianCalendar.getInstance().getTimeInMillis();
			report.setTime(endTime-startTime);
			return report;
		} catch (ChebiWebServiceFault_Exception e) {
			throw new ANoteException(e);
		}
	}

	private void processIsARelations(Map<String, Entity> mapChebiIdEntity,Map<String, IResourceElement> ontologyIDDatabaseIndex, IResourceElement root, long startTime) throws ANoteException {
		int total = 2*mapChebiIdEntity.size();
		int point = mapChebiIdEntity.size();
		for(String chebi:mapChebiIdEntity.keySet())
		{
			Entity entity = mapChebiIdEntity.get(chebi);
			porcessRelations(entity,ontologyIDDatabaseIndex, ontologyIDDatabaseIndex, root);
			if(point % 100 == 0 )
			{
				memoryAndProgress(startTime,point,total);
			}
			point ++;
		}
	}
	
	private void porcessRelations(Entity entity,Map<String, IResourceElement> ontologyIDDatabaseIndex2,Map<String, IResourceElement> ontologyIDDatabaseIndex, IResourceElement root) throws ANoteException {
		
		IResourceElement sun = ontologyIDDatabaseIndex.get(entity.getChebiId());
		List<OntologyDataItem> allEntityRelations = entity.getOntologyParents();
		List<OntologyDataItem> isAEntityRelations = onlyIsARelations(allEntityRelations);
		if(isAEntityRelations.isEmpty())
		{
			InitConfiguration.getDataAccess().addResourceElementsRelation(root, sun, "is_a");
		}
		else
		{
			processIsA(sun,ontologyIDDatabaseIndex, "is_a",isAEntityRelations);
		}
	}
	
	private void processIsA(IResourceElement sun, Map<String, IResourceElement> ontologyIDDatabaseIndex, String relationType,List<OntologyDataItem> isAEntityRelations) throws ANoteException {
		for(OntologyDataItem isA:isAEntityRelations)
		{
			IResourceElement father = ontologyIDDatabaseIndex.get(isA.getChebiId());
			if(father != null && sun != null)
				InitConfiguration.getDataAccess().addResourceElementsRelation(father, sun, relationType);
		}		
	}


	private List<OntologyDataItem> onlyIsARelations(List<OntologyDataItem> isAEntityRelations) {
		List<OntologyDataItem> out = new ArrayList<>();
		for (OntologyDataItem isAEntityRelation : isAEntityRelations) {
			if(isAEntityRelation.getType().equals("is a"))
			{
				out.add(isAEntityRelation);
			}
		}
		return out;
	}



	private void processLiteEntityList(LiteEntityList allChebiEntities, IResourceUpdateReport report, Map<String, Entity> mapChebiIdEntity, Map<String, IResourceElement> ontologyIDDatabaseIndex, long startTime) throws ANoteException, ChebiWebServiceFault_Exception {
		int total = 2*allChebiEntities.getListElement().size();
		for (int point = startpoint ;point<allChebiEntities.getListElement().size();point ++) {
			LiteEntity liteChebiEntity = allChebiEntities.getListElement().get(point);
			Entity chebiComplete = chebiClient.getCompleteEntity(liteChebiEntity.getChebiId());
			String chebiId = liteChebiEntity.getChebiId().substring(6);
			IResourceElement resourceElemnt = convertEntityIntoIResourceElement(chebiComplete);
			ontologyIDDatabaseIndex.put(chebiId,resourceElemnt);
			mapChebiIdEntity.put(chebiId, chebiComplete);
			if(isMetabolite(chebiComplete))
			{
				resourceElemnt.setTermClass(new AnoteClass(metaboliteClass));
			}
			else if(isDrug(chebiComplete))
			{
				resourceElemnt.setTermClass(new AnoteClass(drugClass));

			}
			super.addElementToBatch(resourceElemnt);
			if(point % 100 == 0 )
			{
				memoryAndProgress(startTime,point,total);
			}
			if(!cancel && isBatchSizeLimitOvertaken())
			{
				IResourceManagerReport reportBatchInserted = super.executeBatchWithoutValidation();
				super.updateReport(reportBatchInserted,report);			
			}
			if(debug) System.out.println(point + " " + chebiId);
		}
		if(!cancel)
		{
			IResourceManagerReport reportBatchInserted = super.executeBatchWithoutValidation();
			super.updateReport(reportBatchInserted,report);			
		}		
	}


	private boolean isDrug(Entity chebiComplete) {
		return isClass(chebiComplete,chebiDrugId);
	}


	protected boolean isMetabolite(Entity chebiComplete) {
		return isClass(chebiComplete,chebiMetaboliteId);
	}


	private boolean isClass(Entity chebiComplete,String chebiMetaboliteId) {
		try {
			OntologyDataItemList parents = chebiClient.getOntologyParents(chebiComplete.getChebiId());
			List<OntologyDataItem> ontologyDataItemHasRole = new ArrayList<>();
			for (OntologyDataItem ontologyDataItem : parents.getListElement()) {
				if(ontologyDataItem.getType().equals("has role"))
				{
					ontologyDataItemHasRole.add(ontologyDataItem);
				}
			}
			if(!ontologyDataItemHasRole.isEmpty())
			{
				Set<String> metaboliteIsAChebiIds = new HashSet<>();
				for (OntologyDataItem ontologyDataItem : ontologyDataItemHasRole) {
					List<String> possibleApplication = getApplications(ontologyDataItem,0,new ArrayList<>(),chebiMetaboliteId);
					// only application leaf
					if(!possibleApplication.isEmpty())
						metaboliteIsAChebiIds.add(possibleApplication.get(0));
				}
				return !metaboliteIsAChebiIds.isEmpty();
			}
		} catch (ChebiWebServiceFault_Exception e) {
		}
		return false;
	}


	private List<String> getApplications(OntologyDataItem ontologyDataItem,int deep, List<String> candidateApplicaitonNames,String chebiMetaboliteId) throws ChebiWebServiceFault_Exception {

		if(deep>maxStepsInGrapth)
		{
			return new ArrayList<>();
		}
		if(ontologyDataItem.getChebiId().equals(chebiMetaboliteId))
		{
			return candidateApplicaitonNames;
		}
		List<OntologyDataItem> parents = chebiClient.getOntologyParents(ontologyDataItem.getChebiId()).getListElement();
		candidateApplicaitonNames.add(ontologyDataItem.getChebiId());
		List<String> out = new ArrayList<>();
		for (OntologyDataItem ontologyDataIt : parents) {
			if(ontologyDataIt.getType().equals("is a"))
				out.addAll(getApplications(ontologyDataIt, ++deep, candidateApplicaitonNames,chebiMetaboliteId));
		}
		return out;
	}
	
	private IResourceElement convertEntityIntoIResourceElement(Entity chebiComplete) {
		String term = chebiComplete.getChebiAsciiName();
		String chebiId = chebiComplete.getChebiId().substring(6);
		List<String> synonyms = new ArrayList<String>();
		List<DataItem> chebiCompleteSynonyms = chebiComplete.getSynonyms();
		for (DataItem dataItem : chebiCompleteSynonyms) {
			String source = dataItem.getSource();
			if(!getSynonymExclusionSources().contains(source))
			{
				synonyms.add(dataItem.getData());
				if(debug) { System.out.println("Synonym ["+chebiComplete.getChebiId()+ "] "+dataItem.getData() + " - "+dataItem.getSource());}
			}
		}
		List<IExternalID> externalIDs = new ArrayList<>();
		ISource sourceChebi = new SourceImpl(source);
		externalIDs.add(new ExternalIDImpl(chebiId, sourceChebi));
		// Other Chebi Ids
//		List<String> secondaruChebiIds = chebiComplete.getSecondaryChEBIIds();
//		for (String econdaruChebiId : secondaruChebiIds) {
//			externalIDs.add(new ExternalIDImpl(econdaruChebiId.substring(6), sourceChebi));
//		}
		// Other External Ids besides Chebi
//		chebiComplete.getDatabaseLinks();
		IAnoteClass termClass = new AnoteClass(defaultClass);
		IResourceElement out = new ResourceElementImpl(term,termClass,externalIDs,synonyms, 0,true);
		return out;
	}


	public void stop() {
		cancel = true;		
	}


	public Set<String> getSynonymExclusionSources() {
		if(synonymExclusionSources==null)
		{
			synonymExclusionSources = new HashSet<>();
		}
		return synonymExclusionSources;
	}




}
