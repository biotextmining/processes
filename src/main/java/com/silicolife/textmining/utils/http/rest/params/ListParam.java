package com.silicolife.textmining.utils.http.rest.params;


import java.util.List;

import com.silicolife.textmining.utils.http.rest.IParam;
import com.silicolife.textmining.utils.http.utils.CollectionUtils;

public class ListParam implements IParam<List<IParam<?>>> {

	protected List<IParam<?>> value;
	
	public ListParam(List<IParam<?>> list) {
		this.value = list;
	}
	
	@Override
	public List<IParam<?>> getRaw() {
		return value;
	}

	@Override
	public String buildString(String key) {
		String[] join = new String[value.size()];
		int i =0;
		for(IParam<?> param : value) {
			join[i] = key + "=" + param.getRaw().toString(); 
			i++;
		}
		return CollectionUtils.join(join, "&");
	}
	
	

}
