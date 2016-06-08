package com.silicolife.textmining.utils.http.rest.params;

import com.silicolife.textmining.utils.http.rest.IParam;

public class IntegerParam implements IParam<Integer> {

	protected Integer value;
	
	public IntegerParam(Integer integer) {
		this.value = integer;
	}
	
	@Override
	public String buildString(String key) {
		return key + "=" + String.valueOf(value);
	}
	
	@Override
	public Integer getRaw() {
		return value;
	}
}
