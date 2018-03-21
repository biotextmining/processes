package com.silicolife.textmining.processes.ir.epopatent.opshandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import com.silicolife.textmining.utils.http.ResponseHandler;
import com.silicolife.textmining.utils.http.exceptions.ResponseHandlingException;

public class AutenticationHandler implements ResponseHandler<String>{



	public AutenticationHandler()
	{

	}

	@Override
	public String buildResponse(InputStream response, String responseMessage,Map<String, List<String>> headerFields, int status) throws ResponseHandlingException {
		StringWriter writer = new StringWriter();
		try {
			IOUtils.copy(response, writer, "UTF-8");
			String theString = writer.toString();
			JSONObject responseJSON = new JSONObject(theString);
			String accessToken = responseJSON.getString("access_token");
			return accessToken;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
