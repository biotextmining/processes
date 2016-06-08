package com.silicolife.textmining.utils.http.rest;


import java.util.HashMap;
import java.util.Map;

import com.silicolife.textmining.utils.http.utils.CollectionUtils;

public class RequestParameters {
	
	Map<String, IParam<?>> parameters;
	
	public RequestParameters() {
		parameters = new HashMap<String, IParam<?>>();
	}

	public String join() {
		String[] out = new String[parameters.size()];
		int i = 0;
		for(String key : parameters.keySet()) {
			out[i] = parameters.get(key).buildString(key);
			i++;
		}
		return CollectionUtils.join(out, "&");
		
	}
	
	public void put(String key, IParam<?> value) {
		parameters.put(key, value);
	}
	

}
