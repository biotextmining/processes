package com.silicolife.textmining.processes.nlptools.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JScrollPane;

import com.silicolife.textmining.core.datastructures.annotation.ner.EntityAnnotationImpl;
import com.silicolife.textmining.core.datastructures.documents.structure.SentenceImpl;
import com.silicolife.textmining.core.datastructures.exceptions.SintaticTreeViewException;
import com.silicolife.textmining.core.datastructures.general.ClassPropertiesManagement;
import com.silicolife.textmining.core.datastructures.nlptools.OffsetEntityComparator;
import com.silicolife.textmining.core.datastructures.nlptools.OpenNLPSentenceSpliter;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalNames;
import com.silicolife.textmining.core.datastructures.utils.conf.OtherConfigurations;
import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.annotation.IEventAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.structure.IParsingToken;
import com.silicolife.textmining.core.interfaces.core.document.structure.ISentence;
import com.silicolife.textmining.core.interfaces.core.general.classe.IAnoteClass;
import com.silicolife.textmining.processes.nlptools.opennlp.OpenNLP;

import opennlp.tools.util.InvalidFormatException;

public class SimpliedSentenceSyntaxTree {
	
	private static Set<String> endCharSet = new HashSet<>();
	
	static{
		endCharSet.add(" ");
		endCharSet.add(".");
	 }
	
	/**
	 * 
	 * @param sentence - The Sentence
	 * @param entityAnnotations - The entities in Sentence
	 * @param events - The events triggers in sentence
	 * @param entityOrdered - IF entities are order or not
	 * @param simplificationEntities -  IF simplified entities or not ( Example for word ABcde and entity it just cde if you choose true tree only use cde as token otherwise use entiered word ABcde)
	 * @return
	 * @throws IOException
	 */
	public static ISentence simplySentence(ISentence sentence,List<IEntityAnnotation> entityAnnotations,List<IEventAnnotation> events, boolean entityOrdered,boolean simplificationEntities) throws IOException
	{

		Map<String,IEntityAnnotation> keyCondificationEntity = new HashMap<>();
		// Based EntityName Identification
		String ent = "ENT";
		String sentenceText = sentence.getText();
		sentenceText = sentenceText.replaceAll("\'", "-");
		sentenceText = sentenceText.replaceAll("«", "-");
		sentenceText = sentenceText.replaceAll("»", "-");
		sentenceText = sentenceText.replaceAll("\\s{5}", " - - ");
		sentenceText = sentenceText.replaceAll("\\s{4}", " -- ");
		sentenceText = sentenceText.replaceAll("\\s{3}", " - ");
		sentenceText = sentenceText.replaceAll("\\s{2}", " -");
		int entID=0;
		// Order entities
		if(!entityOrdered)
			Collections.sort(entityAnnotations, new OffsetEntityComparator());
		long startSentence = sentence.getStartOffset();
		for(int i=entityAnnotations.size()-1;i>=0;i--)
		{
			// Modify sentence text for Entities always recognixed as NP or similar (exaple Absicid acid -> ENT00000000) 
			sentenceText = compileEntity(entityAnnotations,keyCondificationEntity, ent, sentenceText, entID,startSentence, i,simplificationEntities);
			entID++;
		}
		List<IParsingToken> parsingtokens = OpenNLP.getInstance().parsingSentence(sentenceText);
		sentence.setParsingTokens(parsingtokens);
		// Change parsing tokens
		for(IParsingToken parsingToken:parsingtokens)
		{
			changeParsingToken(events, keyCondificationEntity, startSentence,parsingToken);
		}
		return sentence;
	}



	private static void changeParsingToken(List<IEventAnnotation> events,
			Map<String, IEntityAnnotation> keyCondificationEntity,
			long startSentence, IParsingToken parsingToken) {
		boolean endswithpoint = false;
		String text = parsingToken.getText();
		// Change if ends with "."
		if(text.endsWith("."))
		{
			text = text.substring(0,text.length()-1);
			endswithpoint = true;
		}
		// Finding codes (ENT100000 and replace by original sentence) and paint them
		replacecodesbyEntitiesagainAndColorthem(keyCondificationEntity, parsingToken,endswithpoint, text);
		if(events!=null && !events.isEmpty())
		{
			// Fiding Clues Verbs and paint them
			eventClueFindingAndColorThem(events, startSentence, parsingToken);
		}
	}



