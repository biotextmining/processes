package com.silicolife.textmining.utils.http;


import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.silicolife.textmining.utils.http.exceptions.ResponseHandlingException;

public interface ResponseHandler<T> {

	public T buildResponse(InputStream response, String responseMessage, Map<String, List<String>> headerFields, int status) throws ResponseHandlingException;
	
}
