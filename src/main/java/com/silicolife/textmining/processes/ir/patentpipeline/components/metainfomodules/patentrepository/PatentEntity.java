package com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.patentrepository;

import java.util.Date;
import java.util.List;


public class PatentEntity {
	

	private String	id;
	
	private String kind;
	
	private String patentStatus;
	
	private List<String> otherIds;
	
	private String	title;
	
	private List<String>	authors;
	
	private List<String>	owners;
	
	private String	abstractText;
	
	private List<String> classifications;
	
	private Date date;
	
	private String	link;
	
	private String	fullTextContent;
	
	private List<String> sources;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<String> getOtherIds() {
		return otherIds;
	}

	public void setOtherIds(List<String> otherIds) {
		this.otherIds = otherIds;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<String> getAuthors() {
		return authors;
	}

	public void setAuthors(List<String> authors) {
		this.authors = authors;
	}

	public List<String> getOwners() {
		return owners;
	}

	public void setOwners(List<String> owners) {
		this.owners = owners;
	}

	public String getAbstractText() {
		return abstractText;
	}

	public void setAbstractText(String abstractText) {
		this.abstractText = abstractText;
	}

	public List<String> getClassifications() {
		return classifications;
	}

	public void setClassifications(List<String> classifications) {
		this.classifications = classifications;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getFullTextContent() {
		return fullTextContent;
	}

	public void setFullTextContent(String fullTextContent) {
		this.fullTextContent = fullTextContent;
	}

	public List<String> getSources() {
		return sources;
	}

	public void setSources(List<String> sources) {
		this.sources = sources;
	}
	
	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getPatentStatus() {
		return patentStatus;
	}

	public void setPatentStatus(String patentStatus) {
		this.patentStatus = patentStatus;
	}


	@Override
	public String toString() {
		return "PatentEntity [id=" + id + ", otherIds=" + otherIds + ", title=" + title + ", authors=" + authors
				+ ", owners=" + owners + ", abstractText=" + abstractText + ", classifications=" + classifications
				+ ", date=" + date + ", link=" + link + ", fullTextContent=" + fullTextContent + ", sources=" + sources
				+ "]";
	}
	
	
}
