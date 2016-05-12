package com.silicolife.textmining.processes.resources.dictionary.loaders.chebi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.silicolife.textmining.core.datastructures.general.ExternalIDImpl;
import com.silicolife.textmining.core.datastructures.general.SourceImpl;
import com.silicolife.textmining.core.datastructures.resources.dictionary.loaders.DictionaryLoaderHelp;
import com.silicolife.textmining.core.datastructures.utils.FileHandling;
import com.silicolife.textmining.core.datastructures.utils.GenericPairImpl;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalSources;
import com.silicolife.textmining.core.datastructures.utils.generic.CSVFileConfigurations;
import com.silicolife.textmining.core.datastructures.utils.generic.ColumnDelemiterDefaultValue;
import com.silicolife.textmining.core.datastructures.utils.generic.ColumnParameters;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.dataaccess.layer.resources.IResourceManagerReport;
import com.silicolife.textmining.core.interfaces.core.general.IExternalID;
import com.silicolife.textmining.core.interfaces.core.general.source.ISource;
import com.silicolife.textmining.core.interfaces.core.report.resources.IResourceUpdateReport;
import com.silicolife.textmining.core.interfaces.process.IE.io.export.DefaultDelimiterValue;
import com.silicolife.textmining.core.interfaces.process.IE.io.export.Delimiter;
import com.silicolife.textmining.core.interfaces.process.IE.io.export.TextDelimiter;
import com.silicolife.textmining.core.interfaces.resource.dictionary.IDicionaryFlatFilesLoader;
import com.silicolife.textmining.core.interfaces.resource.dictionary.IDictionary;
import com.silicolife.textmining.core.interfaces.resource.dictionary.configuration.IDictionaryLoaderConfiguration;

/**
 * @note2
 * 
 * @author Hugo
 *
 */

public class ChebiFlatFileLoader extends DictionaryLoaderHelp implements IDicionaryFlatFilesLoader {

	private static Pattern checkFile = Pattern.compile("^ID\\s+COMPOUND_ID\\s+TYPE");
	private Map<Integer, GenericPairImpl<String, Set<String>>> coupoundIDNameSynonyms;
	private String columnIDName = "ID";
	private String termOrSynonymName = "TermSynonym";
	private String typeName = "Type";
	private String defaultTypeValue = "NAME";
	private int columnIDNumber = 1;
	private int columnTermSynonymNumber = 4;
	private int columnTypeNumber = 2;
	private CSVFileConfigurations csvConfigurations;
	private boolean cancel = false;
	public final static String compounds = "Compound";

	public ChebiFlatFileLoader() {
		super(GlobalSources.chebi);
		this.coupoundIDNameSynonyms = new HashMap<Integer, GenericPairImpl<String, Set<String>>>();
		initCSVConfigurations();
	}

	private void initCSVConfigurations() {
		TextDelimiter textdelimiter = TextDelimiter.NONE;
		Delimiter generalDelimiter = Delimiter.TAB;
		Map<String, ColumnParameters> columnNameColumnParameters = new HashMap<String, ColumnParameters>();
		ColumnParameters columnID = new ColumnParameters(columnIDNumber, Delimiter.USER, DefaultDelimiterValue.USER);
		ColumnParameters columnTErmSynonym = new ColumnParameters(columnTermSynonymNumber, Delimiter.USER, DefaultDelimiterValue.USER);
		columnTErmSynonym.getDefaultValue().setUserDelimiter(defaultTypeValue);
		ColumnParameters columnType = new ColumnParameters(columnTypeNumber, Delimiter.USER, DefaultDelimiterValue.USER);
		columnNameColumnParameters.put(columnIDName, columnID);
		columnNameColumnParameters.put(termOrSynonymName, columnTErmSynonym);
		columnNameColumnParameters.put(typeName, columnType);
		ColumnDelemiterDefaultValue columsDelemiterDefaultValue = new ColumnDelemiterDefaultValue(columnNameColumnParameters);
		csvConfigurations = new CSVFileConfigurations(generalDelimiter, textdelimiter, DefaultDelimiterValue.HYPHEN, columsDelemiterDefaultValue, true);
	}

