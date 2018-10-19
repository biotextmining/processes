package com.silicolife.textmining.processes.ir.patentpipeline;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.IPublicationExternalSourceLink;

public class PatentPipelineUtils {

	public static String labelIPCStart = "Classification IPC";

	private static int verifySectionNumbers(String patentID){
		if(patentID.matches(".*[A-Z]{1}")){
			return 1;
		}
		if(patentID.matches(".*[A-Z]{1}[1-9]{1}")){
			return 2;
		}
		return 0;
	}


	public static List<String> createPatentIDPossibilities(String patentID){
		Set<String> patentIDs=new HashSet<>();
		//the patentID itself
		patentIDs.add(patentID.trim());
		//if patentID has section letters, they will be deleted
		String newPatentID=PatentPipelineUtils.deleteSectionNumbers(patentID);
		patentIDs.add(newPatentID.trim());
		//uses the previous transformation and delete the central 0.
		newPatentID=PatentPipelineUtils.deleteChar0(newPatentID, 0);
		patentIDs.add(newPatentID.trim());
		// Add Zero After Initial Letter
		String addZeroPatent = addZeroAfterInitialLetters(patentID);
		patentIDs.add(addZeroPatent.trim());
		//last transformation. with previous two transformations, the year is converted for two numbers type
		if(PatentPipelineUtils.verifyYearPresence(newPatentID)){
			try {
				newPatentID=PatentPipelineUtils.transformYear(newPatentID);
				String newPatOnlyWithouYear=PatentPipelineUtils.transformYear(patentID);//year transformation only 
				patentIDs.add(newPatentID.trim());
				patentIDs.add(newPatOnlyWithouYear.trim());
			} catch (ParseException e) {
			}
		}
		//for some cases there are only 5five numbers after 0 and not 6 (WO1995006739A1) normally associated with old years
		newPatentID=PatentPipelineUtils.deleteChar0(newPatentID, -1);
		patentIDs.add(newPatentID.trim());
		//delete central 0 transformation only 
		int lettersOfSection = PatentPipelineUtils.verifySectionNumbers(patentID);
		newPatentID=PatentPipelineUtils.deleteChar0(patentID, lettersOfSection);
		patentIDs.add(newPatentID.trim());
		//special case with five numbers after 0 without year association
		newPatentID=PatentPipelineUtils.deleteChar0(patentID, lettersOfSection-1);
		patentIDs.add(newPatentID.trim());
		//delete section numbers on special case (last chance)
		newPatentID=PatentPipelineUtils.deleteSectionNumbers(newPatentID);
		patentIDs.add(newPatentID.trim());
		// remove all the zeros before initial letter until non zero number (Example US08071587 -> US8071587)
		newPatentID=PatentPipelineUtils.deleteInitialZeros(newPatentID);
		patentIDs.add(newPatentID.trim());
		return new ArrayList<>(patentIDs);
	}


	public static String deleteInitialZeros(String patentID) {
		String out = patentID;
		out = out.replaceAll("([A-Za-z]+)0{1,4}(.+)", "$1$2");
		return out;
	}


	public static String addZeroAfterInitialLetters(String patentID) {
		String out = patentID;
		if(patentID.length() > 2 && patentID.charAt(2) != '0')
		{
			out = patentID.substring(0, 2) + '0' + patentID.substring(2);
		}
		return out;
	}


	private static boolean verifyYearPresence(String patentID){
		try{
			if (Character.isLetter(patentID.charAt(0))){
				if (Character.isLetter(patentID.charAt(1))){
					String year=patentID.substring(2,6);//year
					try{
						int date=Integer.parseInt(year);
						if (date>=1900 && date<=Calendar.getInstance().get(Calendar.YEAR)){
							return true;
						}
					}catch(NumberFormatException e){
						return false;				
					}
				}
				else{
					String year=patentID.substring(1,5);//year
					int date=Integer.parseInt(year);
					if (date>=1900 && date<=Calendar.getInstance().get(Calendar.YEAR)){
						return true;
					}
				}
			}
		}catch(StringIndexOutOfBoundsException e){
			return false;
		}
		return false;
	}

	private static String deleteChar0(String patentID,int lettersOfSection){
		String newPatentID = patentID;
		try{
			if(patentID.charAt(2)!='0' && patentID.charAt(patentID.length()-7-(lettersOfSection))=='0'){//some patents have a "0" on middle with 6 numbers after
				newPatentID=patentID.substring(0,patentID.length()-7-(lettersOfSection)).concat(patentID.substring(patentID.length()-6-(lettersOfSection),patentID.length()));
			}
		}
		catch (StringIndexOutOfBoundsException e){
			return newPatentID;
		}


		return newPatentID;
	}


