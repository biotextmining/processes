package com.silicolife.textmining.processes.nlptools.opennlp;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.silicolife.textmining.core.datastructures.documents.structure.ChunkerTokenImpl;
import com.silicolife.textmining.core.datastructures.documents.structure.POSTokenImpl;
import com.silicolife.textmining.core.datastructures.documents.structure.ParsingTokenImpl;
import com.silicolife.textmining.core.datastructures.documents.structure.SentenceImpl;
import com.silicolife.textmining.core.datastructures.nlptools.OpenNLPSentenceSpliter;
import com.silicolife.textmining.core.datastructures.nlptools.TextSegments;
import com.silicolife.textmining.core.interfaces.core.document.structure.IChunkerToken;
import com.silicolife.textmining.core.interfaces.core.document.structure.IPOSToken;
import com.silicolife.textmining.core.interfaces.core.document.structure.IParsingToken;
import com.silicolife.textmining.core.interfaces.core.document.structure.ISentence;
import com.silicolife.textmining.core.interfaces.core.document.structure.ITextSegment;

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

/**
 * Class that represents a OpenNLP Tagging 
 * 
 * @author Hugo Costa
 *
 */
public class OpenNLP {

	private final static String tokeniserModelFile = "nlpmodels/en-token.bin";
	private final static String postaggingModelFile = "nlpmodels/en-pos-maxent.bin";
	private final static String parsingModelFile = "nlpmodels/en-parser-chunking.bin";
	private final static String chunkerModelFile = "nlpmodels/en-chunker.bin";


	private POSModel postaggerModel;
	private TokenizerModel tokeniserModel; 
	private ChunkerModel chunkerModel;
	private ParserModel parserModel;
	private Parser parser;

	private static OpenNLP _instance;

