package com.silicolife.textmining.processes.nlptools.structure;

import com.silicolife.textmining.core.datastructures.documents.structure.TokenImpl;
import com.silicolife.textmining.core.interfaces.core.document.structure.ITextSegment;

public class TextSegments extends TokenImpl implements ITextSegment{

	public TextSegments(long startOffset, long endOffset, String text) {
		super(null,startOffset, endOffset, text);
	}
	
	public String toString()
	{
		return super.toString();
	}
	
}
