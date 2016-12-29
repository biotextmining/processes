package com.silicolife.textmining.processes.ir.patentpipeline.components.retrievalmodules.wipo;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.GregorianCalendar;
import java.util.List;

import org.wipo.pctis.ps.client.Doc;
import org.wipo.pctis.ps.client.UnknownApplicationException_Exception;
import org.wipo.pctis.ps.client.UnknownDocumentException_Exception;

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
import com.silicolife.textmining.processes.ir.patentpipeline.components.retrievalmodules.wipo.help.ServiceHelper;
import com.sun.xml.ws.developer.StreamingDataHandler;

public class WIPOCrawling extends IRProcessImpl implements IIRCrawl{

	public static IPublicationExternalSourceLink type = new PublicationExternalSourceLinkImpl("-1",PublicationSourcesDefaultEnum.patent.name());
	private boolean cancel ;
	private ISimpleTimeLeft progress;
	private Integer startRange;
	private Integer endRAnge;
	private Long startTime;
	private ServiceHelper serviceHelper;


	public WIPOCrawling() {

	}


	@Override
	public IIRCrawlingProcessReport getFullText(List<IPublication> pubs)
			throws ANoteException, InternetConnectionProblemException {
		String userWIPO=InitConfiguration.getPropertyValueFromInitOrProperties(PatentSearchDefaultSettingsWIPO.USER_WIPO).toString();
		String passwordWIPO=InitConfiguration.getPropertyValueFromInitOrProperties(PatentSearchDefaultSettingsWIPO.PASSWORD_WIPO).toString();
		if(userWIPO==null||userWIPO.isEmpty()){
			throw new ANoteException("To Add a document user needs to configure USER_WIPO (PatentSearchDefaultSettingsWIPO.USER_WIPO)");
		}
		if (passwordWIPO==null||passwordWIPO.isEmpty()){
			throw new ANoteException("To Add a document user needs to configure PASSWORD_WIPO (PatentSearchDefaultSettingsWIPO.PASSWORD_WIPO )");
		}
		String saveDocDirectory = InitConfiguration.getPropertyValueFromInitOrProperties(GeneralDefaultSettings.PDFDOCDIRECTORY).toString();

		if(saveDocDirectory==null)
		{
			throw new ANoteException("To Add A document user needs to configure Docs Directory (General.PDFDirectoryDocuments) in settings");
		}

		try {
			serviceHelper = new ServiceHelper(userWIPO, passwordWIPO);
		} catch (MalformedURLException e) {
			throw new ANoteException(e);
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
		int total = pubs.size();
		if(this.endRAnge!=null)
		{
			total = endRAnge;
		}
		IIRCrawlingProcessReport report = new IRCrawlingReportImpl();
		for(IPublication pub:pubs)
		{
			String patentID = PublicationImpl.getPublicationExternalIDForSource(pub, PublicationSourcesDefaultEnum.patent.name());
			if(cancel)
			{
				report.setcancel();
				break;
			}
			if(pub.isPDFAvailable())
			{
				report.addFileAlreadyDownloaded(pub);
			}
			else if(patentID==null)
			{
				report.addFileNotDownloaded(pub);
			}
			else
			{

				File file = null;
				List<Doc> retour;
				try {
					retour = serviceHelper.getStub().getAvailableDocuments(patentID);

					for (int count=0; count<retour.size(); count++) {
						Doc doc = retour.get(count);
						if ((doc.getDocType().equals("PAMPH")) && (doc.getOcrPresence() != null) && (doc.getOcrPresence().equals("yes"))) {
							File outDir = new File(saveDocDirectory);
							String pathStock = saveDocDirectory +"/"+ pub.getId() + ".pdf";
							StreamingDataHandler myfile = (StreamingDataHandler)serviceHelper.getStub().getDocumentOcrContent(doc.getDocId());
							if (myfile != null)
							{
								if (!outDir.exists()) {
									outDir.mkdirs();
								}
								file = new File(pathStock);
								myfile.moveTo(file);
								myfile.close();
							}
						}
					}	
				} catch (UnknownApplicationException_Exception e) {
				} catch (UnknownDocumentException_Exception e) {
				} catch (IOException e) {
					throw new ANoteException(e);
				}
				if(file!= null)
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
	public IProcessType getType() {
		return new ProcessTypeImpl(-1,"IRCrawl");
	}

	@Override
	public IProcessOrigin getProcessOrigin() {
		return new ProcessOriginImpl(-1, "IR Crawl");
	}


	@Override
	public void stop() {
		cancel=true;

	}
	@Override
	public void validateConfiguration(IIRSearchConfiguration configuration) throws InvalidConfigurationException {
		// TODO Auto-generated method stub

	}

	@Override
	public long getId() {
		return 0;
	}

}
