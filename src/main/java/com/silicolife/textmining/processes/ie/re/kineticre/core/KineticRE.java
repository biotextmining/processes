package com.silicolife.textmining.processes.ie.re.kineticre.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.silicolife.textmining.core.datastructures.annotation.re.EventAnnotationImpl;
import com.silicolife.textmining.core.datastructures.annotation.re.EventPropertiesImpl;
import com.silicolife.textmining.core.datastructures.documents.AnnotatedDocumentImpl;
import com.silicolife.textmining.core.datastructures.documents.AnoteClassInDocument;
import com.silicolife.textmining.core.datastructures.exceptions.process.InvalidConfigurationException;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.language.LanguageProperties;
import com.silicolife.textmining.core.datastructures.process.ProcessOriginImpl;
import com.silicolife.textmining.core.datastructures.report.processes.REProcessReportImpl;
import com.silicolife.textmining.core.datastructures.utils.GenerateRandomId;
import com.silicolife.textmining.core.datastructures.utils.Utils;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalNames;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalOptions;
import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.annotation.IEventAnnotation;
import com.silicolife.textmining.core.interfaces.core.annotation.re.IEventProperties;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IAnnotatedDocument;
import com.silicolife.textmining.core.interfaces.core.document.IDocumentSet;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.corpus.ICorpus;
import com.silicolife.textmining.core.interfaces.core.document.structure.ISentence;
import com.silicolife.textmining.core.interfaces.core.general.classe.IAnoteClass;
import com.silicolife.textmining.core.interfaces.core.report.processes.IREProcessReport;
import com.silicolife.textmining.core.interfaces.process.IProcessOrigin;
import com.silicolife.textmining.core.interfaces.process.IE.IIEProcess;
import com.silicolife.textmining.core.interfaces.process.IE.IREProcess;
import com.silicolife.textmining.core.interfaces.process.IE.re.IREConfiguration;
import com.silicolife.textmining.processes.ie.re.kineticre.configuration.IREKineticREConfiguration;
import com.silicolife.textmining.processes.nlptools.opennlp.OpenNLP;

/**
 * Main Class for Kinetic RE
 * 
 * @author Hugo Costa
 * @author Ana Alão Freitas
 *
 */
public class KineticRE implements IREProcess {
	
	final static Logger nerlogger = LoggerFactory.getLogger(KineticRE.class);

	
	public static String kineticREDescrition = "Kinetic RE";
	public final static IProcessOrigin relationProcessType = new ProcessOriginImpl(GenerateRandomId.generateID(),kineticREDescrition);
	private boolean stop = false;

	// Scores das diferentes Classes, defined by user
//	private static int unitScore = 5;
//	private static int valScore = 5;
//	private static int kParamScore = 10000;
//	private static int enzScore = 1000;
//	private static int metScore = 100;
//	private static int orgScore = 0; 
	
	public KineticRE()
	{
		super();
	}

	public IREProcessReport executeRE(IREConfiguration configuration) throws ANoteException,InvalidConfigurationException {
		validateConfiguration(configuration);
		IREKineticREConfiguration reConfiguration = (IREKineticREConfiguration) configuration;
		IIEProcess reProcess = build(configuration, reConfiguration);		
		InitConfiguration.getDataAccess().createIEProcess(reProcess);
		IIEProcess ieProcess = (IIEProcess) configuration.getEntityBasedProcess();
		REProcessReportImpl report = new REProcessReportImpl(LanguageProperties.getLanguageStream("pt.uminho.anote2.kineticre.report.title"),ieProcess,reProcess,false);
		ICorpus corpus = configuration.getCorpus();
		IDocumentSet docs = corpus.getArticlesCorpus();		
		Iterator<IPublication> itDocs = docs.iterator();
		long startime = GregorianCalendar.getInstance().getTimeInMillis();
		int step=0;
		int total = docs.size();
		while(itDocs.hasNext()&& !stop) {
			IPublication doc = itDocs.next();
			IAnnotatedDocument annotdocNER = new AnnotatedDocumentImpl(doc, ieProcess, corpus);
			List<IEntityAnnotation> entitiesdoc = annotdocNER.getEntitiesAnnotations();
			IAnnotatedDocument annotDOcKinetic = new AnnotatedDocumentImpl(doc, reProcess, corpus);
			List<IEventAnnotation> eventsDoc = processDocument(reConfiguration, entitiesdoc, annotdocNER);
			insertAnnotationsInDatabse(reProcess,report,annotDOcKinetic,entitiesdoc,eventsDoc);
			memoryAndProgress(step, total, startime);
			step++;
		};	
		if(stop)
		{
			report.setcancel();
		}
		InitConfiguration.getDataAccess().registerCorpusProcess(corpus, reProcess);
		return report;
	}

	private IIEProcess build(IREConfiguration configuration, IREKineticREConfiguration reConfiguration) {
		IIEProcess reProcess = configuration.getIEProcess();
		reProcess.setName(KineticRE.kineticREDescrition+" "+Utils.SimpleDataFormat.format(new Date()));
		reProcess.setProperties(generateConfiguration(reConfiguration));
		if(reProcess.getCorpus() == null)
			reProcess.setCorpus(configuration.getCorpus());
		return reProcess;
	}

