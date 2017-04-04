package com.silicolife.textmining.processes.resources.dictionary.loaders.ncbi.gene;

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
import java.util.Properties;
import java.util.Set;

import com.silicolife.textmining.core.datastructures.general.ExternalIDImpl;
import com.silicolife.textmining.core.datastructures.general.SourceImpl;
import com.silicolife.textmining.core.datastructures.resources.dictionary.loaders.DictionaryLoaderHelp;
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
 * 
 * 
 * @author Hugo Costa
 *
 * @version 1.0
 * 
 * @since August 27th 2012
 * 
 * 
 */
public class NCBIGeneFlatFileLoader extends DictionaryLoaderHelp implements IDicionaryFlatFilesLoader {

//	private static String oldfirstLine = "#Format: tax_id GeneID Symbol LocusTag Synonyms dbXrefs";
	private static String newfirstLine = "#tax_id	GeneID	Symbol	LocusTag	Synonyms	dbXrefs";

	private String defaultTypeValue = "NEWENTRY";
	private CSVFileConfigurations csvConfigurations;
	private int columnTAXIDNumber = 0;
	private int columnIDNumber = 1;
	private int columnTermNumber = 2;
	private int columnSynonymNumber = 4;
	private int columnOtherExternalID = 5;
	private int columnOtherSynonymsNumber = 13;

	private String columnTaxtIDName = "tax_id";
	private String columnIDName = "GeneID";
	private String columntermName = "Symbol";
	private String columntermSynonym = "Synonyms";
	private String columnTermExternalDatabaseIDs = "dbXrefs";
	private String columnOtherDesignations = "Other_designations";
	private boolean cancel = false;

	public final static String gene = "Gene";
	public final static String propertyOrganism = "Organism Taxonomy ID";
	public final static String propertyHypthotethicalProteins = "Hypothetical protein";


	public NCBIGeneFlatFileLoader() {
		super(GlobalSources.entrezgene);
		initCSVConfigurations();
	}

	private void initCSVConfigurations() {
		TextDelimiter textdelimiter = TextDelimiter.NONE;
		Delimiter generalDelimiter = Delimiter.TAB;
		Map<String, ColumnParameters> columnNameColumnParameters = new HashMap<String, ColumnParameters>();
		ColumnParameters columnTaxID = new ColumnParameters(columnTAXIDNumber, Delimiter.USER, DefaultDelimiterValue.HYPHEN);
		ColumnParameters columnID = new ColumnParameters(columnIDNumber, Delimiter.USER, DefaultDelimiterValue.HYPHEN);
		ColumnParameters columnTErm = new ColumnParameters(columnTermNumber, Delimiter.USER, DefaultDelimiterValue.USER);
		columnTErm.getDefaultValue().setUserDelimiter(defaultTypeValue);
		ColumnParameters columnSynonym = new ColumnParameters(columnSynonymNumber, Delimiter.VERTICAL_BAR, DefaultDelimiterValue.USER);
		ColumnParameters columnOtherSynonyms = new ColumnParameters(this.columnOtherSynonymsNumber, Delimiter.VERTICAL_BAR, DefaultDelimiterValue.USER);
		ColumnParameters columnExternalIds = new ColumnParameters(columnOtherExternalID, Delimiter.VERTICAL_BAR, DefaultDelimiterValue.HYPHEN, Delimiter.COLON);
		columnNameColumnParameters.put(columnTaxtIDName, columnTaxID);
		columnNameColumnParameters.put(columnIDName, columnID);
		columnNameColumnParameters.put(columntermName, columnTErm);
		columnNameColumnParameters.put(columntermSynonym, columnSynonym);
		columnNameColumnParameters.put(columnTermExternalDatabaseIDs, columnExternalIds);
		columnNameColumnParameters.put(columnOtherDesignations, columnOtherSynonyms);
		ColumnDelemiterDefaultValue columsDelemiterDefaultValue = new ColumnDelemiterDefaultValue(columnNameColumnParameters);
		csvConfigurations = new CSVFileConfigurations(generalDelimiter, textdelimiter, DefaultDelimiterValue.HYPHEN, columsDelemiterDefaultValue, true);
	}

