package com.silicolife.textmining.processes.ir;

import org.junit.Test;

import com.silicolife.http.exceptions.ClientErrorException;
import com.silicolife.http.exceptions.ConnectionException;
import com.silicolife.http.exceptions.RedirectionException;
import com.silicolife.http.exceptions.ResponseHandlingException;
import com.silicolife.http.exceptions.ServerErrorException;
import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationSourcesDefaultEnum;
import com.silicolife.textmining.core.datastructures.utils.Utils;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.processes.ir.epopatent.OPSUtils;

public class EPOSearchTest {

	@Test
	public void test() throws RedirectionException, ClientErrorException, ServerErrorException, ConnectionException, ResponseHandlingException {
		IPublication publiction = new PublicationImpl();
		String accessTokenEPO = "LLCAsGwQHRQAi9sKU3L83tMcKszoVnhi:q9sxdjCvGbLDsWrc";
		String autentication = Utils.get64Base(accessTokenEPO);
		String tokenaccess = null;
		try {
			tokenaccess = OPSUtils.postAuth(autentication);
		} catch (RedirectionException | ClientErrorException| ServerErrorException | ConnectionException
				| ResponseHandlingException e) {
			tokenaccess = null;
		}
		String patentID = "CN102153611";
		OPSUtils.updatePatentMetaInformation(tokenaccess, publiction, patentID);
		System.out.println(publiction.getTitle());
		System.out.println(publiction.getAuthors());
		System.out.println(publiction.getAbstractSection());
		System.out.println(publiction.getYeardate());
		System.out.println(PublicationImpl.getPublicationExternalIDForSource(publiction, PublicationSourcesDefaultEnum.patent.name()));

	}

}
