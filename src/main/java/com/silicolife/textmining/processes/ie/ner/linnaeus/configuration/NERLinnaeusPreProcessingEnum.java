package com.silicolife.textmining.processes.ie.ner.linnaeus.configuration;


public enum NERLinnaeusPreProcessingEnum {
	No,
	StopWords;

	public static NERLinnaeusPreProcessingEnum convertStringToNERLinnaeusPreProssecingEnum(String toconvert)
	{
		if(toconvert.equals("No"))
		{
			return NERLinnaeusPreProcessingEnum.No;
		}
		else if(toconvert.equals("StopWords"))
		{
			return NERLinnaeusPreProcessingEnum.StopWords;
		}
		return null;
	}
}
