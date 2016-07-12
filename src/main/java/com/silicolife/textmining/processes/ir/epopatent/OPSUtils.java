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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
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
import com.silicolife.textmining.processes.ir.epopatent.opshandler.OPSPatentIDSearchHandler;
import com.silicolife.textmining.processes.ir.epopatent.opshandler.OPSPatentImageHandler;
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

	private static String version = "3.1";
	private static String autenticationURL = "https://ops.epo.org/" + version + "/auth/accesstoken";
	private static String searchURL = "http://ops.epo.org/" + version + "/rest-services/published-data/search/biblio/?q=";
	private static String publicationDetails = "http://ops.epo.org/" + version + "/rest-services/published-data/publication/epodoc/";
	private static String generalURL = "http://ops.epo.org/" + version + "/rest-services/";

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
		List<IPublication> pubs = client.get(searchURL + query, headers, new OPSSearchHandler());
		return pubs;
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
		updateAbstractwithDescritionandclaims(tokenaccess, publiction);
	}

	public static void updatePatentMetaInformationWithClientError(String tokenaccess,IPublication publiction,String patentID) throws RedirectionException, ClientErrorException, ServerErrorException, ConnectionException, ResponseHandlingException
	{
		Map<String, String> headers = new HashMap<String, String>();
		if (tokenaccess != null) {
			headers.put("Authorization", "Bearer " + tokenaccess);
		}
		String urlPatentDescritpion = "http://ops.epo.org/3.1/rest-services/published-data/search/biblio/?q=" + patentID;
		// Get Biblio Info
		client.get(urlPatentDescritpion, headers, new OPSPatentUpdateHandler(publiction));
		// Try to add claims and description to abstract
		updateAbstractwithDescritionandclaims(tokenaccess, publiction);
	}


	public static Set<String> getSearchPatentIds(String tokenaccess,String query, int step) throws ConnectionException, RedirectionException,
	ClientErrorException, ServerErrorException, ResponseHandlingException {
		Map<String, String> headers = new HashMap<String, String>();
		if (tokenaccess != null) {
			headers.put("Authorization", "Bearer " + tokenaccess);
		}
		headers.put("X-OPS-Range", step + "-" + (step + OPSConfiguration.STEP - 1));
		Set<String> patentIds = client.get(searchURL + query, headers, new OPSPatentIDSearchHandler());
		return patentIds;
	}

	public static int getSearchResults(String query) throws RedirectionException, ClientErrorException, ServerErrorException, ConnectionException, ResponseHandlingException {
		Map<String, String> headers = new HashMap<String, String>();
		Integer result = client.get(searchURL + query, headers, new OPSSearchResultHandler());
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
			newAbstract = newAbstract + descritionconnection + description;
			int enddescritpion = newAbstract.length();
			IPublicationField publicationFieldDescription = new PublicationFieldImpl(startdescritpion, enddescritpion, "Description", PublicationFieldTypeEnum.abstracttext);
			pub.getPublicationFields().add(publicationFieldDescription);
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
			newAbstract = newAbstract +  claimsconnection + claims;
			int endclaims = newAbstract.length();
			IPublicationField publicationFieldClaims = new PublicationFieldImpl(startclaims, endclaims, "Claims", PublicationFieldTypeEnum.abstracttext);
			pub.getPublicationFields().add(publicationFieldClaims);

		}
		pub.setAbstractSection(newAbstract);
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


	public static File getPatentFullTextPDFUsingPatentID(String tokenaccess, String patentID ,String path,long pubID) throws COSVisitorException, IOException, RedirectionException, ClientErrorException, ServerErrorException, ConnectionException, ResponseHandlingException, InterruptedException{
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

		String docPDFFinal = path +"/" + pubID + ".pdf";
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
		query = query.replaceAll("\\?", "%3F");
		query = query.replaceAll("@", "%40");
		query = query.replaceAll("#", "%23");
		query = query.replaceAll("%", "%25");
		query = query.replaceAll("\\$", "%24");
		query = query.replaceAll("&", "%26");
		query = query.replaceAll("\\+", "%2B");
		query = query.replaceAll(",", "%2C");
		query = query.replaceAll(":", "%3A");
		query = query.replaceAll(" ", "%20");
		query = query.replaceAll("=", "%3D");
		query = query.replaceAll("\"", "%22");
		query = query.replaceAll("<", "%3C");
		query = query.replaceAll(">", "%3E");
		query = query.replaceAll("\\{", "%7B");
		query = query.replaceAll("\\}", "%7D");
		query = query.replaceAll("\\|", "%7C");
		query = query.replaceAll("\\^", "%5E");
		query = query.replaceAll("~", "%7E");
		query = query.replaceAll("\\[", "%5B");
		query = query.replaceAll("\\]", "%5D");
		query = query.replaceAll("`", "%60");
		return query;
	}

	private static String tranform(String keywords) {
		keywords = keywords.trim();
		String[] keywordsParts = keywords.split("AND|OR");
		for(String part : keywordsParts)
		{
			part = part.trim();
			String partLower = part.toLowerCase();
			if(!part.isEmpty())
			{
				keywords = keywords.replace(part, "\""+partLower+"\"");
			}
		}
		keywords = keywords.replace("AND"," AND ");
		keywords = keywords.replace("OR"," OR ");
		keywords = keywords.replace("\"\"", "\"");
		keywords = keywords.replace("  ", " ");
		return keywords;
	}

	public static int verifySectionNumbers(String patentID){
		if(patentID.matches(".*[A-Z]{1}")){
			return 1;
		}
		if(patentID.matches(".*[A-Z]{1}[1-9]{1}")){
			return 2;
		}
		return 0;
	}


	public static boolean verify0OnTheMiddle(String patentID){
		int lettersOfSection = verifySectionNumbers(patentID);
		if(patentID.charAt(patentID.length()-7-(lettersOfSection))=='0'||patentID.charAt(patentID.length()-6-(lettersOfSection))=='0'){//some patents have a "0" on middle with 6 numbers after
			return true;
		}

		return false;
	}
	

	public static boolean verifyYearPresence(String patentID){
		if (Character.isLetter(patentID.charAt(0))){
			if (Character.isLetter(patentID.charAt(1))){
				String year=patentID.substring(2,6);//year
				try{
					int date=Integer.parseInt(year);
					if (date>=1900 && date<=Calendar.getInstance().get(Calendar.YEAR)){
						return true;
					}
				}catch(NumberFormatException e){
					return false;				
				}
			}
			else{
				String year=patentID.substring(1,5);//year
				int date=Integer.parseInt(year);
				if (date>=1900 && date<=Calendar.getInstance().get(Calendar.YEAR)){
					return true;
				}
			}
		}
		return false;
	}

	public static String deleteChar0(String patentID,int lettersOfSection){
		String newPatentID = patentID;
		if(patentID.charAt(patentID.length()-7-(lettersOfSection))=='0'){//some patents have a "0" on middle with 6 numbers after
			newPatentID=patentID.substring(0,patentID.length()-7-(lettersOfSection)).concat(patentID.substring(patentID.length()-6-(lettersOfSection),patentID.length()));
		}
		return newPatentID;
	}


	public static String transformYear(String patentID) throws ParseException{
		String newPatentID = new String();
		if (Character.isLetter(patentID.charAt(0))){
			if (Character.isLetter(patentID.charAt(1))){
				String year=patentID.substring(2,6);//year
				int date=Integer.parseInt(year);
				if (date>=1900 && date<=Calendar.getInstance().get(Calendar.YEAR)){
					SimpleDateFormat dateParser = new SimpleDateFormat("yyyy"); //formatter for parsing date
					SimpleDateFormat dateFormatter = new SimpleDateFormat("yy"); //date output
					Date dateParsering = dateParser.parse(year);
					String newYear = dateFormatter.format(dateParsering);
					newPatentID=patentID.substring(0, 2)+newYear+patentID.substring(6, patentID.length());
				}
			}
			else{
				String year=patentID.substring(1,5);//year
				int date=Integer.parseInt(year);
				if (date>=1900 && date<=Calendar.getInstance().get(Calendar.YEAR)){
					SimpleDateFormat dateParser = new SimpleDateFormat("yyyy"); //formatter for parsing date
					SimpleDateFormat dateFormatter = new SimpleDateFormat("yy"); //formatter for formatting date output
					Date dateParsering = dateParser.parse(year);
					String newYear = dateFormatter.format(dateParsering);
					newPatentID=patentID.substring(0, 2)+newYear+patentID.substring(6, patentID.length());
				}
			}
		}
		return newPatentID;
	}


	public static String deleteSectionNumbers(String patentID){

		//char[] array = patentID.toCharArray();
		String newPatentID = patentID;
		if(patentID.matches(".*[A-Z]{1}")){
			newPatentID=patentID.substring(0, patentID.length()-1);
		}
		else{
			if(patentID.matches(".*[A-Z]{1}[1-9]{1}")){
				newPatentID=patentID.substring(0,patentID.length()-2);
			}
		}
		return newPatentID;
	}

	public static List<String> createPatentIDPossibilities (String patentID) throws ParseException{
		List<String> patentIDs=new ArrayList<>();
		patentIDs.add(patentID);//the patentID itself
		String newPatentID=OPSUtils.deleteSectionNumbers(patentID);//if patentID has section letters, they will be deleted
		if (!patentIDs.contains(newPatentID)){//only add if no exist
			patentIDs.add(newPatentID);
		}
		newPatentID=OPSUtils.deleteChar0(newPatentID, 0);//uses the previous transformation and delete the central 0.
		if (!patentIDs.contains(newPatentID)){
			patentIDs.add(newPatentID);
		}
		if(OPSUtils.verifyYearPresence(newPatentID)){//last transformation. with previous two transformations, the year is converted for two numbers type
			newPatentID=OPSUtils.transformYear(newPatentID);
			String newPatOnlyWithouYear=OPSUtils.transformYear(patentID);//year transformation only 
			patentIDs.add(newPatentID);
			if (!patentIDs.contains(newPatOnlyWithouYear)){
				patentIDs.add(newPatOnlyWithouYear);
			}
		}
		newPatentID=OPSUtils.deleteChar0(newPatentID, -1);//for some cases there are only 5five numbers after 0 and not 6 (WO1995006739A1) normally associated with old years
		if (!patentIDs.contains(newPatentID)){
			patentIDs.add(newPatentID);
		}
		newPatentID=OPSUtils.deleteChar0(patentID, -1);//special case with five numbers after 0 without year association
		if (!patentIDs.contains(newPatentID)){
			patentIDs.add(newPatentID);
		}
		int lettersOfSection = OPSUtils.verifySectionNumbers(patentID);
		newPatentID=OPSUtils.deleteChar0(patentID, lettersOfSection);//delete central 0 transformation only 
		if (!patentIDs.contains(newPatentID)){
			patentIDs.add(newPatentID);
		}
		return patentIDs;
	}

}
