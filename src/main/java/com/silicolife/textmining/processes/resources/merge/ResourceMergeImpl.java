package com.silicolife.textmining.processes.resources.merge;

import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.report.resources.ResourceMergeReportImpl;
import com.silicolife.textmining.core.datastructures.resources.ResourceElementSetImpl;
import com.silicolife.textmining.core.datastructures.resources.dictionary.loaders.DictionaryLoaderHelp;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.dataaccess.layer.resources.IResourceManagerReport;
import com.silicolife.textmining.core.interfaces.core.general.classe.IAnoteClass;
import com.silicolife.textmining.core.interfaces.core.report.resources.IResourceMergeReport;
import com.silicolife.textmining.core.interfaces.resource.IResource;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;
import com.silicolife.textmining.core.interfaces.resource.IResourceElementSet;
import com.silicolife.textmining.core.interfaces.resource.lexicalwords.ILexicalWords;
import com.silicolife.textmining.core.interfaces.resource.merge.IResourceMerge;
import com.silicolife.textmining.core.interfaces.resource.merge.IResourceMergeConfiguration;
import com.silicolife.textmining.core.interfaces.resource.rules.IRuleSet;

public class ResourceMergeImpl extends DictionaryLoaderHelp implements IResourceMerge{

	private boolean cancel = false;

	public ResourceMergeImpl() {
		super("");
	}

	@Override
	public IResourceMergeReport merge(IResourceMergeConfiguration configuration) throws ANoteException {
		cancel = false;
		IResourceMergeReport report = null;
		IResourceElementSet<IResourceElement> elementsToMerge = new ResourceElementSetImpl<IResourceElement>();
		IResource<IResourceElement> toInsertResource = null;
		Set<String> klasses = new HashSet<>();
		if(configuration.createANewResourceAsResult())
		{
			IResource<IResourceElement> newResource = configuration.getNewToMergeResources();
			InitConfiguration.getDataAccess().createResource(newResource);
			report = new ResourceMergeReportImpl("",configuration.getNewToMergeResources(),configuration.getSourceResource(),newResource);
			report.setResourceSource2(configuration.getDestinyResource());
			toInsertResource = newResource;
			if(configuration.getDestinyResource() instanceof ILexicalWords)
			{
				IResourceElementSet<IResourceElement> elemsnts = InitConfiguration.getDataAccess().getResourceElements(configuration.getDestinyResource());
				elementsToMerge.addAllElementResource(elemsnts.getResourceElements());
			}
			else
			{
				for(IAnoteClass klass:configuration.getDestinyClasses())
				{
					report.addClassesAdding(1);
					klasses.add(klass.getName());
					IResourceElementSet<IResourceElement> elemsnts = InitConfiguration.getDataAccess().getResourceElementsByClass(configuration.getDestinyResource(), klass);
					elementsToMerge.addAllElementResource(elemsnts.getResourceElements());
				}
			}
		}
		else
		{
			report = new ResourceMergeReportImpl("",configuration.getNewToMergeResources(),configuration.getSourceResource(),configuration.getDestinyResource());
			toInsertResource = configuration.getDestinyResource();
		}

		long startTime = GregorianCalendar.getInstance().getTimeInMillis();
		if(configuration.getSourceResource() instanceof ILexicalWords)
		{
			IResourceElementSet<IResourceElement> elemsnts = InitConfiguration.getDataAccess().getResourceElements(configuration.getSourceResource());
			elementsToMerge.addAllElementResource(elemsnts.getResourceElements());
		}
		else
		{
			for(IAnoteClass klass:configuration.getSourceClasses())
			{
				if(!klasses.contains(klass.getName()))
				{
					report.addClassesAdding(1);
					klasses.add(klass.getName());
				}
				IResourceElementSet<IResourceElement> elemsnts = InitConfiguration.getDataAccess().getResourceElementsByClass(configuration.getSourceResource(), klass);
				elementsToMerge.addAllElementResource(elemsnts.getResourceElements());
			}
		}
		updateTerms(toInsertResource,report, elementsToMerge);
		long endTime = GregorianCalendar.getInstance().getTimeInMillis();
		report.setTime(endTime-startTime);
		return report;
	}

	private void updateTerms(IResource<IResourceElement> toInsertResource, IResourceMergeReport report,IResourceElementSet<IResourceElement> elementsToMerge) throws ANoteException{
		setResource(toInsertResource);
		long startTime = GregorianCalendar.getInstance().getTimeInMillis();
		int lineNumber = 0;
		int totalLines = elementsToMerge.getResourceElements().size();
		for(IResourceElement elem:elementsToMerge.getResourceElements())
		{
			elem.generateNewId();
			if(toInsertResource instanceof IRuleSet)
				elem.setPriority(lineNumber+1);
			super.addElementToBatch(elem);
			if(!cancel  && isBatchSizeLimitOvertaken())
			{
				IResourceManagerReport reportBatchInserted = super.executeBatch();
				super.updateReport(reportBatchInserted,report);			
			}
			if((lineNumber%500)==0)
			{
				memoryAndProgress(startTime,lineNumber, totalLines);
			}
			lineNumber++;
		}
		if(!cancel)
		{
			IResourceManagerReport reportBatchInserted = super.executeBatch();
			super.updateReport(reportBatchInserted,report);			
		}
	}

}
