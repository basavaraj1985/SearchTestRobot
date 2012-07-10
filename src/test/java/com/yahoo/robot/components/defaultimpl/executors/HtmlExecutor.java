package com.yahoo.robot.components.defaultimpl.executors;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.yahoo.robot.components.interfaces.Executable;
import com.yahoo.robot.components.interfaces.IConstants;
import com.yahoo.robot.components.interfaces.IJobRequest;
import com.yahoo.robot.libs.URLHelper;

public class HtmlExecutor extends Executable
{
	
	// Create and initialize WebClient object
    public static WebClient webClient = new WebClient(BrowserVersion.FIREFOX_3_6);
    
	public HtmlExecutor() 
	{
		webClient.setJavaScriptEnabled(false);
	}

	 public Map<String,Object> execute(IJobRequest job)
	 {
		 Map<String,Object> exeMap = new HashMap<String, Object>();
		 
		 String requestUrl = job.getRequestURL();
         String[] urlList = requestUrl.split(URLHelper.URL_PARAM_VALUE_SEPARATOR);
         String url = urlList[0];
         String query = urlList[1];
         //String query = url.split(URLHelper.URL_PARAM_VALUE_SEPARATOR)[1];
         exeMap.put( IConstants.STRING_URL, url);
         exeMap.put( IConstants.PROPERTIES_INPUTS, job.getInputProperties());
         exeMap.put( IConstants.QUERY, query);
         exeMap.put( IConstants.JOB_REQUEST, job);

		 try 
		 {
			HtmlPage currentPage = (HtmlPage)webClient.getPage(url);
			exeMap.put( IConstants.RESPONSE_OBJECT, currentPage);
		 }
		 catch (FailingHttpStatusCodeException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		 return exeMap;
	 }
	
}
