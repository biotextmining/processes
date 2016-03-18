package com.silicolife.textmining.processes.corpora.loaders.pdf.utils;

//import java.io.IOException;
//import java.io.InputStream;
//import java.net.MalformedURLException;
//import java.net.Proxy.Type;
//import java.net.URL;
//import java.net.URLConnection;
//
//import javax.xml.parsers.ParserConfigurationException;
//import javax.xml.xpath.XPathExpressionException;
//
//import org.apache.commons.httpclient.HttpClient;
//import org.apache.commons.httpclient.NameValuePair;
//import org.apache.commons.httpclient.cookie.CookiePolicy;
//import org.apache.commons.httpclient.methods.PostMethod;
//import org.apache.commons.io.IOUtils;
//import org.apache.http.conn.params.ConnRoutePNames;
//import org.xml.sax.SAXException;
//
//import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
//import com.silicolife.textmining.core.interfaces.core.document.IPublication;
//import com.silicolife.textmining.core.interfaces.process.IR.exception.InternetConnectionProblemException;
//
////import es.uvigo.ei.aibench.settings.AIBenchProxy;
//
///**
// * Using CrossRef
// * 
// * @author Hugo Costa
// *
// */
//public class CrossRefDOIMetaDataFinder {
//	
//
//	public static IPublication getDocumentMetaData(String doi,String pid) throws InternetConnectionProblemException
//	{
//		HttpClient client = getHTTPGeneralConfiguration();
//		PostMethod post = configurationHttp(doi,pid);
//		return getPubmedResults(client,post);
//	}
//	
//
//	private static IPublication getPubmedResults(HttpClient client,PostMethod post) throws InternetConnectionProblemException{
//		try {
//			client.executeMethod(post);
//			return readXMLResultFile(post);
//		} catch (IOException | XPathExpressionException | SAXException | ParserConfigurationException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	private static HttpClient getHTTPGeneralConfiguration() {
//		HttpClient client = new HttpClient();
//		client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
//		if(InitConfiguration.getProxy()!=null && !InitConfiguration.getProxy().type().equals(Type.DIRECT))
//			client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,InitConfiguration.getProxy());
//		return client;
//	}
//	
//	private static PostMethod configurationHttp(String doi, String pid) {
//
//		PostMethod post = new PostMethod(
//				"http://doi.crossref.org/servlet/query/");
//
//		NameValuePair[] data = { 
//				new NameValuePair("pid", pid),
//				new NameValuePair("format", "unixsd"),
//				new NameValuePair("id", doi)
//
//		};
//		post.setRequestBody(data);
//		return post;
//	}
//	
//	public static IPublication readXMLResultFile(PostMethod post) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException{
//		InputStream stream = post.getResponseBodyAsStream();
//		String myString = IOUtils.toString(stream, "UTF-8");
//		return null;
//	}
//	
//	public static URL getRedirectURL(String url) throws MalformedURLException, IOException
//	{
//		URLConnection con = new URL( url ).openConnection();
//		con.connect();
//		InputStream is = con.getInputStream();
//		URL result =  con.getURL();
//		is.close();
//		return result;
//	}
//	
//	
//	public static void main(String[] args) throws InternetConnectionProblemException, IOException {
//		String pid = "hcosta@di.uminho.pt";
//		String doi = "10.1073/pnas.1102255108";
//		CrossRefDOIMetaDataFinder.getDocumentMetaData(doi, pid);
////		String url = "http://linkinghub.elsevier.com/retrieve/pii/S0300908404001324";
////		URL redirect = getRedirectURL(url);
//
//	}
//	
//}
