package com.silicolife.textmining.processes.resources.dictionary.loaders.drugbank;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.silicolife.textmining.core.datastructures.general.ExternalIDImpl;
import com.silicolife.textmining.core.datastructures.general.SourceImpl;
import com.silicolife.textmining.core.datastructures.resources.dictionary.loaders.DictionaryLoaderHelp;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.dataaccess.layer.resources.IResourceManagerReport;
import com.silicolife.textmining.core.interfaces.core.general.IExternalID;
import com.silicolife.textmining.core.interfaces.core.general.source.ISource;
import com.silicolife.textmining.core.interfaces.core.report.resources.IResourceUpdateReport;
import com.silicolife.textmining.core.interfaces.resource.dictionary.IDicionaryFlatFilesLoader;
import com.silicolife.textmining.core.interfaces.resource.dictionary.configuration.IDictionaryLoaderConfiguration;

public class DrugBankFlatFileLoader extends DictionaryLoaderHelp implements IDicionaryFlatFilesLoader {
	
	private static final String drugbanksource = "Drug Bank";
	private boolean cancel = false;
	private long startTime;
	
	protected static final String drug = "drug";
	protected static final String drugPartner = "drug_partner";
	protected static final String drugClass = "drug";


	public final static String propertyloadDrugPartner = "Drug Partner Load";
	
	public DrugBankFlatFileLoader()
	{
		super(drugbanksource);
	}
	
