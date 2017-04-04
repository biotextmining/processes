package com.silicolife.textmining.processes.resources.dictionary.loaders.drugbank;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.silicolife.textmining.core.datastructures.general.ExternalIDImpl;
import com.silicolife.textmining.core.datastructures.general.SourceImpl;
import com.silicolife.textmining.core.datastructures.resources.dictionary.loaders.DictionaryLoaderHelp;
import com.silicolife.textmining.core.datastructures.resources.dictionary.loaders.configuration.DictionaryLoaderConfigurationImpl;
import com.silicolife.textmining.core.datastructures.utils.FileHandling;
import com.silicolife.textmining.core.datastructures.utils.generic.CSVFileConfigurations;
import com.silicolife.textmining.core.datastructures.utils.generic.ColumnDelemiterDefaultValue;
import com.silicolife.textmining.core.datastructures.utils.generic.ColumnParameters;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.dataaccess.layer.resources.IResourceManagerReport;
import com.silicolife.textmining.core.interfaces.core.general.IExternalID;
import com.silicolife.textmining.core.interfaces.core.report.resources.IResourceUpdateReport;
import com.silicolife.textmining.core.interfaces.process.IE.io.export.DefaultDelimiterValue;
import com.silicolife.textmining.core.interfaces.process.IE.io.export.Delimiter;
import com.silicolife.textmining.core.interfaces.process.IE.io.export.TextDelimiter;
import com.silicolife.textmining.core.interfaces.resource.dictionary.IDicionaryFlatFilesLoader;
import com.silicolife.textmining.core.interfaces.resource.dictionary.configuration.IDictionaryLoaderConfiguration;

public class DrugBankOpenDataCSVLoader extends DictionaryLoaderHelp implements IDicionaryFlatFilesLoader{

	private static final String drugbanksource = "Drug Bank";
	private static final String CASsource = "CAS";
	private static final String InCHIsource = "InChI Key";
	
	private static final String drug = "drug";

	
	private boolean cancel = false;
	private CSVFileConfigurations csvConfigurations;
	private int columnDrugBAnkIDNumber = 0;
	private int columnAccessionNumbersNumber = 1;
	private int columnCommonNameNumber = 2;
	private int columnCASNumber = 3;
	private int columnUNIINumber = 4;
	private int columnSynonymNumber = 5;
	private int columnStandardInChIKeyNumber = 6;

	
	private String columnDrugBAnkIDName = "DrugBank ID";
	private String columnAccessionNumbersName = "Accession Numbers";
	private String columnCommonNameName = "Common name";
	private String columnCommonCASName = "CAS";
	private String columnUNIIName = "UNII";
	private String columntermSynonymName = "Synonyms";
	private String columnStandardInChIKeyName = "Synonyms";

	
	public DrugBankOpenDataCSVLoader()
	{
		super(drugbanksource);
		initCSVConfigurations();

	}
	
	private void initCSVConfigurations() {
		TextDelimiter textdelimiter = TextDelimiter.QUOTATION_MARK;
		Delimiter generalDelimiter = Delimiter.COMMA;
		Map<String, ColumnParameters> columnNameColumnParameters = new HashMap<String, ColumnParameters>();
		ColumnParameters columnDrugBAnkID = new ColumnParameters(columnDrugBAnkIDNumber , Delimiter.USER, DefaultDelimiterValue.HYPHEN);
		ColumnParameters columnAccessionNumbers = new ColumnParameters(columnAccessionNumbersNumber, Delimiter.VERTICAL_BAR, DefaultDelimiterValue.HYPHEN);
		ColumnParameters columnCommonName = new ColumnParameters(columnCommonNameNumber, Delimiter.USER, DefaultDelimiterValue.USER);
		ColumnParameters columnCommonCAS = new ColumnParameters(columnCASNumber, Delimiter.USER, DefaultDelimiterValue.USER);
		columnCommonCAS.getDefaultValue().setUserDelimiter("\"\"");
		ColumnParameters columnUNII = new ColumnParameters(columnUNIINumber, Delimiter.USER, DefaultDelimiterValue.USER);
		ColumnParameters columnSynonymName = new ColumnParameters(columnSynonymNumber, Delimiter.VERTICAL_BAR, DefaultDelimiterValue.USER);
		columnSynonymName.getDefaultValue().setUserDelimiter("\"\"");
		ColumnParameters columnStandardInChIKey = new ColumnParameters(columnStandardInChIKeyNumber, Delimiter.VERTICAL_BAR, DefaultDelimiterValue.HYPHEN, Delimiter.COLON);
		columnNameColumnParameters.put(columnDrugBAnkIDName , columnDrugBAnkID);
		columnNameColumnParameters.put(columnAccessionNumbersName , columnAccessionNumbers);
		columnNameColumnParameters.put(columnCommonNameName , columnCommonName);
		columnNameColumnParameters.put(columnCommonCASName  , columnCommonCAS);
		columnNameColumnParameters.put(columnUNIIName  , columnUNII);
		columnNameColumnParameters.put(columntermSynonymName , columnSynonymName);
		columnNameColumnParameters.put(columnStandardInChIKeyName, columnStandardInChIKey);
		ColumnDelemiterDefaultValue columsDelemiterDefaultValue = new ColumnDelemiterDefaultValue(columnNameColumnParameters);
		csvConfigurations = new CSVFileConfigurations(generalDelimiter, textdelimiter, DefaultDelimiterValue.HYPHEN, columsDelemiterDefaultValue, true);
	}
	
