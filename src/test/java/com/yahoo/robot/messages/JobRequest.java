package com.yahoo.robot.messages;

import java.util.Properties;

import com.yahoo.robot.components.interfaces.IJobRequest;

public class JobRequest implements IJobRequest
{
	private String request ;
	private String srcDataFileName; 
	private Properties properties ;
	private static int currentRequestNumber ;
	private int thisRequestNumber;
	
	
	/**
	 * 
	 * @param rq - url request
	 * @param aurl 
	 */
	public JobRequest(String rqURL, String srcConfigFileName, Properties props) 
	{
		srcDataFileName = srcConfigFileName;
		request = rqURL;
		properties = props;
		thisRequestNumber = currentRequestNumber++;
	}
	
	@Override
	public String getRequestURL() 
	{
		return request;
	}

	@Override
	public int getRequestNumber() 
	{
		return thisRequestNumber;
	}

	@Override
	public Properties getInputProperties() 
	{
		return this.properties;
	}

	public Properties getProperties() {
		return properties;
	}

	@Override
	public String getJobSourceConfigName()
	{
		return srcDataFileName;
	}
	
}
