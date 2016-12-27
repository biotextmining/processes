package com.silicolife.textmining.processes.ie.re.kineticre.core.oldversions;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.silicolife.textmining.core.datastructures.annotation.re.EventAnnotationImpl;
import com.silicolife.textmining.core.datastructures.annotation.re.EventPropertiesImpl;
import com.silicolife.textmining.core.datastructures.documents.AnnotatedDocumentImpl;
import com.silicolife.textmining.core.datastructures.exceptions.process.InvalidConfigurationException;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.language.LanguageProperties;
import com.silicolife.textmining.core.datastructures.process.IEProcessImpl;
import com.silicolife.textmining.core.datastructures.process.ProcessOriginImpl;
import com.silicolife.textmining.core.datastructures.process.ProcessTypeImpl;
import com.silicolife.textmining.core.datastructures.report.processes.REProcessReportImpl;
import com.silicolife.textmining.core.datastructures.utils.GenerateRandomId;
import com.silicolife.textmining.core.datastructures.utils.Utils;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalNames;
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
import com.silicolife.textmining.processes.ie.re.kineticre.core.EntityAnnotationListSort;
import com.silicolife.textmining.processes.ie.re.kineticre.core.KparamValueUnitBasedRelation;
import com.silicolife.textmining.processes.ie.re.kineticre.core.KparamValueUnitSimpleOrComplex;
import com.silicolife.textmining.processes.ie.re.kineticre.core.ValueUnitBasedRelation;
import com.silicolife.textmining.processes.ie.re.kineticre.core.ValueUnitSimpleOrComplex;
import com.silicolife.textmining.processes.nlptools.opennlp.OpenNLP;

/**
 * Main Class for Kinetic RE
 * 
 * @author Hugo Costa
 * @author Ana Alão Freitas
 *
 */
public class KineticREtriples implements IREProcess {
	
	public static String kineticREDescrition = "Kinetic RE";
	public final static IProcessOrigin relationProcessType = new ProcessOriginImpl(GenerateRandomId.generateID(),kineticREDescrition);
	private boolean stop = false;
	
	// TEST ERROR
	String fileNameE = "C:\\Users\\anaal\\Desktop\\ERROR_KineticRE_exp Km 46 pdfs_semRegras";
	BufferedWriter fileError;
	
	// Distancia m�xima permitida entre as entidades (valor-unidade), definida pelo utilizador
	private static long maxDistBetValueUnit = 3;
	// Distancia m�xima permitida entre 2 pares com um AND no meio, definida pelo utilizador
	private static long maxDistBetVUandVU = 30;

	// Criação das classes que existem neste RE
	private Set<Long> UNITS;
	private Set<Long> VALUES;
	private Set<Long> KINETICPARAMETERS;
	private Set<Long> METABOLITES;
	private Set<Long> ENZYMES;
	private Set<Long> ORGANISM;

	// Scores das diferentes Classes, definidas pelo utilizador
	private static int unitScore = 5;
	private static int valScore = 5;
	private static int kParamScore = 10000;
	private static int enzScore = 1000;
	private static int metScore = 100;
	private static int orgScore = 0; 
	
	private IREKineticREConfiguration configuration;

	private void configureKineticREClasses(IREKineticREConfiguration configuration) {
		this.UNITS = new HashSet<>();
		for(IAnoteClass klass:configuration.getUnitsClasses()) {
			this.UNITS.add(klass.getId()); 
		}	
		this.VALUES = new HashSet<>();
		for(IAnoteClass klass:configuration.getValuesClasses()) {
			this.VALUES.add(klass.getId());
		}
		this.KINETICPARAMETERS = new HashSet<>();
		for(IAnoteClass klass:configuration.getKineticParametersClasses()) {
			this.KINETICPARAMETERS.add(klass.getId());
		}
		this.METABOLITES = new HashSet<>();
		for(IAnoteClass klass:configuration.getMetabolitesClasses()) {
			this.METABOLITES.add(klass.getId());
		}
		this.ENZYMES = new HashSet<>();
		for(IAnoteClass klass:configuration.getEnzymesClasses()) {
			this.ENZYMES.add(klass.getId());
		}
		this.ORGANISM = new HashSet<>();
		for(IAnoteClass klass:configuration.getOrganismClasses()) {
			this.ORGANISM.add(klass.getId());
		}
	}

