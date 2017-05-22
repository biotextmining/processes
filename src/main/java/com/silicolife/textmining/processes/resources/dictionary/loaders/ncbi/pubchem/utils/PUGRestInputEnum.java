package com.silicolife.textmining.processes.resources.dictionary.loaders.ncbi.pubchem.utils;

public enum PUGRestInputEnum {

	compoundName{
		@Override
		public String toString() {
			return "name";
		}
	},
	compoundIdentifier{
		@Override
		public String toString() {
			return "cid";
		}
	},
	
	smiles{
		@Override
		public String toString() {
			return "smiles";
		}
	},

	inchikey{
		@Override
		public String toString() {
			return "inchikey";
		}
	}
	
}