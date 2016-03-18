package com.silicolife.textmining.processes.resources.dictionary.loaders.byocyc;

import java.io.File;


public abstract class  ABioCycMetaCycFileImportAvailable {
		
	public ABioCycMetaCycFileImportAvailable()
	{
		
	}

	public abstract boolean checkFile(File file);
	
	public abstract String getClassLoader();

}
