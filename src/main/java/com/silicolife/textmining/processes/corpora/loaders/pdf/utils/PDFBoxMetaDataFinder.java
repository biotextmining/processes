package com.silicolife.textmining.processes.corpora.loaders.pdf.utils;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.util.PDFTextStripper;

import com.silicolife.textmining.core.datastructures.documents.PublicationExternalSourceLinkImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationSourcesDefaultEnum;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.IPublicationEditable;
import com.silicolife.textmining.core.interfaces.core.document.IPublicationExternalSourceLink;
import com.silicolife.textmining.core.interfaces.process.IR.exception.InternetConnectionProblemException;
import com.silicolife.textmining.processes.ir.pubmed.utils.PMSearch;

public class PDFBoxMetaDataFinder {
	
	private static Pattern testNumbersintheend = Pattern.compile("\\d+.{1,}\\d+$");
	private static Pattern doiFinder = Pattern.compile("(10[.][0-9]{4,}(?:[.][0-9]+)*\\/(?:(?![\"&\'<>])\\S)+)");
	private static Pattern endsDigit = Pattern.compile("[0-9]$");
	
	/**
	 * Get Publication Meta Information using heuristics and searching in Pubmed using DOI retrieved
	 * 
	 * @return
	 * @throws InternetConnectionProblemException
	 * @throws IOException
	 */
	public static void getPublicationMetaInformation(IPublicationEditable pubEdit) throws InternetConnectionProblemException, IOException
	{
		updatePublicationUsingHeuristics(pubEdit);
		String doi = PublicationImpl.getPublicationExternalIDForSource(pubEdit, PublicationSourcesDefaultEnum.DOI.name());
		if(doi!=null && !doi.isEmpty())
		{
			List<IPublication> list = PMSearch.getPublicationByQuery(doi+"[doi] ");
			if(list!=null && !list.isEmpty())
			{
				updatePublicationInformation(pubEdit, list);
			}
			else
			{
				do {
					doi = doi.substring(0, doi.length()-1);
					list = PMSearch.getPublicationByQuery(doi+"[doi] ");
					if(list!=null && !list.isEmpty())
					{
						updatePublicationInformation(pubEdit, list);
						break;
					}
				} while (!doi.endsWith(".") && !doi.endsWith("/") && !endwithdigit(doi));
			}
		}
	}

	private static void updatePublicationUsingHeuristics(IPublicationEditable pub) throws IOException {
		String title = new String();
		String authors = new String();
		String journal = new String();;
		String abstractText = new String();;
		String doi = new String();;
		PDDocument doc = PDDocument.load(pub.getSourceURL());
		PDDocumentInformation docInfo = doc.getDocumentInformation();
		PDFTextStripper stripper = new PDFTextStripper();
		String text = stripper.getText(doc);
		title = docInfo.getTitle();
		if(title==null || title.isEmpty() || title.length() < 15 || title.startsWith("doi:")
				|| !finisherwithnumbers(title) || !havewords(title))
		{
			title = titlefind(doc);
		}
		pub.setTitle(title);
		authors = AuthorsHeuristics.getAuthorsByHeuristics(text);
		if(authors==null || authors.isEmpty() || authors.length() < 3)
		{
			authors = docInfo.getAuthor();
			if(docInfo.getAuthor()==null)
			{
				authors = new String();
			}
		}
		pub.setAuthors(authors);
		journal = docInfo.getSubject();
		if(journal==null || journal.isEmpty() || journal.length() < 3)
		{
			journal = new String();
		}
		pub.setJournal(journal);
		abstractText = docInfo.getCustomMetadataValue("Abstract");
		if(abstractText==null || abstractText.isEmpty()|| abstractText.length() < 10)
		{
			abstractText = new String();
		}
		pub.setAbstract(abstractText);
		doi = findingDOI(text);
		if(doi==null || doi.length() < 6)
		{
			doi = new String();
		}
		if(!doi.isEmpty())
		{
			IPublicationExternalSourceLink source = new PublicationExternalSourceLinkImpl(doi, PublicationSourcesDefaultEnum.DOI.name());
			pub.getPublicationExternalIDSource().add(source);
		}
		doc.close();
	}




	private static String titlefind(PDDocument doc) {
		String title = new String();
		PDDocumentOutline sout = doc.getDocumentCatalog().getDocumentOutline();
		if(sout!=null)
		{
			PDOutlineItem first = sout.getFirstChild();
			if(first!=null)
			{
				String label = first.getTitle();
				if(label!=null && !label.isEmpty() && label.length() > 10)
				{
					title = label;
				}
			}	
		}
		return title;
	}
	
	private static boolean finisherwithnumbers(String text)
	{
		Matcher m = testNumbersintheend.matcher(text);
		if(m.find())
			return false;		
		return true;
	}
	
	private static boolean havewords(String text)
	{
		if(text.split("\\s+").length == 1)
		{
			return false;
		}
		return true;
	}
	
	private static String findingDOI(String text) throws IOException
	{
		String result = null;
		Matcher m = doiFinder.matcher(text);
		if(m.find())
		{
			result =  text.substring(m.start(1),m.end(1)).replaceAll("\\n", "").replaceAll("\\r", "");
		}
		if(result!=null && (result.endsWith(".") || result.endsWith(",") || result.endsWith(",")|| result.endsWith(")") || result.endsWith(";"))) 
		{
			result = result.substring(0, result.length()-1);
		}
		if(result!=null)
		{
			result = result.replaceAll("[^\\x00-\\x80]+", "-");
			// Bioinformatics Journal
			if(result.endsWith("BIOINFORMATICS"))
				result = result.replaceAll("BIOINFORMATICS", "");
			// GAD
			if(result.endsWith("Access"))
				result = result.replaceAll("Access", "");
			// Other
			if(result.endsWith("ORIGINAL"))
				result = result.replaceAll("ORIGINAL", "");
			// Pnas
			if(result.contains("/pnas") && result.endsWith("/-/DCSupplemental"))
				result = result.replaceAll("/-/DCSupplemental", "");
				
		}
		return result;
	}

	private static void updatePublicationInformation(IPublicationEditable pub,
			List<IPublication> list) {
		IPublication pm = list.get(0);
		if(!pm.getTitle().isEmpty())
			pub.setTitle(pm.getTitle());
		if(!pm.getAuthors().isEmpty())
			pub.setAuthors(pm.getAuthors());
		if(!pm.getAbstractSection().isEmpty())
			pub.setAbstract(pm.getAbstractSection());
		if(!pm.getJournal().isEmpty())
			pub.setJournal(pm.getJournal());
		if(!pm.getYeardate().isEmpty())
			pub.setYearDate(pm.getYeardate());
	}
	
	private static boolean endwithdigit(String doi) {
		Matcher m = endsDigit.matcher(doi);
		if(m.find())
			return true;
		return false;
	}

}