	public IResourceUpdateReport loadTerms(IDictionaryLoaderConfiguration configuration) throws ANoteException, IOException {
		super.setResource(configuration.getDictionary());
		File file = configuration.getFlatFile();
		cancel = false;
		boolean loadDrugPArtners = true;
		boolean loadextenalIDs = configuration.loadExternalIDs();

		if(configuration.getProperties().containsKey(propertyloadDrugPartner))
		{
			loadDrugPArtners = Boolean.valueOf(configuration.getProperties().getProperty(propertyloadDrugPartner));
			if(loadDrugPArtners)
				getReport().addClassesAdding(1);
		}
		getReport().addClassesAdding(1);
		getReport().updateFile(file);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			doc.getDocumentElement().normalize();
			startTime = GregorianCalendar.getInstance().getTimeInMillis();
			int totalNumberOfNodes = 0;
			int globalIterator = 0;
			NodeList nosDrug = doc.getElementsByTagName("drug");
			totalNumberOfNodes = nosDrug.getLength();
			if(loadDrugPArtners)
			{
				NodeList nosPartner = doc.getElementsByTagName("partner");
				totalNumberOfNodes = totalNumberOfNodes + nosPartner.getLength();
				parseNode(doc, "partner",drugPartner,globalIterator,totalNumberOfNodes,loadextenalIDs);
				globalIterator = nosPartner.getLength();
			}
			parseNode(doc, "drug",drug,globalIterator,totalNumberOfNodes,loadextenalIDs);
			globalIterator = nosDrug.getLength();
		} catch (ParserConfigurationException e) {
			throw new ANoteException(e);
		} catch (SAXException e) {
			throw new ANoteException(e);
		}	
		return getReport();
	}
	
	public void parseNode (Document doc, String type,String classe, int globalIterator, int totalNumberOfNodes, boolean loadextenalIDs) throws ANoteException, IOException {
		NodeList nos = doc.getElementsByTagName(type);
		String drugbankSource = "";
		
		if (type.equals("drug")){
			drugbankSource = drugbanksource;
		}
		
		for (int iterator = 0; iterator < nos.getLength() && !cancel; iterator++) {
			Set<String> termSynomns = new HashSet<String>();
			List<IExternalID> externalIDs = new ArrayList<IExternalID>();
			String term = new String();
			
			Node currentNode = nos.item(iterator);
			
			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) currentNode;
				
				if(loadextenalIDs && type.equals("drug")){
					//Drugbank Id
					if(element.getElementsByTagName("drugbank-id").getLength() > 0){
						String drugbankId = element.getElementsByTagName("drugbank-id").item(0).getTextContent();
						ISource source = new SourceImpl(drugbankSource);
						IExternalID e = new ExternalIDImpl(drugbankId,source);
						externalIDs.add(e);
					}
				}
				
				//Name
				if(element.getElementsByTagName("name").getLength() > 0){
					term = element.getElementsByTagName("name").item(0).getTextContent();
				}

				//Synonyms
				NodeList synonymsList = element.getElementsByTagName("synonym");
				for (int i = 0; i < synonymsList.getLength(); i++) {
					Element synonym = (Element) synonymsList.item(i);
					String synonymValue = synonym.getFirstChild().getNodeValue();
					termSynomns.add(synonymValue);
				}
				if(loadextenalIDs)
				{
					//External Identifiers
					NodeList externalIdentifiersList = element.getElementsByTagName("external-identifier");
					for (int i = 0; i < externalIdentifiersList.getLength(); i++) {
						Element externalIdentifier = (Element) externalIdentifiersList.item(i);
						String externarSource = "";
						String externarId = "";

						if(externalIdentifier.getElementsByTagName("resource").getLength() > 0){
							externarSource = externalIdentifier.getElementsByTagName("resource").item(0).getTextContent();
						}
						if(externalIdentifier.getElementsByTagName("identifier").getLength() > 0){
							externarId = externalIdentifier.getElementsByTagName("identifier").item(0).getTextContent();
						}
						ISource source = new SourceImpl(externarSource);
						IExternalID e = new ExternalIDImpl(externarId,source);
						externalIDs.add(e);
					}
				}

			}
			super.addElementToBatch(term, classe, termSynomns, externalIDs, 0);
			if (!cancel && isBatchSizeLimitOvertaken()) {
				IResourceManagerReport reportBatchInserted = super.executeBatch();
				super.updateReport(reportBatchInserted, getReport());
			}
			if ((iterator+globalIterator % 100) == 0) {
				memoryAndProgress(iterator+globalIterator+1, totalNumberOfNodes);
			}
		}
		if (!cancel) {
			IResourceManagerReport reportBatchInserted = super.executeBatch();
			super.updateReport(reportBatchInserted, getReport());
		} else {
			getReport().setcancel();
		}
	}

	@Override
	public void stop() {
		cancel = true;
	}

	@Override
	public boolean checkFile(File file) {
		String dialogMsg = "";
		String consoleMsg = "";
		if (!file.getName().endsWith(".xml")) {
            dialogMsg = consoleMsg = "Invalid file format: XML required!";
        }
		else if(!file.exists()){
			dialogMsg = consoleMsg = "Selected file not exist!";
		}
		else if(!file.isFile()){
			dialogMsg = consoleMsg = "You haven't select a file!";
		}
		else if(!file.canRead()){
			dialogMsg = consoleMsg = "Selected file cannot be read!";
		}
		else if(file.length() <= 0){
			dialogMsg = consoleMsg = "Selected file is empty";
		}

		if(!dialogMsg.equals("") || !consoleMsg.equals("")){
			return false;
		}
		return true;
	}

	public static boolean checkDrugbankFile(File file) {
		String dialogMsg = "";
		String consoleMsg = "";
		if (!file.getName().endsWith(".xml")) {
            dialogMsg = consoleMsg = "Invalid file format: XML required!";
        }
		else if(!file.exists()){
			dialogMsg = consoleMsg = "Selected file not exist!";
		}
		else if(!file.isFile()){
			dialogMsg = consoleMsg = "You haven't select a file!";
		}
		else if(!file.canRead()){
			dialogMsg = consoleMsg = "Selected file cannot be read!";
		}
		else if(file.length() <= 0){
			dialogMsg = consoleMsg = "Selected file is empty";
		}

		if(!dialogMsg.equals("") || !consoleMsg.equals("")){
			return false;
		}
		return true;
	}


}