	private OpenNLP(){

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
	
	public SentenceModel getSentenceModel() throws IOException {
		return OpenNLPSentenceSpliter.getInstance().getSentenceModel();
	}

	public POSModel getPostaggerModel() throws IOException {
		if(postaggerModel == null)
			postaggerModel = initPosTagModel();
		return postaggerModel;
	}

	public TokenizerModel getTokeniserModel() throws IOException {
		if(tokeniserModel == null)
			tokeniserModel = initTokenizerModel();
		return tokeniserModel;
	}

	public ChunkerModel getChunkerModel() throws IOException {
		if(chunkerModel == null)
			chunkerModel = initChunkerModelModel();
		return chunkerModel;
	}
	
	public ParserModel getParserModel() throws IOException {
		if(parserModel == null)
			parserModel = initParserModelModel();
		return parserModel;
	}

	public Parser getParser() throws IOException {
		if(parser == null)
			parser = initParser();
		return parser;
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
		SentenceDetectorME sentenceDetector = new SentenceDetectorME(getSentenceModel());
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

		Tokenizer tokenizer = new TokenizerME(getTokeniserModel());
		Span tokenSpans[] = tokenizer.tokenizePos(text);
		String[] tokens = new String[tokenSpans.length];
		
		for(int i=0;i<tokenSpans.length;i++)
			tokens[i] = tokenSpans[i].getCoveredText(text).toString();
		
		POSTaggerME postagger = new POSTaggerME(getPostaggerModel());
		String[] categories = postagger.tag(tokens);
		for(int j=0;j<categories.length;j++){
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
		List<ISentence> sentencesWithChunker = new ArrayList<ISentence>();
		for(ISentence sentence : sentences)
		{
			if(sentence.getOrderPOSTokens()!=null && sentence.getOrderPOSTokens().size() > 0)
			{
				ChunkerME chunker = new ChunkerME(getChunkerModel());
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

		List<ISentence> sentencesWithChunker = new ArrayList<ISentence>();
		for(ISentence sentence : sentences)
			parsingSentence(sentence);
		
		return sentencesWithChunker;
	}

	public List<IParsingToken> parsingSentence(ISentence sentence) throws IOException {

		List<IParsingToken> parsingTokens = new ArrayList<IParsingToken>();
		String sentenceText = sentence.getText();
		if(sentenceText.endsWith("."))
		{
			sentenceText = sentenceText.substring(0, sentenceText.length()-1)+" .";
		}
		Parse topParses[] = ParserTool.parseLine(sentenceText, getParser(), 1);
		for(int i=0;i<topParses.length;i++)
		{
			Parse parserRes = topParses[i];
			ParsingTokenImpl father = new ParsingTokenImpl(String.valueOf(parserRes.getSpan().getStart()+"-"+parserRes.getSpan().getEnd()), parserRes.getSpan().getStart(), parserRes.getSpan().getEnd(), parserRes.getType(), parserRes.getText());
			builtTreeParsingTokens(father,parsingTokens,parserRes);
		}
		return parsingTokens;
	}
	
	public List<IParsingToken> parsingSentence(String sentence) throws IOException {
		List<IParsingToken> parsingTokens = new ArrayList<IParsingToken>();
		if(sentence.endsWith("."))
		{
			sentence = sentence.substring(0, sentence.length()-1)+" .";
		}
		Parse topParses[] = ParserTool.parseLine(sentence, getParser(), 1);
		for(int i=0;i<topParses.length;i++)
		{
			Parse parserRes = topParses[i];
			ParsingTokenImpl father = new ParsingTokenImpl(String.valueOf(parserRes.getSpan().getStart()+"-"+parserRes.getSpan().getEnd()), parserRes.getSpan().getStart(), parserRes.getSpan().getEnd(), parserRes.getType(), parserRes.getText());
			builtTreeParsingTokens(father,parsingTokens,parserRes);
		}
		return parsingTokens;
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


	/**
	 * Creates the singleton instance.
	 */
	private static void createInstance(){

		if (_instance == null) {
			_instance = new OpenNLP();
		}
	}

	private ChunkerModel initChunkerModelModel() throws IOException{
		InputStream modelIn = OpenNLP.class.getClassLoader().getResourceAsStream(chunkerModelFile);
		return new ChunkerModel(modelIn);
	}

	private ParserModel initParserModelModel() throws IOException{
		InputStream parsingModelIn = OpenNLP.class.getClassLoader().getResourceAsStream(parsingModelFile);
		return new ParserModel(parsingModelIn);
	}
	
	private Parser initParser() throws IOException{
		return ParserFactory.create(getParserModel());
	}

	private TokenizerModel initTokenizerModel() throws IOException{
		InputStream tokenizerFileInput = OpenNLP.class.getClassLoader().getResourceAsStream(tokeniserModelFile);
		return new TokenizerModel(tokenizerFileInput);
	}

	private POSModel initPosTagModel() throws IOException{
		InputStream postaggerFileInput = OpenNLP.class.getClassLoader().getResourceAsStream(postaggingModelFile);
		return new POSModel(postaggerFileInput);
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


//	private String show(Parse parserRes) {
//		StringBuffer sb = new StringBuffer(parserRes.getText().length()*4);
//		show(parserRes,sb);
//		return sb.toString();
//	}
//
//	private void show(Parse parserRes,StringBuffer sb) {
//		int start = parserRes.getSpan().getStart();
//		if (!parserRes.getType().equals(AbstractBottomUpParser.TOK_NODE)) {
//			sb.append("(");
//			sb.append(parserRes.getType()).append(" ");
//		}
//		for (Parse c : parserRes.getChildren()) {
//			Span s = c.getSpan();
//			if (start < s.getStart()) {
//				sb.append(encodeToken(parserRes.getText().substring(start, s.getStart())));
//			}
//			c.show(sb);
//			start = s.getEnd();
//		}
//		if (start < parserRes.getSpan().getEnd()) {
//			sb.append(encodeToken(parserRes.getText().substring(start, parserRes.getSpan().getEnd())));
//		}
//		if (!parserRes.getType().equals(AbstractBottomUpParser.TOK_NODE)) {
//			sb.append(")");
//		}
//	}
//
//
//	private static String encodeToken(String token) {
//		if (Parse.BRACKET_LRB.equals(token)) {
//			return "-LRB-";
//		}
//		else if (Parse.BRACKET_RRB.equals(token)) {
//			return "-RRB-";
//		}
//		else if (Parse.BRACKET_LCB.equals(token)) {
//			return "-LCB-";
//		}
//		else if (Parse.BRACKET_RCB.equals(token)) {
//			return "-RCB-";
//		}
//
//		return token;
//	}

}
