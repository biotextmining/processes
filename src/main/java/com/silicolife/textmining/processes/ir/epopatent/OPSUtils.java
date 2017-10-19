package com.silicolife.textmining.processes.ir.epopatent;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.util.PDFMergerUtility;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.silicolife.textmining.core.datastructures.dataaccess.database.dataaccess.implementation.utils.PublicationFieldTypeEnum;
import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationSourcesDefaultEnum;
import com.silicolife.textmining.core.datastructures.documents.structure.PublicationFieldImpl;
import com.silicolife.textmining.core.datastructures.textprocessing.NormalizationForm;
import com.silicolife.textmining.core.datastructures.utils.GenericPairImpl;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.structure.IPublicationField;
import com.silicolife.textmining.processes.ir.epopatent.configuration.OPSConfiguration;
import com.silicolife.textmining.processes.ir.epopatent.opshandler.AutenticationHandler;
import com.silicolife.textmining.processes.ir.epopatent.opshandler.OPSPatentClaimsHandler;
import com.silicolife.textmining.processes.ir.epopatent.opshandler.OPSPatentDescriptionHandler;
import com.silicolife.textmining.processes.ir.epopatent.opshandler.OPSPatentFamilyHandler;
import com.silicolife.textmining.processes.ir.epopatent.opshandler.OPSPatentIDSearchHandler;
import com.silicolife.textmining.processes.ir.epopatent.opshandler.OPSPatentImageHandler;
import com.silicolife.textmining.processes.ir.epopatent.opshandler.OPSPatentOwnersHandler;
import com.silicolife.textmining.processes.ir.epopatent.opshandler.OPSPatentUpdateHandler;
import com.silicolife.textmining.processes.ir.epopatent.opshandler.OPSPatentgetPDFPageHandler;
import com.silicolife.textmining.processes.ir.epopatent.opshandler.OPSSearchHandler;
import com.silicolife.textmining.processes.ir.epopatent.opshandler.OPSSearchResultHandler;
import com.silicolife.textmining.utils.http.HTTPClient;
import com.silicolife.textmining.utils.http.exceptions.ClientErrorException;
import com.silicolife.textmining.utils.http.exceptions.ConnectionException;
import com.silicolife.textmining.utils.http.exceptions.RedirectionException;
import com.silicolife.textmining.utils.http.exceptions.ResponseHandlingException;
import com.silicolife.textmining.utils.http.exceptions.ServerErrorException;

public class OPSUtils {

	private static String version = "3.2";
	private static String autenticationURL = "https://ops.epo.org/" + version + "/auth/accesstoken";
	private static String searchURL = "http://ops.epo.org/" + version + "/rest-services/published-data/search/biblio";
	private static String publicationDetails = "http://ops.epo.org/" + version + "/rest-services/published-data/publication/epodoc/";
	private static String generalURL = "http://ops.epo.org/" + version + "/rest-services/";
	private static String publicationFamily = "http://ops.epo.org/" + version + "/rest-services/family/publication/epodoc/";

	private static Set<String> excludePatentStartLetterSet;

	private static HTTPClient client = new HTTPClient();

	public static String postAuth(String autentication) throws ConnectionException, RedirectionException, ClientErrorException, ServerErrorException, ResponseHandlingException {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", "Basic " + autentication);
		headers.put("Content-Type", "application/x-www-form-urlencoded");
		String token = client.post(autenticationURL, "grant_type=client_credentials", headers, new AutenticationHandler());
		return token;
	}

	public static List<IPublication> getSearch(String tokenaccess, String query, int step) throws ConnectionException, RedirectionException, ClientErrorException,
	ServerErrorException, ResponseHandlingException {
		Map<String, String> headers = new HashMap<String, String>();
		if (tokenaccess != null) {
			headers.put("Authorization", "Bearer " + tokenaccess);
		}
		headers.put("X-OPS-Range", step + "-" + (step + OPSConfiguration.STEP - 1));
		String body = "q="+query;
		try {
			List<IPublication> pubs = client.post(searchURL,body, headers, new OPSSearchHandler());
			return pubs;
		} catch (ClientErrorException e) {
			if(e.getMessage().startsWith("404"))
			{
				return new ArrayList<IPublication>();
			}
			throw e;
		}
	}


