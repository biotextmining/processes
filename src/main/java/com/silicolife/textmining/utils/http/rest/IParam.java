package com.silicolife.textmining.utils.http.rest;

public interface IParam<T> {
	
	T getRaw();
	String buildString(String key);
	
}