	public boolean checkFile(File file) {
		if (!file.isFile()) {
			return false;
		}
		try (FileReader fr = new FileReader(file); BufferedReader br = new BufferedReader(fr)) {
			getReport().updateFile(file);
			String line = br.readLine();
			if (line.startsWith(newfirstLine)) {
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	public IResourceUpdateReport loadTerms(IDictionaryLoaderConfiguration configuration) throws ANoteException, IOException {
		cancel = false;
		super.setResource(configuration.getDictionary());
		Properties properties = configuration.getProperties();
		File file = configuration.getFlatFile();
		boolean importExternlIDs = configuration.loadExternalIDs();
		getReport().addClassesAdding(1);
		getReport().updateFile(file);
		BufferedReader br;
		String line;
		long nowTime;
		int total = 0;
		FileReader fr;
		String organismId = new String();
		boolean hyphotetical = false;
		if (properties.containsKey(propertyOrganism)) {
			organismId = properties.getProperty(propertyOrganism);
		}
		if (properties.containsKey(propertyHypthotethicalProteins) && Boolean.valueOf(properties.get(propertyHypthotethicalProteins).toString())) {
			hyphotetical = true;
		}
		total = getLines(file);
		long startTime = GregorianCalendar.getInstance().getTimeInMillis();
		int lineNumber = 0;
		fr = new FileReader(file);
		br = new BufferedReader(fr);
		// Read First Line
		br.readLine();
		while ((line = br.readLine()) != null && !cancel) {
			if (!cancel && (!hyphotetical || !line.contains("hypothetical protein"))) {
				processLine(line, organismId,importExternlIDs);
			}
			if ((lineNumber % 500) == 0) {
				memoryAndProgress(lineNumber, total);
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
		}
		long end = GregorianCalendar.getInstance().getTimeInMillis();
		nowTime = end - startTime;
		getReport().setTime(nowTime);
		br.close();
		return getReport();
	}

	protected void processLine(String line, String organismId, boolean importExternlIDs) throws ANoteException, IOException {
		String[] columns = line.split(csvConfigurations.getGeneralDelimiter().getValue());
		String taxomomyID = columns[csvConfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters().get(columnTaxtIDName).getColumnNumber()];
		if (organismId.isEmpty() || taxomomyID.equals(organismId)) {

			String term = columns[csvConfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters().get(columntermName).getColumnNumber()];
			if (!term.equals(defaultTypeValue)) {
				String geneid = columns[csvConfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters().get(columnIDName).getColumnNumber()];
				String syns = columns[csvConfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters().get(columntermSynonym).getColumnNumber()];
				String otherSynonym = columns[csvConfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters().get(columnOtherDesignations).getColumnNumber()];
				String externalIDs = columns[csvConfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters().get(columnTermExternalDatabaseIDs).getColumnNumber()];
				Set<String> termSynomns = parseSynonyms(syns);
				Set<String> otherSynonyms = parseOtherSynonyms(otherSynonym);
				termSynomns.addAll(otherSynonyms);
				List<IExternalID> externalDs = new ArrayList<>();
				if(importExternlIDs)
				{
					externalDs = parseExternalIDs(externalIDs);
					ISource source = new SourceImpl(GlobalSources.entrezgene);
					externalDs.add(new ExternalIDImpl(geneid, source));
				}
				super.addElementToBatch(term, gene, termSynomns, externalDs, 0);
			}
		}

	}

	private List<IExternalID> parseExternalIDs(String externalIDs) {
		String database;
		String databaseID;
		List<IExternalID> externalDs = new ArrayList<IExternalID>();
		if (!externalIDs.equals(csvConfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters().get(columnTermExternalDatabaseIDs).getDefaultValue().getValue())) {
			String[] ids = externalIDs.split(csvConfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters().get(columnTermExternalDatabaseIDs).getDelimiter()
					.getValue());

			for (String sourceandID : ids) {
				String[] sourceNameExtID = sourceandID.split(csvConfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters().get(columnTermExternalDatabaseIDs)
						.getSubDelimiter().getValue());
				database = sourceNameExtID[0];
				databaseID = sourceNameExtID[1];
				ISource source = new SourceImpl(database);
				externalDs.add(new ExternalIDImpl(databaseID, source));
			}
		}
		return externalDs;
	}

	private Set<String> parseSynonyms(String syns) {
		Set<String> result = new HashSet<String>();
		if (!syns.equals(csvConfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters().get(columntermSynonym).getDefaultValue().getValue())) {
			String[] columns = syns.split(csvConfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters().get(columntermSynonym).getDelimiter().getValue());
			for (String sy : columns) {
				result.add(sy);
			}
		}
		return result;
	}

	private Set<String> parseOtherSynonyms(String syns) {
		Set<String> result = new HashSet<String>();
		if (!syns.equals(csvConfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters().get(columnOtherDesignations).getDefaultValue().getValue())) {
			String[] columns = syns
					.split(csvConfigurations.getColumsDelemiterDefaultValue().getColumnNameColumnParameters().get(columnOtherDesignations).getDelimiter().getValue());
			for (String sy : columns) {
				result.add(sy);
			}
		}
		return result;
	}

	@Override
	public IDictionary getDictionary() {
		return (IDictionary) getResource();
	}

	protected int getLines(File file) throws IOException {
		int total = 0;
		FileReader fr;
		fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		total = 0;
		while ((br.readLine()) != null) {
			total++;
		}
		br.close();
		return total;
	}

	@Override
	public void stop() {
		this.cancel = true;
	}

	public static boolean checkEntrezgeneFile(File file) {
		if (!file.isFile()) {
			return false;
		}
		try (FileReader fr = new FileReader(file); BufferedReader br = new BufferedReader(fr)) {
			String line = br.readLine();
			if (line.startsWith(newfirstLine)) {
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}
}
