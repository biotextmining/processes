package com.silicolife.textmining.processes.resources.dictionary.loaders.byocyc.classes;

import java.io.File;

import com.silicolife.textmining.processes.resources.dictionary.loaders.byocyc.ABioCycMetaCycFileImportAvailable;
import com.silicolife.textmining.processes.resources.dictionary.loaders.byocyc.BioMetaEcoCycFlatFileLoader;

public class BioMetaEcoCycCompound extends ABioCycMetaCycFileImportAvailable{
	
	public final static String compounds = "Compound";

	public BioMetaEcoCycCompound() {
		
	}

	public boolean checkFile(File file) {
		return BioMetaEcoCycFlatFileLoader.cheackGenericFile(file,"# File Name: compounds.dat");
	}

	@Override
	public String getClassLoader() {
		return compounds;
	}

	@Override
	public String toString() {
		return compounds;
	}
	
}
