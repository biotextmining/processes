package com.silicolife.textmining.processes.ie.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.silicolife.textmining.core.datastructures.annotation.AnnotationType;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.general.classe.IAnoteClass;
import com.silicolife.textmining.core.interfaces.process.IE.IIEProcess;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;

public class REProcessAnalysis extends AProcessAnalysis{

	public REProcessAnalysis(IIEProcess reprocess) {
		super(reprocess);
	}
	
	public Long countREAnnotations() throws ANoteException {
		return InitConfiguration.getDataAccess().countAnnotationsByAnnotationType(this.getProcess(), AnnotationType.re);
	}

	public Map<ImmutablePair<IAnoteClass, IAnoteClass>, Long> countEventAnnotationsByClassInProcess() throws ANoteException{
		return InitConfiguration.getDataAccess().countEventAnnotationsByClassInProcess(this.getProcess());
	}
	
	public Map<ImmutablePair<IAnoteClass, IAnoteClass>, Long> countPublicationsWithEventsByIAnoteClasses() throws ANoteException{
		return InitConfiguration.getDataAccess().countPublicationsWithEventsByIAnoteClasses(this.getProcess());
	}
	
	public Long countPublicationsWithEventsByResourceElement(IResourceElement resourceElement) throws ANoteException{
		return InitConfiguration.getDataAccess().countDocumentsWithResourceElementByAnnotationTypeInProcess(resourceElement,this.getProcess(), AnnotationType.re);
	}
	
	public List<IPublication> getPublicationsFilteredByEventResourceElements(Set<ImmutablePair<IResourceElement, IResourceElement>> resourceElementsPairs) throws ANoteException{
		List<IPublication> pubs = new ArrayList<>();
		List<Long> ids = InitConfiguration.getDataAccess().getPublicationsIdsByEventResourceElements(this.getProcess(),resourceElementsPairs);
		for(Long id:ids)
			pubs.add(InitConfiguration.getDataAccess().getPublication(id));
		return pubs;
	}
	
}
