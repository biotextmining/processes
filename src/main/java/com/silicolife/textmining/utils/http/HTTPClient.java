package com.silicolife.textmining.utils.http;


import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import com.silicolife.textmining.utils.http.exceptions.ClientErrorException;
import com.silicolife.textmining.utils.http.exceptions.ConnectionException;
import com.silicolife.textmining.utils.http.exceptions.RedirectionException;
import com.silicolife.textmining.utils.http.exceptions.ResponseHandlingException;
import com.silicolife.textmining.utils.http.exceptions.ServerErrorException;

public class HTTPClient {
	
	protected static final String POST = "POST";
	protected static final String GET = "GET";
	
	protected boolean followRedirection;
	protected int readTimeout;
	
	public HTTPClient() {
		this.followRedirection = true;
		this.readTimeout = 20000;
	}
	
	public void setTimeout(int readTimeoutMiliseconds)
	{
		this.readTimeout = readTimeoutMiliseconds;
	}
	
	public <T> T get(String url, Map<String, String> headers, ResponseHandler<T> responseHandler) throws ConnectionException, RedirectionException, ClientErrorException, ServerErrorException, ResponseHandlingException  {
		HttpURLConnection conn = null;
		try {
			URL u = new URL(url);
			conn = (HttpURLConnection) u.openConnection();
			conn.setRequestMethod(GET);
			conn.setReadTimeout(readTimeout);
			conn.setConnectTimeout(readTimeout);
			for(String key : headers.keySet())
				conn.setRequestProperty(key, headers.get(key));
			
			return handleResponse(conn, responseHandler);
		}
		
		catch (IOException e) {
			throw new ConnectionException(e);
		}
		catch (RedirectionException e) {
			if(!followRedirection)
				throw e;
			
			if(e.getStatus() == 302 || e.getStatus() == 303) {
				String location = conn.getHeaderField("Location");
				if(location == null)
					location = conn.getHeaderField("location");
				if(location == null)
					location = e.getLocation();
				if(location == null)
					throw e;
				return get(url, headers, responseHandler);
			}
			else
				throw e;
		}
	}
	
	public <T> T post(String url, String body, Map<String, String> headers, ResponseHandler<T> responseHandler) throws ConnectionException, RedirectionException, ClientErrorException, ServerErrorException, ResponseHandlingException {
		HttpURLConnection conn = null;
		try {
			URL u = new URL(url);
			conn = (HttpURLConnection) u.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setReadTimeout(readTimeout);
			conn.setConnectTimeout(readTimeout);
			conn.setRequestMethod(POST);
			conn.setInstanceFollowRedirects(false);
			conn.setUseCaches(false);
			for(String key : headers.keySet())
				conn.setRequestProperty(key, headers.get(key));
			if(body!=null && !body.isEmpty())
			{
				OutputStream os = conn.getOutputStream();
				os.write(body.getBytes());
				os.flush();
			}
			return handleResponse(conn, responseHandler);
		}
		
		catch (IOException e) {
			throw new ConnectionException(e);
		}
		catch (RedirectionException e) {
			if(!followRedirection)
				throw e;
			
			if(e.getStatus() == 302) {
				String location = conn.getHeaderField("Location");
				if(location == null)
					location = conn.getHeaderField("location");
				if(location == null)
					location = e.getLocation();
				if(location == null)
					throw e;
				return get(url, headers, responseHandler);
			}
			else
				throw e;
		}
	}



	protected <T> T handleResponse(HttpURLConnection conn, ResponseHandler<T> responseHandler) throws IOException, RedirectionException, ClientErrorException, ServerErrorException, ResponseHandlingException {
		int status = conn.getResponseCode();
		if(status >= 200 && status < 300)
			return responseHandler.buildResponse(conn.getInputStream(), conn.getResponseMessage(), conn.getHeaderFields(), status);
		else if(status >= 300 && status < 400)
			throw new RedirectionException(status, conn.getResponseMessage());
		else if(status >= 400 && status < 500)
			throw new ClientErrorException(status, conn.getResponseMessage());
		else
			throw new ServerErrorException(status, conn.getResponseMessage());
		
		
	}
	

}