	@Override
	public IREProcessReport executeRE(IREConfiguration configuration) throws ANoteException,InvalidConfigurationException {
		validateConfiguration(configuration);
		IREKineticREConfiguration reConfiguration = (IREKineticREConfiguration) configuration;
		configureKineticREClasses(reConfiguration);
		IIEProcess reProcess = new IEProcessImpl(configuration.getCorpus(), kineticREDescrition+" "+Utils.SimpleDataFormat.format(new Date()),
				configuration.getProcessNotes(), ProcessTypeImpl.getREProcessType(), relationProcessType, configuration.getProperties());
		InitConfiguration.getDataAccess().createIEProcess(reProcess);

		try {
			fileError = new BufferedWriter(new FileWriter(fileNameE));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		// identificar nºprocesso do NER
		IIEProcess ieProcess = (IIEProcess) configuration.getEntityBasedProcess();

		// criação do relatório; para passar strings de texto, tenho q definir na language e aqui só chamar a chave
		REProcessReportImpl report = new REProcessReportImpl(LanguageProperties.getLanguageStream("pt.uminho.anote2.kineticre.report.title"),ieProcess,reProcess,false);

		// corpus => all docs
		ICorpus corpus = configuration.getCorpus();
		IDocumentSet docs = corpus.getArticlesCorpus();
		
		Iterator<IPublication> itDocs = docs.iterator();
		while(itDocs.hasNext()) {
			// lista que vai guardar as relações entre as entidades
			List<IEventAnnotation> eventsDoc = new ArrayList<>();
			// to each doc   // actual doc
			IPublication doc = itDocs.next();
			// doc to doc + annotation
			IAnnotatedDocument annotDOcKinetic = new AnnotatedDocumentImpl(doc, reProcess, corpus);
			// all text
			String text = annotDOcKinetic.getDocumentAnnotationText();
			// vai buscar o doc com as anotações do NER
			IAnnotatedDocument annotdocNER = new AnnotatedDocumentImpl(doc, ieProcess, corpus);
			// lista de entidades para este doc
			List<IEntityAnnotation> entitiesdoc = annotdocNER.getEntitiesAnnotations();
			// garantir que as entidades do doc est�o ordenadas
			Collections.sort(entitiesdoc, new EntityAnnotationListSort());

			// percorrer o texto e identifica frases  // guardar todas as frases
			List<ISentence> sents;
			try {
				sents = OpenNLP.getInstance().getSentencesText(text);
			} catch (IOException e) {
				throw new ANoteException(e);
			}
						
			// para cada frase
			int num_sents = 0;
			for (int i=0; i<sents.size(); i++) {
				num_sents++;
				// buscar frase TODO buscar mais frases
				ISentence sent = sents.get(i);				

				// identificar as entidades presentes na frase
				List<IEntityAnnotation> entSent = getSentenceEntities(entitiesdoc, sent);
				
				// Cria listas de kparametros, valores, unidades, enzimas e metabolitos existentes na frase:
				List<IEntityAnnotation> kparamSent = getEntByClass(entSent, this.KINETICPARAMETERS);
				List<IEntityAnnotation> valueSent = getEntByClass(entSent, this.VALUES);
				List<IEntityAnnotation> unitSent = getEntByClass(entSent, this.UNITS);

				// Cria lista de pares valor-unidade que existem na frase, a partir das entidade da frase;
				List<ValueUnitBasedRelation> listPairsValueUnit = generateListPairsValueUnit(entSent);
				
				// Complex - special pairs
				List<ValueUnitSimpleOrComplex> valueUnitSimpleOrComplexList = generateListPairsValueUnitSimpleOrComplex(sent,listPairsValueUnit, valueSent);
				
				// Cria lista para quardar os Triplos;
				List<KparamValueUnitBasedRelation> listTriples = new ArrayList<>();
				List<KparamValueUnitSimpleOrComplex> listTriplesSorC = new ArrayList<>();
				
				// cria lista com os Triplos na frase, mas só se existirem pares E parametros cinéticos anotados;
				//if (!listPairsValueUnit.isEmpty() && !kparamSent.isEmpty()) {
				if (!valueUnitSimpleOrComplexList.isEmpty() && !kparamSent.isEmpty()) {
					// define região possivel à volta de cada par
					//defineStarEndRelationPairSimple(listPairsValueUnit, sent);
					defineStarEndRelationPairsSimplexComplex(valueUnitSimpleOrComplexList, sent);
					//listTriples = getPairsTriples(sent, listPairsValueUnit, kparamSent);
					listTriplesSorC = getSimplexComplexTriples(sent, valueUnitSimpleOrComplexList, kparamSent);
					try {
						fileError.write("\nDOC:\t" + doc + "\n" + i + " -> " + sent);
						fileError.write("Ents na frase: " + entSent + "\n");
						fileError.write("Ents na frase class Kp: " + kparamSent + "\n");
						fileError.write("Ents na frase class Value: " + valueSent + "\n");
						fileError.write("Ents na frase class Unit: " + unitSent + "\n");
						fileError.write("Nº Pares: " + valueUnitSimpleOrComplexList.size() + "\n");
						fileError.write("Nº Triplos: " + listTriplesSorC.size() + "\n");
					} catch (IOException e) {
						e.printStackTrace();
					} 
					// cria lista para resultados e vai buscar relacionamentos
					//List<IEventAnnotation> results = findrelations(listTriples, sent);
					List<IEventAnnotation> results = findrelationsTriples(listTriplesSorC, sent);
					eventsDoc.addAll(results);
				}
//				else {
//					System.out.println("Sent without triples (pairs or Kparam)-> " + i);
//				}
			}
			try {
				fileError.write(doc.getId() + "->" + sents.size() + "->" + num_sents);
				fileError.write(doc.getTitle() + "->" + sents.size() + "->" + num_sents);
			} catch (IOException e) {
				e.printStackTrace();
			}
			insertAnnotationsInDatabse(reProcess,report,annotDOcKinetic,entitiesdoc,eventsDoc);
		};	
		InitConfiguration.getDataAccess().registerCorpusProcess(corpus, reProcess);
		try {
			fileError.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return report;
	}
	
	private List<ValueUnitSimpleOrComplex> generateListPairsValueUnitSimpleOrComplex(ISentence sent,
			List<ValueUnitBasedRelation> listPairsValueUnit, List<IEntityAnnotation> valuesSent) {
		
		List<ValueUnitSimpleOrComplex> valueUnitSimpleOrComplexList = new ArrayList<>();
		Set<ValueUnitBasedRelation> alreadyUsed = new HashSet<>();
		
		// Case 1
		for(int i=1;i<listPairsValueUnit.size();i++) {
			ValueUnitBasedRelation before = listPairsValueUnit.get(i-1);
			ValueUnitBasedRelation now = listPairsValueUnit.get(i);
			int start = (int) (before.getUnit().getEndOffset() -  sent.getStartOffset());
			int end = (int) (now.getValue().getStartOffset() -  sent.getStartOffset());
			String sentence = sent.getText();
			if(end-start < maxDistBetVUandVU) {
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
				if(end-start < maxDistBetVUandVU) {
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
		
		try {
			fileError.write("TEST listas: \nlistPairsValueUnit-> " + listPairsValueUnit.size() + "\nalreadyUsed-> " + alreadyUsed.size() + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return valueUnitSimpleOrComplexList;

	}

	private void insertAnnotationsInDatabse(IIEProcess process,IREProcessReport report,IAnnotatedDocument annotDoc,List<IEntityAnnotation> entitiesList,List<IEventAnnotation> relationsList) throws ANoteException {
		// Generate new Ids for Entities
		for(IEntityAnnotation entity:entitiesList) {
			entity.generateNewId();
		}
		InitConfiguration.getDataAccess().addProcessDocumentEntitiesAnnotations(process, annotDoc, entitiesList);
		report.incrementEntitiesAnnotated(entitiesList.size());
		InitConfiguration.getDataAccess().addProcessDocumentEventAnnoations(process, annotDoc,relationsList);
		report.increaseRelations(relationsList.size());
	}
	
	// Funcao que procura pares na frase (value-unit), a partir da lista de entidades existentes na frase;
	//// @return Lista de Pares
	private List<ValueUnitBasedRelation> generateListPairsValueUnit(List<IEntityAnnotation> entSent) {
		// criação da lista para os pares
		List<ValueUnitBasedRelation> listPairsValueUnit = new ArrayList<>();
		for (int i=0; i<entSent.size()-1; i++) {
			if (VALUES.contains(entSent.get(i).getClassAnnotation().getId()) &&
					UNITS.contains(entSent.get(i+1).getClassAnnotation().getId()) &&
					entSent.get(i+1).getStartOffset() < (entSent.get(i).getEndOffset() + maxDistBetValueUnit)) {
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
				if(kparamSent.get(j).getStartOffset() > simpleORcomplexPair.getStartRelation() && kparamSent.get(j).getEndOffset() < simpleORcomplexPair.getEndRelation())  {
					if(kparamSent.get(j).getEndOffset() < simpleORcomplexPair.getStartIndex()) {
						hasKparamAtleft = true;
						tripleToAdd = new KparamValueUnitSimpleOrComplex(simpleORcomplexPair, kparamSent.get(j));
						score += kParamScore;
						tripleToAdd.setScore(score);
					}
					else if(kparamSent.get(j).getStartOffset() > simpleORcomplexPair.getEndIndex() && hasKparamAtleft) {
						break;
					}
					else {
						tripleToAdd = new KparamValueUnitSimpleOrComplex(simpleORcomplexPair, kparamSent.get(j));
						score += kParamScore;
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
	private List<IEventAnnotation> findrelationsTriples(List<KparamValueUnitSimpleOrComplex> sentTriples, ISentence sent) {
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
				if(tripleSC.getKparam().getStartOffset() < simpleComplexPair.getValue().getStartOffset()){
					left.add(tripleSC.getKparam());
				}
				else {
					right.add(tripleSC.getKparam());
				}

				IEventProperties eventProperties = new EventPropertiesImpl();
				eventProperties.setGeneralProperty("score", String.valueOf(relationScore));
				IEventAnnotation relationTriple = new EventAnnotationImpl(simpleComplexPair.getStartRelation(), simpleComplexPair.getEndRelation(), GlobalNames.re, left, right, "", 0, "", eventProperties);
			
				results.add(relationTriple);
			}
		}
		return results;
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
	private List<IEntityAnnotation> getEntByClass(List<IEntityAnnotation> entSent, Set<Long> defClass) {
		// cria lista
		List<IEntityAnnotation> entClassSent = new ArrayList<IEntityAnnotation>();
		for (int i=0; i<entSent.size(); i++){
			if (defClass.contains(entSent.get(i).getClassAnnotation().getId())) {
				IEntityAnnotation ent = entSent.get(i);
				entClassSent.add(ent);
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
			IREKineticREConfiguration lexicalResurcesConfiguration = (IREKineticREConfiguration) configuration;
			if(lexicalResurcesConfiguration.getCorpus()==null) {
				throw new InvalidConfigurationException("Corpus can not be null");
			}
		}
		else
			throw new InvalidConfigurationException("configuration must be IRECooccurrenceConfiguration isntance");		
	}

	@Override
	public void stop() {
	
	}

	/////////////////////////
	// Código antigo, funcionava para Lista de pares simples
	////////////////////////
	
	// Funcao que define para cada par DA LISTA DE SIMPLES qual o espaco possivel para a relacao nesse par;
	private void defineStarEndRelationPairSimple(List<ValueUnitBasedRelation> listPairsValueUnit, ISentence sent) {
		// se so existir 1 par
		if(listPairsValueUnit.size() == 1) {
			listPairsValueUnit.get(0).setStartRelation(sent.getStartOffset());
			listPairsValueUnit.get(0).setEndRelation(sent.getEndOffset());
		}
		// se existirem 2 pares
		else if(listPairsValueUnit.size() == 2) {
			listPairsValueUnit.get(0).setStartRelation(sent.getStartOffset());
			listPairsValueUnit.get(0).setEndRelation(listPairsValueUnit.get(1).getValue().getStartOffset() - 1);
			listPairsValueUnit.get(1).setStartRelation(listPairsValueUnit.get(0).getUnit().getEndOffset() + 1);
			listPairsValueUnit.get(1).setEndRelation(sent.getEndOffset());
		}
		// se existirem mais de 2 pares;
		else {
			for(int i=0; i<listPairsValueUnit.size(); i++) {
				if(i == 0) {
					listPairsValueUnit.get(i).setStartRelation(sent.getStartOffset());
					listPairsValueUnit.get(i).setEndRelation(listPairsValueUnit.get(i+1).getValue().getStartOffset() - 1);
				}
				else if(i == listPairsValueUnit.size()-1) {
					listPairsValueUnit.get(i).setStartRelation(listPairsValueUnit.get(i-1).getUnit().getEndOffset() + 1);
					listPairsValueUnit.get(i).setEndRelation(sent.getEndOffset());
				}
				else {
					listPairsValueUnit.get(i).setStartRelation(listPairsValueUnit.get(i-1).getUnit().getEndOffset() + 1);
					listPairsValueUnit.get(i).setEndRelation(listPairsValueUnit.get(i+1).getValue().getStartOffset() - 1);
				}
			}
		}
	}
	
	// Funcao que retorna os Triplos (value-unit + kparam) existentes na frase, a partir da lista de Pares;
	//// @return Lista de Triplos
	private List<KparamValueUnitBasedRelation> getPairsTriples(ISentence sent, List<ValueUnitBasedRelation> listPairsValueUnit, List<IEntityAnnotation> kparamSent) {
		List<KparamValueUnitBasedRelation> listTriplesKpValueUnit = new ArrayList<>();
		for(ValueUnitBasedRelation pair : listPairsValueUnit) {
			float score = pair.getScore();
			boolean hasKparamAtleft = false;
			KparamValueUnitBasedRelation tripleToAdd = null;
			for(int j=0; j<kparamSent.size(); j++) {
				if(kparamSent.get(j).getStartOffset() > pair.getStartRelation() && kparamSent.get(j).getEndOffset() < pair.getEndRelation())  {
					if(kparamSent.get(j).getEndOffset() < pair.getValue().getStartOffset()) {
						hasKparamAtleft = true;
						tripleToAdd = new KparamValueUnitBasedRelation(pair, kparamSent.get(j));
						score += kParamScore;
						tripleToAdd.setScore(score);
					}
					else if(kparamSent.get(j).getStartOffset() > pair.getUnit().getStartOffset() && hasKparamAtleft) {
						break;
					}
					else {
						tripleToAdd = new KparamValueUnitBasedRelation(pair, kparamSent.get(j));
						score += kParamScore;
						tripleToAdd.setScore(score);
						break;
					}
				}
			}
			if(tripleToAdd!=null)
				listTriplesKpValueUnit.add(tripleToAdd);
		}
		return listTriplesKpValueUnit;
	}

	// Funcao que procura as relações existentes numa frase (a partir de um Triplo=> pair + kparameter), calcula o score e guarda o relacionamento;
	//// @return Lista de Relacionamentos
	private List<IEventAnnotation> findrelations(List<KparamValueUnitBasedRelation> sentTriples, ISentence sent) {
		List<IEventAnnotation> results = new ArrayList<IEventAnnotation>();
		
		for(int i=0; i<sentTriples.size(); i++) {	
			float relationScore = sentTriples.get(i).getScore();
			try {
				fileError.write("Valor do TRIPLO: " + relationScore + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			List<IEntityAnnotation> left= new ArrayList<IEntityAnnotation>();
			List<IEntityAnnotation> right= new ArrayList<IEntityAnnotation>();
			
			// Add Base Triple Entities (pair) to relation
			///////TEST left.add(sentTriples.get(i).getPairs().getValue());
			
			right.add(sentTriples.get(i).getPairs().getUnit());
			// Add Kinetic Parameter to relation
			if(sentTriples.get(i).getKparam().getStartOffset() < sentTriples.get(i).getPairs().getValue().getStartOffset()) {
				left.add(sentTriples.get(i).getKparam());
			}
			else {
				right.add(sentTriples.get(i).getKparam());
			}
			left.add(sentTriples.get(i).getPairs().getValue());
			
			IEventProperties eventProperties = new EventPropertiesImpl();
			eventProperties.setGeneralProperty("score", String.valueOf(relationScore));
			// o HUGO mandou mudar de endRelation para startRelatiion para resolver o problam do verde na visualização
			IEventAnnotation relationTriple = new EventAnnotationImpl(sentTriples.get(i).getPairs().getStartRelation(),
					sentTriples.get(i).getPairs().getStartRelation(), GlobalNames.re, left, right, "", 0, "", eventProperties);
			try {
				fileError.write("Results: " + left + "<->" + right + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
			results.add(relationTriple);				
		}
		return results;
	}
}
