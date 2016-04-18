package com.silicolife.textmining.processes.nlptools.opennlp;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JScrollPane;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.Span;

import com.silicolife.textmining.core.datastructures.documents.structure.ChunkerTokenImpl;
import com.silicolife.textmining.core.datastructures.documents.structure.POSTokenImpl;
import com.silicolife.textmining.core.datastructures.documents.structure.ParsingTokenImpl;
import com.silicolife.textmining.core.datastructures.documents.structure.SentenceImpl;
import com.silicolife.textmining.core.datastructures.exceptions.SintaticTreeViewException;
import com.silicolife.textmining.core.datastructures.nlptools.TextSegments;
import com.silicolife.textmining.core.interfaces.core.document.structure.IChunkerToken;
import com.silicolife.textmining.core.interfaces.core.document.structure.IPOSToken;
import com.silicolife.textmining.core.interfaces.core.document.structure.IParsingToken;
import com.silicolife.textmining.core.interfaces.core.document.structure.ISentence;
import com.silicolife.textmining.core.interfaces.core.document.structure.ITextSegment;
import com.silicolife.textmining.processes.nlptools.structure.SyntaxTreeViewerPane;

/**
 * Class that represents a OpenNLP Tagging 
 * 
 * @author Hugo Costa
 *
 */
public class OpenNLP {

	private final static String sentenceModelFile = "nlpmodels/en-sent.bin";
	private final static String tokeniserModelFile = "nlpmodels/en-token.bin";
	private final static String postaggingModelFile = "nlpmodels/en-pos-maxent.bin";
	private final static String parsingModelFile = "nlpmodels/en-parser-chunking.bin";
	private final static String chunkerModelFile = "nlpmodels/en-chunker.bin";


	private SentenceModel sentenceModel;
	private POSModel postaggerModel;
	private TokenizerModel tokeniserModel; 
	private ChunkerModel chunkerModel;
	private ParserModel parserModel;
	private Parser parser;

	private static OpenNLP _instance;

	private OpenNLP()
	{

	}

	/**
	 * Gives access to the OpenNLP instance
	 * @return 
	 */
	public static synchronized OpenNLP getInstance() {
		if (_instance == null) {
			OpenNLP.createInstance();
		}
		return _instance;
	}

	/**
	 * Creates the singleton instance.
	 */
	private static void createInstance(){

		if (_instance == null) {
			_instance = new OpenNLP();
		}
	}

	private void initChunkerModelModel() throws IOException{
		InputStream modelIn = OpenNLP.class.getClassLoader().getResourceAsStream(chunkerModelFile);
//		InputStream modelIn = new FileInputStream(chunkerModelFile);
		chunkerModel = new ChunkerModel(modelIn);
	}

	private void initParserModelModel() throws IOException{
		InputStream parsingModelIn = OpenNLP.class.getClassLoader().getResourceAsStream(parsingModelFile);
//		InputStream parsingModelIn = new FileInputStream(parsingModelFile);
		parserModel = new ParserModel(parsingModelIn);
		parser = ParserFactory.create(parserModel);
	}

	private void initSentenceModel() throws IOException
	{
		InputStream modelIn = OpenNLP.class.getClassLoader().getResourceAsStream(sentenceModelFile);
//		InputStream modelIn = new FileInputStream(sentenceModelFile);
		sentenceModel = new SentenceModel(modelIn);
	}

	private void initTokenizerModel() throws IOException
	{
		InputStream tokenizerFileInput = OpenNLP.class.getClassLoader().getResourceAsStream(tokeniserModelFile);
//		InputStream tokenizerFileInput = new FileInputStream(tokeniserModelFile);
		tokeniserModel = new TokenizerModel(tokenizerFileInput);
	}

	private void initPosTagModel() throws IOException
	{
		InputStream postaggerFileInput = OpenNLP.class.getClassLoader().getResourceAsStream(postaggingModelFile);
//		InputStream postaggerFileInput = new FileInputStream(postaggingModelFile);
		postaggerModel = new POSModel(postaggerFileInput);
	}