	private Properties generateConfiguration(IREKineticREConfiguration reConfiguration) {
		Properties properties = new Properties();
		properties.put(KineticREGroupsEnum.Units.toString(), classesTOString(reConfiguration.getUnitsClasses()));
		properties.put(KineticREGroupsEnum.Values.toString(), classesTOString(reConfiguration.getValuesClasses()));
		properties.put(KineticREGroupsEnum.KParameters.toString(), classesTOString(reConfiguration.getKineticParametersClasses()));
		properties.put(KineticREGroupsEnum.Metabolites.toString(), classesTOString(reConfiguration.getMetabolitesClasses()));
		properties.put(KineticREGroupsEnum.Enzymes.toString(), classesTOString(reConfiguration.getEnzymesClasses()));
		properties.put(KineticREGroupsEnum.Organism.toString(), classesTOString(reConfiguration.getOrganismClasses()));
		return properties;
	}
	
	public String classesTOString(Set<IAnoteClass> classes) 
	{
		String result = new String();
		for(IAnoteClass klass:classes)
			result = result + klass.getName() + ",";
		if(result.isEmpty())
			return result;
		return result.substring(0,result.length()-1);
	}

	protected void memoryAndProgress(int step, int total,long startime) {
		if(step%50==0)
		{
			System.out.println((GlobalOptions.decimalformat.format((double)step/ (double) total * 100)) + " %...");
			nerlogger.info((GlobalOptions.decimalformat.format((double)step/ (double) total * 100)) + " %...");
		}
	}

	protected List<IEventAnnotation> processDocument(IREKineticREConfiguration reConfiguration,
			List<IEntityAnnotation> entitiesdoc, IAnnotatedDocument annotdocNER) throws ANoteException {
		List<IEventAnnotation> eventsDoc = new ArrayList<>();
		String text = annotdocNER.getDocumentAnnotationText();
		// Order document entities
		Collections.sort(entitiesdoc, new EntityAnnotationListSort());
		AnoteClassInDocument organismInDocument = new AnoteClassInDocument(reConfiguration.getOrganismClasses(), annotdocNER);
		AnoteClassInDocument enzymesInDocument = new AnoteClassInDocument(reConfiguration.getEnzymesClasses(), annotdocNER);
		AnoteClassInDocument metabolitesInDocument = new AnoteClassInDocument(reConfiguration.getMetabolitesClasses(), annotdocNER);
		List<ISentence> sentences;
		try {
			sentences = OpenNLP.getInstance().getSentencesText(text);
			for (ISentence sentence:sentences) {
				processDocumentSentence(reConfiguration, entitiesdoc, eventsDoc,sentence,organismInDocument,enzymesInDocument,metabolitesInDocument);
			}
		} catch (IOException e) {
			throw new ANoteException(e);
		}
		return eventsDoc;
	}

	private void processDocumentSentence(IREKineticREConfiguration reConfiguration, List<IEntityAnnotation> entitiesdoc,
			List<IEventAnnotation> eventsDoc, ISentence sentence, AnoteClassInDocument organismInDocument,AnoteClassInDocument enzymesInDocument, AnoteClassInDocument metabolitesInDocument) {
		List<IEntityAnnotation> sentenceEntities = getSentenceEntities(entitiesdoc, sentence);
		List<IEntityAnnotation> kparamSentenceEntities = getEntByClass(sentenceEntities, reConfiguration.getKineticParametersClasses());
		List<IEntityAnnotation> valueSentenceEntities = getEntByClass(sentenceEntities, reConfiguration.getValuesClasses());
		// Create pair valor-unidade list in sentence;
		List<ValueUnitBasedRelation> listPairsValueUnit = generateListPairsValueUnit(reConfiguration,sentenceEntities);
		// Complex - special pairs
		List<ValueUnitSimpleOrComplex> valueUnitSimpleOrComplexList = generateListPairsValueUnitSimpleOrComplex(reConfiguration,sentence,listPairsValueUnit, valueSentenceEntities);		
		List<KparamValueUnitSimpleOrComplex> listTriplesSorC = new ArrayList<>();
		// Create triples in sentence (value, unit and kinetic parameter);
		if (!valueUnitSimpleOrComplexList.isEmpty() && !kparamSentenceEntities.isEmpty()) {
			// Pair range
			defineStarEndRelationPairsSimplexComplex(valueUnitSimpleOrComplexList, sentence);
			listTriplesSorC = getSimplexComplexTriples(sentence, valueUnitSimpleOrComplexList, kparamSentenceEntities);
			// create relations
			List<IEventAnnotation> results = findrelationsTriplesAndOrganism(listTriplesSorC, sentence,organismInDocument,enzymesInDocument,metabolitesInDocument);
			eventsDoc.addAll(results);
		}
	}
	
