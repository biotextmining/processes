package com.silicolife.textmining.processes.ir.epopatent;

import java.util.List;

import org.junit.Test;

import com.silicolife.textmining.core.datastructures.utils.Utils;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.utils.http.exceptions.ClientErrorException;
import com.silicolife.textmining.utils.http.exceptions.ConnectionException;
import com.silicolife.textmining.utils.http.exceptions.RedirectionException;
import com.silicolife.textmining.utils.http.exceptions.ResponseHandlingException;
import com.silicolife.textmining.utils.http.exceptions.ServerErrorException;

public class OPSUtilsMultiWord {

	private static String  accessTokenEPO = "accesstoken";

	@Test
	public void getSearch() throws RedirectionException, ClientErrorException, ServerErrorException, ConnectionException, ResponseHandlingException
	{
		String query = "(A AND B) OR (C OR \"D F\")";
		String token = getAccessToken();
		int step = 1;
		System.out.println(OPSUtils.getSearchResults(token, query));
		List<IPublication> result = OPSUtils.getSearch(token, query, step );
		System.out.println(result.size());
		for(IPublication item:result)
			System.out.println(item.toString());
	}

	private String getAccessToken() throws ConnectionException, RedirectionException, ClientErrorException,
	ServerErrorException, ResponseHandlingException {
		String autentication = Utils.get64Base(accessTokenEPO);
		String token = OPSUtils.postAuth(autentication);
		return token;
	}

}
