package com.silicolife.textmining.processes.ir.epopatent;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
import com.silicolife.textmining.utils.http.exceptions.ClientErrorException;
import com.silicolife.textmining.utils.http.exceptions.ConnectionException;
import com.silicolife.textmining.utils.http.exceptions.RedirectionException;
import com.silicolife.textmining.utils.http.exceptions.ResponseHandlingException;
import com.silicolife.textmining.utils.http.exceptions.ServerErrorException;

public class OPSCrawling extends IRProcessImpl implements IIRCrawl{

	public static IPublicationExternalSourceLink type = new PublicationExternalSourceLinkImpl("-1",PublicationSourcesDefaultEnum.patent.name());
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
		String autentication = InitConfiguration.getPropertyValueFromInitOrProperties(PatentSearchDefaultSettings.ACCESS_TOKEN).toString();
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
		else{
			throw new ANoteException("To Add a document user needs to configure ACCESS_TOKEN (PatentSearchDefaultSettings.ACCESS_TOKEN)");
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
		String saveDocDirectory = InitConfiguration.getPropertyValueFromInitOrProperties(GeneralDefaultSettings.PDFDOCDIRECTORY).toString();
		if(saveDocDirectory==null)
		{
			throw new ANoteException("To Add A document user needs to configure Docs Directory (General.PDFDirectoryDocuments) in settings");
		}
		for(IPublication pub:publications)
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
				File fileDownloaded = getPDFAndUpdateReport(tokenaccess, pub, saveDocDirectory);
				if(fileDownloaded != null)
				{
					report.addFileDownloaded(pub);
					pub.setRelativePath(fileDownloaded.getName());
					//					InitConfiguration.getDataAccess().updatePublication(pub);
				}
				else
				{
					report.addFileNotDownloaded(pub);
				}
			}		
			if(!report.getListPublicationsNotDownloaded().contains(pub)){
				memoryAndProgress(start,step+1,total);
				step++;
			}

		}
		Set<IPublication> listPublicationsNotDownloaded = report.getListPublicationsNotDownloaded();
		if (listPublicationsNotDownloaded.size()>0){
			for (IPublication pub:listPublicationsNotDownloaded){
				String patentID = PublicationImpl.getPublicationExternalIDForSource(pub, PublicationSourcesDefaultEnum.patent.name());
				String newPatentID=deleteSectionNumbers(patentID);
				File fileDownloaded = getPDFAndUpdateReportUsingPatentID(tokenaccess, newPatentID, saveDocDirectory, pub.getId());
				if(fileDownloaded != null)
				{
					report.addFileDownloaded(pub);
					report.getListPublicationsNotDownloaded().remove(pub);
					pub.setRelativePath(fileDownloaded.getName());
					//					InitConfiguration.getDataAccess().updatePublication(pub);
				}
				if(!report.getListPublicationsNotDownloaded().contains(pub)){
					memoryAndProgress(start,step+1,total);
					step++;
				}
			}
		}
		listPublicationsNotDownloaded = report.getListPublicationsNotDownloaded();
		if (listPublicationsNotDownloaded.size()>0){
			for (IPublication pub:listPublicationsNotDownloaded){
				report=tryAlternativePDFDownload(pub, report, tokenaccess, saveDocDirectory, false, false);//without delete section numbers neither transform date
				if(!report.getListPublicationsNotDownloaded().contains(pub)){
					memoryAndProgress(start,step+1,total);
					step++;
				}
			}
		}
		listPublicationsNotDownloaded = report.getListPublicationsNotDownloaded();
		if (listPublicationsNotDownloaded.size()>0){
			for (IPublication pub:listPublicationsNotDownloaded){
				report=tryAlternativePDFDownload(pub, report, tokenaccess, saveDocDirectory, true, false);//with deletion of section numbers only
				if(!report.getListPublicationsNotDownloaded().contains(pub)){
					memoryAndProgress(start,step+1,total);
					step++;
				}
			}
		}

		listPublicationsNotDownloaded = report.getListPublicationsNotDownloaded();
		if (listPublicationsNotDownloaded.size()>0){
			for (IPublication pub:listPublicationsNotDownloaded){
				report=tryAlternativePDFDownload(pub, report, tokenaccess, saveDocDirectory, true, true);//with deletion of section numbers and date transformation
				if(!report.getListPublicationsNotDownloaded().contains(pub)){
					memoryAndProgress(start,step+1,total);
					step++;
				}
			}
		}
		if(cancel)
			report.setcancel();
		long endTime = GregorianCalendar.getInstance().getTimeInMillis();
		report.setTime(endTime-start);
		System.out.println("Downloaded: " + report.getDocumentsRetrieval() + " of " + total);
		return report;
	}


	private IIRCrawlingProcessReport tryAlternativePDFDownload(IPublication pub,IIRCrawlingProcessReport report,String tokenaccess, String saveDocDirectory, boolean deleteSectionNumbers, boolean transformData) throws ANoteException{
		String patentID = PublicationImpl.getPublicationExternalIDForSource(pub, PublicationSourcesDefaultEnum.patent.name());
		String newPatentID;
		try {
			newPatentID = rearrangePatentID(patentID,deleteSectionNumbers,transformData);
			if (!patentID.equals(newPatentID)){
				File fileDownloaded = getPDFAndUpdateReportUsingPatentID(tokenaccess, newPatentID, saveDocDirectory, pub.getId());
				if(fileDownloaded != null)
				{
					report.addFileDownloaded(pub);
					report.getListPublicationsNotDownloaded().remove(pub);
					pub.setRelativePath(fileDownloaded.getName());
					//					InitConfiguration.getDataAccess().updatePublication(pub);
				}

			}
		} catch (ParseException e) {
			throw new ANoteException(e);
		}
		return report;

	}





	public String rearrangePatentID(String patentID,boolean deleteSectionNumbers, boolean transformData) throws ParseException{
		String newPatentID = patentID;
		if(deleteSectionNumbers==false){
			if (Character.isLetter(patentID.charAt(patentID.length()-2))){
				if(transformData==false){
					newPatentID=deleteChar0(newPatentID, true, 2);
				}
				else{
					String newPatentIDChar = deleteChar0(newPatentID, true, 2);
					if (Character.isLetter(newPatentIDChar.charAt(0))){
						if (Character.isLetter(newPatentIDChar.charAt(1))){
							String year=newPatentIDChar.substring(2,6);//year
							String newYear = transformYear(year);
							newPatentID=newPatentIDChar.substring(0, 2)+newYear+newPatentIDChar.substring(6, newPatentIDChar.length());
						}
						else{
							String year=newPatentIDChar.substring(1,5);//year
							String newYear = transformYear(year);
							newPatentID=newPatentIDChar.substring(0, 1)+newYear+newPatentIDChar.substring(5, newPatentIDChar.length());

						}
					}
				}
			}
			else if (Character.isLetter(patentID.charAt(patentID.length()-1))){
				if(transformData==false){
					newPatentID=deleteChar0(newPatentID, true, 1);
				}
				else{
					String newPatentIDChar = deleteChar0(newPatentID, true, 1);
					if (Character.isLetter(newPatentIDChar.charAt(0))){
						if (Character.isLetter(newPatentIDChar.charAt(1))){
							String year=newPatentIDChar.substring(2,6);//year
							String newYear = transformYear(year);
							newPatentID=newPatentIDChar.substring(0, 2)+newYear+newPatentIDChar.substring(6, newPatentIDChar.length());
						}
						else{
							String year=newPatentIDChar.substring(1,5);//year
							String newYear = transformYear(year);
							newPatentID=newPatentIDChar.substring(0, 1)+newYear+newPatentIDChar.substring(5, newPatentIDChar.length());
						}
					}
				}
			}
			else{
				if(transformData==false){
					newPatentID=deleteChar0(newPatentID, false, 0);
				}
				else{
					String newPatentIDChar = deleteChar0(newPatentID, true, 1);
					if (Character.isLetter(newPatentIDChar.charAt(0))){
						if (Character.isLetter(newPatentIDChar.charAt(1))){
							String year=newPatentIDChar.substring(2,6);//year
							String newYear = transformYear(year);
							newPatentID=newPatentIDChar.substring(0, 2)+newYear+newPatentIDChar.substring(6, newPatentIDChar.length());
						}
						else{
							String year=newPatentIDChar.substring(1,5);//year
							String newYear = transformYear(year);
							newPatentID=newPatentIDChar.substring(0, 1)+newYear+newPatentIDChar.substring(5, newPatentIDChar.length());
						}
					}
				}
			}
		}
		else{
			if(transformData==false){
				String newPatentIDDeletion = deleteSectionNumbers(newPatentID);
				newPatentID=deleteChar0(newPatentIDDeletion, false, 0);
			}
			else{
				String newPatentIDDeletion = deleteSectionNumbers(newPatentID);
				String newPatentIDChar = deleteChar0(newPatentIDDeletion, false, 0);
				if (Character.isLetter(newPatentIDChar.charAt(0))){
					if (Character.isLetter(newPatentIDChar.charAt(1))){
						String year=newPatentIDChar.substring(2,6);//year
						String newYear = transformYear(year);
						newPatentID=newPatentIDChar.substring(0, 2)+newYear+newPatentIDChar.substring(6, newPatentIDChar.length());
					}
					else{
						String year=newPatentIDChar.substring(1,5);//year
						String newYear = transformYear(year);
						newPatentID=newPatentIDChar.substring(0, 1)+newYear+newPatentIDChar.substring(5, newPatentIDChar.length());

					}
				}
			}
		}
		return newPatentID;
	}


	private String deleteChar0(String patentID,boolean haveSectionNumbers,int lettersOfSection){
		String newPatentID = patentID;
		if (haveSectionNumbers==false){
			if(patentID.charAt(patentID.length()-7)=='0'){//some patents have a "0" on middle with 6 numbers after
				newPatentID=patentID.substring(0,patentID.length()-7).concat(patentID.substring(patentID.length()-6,patentID.length()));
			}
		}
		else{
			if(patentID.charAt(patentID.length()-7-(lettersOfSection))=='0'){//some patents have a "0" on middle with 6 numbers after
				newPatentID=patentID.substring(0,patentID.length()-7-(lettersOfSection)).concat(patentID.substring(patentID.length()-6-(lettersOfSection),patentID.length()));
			}
		}
		return newPatentID;
	}


	private String transformYear(String date) throws ParseException{
		int year=Integer.parseInt(date);
		String newDate=date;
		if (year>=1900 && year<=Calendar.getInstance().get(Calendar.YEAR)){
			SimpleDateFormat dateParser = new SimpleDateFormat("yyyy"); //formatter for parsing date
			SimpleDateFormat dateFormatter = new SimpleDateFormat("yy"); //formatter for formatting date output
			Date dateParsering = dateParser.parse(date);
			newDate = dateFormatter.format(dateParsering);
		}
		return newDate;
	}


	private String deleteSectionNumbers(String patentID){

		//char[] array = patentID.toCharArray();
		String newPatentID = patentID;
		if (Character.isLetter(patentID.charAt(patentID.length()-1))){
			newPatentID=patentID.substring(0, patentID.length()-1);
		}
		else{
			if(Character.isLetter(patentID.charAt(patentID.length()-2))){
				newPatentID=patentID.substring(0,patentID.length()-2);
			}
		}
		return newPatentID;
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
