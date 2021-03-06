package com.silicolife.textmining.processes.resources.lexicalwords.csvlader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.silicolife.textmining.core.datastructures.dataaccess.database.schema.TableResourcesElements;
import com.silicolife.textmining.core.datastructures.language.LanguageProperties;
import com.silicolife.textmining.core.datastructures.report.resources.ResourceUpdateReportImpl;
import com.silicolife.textmining.core.datastructures.resources.dictionary.loaders.DictionaryLoaderHelp;
import com.silicolife.textmining.core.datastructures.resources.export.ResourceExportColumnEnum;
import com.silicolife.textmining.core.datastructures.utils.FileHandling;
import com.silicolife.textmining.core.datastructures.utils.generic.CSVFileConfigurations;
import com.silicolife.textmining.core.datastructures.utils.generic.ColumnDelemiterDefaultValue;
import com.silicolife.textmining.core.datastructures.utils.generic.ColumnParameters;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.dataaccess.layer.resources.IResourceManagerReport;
import com.silicolife.textmining.core.interfaces.core.general.IExternalID;
import com.silicolife.textmining.core.interfaces.core.report.resources.IResourceUpdateReport;
import com.silicolife.textmining.core.interfaces.resource.IResource;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;
import com.silicolife.textmining.core.interfaces.resource.importer.IResourceImportFromCSVFiles;

public class LexicalWordsCSVLoader extends DictionaryLoaderHelp implements IResourceImportFromCSVFiles{
	
	private BufferedReader br;


	private boolean cancel;



	public LexicalWordsCSVLoader() {
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
		IResourceUpdateReport report = new ResourceUpdateReportImpl(LanguageProperties.getLanguageStream("pt.uminho.anote2.general.resources.lexicalwords.update.report.title"), resource, file, "");
		long startTime = GregorianCalendar.getInstance().getTimeInMillis();
		importCVSFile(report,file,csvfileconfigurations);
		long endTime = GregorianCalendar.getInstance().getTimeInMillis();
		if(cancel)
			report.setcancel();
		report.setTime(endTime-startTime);
		return report;
	}

	public void importCVSFile(IResourceUpdateReport report,File file,CSVFileConfigurations csvfileconfigurations) throws ANoteException, IOException 
	{	
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
					this.addElementToBatch(term, null, new HashSet<String>() , new ArrayList<IExternalID>(), 0);
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


	protected String getTerm(String[] lin,CSVFileConfigurations csvfileconfigurations) {
		ColumnDelemiterDefaultValue delimiter = csvfileconfigurations.getColumsDelemiterDefaultValue();
		Map<String, ColumnParameters> nameMap = delimiter.getColumnNameColumnParameters();
		ColumnParameters column = nameMap.get(ResourceExportColumnEnum.term.toString());
		int index = column.getColumnNumber();
		String value = lin[index];
		if(value!=null)
			value = value.replace(csvfileconfigurations.getTextDelimiter().getValue(),"");
		return value;
	}

}
