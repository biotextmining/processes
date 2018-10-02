package com.silicolife.textmining.processes.resources.ontology.chebi;

import java.util.List;

import org.junit.Test;

import uk.ac.ebi.chebi.webapps.chebiWS.client.ChebiWebServiceClient;
import uk.ac.ebi.chebi.webapps.chebiWS.model.ChebiWebServiceFault_Exception;
import uk.ac.ebi.chebi.webapps.chebiWS.model.DataItem;
import uk.ac.ebi.chebi.webapps.chebiWS.model.Entity;
import uk.ac.ebi.chebi.webapps.chebiWS.model.LiteEntity;
import uk.ac.ebi.chebi.webapps.chebiWS.model.LiteEntityList;
import uk.ac.ebi.chebi.webapps.chebiWS.model.OntologyDataItem;
import uk.ac.ebi.chebi.webapps.chebiWS.model.SearchCategory;
import uk.ac.ebi.chebi.webapps.chebiWS.model.StarsCategory;

public class ChebiWSTest {
	
	private ChebiWebServiceClient chebiClient = new ChebiWebServiceClient();

	@Test
	public void test() throws ChebiWebServiceFault_Exception {
		LiteEntityList result = chebiClient.getLiteEntity("*", SearchCategory.ALL, 10, StarsCategory.ALL);
		System.out.println(result.getListElement().size());
		for(LiteEntity chebi : result.getListElement())
		{
			Entity chebiComplete = chebiClient.getCompleteEntity(chebi.getChebiId());
			System.out.println(chebiComplete.getChebiId());
			System.out.println(chebiComplete.getChebiAsciiName());
			List<DataItem> synonymsDI = chebiComplete.getSynonyms();
			for (DataItem dataItem : synonymsDI) {
				System.out.println(dataItem.getData() + " [" + dataItem.getSource()+"]");
			}
			List<DataItem> databaseLinks = chebiComplete.getDatabaseLinks();
			for (DataItem dataItem : databaseLinks) {
				System.out.println("\t"+dataItem.getType() + ":" + dataItem.getData());
			}
			System.out.println(chebiComplete.getSecondaryChEBIIds());
			List<OntologyDataItem> ontolyParents = chebiComplete.getOntologyParents();
			for (OntologyDataItem ontologyDataItem : ontolyParents) {
				System.out.println("\t\t" + chebi.getChebiId() + " " + ontologyDataItem.getType() + " " + ontologyDataItem.getChebiId());
			}
			System.out.println("\n");
		}	}

}
