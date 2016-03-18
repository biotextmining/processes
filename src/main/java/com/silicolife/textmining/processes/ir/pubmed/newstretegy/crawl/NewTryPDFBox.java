package com.silicolife.textmining.processes.ir.pubmed.newstretegy.crawl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

//clase usada para obtener el texto de un fichero pdf
public class NewTryPDFBox {
		
	// obtiene el texto contenido en un fichero pdf dada la ruta en la
	// que se encuentra almacenado localmente
	public static String getTextoPDF(String archivo) {
		String text = null;
		try {
			FileInputStream in = new FileInputStream(archivo);
			PDFParser parser = new PDFParser(in);
			parser.parse();
			PDDocument doc = parser.getPDDocument();
			if (doc != null) {
				PDFTextStripper stripper = new PDFTextStripper();
				text = stripper.getText(doc);
				doc.close();
			} else {
				text = null;
			}
			in.close();
		} catch (FileNotFoundException fnfe) {
			text = null;
//			System.err.println("No se ha encontrado el fichero " + archivo);
		} catch (IOException ioe) {;
			text = null;
//			System.err.println("Error de E/S");
//			ioe.printStackTrace();
		} catch(Exception cl){
			text = null;
		}
		return text;
	}
}