	/**
	 * Sentence Splitter. Return a List of {@link ISentence}
	 * 
	 * @param text
	 * @return
	 * @throws IOException 
	 */
	public List<ISentence> getSentencesText(String text) throws IOException
	{
		if(sentenceModel==null)
			initSentenceModel();
		SentenceDetectorME sentenceDetector = new SentenceDetectorME(sentenceModel);
		Span sentences[] = sentenceDetector.sentPosDetect(text);
		List<ISentence> sents = new ArrayList<ISentence>();
		for(Span sent:sentences)
		{
			SentenceImpl sen = new SentenceImpl(sent.getStart(), sent.getEnd(), text.substring((int) sent.getStart(), (int) sent.getEnd()));
			sents.add(sen);
		}	
		return sents;
	}

	/**
	 * Return a {@link ISentence} for text with POS Tagging
	 * 
	 * @param text
	 * @param startOffset
	 * @return
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public ISentence posTaggingSentence(String text, long startOffset) throws IOException
	{
		List<IPOSToken> orderPosTokens = new ArrayList<IPOSToken>();
		if(tokeniserModel==null)
			initTokenizerModel();
		Tokenizer tokenizer = new TokenizerME(tokeniserModel);
		Span tokenSpans[] = tokenizer.tokenizePos(text);
		String[] tokens = new String[tokenSpans.length];
		for(int i=0;i<tokenSpans.length;i++)
		{
			tokens[i] = tokenSpans[i].getCoveredText(text).toString();
		}
		if(postaggerModel==null)
			initPosTagModel();
		POSTaggerME postagger = new POSTaggerME(postaggerModel);
		String[] categories = postagger.tag(tokens);
		for(int j=0;j<categories.length;j++)
		{
			IPOSToken e = new POSTokenImpl(null,tokenSpans[j].getStart()+startOffset, tokenSpans[j].getEnd()+startOffset, tokens[j], categories[j],null);
			orderPosTokens.add(e);
		}
		ISentence sentence = new SentenceImpl(startOffset, startOffset + text.length(), text, orderPosTokens);
		return sentence;
	}

	/**
	 * POS Tagging. Return a List of {@link ISentence} with {@link IPOSToken} tokens
	 * 
	 * @param text
	 * @return
	 * @throws IOException 
	 */
	public List<ISentence> getSetencesWhitPOSTagging(String text) throws IOException 
	{
		List<ISentence> sentences = getSentencesText(text);
		List<ISentence> sentencesResult = new ArrayList<ISentence>();
		for(ISentence setence : sentences)
		{
			long start = setence.getStartOffset();
			ISentence newSentencePostagas;
			newSentencePostagas = posTaggingSentence(setence.getText(),start);
			sentencesResult.add(new SentenceImpl(setence.getStartOffset(), setence.getStartOffset(), text.substring((int)setence.getStartOffset(),(int)setence.getEndOffset()),newSentencePostagas.getOrderPOSTokens()));
		}
		return sentencesResult;
	}


	/**
	 * Shallow Parsing. Return a List of {@link ISentence} with {@link IChunkerToken}
	 * 
	 * @param text
	 * @return
	 * @throws IOException 
	 */
	public List<ISentence> getSentenceOrderChunkers(List<ISentence> sentences) throws IOException
	{
		if(chunkerModel==null)
			initChunkerModelModel();
		List<ISentence> sentencesWithChunker = new ArrayList<ISentence>();
		for(ISentence sentence : sentences)
		{
			if(sentence.getOrderPOSTokens()!=null && sentence.getOrderPOSTokens().size() > 0)
			{
				ChunkerME chunker = new ChunkerME(chunkerModel);
				List<IPOSToken> posTAgs = sentence.getOrderPOSTokens();
				String[] tags = new String[posTAgs.size()];
				String[] toks = new String[posTAgs.size()];
				for(int i=0;i<posTAgs.size();i++)
				{
					tags[i] = posTAgs.get(i).getPOSCategory();
					toks[i] = posTAgs.get(i).getText();
				}
				Span[] chunkers = chunker.chunkAsSpans(toks, tags);
				List<IChunkerToken> chunkerTokensSentence = new ArrayList<IChunkerToken>();
				for(int j=0;j<chunkers.length;j++)
				{
					Span span = chunkers[j];
					int posTagStart = span.getStart();
					int posTagEnd = span.getEnd();
					String chunkCatagory = span.getType();
					long endOffset = posTAgs.get(posTagEnd-1).getEndOffset() - sentence.getStartOffset();
					long startOffset = posTAgs.get(posTagStart).getStartOffset() - sentence.getStartOffset();
					String chunkertext = sentence.getText().substring((int)startOffset,(int)endOffset);
					IChunkerToken chunk = new ChunkerTokenImpl(null,startOffset, endOffset, chunkCatagory, chunkertext);
					chunkerTokensSentence.add(chunk);		
				}
				ISentence newsentence = new SentenceImpl(sentence.getStartOffset(), sentence.getEndOffset(), sentence.getText(),posTAgs,chunkerTokensSentence);
				sentencesWithChunker.add(newsentence );
			}
			else
			{
				sentencesWithChunker.add(sentence);
			}

		}
		return sentencesWithChunker;
	}

