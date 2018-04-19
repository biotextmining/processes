package com.silicolife.textmining.processes.ir.epopatent.opshandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.silicolife.textmining.core.datastructures.documents.PublicationExternalSourceLinkImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationSourcesDefaultEnum;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.IPublicationExternalSourceLink;
import com.silicolife.textmining.processes.ir.epopatent.OPSUtils;
import com.silicolife.textmining.processes.ir.patentpipeline.PatentPipelineUtils;
import com.silicolife.textmining.utils.http.ResponseHandler;
import com.silicolife.textmining.utils.http.exceptions.ResponseHandlingException;

public class OPSPatentFamilyHandler implements ResponseHandler<String>{

	private IPublication publication;

	public OPSPatentFamilyHandler(IPublication publication)
	{
		this.publication=publication;	
	}

	@Override
	public String buildResponse(InputStream response, String responseMessage,Map<String, List<String>> headerFields, int status)throws ResponseHandlingException {
		try {
			Document doc = OPSUtils.createJDOMDocument(response);
			NodeList extchangeNode = doc.getElementsByTagName("ops:patent-family");
			if(extchangeNode.getLength() > 0)
			{
				updatePublication(extchangeNode.item(0));
			}
			else
			{
				return new String();
			}
		}catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new String();
	}

	
	private void updatePublication(Node item){
		Set<String> patentFamilySet = getPatentFamilyIds(item);
		if (!patentFamilySet.isEmpty()){
			for (String patentID:patentFamilySet){
				if (!verifyExternalIDSource(patentID)){
					IPublicationExternalSourceLink e = new PublicationExternalSourceLinkImpl(patentID, PublicationSourcesDefaultEnum.patent.name());
					publication.getPublicationExternalIDSource().add(e);	
				}
			}
		}
	}


	private boolean verifyExternalIDSource(String patentID){
		List<IPublicationExternalSourceLink> externalSources = publication.getPublicationExternalIDSource();
		for (IPublicationExternalSourceLink extSource:externalSources){
			if (extSource.getSourceInternalId().equalsIgnoreCase(patentID)){
				return true;
			}
		}
		return false;
	}


	private Set<String> getPatentFamilyIds(Node item) {
		Set<String> epoDocList = new HashSet<>();
		NodeList patentData = item.getChildNodes();

		for(int i=0;i<patentData.getLength();i++){
			Node patentFamily = patentData.item(i);

			if (patentFamily.getNodeName().equals("ops:family-member")){
				NodeList familyMembers=patentFamily.getChildNodes();

				for (int j=0;j<familyMembers.getLength();j++){
					Node publicationReference = familyMembers.item(j);

					if (publicationReference.getNodeName().equals("publication-reference")){
						NodeList node = publicationReference.getChildNodes();

						for (int k=0;k<node.getLength();k++){
							Node documentID = node.item(k);
							String documentIDType = documentID.getAttributes().getNamedItem("document-id-type").getNodeValue();

							if(documentIDType.equals("epodoc"))
							{
								NodeList docNumberChild = documentID.getChildNodes();

								for(int l=0;l<docNumberChild.getLength();l++)
								{
									Node documentidChild = docNumberChild.item(l);

									if(documentidChild.getNodeName().equals("doc-number"))
									{
										String candidatePatentID = documentidChild.getTextContent();
										candidatePatentID = PatentPipelineUtils.deleteSectionNumbers(candidatePatentID);
										epoDocList.add(candidatePatentID);
									}
								}
							}
						}
					}
				}
			}
		}
		return epoDocList;
	}

}