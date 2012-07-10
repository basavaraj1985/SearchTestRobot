package com.yahoo.robot.libs;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.yahoo.robot.components.interfaces.IConstants;
import com.yahoo.robot.components.interfaces.IJobRequest;
import com.yahoo.robot.control.SearchTestRobot;

/**
 * @author basavar
 *
 * To use the scraping functionality, pre-requisite is to use the HTMLUnitExecutor.
 * And then invoke the scraping api, as and when required.
 * 
 * The input data properties file can provide list of xpaths to scrape
 */
public class HTMLScraper 
{
	/**
	 * The validationParameters is a map provided to validators by executors.
	 * Used here to determine the extract file location and name.
	 * @param validationParameters
	 */
	public static void scrapeOut(Map<String, Object> validationParameters)
	{
		IJobRequest job = (IJobRequest) validationParameters.get(IConstants.JOB_REQUEST);
		Properties inputProps = (Properties) validationParameters.get(IConstants.PROPERTIES_INPUTS);
		HtmlPage htmlPage = (HtmlPage) validationParameters.get(IConstants.RESPONSE_OBJECT);	
		String xpaths = (String) inputProps.get(IConstants.SCRAPE_HTML_ELEMENTS);
		
		if ( null == htmlPage )
		{
			System.out.println("NULL html page, cant scrape anything out!");
			System.err.println("NULL html page, cant scrape anything out!");
			return;
		}
		if ( null == xpaths || ( null != xpaths && xpaths.trim().length() < 1 ))
		{
			System.out.println("ERROR: Scrape xpaths are not configured in input data properties file!");
			return;
		}
		String[] listOfHTMLElementsToGet = xpaths.split(URLHelper.VALUE_SEPARATOR);
		try {
			BufferedWriter writer = new BufferedWriter( new FileWriter(SearchTestRobot.controllerProperties.getProperty(
						SearchTestRobot.LOG_DIRECTORY) + job.getJobSourceConfigName().split("\\.")[0] + "_" + URLHelper.NOW_TIME +"/extract.txt" , true));

			
			for ( String eachHTMLElemeString : listOfHTMLElementsToGet )
			{
				List<HtmlDivision> htmlSection = (List<HtmlDivision>) htmlPage.getByXPath(eachHTMLElemeString);
				for ( HtmlDivision division : htmlSection )
				{
					writer.write(division.asXml());
					writer.write("<br>");
				}
			}
			writer.flush();
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * To append some elements/text in the extract output
	 * @param validationParameters
	 * @param toAppend
	 */
	public static void scrapeOutAppend(Map<String, Object> validationParameters, String toAppend)
	{
		IJobRequest job = (IJobRequest) validationParameters.get(IConstants.JOB_REQUEST);
		try {
			BufferedWriter writer = new BufferedWriter( new FileWriter(SearchTestRobot.controllerProperties.getProperty(
						SearchTestRobot.LOG_DIRECTORY) + job.getJobSourceConfigName().split("\\.")[0] + "_" + URLHelper.NOW_TIME +"/extract.txt" , true));
			writer.write(toAppend);
			writer.write("<br>");
			writer.flush();
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
