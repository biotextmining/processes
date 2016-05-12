package com.silicolife.textmining.processes.ir.epopatent.opshandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import com.silicolife.http.ResponseHandler;
import com.silicolife.http.exceptions.ResponseHandlingException;

public class AutenticationHandler implements ResponseHandler<String>{

	

	public AutenticationHandler()
	{
		
	}
	
	@Override
	public String buildResponse(InputStream response, String responseMessage,Map<String, List<String>> headerFields, int status) throws ResponseHandlingException {
		BufferedReader br = new BufferedReader(new InputStreamReader(response));
		String line;
		try {
			while((line = br.readLine()) != null) 
				{
					if(line.contains("access_token"))
					{
						return line.substring(20,48);
					}
				}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
