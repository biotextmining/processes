package com.silicolife.textmining.processes.corpora.loaders.pdf.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthorsHeuristics {
	
	private static int maxLines = 10;
	private static int minScoreValue = 10;
	private static Map<String,Integer> characteresPoints = new HashMap<String, Integer>();
	private static Map<String,Integer> regularexpressionsPoints = new HashMap<String, Integer>();
	private static Map<String,Integer> penalSituations = new HashMap<String, Integer>();
	private static Set<String> andPoints = new HashSet<String>();
	
	static{
		// Characteres
		characteresPoints.put("\\*", 30);
		characteresPoints.put("\\†", 15);	
		characteresPoints.put("\\‡", 15);	
		characteresPoints.put("\\§", 15);	
		characteresPoints.put("\\•", 15);	
		characteresPoints.put("&", 6);
		characteresPoints.put(" and ", 6);
		characteresPoints.put(" AND ", 6);
		characteresPoints.put(",", 1);
		// Regular expressions
		regularexpressionsPoints.put("\\s+[A-Z]{1}\\.", 10);
//		regularexpressionsPoints.put("\\s+[A-Z]{1}\\s+", 2);
//		regularexpressionsPoints.put("\\s+[1-9]{1}\\s+", 2);
		regularexpressionsPoints.put("\\w+-\\w+", 5);
		// AND Points
		andPoints.add(" AND ");
		andPoints.add(" and ");
		andPoints.add(" & ");
		// Penal
		penalSituations.put("Author for correspondence", -100);
		penalSituations.put("Corresponding author", -100);
		penalSituations.put("University", -100);
		penalSituations.put("DOI", -100);
		penalSituations.put("doi", -100);
		penalSituations.put("Faculty", -100);
		penalSituations.put("Institute", -100);


	}
	
	/**
	 * Try search for Authors given a text with lines
	 * 
	 * @param text
	 * @return
	 */
	public static String getAuthorsByHeuristics(String text)
	{
		String[] lines = text.split("\\n");
		int linesNumber = maxLines;
		int linepoints;
		if(lines.length<linesNumber)
		{
			linesNumber = lines.length;
		}
		int[] linepointsScore = new int[linesNumber];
		for(int i=0;i<linesNumber;i++)
		{
			linepoints = getLinePoints(lines[i]);
			linepointsScore[i] = linepoints;
		}
		int indexMAxScore = getMaxIndex(linepointsScore);
		if(indexMAxScore==-1 || linepointsScore[indexMAxScore] < minScoreValue)
		{
			return null;
		}
		return bestEffortAuthors(indexMAxScore,linepointsScore,lines);
	}


	/**
	 * Using scores try searching for authors best effort
	 * 
	 * @param indexMAxScore
	 * @param linepointsScore 
	 * @param lines
	 * @return
	 */
	private static String bestEffortAuthors(int indexMAxScore, int[] linepointsScore, String[] lines) {
		String authors = new String();
		int startindex = indexMAxScore;
		int endtindex = indexMAxScore;
		int scoreMax  = linepointsScore[indexMAxScore];
		if(lines[startindex].endsWith("and") || lines[startindex].endsWith("AND") || lines[startindex].endsWith("&"))
		{
			endtindex = findingBelow(endtindex,scoreMax,linepointsScore,lines);
		}
		else if(lines[startindex].contains(" and ") || lines[startindex].contains(" AND ") || lines[startindex].contains(" & "))
		{
			startindex = findingAbove(startindex,scoreMax,linepointsScore,lines);
		}
		else
		{
			startindex = findingAbove(startindex,scoreMax,linepointsScore,lines);
			endtindex = findingBelow(endtindex,scoreMax,linepointsScore,lines);
		}
		for(int i=startindex;i<=endtindex;i++)
			authors = authors + lines[i];
		return cleanAuthorsStream(authors);
	}

	private static String cleanAuthorsStream(String authors) {
		String authorsResult = authors;
		authorsResult = authorsResult.replaceAll("\\n", " ");
		authorsResult = authorsResult.replaceAll("\\r", "");
		authorsResult = authorsResult.replaceAll("\\*", "");
		authorsResult = authorsResult.replaceAll("\\‡", "");
		authorsResult = authorsResult.replaceAll("\\†", "");
		authorsResult = authorsResult.replaceAll("\\§", "");
		authorsResult = authorsResult.replaceAll("\\•", "");
		authorsResult = authorsResult.replaceAll("\\&", "and");
		authorsResult = authorsResult.replaceAll("[1-9]", "");
		return authorsResult;
	}


	/**
	 * Finding above of maxindex 
	 * 
	 * @param startindex
	 * @param scoreMax
	 * @param linepointsScore
	 * @param lines
	 * @return
	 */
	private static int findingAbove(int startindex, int scoreMax,int[] linepointsScore, String[] lines) {
		if(startindex-1 <0)
			return startindex;
		if(linepointsScore[startindex-1] < 0)
			return startindex;
		if(linepointsScore[startindex-1] >= 10 && (scoreMax / linepointsScore[startindex-1]) < 3)
			return startindex+-1;
		return startindex;
	}


	/**
	 * Finding below of maxindex 
	 * 
	 * @param endtindex
	 * @param scoreMax
	 * @param linepointsScore
	 * @param lines
	 * @return
	 */
	private static int findingBelow(int endtindex, int scoreMax,int[] linepointsScore, String[] lines) {
		if(endtindex+1 >= maxLines)
			return endtindex;
		if(linepointsScore[endtindex+1] < 0)
			return endtindex;
		if(lines[endtindex+1].contains("and") || lines[endtindex+1].contains("AND") || lines[endtindex+1].contains("&"))
			return endtindex+1;
		if(linepointsScore[endtindex+1] >= 10 && (scoreMax / linepointsScore[endtindex+1]) < 3)
			return endtindex+1;
		return endtindex;
	}


	/**
	 * Finding max score index 
	 * 
	 * @param linepointsScore
	 * @return
	 */
	private static int getMaxIndex(int[] linepointsScore) {
		if(linepointsScore.length == 0)
			return -1;
		int maxIndex = 0;
		for(int i=1;i<linepointsScore.length;i++ )
		{
			if(linepointsScore[i] > linepointsScore[maxIndex])
				maxIndex = i;
		}
		return maxIndex;
	}


	/**
	 * Getting line points (score)
	 * 
	 * @param string
	 * @return
	 */
	private static int getLinePoints(String string) {
		int totalpoints = 0;
		for(String seq:characteresPoints.keySet())
		{
			totalpoints = totalpoints + characteresPoints.get(seq)*(string.split(seq, -1).length-1);
		}
		for(String expression:regularexpressionsPoints.keySet())
		{
			totalpoints = totalpoints + regularexpressionsPoints.get(expression)*countRegularExpressionMaches(string,expression);
		}
		for(String penal:penalSituations.keySet())
		{
			totalpoints = totalpoints + penalSituations.get(penal)*(string.split(penal, -1).length-1);
		}
		return totalpoints;
	}



	private static Integer countRegularExpressionMaches(String string,String expression) {
		Pattern pattern = Pattern.compile(expression);
	    Matcher  matcher = pattern.matcher(string);
		int count = 0;
		while (matcher.find())
		    count++;
		return count;
	}

}
