package com.silicolife.textmining.processes.corpora.loaders;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;

import com.silicolife.textmining.core.datastructures.documents.PublicationExternalSourceLinkImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationSourcesDefault;
import com.silicolife.textmining.core.interfaces.core.corpora.loaders.ICorpusPDFDirectory;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.IPublicationExternalSourceLink;
import com.silicolife.textmining.core.interfaces.core.document.labels.IPublicationLabel;
import com.silicolife.textmining.core.interfaces.core.document.structure.IPublicationField;
import com.silicolife.textmining.processes.corpora.loaders.pdf.utils.CorpusLoadersUtils;

public class PDFDirectoryCorpusLoaders implements ICorpusPDFDirectory{

	private boolean cancel = false;
	private static String pdf = "pdf";
	
	@Override
	public List<IPublication> processFile(File fileOrDirectory,Properties properties)
			throws ANoteException, IOException{
		List<IPublication> publications = new ArrayList<>();
		if(validateFile(fileOrDirectory))
		{
			File[] trueFiles = CorpusLoadersUtils.getFilesByType(fileOrDirectory, pdf);
			for(int i=0;i<trueFiles.length &&!cancel;i++)
			{
				File pdfFile = trueFiles[i];
				if(pdfFile.isFile())
				{					
					List<IPublicationExternalSourceLink> publicationExternalIDSource = new ArrayList<IPublicationExternalSourceLink>();
					List<IPublicationField> publicationFields = new ArrayList<>();
					List<IPublicationLabel> publicationLabels = new ArrayList<>();
					IPublication document =  new PublicationImpl(
							"", "", "", "", "",
							"", "", "", "", "", "",
							"", false, "", "",
							publicationExternalIDSource ,
							publicationFields ,
							publicationLabels );
					// Name as otherID/PMID
					if(properties!=null &&
							properties.containsKey(CorpusLoadersUtils.useNameAsOtherID) &&
							Boolean.valueOf(properties.getProperty(CorpusLoadersUtils.useNameAsOtherID)))
					{
						String filenameWithouExtention = FilenameUtils.removeExtension(pdfFile.getName());
						IPublicationExternalSourceLink arg0 = new PublicationExternalSourceLinkImpl(filenameWithouExtention, PublicationSourcesDefault.pubmed);
						publicationExternalIDSource.add(arg0);
					}
					if(properties!=null &&
							properties.containsKey(CorpusLoadersUtils.useMapBetweenFilePathAndOtherID))
					{
						Map<String,String> map = (Map<String, String>) properties.get(CorpusLoadersUtils.useMapBetweenFilePathAndOtherID);
						String pmid  = new String();
						if(map.containsKey(pdfFile.getName()))
						{
							pmid = map.get(pdfFile.getName());
						}
						else if(map.containsKey(pdfFile.getAbsolutePath().replace("\\", "/")))
						{
							pmid = map.get(pdfFile.getAbsolutePath().replace("\\", "/"));
						}
						IPublicationExternalSourceLink arg0 = new PublicationExternalSourceLinkImpl(pmid, PublicationSourcesDefault.pubmed);
						publicationExternalIDSource.add(arg0);
					}
					document.setSourceURL(pdfFile.getAbsolutePath());
					publications.add(document);
				}
			}
		}
		return publications;
	}

	@Override
	public boolean validateFile(File fileOrDirectory) {
		if(fileOrDirectory.isDirectory())
		{
			int countPDFFiles = CorpusLoadersUtils.getNumberPublicationByType(fileOrDirectory,pdf);
			if(countPDFFiles>0)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}

	
	

	@Override
	public void stop() {
		cancel =  true;
		
	}

	
	


}
