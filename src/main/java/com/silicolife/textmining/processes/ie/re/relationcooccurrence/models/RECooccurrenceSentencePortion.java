package com.silicolife.textmining.processes.ie.re.relationcooccurrence.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.silicolife.textmining.core.datastructures.annotation.re.EventAnnotationImpl;
import com.silicolife.textmining.core.datastructures.annotation.re.EventPropertiesImpl;
import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.annotation.IEventAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IAnnotatedDocument;
import com.silicolife.textmining.core.interfaces.core.document.structure.ISentence;
import com.silicolife.textmining.processes.ie.re.relationcooccurrence.RECooccurrence;

public class RECooccurrenceSentencePortion implements IRECooccurrenceSentenceModel{

	public RECooccurrenceSentencePortion()
	{
		
	}

	public List<IEventAnnotation> processDocumetAnnotations(IAnnotatedDocument annotationdDocument,List<IEntityAnnotation> listEntitiesSortedByOffset) throws ANoteException {
		List<ISentence> list;
		try {
			list = RECooccurrence.getSentencesLimits(annotationdDocument);
		} catch (IOException e) {
			throw new ANoteException(e);
		}
		return getRelations(listEntitiesSortedByOffset, list);
	}
	
	private List<IEventAnnotation> getRelations(List<IEntityAnnotation> listEntitiesSortedByOffset, List<ISentence> list) {
		List<IEventAnnotation> events = new ArrayList<IEventAnnotation>();
		for(ISentence sentence:list)
		{
			List<IEntityAnnotation> listEntitiesSentenceOrderOffset = RECooccurrence.getSentenceEntties(listEntitiesSortedByOffset,sentence);;
			for(int i=0;i<listEntitiesSentenceOrderOffset.size();i++)
			{
				if(i+1>=listEntitiesSentenceOrderOffset.size())
				{

				}
				else
				{
					IEntityAnnotation entLeft1 = listEntitiesSentenceOrderOffset.get(i);
					for(int j=i+1;j<listEntitiesSentenceOrderOffset.size();j++)
					{
						IEntityAnnotation entrifgt1 = listEntitiesSentenceOrderOffset.get(j);
						if(!entLeft1.equals(entrifgt1))
						{
							List<IEntityAnnotation> entrifgtList = new ArrayList<>();
							entrifgtList.add(entrifgt1);
							List<IEntityAnnotation> entLeftList = new ArrayList<>();
							entLeftList.add(entLeft1);

							IEventAnnotation ev = new EventAnnotationImpl(sentence.getStartOffset(), sentence.getStartOffset(),"", entLeftList, entrifgtList, "", -1, "", new EventPropertiesImpl());
							events.add(ev);
						}
					}
				}
			}
		}
		return events;
	}
	
	public String getDescription(){
		return "Mix Entity Pairs Sentence";
	}
	
	public String getImagePath() {
		return "icons/recoocorrence_portion.png";
	}

	public String toString(){
		return "Mix Entity Pairs Sentence";
	}

	public String getUID() {
		return getDescription();
	}
	

}
