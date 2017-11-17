package com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.dataholders;

import java.io.Serializable;

public class Document implements Serializable {
	public enum Type {RESEARCH, REVIEW, OTHER}
	public enum Text_raw_type {XML, OCR, PDF2TEXT, TEXT} 

	private static final long serialVersionUID = 6268131204084207996L;
	
	private String ID;

	private String title, abs, body, rawContent;
	
	private Text_raw_type raw_type;
	private String year;
	private boolean ignoreCoordinates = false;
	
	private Author[] authors;
	private Journal journal;
	private ExternalID externalID;
	private Type type;
	
	private String volume;
	private String issue;
	private String pages;
	
	private String xml;
	
	public Document(String id, String title, String abs, String body, String raw, Text_raw_type raw_type, String year, 
			Journal journal, Type type, Author[] authors, String volume, String issue, String pages, String xml, ExternalID externalID){
		this.ID = id;
		this.title = title;
		this.abs = abs;
		this.body = body;
		this.rawContent = raw;
		this.raw_type = raw_type;
		this.year = year;
		this.journal = journal;
		this.type = type;
		this.authors = authors;
		this.volume = volume;
		this.issue = issue;
		this.pages = pages;
		this.xml = xml;
		this.externalID = externalID;
	}
	
	
	public String getID() {
		return ID;
	}

	public boolean isValid(int start, @SuppressWarnings("unused") int end){
		return true;
	}

	public void print(){
		System.out.println(this.toString());
		if (authors != null){
			System.out.println("Authors:");
			for (Author a : authors)
				System.out.println(a.toString());
		}
	}
	

	
	

	public String getTitle() {
		return title;
	}

	public String getYear() {
		return year;
	}

	public boolean hasTitle(){
		return title != null && title.length() > 5;
	}

	public String toHTML(){
		StringBuffer sb = new StringBuffer();

		if (title != null){
			sb.append("<b>" + title.toString() + "</b><br><br>");
		} else {
			sb.append("[Title missing]");
		}
		if (abs != null){
			sb.append("<b>" + abs.toString() + "</b><br><br>");
		} else {
			sb.append("[Abstract missing]");
		}
		if (body != null){
			sb.append("<b>" + body.toString() + "</b><br><br>");
		} else {
			sb.append("[Body missing]");
		}
		if (rawContent != null){
			sb.append("<b>" + rawContent.toString() + "</b><br><br>");
		} else {
			sb.append("[Raw content missing]");
		}

		return sb.toString();		
	}

	public String getRawContent() {
		return rawContent;
	}

	/**
	 * @return the ignoreCoordinates
	 */
	public boolean isIgnoreCoordinates() {
		return ignoreCoordinates;
	}

	/**
	 * @param ignoreCoordinates the ignoreCoordinates to set
	 */
	public void setIgnoreCoordinates(boolean ignoreCoordinates) {
		this.ignoreCoordinates = ignoreCoordinates;
	}


	/**
	 * @return the serialVersionUID
	 */
	public static long getSerialVersionUID() {
		return serialVersionUID;
	}


	/**
	 * @return the abs
	 */
	public String getAbs() {
		return abs;
	}


	/**
	 * @return the body
	 */
	public String getBody() {
		return body;
	}


	/**
	 * @return the raw_type
	 */
	public Text_raw_type getRaw_type() {
		return raw_type;
	}


	/**
	 * @return the authors
	 */
	public Author[] getAuthors() {
		return authors;
	}


	/**
	 * @return the journal
	 */
	public Journal getJournal() {
		return journal;
	}


	/**
	 * @return the externalIDs
	 */
	public ExternalID getExternalID() {
		return externalID;
	}


	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}


	/**
	 * @return the volume
	 */
	public String getVolume() {
		return volume;
	}


	/**
	 * @return the issue
	 */
	public String getIssue() {
		return issue;
	}


	/**
	 * @return the pages
	 */
	public String getPages() {
		return pages;
	}


	/**
	 * @param body the body to set
	 */
	public void setBody(String body) {
		this.body = body;
	}


	/**
	 * @param rawContent the rawContent to set
	 */
	public void setRawContent(String rawContent) {
		this.rawContent = rawContent;
	}


	/**
	 * @return the xml
	 */
	public String getXml() {
		return xml;
	}


	/**
	 * @param raw_type the raw_type to set
	 */
	public void setRaw_type(Text_raw_type raw_type) {
		this.raw_type = raw_type;
	}


	public String getDescription() {
		if (authors == null || authors.length == 0)
			return ID;
		
		String res = "";
		
		if (authors.length == 1)
			res = authors[0].getSurname();
		else if (authors.length == 2)
			res = authors[0].getSurname() + " and " + authors[1].getSurname();
		else
			res = authors[0].getSurname() + " et al.";
		
		if (year != null)
			res += " (" + year + ")";
		
		return res;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public void setAbs(String abs) {
		this.abs = abs;
	}
}
