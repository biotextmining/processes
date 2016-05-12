package com.silicolife.textmining.processes.resources.dictionary.loaders.byocyc.classes;

import java.io.File;

import com.silicolife.textmining.processes.resources.dictionary.loaders.byocyc.ABioCycMetaCycFileImportAvailable;
import com.silicolife.textmining.processes.resources.dictionary.loaders.byocyc.BioMetaEcoCycFlatFileLoader;

public class BioMetaEcoCycGene extends ABioCycMetaCycFileImportAvailable{
	
	public final static String gene = "Gene";

	public BioMetaEcoCycGene() {
		super();
	}

	public boolean checkFile(File file) {
		return BioMetaEcoCycFlatFileLoader.cheackGenericFile(file,"# File Name: genes.dat");

	}

	@Override
	public String getClassLoader() {
		return gene;
	}

	@Override
	public String toString() {
		return gene;
	}
	
}
