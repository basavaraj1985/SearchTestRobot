package com.yahoo.robot.components.interfaces;


public interface IValidationResponse 
{
	public boolean isSuccesful();
	
	public void setIsSuccessful(boolean isSuccess);
	
	public String getFailReason();
	
	public void setFailReason(String reason);
	
	public String getURL();
	
	public void setURL(String url);
	
	public String getQuery();
	
	public void setQuery(String query);
	
	public String getErrorComponent();
	
	public void setErrorComponent(String ec);
	
	public IJobRequest getRequestJob();
	
	public void setRequestJob(IJobRequest req);
	
	public void setActualResponseAsBuffer(StringBuffer buffer);
	
	public StringBuffer getActualResponseAsBuffer();
}
