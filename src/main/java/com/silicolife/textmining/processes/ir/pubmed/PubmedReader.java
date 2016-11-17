package com.silicolife.textmining.processes.ir.pubmed;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.processes.ir.pubmed.reader.MedLineReader;
import com.silicolife.textmining.processes.ir.pubmed.reader.PubmedEbookReader;


public class PubmedReader {

	public static final String pubmedLink = "https://www.ncbi.nlm.nih.gov/pubmed/";

	public PubmedReader(){
	}


	public List<IPublication> getPublications(InputStream stream) throws ANoteException{
		List<IPublication> publicationsResult = new ArrayList<>();	
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
			publicationsResult.addAll(new MedLineReader().getPublications(doc));
			publicationsResult.addAll(new PubmedEbookReader().getPublications(doc));
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new ANoteException(e);
		}		
		return publicationsResult;
	}

	
}
