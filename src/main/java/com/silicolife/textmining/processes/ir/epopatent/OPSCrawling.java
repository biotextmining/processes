package com.silicolife.textmining.processes.ir.epopatent;

import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.pdfbox.exceptions.COSVisitorException;

import com.silicolife.textmining.core.datastructures.documents.PublicationExternalSourceLinkImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationSourcesDefaultEnum;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.init.general.GeneralDefaultSettings;
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
import com.silicolife.textmining.processes.ir.epopatent.configuration.PatentSearchDefaultSettings;
import com.silicolife.textmining.processes.ir.patentpipeline.PatentPipelineUtils;
import com.silicolife.textmining.utils.http.exceptions.ClientErrorException;
import com.silicolife.textmining.utils.http.exceptions.ConnectionException;
import com.silicolife.textmining.utils.http.exceptions.RedirectionException;
import com.silicolife.textmining.utils.http.exceptions.ResponseHandlingException;
import com.silicolife.textmining.utils.http.exceptions.ServerErrorException;

public class OPSCrawling extends IRProcessImpl implements IIRCrawl{

	public static IPublicationExternalSourceLink type = new PublicationExternalSourceLinkImpl("-1",PublicationSourcesDefaultEnum.patent.name());
	private boolean stop ;
	private ISimpleTimeLeft progress;
	private Integer startRange;
	private Integer endRAnge;
	private Long startTime;
	private static long reconnectiontimeSeconds = 1900;


	public OPSCrawling()
	{

	}

	@Override
	public IIRCrawlingProcessReport getFullText(List<IPublication> publications) throws ANoteException {
		String tokenaccess = null;
		String saveDocDirectory = InitConfiguration.getPropertyValueFromInitOrProperties(GeneralDefaultSettings.PDFDOCDIRECTORY).toString();
		if(saveDocDirectory==null)
		{
			throw new ANoteException("To Add A document user needs to configure Docs Directory (General.PDFDirectoryDocuments) in settings");
		}
		String autentication = InitConfiguration.getPropertyValueFromInitOrProperties(PatentSearchDefaultSettings.ACCESS_TOKEN).toString();
		if(autentication!=null)
		{
			autentication = Utils.get64Base(autentication);
			tokenaccess=OPSUtils.loginOPS(autentication);
		}
		else{
			throw new ANoteException("To Add a document user needs to configure ACCESS_TOKEN (PatentSearchDefaultSettings.ACCESS_TOKEN)");
		}

		stop = false;
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
		long startControlTime = System.currentTimeMillis();
		Iterator<IPublication> iterator = publications.iterator();
		while(iterator.hasNext() && !stop)
		{
			IPublication pub = iterator.next();
			Set<String> patentIDs = PublicationImpl.getPublicationExternalIDSetForSource(pub, PublicationSourcesDefaultEnum.patent.name());
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
				long actualControlTime=System.currentTimeMillis();
				if(actualControlTime-startControlTime>=OPSCrawling.reconnectiontimeSeconds*1000){//15min
					try {
						System.out.println("sleeping...5 seconds");
						Thread.sleep(5000);
						tokenaccess=OPSUtils.loginOPS(autentication);
						startControlTime=actualControlTime;
					} catch (InterruptedException e) {
						throw new ANoteException(e);
					}
				}
				Set<String> possiblePatentIDs = calculateAllPossiblePatentIds(patentIDs);
				File fileDownloaded = searchINallpatentIds(saveDocDirectory, tokenaccess, possiblePatentIDs, pub);
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

	private Set<String> calculateAllPossiblePatentIds(Set<String> patentIDs) {
		Set<String> possiblePatentIDs = new HashSet<>();
		for(String patentID :patentIDs)
		{
			possiblePatentIDs.addAll(PatentPipelineUtils.createPatentIDPossibilities(patentID));
		}
		return possiblePatentIDs;
	}

	private File searchINallpatentIds(String saveDocDirectory, String tokenaccess,
			Set<String> possiblePatentIDs, IPublication pub) throws ANoteException {
		File fileDownloaded;
		for (String patentID:possiblePatentIDs){
			// Try to discard all not English downloads
			if(OPSUtils.isAcceptablePatentLanguage(patentID))
			{
				// Try Download
				fileDownloaded =getPDFAndUpdateReportUsingPatentID(tokenaccess, patentID, saveDocDirectory, pub.getId());
				if(fileDownloaded != null)
				{
					pub.setRelativePath(fileDownloaded.getName());
					InitConfiguration.getDataAccess().updatePublication(pub);
					return fileDownloaded;
				}
			}
		}
		return null;
	}


	protected File getPDFAndUpdateReport(String tokenaccess,IPublication pub,String saveDocDirectoty) throws ANoteException{
		File file = null;
		try {
			file = OPSUtils.getPatentFullTextPDF(tokenaccess,pub,saveDocDirectoty);
		} catch (COSVisitorException | RedirectionException
				| ClientErrorException | ServerErrorException
				| ConnectionException | ResponseHandlingException e) {
		} catch (InterruptedException e) {
		} catch (IOException e) {
			throw new ANoteException(e);
		}
		return file;
	}

	protected File getPDFAndUpdateReportUsingPatentID(String tokenaccess,String patentID,String saveDocDirectoty, long pubID) throws ANoteException{
		File file = null;
		try {
			file = OPSUtils.getPatentFullTextPDFUsingPatentID(tokenaccess, patentID, saveDocDirectoty, pubID );
		} catch (COSVisitorException | RedirectionException
				| ClientErrorException | ServerErrorException
				| ConnectionException | ResponseHandlingException e) {
		} catch (InterruptedException e) {
		} catch (IOException e) {
			throw new ANoteException(e);
		}
		return file;
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

	public long getId() {
		return 0;
	}

	public void stop() {
		stop = true;		
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
