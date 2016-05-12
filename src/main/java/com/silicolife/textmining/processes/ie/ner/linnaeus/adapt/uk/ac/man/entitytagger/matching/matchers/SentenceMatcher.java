package com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.entitytagger.matching.matchers;

import java.util.LinkedList;
import java.util.List;

import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.martin.common.Pair;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.martin.common.SentenceSplitter;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.dataholders.Document;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.entitytagger.Mention;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.entitytagger.matching.Matcher;

public class SentenceMatcher extends Matcher {

	@Override
	public List<Mention> match(String text, Document doc) {
		
		SentenceSplitter sp = new SentenceSplitter(text);
		List<Mention> aux = new LinkedList<Mention>();
		String docID = doc != null ? doc.getID() : null;
		
		int i = 0;
		
		for (Pair<Integer> coords : sp){
		
			int s = coords.getX();
			int e = coords.getY();
			String t = text.substring(s,e);
		
			Mention m = new Mention("sentence:" + i++,s,e,t);
			m.setDocid(docID);
			
			aux.add(m);
		}
		
		return aux;
	}
	

}
