/**
 * @author basavar
 * 
 * 
 * 
 */
package com.yahoo.robot.control;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.yahoo.robot.components.QueueManager;
import com.yahoo.robot.components.defaultimpl.executors.HTTPTestAPIExecutor;
import com.yahoo.robot.components.defaultimpl.executors.HtmlExecutor;
import com.yahoo.robot.components.defaultimpl.reporters.DefaultReporter;
import com.yahoo.robot.components.interfaces.IConstants;
import com.yahoo.robot.components.interfaces.IJobRequest;
import com.yahoo.robot.components.interfaces.Reportable;
import com.yahoo.robot.libs.FileUtils;
import com.yahoo.robot.libs.URLHelper;
import com.yahoo.robot.messages.JobRequest;

public class SearchTestRobot 
{

	public static final String LOG_DIRECTORY = "logDirectory";
	public static final String ACCEPT_COOKIES = "accept_cookies";    
	public static final String WRITE_RESPONSE_HTML_FILES = "writeFiles";
	public static final String WRITE_ONLY_FAILED_RQ_HTMLS_FILES = "writeOnlyFailed";
	
	public static final String URL_INPUT_FILES = "urlinputFiles";
	public static final String DD_VALIDATOR_CONFIG_FILE = "DDValidatorConfigFile";
	
	private Properties properties;
	private String controllerFile ; 
	
	public static Properties controllerProperties;
	
	public SearchTestRobot(String cntrFile) 
	{
		controllerFile = cntrFile;
	}
	
	public static void main(String[] args) 
	{
		if ( args.length <1 )
		{
			System.err.println("Usage : java -cp classpath com.yahoo.robot.vis.traveldd.tests.TestTravelDD TestController.properties ");
			System.exit(0);
		}
		System.out.println("Search test robot, starting to test..");
		SearchTestRobot testTravelDD = new SearchTestRobot(args[0]);
		testTravelDD.runTest();
	}
	

	public void runTest() 
	{
		controllerProperties = FileUtils.loadFileIntoProperties(controllerFile);
		
		String[] urlFilePaths = controllerProperties.getProperty(URL_INPUT_FILES).trim().split(",");
		for ( String urlFilePath : urlFilePaths )
		{
			URLHelper urlHelper = new URLHelper(urlFilePath.trim());
			File dir = new File(controllerProperties.getProperty(LOG_DIRECTORY) + urlHelper.getConfigFileName().split("\\.")[0] + "_" + URLHelper.NOW_TIME +"/");
			dir.mkdirs();
			
			File failDirectory = new File(dir.getAbsolutePath() + "/failedQueries/" );
			failDirectory.mkdirs();
			File passDirectory = new File( dir.getAbsolutePath() + "/passedQueries/" );
			passDirectory.mkdirs();
			
			File logFile = new File ( dir.getPath() +  "/Report.txt" );
			System.out.println("Logging in : " + logFile.getAbsolutePath());
			System.out.println("Snapshots in " + failDirectory.getAbsolutePath() + " & " + passDirectory.getAbsolutePath() );
			try {
				System.setOut(new PrintStream( logFile ));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			System.out.println("========================================================");
			System.out.println("Starting to test for : " + urlHelper.getConfigFileName() );
				
			List<String> allPossibleURLs = urlHelper.getAllPossibleURLs();
			QueueManager.Initialize(allPossibleURLs.size(), allPossibleURLs.size());

			System.out.println("There are " + allPossibleURLs.size() + " urls to test!");
			IJobRequest jobReq = null;
			for ( String aURL : allPossibleURLs )
			{
				System.out.println(aURL);
				jobReq = new JobRequest(aURL, urlHelper.getConfigFileName(), urlHelper.getProperties());
				QueueManager.addJobToQueue(jobReq);
			}
			
			ArrayBlockingQueue<Runnable> WORKERS_QUEUE = new ArrayBlockingQueue<Runnable>(20);
			ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 20, 1L, TimeUnit.SECONDS, WORKERS_QUEUE );
			int numberOfWorkers = URLHelper.getOptimumNumberOfThreads(allPossibleURLs.size());
			for ( int i = 0 ; i < numberOfWorkers ; i++ )
			{
				/*
				 * Here You can submit your own Executor which extends 'Executable' and implements execute()
				 */
				if ( null != urlHelper.getProperty(IConstants.EXECUTOR_TYPE) && 
						urlHelper.getProperty(IConstants.EXECUTOR_TYPE).trim().compareTo("0") == 0)
				{
					System.out.println("Using HTTPTestAPI executor!");
					executor.execute(new HTTPTestAPIExecutor());
				}
				else
				{
					System.out.println("Using HTMLUnit executor!");
					executor.execute(new HtmlExecutor());
				}
			}

			Reportable reportingWorker = new DefaultReporter(urlHelper.getConfigFileName().replace(".properties", ".html"), urlHelper);
			Thread reportingThread = new Thread(reportingWorker, "ReportingThread");
			reportingThread.setDaemon(true);
			reportingThread.setPriority(Thread.MAX_PRIORITY);
			reportingThread.start();

			executor.shutdown();
			while ( ! executor.isTerminated() || ! QueueManager.isValidationResponseQEmpty() )
			{
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			executor.shutdownNow();
			
			Reportable.setStopIt(true);
			try
			{
				/*
				 * Wait until reporting thread has completed
				 * its job
				 */
				reportingThread.join();
			} catch (InterruptedException e1)
			{
				e1.printStackTrace();
			}
		}
			
		}

	public static String now() 
	{
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	    return sdf.format(cal.getTime());
	  }
	
	public String getProperty(String key)
	{
		return properties.getProperty(key);
	}
	
	
}
