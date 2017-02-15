package com.silicolife.textmining.processes.ir;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.processes.ir.pubmed.reader.PMCReader;

public class PMCReaderTest {

	@Test
	public void test() throws ANoteException {
		File nxml = new File("src//test//resources//pmc//testenxml.xml");
		PMCReader pmcreader = new PMCReader();
		List<IPublication> pubs = pmcreader.getPublications(nxml);
		for(IPublication pub : pubs)
		{
			System.out.println(pub.getTitle());
			System.out.println(pub.getAbstractSection());
			System.out.println(pub.getFullTextContent());

		}
	}

}
