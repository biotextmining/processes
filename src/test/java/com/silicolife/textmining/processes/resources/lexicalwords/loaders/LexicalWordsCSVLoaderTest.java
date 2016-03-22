package com.silicolife.textmining.processes.resources.lexicalwords.loaders;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.silicolife.textmining.core.datastructures.init.exception.InvalidDatabaseAccess;
import com.silicolife.textmining.core.datastructures.resources.lookuptable.loader.csvstandard.ColumnNames;
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
import com.silicolife.textmining.processes.resources.lexicalwords.CreateLexicalWordsTest;
import com.silicolife.textmining.processes.resources.lexicalwords.csvlader.LexicalWordsCSVLoader;

public class LexicalWordsCSVLoaderTest {

	@Test
	public void test() throws ANoteException, IOException, InvalidDatabaseAccess {
		DatabaseConnectionInit.init("localhost","3306","createdatest","root","admin");
		IResource<IResourceElement> lw = CreateLexicalWordsTest.createLexicalWords("lexical Words");
		LexicalWordsCSVLoader lwCSVLoader = new LexicalWordsCSVLoader();
		boolean hasHeaders = false;
		Map<String, ColumnParameters> columnNameColumnParameters = new HashMap<String, ColumnParameters>();
		ColumnParameters value = new ColumnParameters(0, null, null);
		columnNameColumnParameters.put(ColumnNames.term, value );
		ColumnDelemiterDefaultValue columsDelemiterDefaultValue = new ColumnDelemiterDefaultValue(columnNameColumnParameters);
		Delimiter generalDelimiter = Delimiter.TAB;
		DefaultDelimiterValue defaultValue = DefaultDelimiterValue.NONE;
		TextDelimiter textDelimiters = TextDelimiter.NONE;
		CSVFileConfigurations csvfileconfigurations = new CSVFileConfigurations(generalDelimiter, textDelimiters, defaultValue, columsDelemiterDefaultValue, hasHeaders);
		File file = new File("src/test/resources/biological_verbs.csv");
		IResourceUpdateReport report = lwCSVLoader.loadTermFromGenericCVSFile(lw, file, csvfileconfigurations);
		assertTrue(report.isFinishing());
	}

}
