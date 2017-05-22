package com.silicolife.textmining.processes.resources.dictionary.loaders.ncbi.pubchem.utils;

public enum PUGRestOutputEnum {

	xml{
		@Override
		public String toString() {
			return "xml";
		}
	},
	json{
		@Override
		public String toString() {
			return "json";
		}
	},

	csv{
		@Override
		public String toString() {
			return "csv";
		}
	},

	sdf{
		@Override
		public String toString() {
			return "sdf";
		}
	},

	txt{
		@Override
		public String toString() {
			return "txt";
		}
	},

	png{
		@Override
		public String toString() {
			return "png";
		}
	}

}
