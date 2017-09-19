package com.silicolife.textmining.processes.resources.export;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import com.silicolife.textmining.core.datastructures.general.ClassPropertiesManagement;
import com.silicolife.textmining.core.datastructures.resources.export.ResourceExportColumnEnum;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalOptions;
import com.silicolife.textmining.core.datastructures.utils.generic.CSVFileConfigurations;
import com.silicolife.textmining.core.datastructures.utils.generic.ColumnParameters;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.general.IExternalID;
import com.silicolife.textmining.core.interfaces.core.general.classe.IAnoteClass;
import com.silicolife.textmining.core.interfaces.core.general.source.ISource;
import com.silicolife.textmining.core.interfaces.process.IE.io.export.Delimiter;
import com.silicolife.textmining.core.interfaces.resource.IResource;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;
import com.silicolife.textmining.core.interfaces.resource.IResourceElementSet;
import com.silicolife.textmining.core.interfaces.resource.export.tsv.IResourceExportConfiguration;
import com.silicolife.textmining.core.interfaces.resource.export.tsv.IResourceExportToCSV;

public class ResourceExportToCSVImpl implements IResourceExportToCSV{
	
	public static final String processUID = "resource.export";

	@Override
	public void exportCSVFile(IResource<IResourceElement> resource,IResourceExportConfiguration configuration) throws ANoteException,IOException {
		File file = new File(configuration.getFile());
		if(!file.exists())
			file.createNewFile();
		IResourceElementSet<IResourceElement> elemenst = resource.getResourceElements();
		int total = elemenst.size();
		PrintWriter pw = new PrintWriter(file);
		CSVFileConfigurations csvfileconfigurations = configuration.getCSVFileConfigurations();
		
		int step = 0;
		for(IResourceElement elem :elemenst.getResourceElements())
		{
			String line = new String();
			String[] columnsline = fillLine(csvfileconfigurations, elem);
			boolean first = true;
			for(String column : columnsline){
				if(!first){
					line = line + configuration.getCSVFileConfigurations().getGeneralDelimiter().getValue();
				}
				line = line + column;
				first = false;
			}
			pw.write(line);
			pw.println();
			if(step % 10 == 0)
			{
				memoryAndProgress(step, total);
			}
			step ++;
		}
		pw.close();		
	}


	private String[] fillLine(CSVFileConfigurations csvfileconfigurations, IResourceElement elem)
			throws ANoteException {
		Map<String, ColumnParameters> columnParam = csvfileconfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters();
		String[] line = new String[columnParam.size()];
		
		line[columnParam.get(ResourceExportColumnEnum.term.toString()).getColumnNumber()] = elem.getTerm();
		fillClass(csvfileconfigurations, elem, columnParam, line);
		fillSynonyms(csvfileconfigurations, elem, columnParam, line);
		fillExternalIDs(csvfileconfigurations, elem, columnParam, line);
		
		return line;
	}


	private void fillClass(CSVFileConfigurations csvfileconfigurations, IResourceElement elem,
			Map<String, ColumnParameters> columnParam, String[] line) throws ANoteException {
		if(columnParam.containsKey(ResourceExportColumnEnum.classe.toString())){
			if(elem.getTermClass()!=null){
				IAnoteClass klass = ClassPropertiesManagement.getClassGivenClassID(elem.getTermClass().getId());
				line[columnParam.get(ResourceExportColumnEnum.classe.toString()).getColumnNumber()] = klass.getName();
			}else{
				line[columnParam.get(ResourceExportColumnEnum.classe.toString()).getColumnNumber()] = csvfileconfigurations.getDefaultValue().getValue();
			}
		}
	}


	private void fillSynonyms(CSVFileConfigurations csvfileconfigurations, IResourceElement elem,
			Map<String, ColumnParameters> columnParam, String[] line) {
		if(columnParam.containsKey(ResourceExportColumnEnum.synonyms.toString())){
			if(elem.getSynonyms() !=null && !elem.getSynonyms().isEmpty()){
				ColumnParameters synmdelem = columnParam.get(ResourceExportColumnEnum.synonyms.toString());
				Delimiter synmDel = synmdelem.getDelimiter();
				StringBuilder sb = new StringBuilder();
				boolean first = true;
				for(String synonm : elem.getSynonyms()){
					if(!first){
						sb.append(synmDel.getValue());
					}
					sb.append(synonm);
					first = false;
				}
				line[columnParam.get(ResourceExportColumnEnum.synonyms.toString()).getColumnNumber()] = sb.toString();
			}else{
				line[columnParam.get(ResourceExportColumnEnum.synonyms.toString()).getColumnNumber()] = csvfileconfigurations.getDefaultValue().getValue();
			}
		}
	}


	private void fillExternalIDs(CSVFileConfigurations csvfileconfigurations, IResourceElement elem,
			Map<String, ColumnParameters> columnParam, String[] line) throws ANoteException {
		if(columnParam.containsKey(ResourceExportColumnEnum.externalID.toString())){
			if(elem.getExtenalIDs() != null && !elem.getExtenalIDs().isEmpty()){
				ColumnParameters extdelem = columnParam.get(ResourceExportColumnEnum.externalID.toString());
				Delimiter externalIDDel = extdelem.getDelimiter();
				Delimiter externalIDSubDel = extdelem.getSubDelimiter();
				StringBuilder sb = new StringBuilder();
				boolean first = true;
				for(IExternalID externalID : elem.getExtenalIDs()){
					if(!first){
						sb.append(externalIDDel.getValue());
					}
					sb.append(externalID.getExternalID());
					if(externalIDSubDel != null){
						sb.append(externalIDSubDel.getValue());
					}
					ISource source = externalID.getSource();
					if(source!= null){
						String sourcename = source.getSource();
						if(sourcename != null){
							sb.append(sourcename);
						}
					}
					first = false;
				}
				line[columnParam.get(ResourceExportColumnEnum.externalID.toString()).getColumnNumber()] = sb.toString();
			}else{
				line[columnParam.get(ResourceExportColumnEnum.externalID.toString()).getColumnNumber()] = csvfileconfigurations.getDefaultValue().getValue();
			}
		}
	}
	
	
	protected void memoryAndProgress(int step, int total) {
		System.out.println((GlobalOptions.decimalformat.format((double)step/ (double) total * 100)) + " %...");
		Runtime.getRuntime().gc();
		System.out.println((Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory())/(1024*1024) + " MB ");
	}
	
	
	
	
	

}
