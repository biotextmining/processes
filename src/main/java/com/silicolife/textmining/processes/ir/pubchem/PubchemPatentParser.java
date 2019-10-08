package com.silicolife.textmining.processes.ir.pubchem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.silicolife.textmining.processes.ir.patentpipeline.PatentPipelineUtils;

public class PubchemPatentParser {

	private static SimpleDateFormat dt = new SimpleDateFormat("yyyy/MM/dd"); 

	public static PubchemPatentDataObject retrieveMetaInformation(String patentID)
	{
		String patentbody = PubchemPatentRetrievalAPI.getPatentJson(patentID);
		if(patentbody==null)
			return null;
		JSONObject patentbodyjson = new JSONObject(patentbody);
		String hid = getClassificationHId(patentbodyjson);
		String patentClassification = null;
		JSONObject patentClassificationJson = null;
		if(hid!=null)
		{
			patentClassification = PubchemPatentRetrievalAPI.getPatentClassificationJson(hid);
			if(patentClassification!=null)
				patentClassificationJson = new JSONObject(patentClassification);
		}
		PubchemPatentDataObject out = parseMetaInformation(patentID,patentbodyjson,patentClassificationJson);
		return out;
	}


	private static String getClassificationHId(JSONObject patentbodyjson) {
		JSONObject getPatentInformationFieldRoot = getPatentInformationFieldRoot(patentbodyjson,"Patent Classification");
		if(getPatentInformationFieldRoot!=null)
		{
			JSONArray sectionArrayClassification = getPatentInformationFieldRoot.getJSONArray("Section");
			for(int j=0;j<getPatentInformationFieldRoot.length();j++)
			{
				JSONObject classificationSection = sectionArrayClassification.getJSONObject(j);
				Object tocHeadingClassification = classificationSection.get("TOCHeading");
				if(tocHeadingClassification.equals("WIPO IPC"))
				{
					JSONArray classificationInformation = classificationSection.getJSONArray("Information");
					for(int k=0;k<classificationInformation.length();k++)
					{
						JSONObject classificationInformationData = classificationInformation.getJSONObject(k);						
						String hidName = classificationInformationData.getString("Name");
						if(hidName.equals("HID"))
						{
							int hid = classificationInformationData.getInt("NumValue");
							return String.valueOf(hid);
						}
					}
				}
			}
		}
		return null;
	}

	private static JSONObject getPatentInformationFieldRoot(JSONObject patentbodyjson,String field)
	{
		JSONObject record = patentbodyjson.getJSONObject("Record");
		JSONArray sectionArray = record.getJSONArray("Section");
		for(int i=0;i<sectionArray.length();i++)
		{
			JSONObject section = sectionArray.getJSONObject(i);
			Object tocHeading = section.get("TOCHeading");
			if(tocHeading.equals(field))
			{
				return section;
			}
		}
		return null;
	}


	private static PubchemPatentDataObject parseMetaInformation(String patentID,JSONObject patentbodyjson, JSONObject patentClassificationJson) {
		PubchemPatentDataObject out = new PubchemPatentDataObject();
		out.setPatentID(patentID);
		Set<String> otherPatentIds = getPatentOtherIds(patentbodyjson);
		otherPatentIds.remove(patentID);
		out.setOtherPatentIDs(new ArrayList<>(otherPatentIds));
		List<String> inventors = getInventors(patentbodyjson);
		out.setInventors(inventors);
		List<String> owners = getOwners(patentbodyjson);
		out.setOwners(owners);
		String title = getTitle(patentbodyjson);
		out.setTitle(title);
		String abstractText = getAbstract(patentbodyjson);
		out.setAbstractText(abstractText);
		Set<String> patentClassification = getClassification(patentClassificationJson);
		out.setPatentClassifications(patentClassification);
		Date date =  getDate(patentbodyjson);
		out.setDate(date);
		String url = getURL(patentbodyjson);
		out.setLink(url);
		return out;
	}


	private static String getURL(JSONObject patentbodyjson) {
		JSONObject getPatentURLFieldRoot = getPatentInformationFieldRoot(patentbodyjson,"Patent URL");
		if(getPatentURLFieldRoot!=null)
		{
			JSONArray getPatentURLInformationArray = getPatentURLFieldRoot.getJSONArray("Information");
			JSONObject getPatentURLInformationObject = getPatentURLInformationArray.getJSONObject(0);
			String url = getPatentURLInformationObject.getString("StringValue");
			return url;
		}
		return "";
	}


	private static Set<String> getClassification(JSONObject patentClassificationJson) {
		Set<String> out = new HashSet<>();
		if(patentClassificationJson!=null)
		{
			JSONObject hierarchiesJSONObject = patentClassificationJson.getJSONObject("Hierarchies");
			if(hierarchiesJSONObject!=null)
			{
				JSONArray hierarchyJSONArray = hierarchiesJSONObject.getJSONArray("Hierarchy");
				JSONObject hierarchyJSONObject = hierarchyJSONArray.getJSONObject(0);
				JSONArray nodesArray = hierarchyJSONObject.getJSONArray("Node");
				for(int i=0;i<nodesArray.length();i++)
				{
					JSONObject nodeJsonObject = nodesArray.getJSONObject(i);
					JSONObject informationJsonObject =  nodeJsonObject.getJSONObject("Information");
					if(informationJsonObject.has("Match"))
					{
						String code = informationJsonObject.getString("Name");
						code = code.substring(0,code.indexOf(" "));
						out.add(code);
					}
				}
			}
		}
		return out;
	}

