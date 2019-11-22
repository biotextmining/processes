package com.silicolife.textmining.processes.ir.pubmed.crawl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

//clase usada para obtener el texto de un fichero pdf
public class TryPDFBox {
		
	// obtiene el texto contenido en un fichero pdf dada la ruta en la
	// que se encuentra almacenado localmente
	public static String getTextoPDF(String archivo) {
		String text = null;
		try {
			FileInputStream in = new FileInputStream(archivo);
			PDDocument doc = PDDocument.load(in);
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