	private static void eventClueFindingAndColorThem(
			List<IEventAnnotation> events, long startSentence,
			IParsingToken parsingToken) {
		for(IEventAnnotation event : events)
		{
			if(parsingnodeinsedeEventCLue(event,startSentence,parsingToken))
			{
				parsingToken.addProperty(GlobalNames.color, OtherConfigurations.getVerbColor());
				parsingToken.addProperty(GlobalNames.backgoundcolor, OtherConfigurations.getVerbColorBackGround());
				break;
			}			
		}
	}



	private static void replacecodesbyEntitiesagainAndColorthem(
			Map<String, IEntityAnnotation> keyCondificationEntity,
			IParsingToken parsingToken, boolean endswithpoint, String text) {
		if(keyCondificationEntity.containsKey(text))
		{
			String getAnnotation = keyCondificationEntity.get(text).getAnnotationValue();
			if(endswithpoint)
				getAnnotation = getAnnotation + ".";
			parsingToken.setText(getAnnotation);
			long classID = keyCondificationEntity.get(text).getClassAnnotation().getId();
			try {
				IAnoteClass klass = ClassPropertiesManagement.getClassGivenClassID(classID);
				String color = klass.getColor();
				if(color!=null)
				{
					parsingToken.addProperty(GlobalNames.color, color);
				}
			} catch (ANoteException e) {
			} 
		}
	}



	private static String compileEntity(
			List<IEntityAnnotation> entityAnnotations,
			Map<String, IEntityAnnotation> keyCondificationEntity, String ent,
			String sentenceText, int entID, long startSentence, int i, boolean simplificationEntities) {
		String sentenceBefore;
		String SentenceMidlle;
		String sentenceAfter;
		IEntityAnnotation entity = entityAnnotations.get(i);
		Long startposition = entity.getStartOffset() - startSentence;
		Long endposition = entity.getEndOffset() - startSentence;	
		// Finding for rules propose
		int statingpoint = Integer.parseInt(startposition.toString());
		int endingpoint = Integer.parseInt(endposition.toString());
		if(simplificationEntities)
		{
			statingpoint = statingwordposition(sentenceText,statingpoint);
			endingpoint = endingpointposition(sentenceText,endingpoint);
		}
		// Finding for rules propose
		int differPosition = endingpoint -  statingpoint;
		String key = ent+entID;
		if(key.length()>differPosition)
		{
			int keydifferPosition = key.length()-differPosition;
			key = key.substring(keydifferPosition, key.length());
		}
		else if(key.length()<differPosition)
		{
			int keydifferPosition = differPosition-key.length();
			key = key + new String(new char[keydifferPosition]).replace("\0", "0");
		}
		sentenceBefore = sentenceText.substring(0,statingpoint);
		SentenceMidlle = key;
		if(endingpoint>sentenceText.length())
		{
			sentenceAfter = "";
		}
		else
		{
			sentenceAfter = sentenceText.substring(endingpoint);
		}
		sentenceText = sentenceBefore + SentenceMidlle + sentenceAfter;
		// Put key in Map
		keyCondificationEntity.put(key, entity);
		return sentenceText;
	}

	
	
	private static boolean parsingnodeinsedeEventCLue(IEventAnnotation event, long startSentence, IParsingToken parsingToken) {
		if(event==null)
			return false;
		if(event.getEventClue().isEmpty())
			return false;
		if(event.getEventClue().length() == event.getEndOffset()-event.getStartOffset())
		{
			long startToken = parsingToken.getStartOffset();
			long eventClueStartToken = event.getStartOffset()-startSentence;
			long eventClueEndToken = event.getEndOffset()-startSentence;

			if(eventClueStartToken<=startToken && startToken<=eventClueEndToken)
			{
				return true;
			}
		}
		return false;
	}



