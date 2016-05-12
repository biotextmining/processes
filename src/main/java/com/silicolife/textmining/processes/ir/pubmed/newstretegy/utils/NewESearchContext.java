package com.silicolife.textmining.processes.ir.pubmed.newstretegy.utils;

public class NewESearchContext {
	private String webEnv;
	private String queryKey;
	private int count;

	public NewESearchContext(final String webEnv, final String queryKey,
			int count) {
		super();
		this.webEnv = webEnv;
		this.queryKey = queryKey;
		this.count = count;
	}

	@Override
	public String toString() {
		return "context: " + this.webEnv + " " + this.queryKey;
	}

	public String getWebEnv() {
		return webEnv;
	}


	public String getQueryKey() {
		return queryKey;
	}

	public int getCount() {
		return count;
	}
	
}