	public static void getPatentFamily(String tokenaccess, IPublication pub, String patentID) {
		Map<String, String> headers = new HashMap<String, String>();
		if (tokenaccess != null) {
			headers.put("Authorization", "Bearer " + tokenaccess);
		}
		String urlPatentDescritpion = publicationFamily + patentID;
		// Get patent family Info
		try {
			client.get(urlPatentDescritpion, headers, new OPSPatentFamilyHandler(pub));
		} catch (RedirectionException | ClientErrorException | ServerErrorException | ConnectionException
				| ResponseHandlingException e) {
		}

	}


	public static void updatePatentMetaInformation(String tokenaccess,IPublication publiction,String patentID) throws RedirectionException, ClientErrorException, ServerErrorException, ConnectionException, ResponseHandlingException
	{
		Map<String, String> headers = new HashMap<String, String>();
		if (tokenaccess != null) {
			headers.put("Authorization", "Bearer " + tokenaccess);
		}
		String urlPatentDescritpion = publicationDetails + patentID + "/biblio";
		// Get Biblio Info
		client.get(urlPatentDescritpion, headers, new OPSPatentUpdateHandler(publiction));
		// Try to add claims and description to abstract
		if (!publiction.getNotes().startsWith("NF")){//STATUS - not found
			updateAbstractwithDescritionandclaims(tokenaccess, publiction);
		}
		else{
			publiction.setNotes(publiction.getNotes().substring(2, publiction.getNotes().length()));//return to the previous note state
		}
	}

	public static String getPatentOwners(String tokenaccess,String patentID) throws RedirectionException, ClientErrorException, ServerErrorException, ConnectionException, ResponseHandlingException
	{
		Map<String, String> headers = new HashMap<String, String>();
		if (tokenaccess != null) {
			headers.put("Authorization", "Bearer " + tokenaccess);
		}
		String urlPatentDescritpion = publicationDetails + patentID + "/biblio";
		// Get Biblio Info
		return client.get(urlPatentDescritpion, headers, new OPSPatentOwnersHandler());

	}


	public static Set<String> getSearchPatentIds(String tokenaccess,String query, int step) throws ConnectionException, RedirectionException,
	ClientErrorException, ServerErrorException, ResponseHandlingException {
		Map<String, String> headers = new HashMap<String, String>();
		if (tokenaccess != null) {
			headers.put("Authorization", "Bearer " + tokenaccess);
		}
		headers.put("X-OPS-Range", step + "-" + (step + OPSConfiguration.STEP - 1));
		try {
			Set<String> patentIds = client.get(searchURL + query, headers, new OPSPatentIDSearchHandler());
			return patentIds;
		} catch (ClientErrorException e) {
			if(e.getMessage().startsWith("404"))
			{
				return new HashSet<String>();
			}
			throw e;
		}
	}

	public static int getSearchResults(String tokenaccess,String query) throws RedirectionException, ServerErrorException, ConnectionException, ResponseHandlingException, ClientErrorException {
		Map<String, String> headers = new HashMap<String, String>();
		if (tokenaccess != null) {
			headers.put("Authorization", "Bearer " + tokenaccess);
		}
		String body = "q="+query;
		Integer result;
		try {
			result = client.post(searchURL,body, headers, new OPSSearchResultHandler());
		} catch (ClientErrorException e) {
			if(e.getMessage().startsWith("404"))
			{
				return 0;
			}
			throw e;
		}
		return result;
	}

