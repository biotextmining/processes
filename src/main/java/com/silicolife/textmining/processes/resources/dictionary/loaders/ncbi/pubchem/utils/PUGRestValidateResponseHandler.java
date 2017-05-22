package com.silicolife.textmining.processes.resources.dictionary.loaders.ncbi.pubchem.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import com.silicolife.textmining.utils.http.ResponseHandler;
import com.silicolife.textmining.utils.http.exceptions.ResponseHandlingException;

public class PUGRestValidateResponseHandler implements ResponseHandler<String>{


	public PUGRestValidateResponseHandler() {
	}

	@Override
	public String buildResponse(InputStream response, String responseMessage,
			Map<String, List<String>> headerFields, int status) throws ResponseHandlingException {
		String message = new String();
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();//Using sax parser in order to read inputstream.
			SAXParser sp = spf.newSAXParser();
			PUGRestValidateResponseParser parseEventsHandler = new PUGRestValidateResponseParser();
			sp.parse(response,parseEventsHandler);
			message = parseEventsHandler.getMessage();

		} catch (SAXException | IOException | ParserConfigurationException e) {
		}
		return message;

	}

}
