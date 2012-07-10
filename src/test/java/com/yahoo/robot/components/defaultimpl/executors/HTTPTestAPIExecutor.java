package com.yahoo.robot.components.defaultimpl.executors;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.Cookie;

import com.yahoo.robot.components.interfaces.Executable;
import com.yahoo.robot.components.interfaces.IConstants;
import com.yahoo.robot.components.interfaces.IJobRequest;
import com.yahoo.robot.libs.URLHelper;
import com.yahoo.yqatestng.framework.wsmanager.Response;
import com.yahoo.yqatestng.framework.wsmanager.testapi.HTTPTestAPI;

public class HTTPTestAPIExecutor extends Executable 
{
	private static final String ACCEPT_COOKIES = "accept_cookies";
	private boolean acceptCookies = false;

	public static boolean firstRequest = true ;
	
	public static volatile long minTimeTaken = Long.MAX_VALUE ;
	public static volatile long maxTimeTaken = 0 ;
	public static volatile long tempTime = 0;
	
	@Override
	public Map<String, Object> execute(IJobRequest job) 
	{
		String acceptCookiesStr = job.getInputProperties().getProperty(ACCEPT_COOKIES);
		if ( null != acceptCookiesStr )
		{
			acceptCookies = acceptCookiesStr.equalsIgnoreCase("true")? true : false ; 
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String urlInputs = job.getRequestURL();
		String url = urlInputs.split(URLHelper.URL_PARAM_VALUE_SEPARATOR)[0];
		// String url = urlInputz.substring(0, urlInputz.indexOf(URLHelper.URL_PARAM_VALUE_SEPARATOR));
		String inputs = urlInputs.split(URLHelper.URL_PARAM_VALUE_SEPARATOR).length > 1 ? 
							urlInputs.split(URLHelper.URL_PARAM_VALUE_SEPARATOR)[1] : "No parameter values found";
		HTTPTestAPI client = new HTTPTestAPI(false);
		System.out.println( Thread.currentThread().getId() + " " + Thread.currentThread().getName() 
							+ "Testing : " + job.getRequestNumber() + " " + url);
		
		if ( firstRequest )
		{
			try {
				client.httpGet(url);
			} catch (Throwable e) {
			}
			firstRequest = false;
		}
		
		Response response = null;
		try {
			response = client.httpGet(url);
		} catch (Throwable t) {
			t.printStackTrace();
			System.out.println("ERROR : Could not get response from " + url);
			System.out.println(t.getMessage());
			resultMap.put(IConstants.STRING_URL, url);
			resultMap.put(IConstants.CLIENT_OBJECT, client);
			resultMap.put(IConstants.PROPERTIES_INPUTS, job.getInputProperties());
			resultMap.put(IConstants.RESPONSE_OBJECT, null);
			resultMap.put(IConstants.STRING_INPUT_PARAM_VALUES, inputs);
			resultMap.put( IConstants.JOB_REQUEST, job);
			return resultMap;
		}
		
		if ( response.getResponseTime() > maxTimeTaken )
		{
			maxTimeTaken = response.getResponseTime();
		}
		if ( response.getResponseTime() < minTimeTaken )
		{
			minTimeTaken = response.getResponseTime();
		}
		tempTime = tempTime + response.getResponseTime();
		
		if ( acceptCookies )
		{
			response.logCookies();
			Cookie[] cookies = response.getCookies() ; //getCookies();
			for ( Cookie cookie : cookies )
			{
				client.addCookie(cookie);
			}
		}
		
		resultMap.put(IConstants.STRING_URL, url);
		resultMap.put(IConstants.CLIENT_OBJECT, client);
		resultMap.put(IConstants.PROPERTIES_INPUTS, job.getInputProperties());
		resultMap.put(IConstants.RESPONSE_OBJECT, response);
		resultMap.put(IConstants.STRING_INPUT_PARAM_VALUES, inputs);
		resultMap.put( IConstants.JOB_REQUEST, job);
		return resultMap;
	}

}