	public static void updateAbstractwithDescritionandclaims(String tokenaccess, IPublication pub)
	{
		Map<String, String> headers = new HashMap<String, String>();
		if (tokenaccess != null) {
			headers.put("Authorization", "Bearer " + tokenaccess);
		}
		String patent = PublicationImpl.getPublicationExternalIDForSource(pub, PublicationSourcesDefaultEnum.patent.name());
		String urlPatentDescritpion = publicationDetails + patent + "/description";
		String description = null;
		try {
			description = client.get(urlPatentDescritpion, headers, new OPSPatentDescriptionHandler());
		} catch (RedirectionException e) {
		} catch (ClientErrorException e) {
		} catch (ServerErrorException e) {
		} catch (ConnectionException e) {
		} catch (ResponseHandlingException e) {
		}
		String newAbstract = pub.getAbstractSection();
		if(description!=null && description.length() > 0)
		{
			description = NormalizationForm.removeOffsetProblemSituation(description);
			int intEndAbstarct = newAbstract.length();
			String descritionconnection = " DESCRIPTION: ";
			int startdescritpion = intEndAbstarct + 1;
			if (returnIntPublicationField(pub,"Description")!=-1){
				if (verifyMinorPublicationFieldLenght (pub,"Description",description)){
					newAbstract = newAbstract + descritionconnection + description;
					int enddescritpion = newAbstract.length();
					IPublicationField publicationFieldDescription = new PublicationFieldImpl(startdescritpion, enddescritpion, "Description", PublicationFieldTypeEnum.abstracttext);
					pub.getPublicationFields().remove(returnIntPublicationField(pub, "Description"));
					pub.getPublicationFields().add(publicationFieldDescription);
				}
			}
			else{
				newAbstract = newAbstract + descritionconnection + description;
				int enddescritpion = newAbstract.length();
				IPublicationField publicationFieldDescription = new PublicationFieldImpl(startdescritpion, enddescritpion, "Description", PublicationFieldTypeEnum.abstracttext);
				pub.getPublicationFields().add(publicationFieldDescription);	
			}
		}
		String urlPatentClains = publicationDetails + patent + "/claims";
		String claims = null;
		try {
			claims = client.get(urlPatentClains, headers, new OPSPatentClaimsHandler());
		} catch (RedirectionException e) {
		} catch (ClientErrorException e) {
		} catch (ServerErrorException e) {
		} catch (ConnectionException e) {
		} catch (ResponseHandlingException e) {
		}
		if(claims!= null && claims.length() > 0)
		{
			claims = NormalizationForm.removeOffsetProblemSituation(claims);
			int intEndAbstarct = newAbstract.length();
			String claimsconnection = " CLAIMS: ";
			int startclaims = intEndAbstarct +1;
			if (returnIntPublicationField(pub,"Claims")!=-1){
				if (verifyMinorPublicationFieldLenght (pub,"Claims",claims)){
					newAbstract = newAbstract +  claimsconnection + claims;
					int endclaims = newAbstract.length();
					IPublicationField publicationFieldClaims = new PublicationFieldImpl(startclaims, endclaims, "Claims", PublicationFieldTypeEnum.abstracttext);	
					pub.getPublicationFields().remove(returnIntPublicationField(pub, "Claims"));
					pub.getPublicationFields().add(publicationFieldClaims);
				}
			}
			else{
				newAbstract = newAbstract +  claimsconnection + claims;
				int endclaims = newAbstract.length();
				IPublicationField publicationFieldClaims = new PublicationFieldImpl(startclaims, endclaims, "Claims", PublicationFieldTypeEnum.abstracttext);	
				pub.getPublicationFields().add(publicationFieldClaims);
			}
		}
		pub.setAbstractSection(newAbstract);
	}

	private static int returnIntPublicationField(IPublication pub, String name){
		List<IPublicationField> fields = pub.getPublicationFields();
		for (IPublicationField field:fields){
			if (field.getName().equalsIgnoreCase(name)){
				return fields.indexOf(field);
			}
		}
		return -1;
	}

	private static boolean verifyMinorPublicationFieldLenght (IPublication pub, String name, String newText){
		List<IPublicationField> fields = pub.getPublicationFields();
		int init=0;
		int end=0;
		for (IPublicationField field:fields){
			if (field.getName().equalsIgnoreCase(name)){
				init=(int) field.getStart();
				end=(int) field.getEnd();
				if ((end-init)<newText.length()){
					return true;
				}
			}
		}
		return false;
	}

