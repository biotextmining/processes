package com.silicolife.textmining.processes.corpora.loaders.pdf.utils;

import java.util.List;

import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationSourcesDefault;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.IPublicationEditable;
import com.silicolife.textmining.core.interfaces.process.IR.exception.InternetConnectionProblemException;
import com.silicolife.textmining.processes.ir.pubmed.newstretegy.utils.NewPMSearch;

public class PMIDSearch {

	public static void getPublicationByPMID(IPublicationEditable pub) throws InternetConnectionProblemException {
		String PMID = PublicationImpl.getPublicationExternalIDForSource(pub, PublicationSourcesDefault.pubmed);
		List<IPublication> list = NewPMSearch.getPublicationByQuery(PMID+"[uid]");
		if(list!=null && !list.isEmpty())
			updatePublicationInformation(pub,list);
	}

	
	private static void updatePublicationInformation(IPublicationEditable pub,
			List<IPublication> list) {
		IPublication pm = list.get(0);
		if(!pm.getTitle().isEmpty())
			pub.setTitle(pm.getTitle());
		if(!pm.getAuthors().isEmpty())
			pub.setAuthors(pm.getAuthors());
		if(!pm.getAbstractSection().isEmpty())
			pub.setAbstract(pm.getAbstractSection());
		if(!pm.getJournal().isEmpty())
			pub.setJournal(pm.getJournal());
		if(!pm.getYeardate().isEmpty())
			pub.setYearDate(pm.getYeardate());
	}
}
