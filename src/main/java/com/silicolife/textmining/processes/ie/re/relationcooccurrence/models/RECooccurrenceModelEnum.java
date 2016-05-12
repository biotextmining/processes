package com.silicolife.textmining.processes.ie.re.relationcooccurrence.models;


public enum RECooccurrenceModelEnum {
	Sentence_Contigous{
		public IRECooccurrenceSentenceModel getRelationCooccurrenceModel(){
			return new RECooccurrenceSentenceContiguous();
		}
		
		public String toString(){
			return "Entity Sentence Contigous";
		}
		
	},
	Sentence_Portion{
		public IRECooccurrenceSentenceModel getRelationCooccurrenceModel(){
			return 	new RECooccurrenceSentencePortion();
		}

		public String toString(){
			return "Mix Entity Pairs Sentence";
		}
	};

	public IRECooccurrenceSentenceModel getRelationCooccurrenceModel(){
		return this.getRelationCooccurrenceModel();
	}
	
	public String toString() {
		return this.toString();
	}

	public static RECooccurrenceModelEnum convert(IRECooccurrenceSentenceModel cooccurrenceModel) {
		if(cooccurrenceModel.getUID().equals(RECooccurrenceModelEnum.Sentence_Contigous.getRelationCooccurrenceModel().getUID()))
		{
			return RECooccurrenceModelEnum.Sentence_Contigous;
		}
		return RECooccurrenceModelEnum.Sentence_Portion;
	}
	
	public static RECooccurrenceModelEnum convertString(String str)
	{
		if(RECooccurrenceModelEnum.Sentence_Contigous.getRelationCooccurrenceModel().getUID().equals(str))
		{
			return RECooccurrenceModelEnum.Sentence_Contigous;
		}
		else
		{
			return RECooccurrenceModelEnum.Sentence_Portion;
		}
	}
	
}
