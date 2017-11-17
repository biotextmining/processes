package com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.entitytagger.entities.species;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

public class ExtractMesh {

	public static HashMap<String,Integer> loadMeshToTaxFile(File file){
		HashMap<String,Integer> res = new HashMap<String,Integer>();
		try{
			BufferedReader inStream = new BufferedReader(new FileReader(file));

			String line = inStream.readLine();

			while (line != null){
				if (!line.startsWith("#")){
					String[] fields = line.split("\t");
					res.put(fields[2],Integer.parseInt(fields[1]));
				}
				line = inStream.readLine();
			}

			inStream.close();
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
		return res;
	}
}
