package com.silicolife.textmining.processes.ie.pipelines.kineticparameters.steps;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import com.silicolife.textmining.core.interfaces.core.annotation.IEventAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IAnnotatedDocument;
import com.silicolife.textmining.processes.ie.re.kineticre.io.ExportKineticResultsTOCSV;
import com.silicolife.textmining.processes.ie.re.kineticre.io.IREKineticREResultsExportConfiguration;

public class ExportKineticResultsTOCSVExtension extends ExportKineticResultsTOCSV{
	
	private IREKineticREResultsExportConfiguration configuration;
	private PrintWriter pw;
	
	public ExportKineticResultsTOCSVExtension(IREKineticREResultsExportConfiguration configuration)
	{
		this.configuration=configuration;
		try {
			File file = new File(configuration.getExportFile());	
			if(!file.exists())
				file.createNewFile();
			pw = new PrintWriter(configuration.getExportFile());
			super.writeHeaderLine(pw);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeEvent(IAnnotatedDocument docAnnot, IEventAnnotation ev) throws ANoteException {				
		try {
			super.writeline(pw, docAnnot, ev, configuration);
		} catch (IOException e) {
			throw new ANoteException(e);
		}	
	}

	public void close()
	{
		pw.close();
	}
}
