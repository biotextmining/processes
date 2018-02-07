package com.silicolife.textmining.processes.resources.dictionary.loaders.kegg.webservice;

public enum KeggEntitiesEnum {
	Compound
	{
		public String getClassEntity() { return "metabolite";}
		public String getKeggShortIndentifier(){ return "cpd";}
	},
	Glycan
	{
		public String getClassEntity() { return "metabolite";}
		public String getKeggShortIndentifier(){ return "gl";}
	},
	Drugs	{
		public String getClassEntity() { return "metabolite";}
		public String getKeggShortIndentifier(){ return "dr";}
	},
	Enzymes
	{
		public String getClassEntity() { return "enzyme";}
		public String getKeggShortIndentifier(){ return "ec";}
	},;
	
	public String getClassEntity()
	{	
		return this.getClassEntity();
	}
	
	public String getKeggShortIndentifier()
	{
		return this.getKeggShortIndentifier();
	}
	
}