	public List<ISentence> getSentenceParserResults(List<ISentence> sentences) throws IOException
	{
		if(parserModel==null)
			initParserModelModel();
		List<ISentence> sentencesWithChunker = new ArrayList<ISentence>();
		for(ISentence sentence : sentences)
		{		
			parsingSentence(sentence);
		}
		return sentencesWithChunker;
	}

	public List<IParsingToken> parsingSentence(ISentence sentence) throws IOException {
		if(parserModel==null)
			initParserModelModel();
		List<IParsingToken> parsingTokens = new ArrayList<IParsingToken>();
		String sentenceText = sentence.getText();
		if(sentenceText.endsWith("."))
		{
			sentenceText = sentenceText.substring(0, sentenceText.length()-1)+" .";
		}
		Parse topParses[] = ParserTool.parseLine(sentenceText, parser, 1);
		for(int i=0;i<topParses.length;i++)
		{
			Parse parserRes = topParses[i];
			ParsingTokenImpl father = new ParsingTokenImpl(String.valueOf(parserRes.getSpan().getStart()+"-"+parserRes.getSpan().getEnd()), parserRes.getSpan().getStart(), parserRes.getSpan().getEnd(), parserRes.getType(), parserRes.getText());
			builtTreeParsingTokens(father,parsingTokens,parserRes);
		}
		return parsingTokens;
	}
	
	public List<IParsingToken> parsingSentence(String sentence) throws IOException {
		if(parserModel==null)
			initParserModelModel();
		List<IParsingToken> parsingTokens = new ArrayList<IParsingToken>();
		if(sentence.endsWith("."))
		{
			sentence = sentence.substring(0, sentence.length()-1)+" .";
		}
		Parse topParses[] = ParserTool.parseLine(sentence, parser, 1);
		for(int i=0;i<topParses.length;i++)
		{
			Parse parserRes = topParses[i];
			ParsingTokenImpl father = new ParsingTokenImpl(String.valueOf(parserRes.getSpan().getStart()+"-"+parserRes.getSpan().getEnd()), parserRes.getSpan().getStart(), parserRes.getSpan().getEnd(), parserRes.getType(), parserRes.getText());
			builtTreeParsingTokens(father,parsingTokens,parserRes);
		}
		return parsingTokens;
	}


	private void builtTreeParsingTokens(ParsingTokenImpl father, List<IParsingToken> parsingTokens,Parse parserRes) {
		parsingTokens.add(father);
		if(parserRes.getChildren()==null || parserRes.getChildren().length == 0)
		{
			return;
		}
		for (Parse c : parserRes.getChildren()) {
			Span s = c.getSpan();
			if(!c.getType().equals(AbstractBottomUpParser.TOK_NODE))
			{
				ParsingTokenImpl sun = new ParsingTokenImpl(String.valueOf(s.getStart()+"-"+s.getEnd()), s.getStart(), s.getEnd(), c.getType(),c.getText().substring(s.getStart(), s.getEnd()));
				father.addConsist(sun);
				builtTreeParsingTokens(sun,parsingTokens,c);
			}
		}
	}

	private String show(Parse parserRes) {
		StringBuffer sb = new StringBuffer(parserRes.getText().length()*4);
		show(parserRes,sb);
		return sb.toString();
	}