	private List<ValueUnitSimpleOrComplex> generateListPairsValueUnitSimpleOrComplex(IREKineticREConfiguration configuration,ISentence sent,
			List<ValueUnitBasedRelation> listPairsValueUnit, List<IEntityAnnotation> valuesSent) {
		// Special cases
		List<ValueUnitSimpleOrComplex> valueUnitSimpleOrComplexList = new ArrayList<>();
		Set<ValueUnitBasedRelation> alreadyUsed = new HashSet<>();

		System.out.println("frase: " + sent);
		System.out.println("tamanho lista pares: " + listPairsValueUnit.size());
		//case 1_3: vu1, vu2, vu3, vu4 and vu5
		if(listPairsValueUnit.size() > 4) {
			//System.out.println("Entrei nos VU-VUs -> " + sent + "-> " + valuesSent.size() + "\n");
			for(int i=4;i<listPairsValueUnit.size();i++) {
				ValueUnitBasedRelation before_4 = listPairsValueUnit.get(i-4);
				ValueUnitBasedRelation before_3 = listPairsValueUnit.get(i-3);
				ValueUnitBasedRelation before_2 = listPairsValueUnit.get(i-2);
				ValueUnitBasedRelation before = listPairsValueUnit.get(i-1);
				ValueUnitBasedRelation now = listPairsValueUnit.get(i);
				int start = (int) (before.getUnit().getEndOffset() -  sent.getStartOffset());
				int end = (int) (now.getValue().getStartOffset() -  sent.getStartOffset());
				String sentence = sent.getText();
				if(end-start < configuration.getAdvancedConfiguration().getMaxDistanceBetweenTwoVUPairsWithAndInside()) {
					String substringSentence = sentence.substring(start, end).toLowerCase();
					if(KineticREUtils.hasPairConnector(substringSentence)) {
						int start_1 = (int) (before_2.getUnit().getEndOffset() -  sent.getStartOffset());
						int end_1 = (int) (before.getValue().getStartOffset() -  sent.getStartOffset());
						//String sentence = sent.getText();
						if(end_1-start_1 < configuration.getAdvancedConfiguration().getMaxDistanceBetweenTwoVUPairsWithAndInside()) {
							String substringSentence_1 = sentence.substring(start_1, end_1).toLowerCase();
							if(KineticREUtils.hasPairConnector(substringSentence_1)) {
								int start_2 = (int) (before_3.getUnit().getEndOffset() -  sent.getStartOffset());
								int end_2 = (int) (before_2.getValue().getStartOffset() -  sent.getStartOffset());
								//String sentence = sent.getText();
								if(end_2-start_2 < configuration.getAdvancedConfiguration().getMaxDistanceBetweenTwoVUPairsWithAndInside()) {
									String substringSentence_2 = sentence.substring(start_2, end_2).toLowerCase();
									if(KineticREUtils.hasPairConnector(substringSentence_2)) {
										int start_3 = (int) (before_4.getUnit().getEndOffset() -  sent.getStartOffset());
										int end_3 = (int) (before_3.getValue().getStartOffset() -  sent.getStartOffset());
										//String sentence = sent.getText();
										if(end_3-start_3 < configuration.getAdvancedConfiguration().getMaxDistanceBetweenTwoVUPairsWithAndInside()) {
											String substringSentence_3 = sentence.substring(start_3, end_3).toLowerCase();
											if(KineticREUtils.hasPairConnector(substringSentence_3)) {
												List<ValueUnitBasedRelation> list = new ArrayList<>();
												list.add(before_4);
												list.add(before_3);
												list.add(before_2);
												list.add(before);
												list.add(now);
												valueUnitSimpleOrComplexList.add(new ValueUnitSimpleOrComplex(list));
												alreadyUsed.add(before_4);
												alreadyUsed.add(before_3);
												alreadyUsed.add(before_2);
												alreadyUsed.add(before);
												alreadyUsed.add(now);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		//case 1_2: vu1, vu2, vu3 and vu4
		else if(listPairsValueUnit.size() > 3) {
			for(int i=3;i<listPairsValueUnit.size();i++) {
				ValueUnitBasedRelation before_3 = listPairsValueUnit.get(i-3);
				ValueUnitBasedRelation before_2 = listPairsValueUnit.get(i-2);
				ValueUnitBasedRelation before = listPairsValueUnit.get(i-1);
				ValueUnitBasedRelation now = listPairsValueUnit.get(i);
				int start = (int) (before.getUnit().getEndOffset() -  sent.getStartOffset());
				int end = (int) (now.getValue().getStartOffset() -  sent.getStartOffset());
				String sentence = sent.getText();
				if(end-start < configuration.getAdvancedConfiguration().getMaxDistanceBetweenTwoVUPairsWithAndInside()) {
					String substringSentence = sentence.substring(start, end).toLowerCase();
					if(KineticREUtils.hasPairConnector(substringSentence)) {
						int start_1 = (int) (before_2.getUnit().getEndOffset() -  sent.getStartOffset());
						int end_1 = (int) (before.getValue().getStartOffset() -  sent.getStartOffset());
						//String sentence = sent.getText();
						if(end_1-start_1 < configuration.getAdvancedConfiguration().getMaxDistanceBetweenTwoVUPairsWithAndInside()) {
							String substringSentence_1 = sentence.substring(start_1, end_1).toLowerCase();
							if(KineticREUtils.hasPairConnector(substringSentence_1)) {
								int start_2 = (int) (before_3.getUnit().getEndOffset() -  sent.getStartOffset());
								int end_2 = (int) (before_2.getValue().getStartOffset() -  sent.getStartOffset());
								//String sentence = sent.getText();
								if(end_2-start_2 < configuration.getAdvancedConfiguration().getMaxDistanceBetweenTwoVUPairsWithAndInside()) {
									String substringSentence_2 = sentence.substring(start_2, end_2).toLowerCase();
									if(KineticREUtils.hasPairConnector(substringSentence_2)) {
										List<ValueUnitBasedRelation> list = new ArrayList<>();
										list.add(before_3);
										list.add(before_2);
										list.add(before);
										list.add(now);
										valueUnitSimpleOrComplexList.add(new ValueUnitSimpleOrComplex(list));
										alreadyUsed.add(before_3);
										alreadyUsed.add(before_2);
										alreadyUsed.add(before);
										alreadyUsed.add(now);	
									}
								}
							}
						}
					}
				}
			}
		}
		//case 1_1: vu1, vu2 and vu3
		else if(listPairsValueUnit.size() > 2) {
			for(int i=2;i<listPairsValueUnit.size();i++) {
				ValueUnitBasedRelation before_2 = listPairsValueUnit.get(i-2);
				ValueUnitBasedRelation before = listPairsValueUnit.get(i-1);
				ValueUnitBasedRelation now = listPairsValueUnit.get(i);
				int start = (int) (before.getUnit().getEndOffset() -  sent.getStartOffset());
				int end = (int) (now.getValue().getStartOffset() -  sent.getStartOffset());
				String sentence = sent.getText();
				if(end-start < configuration.getAdvancedConfiguration().getMaxDistanceBetweenTwoVUPairsWithAndInside()) {
					String substringSentence = sentence.substring(start, end).toLowerCase();
					if(KineticREUtils.hasPairConnector(substringSentence)) {
						int start_1 = (int) (before_2.getUnit().getEndOffset() -  sent.getStartOffset());
						int end_1 = (int) (before.getValue().getStartOffset() -  sent.getStartOffset());
						//String sentence = sent.getText();
						if(end_1-start_1 < configuration.getAdvancedConfiguration().getMaxDistanceBetweenTwoVUPairsWithAndInside()) {
							String substringSentence_1 = sentence.substring(start_1, end_1).toLowerCase();
							if(KineticREUtils.hasPairConnector(substringSentence_1)) {
								List<ValueUnitBasedRelation> list = new ArrayList<>();
								list.add(before_2);
								list.add(before);
								list.add(now);
								valueUnitSimpleOrComplexList.add(new ValueUnitSimpleOrComplex(list));
								alreadyUsed.add(before_2);
								alreadyUsed.add(before);
								alreadyUsed.add(now);	
							}
						}
					}
				}
			}
		}	
		// case 1_0: vu1 and vu2
		else {
			for(int i=1;i<listPairsValueUnit.size();i++) {
				ValueUnitBasedRelation before = listPairsValueUnit.get(i-1);
				ValueUnitBasedRelation now = listPairsValueUnit.get(i);
				int start = (int) (before.getUnit().getEndOffset() -  sent.getStartOffset());
				int end = (int) (now.getValue().getStartOffset() -  sent.getStartOffset());
				String sentence = sent.getText();
				if(end-start < configuration.getAdvancedConfiguration().getMaxDistanceBetweenTwoVUPairsWithAndInside()) {
					String substringSentence = sentence.substring(start, end).toLowerCase();
					//if((substringsentence.contains(" and ") || substringsentence.contains(" or ") || substringsentence.contains(" vs ") || substringsentence.contains(" , ")) && !(substringsentence.contains(")") && substringsentence.contains("("))) {
					if(KineticREUtils.hasPairConnector(substringSentence)) {
						List<ValueUnitBasedRelation> list = new ArrayList<>();
						list.add(before);
						list.add(now);
						valueUnitSimpleOrComplexList.add(new ValueUnitSimpleOrComplex(list));
						alreadyUsed.add(before);
						alreadyUsed.add(now);
					}
				}
			}
		}

		//System.out.println("tamanho lista values: " + valuesSent.size());
		//Case 2 -> Vs - VU
		calculateComplexVVU(listPairsValueUnit, valueUnitSimpleOrComplexList, alreadyUsed, valuesSent, sent);
		
		//Case 3: adicionar os pares simples
		calculateSimplePairs(listPairsValueUnit, valueUnitSimpleOrComplexList, alreadyUsed);
		
		return valueUnitSimpleOrComplexList;
		
/*		// Case 1
		for(int i=1;i<listPairsValueUnit.size();i++) {
			ValueUnitBasedRelation before = listPairsValueUnit.get(i-1);
			ValueUnitBasedRelation now = listPairsValueUnit.get(i);
			int start = (int) (before.getUnit().getEndOffset() -  sent.getStartOffset());
			int end = (int) (now.getValue().getStartOffset() -  sent.getStartOffset());
			String sentence = sent.getText();
			if(end-start < configuration.getAdvancedConfiguration().getMaxDistanceBetweenTwoVUPairsWithAndInside()) {
				String substringsentence = sentence.substring(start, end).toLowerCase();
				//if((substringsentence.contains(" and ") || substringsentence.contains(" or ") || substringsentence.contains(" vs ") || substringsentence.contains(" , ")) && !(substringsentence.contains(")") && substringsentence.contains("("))) {
				if(substringsentence.contains(" and ") || substringsentence.contains(" or ") || substringsentence.contains(" vs ") || substringsentence.contains(" to ") || substringsentence.contains(" as opposed to ") || substringsentence.contains(" versus ") || substringsentence.contains(" , ")) {
					List<ValueUnitBasedRelation> list = new ArrayList<>();
					list.add(before);
					list.add(now);
					valueUnitSimpleOrComplexList.add(new ValueUnitSimpleOrComplex(list));
					alreadyUsed.add(before);
					alreadyUsed.add(now);
				}
			}
		}
		
		// Case 2
		for(ValueUnitBasedRelation pair : listPairsValueUnit) {
			//método para descobrir o Valor à esquerda do Par, mais perto
			IEntityAnnotation valueLeft = closestEntityLeftPair(pair, valuesSent, sent);
			if(!alreadyUsed.contains(pair) && valueLeft!=null) {
				ValueUnitBasedRelation before = new ValueUnitBasedRelation(valueLeft, pair.getUnit());
				ValueUnitBasedRelation now = pair;
				int start = (int) (valueLeft.getEndOffset() -  sent.getStartOffset());
				int end = (int) (now.getValue().getStartOffset() -  sent.getStartOffset());
				String sentence = sent.getText();
				if(end-start < configuration.getAdvancedConfiguration().getMaxDistanceBetweenTwoVUPairsWithAndInside()) {
					//System.out.println("test substring: " + start + "<->" + end);
					String substringsentence = sentence.substring(start, end).toLowerCase();
					//if((substringsentence.contains(" and ") || substringsentence.contains(" or ") || substringsentence.contains(" vs ") || substringsentence.contains(" , ")) && !(substringsentence.contains(")") && substringsentence.contains("("))) {
					if(substringsentence.contains(" and ") || substringsentence.contains(" or ") || substringsentence.contains(" vs ") || substringsentence.contains(" to ") || substringsentence.contains(" as opposed to ") || substringsentence.contains(" versus ") || substringsentence.contains(" , ")) {	
						List<ValueUnitBasedRelation> list = new ArrayList<>();
						list.add(before);
						list.add(now);
						valueUnitSimpleOrComplexList.add(new ValueUnitSimpleOrComplex(list));
						alreadyUsed.add(now);
					}
				}
			}
		}	
		//Case 3: adicionar os pares simples
		for(ValueUnitBasedRelation pair : listPairsValueUnit) {
			if(!alreadyUsed.contains(pair)) {
				ValueUnitBasedRelation now = pair;
				List<ValueUnitBasedRelation> list = new ArrayList<>();
				list.add(now);
				valueUnitSimpleOrComplexList.add(new ValueUnitSimpleOrComplex(list));
				alreadyUsed.add(now);
			}
		}
		return valueUnitSimpleOrComplexList;
*/
	}
	
	private void calculateComplexVVU(List<ValueUnitBasedRelation> listPairsValueUnit,
			List<ValueUnitSimpleOrComplex> valueUnitSimpleOrComplexList, Set<ValueUnitBasedRelation> alreadyUsed, List<IEntityAnnotation> valuesSent, ISentence sent) {
		for(ValueUnitBasedRelation pair : listPairsValueUnit) {
			//only using the ones that were not used yet;
			if(!alreadyUsed.contains(pair)) {
				List<ValueUnitSimpleOrComplex> complexVVUlist = getVVUcomplex(pair, valuesSent, sent);
				if(complexVVUlist != null) {
					alreadyUsed.add(pair);
					valueUnitSimpleOrComplexList.addAll(complexVVUlist);	
				}
			}	
		}
	}

	private List<ValueUnitSimpleOrComplex> getVVUcomplex(ValueUnitBasedRelation pair, List<IEntityAnnotation> valuesSent, ISentence sent) {
		List<IEntityAnnotation> valuesSentLeftPair = new ArrayList<>();
		List<ValueUnitSimpleOrComplex> listComplex = new ArrayList<>();
		//cria lista de valores à Esquerda de um Par
		for(IEntityAnnotation value : valuesSent) {
			if(value.getEndOffset() < pair.getValue().getStartOffset()) {
				valuesSentLeftPair.add(value);
			}
		}
		if(!valuesSentLeftPair.isEmpty()) {
			int endPoint = (int) (pair.getValue().getStartOffset() - sent.getStartOffset());
			List<ValueUnitBasedRelation> list = new ArrayList<>();

			for(int i=valuesSentLeftPair.size()-1; i>=0; i--) {
				int startPoint = (int) (valuesSentLeftPair.get(i).getEndOffset() - sent.getStartOffset());
				String sentence = sent.getText();
				String substringSentence = sentence.substring(startPoint, endPoint).toLowerCase();
				if(KineticREUtils.hasPairConnector(substringSentence)) {	
					ValueUnitBasedRelation newPair = new ValueUnitBasedRelation(valuesSentLeftPair.get(i), pair.getUnit());
					list.add(newPair);
					endPoint = (int) (valuesSentLeftPair.get(i).getStartOffset() - sent.getStartOffset());
				}
				else {
					if(list.isEmpty())
						return null;
					list.add(pair);
					//listComplex.add(new ValueUnitSimpleOrComplex(pair));
					listComplex.add(new ValueUnitSimpleOrComplex(list));
					return listComplex;
				}
			}
			if(list.isEmpty())
				return null;
			list.add(pair);
			//listComplex.add(new ValueUnitSimpleOrComplex(pair));
			listComplex.add(new ValueUnitSimpleOrComplex(list));
			return listComplex;
		}
		return null;
	}

	private void calculateSimplePairs(List<ValueUnitBasedRelation> listPairsValueUnit,List<ValueUnitSimpleOrComplex> valueUnitSimpleOrComplexList, Set<ValueUnitBasedRelation> alreadyUsed) {
		for(ValueUnitBasedRelation pair : listPairsValueUnit) {
			if(!alreadyUsed.contains(pair)) {
				ValueUnitBasedRelation now = pair;
				List<ValueUnitBasedRelation> list = new ArrayList<>();
				list.add(now);
				valueUnitSimpleOrComplexList.add(new ValueUnitSimpleOrComplex(list));
				alreadyUsed.add(now);
			}
		}
	}

	private void insertAnnotationsInDatabse(IIEProcess process,IREProcessReport report,IAnnotatedDocument annotDoc,List<IEntityAnnotation> entitiesList,List<IEventAnnotation> relationsList) throws ANoteException {
		for(IEntityAnnotation entity:entitiesList) {
			entity.generateNewId();
		}
		InitConfiguration.getDataAccess().addProcessDocumentEntitiesAnnotations(process, annotDoc, entitiesList);
		report.incrementEntitiesAnnotated(entitiesList.size());
		InitConfiguration.getDataAccess().addProcessDocumentEventAnnoations(process, annotDoc,relationsList);
		report.increaseRelations(relationsList.size());
	}
	
	// method that look at (value-unit) in sentence
	//// @return Lista de Pares
	private List<ValueUnitBasedRelation> generateListPairsValueUnit(IREKineticREConfiguration configuration,List<IEntityAnnotation> entSent) {
		List<ValueUnitBasedRelation> listPairsValueUnit = new ArrayList<>();
		for (int i=0; i<entSent.size()-1; i++) {
			if (configuration.getValuesClasses().contains(entSent.get(i).getClassAnnotation()) &&
					configuration.getUnitsClasses().contains(entSent.get(i+1).getClassAnnotation()) &&
					entSent.get(i+1).getStartOffset() < (entSent.get(i).getEndOffset() + configuration.getAdvancedConfiguration().getMaxDistanceBetweenValueAndUnit())) {
				IEntityAnnotation entValue = entSent.get(i);
				IEntityAnnotation entUnit = entSent.get(i+1);
				ValueUnitBasedRelation pair = new ValueUnitBasedRelation(entValue, entUnit);
				listPairsValueUnit.add(pair);
				i++;	
			}
		}
		return listPairsValueUnit;
	}
	
	// Funcao que define para cada "par" DA LISTA DE SIMPLES e COMPELXOS qual o espaco possivel para a relacao nesse par;
	private void defineStarEndRelationPairsSimplexComplex(List<ValueUnitSimpleOrComplex> valueUnitSimpleOrComplexList, ISentence sent) {
		// se so existir 1 par
		if(valueUnitSimpleOrComplexList.size() == 1) {
			valueUnitSimpleOrComplexList.get(0).setStartRelation(sent.getStartOffset());
			valueUnitSimpleOrComplexList.get(0).setEndRelation(sent.getEndOffset());
		}
		// se existir mais de 1 "par" ou "par + and + par";
		else {
			for(int i=0; i<valueUnitSimpleOrComplexList.size(); i++) {
				if(i == 0) {
					valueUnitSimpleOrComplexList.get(i).setStartRelation(sent.getStartOffset());
					valueUnitSimpleOrComplexList.get(i).setEndRelation(valueUnitSimpleOrComplexList.get(i+1).getStartIndex() - 1);
				}
				else if(i == valueUnitSimpleOrComplexList.size()-1) {
					valueUnitSimpleOrComplexList.get(i).setStartRelation(valueUnitSimpleOrComplexList.get(i-1).getEndIndex() + 1);
					valueUnitSimpleOrComplexList.get(i).setEndRelation(sent.getEndOffset());
				}
				else {
					valueUnitSimpleOrComplexList.get(i).setStartRelation(valueUnitSimpleOrComplexList.get(i-1).getEndIndex() + 1);
					valueUnitSimpleOrComplexList.get(i).setEndRelation(valueUnitSimpleOrComplexList.get(i+1).getStartIndex() - 1);
				}
			}
		}	
	}
	
	// Funcao que retorna os Triplos ("VU" ou "VU and VU" + kparam) existentes na frase, a partir da lista de SimpleComplex;
	private List<KparamValueUnitSimpleOrComplex> getSimplexComplexTriples(ISentence sent, List<ValueUnitSimpleOrComplex> listSimpleOrComplexPairs, List<IEntityAnnotation> kparamSent) {
		List<KparamValueUnitSimpleOrComplex> listTriplesKpSimpleORComplex = new ArrayList<>();
		for(ValueUnitSimpleOrComplex simpleORcomplexPair : listSimpleOrComplexPairs) {
			float score = simpleORcomplexPair.getScore();
			boolean hasKparamAtleft = false;
			KparamValueUnitSimpleOrComplex tripleToAdd = null;
			for(int j=0; j<kparamSent.size(); j++) {
				if(kparamSent.get(j).getStartOffset() >= simpleORcomplexPair.getStartRelation() && kparamSent.get(j).getEndOffset() <= simpleORcomplexPair.getEndRelation())  {
					if(kparamSent.get(j).getEndOffset() < simpleORcomplexPair.getStartIndex()) {
						hasKparamAtleft = true;
						tripleToAdd = new KparamValueUnitSimpleOrComplex(simpleORcomplexPair, kparamSent.get(j));
//						score += kParamScore;
						tripleToAdd.setScore(score);
					}
					else if(kparamSent.get(j).getStartOffset() > simpleORcomplexPair.getEndIndex() && hasKparamAtleft) {
						break;
					}
					else {
						tripleToAdd = new KparamValueUnitSimpleOrComplex(simpleORcomplexPair, kparamSent.get(j));
//						score += kParamScore;
						tripleToAdd.setScore(score);
						break;
					}
				}
			}
			if(tripleToAdd!=null)
				listTriplesKpSimpleORComplex.add(tripleToAdd);
		}
		return listTriplesKpSimpleORComplex;
	}

	// Funcao que procura as relações existentes numa frase (a partir de um Triplo=> pair + kparameter), calcula o score e guarda o relacionamento;
	//// @return Lista de Relacionamentos
	private List<IEventAnnotation> findrelationsTriplesAndOrganism(List<KparamValueUnitSimpleOrComplex> sentTriples, ISentence sent,
			AnoteClassInDocument organismInDocument,AnoteClassInDocument enzymesInDocument, AnoteClassInDocument metabolitesInDocument) {
		List<IEventAnnotation> results = new ArrayList<IEventAnnotation>();
		for(KparamValueUnitSimpleOrComplex tripleSC : sentTriples) {
			for(ValueUnitBasedRelation simpleComplexPair : tripleSC.getSimpleORcomplexPairs().getValueUnitBasedRelationListSorted()) {
				float relationScore = simpleComplexPair.getScore();
				List<IEntityAnnotation> left= new ArrayList<IEntityAnnotation>();
				List<IEntityAnnotation> right= new ArrayList<IEntityAnnotation>();
				
				// Add Base Triple Entities (pair) to relation
				left.add(simpleComplexPair.getValue());
				right.add(simpleComplexPair.getUnit());
				// Add Kinetic Parameter to relation
				IEventProperties eventProperties = new EventPropertiesImpl();

				addKineticParameter(tripleSC, simpleComplexPair, left, right);
				addOrganism(sent, organismInDocument, simpleComplexPair, left, right,eventProperties);
				addEnzyme(sent, enzymesInDocument, simpleComplexPair, left, right,eventProperties);
				addMetabolites(sent, metabolitesInDocument, simpleComplexPair, left, right,eventProperties);
				
				eventProperties.setGeneralProperty("score", String.valueOf(relationScore));
				IEventAnnotation relationTriple = new EventAnnotationImpl(tripleSC.getSimpleORcomplexPairs().getStartRelation(), tripleSC.getSimpleORcomplexPairs().getStartRelation(), GlobalNames.re, left, right, "",eventProperties,false);
			
				results.add(relationTriple);
			}
		}
		return results;
	}

	private void addMetabolites(ISentence sent, AnoteClassInDocument metabolitesInDocument,
			ValueUnitBasedRelation simpleComplexPair, List<IEntityAnnotation> left, List<IEntityAnnotation> right, IEventProperties eventProperties) {
		List<IEntityAnnotation> metabolites = calculateMetabolitesToRelation(sent,metabolitesInDocument,eventProperties);
		for(IEntityAnnotation enzyme:metabolites)
		{
			if(enzyme.getStartOffset()<simpleComplexPair.getValue().getStartOffset())
			{
				left.add(enzyme);
			}
			else
			{
				right.add(enzyme);
			}
		}
	}

	private void addEnzyme(ISentence sent, AnoteClassInDocument enzymesInDocument,
			ValueUnitBasedRelation simpleComplexPair, List<IEntityAnnotation> left, List<IEntityAnnotation> right, IEventProperties eventProperties) {
		List<IEntityAnnotation> enzymes = calculateenzymeToRelation(sent,enzymesInDocument,eventProperties);
		for(IEntityAnnotation enzyme:enzymes)
		{
			if(enzyme.getStartOffset()<simpleComplexPair.getValue().getStartOffset())
			{
				left.add(enzyme);
			}
			else
			{
				right.add(enzyme);
			}
		}
	}

	private void addOrganism(ISentence sent, AnoteClassInDocument organismInDocument,
			ValueUnitBasedRelation simpleComplexPair, List<IEntityAnnotation> left, List<IEntityAnnotation> right, IEventProperties eventProperties) {
		List<IEntityAnnotation> organisms = calculateorganismToRelation(sent,organismInDocument,eventProperties);
		for(IEntityAnnotation organism:organisms)
		{
			if(organism.getStartOffset()<simpleComplexPair.getValue().getStartOffset())
			{
				left.add(organism);
			}
			else
			{
				right.add(organism);
			}
		}
	}

	private void addKineticParameter(KparamValueUnitSimpleOrComplex tripleSC, ValueUnitBasedRelation simpleComplexPair,
			List<IEntityAnnotation> left, List<IEntityAnnotation> right) {
		if(tripleSC.getKparam().getStartOffset() < simpleComplexPair.getValue().getStartOffset()){
			left.add(tripleSC.getKparam());
		}
		else {
			right.add(tripleSC.getKparam());
		}
	}
	
	private List<IEntityAnnotation> calculateMetabolitesToRelation(ISentence sentence, AnoteClassInDocument metabolitesInDocument, IEventProperties eventProperties) {
		
		List<IEntityAnnotation> metabolitesInSentence = metabolitesInDocument.getEntityFilterByClassInSentence(sentence);
		if(!metabolitesInSentence.isEmpty())
		{
			eventProperties.setGeneralProperty("metabolites_score_penalty", String.valueOf(metabolitesInSentence.size()-1));
			return metabolitesInSentence;
		}
		List<IEntityAnnotation> metabolitesInPreviousSentence = metabolitesInDocument.getEntityFilterByClassInPreviousSentence(sentence);
		if(!metabolitesInPreviousSentence.isEmpty())
		{
			eventProperties.setGeneralProperty("metabolites_score_penalty", String.valueOf(10+metabolitesInPreviousSentence.size()-1));

			return metabolitesInPreviousSentence;
		}
		eventProperties.setGeneralProperty("metabolites_score_penalty", "Na");
		return new ArrayList<>();
	}

	private List<IEntityAnnotation> calculateenzymeToRelation(ISentence sentence, AnoteClassInDocument enzymesInDocument, IEventProperties eventProperties) {
		List<IEntityAnnotation> enzymesInSentence = enzymesInDocument.getEntityFilterByClassInSentence(sentence);
		if(!enzymesInSentence.isEmpty())
		{
			eventProperties.setGeneralProperty("enzyme_score_penalty", String.valueOf(enzymesInSentence.size()-1));
			return enzymesInSentence;
		}
		List<IEntityAnnotation> enzymesInPreviousSentence = enzymesInDocument.getEntityFilterByClassInPreviousSentence(sentence);
		if(!enzymesInPreviousSentence.isEmpty())
		{
			eventProperties.setGeneralProperty("enzyme_score_penalty", String.valueOf(10+enzymesInPreviousSentence.size()-1));
			return enzymesInPreviousSentence;
		}
		eventProperties.setGeneralProperty("enzyme_score_penalty", "Na");
		return new ArrayList<>();
	}

	private List<IEntityAnnotation> calculateorganismToRelation(ISentence sentence, AnoteClassInDocument organismInDocument, IEventProperties eventProperties) {
		List<IEntityAnnotation> organismsInSentence = organismInDocument.getEntityFilterByClassInSentence(sentence);
		if(!organismsInSentence.isEmpty())
		{
			eventProperties.setGeneralProperty("organism_score_penalty", String.valueOf(organismsInSentence.size()-1));
			return organismsInSentence;
		}
		List<IEntityAnnotation> organismsInPreviousSentence = organismInDocument.getEntityFilterByClassInPreviousSentence(sentence);
		if(!organismsInPreviousSentence.isEmpty())
		{
			eventProperties.setGeneralProperty("organism_score_penalty", String.valueOf(10+organismsInPreviousSentence.size()-1));
			return organismsInPreviousSentence;
		}
		Collection<IEntityAnnotation> organismAnnotation = organismInDocument.getMapResourceElementAnnotationClosedToOffset().values();
		if(!organismAnnotation.isEmpty())
		{
			eventProperties.setGeneralProperty("organism_score_penalty", String.valueOf(100+organismAnnotation.size()-1));
		}
		else
			eventProperties.setGeneralProperty("organism_score_penalty", "Na");

		return new ArrayList<>(organismAnnotation);
	}

	// Função que buscar entidades de uma frase. (@return Lista das Entidades numa frase)
	protected List<IEntityAnnotation> getSentenceEntities(List<IEntityAnnotation> listEntitiesSortedByOffset, ISentence sentence) {
		List<IEntityAnnotation> result = new ArrayList<IEntityAnnotation>();
		for(IEntityAnnotation ent:listEntitiesSortedByOffset) {
			if(ent.getStartOffset()>=sentence.getStartOffset() && ent.getStartOffset()<sentence.getEndOffset()) {
				result.add(ent);
			}
		}
		return result;
	}

	// Método que retorna uma lista das entidades de uma classe presentes na frase.
	private List<IEntityAnnotation> getEntByClass(List<IEntityAnnotation> entSent, Set<IAnoteClass> defClass) {
		// cria lista
		List<IEntityAnnotation> entClassSent = new ArrayList<IEntityAnnotation>();
		for (IEntityAnnotation entity:entSent)
		{
			if (defClass.contains(entity.getClassAnnotation())) {
				entClassSent.add(entity);
			}
		}
		return entClassSent;	
	}
	
	//Método que retorna o valor à esquerda mais perto do par (value-unit)
	private IEntityAnnotation closestEntityLeftPair(ValueUnitBasedRelation pair, List<IEntityAnnotation> valuesSent, ISentence sent) {
		IEntityAnnotation valueLeft = null;
		long posValueLeft = 0;
		//System.out.println("Test sent:\n" + sent + "\n");
		//System.out.println("TEST Pair: " + pair.getValue() + "<->" + pair.getUnit() + "\n");
		for(IEntityAnnotation value : valuesSent) {
			//System.out.println("TEST each Value:\nvalue-> " + value + "\nvalue.getEndOffset-> " + value.getEndOffset() + "\n");
			if(value.getEndOffset() < pair.getValue().getStartOffset() && value.getEndOffset() > posValueLeft) {
				//System.out.println("TEST VALUE LEFT :\nvalueLeft-> " + valueLeft + "\nvalue.getEndOffset()-> " + value.getEndOffset() + "\nposValueLeft->" + posValueLeft + "\n");
				posValueLeft = value.getEndOffset();
				valueLeft = value;
				//System.out.println("TEST VALUE LEFT 2:\nvalueLeft-> " + valueLeft + "\nvalue.getEndOffset()-> " + value.getEndOffset() + "\nposValueLeft->" + posValueLeft + "\n");
			}
		}
		return valueLeft;
	}

	@Override
	public void validateConfiguration(IREConfiguration configuration)throws InvalidConfigurationException {
		if(configuration instanceof IREKineticREConfiguration) {
			IREKineticREConfiguration kineticREConfiguration = (IREKineticREConfiguration) configuration;
			if(kineticREConfiguration.getCorpus()==null) {
				throw new InvalidConfigurationException("Corpus can not be null");
			}
			if(kineticREConfiguration.getEntityBasedProcess() == null)
			{
				throw new InvalidConfigurationException("IEProcess can not be null");
			}
			if(kineticREConfiguration.getValuesClasses().isEmpty())
			{
				throw new InvalidConfigurationException("Values Class Set can not be empty");
			}
			if(kineticREConfiguration.getUnitsClasses().isEmpty())
			{
				throw new InvalidConfigurationException("Unit Class Set can not be empty");
			}
			if(kineticREConfiguration.getKineticParametersClasses().isEmpty())
			{
				throw new InvalidConfigurationException("KineticParameters Class Set can not be empty");
			}
		}
		else
			throw new InvalidConfigurationException("configuration must be IREKineticREConfiguration isntance");		
	}

	@Override
	public void stop() {
		this.stop = true;
	}
}
