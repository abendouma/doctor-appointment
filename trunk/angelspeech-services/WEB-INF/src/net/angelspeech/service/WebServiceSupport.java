package net.angelspeech.service;

import javax.servlet.ServletContext;

import net.angelspeech.util.MessageResourcesManager;

public class WebServiceSupport {
	@javax.ws.rs.core.Context 
	protected ServletContext context;
	protected MessageResourcesManager messageResources;
	
}
