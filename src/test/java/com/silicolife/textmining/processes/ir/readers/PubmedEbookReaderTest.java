package com.silicolife.textmining.processes.ir.readers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.processes.ir.pubmed.reader.PubmedEbookReader;

public class PubmedEbookReaderTest {

	@Test
	public void test() throws SAXException, IOException, ParserConfigurationException, ANoteException {
		File ebookxml = new File("src/test/resources/pubmed/book/books.xml");
		InputStream impustrem = new FileInputStream(ebookxml);
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(impustrem );
		List<IPublication> publictions = new PubmedEbookReader().getPublications(doc );
		for(IPublication publiction:publictions)
			System.out.println(publiction.toString());
	}

}
