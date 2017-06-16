package com.silicolife.textmining.processes.resources.dictionary.loaders.ncbi.pubchem.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.silicolife.textmining.utils.http.ResponseHandler;
import com.silicolife.textmining.utils.http.exceptions.ResponseHandlingException;

public class PUGRestPubChemIDHandler implements ResponseHandler<List<String>>{


	public PUGRestPubChemIDHandler() {
	}


	@Override
	public List<String> buildResponse(InputStream response, String responseMessage,
			Map<String, List<String>> headerFields, int status) throws ResponseHandlingException {
		try {
			List<String> out = new ArrayList<>(); 
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(response);
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("PC-CompoundType_id_cid");
			for(int i=0;i<nList.getLength();i++)
			{
				Node item = nList.item(i);
				if(item!=null)
				{
					String cid = item.getTextContent();
					out.add(cid);
				}
			}
			return out;
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new ResponseHandlingException(e);
		}


	}

}
