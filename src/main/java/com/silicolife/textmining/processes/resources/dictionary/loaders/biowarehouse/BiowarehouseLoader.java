package com.silicolife.textmining.processes.resources.dictionary.loaders.biowarehouse;

/**
 * @author Hugo Costa
 * 
**/


public class BiowarehouseLoader {//implements IBiowareHouseLoader{
	
//
//	private boolean cancel;
//	private IResourceUpdateReport report;
//	private List<Long> insertedTErmIDList;
//	private Set<Long> newClassesAdded;
//	
//	public final static String compounds = "Compound";
//	public final static String enzymes = "Enzyme";
//	public final static String protein = "Protein";
//	public final static String gene = "Gene";
//	public final static String pathways = "Pathways";
//	public final static String organism = "Organism";
//
//
//
//	
//	public BiowarehouseLoader(IDictionary dictionary,IDatabase source)
//	{
//		super(dictionary,source);
//		report = new ResourceUpdateReportImpl(LanguageProperties.getLanguageStream("pt.uminho.anote2.general.resources.dictionary.update.report.title"), dictionary, new File(""), "Bioware House Database");
//		cancel = false;
//		this.insertedTErmIDList = new ArrayList<Long>();
//		this.newClassesAdded = new HashSet<Long>();
//	}
//	
//	
//	protected static String  metabolic_gene_query = "SELECT GeneWID,Name " +
//												   "FROM GeneWIDProteinWID JOIN Gene ON GeneWID=Gene.WID " +
//												   "WHERE ProteinWID IN " +
//												   "(SELECT ProteinWID " +
//												   "FROM EnzymaticReaction) AND Name IS NOT NULL ";
//	
//	protected static String enzymes_query = "SELECT DISTINCT Protein.WID,Name " +
//										  "FROM EnzymaticReaction JOIN Protein " +
//										  "ON ProteinWID=Protein.WID " +
//										  "WHERE Name IS NOT NULL ";
//	
//	protected static String gene_query = "SELECT WID,Name " +
//									   "FROM Gene " +
//									   "WHERE Name IS NOT NULL ";
//
//	protected static String protein_query = "SELECT WID,Name " +
//										  "FROM Protein " +
//										  "WHERE Name IS NOT NULL";
//
//	protected static String pathways_query = "SELECT WID,Name " +
//										   "FROM Pathway " +
//										   "WHERE Name IS NOT NULL ";
//	
//	protected static String organism_query = "SELECT WID,Name " +
//										   "FROM Taxon " +
//										   "WHERE Name IS NOT NULL ";
//	
//	protected static String compound_query = "SELECT WID,Name,SystematicName " +
//										   "FROM Chemical " +
//										   "WHERE Name IS NOT NULL";
//	
//	protected static String reaction_query = "SELECT WID,Name,ECNumber " +
//										   "FROM Reaction " +
//										   "WHERE Name IS NOT NULL" ;
//	
//	
//	public IResourceUpdateReport loadTermsFromBiowareHouse(Set<String> classForLoading,boolean synonyms) throws ANoteException, SQLException  {
//	
//		Map<String,Long> wuiResourceID = new HashMap<String, Long>();
//		if(classForLoading.contains("metabolic gene")&&!cancel)
//		{
//			long metGenesID =  ClassProperties.getClassIDOrinsertIfNotExist(GlobalNames.metabolicGenes);
//			metabolicInformation(metabolic_gene_query,metGenesID,wuiResourceID);
//		}
//		if(classForLoading.contains(enzymes)&&!cancel)
//		{
//			long enzymesID =  ClassProperties.getClassIDOrinsertIfNotExist(enzymes);
//			metabolicInformation(enzymes_query,enzymesID,wuiResourceID);
//		}
//		if(classForLoading.contains(gene)&&!cancel)
//			selectInsert(gene_query,gene,wuiResourceID);
//		if(classForLoading.contains(protein)&&!cancel)
//			selectInsert(protein_query,protein,wuiResourceID);
//		if(classForLoading.contains(pathways)&&!cancel)
//			selectInsert(pathways_query,pathways,wuiResourceID);
//		if(classForLoading.contains(organism)&&!cancel)
//			selectInsert(organism_query,.organism,wuiResourceID);
//		if(classForLoading.contains(compounds)&&!cancel)
//			selectInsert(compound_query,compounds,wuiResourceID);	
//		//There are no reaction names.!!
//		if(classForLoading.contains(GlobalNames.reactions)&&!cancel)
//			selectInsert(reaction_query,GlobalNames.reactions,wuiResourceID);
//		if(synonyms)
//			synonyms(wuiResourceID);	
//		return getReport();
//	}
//	
//	
//	protected void metabolicInformation(String query, long idclass, Map<String, Long> wuiResourceID) throws ANoteException, SQLException {
//		Statement stmt = (Statement) getSource().getConnection().createStatement();	
//		ResultSet res=stmt.executeQuery(query);
//		while (res.next()) {
//			String wuid = res.getString(1);
//			String term=res.getString(2);
//			IResourceElement elem = new ResourceElementImpl(term,idclass,"",null,null,-1,true);
//			if(getDictionary().addResourceElement(elem))
//			{
//				report.addTermAdding(1);
//				this.insertedTErmIDList.add(elem.getID());
//				wuiResourceID.put(wuid, elem.getID());	
//			}
//			else
//			{
//				IInsertConflits conflit;
//				IResourceElement elemValue = getDictionary().getResourceElementsByName(elem.getTerm()).getElementsOrder().get(0);
//				if(elem.getTermClassID()==elemValue.getTermClassID())
//				{
//					conflit = new ResourceMergeConflits(ConflitsType.AlreadyHaveTerm, elemValue, elem);
//				}
//				else
//				{
//					conflit = new ResourceMergeConflits(ConflitsType.TermInDiferentClasses, elemValue, elem) ;
//				}
//				report.addConflit(conflit);
//			}
//		}
//		res.close();
//		stmt.close();
//	}
//
//	protected void selectInsert(String query,String classeName, Map<String, Long> wuiResourceID) throws ANoteException, SQLException {
//		long idclass= ClassProperties.getClassIDOrinsertIfNotExist(classeName);
//		Statement stmt = (Statement) getSource().getConnection().createStatement();		
//		ResultSet res=stmt.executeQuery(query);
//		while (res.next()) {
//			String uid = res.getString(1);
//			String term=res.getString(2);
//			IResourceElement elem = new ResourceElementImpl(term,idclass,"",null,null,-1,true);
//			if(getDictionary().addResourceElement(elem))
//			{
//				wuiResourceID.put(uid, elem.getID());
//				report.addTermAdding(1);
//				//Check whether the systematic name or the ECnumber exist
//				if(classeName.equals(GlobalNames.compounds) || classeName.equals(GlobalNames.reactions)){ 
//					String synonym=res.getString(3);
//					IResourceElement newelem = new ResourceElementImpl(elem.getID(),synonym,idclass,"",null,null,-1,true);
//					if(getDictionary().addResourceElementSynomyn(elem, synonym))
//					{
//						report.addSynonymsAdding(1);
//					}
//					else
//					{
//						IInsertConflits conflit = new ResourceMergeConflits(ConflitsType.AlreadyHaveSynonyms, elem, newelem);
//						report.addConflit(conflit);
//					}
//				}
//
//			}
//			else
//			{
//				IInsertConflits conflit;
//				IResourceElement elemValue = getDictionary().getResourceElementsByName(elem.getTerm()).getElementsOrder().get(0);
//				if(elem.getTermClassID()==elemValue.getTermClassID())
//				{
//					conflit = new ResourceMergeConflits(ConflitsType.AlreadyHaveTerm, elemValue, elem);
//				}
//				else
//				{
//					conflit = new ResourceMergeConflits(ConflitsType.TermInDiferentClasses, elemValue, elem) ;
//				}
//				report.addConflit(conflit);
//			}
//		}	
//		res.close();
//		stmt.close();		
//	}	
//
//	protected void synonyms(Map<String, Long> wuiResourceID) throws ANoteException, SQLException  
//	{
//		PreparedStatement wid_synonyms = (PreparedStatement) getSource().getConnection().prepareStatement("SELECT OtherWID,Syn "+ 
//				"FROM SynonymTable ");													
//		ResultSet syns = wid_synonyms.executeQuery();
//		while(syns.next())
//		{
//			String externalID = syns.getString(1);
//			if(wuiResourceID.containsKey(externalID))
//			{
//				long term_id = wuiResourceID.get(externalID);
//				String synonym=syns.getString(2);
//
//				IResourceElement elem = new ResourceElementImpl(term_id,"",-1,"",null,null,-1,false);
//				if(getDictionary().addResourceElementSynomyn(elem, synonym))
//				{
//					report.addSynonymsAdding(1);
//				}
//				else
//				{
//					IInsertConflits conflit = new ResourceMergeConflits(ConflitsType.AlreadyHaveSynonyms, elem, elem);
//					report.addConflit(conflit);
//				}
//			}
//		}
//		syns.close();
//		wid_synonyms.close();
//	}	
//	
//	public IDatabase getBiowareHouseDB() {
//		return getSource();
//	}
//
//
//	public void setcancel(boolean newCancel) {
//		cancel=newCancel;	
//	}
//	
//	public boolean isCancel() {
//		return cancel;
//	}
//
//
//	public void setCancel(boolean cancel) {
//		this.cancel = cancel;
//	}
//
//	public static String getMetabolic_gene_query() {
//		return metabolic_gene_query;
//	}
//
//
//	public static String getEnzymes_query() {
//		return enzymes_query;
//	}
//
//
//	public static String getGene_query() {
//		return gene_query;
//	}
//
//
//	public static String getProtein_query() {
//		return protein_query;
//	}
//
//
//	public static String getPathways_query() {
//		return pathways_query;
//	}
//
//
//	public static String getOrganism_query() {
//		return organism_query;
//	}
//
//
//	public static String getCompound_query() {
//		return compound_query;
//	}
//
//
//	public static String getReaction_query() {
//		return reaction_query;
//	}
//
//
//	public static void setReaction_query(String reaction_query) {
//		BiowarehouseLoader.reaction_query = reaction_query;
//	}
//
//	public IResourceUpdateReport getReport() {
//		return report;
//	}
//
//	@Override
//	public List<Long> getInsertedTermIDList() {
//		// TODO Auto-generated method stub
//		return this.insertedTErmIDList;
//	}
//
//	@Override
//	public Set<Long> getNewClassesAdded() {
//		// TODO Auto-generated method stub
//		return this.newClassesAdded;
//	}
//
//	@Override
//	public void stop() {
//		cancel=true;
//	}
	
	
}
