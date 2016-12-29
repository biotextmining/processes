package com.silicolife.textmining.processes.ir.patentpipeline.components.retrievalmodules.wipo.help;


import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class AuthenticatorPatentScopeWs extends Authenticator {
	PasswordAuthentication pw;
	
	public PasswordAuthentication getPasswordAuthentication () {
	return pw;
	}
	
	public void setPasswordAuthentication(final String user, final String password) {
		pw = new PasswordAuthentication(user, password.toCharArray());
	}
	
}
