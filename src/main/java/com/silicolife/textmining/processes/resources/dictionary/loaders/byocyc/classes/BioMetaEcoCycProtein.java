package com.silicolife.textmining.processes.resources.dictionary.loaders.byocyc.classes;

import java.io.File;

import com.silicolife.textmining.processes.resources.dictionary.loaders.byocyc.ABioCycMetaCycFileImportAvailable;
import com.silicolife.textmining.processes.resources.dictionary.loaders.byocyc.BioMetaEcoCycFlatFileLoader;

public class BioMetaEcoCycProtein extends ABioCycMetaCycFileImportAvailable{
	
	public final static String protein = "Protein";


	public BioMetaEcoCycProtein() {
	}

	public boolean checkFile(File file) {
		return BioMetaEcoCycFlatFileLoader.cheackGenericFile(file,"# File Name: proteins.dat");

	}

	@Override
	public String getClassLoader() {
		return protein;
	}

	@Override
	public String toString() {
		return protein;
	}



}
