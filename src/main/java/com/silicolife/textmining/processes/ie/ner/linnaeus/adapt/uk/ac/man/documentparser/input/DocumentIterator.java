package com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.input;

import java.util.Iterator;

import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.dataholders.Document;

public interface DocumentIterator extends Iterator<Document>, Iterable<Document>{
	public void skip();
}
