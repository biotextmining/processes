package com.silicolife.textmining.processes.corpora.loaders;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.silicolife.textmining.core.interfaces.core.corpora.loaders.ICorpusLoader;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.processes.corpora.readers.PatentMetaFilesReader;

public class PatentMetaFilesCorpusLoader implements ICorpusLoader{

	@Override
	public List<IPublication> processFile(File fileOrDirectory, Properties properties) throws ANoteException, IOException {
		validateFile(fileOrDirectory);
		PatentMetaFilesReader reader = new PatentMetaFilesReader();
		List<IPublication> out = new ArrayList<>();
		for(File file:fileOrDirectory.listFiles())
		{
			if(file.getName().endsWith(".meta"))
			{
				InputStream inStream = new FileInputStream(file);
				out.addAll(reader.getPatent(inStream , file));
			}
		}
		return out;
	}

	@Override
	public boolean validateFile(File metainformationdirectory) {
		if(metainformationdirectory.isDirectory())
		{
			for(File file:metainformationdirectory.listFiles())
			{
				if(!file.getName().endsWith(".meta") &&  !file.getName().endsWith(".txt"))
				{
					return false;
				}
			}
		}
		else 
			return false;
		return true;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

}
