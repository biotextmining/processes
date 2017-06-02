package com.silicolife.textmining.processes.corpora.readers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;

import com.silicolife.textmining.core.datastructures.documents.PublicationExternalSourceLinkImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationSourcesDefaultEnum;
import com.silicolife.textmining.core.datastructures.textprocessing.NormalizationForm;
import com.silicolife.textmining.core.datastructures.utils.FileHandling;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.IPublicationExternalSourceLink;
import com.silicolife.textmining.core.interfaces.core.document.labels.IPublicationLabel;
import com.silicolife.textmining.core.interfaces.core.document.structure.IPublicationField;

/**
 * Parser Meta and Full text information from USPTO Dynamic IR project ( Files .meta and .txt)
 * 
 * 
 * @author Hugo Costa
 *
 */
public class PatentMetaFilesReader {
	
	public List<IPublication> getPatent(InputStream inStream,File metFileName) throws ANoteException{
		try {
			List<IPublication> out = new ArrayList<>();
			IPublication pub = PublicaitonMetaInfomration(inStream);
			String fulltextFilepath = metFileName.getParentFile().getAbsolutePath() + "/" + FilenameUtils.removeExtension(metFileName.getName())+ ".txt";
			File fulltextFilepathFile = new File(fulltextFilepath);
			String fullTextContent = FileHandling.getFileContent(fulltextFilepathFile);
			pub.setFullTextContent(NormalizationForm.removeOffsetProblemSituation(fullTextContent));
			out.add(pub);
			inStream.close();
			return out;
		} catch (IOException e) {
			throw new ANoteException(e);
		}
	}

	private IPublication PublicaitonMetaInfomration(InputStream inStream) throws IOException {
		Properties prop = new Properties();
		prop.load(inStream);
		String patentUsptoID = prop.getProperty("PatentID");
		String otherPatentIds = prop.getProperty("OtherPatentIDs");
		boolean freeFullText = false;
		List<IPublicationExternalSourceLink> publicationExternalIDSource = new ArrayList<>();
		publicationExternalIDSource.add(new PublicationExternalSourceLinkImpl(patentUsptoID, PublicationSourcesDefaultEnum.patent.toString()));
		publicationExternalIDSource.add(new PublicationExternalSourceLinkImpl(patentUsptoID, PublicationSourcesDefaultEnum.uspto.toString()));
		String[] otherpatentIDs = otherPatentIds.split(",");
		for(String otherpatentID:otherpatentIDs)
		{
			if(!otherpatentID.trim().isEmpty())
				publicationExternalIDSource.add(new PublicationExternalSourceLinkImpl(otherpatentID.trim(), PublicationSourcesDefaultEnum.patent.toString()));

		}
		String authors = prop.getProperty("Authors");
		String externalLink = prop.getProperty("Link");
		String journal = "";
		String relativePath = null;
		String volume = "";
		String fulldate = prop.getProperty("Date");
		String issue  = null;
		String abstractSection = prop.getProperty("Abstract");		
		List<IPublicationLabel> publicationLabels = new ArrayList<>();
		String pages = "";
		String type = "Patent";
		String status = "";
		String title = prop.getProperty("Title");	
		String yeardate = prop.getProperty("Date").substring(0,4);
		List<IPublicationField> publicationFields = new ArrayList<>();
		String notes = new String();
		if(prop.getProperty("Owners")!=null && !prop.getProperty("Owners").isEmpty())
		{
			authors = authors + " Owners: "+prop.getProperty("Owners");
		}
		if(prop.getProperty("Classification")!=null && !prop.getProperty("Classification").isEmpty())
		{
			notes = "Classification :"+prop.getProperty("Classification");

		}
		IPublication pub = new PublicationImpl(title , authors , type , yeardate , fulldate , status , journal , volume , issue, pages , abstractSection , externalLink , freeFullText , notes, relativePath , publicationExternalIDSource , publicationFields , publicationLabels);
		return pub;
	}
	
	

}
