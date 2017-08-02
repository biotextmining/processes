package com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.fgo.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.fgo.FGOPatentDataObject;

public class FGOParser {

	private static SimpleDateFormat dt = new SimpleDateFormat("yyyyy-mm-dd"); 
	private static Set<String> removeSourceOtherPatentIds;
	private static String link = "https://www.google.com/patents/";

	public static FGOPatentDataObject retrieveMetaInformation(String patentID)
	{
		String html = FGOUtils.getPatentTextHTML(patentID);
		if(html==null)
			return null;
		FGOPatentDataObject out = parseMetaInformation(patentID,html);
		return out;
	}

	public static FGOPatentDataObject retrieveFullInformation(String patentID)
	{
		String html = FGOUtils.getPatentTextHTML(patentID);
		if(html==null)
			return null;
		FGOPatentDataObject out = parseFullInformation(patentID,html);
		return out;
	}

	private static FGOPatentDataObject parseFullInformation(String patentID, String html) {
		FGOPatentDataObject metainformation = parseMetaInformation(patentID,html);
		List<String> description = getDescription(html);
		metainformation.setDescription(description);
		List<String> claims = getClaims(html);
		metainformation.setClaims(claims);
		return metainformation;
	}

	private static FGOPatentDataObject parseMetaInformation(String patentID,String html) {
		FGOPatentDataObject out = new FGOPatentDataObject();
		Document document = Jsoup.parse(html);
		out.setPatentID(patentID);
		List<String> otherPatentIds = getPatentOtherIds(document);
		out.setOtherPatentIDs(otherPatentIds);
		out.getOtherPatentIDs().remove(patentID);
		List<String> inventors = getInventors(document);
		out.setInventors(inventors);
		List<String> owners = getOwners(document);
		out.setOwners(owners);
		String title = getTitle(document);
		out.setTitle(title);
		String abstractText = getAbstract(document);
		out.setAbstractText(abstractText);
		Set<String> patentClassification = getClassification(document);
		out.setPatentClassifications(patentClassification);
		Date date =  getDate(document);
		out.setDate(date);
		out.setLink(link+patentID);
		return out;
	}

	private static List<String> getClaims(String html) {
		List<String> out = new ArrayList<>();
		Document document = Jsoup.parse(html);
		Elements nodesMeta = document.select("div*[class=claim-text]");
		for(Element nodeMeta:nodesMeta)
		{
			out.add(replaceCaracters(nodeMeta.text()).trim());
		}
		return out;
	}


	private static List<String> getDescription(String html) {
		List<String> out = new ArrayList<>();
		Document document = Jsoup.parse(html);
		Elements nodesMeta = document.select("div*[class=description-line]");
		for(Element nodeMeta:nodesMeta)
		{
			out.add(replaceCaracters(nodeMeta.text()).trim());
		}
		return out;
	}

	private static Set<String> getClassification(Document document) {
		Set<String> out = new HashSet<>();
		Elements nodesMeta = document.select("a[href*=http://web2.wipo.int/ipcpub/]");
		for(Element nodeMeta:nodesMeta)
		{
			out.add(nodeMeta.text().trim());
		}
		return out;
	}

	private static List<String> getPatentOtherIds(Document document) {
		List<String> out = new ArrayList<>();
		Elements nodesMeta = document.select("span*[class=patent-bibdata-value] > a[href^=/patents/]");
		for(Element elementMeta:nodesMeta)
		{
			String cadidatePatentId = elementMeta.text();
			if(!getRemovedSourceOtherIds().contains(cadidatePatentId))
			{
				out.add(cadidatePatentId);
			}
		}
		return out;
	}

	private static Set<String> getRemovedSourceOtherIds()
	{
		if(removeSourceOtherPatentIds==null)
		{
			removeSourceOtherPatentIds = new HashSet<>();
			removeSourceOtherPatentIds.add("BiBTeX");
			removeSourceOtherPatentIds.add("EndNote");
			removeSourceOtherPatentIds.add("RefMan");
		}
		return removeSourceOtherPatentIds;
	}

	private static String getAbstract(Document document) {
		Elements nodesMeta = document.select("meta*[name=DC.description]");
		if(nodesMeta.size()==1)
		{
			return replaceCaracters(nodesMeta.get(0).attr("content")).trim();
		}
		return "";
	}

	private static Date getDate(Document document) {
		Date date = null;
		Elements elementsMeta = document.select("meta*[name=DC.date]");
		for(Element elementMeta:elementsMeta)
		{
			try {

				String dateStr = elementMeta.attr("content");
				if(elementMeta.attr("scheme").isEmpty())
				{
					date = dt.parse(dateStr);
					return date;
				}
				else
				{
					date = dt.parse(dateStr);
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return date;
	}

	private static List<String> getOwners(Document document) {
		List<String> out = new ArrayList<>();
		Elements elementsMeta = document.select("meta*[name=DC.contributor]*[scheme=assignee]");
		for(Element elementMeta:elementsMeta)
		{
			out.add(elementMeta.attr("content"));
		}
		return out;
	}

	private static List<String> getInventors(Document document) {
		List<String> out = new ArrayList<>();
		Elements elementsMeta = document.select("meta*[name=DC.contributor]*[scheme=inventor]");
		for(Element elementMeta:elementsMeta)
		{
			out.add(elementMeta.attr("content"));
		}
		return out;
	}

	private static String getTitle(Document document) {
		Elements nodesMeta = document.select("meta*[name=DC.title]");
		if(nodesMeta.size()==1)
		{
			return replaceCaracters(nodesMeta.get(0).attr("content")).trim();
		}
		return "";
	}
	
	private static String replaceCaracters(String original)
	{
		return original.replaceAll("[^\\x00-\\x7F]", "");
	}

}
