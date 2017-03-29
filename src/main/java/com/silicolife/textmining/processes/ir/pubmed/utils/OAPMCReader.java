package com.silicolife.textmining.processes.ir.pubmed.utils;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;

public class OAPMCReader {
	
	private InputStream stream;

	public OAPMCReader(InputStream stream) {
		this.stream = stream;
	}
	
	/**
	 * 
	 * 
	 * @return String[0] pdf link and String[1] tgz link
	 * @throws ANoteException
	 */
	public String[] getPDFURL() throws ANoteException{
		try{
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(getInputStream());
			String[] urls= new String[2];
			urls[0] = getURLGivenFormat(doc,"pdf");
			urls[1] = getURLGivenFormat(doc,"tgz");
			return urls;

		}catch(SAXException | IOException | ParserConfigurationException e){
			throw new ANoteException(e);
		}
	}
	
	private String getURLGivenFormat(Document doc,String format)
	{
		NodeList nodesLinks = doc.getElementsByTagName("link");
		for(int i=0;i<nodesLinks.getLength();i++)
		{
			Node formatType = nodesLinks.item(i).getAttributes().getNamedItem("format");
			if(formatType!=null)
			{
				if(formatType.getNodeValue().equals(format))
				{
					Node href = nodesLinks.item(i).getAttributes().getNamedItem("href");
					if(href!=null)
					{
						return href.getNodeValue();
					}
				}

			}
		}
		return null;
	}
	
	private InputStream getInputStream(){
		return stream;
	}

}
