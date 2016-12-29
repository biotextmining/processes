package com.silicolife.textmining.processes.ir.patentpipeline.components.retrievalmodules.wipo.help;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.silicolife.textmining.core.interfaces.core.document.IPublication;

public class WIPOXMLSAXPHandler extends DefaultHandler{
	String tempString;
	IPublication pub; 
	String authors;
	public WIPOXMLSAXPHandler(IPublication pub){
		this.pub=pub;

	}

	public void startElement(String s, String s1, String elementName, Attributes attributes) throws SAXException{
		//no operation


	}

	public void endElement(String s, String s1, String element) throws SAXException{
		if (element.equalsIgnoreCase("date")){
			if (pub.getFulldate()==null||pub.getFulldate().isEmpty()){
				pub.setYeardate(tempString);
			}
		}
		if (element.equalsIgnoreCase("invention-title")){
			if (pub.getTitle()==null||pub.getTitle().isEmpty()){
				pub.setTitle(tempString);	
			}
		}
		if (element.equalsIgnoreCase("name")){
			if (authors==null||authors.isEmpty()){
				//pub.setAuthors(tempString);
				authors=tempString;
			}
			else if(!authors.contains(tempString)){
				//pub.setAuthors(pub.getAuthors()+" AND "+tempString);
				authors=authors+" AND "+tempString;
			}
		}
		if (element.equalsIgnoreCase("abstract")){
			if (pub.getAbstractSection()==null||pub.getAbstractSection().isEmpty()){
				pub.setAbstractSection(tempString);
			}
		}
		if (element.equalsIgnoreCase("applicants")){
			if (pub.getAuthors()==null||pub.getAuthors().isEmpty()){
				pub.setAuthors(authors);
			}
			
		}

	}

	public void characters(char[] ac, int i, int j) throws SAXException {
		tempString=new String(ac,i,j);//initialize the string with the correspondent information


	}

}

