package com.silicolife.textmining.processes.resources.dictionary.loaders.byocyc.classes;

import java.io.File;

import com.silicolife.textmining.processes.resources.dictionary.loaders.byocyc.ABioCycMetaCycFileImportAvailable;
import com.silicolife.textmining.processes.resources.dictionary.loaders.byocyc.BioMetaEcoCycFlatFileLoader;

public class BioMetaEcoCycEnzyme extends ABioCycMetaCycFileImportAvailable{

	
	public final static String enzymes = "Enzyme";

	public BioMetaEcoCycEnzyme() {

	}

	public boolean checkFile(File file) {
		return BioMetaEcoCycFlatFileLoader.cheackGenericFile(file,"# File Name: enzrxns.dat");

	}

	@Override
	public String getClassLoader() {
		return enzymes;
	}

	@Override
	public String toString() {
		return enzymes;
	}
	
}
