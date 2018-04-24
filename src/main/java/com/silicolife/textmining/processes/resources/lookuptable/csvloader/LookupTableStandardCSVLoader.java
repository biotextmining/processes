package com.silicolife.textmining.processes.resources.lookuptable.csvloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.silicolife.textmining.core.datastructures.dataaccess.database.schema.TableResourcesElements;
import com.silicolife.textmining.core.datastructures.general.ExternalIDImpl;
import com.silicolife.textmining.core.datastructures.general.SourceImpl;
import com.silicolife.textmining.core.datastructures.language.LanguageProperties;
import com.silicolife.textmining.core.datastructures.report.resources.ResourceUpdateReportImpl;
import com.silicolife.textmining.core.datastructures.resources.dictionary.loaders.DictionaryLoaderHelp;
import com.silicolife.textmining.core.datastructures.resources.export.ResourceExportColumnEnum;
import com.silicolife.textmining.core.datastructures.utils.FileHandling;
import com.silicolife.textmining.core.datastructures.utils.generic.CSVFileConfigurations;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.dataaccess.layer.resources.IResourceManagerReport;
import com.silicolife.textmining.core.interfaces.core.general.IExternalID;
import com.silicolife.textmining.core.interfaces.core.report.resources.IResourceUpdateReport;
import com.silicolife.textmining.core.interfaces.resource.IResource;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;
import com.silicolife.textmining.core.interfaces.resource.importer.IResourceImportFromCSVFiles;

public class LookupTableStandardCSVLoader extends DictionaryLoaderHelp implements IResourceImportFromCSVFiles{
	
	private BufferedReader br;


	private boolean cancel;



	public LookupTableStandardCSVLoader() {
		super("Update Standard File");
	}



	@Override
	public void stop() {
		cancel = true;		
	}

	@Override
	public List<Long> getInsertedTermIDList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Long> getNewClassesAdded() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public IResourceUpdateReport loadTermFromGenericCVSFile(IResource<IResourceElement> resource, File file,CSVFileConfigurations csvfileconfigurations) throws ANoteException,
			IOException {
		super.setResource(resource);
		cancel = false;	
		IResourceUpdateReport report = new ResourceUpdateReportImpl(LanguageProperties.getLanguageStream("pt.uminho.anote2.general.resources.lookuptable.update.report.title"), resource, file, "");
		long startTime = GregorianCalendar.getInstance().getTimeInMillis();
		importCVSFile(report,file,csvfileconfigurations);
		long endTime = GregorianCalendar.getInstance().getTimeInMillis();
		if(cancel)
			report.setcancel();
		report.setTime(endTime-startTime);
		return report;
	}
	
	public void importCVSFile(IResourceUpdateReport report,File file,CSVFileConfigurations csvfileconfigurations) throws ANoteException, IOException {	
		if(file==null)
		{
			throw new IOException("File is null");
		}
		else if(!file.exists())
		{
			throw new IOException("File not exists");
		}
		else
		{
			String line;
			int step = 0;
			int total = FileHandling.getFileLines(file);
			String term;
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			while((line = br.readLine())!=null && !cancel)
			{
				String[] lin = line.split(csvfileconfigurations.getGeneralDelimiter().getValue());
				term = getTerm(lin,csvfileconfigurations);
				if(term != null && term.length()>=TableResourcesElements.mimimumElementSize && term.length()<TableResourcesElements.elementSize)
				{
					String klass = getClass(lin,csvfileconfigurations);
					if(klass != null && klass.length()>=TableResourcesElements.mimimumClasseElementSize && klass.length()<TableResourcesElements.classeSize)
					{
						List<IExternalID> extLis = getExternalIds(lin,csvfileconfigurations);
						this.addElementToBatch(term, klass, new HashSet<String>() , extLis, 0);
					}
				}
				if(step%1000==0)
				{
					memoryAndProgress(step, total);
				}
				if(!cancel && isBatchSizeLimitOvertaken())
				{
					IResourceManagerReport reportBatchInserted = super.executeBatch();
					super.updateReport(reportBatchInserted,report);			
				}
				step++;
			}
			if(!cancel)
			{
				IResourceManagerReport reportBatchInserted = super.executeBatch();
				super.updateReport(reportBatchInserted,report);			
			}
		}
	}
	
	private List<IExternalID> getExternalIds(String[] lin,CSVFileConfigurations csvfileconfigurations) {
		if(csvfileconfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters().get(ResourceExportColumnEnum.externalID.toString())==null)
		{
			return new ArrayList<IExternalID>();
		}
		String value = lin[csvfileconfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters().get(ResourceExportColumnEnum.externalID.toString()).getColumnNumber()];
		if(value.equals(csvfileconfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters().get(ResourceExportColumnEnum.externalID.toString()).getDefaultValue().getValue()))
		{
			return new ArrayList<IExternalID>();
		}
		String[] extIDs = value.split(csvfileconfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters().get(ResourceExportColumnEnum.externalID.toString()).getDelimiter().getValue());
		List<IExternalID> listExtID = new ArrayList<IExternalID>();
		for(String extID:extIDs)
		{
			String[] ex = extID.split(csvfileconfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters().get(ResourceExportColumnEnum.externalID.toString()).getSubDelimiter().getValue());
			if(ex.length>1)
			{
				String id = ex[0].replace(csvfileconfigurations.getTextDelimiter().getValue(),"");
				String source = ex[1].replace(csvfileconfigurations.getTextDelimiter().getValue(),"");
				listExtID.add(new ExternalIDImpl(id,  new SourceImpl(source)));
			}
		}
		return listExtID;
	}
	
	private String getClass(String[] lin,CSVFileConfigurations csvfileconfigurations) {
		String value = lin[csvfileconfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters().get(ResourceExportColumnEnum.classe.toString()).getColumnNumber()];
		value = value.replace(csvfileconfigurations.getTextDelimiter().getValue(),"");
		return value;
	}

	protected String getTerm(String[] lin,CSVFileConfigurations csvfileconfigurations) {
		String value = lin[csvfileconfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters().get(ResourceExportColumnEnum.term.toString()).getColumnNumber()];
		if(value!=null)
			value = value.replace(csvfileconfigurations.getTextDelimiter().getValue(),"");
		return value;
	}

}