	public static File getPatentFullTextPDF(String tokenaccess, IPublication pub,String path) throws COSVisitorException, IOException, RedirectionException, ClientErrorException,
	ServerErrorException, ConnectionException, ResponseHandlingException, InterruptedException {
		Map<String, String> headers = new HashMap<String, String>();
		if (tokenaccess != null) {
			headers.put("Authorization", "Bearer " + tokenaccess);
		}
		String epodoc = PublicationImpl.getPublicationExternalIDForSource(pub, PublicationSourcesDefaultEnum.patent.name());
		String urlPatentImages = publicationDetails + epodoc + "/images";
		GenericPairImpl<Integer, String> pagesLink = client.get(urlPatentImages, headers, new OPSPatentImageHandler());
		if(pagesLink==null)
			return null;
		Path docPath = Paths.get(path +"/" + "/tmp_" + epodoc);
		if (!Files.exists(docPath))
			Files.createDirectories(docPath);

		// -- API to merge PDF
		PDFMergerUtility merger = new PDFMergerUtility();

		Integer numberPages = pagesLink.getX();
		for (int x = 1; x <= numberPages; x++) {
			String urlpages = generalURL + pagesLink.getY() + ".pdf?" + "Range=" + x;
			File pdfOnePage = client.get(urlpages, headers, new OPSPatentgetPDFPageHandler(docPath.toString()));
			// -- Add source to merge
			merger.addSource(pdfOnePage);
			if(x%5 == 0 && tokenaccess==null)
			{
				if(tokenaccess==null)
				{
					System.out.println("sleeping...62 seconds");
					Thread.sleep(62000);
				}
			}
		}

		String docPDFFinal = path +"/" + pub.getId() + ".pdf";
		merger.setDestinationFileName(docPDFFinal);
		merger.mergeDocuments();

		recursiveDelete(docPath.toFile());

		return new File(docPDFFinal);
	}


	public static File getPatentFullTextPDFUsingPatentID(String tokenaccess, String patentID, File outDir) throws COSVisitorException, RedirectionException, ClientErrorException, ServerErrorException, IOException, ConnectionException, ResponseHandlingException, InterruptedException {
		return getPatentFullTextPDFUsingPatentID(tokenaccess,patentID ,outDir.getPath(),null);
	}

	public static File getPatentFullTextPDFUsingPatentID(String tokenaccess, String patentID ,String path,Long pubID) throws COSVisitorException, IOException, RedirectionException, ClientErrorException, ServerErrorException, ConnectionException, ResponseHandlingException, InterruptedException{
		Map<String, String> headers = new HashMap<String, String>();
		if (tokenaccess != null) {
			headers.put("Authorization", "Bearer " + tokenaccess);
		}
		String urlPatentImages = publicationDetails + patentID + "/images";
		GenericPairImpl<Integer, String> pagesLink = client.get(urlPatentImages, headers, new OPSPatentImageHandler());
		if(pagesLink==null)
			return null;
		Path docPath = Paths.get(path +"/" + "/tmp_" + pubID);
		if (!Files.exists(docPath))
			Files.createDirectories(docPath);

		// -- API to merge PDF
		PDFMergerUtility merger = new PDFMergerUtility();

		Integer numberPages = pagesLink.getX();
		for (int x = 1; x <= numberPages; x++) {
			String urlpages = generalURL + pagesLink.getY() + ".pdf?" + "Range=" + x;
			File pdfOnePage = client.get(urlpages, headers, new OPSPatentgetPDFPageHandler(docPath.toString()));
			// -- Add source to merge
			merger.addSource(pdfOnePage);
			if(x%5 == 0 && tokenaccess==null)
			{
				if(tokenaccess==null)
				{
					System.out.println("sleeping...62 seconds");
					Thread.sleep(62000);
				}
			}
		}
		String docPDFFinal;
		if(pubID!=null)
		{
			docPDFFinal = path +"/" + pubID + ".pdf";
		}
		else
		{
			docPDFFinal = path +"/" + patentID + ".pdf";
		}
		merger.setDestinationFileName(docPDFFinal);
		merger.mergeDocuments();
		recursiveDelete(docPath.toFile());
		return new File(docPDFFinal);
	}



	private static void recursiveDelete(File file) {
		if (file.isDirectory()) {
			if (file.list().length == 0) {
				file.delete();
			} else {
				String files[] = file.list();
				for (String temp : files) {
					File fileDelete = new File(file, temp);
					recursiveDelete(fileDelete);
				}

				recursiveDelete(file);
			}
		} else {
			file.delete();
		}
	}

