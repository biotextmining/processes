package com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.bing.entities;

public class BingWebQuery extends ABingQuery{

	/**
	 * @return the bingsearch source name
	 */
	public String getbingsearch(){
		return bingsearch;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public String getQueryPath() {
		return this.getPath();
	}


}
