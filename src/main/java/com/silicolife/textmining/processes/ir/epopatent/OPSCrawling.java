package com.silicolife.textmining.processes.ir.epopatent;

import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.pdfbox.exceptions.COSVisitorException;

import com.silicolife.http.exceptions.ClientErrorException;
import com.silicolife.http.exceptions.ConnectionException;
import com.silicolife.http.exceptions.RedirectionException;
import com.silicolife.http.exceptions.ResponseHandlingException;
import com.silicolife.http.exceptions.ServerErrorException;
import com.silicolife.textmining.core.datastructures.documents.PublicationExternalSourceLinkImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.init.general.GeneralDefaultSettings;
import com.silicolife.textmining.core.datastructures.init.propertiesmanager.PropertiesManager;
import com.silicolife.textmining.core.datastructures.process.IRProcessImpl;
import com.silicolife.textmining.core.datastructures.process.ProcessOriginImpl;
import com.silicolife.textmining.core.datastructures.process.ProcessTypeImpl;
import com.silicolife.textmining.core.datastructures.report.processes.IRCrawlingReportImpl;
import com.silicolife.textmining.core.datastructures.utils.Utils;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalOptions;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IDocumentSet;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.IPublicationExternalSourceLink;
import com.silicolife.textmining.core.interfaces.core.report.processes.ir.IIRCrawlingProcessReport;
import com.silicolife.textmining.core.interfaces.process.IProcessOrigin;
import com.silicolife.textmining.core.interfaces.process.IProcessType;
import com.silicolife.textmining.core.interfaces.process.IR.IIRCrawl;
import com.silicolife.textmining.core.interfaces.process.IR.IIRSearchConfiguration;
import com.silicolife.textmining.core.interfaces.process.utils.ISimpleTimeLeft;
import com.silicolife.textmining.processes.ir.epopatent.configuration.OPSConfiguration;
import com.silicolife.textmining.processes.ir.epopatent.configuration.PatentSearchDefaultSettings;

public class OPSCrawling extends IRProcessImpl implements IIRCrawl{
	
	public static IPublicationExternalSourceLink type = new PublicationExternalSourceLinkImpl("-1",OPSConfiguration.epodoc);
	private boolean cancel ;
	private ISimpleTimeLeft progress;
	private Integer startRange;
	private Integer endRAnge;
	private Long startTime;
	

	public OPSCrawling()
	{
		
	}
	
	@Override
	public IIRCrawlingProcessReport getFullText(List<IPublication> publications) throws ANoteException {
		String tokenaccess = null;
		String autentication = PropertiesManager.getPManager().getProperty(PatentSearchDefaultSettings.ACCESS_TOKEN).toString();
		if(autentication!=null)
		{
			autentication = Utils.get64Base(autentication);
			try {
				tokenaccess = OPSUtils.postAuth(autentication);
			} catch (RedirectionException | ClientErrorException| ServerErrorException | ConnectionException
					| ResponseHandlingException e) {
				tokenaccess = null;
			}
		}
		cancel = false;
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
		for(IPublication pub:publications)
		{
			String epodoc = PublicationImpl.getPublicationExternalIDForSource(pub, OPSConfiguration.epodoc);
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
			else if(epodoc==null)
			{
				report.addFileNotDownloaded(pub);
			}
			else
			{
				
				File file = null;;
				try {
					file = OPSUtils.getPatentFullTextPDF(tokenaccess , pub,saveDocDirectoty);
				} catch (COSVisitorException | RedirectionException
						| ClientErrorException | ServerErrorException
						| ConnectionException | ResponseHandlingException e) {
				} catch (InterruptedException e) {
				} catch (IOException e) {
					throw new ANoteException(e);
				}
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
	public void validateConfiguration(IIRSearchConfiguration configuration) {
		// TODO Auto-generated method stub
		
	}
}
