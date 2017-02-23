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
import com.silicolife.textmining.processes.ie.re.kineticre.core.KineticRE;
import com.silicolife.textmining.processes.ie.re.kineticre.core.KparamValueUnitBasedRelation;
import com.silicolife.textmining.processes.ie.re.kineticre.core.ValueUnitBasedRelation;
import com.silicolife.textmining.processes.nlptools.opennlp.OpenNLP;

/**
 * Main Class for Kinetic RE
 * 
 * @author Hugo Costa
 * @author Ana Al�o Freitas
 *
 */
public class KineticREtriplesANDmetsEnzs implements IREProcess{
	
	public static String kineticREDescrition = "Kinetic RE";
	public final static IProcessOrigin relationProcessType = new ProcessOriginImpl(GenerateRandomId.generateID(),kineticREDescrition);
	private boolean stop = false;
	
	// TEST ERROR
	String fileNameE = "C:\\Users\\anaal\\Desktop\\test_kineticRE\\Error_KineticRE.txt";
	BufferedWriter fileError;
	
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

	// Distancia m�xima permitida entre as entidades (valor-unidade), definida pelo utilizador
	long maxDistBetValueUnit = 3;
	// Distancia m�xima permitida entre um par value-unit e outro par (com "and" no meio), def by user
	long maxDistBetPairsVU = 7;

	private IREKineticREConfiguration configuration;