	public static Document createJDOMDocument(InputStream response) throws ParserConfigurationException, SAXException, IOException {
		String stream = IOUtils.toString(response, "UTF-8");
		stream = stream.replaceAll("\n", "");
		stream = stream.replaceAll("\\s{2,}", "");
		InputStream imputstream = new ByteArrayInputStream(stream.getBytes("UTF-8"));
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser = factory.newDocumentBuilder();
		Document doc = parser.parse(imputstream);
		return doc;

	}

	public static String readString(InputStream is) throws IOException {
		char[] buf = new char[2048];
		Reader r = new InputStreamReader(is, "UTF-8");
		StringBuilder s = new StringBuilder();
		while (true) {
			int n = r.read(buf);
			if (n < 0)
				break;
			s.append(buf, 0, n);
		}
		String rsult = s.toString();
		r.close();
		return rsult;
	}

	public static String queryBuilder(String query)
	{
		query = tranform(query);
		//		query = query.replaceAll("\\?", "%3F");
		//		query = query.replaceAll("@", "%40");
		//		query = query.replaceAll("#", "%23");
		//		query = query.replaceAll("%", "%25");
		//		query = query.replaceAll("\\$", "%24");
		//		query = query.replaceAll("&", "%26");
		//		query = query.replaceAll("\\+", "%2B");
		//		query = query.replaceAll(",", "%2C");
		//		query = query.replaceAll(":", "%3A");
		//		query = query.replaceAll(" ", "%20");
		//		query = query.replaceAll("=", "%3D");
		//		query = query.replaceAll("\"", "%22");
		//		query = query.replaceAll("<", "%3C");
		//		query = query.replaceAll(">", "%3E");
		//		query = query.replaceAll("\\{", "%7B");
		//		query = query.replaceAll("\\}", "%7D");
		//		query = query.replaceAll("\\|", "%7C");
		//		query = query.replaceAll("\\^", "%5E");
		//		query = query.replaceAll("~", "%7E");
		//		query = query.replaceAll("\\[", "%5B");
		//		query = query.replaceAll("\\]", "%5D");
		//		query = query.replaceAll("`", "%60");
		return query;
	}

	private static String tranform(String keywords) {
		keywords = keywords.trim();
		keywords = keywords.replace("AND"," AND ");
		keywords = keywords.replace("OR"," OR ");
		keywords = keywords.replace("\"\"", "\"");
		keywords = keywords.replace("  ", " ");
		return keywords;
	}


	public static String loginOPS(String authentication){
		String tokenaccess=null;
		try {
			tokenaccess = OPSUtils.postAuth(authentication);
		} catch (RedirectionException | ClientErrorException| ServerErrorException | ConnectionException
				| ResponseHandlingException e) {
			tokenaccess = null;
		}
		return tokenaccess;
	}

	public static boolean isAcceptablePatentLanguage(String patentID)
	{
		if(excludePatentStartLetterSet==null)
			createExcluseStartSettings();
		for(String excludePatentStartLetter:excludePatentStartLetterSet)
		{
			if(patentID.startsWith(excludePatentStartLetter))
			{
				return false;
			}
		}
		return true;
	}

	private static void createExcluseStartSettings() {
		excludePatentStartLetterSet = new HashSet<>();
		excludePatentStartLetterSet.add("JP");
		excludePatentStartLetterSet.add("CN");
		excludePatentStartLetterSet.add("RU");
		excludePatentStartLetterSet.add("DE");
		excludePatentStartLetterSet.add("TW");
		excludePatentStartLetterSet.add("KR");
		excludePatentStartLetterSet.add("FR");
		excludePatentStartLetterSet.add("CH");
		excludePatentStartLetterSet.add("FI");
		excludePatentStartLetterSet.add("MX");
		excludePatentStartLetterSet.add("ES");
		excludePatentStartLetterSet.add("DK");
		excludePatentStartLetterSet.add("HRP");
		excludePatentStartLetterSet.add("CZ");
	}


}
