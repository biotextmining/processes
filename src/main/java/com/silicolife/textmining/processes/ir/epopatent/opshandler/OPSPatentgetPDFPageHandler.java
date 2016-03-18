package com.silicolife.textmining.processes.ir.epopatent.opshandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.silicolife.http.ResponseHandler;
import com.silicolife.http.exceptions.ResponseHandlingException;

public class OPSPatentgetPDFPageHandler implements ResponseHandler<File> {

	private String pathDoc;

	public OPSPatentgetPDFPageHandler(String pathDoc) {
		this.pathDoc = pathDoc;
	}

	@Override
	public File buildResponse(InputStream response, String responseMessage, Map<String, List<String>> headerFields, int status) throws ResponseHandlingException {
		Long time = GregorianCalendar.getInstance().getTimeInMillis();
		File file = new File(pathDoc + "/" + time.toString() + ".pdf");

		try {
			FileOutputStream outputStream = new FileOutputStream(file);
			file.createNewFile();
			IOUtils.copy(response, outputStream);
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}

}