	public boolean checkFile(File file) {
		if (!file.isFile()) {
			return false;
		}
		getReport().updateFile(file);

		try (FileReader fr = new FileReader(file); 
				BufferedReader br = new BufferedReader(fr)) {
			
			String line = br.readLine();
			Matcher m = checkFile.matcher(line);
			if (m.find()) {
				return true;
			} else {
				return false;
			}

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public IResourceUpdateReport loadTerms(IDictionaryLoaderConfiguration configuration) throws ANoteException, IOException {
		super.setResource(configuration.getDictionary());
		boolean useexternalids= configuration.loadExternalIDs();
		File file = configuration.getFlatFile();
		cancel = false;
		BufferedReader br;
		String line;
		getReport().addClassesAdding(1);
		getReport().updateFile(file);
		FileReader fr;
		long startTime = GregorianCalendar.getInstance().getTimeInMillis();
		int lineNumber = 0;
		int totalLines = FileHandling.getFileLines(file);
		fr = new FileReader(file);
		br = new BufferedReader(fr);
		// The first line is file field description
		line = br.readLine();
		while ((line = br.readLine()) != null && !cancel) {
			processLine(line);
		}
		br.close();
		fr.close();
		
		
		for (Integer externalID : coupoundIDNameSynonyms.keySet()) {
			if (cancel) {
				break;
			}
			Set<String> termSynomns = coupoundIDNameSynonyms.get(externalID).getY();
			String term = coupoundIDNameSynonyms.get(externalID).getX();
			List<IExternalID> externalIDs = new ArrayList<IExternalID>();
			ISource source = new SourceImpl(GlobalSources.chebi);
			if(useexternalids)
				externalIDs.add(new ExternalIDImpl(String.valueOf(externalID), source));
			super.addElementToBatch(term, compounds, termSynomns, externalIDs, 0);
			if ((lineNumber % 500) == 0) {
				memoryAndProgress(lineNumber, totalLines);
			}
			if (!cancel && isBatchSizeLimitOvertaken()) {
				IResourceManagerReport reportBatchInserted = super.executeBatch();
				super.updateReport(reportBatchInserted, getReport());
			}
			lineNumber++;
		}
		if (!cancel) {
			IResourceManagerReport reportBatchInserted = super.executeBatch();
			super.updateReport(reportBatchInserted, getReport());
		} else {
			getReport().setcancel();
		}
		long endtime = GregorianCalendar.getInstance().getTimeInMillis();
		getReport().setTime(endtime - startTime);
		return getReport();
	}

	protected void processLine(String line) {
		String[] columns = line.split(csvConfigurations.getGeneralDelimiter().getValue());
		int id = Integer.parseInt(columns[csvConfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters().get(columnIDName).getColumnNumber()]);
		String type = columns[csvConfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters().get(typeName).getColumnNumber()];
		String value = columns[csvConfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters().get(termOrSynonymName).getColumnNumber()];
		if (!coupoundIDNameSynonyms.containsKey(id)) {
			GenericPairImpl<String, Set<String>> nameSynonyms = new GenericPairImpl<String, Set<String>>(new String(), new HashSet<String>());
			coupoundIDNameSynonyms.put(id, nameSynonyms);
		}
		if (type.equals(csvConfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters().get(typeName).getDefaultValue().getValue())) {
			coupoundIDNameSynonyms.get(id).setX(value);
		} else {
			coupoundIDNameSynonyms.get(id).getY().add(value);
		}

	}

	public IDictionary getDictionary() {
		return (IDictionary) getResource();
	}

	@Override
	public void stop() {
		cancel = false;
	}

	public static boolean checkChebiFile(File file) {
		if (!file.isFile()) {
			return false;
		}

		try (FileReader fr = new FileReader(file); 
				BufferedReader br = new BufferedReader(fr)) {
			
			String line = br.readLine();
			Matcher m = checkFile.matcher(line);
			if (m.find()) {
				return true;
			} else {
				return false;
			}

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

}
