package com.yahoo.robot.messages;

import com.yahoo.robot.components.interfaces.IJobRequest;
import com.yahoo.robot.components.interfaces.IValidationResponse;

public class DefaultValidationResponse implements IValidationResponse 
{
	IJobRequest requestJob;
	String errorComponent;
	String failReason ;
	String query ;
	String URL ; 
	boolean isSuccess = true;
	StringBuffer actualResponse = new StringBuffer();
	
	@Override
	public String getFailReason()
	{
		return failReason;
	}

	@Override
	public String getQuery() 
	{
		return query;
	}

	@Override
	public String getURL() 
	{
		return URL;
	}

	@Override
	public boolean isSuccesful() 
	{
		return isSuccess;
	}

	@Override
	public void setFailReason(String reason) 
	{
		failReason = reason;
	}

	@Override
	public void setQuery(String query) 
	{
		this.query = query;
	}

	@Override
	public void setURL(String url) 
	{
		this.URL = url;
	}

	@Override
	public void setIsSuccessful(boolean isSuccess) 
	{
		this.isSuccess = isSuccess;
	}

	@Override
	public String getErrorComponent() 
	{
		return errorComponent;
	}

	@Override
	public void setErrorComponent(String ec) 
	{
		errorComponent = ec;
	}

	@Override
	public IJobRequest getRequestJob() 
	{
		return requestJob;
	}
	
	@Override
	public void setRequestJob(IJobRequest req) 
	{
		requestJob = req;
	}

	@Override
	public StringBuffer getActualResponseAsBuffer() 
	{
		return actualResponse;
	}

	@Override
	public void setActualResponseAsBuffer(StringBuffer buffer) 
	{
		actualResponse = buffer;
	}
}
