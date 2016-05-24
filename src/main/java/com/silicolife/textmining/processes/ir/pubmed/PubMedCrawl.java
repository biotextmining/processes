package com.silicolife.textmining.processes.ir.pubmed;

import java.io.File;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.silicolife.textmining.core.datastructures.documents.PublicationExternalSourceLinkImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationSourcesDefault;
import com.silicolife.textmining.core.datastructures.exceptions.process.InvalidConfigurationException;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.init.general.GeneralDefaultSettings;
import com.silicolife.textmining.core.datastructures.init.propertiesmanager.PropertiesManager;
import com.silicolife.textmining.core.datastructures.process.IRProcessImpl;
import com.silicolife.textmining.core.datastructures.process.ProcessOriginImpl;
import com.silicolife.textmining.core.datastructures.process.ProcessTypeImpl;
import com.silicolife.textmining.core.datastructures.report.processes.IRCrawlingReportImpl;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalOptions;
import com.silicolife.textmining.core.datastructures.utils.conf.OtherConfigurations;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IDocumentSet;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.IPublicationExternalSourceLink;
import com.silicolife.textmining.core.interfaces.core.report.processes.ir.IIRCrawlingProcessReport;
import com.silicolife.textmining.core.interfaces.process.IProcessOrigin;
import com.silicolife.textmining.core.interfaces.process.IProcessType;
import com.silicolife.textmining.core.interfaces.process.IR.IIRCrawl;
import com.silicolife.textmining.core.interfaces.process.IR.IIRSearchConfiguration;
import com.silicolife.textmining.core.interfaces.process.IR.exception.InternetConnectionProblemException;
import com.silicolife.textmining.core.interfaces.process.utils.ISimpleTimeLeft;
import com.silicolife.textmining.processes.ir.pubmed.crawl.WebConnectionConsole;
import com.silicolife.textmining.processes.ir.pubmed.utils.PMSearch;

public class PubMedCrawl extends IRProcessImpl implements IIRCrawl{

	public static IPublicationExternalSourceLink type = new PublicationExternalSourceLinkImpl("-1",PublicationSourcesDefault.pubmed);
	private boolean cancel ;
	private boolean onlyFreeFullText;
	private ISimpleTimeLeft progress;
	private Integer startRange;
	private Integer endRAnge;
	private Long startTime;
	public boolean ignoreTextConten = false;

	public PubMedCrawl()
	{

	}


	@Override
	public IIRCrawlingProcessReport getFullText(List<IPublication> publications) throws ANoteException, InternetConnectionProblemException{
		cancel = false;
		this.onlyFreeFullText = OtherConfigurations.getFreeFullTextOnly();
		long start = GregorianCalendar.getInstance().getTimeInMillis();
		if(this.startTime!=null)
		{
			start = startTime;
		}
		int step =0;
		if(this.startRange!=null)
		{
			step = this.startRange;
		}
		int total = publications.size();
		if(this.endRAnge!=null)
		{
			total = endRAnge;
		}
		IIRCrawlingProcessReport report = new IRCrawlingReportImpl();
		Set<String> pmidFreeFullTExt = getAllFreeFullTextPMID(publications);
		for(IPublication pub:publications)
		{
			String pmid = PublicationImpl.getPublicationExternalIDForSource(pub, PublicationSourcesDefault.pubmed);
			String saveDocDirectoty = (String) PropertiesManager.getPManager().getProperty(GeneralDefaultSettings.PDFDOCDIRECTORY);
			if(saveDocDirectoty==null)
			{
				throw new ANoteException("To Add A document user needs to configure Docs Directory (General.PDFDirectoryDocuments) in settings");
			}
			if(cancel)
			{
				report.setcancel();
				break;
			}
			if(pub.isPDFAvailable())
			{
				report.addFileAlreadyDownloaded(pub);
			}
			else if(onlyFreeFullText && !pmidFreeFullTExt.contains(pmid))
			{
				report.addFileRestrictedDownloaded(pub);
			}
			else if(pmid==null)
			{
				report.addFileNotDownloaded(pub);
			}
			else
			{
				File file = downloadPDF(pub,pmid,saveDocDirectoty,ignoreTextConten);
				if(file != null)
				{
					report.addFileDownloaded(pub);
					pub.setRelativePath(file.getName());
					InitConfiguration.getDataAccess().updatePublication(pub);
				}
				else
				{
					report.addFileNotDownloaded(pub);
				}
			}
			memoryAndProgress(start,step+1,total);
			step++;
		}
		if(cancel)
			report.setcancel();
		long endTime = GregorianCalendar.getInstance().getTimeInMillis();
		report.setTime(endTime-start);
		return report;
	}


	public static Set<String> getAllFreeFullTextPMID(List<IPublication> publications) throws ANoteException, InternetConnectionProblemException {
		String query = "(free full text[sb]) AND (";
		Set<String> toSearch = new HashSet<>();
		for(IPublication pub:publications)
		{
			String pmid = PublicationImpl.getPublicationExternalIDForSource(pub, PublicationSourcesDefault.pubmed);
			if(pmid!=null)
			{
				toSearch.add(pmid);
			}
		}
		if(toSearch.isEmpty())
			return toSearch;
		for(String pmid:toSearch)
		{
			query = query + pmid +"[uid] OR ";
		}
		query = query.substring(0, query.length()-4);
		query = query + ")";
		Set<String> result = PMSearch.getPublicationAvailableFreeFullText(query);
		return result;
	}


	protected void memoryAndProgress(long start, int step, int total) {
		if(this.progress!=null)
		{
			long nowTime = GregorianCalendar.getInstance().getTimeInMillis();
			progress.setProgress((float) step/ (float) total);
			progress.setTime(nowTime-start, step, total);
		}
		System.out.println((GlobalOptions.decimalformat.format((double)step/ (double) total * 100)) + " %...");
		Runtime.getRuntime().gc();
		System.out.println((Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory())/(1024*1024) + " MB ");
	}


	public IDocumentSet getDocuments() { return null;}

	public void setDocuments(IDocumentSet arg0) {}

	public IProcessType getType() {
		return new ProcessTypeImpl(-1,"IRCrawl");
	}

	public long getID() {
		return 0;
	}


	@Override
	public void stop() {
		cancel = true;		
	}

	public static File downloadPDF(IPublication pub, String pmid, String path,boolean ignoretextcontent) {
		if (!path.endsWith("/")) {
			path += "/";
		}
		if (WebConnectionConsole.buscarPDF(pub,path, pmid,ignoretextcontent)) {
			return new File(path + "/" + pmid + "/" + pub.getId() + ".pdf");
		} else {
			return null;
		}
	}


	@Override
	public IProcessOrigin getProcessOrigin() {
		return new ProcessOriginImpl(-1, "IR Crawl");
	}


	@Override
	public void setTimeProgress(ISimpleTimeLeft progress) {
		this.progress = progress;
	}


	@Override
	public void setRange(Integer startRange, Integer endRAnge, Long startTime) {
		this.startRange = startRange;
		this.endRAnge = endRAnge;
		this.startTime = startTime;
	}


	@Override
	public void validateConfiguration(IIRSearchConfiguration configuration)
			throws InvalidConfigurationException {
		// TODO Auto-generated method stub
		
	}

}