	private void show(Parse parserRes,StringBuffer sb) {
		int start = parserRes.getSpan().getStart();
		if (!parserRes.getType().equals(AbstractBottomUpParser.TOK_NODE)) {
			sb.append("(");
			sb.append(parserRes.getType()).append(" ");
		}
		for (Parse c : parserRes.getChildren()) {
			Span s = c.getSpan();
			if (start < s.getStart()) {
				sb.append(encodeToken(parserRes.getText().substring(start, s.getStart())));
			}
			c.show(sb);
			start = s.getEnd();
		}
		if (start < parserRes.getSpan().getEnd()) {
			sb.append(encodeToken(parserRes.getText().substring(start, parserRes.getSpan().getEnd())));
		}
		if (!parserRes.getType().equals(AbstractBottomUpParser.TOK_NODE)) {
			sb.append(")");
		}
	}


	private static String encodeToken(String token) {
		if (Parse.BRACKET_LRB.equals(token)) {
			return "-LRB-";
		}
		else if (Parse.BRACKET_RRB.equals(token)) {
			return "-RRB-";
		}
		else if (Parse.BRACKET_LCB.equals(token)) {
			return "-LCB-";
		}
		else if (Parse.BRACKET_RCB.equals(token)) {
			return "-RCB-";
		}

		return token;
	}

	public List<ITextSegment> geTextSegmentsFilterByPOSTags(String text,Set<String> posTags) throws IOException {
		List<ITextSegment> segment = new ArrayList<ITextSegment>();
		List<ISentence> sentences = getSentencesText(text);
		for(ISentence sentence:sentences)
		{
			long start = sentence.getStartOffset();
			ISentence newSentencePostagas;
			newSentencePostagas = posTaggingSentence(sentence.getText(),start);
			List<ITextSegment> sentencesAux = searchContinuosSegments(newSentencePostagas,posTags,text);
			for(ITextSegment sentAux:sentencesAux)
			{
				segment.add(sentAux);
			}
		}
		return segment;
	}

	private List<ITextSegment> searchContinuosSegments(ISentence sentence,Set<String> posTags,String originalText) {
		List<IPOSToken> tokens = sentence.getOrderPOSTokens();
		List<ITextSegment> list = new ArrayList<ITextSegment>();
		long start = sentence.getStartOffset();
		long end = sentence.getStartOffset();
		for(int i=0;i<tokens.size();i++)
		{
			if(posTags.contains(tokens.get(i).getPOSCategory()))
			{
				end = tokens.get(i).getEndOffset();
				if(i+1>=tokens.size() && end-start>1)
				{
					ITextSegment e = new TextSegments(start, end, originalText.substring((int)start, (int) end));
					list.add(e);
				}
			}
			else
			{
				if(i+1<tokens.size() && start!=end && end-start>1)
				{

					ITextSegment e = new TextSegments(start, end, originalText.substring((int)start, (int) end));
					list.add(e);
					start = tokens.get(i+1).getStartOffset();
					end = start;
				}
				else if(i+1>=tokens.size())
				{

				}
				else
				{
					start = tokens.get(i+1).getStartOffset();
					end = start;
				}
			}
		}

		return list;
	}

