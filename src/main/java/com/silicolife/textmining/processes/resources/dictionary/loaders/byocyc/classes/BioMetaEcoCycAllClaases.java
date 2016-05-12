package com.silicolife.textmining.processes.resources.dictionary.loaders.byocyc.classes;

import java.io.File;
import java.util.HashSet;

import com.silicolife.textmining.processes.resources.dictionary.loaders.byocyc.ABioCycMetaCycFileImportAvailable;

public class BioMetaEcoCycAllClaases extends ABioCycMetaCycFileImportAvailable{
	
	private HashSet<ABioCycMetaCycFileImportAvailable> classFilesLoader;

	public BioMetaEcoCycAllClaases()
	{
		this.classFilesLoader = new HashSet<>();
		this.classFilesLoader.add(new BioMetaEcoCycCompound());
		this.classFilesLoader.add(new BioMetaEcoCycEnzyme());
		this.classFilesLoader.add(new BioMetaEcoCycGene());
		this.classFilesLoader.add(new BioMetaEcoCycPathways());
		this.classFilesLoader.add(new BioMetaEcoCycProtein());
		this.classFilesLoader.add(new BioMetaEcoCycReaction());
	}

	@Override
	public boolean checkFile(File file) {
		if(file.isFile())
		{
			return false;
		}
		else
		{
			for(File fileList : file.listFiles())
			{
				for(ABioCycMetaCycFileImportAvailable klass:classFilesLoader)
				{
					if(klass.checkFile(fileList))
					{
						return true;
					}
				}
			}
		}	
		return false;
	}

	@Override
	public String getClassLoader() {
		return "All Files";
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "All Files";
	}

}