	private static int endingpointposition(String sentenceText, int parseInt) {
		int position = parseInt;
		if(position>sentenceText.length()-1)
		{
			return position;
		}
		else if(endCharSet.contains(sentenceText.subSequence(position, position+1)))
		{
			return position;
		}
		return endingpointposition(sentenceText,++position);
	}



	private static int statingwordposition(String sentenceText, int parseInt) {
		int position = parseInt;
		if(position<=0)
		{
			return 0;
		}
		else if(endCharSet.contains(sentenceText.subSequence(position-1, position)))
		{
			return position;
		}
		return statingwordposition(sentenceText,--position);
	}



	public static void main(String[] args) throws InvalidFormatException, IOException, SintaticTreeViewException {
		String text = "Escherichia coli is an enteric bacterium that is capable of growing over a wide range of pH values (pH 5-9) and, incredibly, is able to survive extreme acid stresses including passage through the mammalian stomach where the pH can fall to as low as pH 1-2. To enable such a broad range of acidic pH survival, E. coli possesses four different inducible amino acid decarboxylases that decarboxylate their substrate amino acids in a proton-dependent manner thus raising the internal pH. The decarboxylases include the glutamic acid decarboxylases GadA and GadB, the arginine decarboxylase AdiA, the lysine decarboxylase LdcI and the ornithine decarboxylase SpeF. All of these enzymes utilize pyridoxal-5'-phosphate as a co-factor and function together with inner-membrane substrate-product antiporters that remove decarboxylation products to the external medium in exchange for fresh substrate. In the case of LdcI, the lysine-cadaverine antiporter is called CadB. Recently, we determined the X-ray crystal structure of LdcI to 2.0 Å, and we discovered a novel small-molecule bound to LdcI the stringent response regulator guanosine 5'-diphosphate,3'-diphosphate (ppGpp). The stringent response occurs when exponentially growing cells experience nutrient deprivation or one of a number of other stresses. As a result, cells produce ppGpp which leads to a signaling cascade culminating in the shift from exponential growth to stationary phase growth. We have demonstrated that ppGpp is a specific inhibitor of LdcI. Here we describe the lysine decarboxylase assay, modified from the assay developed by Phan et al., that we have used to determine the activity of LdcI and the effect of pppGpp/ppGpp on that activity. The LdcI decarboxylation reaction removes the α-carboxy group of L-lysine and produces carbon dioxide and the polyamine cadaverine (1,5-diaminopentane). L-lysine and cadaverine can be reacted with 2,4,6-trinitrobenzensulfonic acid (TNBS) at high pH to generate N,N'-bistrinitrophenylcadaverine (TNP-cadaverine) and N,N'-bistrinitrophenyllysine (TNP-lysine), respectively. The TNP-cadaverine can be separated from the TNP-lysine as the former is soluble in organic solvents such as toluene while the latter is not. The linear range of the assay was determined empirically using purified cadaverine.";
		String text1 = "Escherichia coli is an enteric bacterium that is capable of growing over a wide range of pH values (pH 5-9) and, incredibly, "
				+ "is able to survive extreme acid stresses including passage through the mammalian stomach where the pH can fall to as low as pH 1-2.";
		String text2 = "To enable such a broad range of acidic pH survival, E. coli possesses four different inducible amino acid decarboxylases that decarboxylate"
				+ " their substrate amino acids in a proton-dependent manner thus raising the internal pH.";
		String text3 = "The decarboxylases include the glutamic acid decarboxylases GadA and GadB, the arginine decarboxylase AdiA, the lysine decarboxylase LdcI and the ornithine decarboxylase SpeF.";
		String text4 = "All of these enzymes utilize pyridoxal-5'-phosphate as a co-factor and function together with inner-membrane substrate-product antiporters that remove decarboxylation products to the external medium in exchange for fresh substrate.";
		String test5 = "In the case of LdcI, the lysine-cadaverine antiporter is called CadB.";
		String test6 = "Recently, we determined the X-ray crystal structure of LdcI to 2.0 Å, and we discovered a novel small-molecule bound to LdcI the stringent response regulator guanosine 5'-diphosphate,3'-diphosphate (ppGpp).";
		String test7 = "The stringent response occurs when exponentially growing cells experience nutrient deprivation or one of a number of other stresses.";
		String test8 = "As a result, cells produce ppGpp which leads to a signaling cascade culminating in the shift from exponential growth to stationary phase growth.";
		String test9 = "We have demonstrated that ppGpp is a specific inhibitor of LdcI.";
		String test10 = "Here we describe the lysine decarboxylase assay, modified from the assay developed by Phan et al., that we have used to determine the activity of LdcI and the effect of pppGpp/ppGpp on that activity.";
		String test11 = "The LdcI decarboxylation reaction removes the α-carboxy group of L-lysine and produces carbon dioxide and the polyamine cadaverine (1,5-diaminopentane).";
		String test12 = "L-lysine and cadaverine can be reacted with 2,4,6-trinitrobenzensulfonic acid (TNBS) at high pH to generate N,N'-bistrinitrophenylcadaverine (TNP-cadaverine) and N,N'-bistrinitrophenyllysine (TNP-lysine), respectively.";
		String test12b = "ENT1 and cadaverine can be reacted with ENT2 (TNBS) at high pH to generate ENT3 (TNP-cadaverine) and ENT4 (TNP-lysine), respectively.";
		String test13 = "The TNP-cadaverine can be separated from the TNP-lysine as the former is soluble in organic solvents such as toluene while the latter is not.";
		String test14 = "The linear range of the assay was determined empirically using purified cadaverine.";
		String test12simp = "L-lysine and cadaverine can be reacted with 2,4,6-trinitrobenzensulfonic acid (TNBS).";

//		ISentence sentence = new Sentence(0, text1.length()-1, text1);
//		sentence = new Sentence(0, test9.length()-1, test9);	
//		List<IEntityAnnotation> entityAnnotations = new ArrayList<IEntityAnnotation>();
//		entityAnnotations.add(new EntityAnnotation(-1, 59, 63, 4, 0, "LdcI", "LdcI"));
//		entityAnnotations.add(new EntityAnnotation(-1, 26, 31, 4, 0, "ppGpp", "ppGpp"));
//		SimplySentences.simplySentence(sentence, entityAnnotations , true);
//		SyntaxTreeViewerGUI gui = new SyntaxTreeViewerGUI(sentence);
//		JDialog jDialog = new JDialog();
//		jDialog.setLayout(new BorderLayout());
//		JScrollPane pane = new JScrollPane();
//		pane.setViewportView(gui);
//		jDialog.add(pane);
//		jDialog.setSize(new Dimension(1000, 800));
//		jDialog.setModal(true);
//		jDialog.setVisible(true);
		
//		ISentence sentence = new SentenceImpl(0, test12simp.length()-1, test12simp);
//		List<IEntityAnnotation> entityAnnotations = new ArrayList<IEntityAnnotation>();
//		entityAnnotations.add(new EntityAnnotationImpl(-1, 0, 8, 4, 0, "L-lysine", "L-lysine"));
//		entityAnnotations.add(new EntityAnnotationImpl(-1, 13, 23, 4, 0, "cadaverine", "cadaverine"));
//		entityAnnotations.add(new EntityAnnotationImpl(-1, 44, 77, 4, 0, "2,4,6-trinitrobenzensulfonic acid", "2,4,6-trinitrobenzensulfonic acid"));
//		entityAnnotations.add(new EntityAnnotationImpl(-1, 79, 83, 4, 0, "TNBS", "TNBS"));
//
//		SimpliedSentenceSyntaxTree.simplySentence(sentence, entityAnnotations ,null, true,false);
//		SyntaxTreeViewerPane gui = new SyntaxTreeViewerPane(sentence);
//		JDialog jDialog = new JDialog();
//		jDialog.setLayout(new BorderLayout());
//		JScrollPane pane = new JScrollPane();
//		pane.setViewportView(gui);
//		jDialog.add(pane);
//		jDialog.setSize(new Dimension(1000, 800));
//		jDialog.setModal(true);
//		jDialog.setVisible(true);
	}

}