	public static void main(String[] args) throws InvalidFormatException, IOException, SintaticTreeViewException {
		OpenNLP nlp = new OpenNLP();
		String text = "arboxylate their substrate amino acids in a proton-dependent manner thus raising the internal pH. The decarboxylases include the glutamic acid decarboxylases GadA and GadB, the arginine decarboxylase AdiA, the lysine decarboxylase LdcI and the ornithine decarboxylase SpeF. All of these enzymes utilize pyridoxal-5'-phosphate as a co-factor and function together with inner-membrane substrate-product antiporters that remove decarboxylation products to the external medium in exchange for fresh substrate. In the case of LdcI, the lysine-cadaverine antiporter is called CadB. Recently, we determined the X-ray crystal structure of LdcI to 2.0 Å, and we discovered a novel small-molecule bound to LdcI the stringent response regulator guanosine 5'-diphosphate,3'-diphosphate (ppGpp). The stringent response occurs when exponentially growing cells experience nutrient deprivation or one of a number of other stresses. As a result, cells produce ppGpp which leads to a signaling cascade culminating in the shift from exponential growth to stationary phase growth. We have demonstrated that ppGpp is a specific inhibitor of LdcI. Here we describe the lysine decarboxylase assay, modified from the assay developed by Phan et al., that we have used to determine the activity of LdcI and the effect of pppGpp/ppGpp on that activity. The LdcI decarboxylation reaction removes the α-carboxy group of L-lysine and produces carbon dioxide and the polyamine cadaverine (1,5-diaminopentane). L-lysine and cadaverine can be reacted with 2,4,6-trinitrobenzensulfonic acid (TNBS) at high pH to generate N,N'-bistrinitrophenylcadaverine (TNP-cadaverine) and N,N'-bistrinitrophenyllysine (TNP-lysine), respectively. The TNP-cadaverine can be separated from the TNP-lysine as the former is soluble in organic solvents such as toluene while the latter is not. The linear range of the assay was determined empirically using purified cadaverine.";
		String text1 = "Expression of Arabidopsis glycine - rich RNA - binding protein ENT100 or ENT200 improves grain yield of rice ( Oryza sativa ) under ENT00000000000 conditions .";
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

		ISentence sentence = new SentenceImpl(0, text1.length()-1, text1);
//		sentence.setParsingTokens(nlp.parsingSentence(sentence));
//		SyntaxTreeViewerGUI gui = new SyntaxTreeViewerGUI(sentence);
//		JDialog jDialog = new JDialog();
//		jDialog.setLayout(new BorderLayout());
//		JScrollPane pane = new JScrollPane();
//		pane.setViewportView(gui);
//		jDialog.add(pane);
//		jDialog.setSize(new Dimension(1000, 800));
//		jDialog.setModal(true);
//		jDialog.setVisible(true);
		//		sentence = new Sentence(0, text2.length()-1, text2);
		//		nlp.parsingSentence(sentence);
		//		sentence = new Sentence(0, text3.length()-1, text3);
		//		nlp.parsingSentence(sentence);
		//		sentence = new Sentence(0, text4.length()-1, text4);
		//		nlp.parsingSentence(sentence);
		//		sentence = new Sentence(0, test5.length()-1, test5);
		//		nlp.parsingSentence(sentence);
		//		sentence = new Sentence(0, test6.length()-1, test6);
		//		nlp.parsingSentence(sentence);
		//		sentence = new Sentence(0, test7.length()-1, test7);
		//		nlp.parsingSentence(sentence);
		//		sentence = new Sentence(0, test8.length()-1, test8);
		//		nlp.parsingSentence(sentence);
//		sentence = new Sentence(0, test12simp.length()-1, test12simp);	
		sentence.setParsingTokens(nlp.parsingSentence(sentence));
		SyntaxTreeViewerPane gui = new SyntaxTreeViewerPane(sentence);
		JDialog jDialog = new JDialog();
		jDialog.setLayout(new BorderLayout());
		JScrollPane pane = new JScrollPane();
		pane.setViewportView(gui);
		jDialog.add(pane);
		jDialog.setSize(new Dimension(1000, 800));
		jDialog.setModal(true);
		jDialog.setVisible(true);
		//		sentence = new Sentence(0, test10.length()-1, test10);
		//		nlp.parsingSentence(sentence);
		//		sentence = new Sentence(0, test11.length()-1, test11);
		//		nlp.parsingSentence(sentence);
		//		sentence = new Sentence(0, test12b.length()-1, test12b);
		//		nlp.parsingSentence(sentence);
		//		sentence = new Sentence(0, test13.length()-1, test13);
		//		nlp.parsingSentence(sentence);
		//		sentence = new Sentence(0, test14.length()-1, test14);
		//		nlp.parsingSentence(sentence);
		//		Set<String> posTags = new HashSet<String>();
		//		for(PartOfSpeechLabels label:PartOfSpeechLabels.values())
		//		{
		//			if(label.getEnableDefaultValue())
		//			{
		//				posTags.add(label.value());
		//			}
		//		}
		//		nlp.geTextSegmentsFilterByPOSTags(TermSeparator.termSeparator(text),posTags);
	}



}