	@Override
	public void stop() {
		this.cancel = true;
	}

	@Override
	public IResourceUpdateReport loadTerms(IDictionaryLoaderConfiguration configuration) throws ANoteException, IOException {
		cancel = false;
		super.setResource(configuration.getDictionary());
		File file = configuration.getFlatFile();
		boolean importExternlIDs = configuration.loadExternalIDs();
		getReport().addClassesAdding(1);
		getReport().updateFile(file);
		List<String> fileLines = FileHandling.getFileLinesContent(file);
		long starttime = GregorianCalendar.getInstance().getTimeInMillis();
		if(csvConfigurations.isHasHeaders())
			fileLines.remove(0);
		int total = fileLines.size();
		int step =0;
		for(String line:fileLines)
		{
			if(cancel)
			{
				getReport().setcancel();
				long nowtime = GregorianCalendar.getInstance().getTimeInMillis() - starttime;
				getReport().setTime(nowtime);
				return getReport();
			}
			processDrugLine(line,importExternlIDs);
			step++;
			if(step%100==0)
			{
				memoryAndProgress(step, total);
			}
		}
		if (!cancel) {
			IResourceManagerReport reportBatchInserted = super.executeBatch();
			super.updateReport(reportBatchInserted, getReport());
		} else {
			getReport().setcancel();
		}
		long nowtime = GregorianCalendar.getInstance().getTimeInMillis() - starttime;
		getReport().setTime(nowtime);
		return getReport();
	}

	private void processDrugLine(String line, boolean importExternlIDs) throws ANoteException {
		List<IExternalID> externalIDs = new ArrayList<>();
		int correctionfactor = 0;
		String[] lineSplitered = line.split(csvConfigurations.getGeneralDelimiter().getValue());
		String dbprimaryID = lineSplitered[csvConfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters().get(columnDrugBAnkIDName).getColumnNumber()];
		externalIDs.add(new ExternalIDImpl(dbprimaryID.trim(), new SourceImpl(drugbanksource)));	
		String acessionNumbersID = lineSplitered[csvConfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters().get(columnAccessionNumbersName).getColumnNumber()];
		List<String> alternativeDrugBankIds = getDrugBAnkAcessionNumber(acessionNumbersID);
		for(String alternativeDrugBankId:alternativeDrugBankIds)
			externalIDs.add(new ExternalIDImpl(alternativeDrugBankId.trim(), new SourceImpl(drugbanksource)));	
		String primartyName = lineSplitered[csvConfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters().get(columnCommonNameName).getColumnNumber()];
		boolean cancel = false;
		if(primartyName.trim().startsWith("\""))
		{
			for(int i=csvConfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters().get(columnCommonNameName).getColumnNumber()+1
					;i<lineSplitered.length && !cancel;i++)
			{
				if(lineSplitered[i].endsWith("\""))
				{
					cancel = true;
				}
				primartyName = primartyName + lineSplitered[i];
				correctionfactor++;
			}
			primartyName = primartyName.replace("\"", "");
		}
		String cas = lineSplitered[csvConfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters().get(columnCommonCASName).getColumnNumber()+correctionfactor];
		if(!cas.trim().isEmpty())
			externalIDs.add(new ExternalIDImpl(cas.trim(), new SourceImpl(CASsource)));	
		if(!line.endsWith(","))
		{
			externalIDs.add(new ExternalIDImpl(lineSplitered[lineSplitered.length-1].trim(), new SourceImpl(InCHIsource)));	
		}
		List<String> synonyms =  getSynonyms(line, lineSplitered,correctionfactor);
//		System.out.println("#### Drug");
//		System.out.println(primartyName.trim());
//		System.out.println(synonyms);
//		System.out.println(externalIDs);
		super.addElementToBatch(primartyName.trim(), drug, new HashSet<>(synonyms), externalIDs, 0);
		if (!cancel && isBatchSizeLimitOvertaken()) {
			IResourceManagerReport reportBatchInserted = super.executeBatch();
			super.updateReport(reportBatchInserted, getReport());
		}
	}
	
