package com.silicolife.textmining.processes.ir.pubmed;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy.Type;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.conn.params.ConnRoutePNames;

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
import com.silicolife.textmining.processes.ir.pubmed.crawl.Comun;
import com.silicolife.textmining.processes.ir.pubmed.utils.OAPMCReader;

public class PubMedCentralCrawl extends IRProcessImpl implements IIRCrawl{
	
	public static IPublicationExternalSourceLink type = new PublicationExternalSourceLinkImpl("-1",PublicationSourcesDefault.pmc);
	private static HttpClient client;
	private static String url = "http://www.ncbi.nlm.nih.gov/pmc/utils/oa/oa.fcgi";
//	private static String url = "http://www.ncbi.nlm.nih.gov/pmc/utils/oa/oa.fcgi?id=";
	private boolean cancel ;
	private ISimpleTimeLeft progress;
	private Integer startRange;
	private Integer endRAnge;
	private Long startTime;
	private static String server = "ftp.ncbi.nlm.nih.gov";

	
	static {
		// Create an instance of HttpClient.
		client = new HttpClient();
		// setting property
		if(InitConfiguration.getProxy()!=null && !InitConfiguration.getProxy().type().equals(Type.DIRECT))
			client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,InitConfiguration.getProxy());	
		// para aumentar la compatibilidad con scripts CGI mal escritos se ponen
		// todas las cookies en una nica peticion
//		client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
		// setting property
		client.getParams().setParameter("http.protocol.single-cookie-header", true);
	}
	


	public PubMedCentralCrawl()
	{
		
	}
	
	@Override
	public IIRCrawlingProcessReport getFullText(List<IPublication> publications) throws ANoteException, InternetConnectionProblemException {
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
			String pmc = PublicationImpl.getPublicationExternalIDForSource(pub, PublicationSourcesDefault.pmc);
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
			else if(pmc==null)
			{
				report.addFileNotDownloaded(pub);
			}
			else
			{
				pmc = pmc.toUpperCase();
				File file = downloadPDF(pub,pmc,saveDocDirectoty);
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
	
	public static File downloadPDF(IPublication pub, String pmc, String path){
		if (!path.endsWith("/")) {
			path += "/";
		}
		String pathToDownloadINPMC = findPMCPDF(pub,path, pmc);
		if(pathToDownloadINPMC==null)
			return null;
		return new File(pathToDownloadINPMC);
	}

	private static String findPMCPDF(IPublication pub, String path,String pmc){
		String url_cad = Comun.getUTF8String(url);
		PostMethod post = new PostMethod(url_cad);

		NameValuePair[] data = { 
				new NameValuePair("id", pmc.toUpperCase()),
		};
		post.setRequestBody(data);
		try {
			client.executeMethod(post);
			String url = readXMLResultFile(post);
			if(url==null)
				return null;	
			return retreivePMCPDF(url,path+"/"+pub.getId()+".pdf");
		} catch (IOException e) {
			return null;
		} catch (ANoteException e) {
			return null;
		}
	}
	
	private static String retreivePMCPDF(String url, String filepath) throws IOException{
		String urlChange = url.replace("ftp://", "http://");
		GetMethod getMehot = new GetMethod(urlChange);
		int statusCode =  client.executeMethod(getMehot);
		if (statusCode != HttpStatus.SC_OK) {
//			System.err.println("Method failed: " + method.getStatusLine());
			if (statusCode == HttpStatus.SC_NOT_FOUND
					|| statusCode == HttpStatus.SC_FORBIDDEN
					|| statusCode == HttpStatus.SC_UNAUTHORIZED
					|| statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR)
				return null;
		}
		InputStream inputStream = getMehot.getResponseBodyAsStream();
		File out = new File(filepath);
		FileOutputStream outputStream = new FileOutputStream(out);
        byte[] bytesIn = new byte[4096];
        int read = 0;

        while ((read = inputStream.read(bytesIn)) != -1) {
        	outputStream.write(bytesIn, 0, read);
        }
        inputStream.close();
        outputStream.close();
        return  filepath;
	}

	public static String readXMLResultFile(PostMethod post) throws ANoteException, IOException{
		InputStream stream = post.getResponseBodyAsStream();
		OAPMCReader reader = new OAPMCReader(stream);
		return reader.getPDFURL();
	}

	@Override
	public void validateConfiguration(IIRSearchConfiguration configuration) throws InvalidConfigurationException {
		
	}

	@Override
	public IProcessType getType() {
		return new ProcessTypeImpl(-1,"IRCrawl");
	}

	@Override
	public IProcessOrigin getProcessOrigin() {
		return new ProcessOriginImpl(-1, "IR Crawl");
	}

	public long getID() {
		return 0;
	}


	@Override
	public void stop() {
		cancel = true;		
	}



	@Override
	public void setTimeProgress(ISimpleTimeLeft progress) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRange(Integer startRange, Integer endRAnge, Long startTime) {
		// TODO Auto-generated method stub
		
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

	
}
