package com.silicolife.textmining.processes.ie.re.kineticre.configuration;

public enum KineticREClassesEnum {
	 None{
		 @Override
		public String toString() {
			return "--";
		}
	 },
	 Units{
		 @Override
		public String toString() {
			return "Units";
		}
	 },
	 Values{
		 @Override
		public String toString() {
			return "Values";
		}
	 },
	 KineticParameters{
		 @Override
		public String toString() {
			return "Kinetic Parameters";
		}
	 }, 
	 Metabolites{
		 @Override
		public String toString() {
			return "Metabolites";
		}
	 }, 
	 Enzymes{
		 @Override
		public String toString() {
			return "Enzymes";
		}
	 }, 
	 Organism{
		 @Override
		public String toString() {
			return "Organism";
		}
	 };
}
