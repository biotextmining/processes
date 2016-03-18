package com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.input;

public interface InputFactory {
	public DocumentIterator parse(String file);
	public DocumentIterator parse(java.io.File file);
	public DocumentIterator parse(StringBuffer data);
}
