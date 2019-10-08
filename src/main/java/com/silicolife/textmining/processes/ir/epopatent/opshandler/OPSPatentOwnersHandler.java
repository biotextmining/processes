package com.silicolife.textmining.processes.ir.epopatent.opshandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.silicolife.textmining.processes.ir.epopatent.OPSUtils;
import com.silicolife.textmining.utils.http.ResponseHandler;
import com.silicolife.textmining.utils.http.exceptions.ResponseHandlingException;

public class OPSPatentOwnersHandler implements ResponseHandler<String>{


	public OPSPatentOwnersHandler()
	{

	}

	@Override
	public String buildResponse(InputStream response, String responseMessage,Map<String, List<String>> headerFields, int status)throws ResponseHandlingException {
		try {
			Document doc = OPSUtils.createJDOMDocument(response);
			NodeList extchangeNode = doc.getElementsByTagName("exchange-document");
			if(extchangeNode.getLength() > 0)
			{
				String ownders =  getOwnersInfo(extchangeNode.item(0));
				String authors = getAuthors(extchangeNode.item(0));
				return ownders + " " + authors;
			}
			else
			{
				return new String();
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new String();
	}

	private String getOwnersInfo(Node item) {
		String ownres = getApplicants (item);
		return ownres;
	}



	private String getApplicants (Node item){//method to return applicants from patent bibliographic data
		String applicants = new String();
		Node bibliographicDate = item.getFirstChild();
		NodeList bibliographicDateChilds = bibliographicDate.getChildNodes();
		for(int i=0;i<bibliographicDateChilds.getLength();i++)
		{
			Node bibliographicDateChild = bibliographicDateChilds.item(i);
			String nodeNAme = bibliographicDateChild.getNodeName();
			if(nodeNAme.equals("parties"))
			{
				Node partiesNode = bibliographicDateChild;
				NodeList partiesChilds = partiesNode.getChildNodes();
				for(int j=0;j<partiesChilds.getLength();j++)
				{
					Node partiesChild = partiesChilds.item(j);
					String partiesChildNodeName = partiesChild.getNodeName();
					if(partiesChildNodeName.equals("applicants"))
					{
						Node applicnts = partiesChild;
						NodeList applicantList = applicnts.getChildNodes();
						for(int k=0;k<applicantList.getLength();k++)
						{
							Node applicant = applicantList.item(k);
							String inventorType = applicant.getAttributes().getNamedItem("data-format").getNodeValue();
							if(inventorType.equals("epodoc"))
							{
								applicants= applicants + applicant.getTextContent() + ", ";
							}
						}
						if(applicants.isEmpty())
							return applicants;
						return applicants.substring(0, applicants.length()-2);
					}
				}
			}	
		}
		return applicants;
	}
	
	private String getAuthors(Node item) {
		String authors = new String();
		Node bibliographicDate = item.getFirstChild();
		NodeList bibliographicDateChilds = bibliographicDate.getChildNodes();
		for(int i=0;i<bibliographicDateChilds.getLength();i++)
		{
			Node bibliographicDateChild = bibliographicDateChilds.item(i);
			String nodeNAme = bibliographicDateChild.getNodeName();
			if(nodeNAme.equals("parties"))
			{
				Node partiesNode = bibliographicDateChild;
				NodeList partiesChilds = partiesNode.getChildNodes();
				for(int j=0;j<partiesChilds.getLength();j++)
				{
					Node partiesChild = partiesChilds.item(j);
					String partiesChildNodeName = partiesChild.getNodeName();
					if(partiesChildNodeName.equals("inventors"))
					{
						Node invetors = partiesChild;
						NodeList inventorList = invetors.getChildNodes();
						for(int k=0;k<inventorList.getLength();k++)
						{
							Node inventor = inventorList.item(k);
							String inventorType = inventor.getAttributes().getNamedItem("data-format").getNodeValue();
							if(inventorType.equals("epodoc"))
							{
								authors = authors + inventor.getTextContent() + ", ";
							}
						}
						return authors.substring(0, authors.length()-2);
					}
				}
			}	
		}
		return authors;
	}

}
