package com.silicolife.textmining.processes.ir;

import java.io.File;

import org.junit.Test;

import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.processes.ir.pubmed.PMCReader;

public class PMCReaderTest {

	@Test
	public void test() throws ANoteException {
		File nxml = new File("src//test//resources//pmc//testenxml.xml");
		PMCReader pmcreader = new PMCReader();
		IPublication pub = pmcreader.getPublications(nxml);
		System.out.println(PublicationImpl.getPublicationExternalIDsStream(pub));
	}

}
