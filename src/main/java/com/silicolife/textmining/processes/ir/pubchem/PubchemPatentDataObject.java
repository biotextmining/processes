package com.silicolife.textmining.processes.ir.pubchem;

import java.util.Date;
import java.util.List;
import java.util.Set;

public class PubchemPatentDataObject {
	
	private String patentID;
	private String kind;
	private List<String> otherPatentIDs; 
	private List<String> inventors;
	private List<String> owners;
	private String title;
	private String abstractText;
	private String description;
	private Set<String> patentClassifications;
	private String link;
	private Date date;
	
	public PubchemPatentDataObject()
	{
		
	}

	

	public PubchemPatentDataObject(String patentID, String kind, List<String> otherPatentIDs, List<String> inventors,
			List<String> owners, String title, String abstractText, String description, List<String> claims,
			Set<String> patentClassifications, Date date,String link) {
		super();
		this.patentID = patentID;
		this.kind = kind;
		this.otherPatentIDs = otherPatentIDs;
		this.inventors = inventors;
		this.owners = owners;
		this.title = title;
		this.abstractText = abstractText;
		this.description = description;
		this.patentClassifications = patentClassifications;
		this.date = date;
		this.link = link;
	}



	public String getPatentID() {
		return patentID;
	}

	public void setPatentID(String patentID) {
		this.patentID = patentID;
	}


	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAbstractText() {
		return abstractText;
	}

	public void setAbstractText(String abstractText) {
		this.abstractText = abstractText;
	}


	public Set<String> getPatentClassifications() {
		return patentClassifications;
	}

	public void setPatentClassifications(Set<String> patentClassification) {
		this.patentClassifications = patentClassification;
	}

	public String getOtherPatentIdStreamText()
	{
		if(otherPatentIDs!=null)
		{
			String out = new String();
			for(String otherPatentID:otherPatentIDs)
			{
				out = out + otherPatentID +";";
			}
			if(out.isEmpty())
				return out;
			return out.substring(0,out.length()-1);
		}
		else
		{
			return new String();
		}
	}
	
	public String getInventorsStreamText()
	{
		if(inventors!=null)
		{
			String out = new String();
			for(String author:inventors)
			{
				out = out + author +";";
			}
			if(out.isEmpty())
				return out;
			return out.substring(0,out.length()-1);
		}
		else
		{
			return new String();
		}
	}
	
	public String getPatentClassificationStreamText()
	{
		if(patentClassifications!=null)
		{
			String out = new String();
			for(String patentClassification:patentClassifications)
			{
				out = out + patentClassification +";";
			}
			if(out.isEmpty())
				return out;
			return out.substring(0,out.length()-1);
		}
		else
		{
			return new String();
		}
	}

	public String getOwnersStreamText() {
		if(owners!=null)
		{
			String out = new String();
			for(String owner:owners)
			{
				out = out + owner +";";
			}
			if(out.isEmpty())
				return out;
			return out.substring(0,out.length()-1);
		}
		else
		{
			return new String();
		}
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public List<String> getOtherPatentIDs() {
		return otherPatentIDs;
	}

	public void setOtherPatentIDs(List<String> otherPatentIDs) {
		this.otherPatentIDs = otherPatentIDs;
	}

	public List<String> getInventors() {
		return inventors;
	}

	public void setInventors(List<String> inventors) {
		this.inventors = inventors;
	}

	public List<String> getOwners() {
		return owners;
	}

	public void setOwners(List<String> owners) {
		this.owners = owners;
	}


	public String getKind() {
		return kind;
	}


	public void setKind(String kind) {
		this.kind = kind;
	}



	@Override
	public String toString() {
		return "{\npatentID=" + patentID + ",\nkind=" + kind + ",\notherPatentIDs=" + otherPatentIDs
				+ ",\ninventors=" + inventors + ",\nowners=" + owners + ",\ntitle=" + title + ",\nabstractText="
				+ abstractText + ",\ndescription=" + description + ",\npatentClassifications="
				+ patentClassifications + ",\ndate=" + date + "\n link="+link +"\n}\n";
	}



	public String getLink() {
		return link;
	}



	public void setLink(String link) {
		this.link = link;
	}

}