	private static String transformYear(String patentID) throws ParseException{
		String newPatentID = new String();
		try{
			if (Character.isLetter(patentID.charAt(0))){
				if (Character.isLetter(patentID.charAt(1))){
					String year=patentID.substring(2,6);//year
					int date=Integer.parseInt(year);
					if (date>=1900 && date<=Calendar.getInstance().get(Calendar.YEAR)){
						SimpleDateFormat dateParser = new SimpleDateFormat("yyyy"); //formatter for parsing date
						SimpleDateFormat dateFormatter = new SimpleDateFormat("yy"); //date output
						Date dateParsering = dateParser.parse(year);
						String newYear = dateFormatter.format(dateParsering);
						newPatentID=patentID.substring(0, 2)+newYear+patentID.substring(6, patentID.length());
					}
				}
				else{
					String year=patentID.substring(1,5);//year
					int date=Integer.parseInt(year);
					if (date>=1900 && date<=Calendar.getInstance().get(Calendar.YEAR)){
						SimpleDateFormat dateParser = new SimpleDateFormat("yyyy"); //formatter for parsing date
						SimpleDateFormat dateFormatter = new SimpleDateFormat("yy"); //formatter for formatting date output
						Date dateParsering = dateParser.parse(year);
						String newYear = dateFormatter.format(dateParsering);
						newPatentID=patentID.substring(0, 2)+newYear+patentID.substring(6, patentID.length());
					}
				}
			}
		}catch(StringIndexOutOfBoundsException e){
			return newPatentID;
		}
		return newPatentID;
	}


	public static String deleteSectionNumbers(String patentID){
		String newPatentID = patentID;
		try{
			if(patentID.matches(".*[A-Z]{1}$")){
				newPatentID=patentID.substring(0, patentID.length()-1);
			}
			else{
				if(patentID.matches(".*[A-Z]{1}[1-9]{1}$")){
					newPatentID=patentID.substring(0,patentID.length()-2);
				}
			}
		}catch(StringIndexOutOfBoundsException e){
			return newPatentID;
		}
		return newPatentID;
	}


	public static Map<String,List<String>> getAllPatentIDPossibilitiesForAGivenSet(Set<String> patentIDs){
		Map<String,List<String>> allOptions=new HashMap<>();
		for (String patent:patentIDs){
			List<String> patentPossible = PatentPipelineUtils.createPatentIDPossibilities(patent);
			allOptions.put(patent, patentPossible);
		}
		return allOptions;
	}


	private static Set<String> getAllExternalIDsPossibilities(IPublication pub){
		Set<String> pubExternalIDs=new HashSet<>();
		List<IPublicationExternalSourceLink> externalLinksList = pub.getPublicationExternalIDSource();
		for (IPublicationExternalSourceLink externaLink:externalLinksList){
			String externalID = externaLink.getSourceInternalId();
			List<String> possext = PatentPipelineUtils.createPatentIDPossibilities(externalID);
			pubExternalIDs.addAll(possext);
		}
		return pubExternalIDs;
	}


	private static Set<String> findRepeatedPatents (String patentID , Set<String> pubExternalIDs, Set<String> patentsToMaintain, Map<String, List<String>> allPossibleSolutions){
		Set<String> toRemoveIDs=new HashSet<>();
		//find all patent possibilities for the given ID and the pub external IDs- with/without the kind code, etc..
		List<String> allIDPossibilities = PatentPipelineUtils.createPatentIDPossibilities(patentID);

		for (String externalID:pubExternalIDs){
			//process each external ID, verifying if it already exists on choosed pubs
			if (!allIDPossibilities.contains(externalID) && 
					existsOnCollection(externalID, allPossibleSolutions.values()) && 
					//					!verifyChoosedPatents(getKeyForAValue(allPossibleSolutions,externalID), patentsToMaintain)){
					!patentsToMaintain.contains(externalID)){
				toRemoveIDs.add(getKeyForAValue(allPossibleSolutions,externalID));
			}
		}
		return toRemoveIDs;

	}


	public static Map<String, IPublication> processPatentMapWithMetadata(Map<String, IPublication> patentMap, Map<String, List<String>> allPossibleSolutions){
		Set<String> choosedPatents=new HashSet<>();
		Set<String> toRemoveIDs=new HashSet<>();

		for(String patentID:patentMap.keySet()){
			IPublication pub = patentMap.get(patentID);
			Set<String> pubExternalIDs = getAllExternalIDsPossibilities(pub);
			toRemoveIDs.addAll(findRepeatedPatents(patentID, pubExternalIDs, choosedPatents, allPossibleSolutions));
			choosedPatents.addAll(pubExternalIDs); 
		}
		for (String toRemoveID: toRemoveIDs){
			patentMap.remove(toRemoveID);
		}
		return patentMap;
	}



	public static boolean existsOnCollection(String patent, Collection<List<String>> patentLists) {
		Set<String> patentSet= new HashSet<>();
		for (List<String> col: patentLists){
			patentSet.addAll(col);
		}
		return patentSet.contains(patent);
	}


	public static String getKeyForAValue(Map<String, List<String>> allPossibleSolutions, String patentID){
		for (String key:allPossibleSolutions.keySet()){
			List<String> valueList = allPossibleSolutions.get(key);
			if (valueList.contains(patentID)){
				return key;
			}
		}
		return null;
	}


}
