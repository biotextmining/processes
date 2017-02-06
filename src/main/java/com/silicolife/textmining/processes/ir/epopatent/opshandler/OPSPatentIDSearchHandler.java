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

import com.silicolife.textmining.processes.ir.epopatent.OPSUtils;
import com.silicolife.textmining.utils.http.ResponseHandler;
import com.silicolife.textmining.utils.http.exceptions.ResponseHandlingException;

public class OPSPatentIDSearchHandler implements ResponseHandler<Set<String>>{

	
	public OPSPatentIDSearchHandler()
	{
	}

	@Override
	public Set<String> buildResponse(InputStream response, String responseMessage,Map<String, List<String>> headerFields, int status)throws ResponseHandlingException {
		Set<String> result = new HashSet<>();
		try {
			Document doc = OPSUtils.createJDOMDocument(response);
			NodeList extchangeNode = doc.getElementsByTagName("exchange-document");
			for(int i=0;i<extchangeNode.getLength();i++)
			{
				String patentID = parserDocument(extchangeNode.item(i));
				result.add(patentID);
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return result;
	}

	private String parserDocument(Node item) {
		String epodocID = getEpoDoc(item);
		return epodocID;
	}
	
	private String getEpoDoc(Node item) {
		String epoDoc = new String();
		Node bibliographicDate = item.getFirstChild();
		Node publicationReference = bibliographicDate.getFirstChild();
		NodeList documentids = publicationReference.getChildNodes();
		for(int i=0;i<documentids.getLength();i++)
		{
			Node documentid = documentids.item(i);
			String documentIDType = documentid.getAttributes().getNamedItem("document-id-type").getNodeValue();
			if(documentIDType.equals("epodoc"))
			{
				NodeList docNumberChild = documentid.getChildNodes();
				for(int j=0;j<docNumberChild.getLength();j++)
				{
					Node documentidChild = docNumberChild.item(j);
					String nodeName = documentidChild.getNodeName();
					if(nodeName.equals("doc-number"))
					{
						return documentidChild.getTextContent();
					}
				}
			}
		}
		return epoDoc;
	}

}
