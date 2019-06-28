package com.silicolife.textmining.processes.ie.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.silicolife.textmining.core.datastructures.analysis.StatisticsImpl;
import com.silicolife.textmining.core.datastructures.annotation.AnnotationType;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.interfaces.core.analysis.IStatistics;
import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.IPublicationFilter;
import com.silicolife.textmining.core.interfaces.core.general.classe.IAnoteClass;
import com.silicolife.textmining.core.interfaces.process.IE.IIEProcess;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;

public class NERProcessAnalysis extends AProcessAnalysis {


	public NERProcessAnalysis(IIEProcess nerProcess) {
		super(nerProcess);
	}

	public Long countNERAnnotations() throws ANoteException {
		return InitConfiguration.getDataAccess().countAnnotationsByAnnotationType(this.getProcess(), AnnotationType.ner);
	}
	
	public Long countPublicationsWithNERAnnotationsByResourceElement(IResourceElement resourceElement) throws ANoteException {
		return InitConfiguration.getDataAccess().countDocumentsWithResourceElementByAnnotationTypeInProcess(resourceElement, this.getProcess(), AnnotationType.re);
	}
	
	public Map<IAnoteClass, Long> countNERAnoteClassInProcess() throws ANoteException{
		return InitConfiguration.getDataAccess().countEntityAnnotationsByClassInProcess(this.getProcess());
	}
	
	public Map<IResourceElement,Long> countDocumentsAnnotatedByResourceElementsInProcess() throws ANoteException{
		return InitConfiguration.getDataAccess().countDocumentsWithEntityAnnotationsByResourceElementInProcess(this.getProcess());
	}
	
	public Map<IResourceElement, Long> countNERAnoteResourcesAnnotatedInProcess() throws ANoteException{
		return InitConfiguration.getDataAccess().countEntityAnnotationsByResourceElementInProcess(this.getProcess());
	}
	
	public List<IPublication> getPublicationsFilteredByEntityResourceElement(Set<IResourceElement> resourceElements, IPublicationFilter pubFilter) throws ANoteException{
		List<IPublication> pubs = new ArrayList<>();
		List<Long> publicationIds = InitConfiguration.getDataAccess().getPublicationsIdsByResourceElementsFilteredByPublicationFilter(resourceElements, pubFilter);
		for(Long documentID : publicationIds) 
			pubs.add(InitConfiguration.getDataAccess().getPublication(documentID));
		
		return pubs;
			
	}

	
	public IStatistics<IIEProcess> getProcessStatistics() throws ANoteException{
		IStatistics<IIEProcess> processstatistics = new StatisticsImpl<>();
		
		// generate NER statistics
		IStatistics<IEntityAnnotation> nerstatistics = new StatisticsImpl<>();
		nerstatistics.setTotalCount(this.countNERAnnotations());
		//TODO by entity name w/ synonyms
		processstatistics.getSubStatistics().add(nerstatistics);
		
		//generate class statistics
		IStatistics<IAnoteClass> nerclassstatistics = new StatisticsImpl<>();
		nerclassstatistics.addOrSumAllCountBy(this.countNERAnoteClassInProcess());
		processstatistics.getSubStatistics().add(nerclassstatistics);
		
		//generate resources statistics
		IStatistics<IResourceElement> nerResourceStatistics = new StatisticsImpl<>();
		nerResourceStatistics.addOrSumAllCountBy(this.countNERAnoteResourcesAnnotatedInProcess());
		processstatistics.getSubStatistics().add(nerResourceStatistics);
		// doc id 2120595566144580435 procc: 791710063734696223
		//if annotation does not have resource element, it is null, so group by ann_element, otherwise group by resource_element_id

		
		return processstatistics;
	}
	
}