	private List<String> getSynonyms(String line,String[] lineSplitered, int correctionfactor)
	{
		int finalNumber = lineSplitered.length;
		if(!line.endsWith(","))
		{
			finalNumber = lineSplitered.length-1;
		}
		String strConcat = new String();
		for(int i = 5+correctionfactor;i< finalNumber;i++)
		{
			strConcat = strConcat + lineSplitered[i];
		}
		strConcat = strConcat.replace("\"", "");
		String delimiter = csvConfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters().get(columntermSynonymName).getDelimiter().getValue();
		String[] candidateSynonyms = strConcat.split(delimiter);
		List<String> out = new ArrayList<>();
		for(String candidateSynonym:candidateSynonyms)
			out.add(candidateSynonym.trim());
		return out;
	}
	
	private List<String> getDrugBAnkAcessionNumber(String acessionNumbersID)
	{
		List<String> out = new ArrayList<>();
		String delimiter = csvConfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters().get(columnAccessionNumbersName).getDelimiter().getValue();
		String[] cadidateAcessionNumbers = acessionNumbersID.split(delimiter);
		for(String cadidateAcessionNumber:cadidateAcessionNumbers)
		{
			if(cadidateAcessionNumber.trim().startsWith("DB"))
			{
				out.add(cadidateAcessionNumber.trim());
			}
		}
		return out;
	}

	@Override
	public boolean checkFile(File file) {
		String dialogMsg = "";
		String consoleMsg = "";
		if (!file.getName().endsWith(".csv")) {
            dialogMsg = consoleMsg = "Invalid file format: CSV required!";
        }
		else if(!file.exists()){
			dialogMsg = consoleMsg = "Selected file does not exist!";
		}
		else if(!file.isFile()){
			dialogMsg = consoleMsg = "You haven't select a file!";
		}
		else if(!file.canRead()){
			dialogMsg = consoleMsg = "Selected file cannot be read!";
		}
		else if(file.length() <= 0){
			dialogMsg = consoleMsg = "Selected file is empty";
		}
		if(!dialogMsg.equals("") || !consoleMsg.equals("")){
			return false;
		}
		return testFileHeader(file);
	}
	

	public static boolean checkFileDrugBank(File file) {
		String dialogMsg = "";
		String consoleMsg = "";
		if (!file.getName().endsWith(".csv")) {
            dialogMsg = consoleMsg = "Invalid file format: CSV required!";
        }
		else if(!file.exists()){
			dialogMsg = consoleMsg = "Selected file does not exist!";
		}
		else if(!file.isFile()){
			dialogMsg = consoleMsg = "You haven't select a file!";
		}
		else if(!file.canRead()){
			dialogMsg = consoleMsg = "Selected file cannot be read!";
		}
		else if(file.length() <= 0){
			dialogMsg = consoleMsg = "Selected file is empty";
		}
		if(!dialogMsg.equals("") || !consoleMsg.equals("")){
			return false;
		}
		return testFileHeader(file);
	}
	
	private static boolean testFileHeader(File file)
	{
		try {
			String header = FileHandling.getFileFirstLine(file);
			String[] columnHeaders = header.split(",");
			if(columnHeaders[0].equals("DrugBank ID")
					&& columnHeaders[2].equals("Common name"))
			{
				return true;
			}
		} catch (IOException e) {
			return false;
		}
		return false;
	}
	
	public static void main(String[] args) throws ANoteException, IOException {
		DrugBankOpenDataCSVLoader loader = new DrugBankOpenDataCSVLoader();
		File file =new  File("src/test/resources/drugbank/drugbank vocabulary.csv");
		IDictionaryLoaderConfiguration configuration = new DictionaryLoaderConfigurationImpl("drugbank", null, file , null, true);
		loader.loadTerms(configuration);
	}

}
