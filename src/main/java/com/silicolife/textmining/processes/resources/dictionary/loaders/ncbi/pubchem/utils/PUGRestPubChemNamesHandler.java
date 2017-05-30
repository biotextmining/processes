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

public class PUGRestPubChemNamesHandler implements ResponseHandler<List<String>>{


	public PUGRestPubChemNamesHandler() {
	}


	@Override
	public List<String> buildResponse(InputStream response, String responseMessage,
			Map<String, List<String>> headerFields, int status) throws ResponseHandlingException {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(response);
			doc.getDocumentElement().normalize();
			List<String> out = new ArrayList<>();
			NodeList nList = doc.getElementsByTagName("Synonym");
			for(int i=0;i<nList.getLength();i++)
			{
				Node item = nList.item(i);
				out.add(item.getTextContent().trim());
			}
			return out;
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new ResponseHandlingException(e);
		}


	}

}
