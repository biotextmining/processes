package com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.martin.common.ArgParser;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.martin.common.Misc;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.martin.common.MyConnection;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.dataholders.Document;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.input.BMCFactory;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.input.BMCXMLFactory;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.input.DatabaseIterator;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.input.DatabaseListIterator;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.input.Directory;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.input.DocumentIterator;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.input.ElsevierFactory;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.input.IDIterator;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.input.InputFactory;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.input.MedlineIndexFactory;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.input.MedlinePMCIndexFactory;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.input.OTMI;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.input.OTMIFactory;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.input.PMCAbstract;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.input.PMCFactory;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.input.PMCIndexFactory;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.input.TextFile;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.input.TextFileFactory;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.input.util.CleanUnicode;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.input.util.DocumentBuffer;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.input.util.Skipper;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.input.util.Splitter;

public class DocumentParser {



	/**
	 * @param ap an ArgParser object, containing user-supplied arguments used to determine how to load the documents
	 * @param validIDs if specified (i.e. not null), will restrict the iterator to only return documents contained in validIDs
	 * @return an iterator used for iterating over a set of documents
	 */
	public static DocumentIterator getDocuments(ArgParser ap){
		return getDocuments(ap, null);
	}

	/**
	 * @param ap an ArgParser object, containing user-supplied arguments used to determine how to load the documents
	 * @param validIDs if specified (i.e. not null), will restrict the iterator to only return documents contained in validIDs
	 * @param logger logging object used for performing logging (may be null). 
	 * @return an iterator used for iterating over a set of documents
	 */
	public static DocumentIterator getDocuments(ArgParser ap, Logger logger){
		String[] dtds = ap.gets("dtd");
		DocumentIterator documents = null;

		if (ap.containsKey("pmcAbs")){
			//not really used
			File medlineBaseDir = ap.getFile("medlineBaseDir");
			File medlineIndexFile = ap.getFile("medlineIndex");
			DocumentIterator medlineDocs = medlineIndexFile !=null ? new MedlineIndexFactory(medlineBaseDir,null).parse(medlineIndexFile) : null;

			File pmcBaseDir = ap.getFile("pmcBaseDir");
			File pmcIndexFile = ap.getFile("pmcIndex");
			DocumentIterator pmcDocs = pmcIndexFile != null ? new PMCIndexFactory(pmcBaseDir,dtds).parse(pmcIndexFile) : null;

			documents = new PMCAbstract(pmcDocs, medlineDocs);
		} else if (ap.containsKey("medlineIndex")){
			//MEDLINE XML, specified by a special index file (see documentation for format)
			File medlineBaseDir = ap.getFile("medlineBaseDir");
			File indexFile = ap.getFile("medlineIndex");

			documents = new MedlineIndexFactory(medlineBaseDir,null).parse(indexFile);
		} else if (ap.containsKey("medlinePMCIndex")){
			//combines a PMC and MEDLINE repository, returning documents
			//with data from both

			File medlineBaseDir = ap.getFile("medlineBaseDir");
			File pmcBaseDir = ap.getFile("pmcBaseDir");
			File indexFile = ap.getFile("medlinePMCIndex");

			documents = new MedlinePMCIndexFactory(medlineBaseDir,pmcBaseDir,dtds,null).parse(indexFile);
		} else if (ap.containsKey("pmcIndex")){
			//PMC XML, specified by a special index file (see documentation for format)
			File pmcBaseDir = ap.getFile("pmcBaseDir");
			File indexFile = ap.getFile("pmcIndex");

			documents = new PMCIndexFactory(pmcBaseDir,dtds).parse(indexFile);
		} else if (ap.containsKey("pmcDir")){
			//Directory containing PMC .xml files
			InputFactory pmcFactory = new PMCFactory(dtds);
			documents = new Directory(ap.getFile("pmcDir"),pmcFactory,"xml", ap.containsKey("recursive"));
		} else if (ap.containsKey("pmc")){
			//Directory containing PMC .xml files
			InputFactory pmcFactory = new PMCFactory(dtds);
			documents = pmcFactory.parse(ap.getFile("pmc"));
		} else if (ap.containsKey("OTMI")){
			//OTMI XML file
			documents = new OTMI(ap.getFile("OTMI"));
		} else if (ap.containsKey("OTMIDir")){
			//Directory containing OTMI XML files
			documents = new Directory(ap.getFile("OTMIDir"),new OTMIFactory(),".otmi", ap.containsKey("recursive"));
		} else if (ap.containsKey("text")){
			//plain text-file
			return new TextFile(ap.getFiles("text"));
		} else if (ap.containsKey("textDir")){
			//directory containing plain text files
			documents = new Directory(ap.getFile("textDir"),new TextFileFactory(),".txt",ap.containsKey("recursive"));
		} else if (ap.containsKey("bmcxml")){
			//BMC XML file
			documents = new BMCXMLFactory().parse(ap.getFile("bmcxml"));
		} else if (ap.containsKey("bmcxmlDir")){
			//Directory containing BMC XML files
			documents = new Directory(ap.getFile("bmcxmlDir"),new BMCXMLFactory(),".xml",ap.containsKey("recursive"));
		} else if (ap.containsKey("bmcDir")){
			//Directory containing BMC XML files, alternative parsing
			documents = new Directory(ap.getFile("bmcDir"),new BMCFactory(dtds),".xml",ap.containsKey("recursive"));
		} else if (ap.containsKey("databaseDocs")){
			//Reads documents from a MySQL database
			MyConnection conn = com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.martin.common.SQL.connectMySQL2(ap, logger, "articles");
			documents = new DatabaseIterator(conn, ap.get("databaseDocs"), ap.containsKey("full"), ap.get("skipDocIdsQuery"));
		} else if (ap.containsKey("databaseList")){
			
			if (ap.gets("databaseList").length != 2)
				throw new IllegalStateException("Usage: --databaseList <table> <file with docids>");
			
			MyConnection conn = com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.martin.common.SQL.connectMySQL2(ap, logger, "articles");
			
			List<String> ids = new ArrayList<String>();
			ids.addAll(Misc.loadStringSetFromFile(ap.getFiles("databaseList")[1]));
			
			documents = new DatabaseListIterator(conn, ap.gets("databaseList")[0], ids, ap.containsKey("full"));
		} else if (ap.containsKey("elsevierDir")){
			//Directory containing Elsevier .xml files
			InputFactory factory = new ElsevierFactory(dtds);
			documents = new Directory(ap.getFile("elsevierDir"),factory,"xml", ap.containsKey("recursive"));
		} else if (ap.containsKey("idsOnly")){
			File f = ap.getFile("idsOnly");
			documents = new IDIterator(f);
		}

		if (ap.containsKey("buffer"))
			documents = new DocumentBuffer(documents, ap.getInt("buffer", 250), logger);

		if (documents != null && ap.containsKey("skip")){
			if (logger != null)
				logger.info("%t: Skipping " + ap.getInt("skip") + " documents...\n");
			for (int i = 0; i < ap.getInt("skip"); i++)
				documents.skip();		
			if (logger != null)
				logger.info("%t: Skip complete.\n");
		}

		if (documents != null && ap.containsKey("skipEvery")){
			if (logger != null)
				logger.info("%t: Will be skipping " + ap.getInt("skipEvery") + " documents for each processed document.\n");
			documents = new Skipper(documents, ap.getInt("skipEvery"));
		}
		
		if (documents != null && ap.containsKey("cleanUnicode")){
			if (logger != null)
				logger.info("%t: Removing high unicode characters from documents.");
			documents = new CleanUnicode(documents);
		}

		if (documents != null && ap.getInt("split",0) > 0){
			if (logger != null)
				logger.info("%t: Splitting all documents at " + ap.getInt("split") + " sentencens.");
			documents = new Splitter(documents, ap.getInt("split"));
		}

		return documents;		
	}

	public static String getDocumentHelpMessage() {
		return "[--medlineIndex <file> --medlineBaseDir <dir>]\n" +
		"[--medlinePMCIndex <file> --medlineBaseDir <dir> --pmcBaseDir <dir> --dtd <files>]\n" +
		"[--pmcIndex <file> --pmcBaseDir<dir> --dtd <files>]\n" +
		"[--textDir <dir> [--recursive]]\n"+
		"[--OTMIDir <dir> [--recursive]]\n";
	}

	public static Map<String, Document> getDocumentsToHash(ArgParser ap) {
		Map<String,Document> aux = new HashMap<String,Document>();
		DocumentIterator documents = getDocuments(ap);
		for (Document d : documents)
			aux.put(d.getID(),d);
		return aux;
	}
}
