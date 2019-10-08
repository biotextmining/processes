package com.silicolife.textmining.processes.resources.dictionary.loaders.kegg.deprecated;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Deprecated
public abstract class AKeggClassLoader {
	
	
	private Pattern externalID = Pattern.compile("ENTRY\\s+(.+)\\s+?");

	public abstract boolean checkFile(File file);
	
	public abstract String getClassLoader();
	
	public boolean checkAllFile(File file, String type) {
		if (!file.isFile()) {
			return false;
		}

		try (FileReader fr = new FileReader(file); BufferedReader br = new BufferedReader(fr)) {
			String line;
			int i = 0;
			while ((line = br.readLine()) != null && i < 100) {
				Matcher m = externalID.matcher(line);
				if (m.find()) {
					if (line.contains(type)) {
						return true;
					} else {
						return false;
					}
				}
				i++;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

}
