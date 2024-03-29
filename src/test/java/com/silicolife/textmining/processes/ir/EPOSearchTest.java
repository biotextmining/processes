package com.silicolife.textmining.processes.ir;

import org.junit.Test;

import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationSourcesDefaultEnum;
import com.silicolife.textmining.core.datastructures.utils.Utils;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.processes.ir.epopatent.OPSUtils;
import com.silicolife.textmining.utils.http.exceptions.ClientErrorException;
import com.silicolife.textmining.utils.http.exceptions.ConnectionException;
import com.silicolife.textmining.utils.http.exceptions.RedirectionException;
import com.silicolife.textmining.utils.http.exceptions.ResponseHandlingException;
import com.silicolife.textmining.utils.http.exceptions.ServerErrorException;

public class EPOSearchTest {
	
	private static String accessTokenEPO = "access";
	
	

	@Test
	public void updatePublication() throws RedirectionException, ClientErrorException, ServerErrorException, ConnectionException, ResponseHandlingException {
		IPublication publiction = new PublicationImpl();
		String autentication = Utils.get64Base(accessTokenEPO);
		String tokenaccess = null;
		try {
			tokenaccess = OPSUtils.postAuth(autentication);
		} catch (RedirectionException | ClientErrorException| ServerErrorException | ConnectionException
				| ResponseHandlingException e) {
			tokenaccess = null;
		}
		String patentID = "	EP2746277";
		OPSUtils.updatePatentMetaInformation(tokenaccess, publiction, patentID,true);
		System.out.println(publiction.getTitle());
		System.out.println(publiction.getAuthors());
		System.out.println(publiction.getAbstractSection());
		System.out.println(publiction.getYeardate());
		System.out.println(PublicationImpl.getPublicationExternalIDForSource(publiction, PublicationSourcesDefaultEnum.patent.name()));
		System.out.println(publiction.getNotes());
		System.out.println(publiction.getPublicationLabels());

	}

}
