package com.silicolife.textmining.processes.resources.dictionary.loaders.kegg.deprecated.classes;

import java.io.File;

import com.silicolife.textmining.processes.resources.dictionary.loaders.kegg.deprecated.AKeggClassLoader;

@Deprecated
public class KeggReactions extends AKeggClassLoader{
		
	public static final String klass = "Reaction";

	
	public boolean checkFile(File file) {
		return checkAllFile(file, klass);
	}


	@Override
	public String getClassLoader() {
		return klass;
	}

	public String toString() {
		return klass;
	}

}

