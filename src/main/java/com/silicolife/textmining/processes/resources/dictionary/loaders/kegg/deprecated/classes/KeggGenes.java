package com.silicolife.textmining.processes.resources.dictionary.loaders.kegg.deprecated.classes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.silicolife.textmining.processes.resources.dictionary.loaders.kegg.deprecated.AKeggClassLoader;

@Deprecated
public class KeggGenes extends AKeggClassLoader {

	private Pattern ntseq = Pattern.compile("NTSEQ");
	public final static String klass = "Gene";

	public KeggGenes() {
		super();
	}

	public boolean checkFile(File file) {

		try (FileReader fr = new FileReader(file); BufferedReader br = new BufferedReader(fr)) {
			String line;
			int i = 0;
			while ((line = br.readLine()) != null && i < 100) {
				Matcher m = ntseq.matcher(line);
				if (m.find()) {
					return true;
				}
				i++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override
	public String getClassLoader() {
		return klass;
	}

	public String toString() {
		return klass;
	}

}
