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

import com.silicolife.http.ResponseHandler;
import com.silicolife.http.exceptions.ResponseHandlingException;
import com.silicolife.textmining.core.datastructures.utils.GenericPairImpl;
import com.silicolife.textmining.processes.ir.epopatent.OPSUtils;

public class OPSPatentImageHandler implements
		ResponseHandler<GenericPairImpl<Integer, String>> {

	@Override
	public GenericPairImpl<Integer, String> buildResponse(InputStream response,String responseMessage, Map<String, List<String>> headerFields,int status) throws ResponseHandlingException {
		Document doc;
		try {
		//	String print = OPSUtils.readString(response);
	//	System.out.println(print);
			
			
			doc = OPSUtils.createJDOMDocument(response);
			
			NodeList documentInstanceNodeList = doc.getElementsByTagName("ops:document-instance");
			for(int i=0;i<documentInstanceNodeList.getLength();i++)
			{
				Node documentInstanceNode = documentInstanceNodeList.item(i);
				String description = documentInstanceNode.getAttributes().getNamedItem("desc").getNodeValue();
				if(description.equals("FullDocument"))
				{
					if(documentInstanceNode.getTextContent().contains("application/pdf"))
					{
						Integer numberOFpages = Integer.valueOf(documentInstanceNode.getAttributes().getNamedItem("number-of-pages").getNodeValue());
						String link = documentInstanceNode.getAttributes().getNamedItem("link").getNodeValue();
						return new GenericPairImpl<Integer, String>(numberOFpages, link);
					}
				}
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
