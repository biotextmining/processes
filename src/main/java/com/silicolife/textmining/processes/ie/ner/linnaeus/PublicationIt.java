package com.silicolife.textmining.processes.ie.ner.linnaeus;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.silicolife.textmining.core.datastructures.documents.AnnotatedDocumentImpl;
import com.silicolife.textmining.core.datastructures.language.LanguageProperties;
import com.silicolife.textmining.core.datastructures.textprocessing.TermSeparator;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalNames;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IAnnotatedDocument;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.corpus.ICorpus;
import com.silicolife.textmining.core.interfaces.process.IE.INERProcess;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.dataholders.Document;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.dataholders.Document.Text_raw_type;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.input.DocumentIterator;

public class PublicationIt implements DocumentIterator{
	
	private Document nextDocument;
	private Iterator<IPublication> documentIt;
	private ICorpus corpus;
	private INERProcess process;
	
	
	public PublicationIt(ICorpus corpus,INERProcess process) throws ANoteException
	{
		this.corpus = corpus;
		documentIt = corpus.getArticlesCorpus().iterator();
		this.process = process;
		fetchNext();
	}

	@Override
	public Document next() {
		if (nextDocument == null)
			throw new NoSuchElementException(LanguageProperties.getLanguageStream("pt.uminho.anote2.linnaeus.operation.err.nomoredocuments"));

		Document res = nextDocument;
		try {
			fetchNext();
		} catch (ANoteException e) {
			e.printStackTrace();
			return null;
		}
		return res;
	}

	@Override
	public void remove() {
		throw new IllegalStateException("not implemented");		
	}

	@Override
	public Iterator<Document> iterator() {
		return this;
	}

	
	public void skip() {
		if (!hasNext())
			throw new NoSuchElementException();

		try {
			fetchNext();
		} catch (ANoteException e) {
			e.printStackTrace();
		}
	}

	public boolean hasNext() {
		return nextDocument != null;
	}
	
	private void fetchNext() throws ANoteException{
		nextDocument = null;
		if(documentIt.hasNext())
		{
			IPublication anoteDocument = documentIt.next();
			nextDocument = convertAnoteDocumentToLinnaeus(anoteDocument);
		}
		
	}

	private Document convertAnoteDocumentToLinnaeus(IPublication anoteDocument) throws ANoteException {
		if(anoteDocument instanceof IAnnotatedDocument){
			String rawText = ((IAnnotatedDocument) anoteDocument).getDocumentAnnotationText();
			String documentText = rawText;
			if(process.getProperties().containsKey(GlobalNames.normalization))
			{
				if(Boolean.valueOf(process.getProperties().getProperty(GlobalNames.normalization)))
				{
					documentText = TermSeparator.termSeparator(rawText);
				}
			}
			return new Document(String.valueOf(anoteDocument.getId()),null , null, documentText, rawText,
					Text_raw_type.TEXT, ((IAnnotatedDocument) anoteDocument).getYeardate(), null, null, null, ((IAnnotatedDocument) anoteDocument).getVolume(),
					((IAnnotatedDocument) anoteDocument).getIssue(), ((IAnnotatedDocument) anoteDocument).getPages(), null, null);
		}

		if(anoteDocument instanceof IPublication)
		{
			IAnnotatedDocument annotDOc = new AnnotatedDocumentImpl(anoteDocument,process, corpus);
			String rawText = annotDOc.getDocumentAnnotationText();
			String documentText = rawText;
			if(process.getProperties().containsKey(GlobalNames.normalization))
			{
				if(Boolean.valueOf(process.getProperties().getProperty(GlobalNames.normalization)))
				{
					documentText = TermSeparator.termSeparator(rawText);
				}
			}
			return new Document(String.valueOf(annotDOc.getId()), null, null, documentText,
					rawText, Text_raw_type.TEXT, annotDOc.getYeardate(), null, null, null,
					annotDOc.getVolume(), annotDOc.getIssue(), annotDOc.getPages(), null,null);
		}
		else{
			String rawText = anoteDocument.getFullTextContent();
			String documentText = rawText;
			if(process.getProperties().containsKey(GlobalNames.normalization))
			{
				if(Boolean.valueOf(process.getProperties().getProperty(GlobalNames.normalization)))
				{
					documentText = TermSeparator.termSeparator(rawText);
				}
			}
			return new Document(String.valueOf(anoteDocument.getId()), null, null, documentText, rawText, Text_raw_type.TEXT,
					null, null, null, null, null, null, null, null, null);
		}


	}

}
