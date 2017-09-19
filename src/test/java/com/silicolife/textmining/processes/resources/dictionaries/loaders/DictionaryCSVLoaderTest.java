package com.silicolife.textmining.processes.resources.dictionaries.loaders;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.silicolife.textmining.core.datastructures.init.exception.InvalidDatabaseAccess;
import com.silicolife.textmining.core.datastructures.resources.export.ResourceExportColumnEnum;
import com.silicolife.textmining.core.datastructures.utils.generic.CSVFileConfigurations;
import com.silicolife.textmining.core.datastructures.utils.generic.ColumnDelemiterDefaultValue;
import com.silicolife.textmining.core.datastructures.utils.generic.ColumnParameters;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.report.resources.IResourceUpdateReport;
import com.silicolife.textmining.core.interfaces.process.IE.io.export.DefaultDelimiterValue;
import com.silicolife.textmining.core.interfaces.process.IE.io.export.Delimiter;
import com.silicolife.textmining.core.interfaces.process.IE.io.export.TextDelimiter;
import com.silicolife.textmining.core.interfaces.resource.IResource;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;
import com.silicolife.textmining.processes.DatabaseConnectionInit;
import com.silicolife.textmining.processes.resources.dictionaries.CreateDictionaryTest;
import com.silicolife.textmining.processes.resources.dictionary.loaders.csvstandard.DictionaryStandardCSVLoader;

public class DictionaryCSVLoaderTest {

	@Test
	public void test() throws InvalidDatabaseAccess, ANoteException, IOException {
		DatabaseConnectionInit.init("localhost","3306","createdatest","root","admin");
		IResource<IResourceElement> dictionary = CreateDictionaryTest.createDictionary("Brenda - fdadsf","");
		DictionaryStandardCSVLoader dictionaryCSVLoader = new DictionaryStandardCSVLoader();
		boolean hasHeaders = true;
		Map<String, ColumnParameters> columnNameColumnParameters = new HashMap<String, ColumnParameters>();
		ColumnParameters value = new ColumnParameters(0, null, null);
		// Primary Term
		columnNameColumnParameters.put(ResourceExportColumnEnum.term.toString(), value );
		// Class
		ColumnParameters klassColumn = new ColumnParameters(1, null, null);
		columnNameColumnParameters.put(ResourceExportColumnEnum.classe.toString(), klassColumn );
		// Synonyms
		ColumnParameters synonymColumn = new ColumnParameters(2, Delimiter.VERTICAL_BAR, DefaultDelimiterValue.HYPHEN);
		columnNameColumnParameters.put(ResourceExportColumnEnum.synonyms.toString(), synonymColumn );
		ColumnDelemiterDefaultValue columsDelemiterDefaultValue = new ColumnDelemiterDefaultValue(columnNameColumnParameters);
		// External Ids
		Delimiter subdelimiter = Delimiter.USER;
		subdelimiter.setUserDelimiter(":");
		ColumnParameters externalIdsColumn = new ColumnParameters(3, Delimiter.VERTICAL_BAR, DefaultDelimiterValue.HYPHEN,subdelimiter);
		columnNameColumnParameters.put(ResourceExportColumnEnum.externalID.toString(), externalIdsColumn );
		
		// General 
		Delimiter generalDelimiter = Delimiter.TAB;
		DefaultDelimiterValue defaultValue = DefaultDelimiterValue.NONE;
		TextDelimiter textDelimiters = TextDelimiter.NONE;
		CSVFileConfigurations csvfileconfigurations = new CSVFileConfigurations(generalDelimiter, textDelimiters, defaultValue, columsDelemiterDefaultValue, hasHeaders);
		File file = new File("src/test/resources/sisbi/resources/Enzymes_02_08_16.txt");
		IResourceUpdateReport report = dictionaryCSVLoader.loadTermFromGenericCVSFile(dictionary, file, csvfileconfigurations);
		assertTrue(report.isFinishing());
	}

}
