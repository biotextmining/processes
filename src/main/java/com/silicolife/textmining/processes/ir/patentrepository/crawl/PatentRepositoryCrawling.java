package com.silicolife.textmining.processes.ir.patentrepository.crawl;

import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.pdfbox.exceptions.COSVisitorException;

import com.lowagie.text.DocumentException;
import com.silicolife.textmining.core.datastructures.documents.PublicationExternalSourceLinkImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationSourcesDefaultEnum;
import com.silicolife.textmining.core.datastructures.exceptions.process.InvalidConfigurationException;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.init.general.GeneralDefaultSettings;
import com.silicolife.textmining.core.datastructures.process.IRProcessImpl;
import com.silicolife.textmining.core.datastructures.process.ProcessOriginImpl;
import com.silicolife.textmining.core.datastructures.process.ProcessTypeImpl;
import com.silicolife.textmining.core.datastructures.report.processes.IRCrawlingReportImpl;
import com.silicolife.textmining.core.datastructures.utils.FileHandling;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalOptions;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.IPublicationExternalSourceLink;
import com.silicolife.textmining.core.interfaces.core.report.processes.ir.IIRCrawlingProcessReport;
import com.silicolife.textmining.core.interfaces.process.IProcessOrigin;
import com.silicolife.textmining.core.interfaces.process.IProcessType;
import com.silicolife.textmining.core.interfaces.process.IR.IIRCrawl;
import com.silicolife.textmining.core.interfaces.process.IR.IIRSearchConfiguration;
import com.silicolife.textmining.core.interfaces.process.IR.exception.InternetConnectionProblemException;
import com.silicolife.textmining.core.interfaces.process.utils.ISimpleTimeLeft;
import com.silicolife.textmining.processes.ir.patentrepository.PatentRepositoryAPI;

public class PatentRepositoryCrawling extends IRProcessImpl implements IIRCrawl{
	
	public static IPublicationExternalSourceLink typeUSPTO = new PublicationExternalSourceLinkImpl("-1",PublicationSourcesDefaultEnum.uspto.name());
	public static IPublicationExternalSourceLink typePatent = new PublicationExternalSourceLinkImpl("-1",PublicationSourcesDefaultEnum.patent.name());

	private boolean stop = false ;
	private ISimpleTimeLeft progress;
	private Integer startRange;
	private Integer endRAnge;
	private Long startTime;

	private String urlServer;
	
	public PatentRepositoryCrawling()
	{
		this("http://mendel.di.uminho.pt:8080/patentrepository/");
	}

	public PatentRepositoryCrawling(String urlServer)
	{
		this.urlServer = urlServer;
	}

	@Override
	public IIRCrawlingProcessReport getFullText(List<IPublication> publications)
			throws ANoteException, InternetConnectionProblemException {
		stop = false;
		String saveDocDirectory = InitConfiguration.getPropertyValueFromInitOrProperties(GeneralDefaultSettings.PDFDOCDIRECTORY).toString();
		if(saveDocDirectory==null)
		{
			throw new ANoteException("To Add A document user needs to configure Docs Directory (General.PDFDirectoryDocuments) in settings");
		}
		long start = GregorianCalendar.getInstance().getTimeInMillis();
		if(this.startTime!=null){
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
		Iterator<IPublication> iterator = publications.iterator();
		while(iterator.hasNext() && !stop)
		{
			IPublication pub = iterator.next();
			Set<String> patentIDs = getCompatibleSource(pub);
			if(pub.isPDFAvailable())
			{
				report.addFileAlreadyDownloaded(pub);
			}
			else if(patentIDs.isEmpty())
			{
				report.addFileNotDownloaded(pub);
			}
			else
			{
				File fileDownloaded = searchINallpatentIds(saveDocDirectory, patentIDs, pub);
				if (fileDownloaded==null){
					report.addFileNotDownloaded(pub);
				}
				else
				{
					report.addFileDownloaded(pub);
				}
			}
			memoryAndProgress(start,step+1,total);
			step++;
		}
		if(stop)
			report.setcancel();
		long endTime = GregorianCalendar.getInstance().getTimeInMillis();
		report.setTime(endTime-start);
		return report;	
	}
	
	private File searchINallpatentIds(String saveDocDirectory,Set<String> patentIDs, IPublication pub) throws ANoteException {
		File fileDownloaded;
		for (String patentID:patentIDs){
			// Try Download
			fileDownloaded =getPDFAndUpdateReportUsingPatentID(patentID, saveDocDirectory, pub.getId());
			if(fileDownloaded != null)
			{
				pub.setRelativePath(fileDownloaded.getName());
				InitConfiguration.getDataAccess().updatePublication(pub);
				return fileDownloaded;
			}

		}
		return null;
	}
	
	protected File getPDFAndUpdateReportUsingPatentID(String patentID,String saveDocDirectoty, long pubID) throws ANoteException{
		try {
			String fullTextContent = PatentRepositoryAPI.getPatentFullText(urlServer, patentID);
			if(fullTextContent!=null && !fullTextContent.isEmpty())
			{
				String filepath = saveDocDirectoty + "/" + pubID + ".pdf";
				FileHandling.createPDFFileWithText(filepath,fullTextContent);
				return new File(filepath);
			}
		} catch (IOException | COSVisitorException | DocumentException e) {
		}
		return null;
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

	protected Set<String> getCompatibleSource(IPublication pub) {
		Set<String> patentIDs = PublicationImpl.getPublicationExternalIDSetForSource(pub, PublicationSourcesDefaultEnum.patent.name());
		patentIDs.addAll(PublicationImpl.getPublicationExternalIDSetForSource(pub, PublicationSourcesDefaultEnum.uspto.name()));
		return patentIDs;
	}
	
	public void stop() {
		stop = true;
	}

	public void setTimeProgress(ISimpleTimeLeft progress) {
		this.progress = progress;		
	}
	
	public IProcessOrigin getProcessOrigin() { return new ProcessOriginImpl(-1, "IR Crawl"); }
	
	public IProcessType getType() { return new ProcessTypeImpl(-1,"IRCrawl");}
	
	public long getId() { return 0;}

	public void setRange(Integer startRange, Integer endRAnge, Long startTime) {
		this.startRange = startRange;
		this.endRAnge = endRAnge;
		this.startTime = startTime;
	}
	
	public void validateConfiguration(IIRSearchConfiguration configuration) throws InvalidConfigurationException {}

}