	private static Set<String> getPatentOtherIds(JSONObject patentbodyjson) {
		Set<String> out = new HashSet<>();
		JSONObject getPatentPrimaryIndentifierFieldRoot = getPatentInformationFieldRoot(patentbodyjson,"Primary Patent Identifier");
		JSONObject getPatentIdentifierSynonymsFieldRoot = getPatentInformationFieldRoot(patentbodyjson,"Patent Identifier Synonyms");
		// Process Primary Identifier
		JSONArray getPatentPrimaryIndentifierInformationArray = getPatentPrimaryIndentifierFieldRoot.getJSONArray("Information");
		if(getPatentPrimaryIndentifierInformationArray!=null)
		{
			JSONObject getPatentPrimaryIndentifierInformationObject = getPatentPrimaryIndentifierInformationArray.getJSONObject(0);
			String patentPrimaryIndentifierString = getPatentPrimaryIndentifierInformationObject.getString("StringValue");
			patentPrimaryIndentifierString = PatentPipelineUtils.deleteSectionNumbers(patentPrimaryIndentifierString);
			out.add(patentPrimaryIndentifierString);
			// Process Patent Identifier Synonyms
			if(getPatentIdentifierSynonymsFieldRoot!=null)
			{
				JSONArray getPatentIdentifierSynonymsInformationArray = getPatentIdentifierSynonymsFieldRoot.getJSONArray("Information");
				JSONObject getPatentIdentifierSynonymsInformationObject = getPatentIdentifierSynonymsInformationArray.getJSONObject(0);
				JSONArray getPatentIdentifierSynonymsInformationIds = getPatentIdentifierSynonymsInformationObject.getJSONArray("StringValueList");
				for(int i=0;i<getPatentIdentifierSynonymsInformationIds.length();i++)
				{
					String candidateId = getPatentIdentifierSynonymsInformationIds.getString(i);
					if(!candidateId.contains("."))
					{
						candidateId = PatentPipelineUtils.deleteSectionNumbers(candidateId);
						out.add(candidateId);
					}
				}
			}
			else
			{
				System.out.println("ERR " +patentPrimaryIndentifierString);
			}
		}
		return out;
	}

	private static String getAbstract(JSONObject patentbodyjson) {
		JSONObject getPatentAbstractFieldRoot = getPatentInformationFieldRoot(patentbodyjson,"Patent Abstract");
		if(getPatentAbstractFieldRoot!=null)
		{
			JSONArray getPatentAbstractInformationArray = getPatentAbstractFieldRoot.getJSONArray("Information");
			JSONObject getPatentAbstractInformationObject = getPatentAbstractInformationArray.getJSONObject(0);
			String abstractText = getPatentAbstractInformationObject.getString("StringValue");
			return abstractText;
		}
		return "";
	}

	private static Date getDate(JSONObject patentbodyjson) {
		Date date = null;
		try {
			date = getDate(patentbodyjson,"Patent Grant Date");
			if(date==null)
				date = getDate(patentbodyjson,"Patent Submission Date");
		} catch (ParseException e) {
		}
		return date;
	}

	private static Date getDate(JSONObject patentbodyjson,String field) throws ParseException
	{
		JSONObject getPatentDateFieldRoot = getPatentInformationFieldRoot(patentbodyjson,field);
		if(getPatentDateFieldRoot==null)
			return null;
		JSONArray getPatentDateInformationArray = getPatentDateFieldRoot.getJSONArray("Information");
		JSONObject getPatentDateInformationObject = getPatentDateInformationArray.getJSONObject(0);
		String dateStr = getPatentDateInformationObject.getString("DateValue");
		Date date = dt.parse(dateStr);
		return date;
	}

	private static List<String> getOwners(JSONObject patentbodyjson) {
		List<String> out = new ArrayList<>();
		JSONObject getPatentApplicantsFieldRoot = getPatentInformationFieldRoot(patentbodyjson,"Patent Applicant");
		if(getPatentApplicantsFieldRoot!=null)
		{
			JSONArray getPatentApplicantsInformationArray = getPatentApplicantsFieldRoot.getJSONArray("Information");
			JSONObject getPatentApplicantsInformationObject = getPatentApplicantsInformationArray.getJSONObject(0);
			JSONArray getPatentInventorsInformationNames = getPatentApplicantsInformationObject.getJSONArray("StringValueList");
			for(int i=0;i<getPatentInventorsInformationNames.length();i++)
			{
				String inventor = getPatentInventorsInformationNames.getString(i);
				out.add(inventor);
			}
		}
		return out;
	}

	private static List<String> getInventors(JSONObject patentbodyjson) {
		List<String> out = new ArrayList<>();
		JSONObject getPatentInventorsFieldRoot = getPatentInformationFieldRoot(patentbodyjson,"Patent Inventor");
		if(getPatentInventorsFieldRoot!=null)
		{
			JSONArray getPatentInventorsInformationArray = getPatentInventorsFieldRoot.getJSONArray("Information");
			JSONObject getPatentInventorsInformationObject = getPatentInventorsInformationArray.getJSONObject(0);
			JSONArray getPatentInventorsInformationNames = getPatentInventorsInformationObject.getJSONArray("StringValueList");
			for(int i=0;i<getPatentInventorsInformationNames.length();i++)
			{
				String inventor = getPatentInventorsInformationNames.getString(i);
				out.add(inventor);
			}
		}
		return out;
	}

	private static String getTitle(JSONObject patentbodyjson) {
		JSONObject getPatentTitleFieldRoot = getPatentInformationFieldRoot(patentbodyjson,"Patent Title");
		if(getPatentTitleFieldRoot!=null)
		{
			JSONArray getPatentTitleInformationArray = getPatentTitleFieldRoot.getJSONArray("Information");
			JSONObject getPatentTitleInformationObject = getPatentTitleInformationArray.getJSONObject(0);
			String title = getPatentTitleInformationObject.getString("StringValue");
			return title;
		}
		return "";
	}

}
