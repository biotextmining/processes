package com.silicolife.textmining.processes.ie.re.kineticre.core.oldversions;


/**
 * Main Class for Kinetic RE
 * 
 * @author Hugo Costa
 * @author Ana Alão Freitas
 *
 */
//public class KineticREFirstVersion extends IEProcessImpl implements IREProcess{
//
//	private IREProcessReport report;
//	private IREKineticREConfiguration configuration;
//	
//	public static String kineticREDescrition = "Kinetic RE";
//	public final static IProcessOrigin relationProcessType = new ProcessOriginImpl(GenerateRandomId.generateID(),kineticREDescrition);
//	private boolean stop = false;
//
//	// Criação das classes que existem neste RE
//	private Set<Long> UNITS;
//	private Set<Long> VALUES;
//	private Set<Long> KINETICPARAMETERS;
//	private Set<Long> METABOLITES;
//	private Set<Long> ENZYMES;
//	private Set<Long> ORGANISM;
//
//	// Scores das diferentes Classes, definidas pelo utilizador
//	private static int unitScore = 5;
//	private static int valScore = 5;
//	private static int kParamScore = 10000;
//	private static int metScore = 100;
//	private static int enzScore = 1000;
//	private static int orgScore = 0; 
//
//	// Distancia máxima permitida entre as entidades (valor-unidade), definida pelo utilizador
//	long maxDistBetValueUnit = 3;
//	// Distancia máxima permitida entre um par value-unit e outro par (com "and" no meio), def by user
//	long maxDistBetPairsVU = 7;
//
//	public KineticREFirstVersion(IREKineticREConfiguration configuration) {
//		super(configuration.getCorpus(), kineticREDescrition+" "+Utils.SimpleDataFormat.format(new Date()), configuration.getNotes(),
//				ProcessTypeImpl.getREProcessType(),relationProcessType, configuration.getProperties());
//				
//		this.configuration = configuration;
//		// Mapeamento feito pelo utilizador: entre as classes usadas no NER e as que precisa pra o RE
//		this.UNITS = new HashSet<>();
//		this.UNITS.addAll(configuration.getUnitsClasses());
//
//		this.VALUES = new HashSet<>();
//		this.VALUES.addAll(configuration.getValuesClasses());
//
//		this.KINETICPARAMETERS = new HashSet<>();
//		this.KINETICPARAMETERS.addAll(configuration.getKineticParametersClasses());
//		
//		this.METABOLITES = new HashSet<>();
//		this.METABOLITES.addAll(configuration.getMetabolitesClasses());
//		
//		this.ENZYMES = new HashSet<>();
//		this.ENZYMES.addAll(configuration.getEnzymesClasses());
//
//		this.ORGANISM = new HashSet<>();
//		this.ORGANISM.addAll(configuration.getOrganismClasses());
//	}
//
//	@Override
//	public IREProcessReport executeRE() throws ANoteException {
//		// identificar nºprocesso do NER
//		IIEProcess ieProcess = (IIEProcess) configuration.getIEProcess();
//
//		// criação do relatório
//		// para passar strings de texto, tenho q definir na language e aqui só chamar a chave
//		report = new REProcessReportImpl(LanguageProperties.getLanguageStream("pt.uminho.anote2.kineticre.report.title"),ieProcess,this,false);
//
//		// corpus => all docs
//		IDocumentSet docs = getCorpus().getArticlesCorpus();
//
//		Iterator<IPublication> itDocs = docs.iterator();
//		
//		while(itDocs.hasNext()) {	
//			// lista que vai guardar as relações entre as entidades
//			List<IEventAnnotation> eventsDoc = new ArrayList<>();
//			// to each doc   // actual doc
//			IPublication doc = itDocs.next();
//			// doc to doc + annotation
//			IAnnotatedDocument annotDOcKinetic = new AnnotatedDocumentImpl(doc, this, getCorpus());
//			// all text
//			String text = annotDOcKinetic.getDocumentAnnotationText();
//
//			// vai buscar o doc com as anotações do NER
//			IAnnotatedDocument annotdocNER = new AnnotatedDocumentImpl(doc, ieProcess, getCorpus());
//			// lista de entidades para este doc
//			List<IEntityAnnotation> entitiesdoc = annotdocNER.getEntitiesAnnotations();
//
//			// percorrer o texto e identifica frases  // guardar todas as frases
//			List<ISentence> sents;
//			try {
//				sents = OpenNLP.getInstance().getSentencesText(text);
//			} catch (IOException e) {
//				throw new ANoteException(e);
//			}
//			
//			// para cada frase
//			int num_sents = 0;
//			for (int i=0; i<sents.size(); i++) // TODO capacidade de relacionar entidades em várias frases
//			{
//				num_sents++;
//				// buscar frase TODO buscar mais frases
//				ISentence sent = sents.get(i);
////				System.out.println(i);
////				System.out.println(sent);
//
//				// determinar o tamanho da frase (para adicionar ao score mais tarde TODO)
//				//long sizeSent = (sent.getEndOffset() - sent.getStartOffset()) + 1;
//
//				// identificar as entidades presentes na frase TODO buscar entidades das outras frases
//				List<IEntityAnnotation> entSent = getSentenceEntities(entitiesdoc, sent);
////				System.out.println(entSent);
//
//				// buscar relacionamentos
//				List<IEventAnnotation> sentEvents = geteventSent(sent, entSent);
////				System.out.println("sentEvents:" + sentEvents);
////				System.out.println("");
//				eventsDoc.addAll(sentEvents);
//
//				// TODO adicionar à lista geral de relacionamentos
//			}
//			System.out.println(annotDOcKinetic.getId() + "->" + sents.size() + "->" + num_sents);
//			System.out.println(doc.getId() + "->" + sents.size() + "->" + num_sents);
//			System.out.println(doc.getTitle() + "->" + sents.size() + "->" + num_sents);
//			// TODO inserir relações na BD (tratar "results")
//			insertAnnotationsInDatabse(report, annotDOcKinetic, entitiesdoc, eventsDoc);
//		};		
//		return report;
//	}
//	
//	private void insertAnnotationsInDatabse(IREProcessReport report,IAnnotatedDocument annotDoc,List<IEntityAnnotation> entitiesList,List<IEventAnnotation> relationsList) throws ANoteException {
//		// Generate new Ids for Entities
//		for(IEntityAnnotation entity:entitiesList)
//		{
//			entity.generateNewId();
//		}
//		Configuration.getDataAccess().addProcessDocumentEntitiesAnnotations(this, annotDoc, entitiesList);
//		report.incrementEntitiesAnnotated(entitiesList.size());
//		Configuration.getDataAccess().addProcessDocumentEventAnnoations(this, annotDoc,relationsList);
//		report.increaseRelations(relationsList.size());
//	}
//
//	/*
//	 * Função que retorna os relacionamentos existentes.
//	 *  TODO 1. verificar existência de relacionamento numa frase + Score + guardar relacionamento
//	 *  TODO 2. verificar existência de vários relacionamento na mesma frase + Scores + guardar relacionamentos
//	 *  @return Lista de Relacionamentos
//	 */
//	private List<IEventAnnotation> geteventSent(ISentence sent, List<IEntityAnnotation> entSent) {
//		// cria lista
//		List<IEventAnnotation> results = new ArrayList<IEventAnnotation>();
//
//		// TODO percorrer entidades
//		// TODO verificar relaciomento TODO calcular score TODO guardar relacioamento
//		// criação da lista para os pares
//		List<GenericPairImpl<IEntityAnnotation, IEntityAnnotation> > listPairsValueUnit = new ArrayList<>();
//		// criação do Score da Relação associada a cada par
//		Map<GenericPairImpl<IEntityAnnotation, IEntityAnnotation>, Integer> pairScore = new HashMap<> ();
//		for (int i=0; i<entSent.size()-1; i++)
//		{
//			// procura na frase os pares: valor, unidade
//			//			System.out.println("Procura de pares: " + i + " " + entSent.get(i).getClassAnnotationID() + " " + entSent.get(i+1).getClassAnnotationID());
//			//			System.out.println("Procura de pares: " + entSent.get(i+1).getStartOffset() + " " + entSent.get(i).getEndOffset());
//			if (VALUES.contains(entSent.get(i).getClassAnnotation()) &&
//					UNITS.contains(entSent.get(i+1).getClassAnnotation()) &&
//					entSent.get(i+1).getStartOffset() < (entSent.get(i).getEndOffset() + maxDistBetValueUnit))
//			{
//				//				System.out.println("FIZ O IF");
//				IEntityAnnotation entValue = entSent.get(i);
//				IEntityAnnotation entUnit = entSent.get(i+1);
//				//				System.out.println("entValue entUnit: " + entValue + " " + entUnit);
//				GenericPairImpl<IEntityAnnotation, IEntityAnnotation> pair = new GenericPairImpl<>(entValue, entUnit);
//				listPairsValueUnit.add(pair);
//				pairScore.put(pair, unitScore + valScore);
//				i++;	
//			}
//		}
////		System.out.println("lista Pares: " + listPairsValueUnit);
//
//		// Relacoes simples (só par valor-unidade)
//		//		for(GenericPair<IEntityAnnotation, IEntityAnnotation> pair:listPairsValueUnit)
//		//		{
//		//			System.out.println("RELAÇÕES SIMPLES: ");
//		//			System.out.println("PAR: " + listPairsValueUnit.toString());
//		//			long startRelation = pair.getX().getStartOffset();
//		//			long endRelation = pair.getY().getEndOffset();
//		//			
//		//			List<IEntityAnnotation> right = new ArrayList<IEntityAnnotation>();
//		//			right.add(pair.getY()); 
//		//			List<IEntityAnnotation> left= new ArrayList<IEntityAnnotation>();
//		//			left.add(pair.getX());
//		//			
//		//			IEventProperties eventProperties = new EventProperties();
//		//			
//		//			int relationScore = pairScore.get(listPairsValueUnit.get(0));
//		//			eventProperties.addProperty("score", String.valueOf(relationScore));
//		//			// o HUGO mandou mudar de endRelation para startRelation para resolver o problam do verde na visualização
//		//			IEventAnnotation relation = new EventAnnotation(-1, startRelation, startRelation,
//		//					GlobalNames.re, left, right, "", 0, "", eventProperties );
//		//
//		//			results.add(relation);
//		//		}
//
//		//		// Cria listas de enzima, metabolitos e Kparametros na frase
//		List<IEntityAnnotation> enzSent = getEntByClass(entSent, this.ENZYMES);
//		List<IEntityAnnotation> metSent = getEntByClass(entSent, this.METABOLITES);
//		List<IEntityAnnotation> kparamSent = getEntByClass(entSent, this.KINETICPARAMETERS);
//		if(listPairsValueUnit.isEmpty())
//		{
//			// Do nothing
//		}
//		// Relações Complexas (par (valor_unidades) + entidades de outras classes)		
//		////se a frase só tiver um par: valor-unidade:
//		else if(listPairsValueUnit.size() == 1)
//		{
//			onlyonepair(sent, results, listPairsValueUnit, pairScore, enzSent,metSent, kparamSent);
//		}
//		//// se a frase tiver mais que um par
//		else
//		{
//			// Relacoes de dois pares seguidos com "and", só 2 pares numa frase;
//			if(listPairsValueUnit.size() == 2 && (listPairsValueUnit.get(1).getX().getStartOffset() < (listPairsValueUnit.get(0).getY().getEndOffset() + maxDistBetPairsVU)))
//			{
////				System.out.println("RELAÇÕES COMPLEXAS: frase com mais que 1 par -> 2 pares e tem um AND");
//				long startRelation = sent.getStartOffset();
//				long endRelation = sent.getEndOffset();
//
//				int relationScore = pairScore.get(listPairsValueUnit.get(0)) + pairScore.get(listPairsValueUnit.get(1));
////				System.out.println("Valor do PAR: " + relationScore);
//
//				List<IEntityAnnotation> left= new ArrayList<IEntityAnnotation>();
//				for(int i=0; i<enzSent.size(); i++)
//				{
//					if(enzSent.get(i).getEndOffset() < listPairsValueUnit.get(0).getX().getStartOffset())
//					{
//						left.add(enzSent.get(i));
//						relationScore += enzScore;
//					}
//				}
//				for(int i=0; i<metSent.size(); i++)
//				{
//					if(metSent.get(i).getEndOffset() < listPairsValueUnit.get(0).getX().getStartOffset())
//					{
//						left.add(metSent.get(i));
//						relationScore += metScore;
//					}
//				}
//				for(int i=0; i<kparamSent.size(); i++)
//				{
//					if(kparamSent.get(i).getEndOffset() < listPairsValueUnit.get(0).getX().getStartOffset())
//					{
//						left.add(kparamSent.get(i));
//						relationScore += kParamScore;
//					}
//				}
//				left.add(listPairsValueUnit.get(0).getX());
//				left.add(listPairsValueUnit.get(0).getY());
////				System.out.println("Valor da relação Esquerda: " + relationScore);
//
//				List<IEntityAnnotation> right= new ArrayList<IEntityAnnotation>();
//				for(int i=0; i<enzSent.size(); i++)
//				{
//					if(enzSent.get(i).getStartOffset() > listPairsValueUnit.get(0).getY().getEndOffset())
//					{
//						right.add(enzSent.get(i));
//						relationScore += enzScore;
//					}
//				}
//				for(int i=0; i<metSent.size(); i++)
//				{
//					if(metSent.get(i).getStartOffset() > listPairsValueUnit.get(0).getY().getEndOffset())
//					{
//						right.add(metSent.get(i));
//						relationScore += metScore;
//					}
//				}
//				for(int i=0; i<kparamSent.size(); i++)
//				{
//					if(kparamSent.get(i).getStartOffset() > listPairsValueUnit.get(0).getY().getEndOffset())
//					{
//						right.add(kparamSent.get(i));
//						relationScore += kParamScore;
//					}
//				}
//				right.add(listPairsValueUnit.get(1).getY());
//				right.add(listPairsValueUnit.get(1).getX());
////				System.out.println("Valor da relação Direita: " + relationScore);
//
//				IEventProperties eventProperties = new EventPropertiesImpl();
//				eventProperties.setGeneralProperty("score", String.valueOf(relationScore));
//				// o HUGO mandou mudar de endRelation para startRelation para resolver o problam do verde na visualização
//				IEventAnnotation relationTwoPairsAnd = new EventAnnotationImpl(startRelation, startRelation, GlobalNames.re, left, right,
//						"", 0, "", eventProperties);
//				// para não imprimir relações só valor-unidade, sem entidades das classes kparam/enz/met;
//				if(left.size()+right.size()!=2)
//				{
//					results.add(relationTwoPairsAnd);	
//				}
//			}
//			// Relacoes de dois pares seguidos com "and", só 3 pares numa frase;
//			else if(listPairsValueUnit.size() == 3)
//			{	
////				System.out.println("RELAÇÕES COMPLEXAS: frase com mais que 1 par -> 3 pares e tem um AND");
//				for(int i=0; i < (listPairsValueUnit.size() - 1); i++)
//				{
//					if(listPairsValueUnit.get(i+1).getX().getStartOffset() < (listPairsValueUnit.get(i).getY().getEndOffset() + maxDistBetPairsVU))
//					{
////						System.out.println("Os pares tem um AND: " + entSent);				
//						long startRelation;
//						long endRelation;				
//
//						if(i ==0)
//						{
//							startRelation = sent.getStartOffset();
//							endRelation = (listPairsValueUnit.get(i+2).getX().getStartOffset() - 1);				
//						}
//						else
//						{
//							startRelation = (listPairsValueUnit.get(i-1).getY().getEndOffset() + 1);
//							endRelation = sent.getEndOffset();				
//						}
//
//						int relationScore = pairScore.get(listPairsValueUnit.get(i)) + pairScore.get(listPairsValueUnit.get(i+1));
////						System.out.println("Valor do PAR: " + relationScore);
//
//						List<IEntityAnnotation> left= new ArrayList<IEntityAnnotation>();
//						for(int j=0; j<enzSent.size(); j++)
//						{
//							if(enzSent.get(j).getEndOffset() < listPairsValueUnit.get(i).getX().getStartOffset() && enzSent.get(j).getStartOffset() > startRelation)
//							{
//								left.add(enzSent.get(j));
//								relationScore += enzScore;
//							}
//						}
//						for(int j=0; j<metSent.size(); j++)
//						{
//							if(metSent.get(j).getEndOffset() < listPairsValueUnit.get(i).getX().getStartOffset() && metSent.get(j).getStartOffset() > startRelation)
//							{
//								left.add(metSent.get(j));
//								relationScore += metScore;
//							}
//						}
//						for(int j=0; j<kparamSent.size(); j++)
//						{
//							if(kparamSent.get(j).getEndOffset() < listPairsValueUnit.get(i).getX().getStartOffset() && kparamSent.get(j).getStartOffset() > startRelation)
//							{
//								left.add(kparamSent.get(j));
//								relationScore += kParamScore;
//							}
//						}
//						left.add(listPairsValueUnit.get(i).getX());
//						left.add(listPairsValueUnit.get(i).getY());
////						System.out.println("Valor da relação Esquerda: " + relationScore);
//
//						List<IEntityAnnotation> right= new ArrayList<IEntityAnnotation>();
//						for(int j=0; j<enzSent.size(); j++)
//						{
//							if(enzSent.get(j).getStartOffset() > listPairsValueUnit.get(i).getY().getEndOffset() && enzSent.get(j).getStartOffset() < endRelation)
//							{
//								right.add(enzSent.get(j));
//								relationScore += enzScore;
//							}
//						}
//						for(int j=0; j<metSent.size(); j++)
//						{
//							if(metSent.get(j).getStartOffset() > listPairsValueUnit.get(i).getY().getEndOffset() && metSent.get(j).getStartOffset() < endRelation)
//							{
//								right.add(metSent.get(j));
//								relationScore += metScore;
//							}
//						}
//						for(int j=0; j<kparamSent.size(); j++)
//						{
//							if(kparamSent.get(j).getStartOffset() > listPairsValueUnit.get(i).getY().getEndOffset() && kparamSent.get(j).getStartOffset() < endRelation)
//							{
//								right.add(kparamSent.get(j));
//								relationScore += kParamScore;
//							}
//						}
//						right.add(listPairsValueUnit.get(i+1).getY());
//						right.add(listPairsValueUnit.get(i+1).getX());
////						System.out.println("Valor da relação Direita: " + relationScore);
//
//						IEventProperties eventProperties = new EventPropertiesImpl();
//						eventProperties.setGeneralProperty("score", String.valueOf(relationScore));
//						// o HUGO mandou mudar de endRelation para startRelation para resolver o problam do verde na visualização
//						IEventAnnotation relationThreePairsAnd = new EventAnnotationImpl(startRelation, startRelation, GlobalNames.re, left, right,
//								"", 0, "", eventProperties);
//						// para não imprimir relações só valor-unidade, sem entidades das classes kparam/enz/met;
//						if(left.size()+right.size()!=2)
//						{
//							results.add(relationThreePairsAnd);	
//						}
//					}
//				}
//			}
//			// Relacoes de dois pares seguidos com "and", só 4 pares numa frase;
//			else if(listPairsValueUnit.size() == 4)
//			{	
////				System.out.println("RELAÇÕES COMPLEXAS: frase com mais que 1 par -> 4 pares e tem um AND");
//				for(int i=0; i < (listPairsValueUnit.size() - 1); i++)
//				{
//					if(listPairsValueUnit.get(i+1).getX().getStartOffset() < (listPairsValueUnit.get(i).getY().getEndOffset() + maxDistBetPairsVU))
//					{
////						System.out.println("Os pares tem um AND: " + entSent);				
//						long startRelation;
//						long endRelation;				
//
//						if(i == 0)
//						{
//							startRelation = sent.getStartOffset();
//							endRelation = (listPairsValueUnit.get(i+2).getX().getStartOffset() - 1);				
//						}
//						else if(i == 1)
//						{
//							startRelation = (listPairsValueUnit.get(i-1).getY().getEndOffset() + 1);
//							endRelation = (listPairsValueUnit.get(i+2).getX().getStartOffset() - 1);				
//						}
//						else
//						{
//							startRelation = (listPairsValueUnit.get(i-1).getY().getEndOffset() + 1);
//							endRelation = sent.getEndOffset();				
//						}
//
//						int relationScore = pairScore.get(listPairsValueUnit.get(i)) + pairScore.get(listPairsValueUnit.get(i+1));
////						System.out.println("Valor do PAR: " + relationScore);
//
//						List<IEntityAnnotation> left= new ArrayList<IEntityAnnotation>();
//						for(int j=0; j<enzSent.size(); j++)
//						{
//							if(enzSent.get(j).getEndOffset() < listPairsValueUnit.get(i).getX().getStartOffset() && enzSent.get(j).getStartOffset() > startRelation)
//							{
//								left.add(enzSent.get(j));
//								relationScore += enzScore;
//							}
//						}
//						for(int j=0; j<metSent.size(); j++)
//						{
//							if(metSent.get(j).getEndOffset() < listPairsValueUnit.get(i).getX().getStartOffset() && metSent.get(j).getStartOffset() > startRelation)
//							{
//								left.add(metSent.get(j));
//								relationScore += metScore;
//							}
//						}
//						for(int j=0; j<kparamSent.size(); j++)
//						{
//							if(kparamSent.get(j).getEndOffset() < listPairsValueUnit.get(i).getX().getStartOffset() && kparamSent.get(j).getStartOffset() > startRelation)
//							{
//								left.add(kparamSent.get(j));
//								relationScore += kParamScore;
//							}
//						}
//						left.add(listPairsValueUnit.get(i).getX());
//						left.add(listPairsValueUnit.get(i).getY());
////						System.out.println("Valor da relação Esquerda: " + relationScore);
//
//						List<IEntityAnnotation> right= new ArrayList<IEntityAnnotation>();
//						for(int j=0; j<enzSent.size(); j++)
//						{
//							if(enzSent.get(j).getStartOffset() > listPairsValueUnit.get(i).getY().getEndOffset() && enzSent.get(j).getStartOffset() < endRelation)
//							{
//								right.add(enzSent.get(j));
//								relationScore += enzScore;
//							}
//						}
//						for(int j=0; j<metSent.size(); j++)
//						{
//							if(metSent.get(j).getStartOffset() > listPairsValueUnit.get(i).getY().getEndOffset() && metSent.get(j).getStartOffset() < endRelation)
//							{
//								right.add(metSent.get(j));
//								relationScore += metScore;
//							}
//						}
//						for(int j=0; j<kparamSent.size(); j++)
//						{
//							if(kparamSent.get(j).getStartOffset() > listPairsValueUnit.get(i).getY().getEndOffset() && kparamSent.get(j).getStartOffset() < endRelation)
//							{
//								right.add(kparamSent.get(j));
//								relationScore += kParamScore;
//							}
//						}
//						right.add(listPairsValueUnit.get(i+1).getY());
//						right.add(listPairsValueUnit.get(i+1).getX());
////						System.out.println("Valor da relação Direita: " + relationScore);
//
//						IEventProperties eventProperties = new EventPropertiesImpl();
//						eventProperties.setGeneralProperty("score", String.valueOf(relationScore));
//						// o HUGO mandou mudar de endRelation para startRelation para resolver o problam do verde na visualização
//						IEventAnnotation relationFourPairsAnd = new EventAnnotationImpl(startRelation, startRelation, GlobalNames.re, left, right,
//								"", 0, "", eventProperties);
//						// para não imprimir relações só valor-unidade, sem entidades das classes kparam/enz/met;
//						if(left.size()+right.size()!=2)
//						{
//							results.add(relationFourPairsAnd);	
//						}
//					}
//				}
//			}
//			else
//			{
////				System.out.println("RELAÇÕES COMPLEXAS: frase com mais que 1 par, sem ANDs");
//				for(int i=0; i<listPairsValueUnit.size(); i++)
//				{
//					long startRelation;
//					long endRelation;				
//
//					if(i == 0)
//					{
//						startRelation = sent.getStartOffset();
//						endRelation = (listPairsValueUnit.get(i+1).getX().getStartOffset() - 1);				
//					}
//					else if(i == listPairsValueUnit.size() - 1)
//					{
//						startRelation = (listPairsValueUnit.get(i-1).getY().getEndOffset() + 1);
//						endRelation = sent.getEndOffset();				
//					}
//					else
//					{
//						startRelation = (listPairsValueUnit.get(i-1).getY().getEndOffset() + 1);
//						endRelation = (listPairsValueUnit.get(i+1).getX().getStartOffset() - 1);				
//					}
//
////					System.out.println("START END RELATION: " + startRelation + " " + endRelation);
//
//					int relationScore = pairScore.get(listPairsValueUnit.get(0));
//					//					System.out.println("Valor do PAR: " + relationScore);
//
//					List<IEntityAnnotation> left= new ArrayList<IEntityAnnotation>();
//					for(int j=0; j<enzSent.size(); j++)
//					{
//						if(enzSent.get(j).getEndOffset() < listPairsValueUnit.get(i).getX().getStartOffset() && enzSent.get(j).getStartOffset() > startRelation)
//						{
//							left.add(enzSent.get(j));
//							relationScore += enzScore;
//						}
//					}
//					for(int j=0; j<metSent.size(); j++)
//					{
//						if(metSent.get(j).getEndOffset() < listPairsValueUnit.get(i).getX().getStartOffset() && metSent.get(j).getStartOffset() > startRelation)
//						{
//							left.add(metSent.get(j));
//							relationScore += metScore;
//						}
//					}
//					for(int j=0; j<kparamSent.size(); j++)
//					{
//						if(kparamSent.get(j).getEndOffset() < listPairsValueUnit.get(i).getX().getStartOffset() && kparamSent.get(j).getStartOffset() > startRelation)
//						{
//							left.add(kparamSent.get(j));
//							relationScore += kParamScore;
//						}
//					}
//					left.add(listPairsValueUnit.get(i).getX());
//					//					System.out.println("Valor da relação Esquerda: " + relationScore);
//
//					List<IEntityAnnotation> right= new ArrayList<IEntityAnnotation>();
//					for(int j=0; j<enzSent.size(); j++)
//					{
//						if(enzSent.get(j).getStartOffset() > listPairsValueUnit.get(i).getY().getEndOffset() && enzSent.get(j).getStartOffset() < endRelation)
//						{
//							right.add(enzSent.get(j));
//							relationScore += enzScore;
//						}
//					}
//					for(int j=0; j<metSent.size(); j++)
//					{
//						if(metSent.get(j).getStartOffset() > listPairsValueUnit.get(i).getY().getEndOffset() && metSent.get(j).getStartOffset() < endRelation)
//						{
//							right.add(metSent.get(j));
//							relationScore += metScore;
//						}
//					}
//					for(int j=0; j<kparamSent.size(); j++)
//					{
//						if(kparamSent.get(j).getStartOffset() > listPairsValueUnit.get(i).getY().getEndOffset() && kparamSent.get(j).getStartOffset() < endRelation)
//						{
//							right.add(kparamSent.get(j));
//							relationScore += kParamScore;
//						}
//					}
//					right.add(listPairsValueUnit.get(i).getY());
//					//System.out.println("Valor da relação Direita: " + relationScore);
//
//					IEventProperties eventProperties = new EventPropertiesImpl();
//					eventProperties.setGeneralProperty("score", String.valueOf(relationScore));
//					// o HUGO mandou mudar de endRelation para startRelatiion para resolver o problam do verde na visualização
//					IEventAnnotation relationMorePairs = new EventAnnotationImpl(startRelation, startRelation, GlobalNames.re, left, right,
//							"", 0, "", eventProperties);
//					// para não imprimir relações só valor-unidade, sem entidades das classes kparam/enz/met;		
//					if(left.size()+right.size()!=2)
//					{
//						results.add(relationMorePairs);	
//					}
//				}
//			}
//		}
//
//		// Tratar tabelas
//
//		//retorna valores
//		return results;
//	}
//
//	// Só um par a ana garnate que funciona bem
//	private void onlyonepair(
//			ISentence sent,
//			List<IEventAnnotation> results,
//			List<GenericPairImpl<IEntityAnnotation, IEntityAnnotation>> listPairsValueUnit,
//			Map<GenericPairImpl<IEntityAnnotation, IEntityAnnotation>, Integer> pairScore,
//			List<IEntityAnnotation> enzSent, List<IEntityAnnotation> metSent,
//			List<IEntityAnnotation> kparamSent) {
////		System.out.println("RELAÇÕES COMPLEXAS: frase com um só par");
//		long startRelation = sent.getStartOffset();
//		long endRelation = sent.getEndOffset();
//
//		int relationScore = pairScore.get(listPairsValueUnit.get(0));
//		//			System.out.println("Valor do PAR: " + relationScore);
//
//		List<IEntityAnnotation> left= new ArrayList<IEntityAnnotation>();
//		for(int i=0; i<enzSent.size(); i++)
//		{
//			if(enzSent.get(i).getEndOffset() < listPairsValueUnit.get(0).getX().getStartOffset())
//			{
//				left.add(enzSent.get(i));
//				relationScore += enzScore;
//			}
//		}
//		for(int i=0; i<metSent.size(); i++)
//		{
//			if(metSent.get(i).getEndOffset() < listPairsValueUnit.get(0).getX().getStartOffset())
//			{
//				left.add(metSent.get(i));
//				relationScore += metScore;
//			}
//		}
//		for(int i=0; i<kparamSent.size(); i++)
//		{
//			if(kparamSent.get(i).getEndOffset() < listPairsValueUnit.get(0).getX().getStartOffset())
//			{
//				left.add(kparamSent.get(i));
//				relationScore += kParamScore;
//			}
//		}
//		left.add(listPairsValueUnit.get(0).getX());
//		// System.out.println("Valor da relação Esquerda: " + relationScore);
//
//		List<IEntityAnnotation> right= new ArrayList<IEntityAnnotation>();
//		for(int i=0; i<enzSent.size(); i++)
//		{
//			if(enzSent.get(i).getStartOffset() > listPairsValueUnit.get(0).getY().getEndOffset())
//			{
//				right.add(enzSent.get(i));
//				relationScore += enzScore;
//			}
//		}
//		for(int i=0; i<metSent.size(); i++)
//		{
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
//		IEventAnnotation relationOnePair = new EventAnnotationImpl(startRelation, startRelation, GlobalNames.re, left, right,
//				"", 0, "", eventProperties);
//		// para não imprimir relações só valor-unidade, sem entidades das classes kparam/enz/met;
//		if(left.size()+right.size()!=2)
//		{
//			results.add(relationOnePair);	
//		}
//	}
//
//	/*
//	 * Função que buscar entidades de uma frase.
//	 * 
//	 * @return Lista das Entidades da frase
//	 */
//	protected List<IEntityAnnotation> getSentenceEntities(List<IEntityAnnotation> listEntitiesSortedByOffset, ISentence sentence) {
//		List<IEntityAnnotation> result = new ArrayList<IEntityAnnotation>();
//		for(IEntityAnnotation ent:listEntitiesSortedByOffset)
//		{
//			if(ent.getStartOffset()>=sentence.getStartOffset() && ent.getStartOffset()<sentence.getEndOffset())
//			{
//				result.add(ent);
//			}
//		}
//		return result;
//	}
//
//	/*
//	 * Método que retorna uma lista das entidades de uma classe presentes na frase.
//	 */
//	private List<IEntityAnnotation> getEntByClass(List<IEntityAnnotation> entSent, Set<Long> defClass) {
//		// cria lista
//		List<IEntityAnnotation> entClassSent = new ArrayList<IEntityAnnotation>();
//		for (int i=0; i<entSent.size(); i++){
//			if (defClass.contains(entSent.get(i).getClassAnnotation())){
//				IEntityAnnotation ent = entSent.get(i);
//				entClassSent.add(ent);
//			}
//		}
//		return entClassSent;	
//	}
//
//	/*
//	 *  Método que retorna se existem entidades (met, enz ou kparam) atrás ou à frente de um par
//	 */
//	private List<IEntityAnnotation> getEntBefPairAfter(int startRegion, int endRegion, List<IEntityAnnotation> entSentByClass)
//	{
//		// cria lista
//		List<IEntityAnnotation> entSentRegion = new ArrayList<IEntityAnnotation>();
//
//		return entSentRegion;
//	}
//
//}
