/**
 * @author basavar
 */
package com.yahoo.robot.components.interfaces;

public interface IConstants 
{
	/**
	 * For validator classes, to retrieve standard objects from framework
	 */
	
	/**
	 * ValidationParameters map will have this object, use this key to retrieve URL
	 */
	public static String STRING_URL = "URL";
	/**
	 * ValidationParameters map will have this object, use this key to retrieve Response object
	 */
	public static String RESPONSE_OBJECT = "Response";
	/**
	 * ValidationParameters map will have this object, use this key to retrieve properties
	 */
	public static String PROPERTIES_INPUTS = "properties";
	public static String CLIENT_OBJECT = "Client";
	public static String STRING_INPUT_PARAM_VALUES = "inputs";
	public static String QUERY = "Query";
	public static String JOB_REQUEST = "JobRequest";
	public static final String EXECUTOR_TYPE = "ExecutorType";
	
	/*
	 * For Reporting related
	 */
	public static String LIST_OF_PASSED = "allPasses";
	public static String LIST_OF_FAILED = "allFails";
	public static String LIST_OF_ERRORS = "allErrors";
	public static String ALL_QUERIES = "allQueries";
	public static String CONSOLIDATED_REPORT_FILE_NAME = "consolidatedReport_";
	
	/**
	 *  in controller properties file to control the number of executors
	 */
	public static String NUMBER_OF_THREADS = "FixedNumberOfThreads";
	
	/**
	 *  in data input properties, ",," separated value
	 */
	public static String MUST_PRESENT_TEXT_TOKENS = "MustPresentText";
	
	/**
	 *  in data input properties, list of xpaths to be scraped out to extract.txt file
	 */
	public static String SCRAPE_HTML_ELEMENTS = "ScrapeHTMLElementsList";
	
}