	public KineticREtriplesANDmetsEnzs() {
		try {
			fileError = new BufferedWriter(new FileWriter(fileNameE));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

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
		IIEProcess reProcess = build(configuration, reConfiguration);
		InitConfiguration.getDataAccess().createIEProcess(reProcess);

		// identificar nºprocesso do NER
		IIEProcess ieProcess = (IIEProcess) configuration.getEntityBasedProcess();

		// criação do relatório; para passar strings de texto, tenho q definir na language e aqui só chamar a chave
		REProcessReportImpl report = new REProcessReportImpl(LanguageProperties.getLanguageStream("pt.uminho.anote2.kineticre.report.title"),ieProcess,reProcess,false);

		// corpus => all docs
		ICorpus corpus = configuration.getCorpus();
		IDocumentSet docs = corpus.getArticlesCorpus();
		
		Iterator<IPublication> itDocs = docs.iterator();
		while(itDocs.hasNext())
		{
			// lista que vai guardar as relações entre as entidades
			List<IEventAnnotation> eventsDoc = new ArrayList<>();
			// to each doc   // actual doc
			IPublication doc = itDocs.next();
			// doc to doc + annotation
			IAnnotatedDocument annotDOcKinetic = new AnnotatedDocumentImpl(doc,reProcess, corpus);
			// all text
			String text = annotDOcKinetic.getDocumentAnnotationText();
			// vai buscar o doc com as anotações do NER
			IAnnotatedDocument annotdocNER = new AnnotatedDocumentImpl(doc,ieProcess, corpus);
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
			for (int i=0; i<sents.size(); i++) // TODO capacidade de relacionar entidades em várias frases
			{
				num_sents++;
				// buscar frase TODO buscar mais frases
				ISentence sent = sents.get(i);				
				try {
					fileError.write("\n" + i + " -> " + sent);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// identificar as entidades presentes na frase
				List<IEntityAnnotation> entSent = getSentenceEntities(entitiesdoc, sent);
//				try {
//					fileError.write("Ents na frase: " + entSent + "\n");
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				
				// Cria listas de kparametros, valores, unidades, enzimas e metabolitos existentes na frase:
				List<IEntityAnnotation> kparamSent = getEntByClass(entSent, this.KINETICPARAMETERS);
				List<IEntityAnnotation> valueSent = getEntByClass(entSent, this.VALUES);
				List<IEntityAnnotation> unitSent = getEntByClass(entSent, this.UNITS);
				List<IEntityAnnotation> enzSent = getEntByClass(entSent, this.ENZYMES);
				List<IEntityAnnotation> metSent = getEntByClass(entSent, this.METABOLITES);

				// Cria lista de pares valor-unidade que existem na frase, a partir das entidade da frase;
				List<ValueUnitBasedRelation> listPairsValueUnit = generateListPairsValueUnit(entSent);
			
				// Cria lista para quardar os Triplos;
				List<KparamValueUnitBasedRelation> listTriples = new ArrayList<>();

				// cria lista com os Triplos na frase
				if (!listPairsValueUnit.isEmpty() && !kparamSent.isEmpty()) {
					// define região possivel à volta de cada par
					defineStarEndRelation(listPairsValueUnit, sent);
					listTriples = getPairsTriples(sent, listPairsValueUnit, kparamSent);
					try {
						//fileError.write("Ents na frase: " + entSent + "\n");
						fileError.write("Ents na frase class Kp: " + kparamSent + "\n");
						fileError.write("Ents na frase class Value: " + valueSent + "\n");
						fileError.write("Ents na frase class Unit: " + unitSent + "\n");
						fileError.write("Ents na frase class Enz: " + enzSent + "\n");
						fileError.write("Ents na frase class Met: " + metSent + "\n");
						fileError.write("Nº Pares: " + listPairsValueUnit.size() + "\n");
						fileError.write("Nº Triplos: " + listTriples.size() + "\n");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
					// cria lista para resultados e vai buscar relacionamentos
					List<IEventAnnotation> results = findrelations(listTriples, sent, enzSent, metSent);
					eventsDoc.addAll(results);
				}
				else {
					System.out.println("Sent without triples -> " + i);
				}
			}
			System.out.println(doc.getId() + "->" + sents.size() + "->" + num_sents);
			//System.out.println(doc.getTitle() + "->" + sents.size() + "->" + num_sents);
			// TODO inserir relações na BD (tratar "results")
			insertAnnotationsInDatabse(reProcess,report,annotDOcKinetic,entitiesdoc,eventsDoc);
		};	
		InitConfiguration.getDataAccess().registerCorpusProcess(corpus, reProcess);
		return report;
	}
	
	private IIEProcess build(IREConfiguration configuration, IREKineticREConfiguration reConfiguration) {
		IIEProcess reProcess = configuration.getIEProcess();
		reProcess.setName(KineticRE.kineticREDescrition+" "+Utils.SimpleDataFormat.format(new Date()));
		return reProcess;
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
	
	// Funcao que define para cada par qual o espaco possivel para a relacao nesse par;
	private void defineStarEndRelation(List<ValueUnitBasedRelation> listPairsValueUnit, ISentence sent) {
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
			for(int j=0; j<kparamSent.size(); j++) {
				if(kparamSent.get(j).getStartOffset() > pair.getStartRelation() && kparamSent.get(j).getEndOffset() < pair.getEndRelation())  {
					KparamValueUnitBasedRelation triple = new KparamValueUnitBasedRelation(pair, kparamSent.get(j));
					score += kParamScore;
					triple.setScore(score);
					listTriplesKpValueUnit.add(triple);
				}
			}
		}
		return listTriplesKpValueUnit;
	}

	// Funcao que procura as relações existentes numa frase (a partir de um Triplo=> pair + kparameter), calcula o score e guarda o relacionamento;
	//// @return Lista de Relacionamentos
	private List<IEventAnnotation> findrelations(List<KparamValueUnitBasedRelation> sentTriples, ISentence sent, 
			List<IEntityAnnotation> enzSent, List<IEntityAnnotation> metSent) {

		List<IEventAnnotation> results = new ArrayList<IEventAnnotation>();
		for(int i=0; i<sentTriples.size(); i++) {	
			float relationScore = sentTriples.get(i).getScore();
			try {
				fileError.write("Valor do TRIPLO: " + relationScore + "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			List<IEntityAnnotation> left= new ArrayList<IEntityAnnotation>();
			List<IEntityAnnotation> right= new ArrayList<IEntityAnnotation>();
			// Add Base Triple Entities (pair) to relation
			left.add(sentTriples.get(i).getPairs().getValue());
			right.add(sentTriples.get(i).getPairs().getUnit());
			// Add Kinetic Parameter to relation
			if(sentTriples.get(i).getKparam().getStartOffset() < sentTriples.get(i).getPairs().getValue().getStartOffset())
			{
				left.add(sentTriples.get(i).getKparam());
			}
			else
			{
				right.add(sentTriples.get(i).getKparam());
			}
			// Get Other Entities (enz and met), at left and at right
			relationScore = getOtherEntitiesAtLeftAndScore(sentTriples, enzSent, metSent, i, relationScore, left, sent);
			relationScore = getOtherEntitiesAtRightAndScore(sentTriples, enzSent, metSent, i, relationScore, right, sent);
			
			try {
				fileError.write("Valor da relacao: " + relationScore + "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			IEventProperties eventProperties = new EventPropertiesImpl();
			eventProperties.setGeneralProperty("score", String.valueOf(relationScore));
			// o HUGO mandou mudar de endRelation para startRelatiion para resolver o problam do verde na visualização
			IEventAnnotation relationTriple = new EventAnnotationImpl(sentTriples.get(i).getPairs().getStartRelation(),
					sentTriples.get(i).getPairs().getStartRelation(), GlobalNames.re, left, right, "",eventProperties,false);
//			// para não imprimir relações só valor-unidade, sem entidades das classes kparam/enz/met;		
//			if(left.size()+right.size()!=2)
//			{
				results.add(relationTriple);	
//			}			
		}
		return results;
	}
	
	private float getOtherEntitiesAtLeftAndScore(List<KparamValueUnitBasedRelation> sentTriples, List<IEntityAnnotation> enzSent,
			List<IEntityAnnotation> metSent, int i, float relationScore, List<IEntityAnnotation> left, ISentence sent) {
		
		for(int j=0; j<enzSent.size(); j++)
		{
			if(enzSent.get(j).getEndOffset() < sentTriples.get(i).getPairs().getValue().getStartOffset() && enzSent.get(j).getStartOffset() > sentTriples.get(i).getPairs().getStartRelation())
			{
				left.add(enzSent.get(j));
				relationScore += enzScore;
			}
		}
		for(int j=0; j<metSent.size(); j++)
		{
			if(metSent.get(j).getEndOffset() < sentTriples.get(i).getPairs().getValue().getStartOffset() && metSent.get(j).getStartOffset() > sentTriples.get(i).getPairs().getStartRelation())
			{
				left.add(metSent.get(j));
				relationScore += metScore;
			}
		}
		return relationScore;
	}

	private float getOtherEntitiesAtRightAndScore(List<KparamValueUnitBasedRelation> sentTriples, List<IEntityAnnotation> enzSent,
			List<IEntityAnnotation> metSent, int i, float relationScore, List<IEntityAnnotation> right, ISentence sent) {
		for(int j=0; j<enzSent.size(); j++) {
			if(enzSent.get(j).getStartOffset() > sentTriples.get(i).getPairs().getUnit().getEndOffset() && enzSent.get(j).getStartOffset() < sentTriples.get(i).getPairs().getEndRelation()) {
				right.add(enzSent.get(j));
				relationScore += enzScore;
			}
		}
		for(int j=0; j<metSent.size(); j++) {
			if(metSent.get(j).getStartOffset() > sentTriples.get(i).getPairs().getUnit().getEndOffset() && metSent.get(j).getStartOffset() < sentTriples.get(i).getPairs().getEndRelation()) {
				right.add(metSent.get(j));
				relationScore += metScore;
			}
		}
		return relationScore;
	}

//	// S� um par a ana garnate que funciona bem
//	private void onlyonepair(
//			ISentence sent,
//			List<IEventAnnotation> results,
//			List<GenericPairImpl<IEntityAnnotation, IEntityAnnotation>> listPairsValueUnit,
//			Map<GenericPairImpl<IEntityAnnotation, IEntityAnnotation>, Integer> pairScore,
//			List<IEntityAnnotation> enzSent, List<IEntityAnnotation> metSent,
//			List<IEntityAnnotation> kparamSent) {
//		System.out.println("Frase com UM so par");
//		long startRelation = sent.getStartOffset();
//		long endRelation = sent.getEndOffset();
//
//		int relationScore = pairScore.get(listPairsValueUnit.get(0));
//
//		List<IEntityAnnotation> left= new ArrayList<IEntityAnnotation>();
//		for(int i=0; i<enzSent.size(); i++) {
//			if(enzSent.get(i).getEndOffset() < listPairsValueUnit.get(0).getX().getStartOffset())
//			{
//				left.add(enzSent.get(i));
//				relationScore += enzScore;
//			}
//		}
//		for(int i=0; i<metSent.size(); i++) {
//			if(metSent.get(i).getEndOffset() < listPairsValueUnit.get(0).getX().getStartOffset()) {
//				left.add(metSent.get(i));
//				relationScore += metScore;
//			}
//		}
//		for(int i=0; i<kparamSent.size(); i++) {
//			if(kparamSent.get(i).getEndOffset() < listPairsValueUnit.get(0).getX().getStartOffset()) {
//				left.add(kparamSent.get(i));
//				relationScore += kParamScore;
//			}
//		}
//		left.add(listPairsValueUnit.get(0).getX());
//		// System.out.println("Valor da relação Esquerda: " + relationScore);
//
//		List<IEntityAnnotation> right= new ArrayList<IEntityAnnotation>();
//		for(int i=0; i<enzSent.size(); i++) {
//			if(enzSent.get(i).getStartOffset() > listPairsValueUnit.get(0).getY().getEndOffset()) {
//				right.add(enzSent.get(i));
//				relationScore += enzScore;
//			}
//		}
//		for(int i=0; i<metSent.size(); i++) {
//			if(metSent.get(i).getStartOffset() > listPairsValueUnit.get(0).getY().getEndOffset())
//			{
//				right.add(metSent.get(i));
//				relationScore += metScore;
//			}
//		}
//		for(int i=0; i<kparamSent.size(); i++)
//		{
//			if(kparamSent.get(i).getStartOffset() > listPairsValueUnit.get(0).getY().getEndOffset())
//			{
//				right.add(kparamSent.get(i));
//				relationScore += kParamScore;
//			}
//		}
//		right.add(listPairsValueUnit.get(0).getY());
//		// System.out.println("Valor da relação Direita: " + relationScore);
//
//		IEventProperties eventProperties = new EventPropertiesImpl();
//		eventProperties.setGeneralProperty("score", String.valueOf(relationScore));
//		// o HUGO mandou mudar de endRelation para startRelation para resolver o problam do verde na visualização
//		IEventAnnotation relationOnePair = new EventAnnotationImpl(startRelation, startRelation,
//				GlobalNames.re, left, right, "", 0, "", eventProperties);
//		// para não imprimir relações só valor-unidade, sem entidades das classes kparam/enz/met;
//		if(left.size()+right.size()!=2)
//		{
//			results.add(relationOnePair);	
//		}
//	}

	// Função que buscar entidades de uma frase. (@return Lista das Entidades numa frase)
	protected List<IEntityAnnotation> getSentenceEntities(List<IEntityAnnotation> listEntitiesSortedByOffset, ISentence sentence) {
		List<IEntityAnnotation> result = new ArrayList<IEntityAnnotation>();
		for(IEntityAnnotation ent:listEntitiesSortedByOffset)
		{
			if(ent.getStartOffset()>=sentence.getStartOffset() && ent.getStartOffset()<sentence.getEndOffset())
			{
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
			if (defClass.contains(entSent.get(i).getClassAnnotation().getId())){
				IEntityAnnotation ent = entSent.get(i);
				entClassSent.add(ent);
			}
		}
		return entClassSent;	
	}

	// Método que retorna se existem entidades (met, enz ou kparam) atrás ou à frente de um par
	private List<IEntityAnnotation> getEntBefPairAfter(int startRegion, int endRegion, List<IEntityAnnotation> entSentByClass)
	{
		// cria lista
		List<IEntityAnnotation> entSentRegion = new ArrayList<IEntityAnnotation>();

		return entSentRegion;
	}

	@Override
	public void validateConfiguration(IREConfiguration configuration)throws InvalidConfigurationException {
		if(configuration instanceof IREKineticREConfiguration)
		{
			IREKineticREConfiguration lexicalResurcesConfiguration = (IREKineticREConfiguration) configuration;
			if(lexicalResurcesConfiguration.getCorpus()==null)
			{
				throw new InvalidConfigurationException("Corpus can not be null");
			}
		}
		else
			throw new InvalidConfigurationException("configuration must be IRECooccurrenceConfiguration isntance");		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}


}
