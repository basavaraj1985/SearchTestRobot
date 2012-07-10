
package com.yahoo.robot.control;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;

import com.yahoo.robot.libs.FileUtils;

/**
 * @author basavar
 *
 */
public class ResultAnalyzer 
{
	public static String REPORT_TEMPLATE = "./conf/DeltaReport.html";
	public static File deltaRepLocation = new File("./Reports/DeltaReporter/"+now()+"/");
	
	public static final String DYN_TEMPLATE_REPORT1_FILE_LOCATION = "$report1";
	public static final String DYN_TEMPLATE_REPORT2_FILE_LOCATION = "$report2";

	public static final String DYN_TEMPLATE_REPORT1_TOTAL_COUNT = "$report1.totalCount";
	public static final String DYN_TEMPLATE_REPORT1_PASS_COUNT = "$report1.passCount";
	public static final String DYN_TEMPLATE_REPORT1_FAIL_COUNT = "$report1.failCount";
	
	public static final String DYN_TEMPLATE_REPORT2_TOTAL_COUNT = "$report2.totalCount";
	public static final String DYN_TEMPLATE_REPORT2_PASS_COUNT = "$report2.passCount";
	public static final String DYN_TEMPLATE_REPORT2_FAIL_COUNT = "$report2.failCount";
	
	public static final String DYN_TEMPLATE_PASS_2_PASS_COUNT = "$passToPassCount";
	public static final String DYN_TEMPLATE_PASS_2_FAIL_COUNT = "$passToFailCount";
	public static final String DYN_TEMPLATE_FAIL_2_PASS_COUNT = "$failToPassCount";
	public static final String DYN_TEMPLATE_FAIL_2_FAIL_COUNT = "$failToFailCount";
	
	public static final String DYN_TEMPLATE_PASS_2_PASS_LIST = "$passToPassList";
	public static final String DYN_TEMPLATE_PASS_2_FAIL_LIST = "$passToFailList";
	public static final String DYN_TEMPLATE_FAIL_2_PASS_LIST = "$failToPassList";
	public static final String DYN_TEMPLATE_FAIL_2_FAIL_LIST = "$failToFailList";

	public static String now() {
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    return sdf.format(cal.getTime());
	  }
	
	public static void main(String[] args) 
	{
		System.out.println("Delta reporter running...");
		String report1file = "./Reports/May2012/Autos/consolidatedReport_2012_05_23_23_59_41.html";
		String report2file = "./Reports/May2012/Autos/consolidatedReport_2012_05_24_02_22_02.html";
		
		if ( args.length >= 2 )
		{
			report1file = args[0];
			report2file = args[1];
		}
		
		deltaRepLocation.mkdirs();
		
		File logFile = new File (deltaRepLocation.getAbsolutePath() + "/Report.txt" );
		try {
			System.setOut(new PrintStream( logFile ));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		System.out.println("Report 1 : " + report1file );
		System.out.println("Report 2 : " + report2file );
		
		Properties report1 = FileUtils.loadFileIntoProperties(report1file);
		Properties report2 = FileUtils.loadFileIntoProperties(report2file);
		
		String[] report1passed = report1.getProperty("passList").split(",,");
		String[] report2passed = report2.getProperty("passList").split(",,");
		String[] report1failed = report1.getProperty("failList").split(",,");
		String[] report2failed = report2.getProperty("failList").split(",,");
		
		ArrayList<String> passedPassed = new ArrayList<String>();
		ArrayList<String> passedFailed = new ArrayList<String>();
		ArrayList<String> failedPassed = new ArrayList<String>();
		ArrayList<String> failedFailed = new ArrayList<String>();
		
		// get passed-passed
		StringBuffer passedPassedListBuffer = new StringBuffer();
		for ( String eachPassed : report1passed )
		{
			for ( String each2ndTimePassed : report2passed )
			{
				if ( each2ndTimePassed.compareToIgnoreCase(eachPassed) == 0 )
				{
					passedPassed.add(eachPassed);
					passedPassedListBuffer.append(eachPassed);
					passedPassedListBuffer.append("<br>");
					break;
				}
			}
		}
		
		if ( passedPassed.size() == 0 )
		{
			passedPassedListBuffer.append("[NONE]");
			passedPassedListBuffer.append("<br>");
		}
		
		// get passed-failed
		StringBuffer passedFailedListBuffer = new StringBuffer();
		for ( String eachPassed : report1passed )
		{
			//check if present in report 2 failed
			for ( String eachFailed : report2failed )
			{
				if ( eachFailed.compareToIgnoreCase(eachPassed) == 0 )
				{
					passedFailed.add(eachPassed);
					passedFailedListBuffer.append(eachPassed);
					passedFailedListBuffer.append("<br>");
					break;
				}
			}
		}
		
		if ( passedFailed.size() == 0 )
		{
			passedFailedListBuffer.append("[NONE]");
			passedFailedListBuffer.append("<br>");
		}
		
		 //  get failed-then-passed
		StringBuffer failedPassedListBuffer = new StringBuffer();
		for ( String eachFailed : report1failed )
		{
			for ( String eachPassedInSecondRun : report2passed )
			{
				if ( eachFailed.compareToIgnoreCase(eachPassedInSecondRun) == 0 )
				{
					failedPassed.add(eachFailed);    
					failedPassedListBuffer.append(eachFailed);
					failedPassedListBuffer.append("<br>");
					break;
				}
			}
		}
		
		if ( failedPassed.size() == 0 )
		{
			failedPassedListBuffer.append("[NONE]");
			failedPassedListBuffer.append("<br>");
		}
		
		
		// get failed - then again failed
		StringBuffer failedFailedListBuffer = new StringBuffer();
		for ( String eachFailed : report1failed )
		{
			for ( String eachFailedInSecondRun : report2failed )
			{
				if ( eachFailed.compareToIgnoreCase(eachFailedInSecondRun) == 0 )
				{
					failedFailed.add(eachFailed);
					failedFailedListBuffer.append(eachFailed);
					failedFailedListBuffer.append("<br>");
					break;
				}
			}
		}
		
		if ( failedFailed.size() == 0 )
		{
			failedFailedListBuffer.append("[NONE]");
			failedFailedListBuffer.append("<br>");
		}
		// find not repeated queries ===> report1Queries - report2Queries
		
		
		// find new Queries ==> report2Queries - report1Queries 
		
//		System.out.println(report1MaxLengthQuery + " , " + report2MaxLenghtQuery );
		
		StringBuffer reportTemplate = FileUtils.readFileAsBuffer(REPORT_TEMPLATE);
		String reportToWrite = reportTemplate.toString();
		
		reportToWrite = reportToWrite.replace(DYN_TEMPLATE_REPORT1_TOTAL_COUNT, String.valueOf( report1passed.length + report1failed.length ) );
		reportToWrite = reportToWrite.replace(DYN_TEMPLATE_REPORT2_TOTAL_COUNT, String.valueOf( report2passed.length + report2failed.length ) );
		
		reportToWrite = reportToWrite.replace(DYN_TEMPLATE_REPORT1_PASS_COUNT, String.valueOf( report1passed.length ) );
		reportToWrite = reportToWrite.replace(DYN_TEMPLATE_REPORT1_FAIL_COUNT, String.valueOf( report1failed.length ) );
		
		reportToWrite = reportToWrite.replace(DYN_TEMPLATE_REPORT2_PASS_COUNT, String.valueOf( report2passed.length ) );
		reportToWrite = reportToWrite.replace(DYN_TEMPLATE_REPORT2_FAIL_COUNT, String.valueOf( report2failed.length ) );
		
		reportToWrite = reportToWrite.replace(DYN_TEMPLATE_PASS_2_PASS_COUNT, String.valueOf( passedPassed.size() ) );
		reportToWrite = reportToWrite.replace(DYN_TEMPLATE_PASS_2_FAIL_COUNT, String.valueOf( passedFailed.size() ) );
		reportToWrite = reportToWrite.replace(DYN_TEMPLATE_FAIL_2_PASS_COUNT, String.valueOf( failedPassed.size() ) );
		reportToWrite = reportToWrite.replace(DYN_TEMPLATE_FAIL_2_FAIL_COUNT, String.valueOf( failedFailed.size() ) );
		
		reportToWrite = reportToWrite.replace(DYN_TEMPLATE_PASS_2_PASS_LIST, passedPassedListBuffer.toString() );
		reportToWrite = reportToWrite.replace(DYN_TEMPLATE_PASS_2_FAIL_LIST, passedFailedListBuffer.toString() );
		reportToWrite = reportToWrite.replace(DYN_TEMPLATE_FAIL_2_FAIL_LIST, failedFailedListBuffer.toString() );
		reportToWrite = reportToWrite.replace(DYN_TEMPLATE_FAIL_2_PASS_LIST, failedPassedListBuffer.toString() );
		
		reportToWrite = reportToWrite.replace(DYN_TEMPLATE_REPORT1_FILE_LOCATION, new File(report1file).getAbsolutePath());
		reportToWrite = reportToWrite.replace(DYN_TEMPLATE_REPORT2_FILE_LOCATION, new File(report2file).getAbsolutePath());
		
		FileUtils.writeToFile( deltaRepLocation.getAbsolutePath() + "/DeltaReport.html", reportToWrite);
		
		// Report detailed
		System.out.println("Report 1 has " + ( report1failed.length + report1passed.length ) + " queries.");
		System.out.println("Report 2 has " + ( report2failed.length + report2passed.length ) + " queries.");
		System.out.println("----------------------------------------------------------------------------------------------------------------");
		System.out.println();
		if ( passedFailed.size() > 0 )
		{
			System.out.println("These " + passedFailed.size() + " query/ies passed in report 1 but failed in report 2 ( PASS --> FAIL )");
			for ( String inducedFails : passedFailed )
			{
				System.out.println(inducedFails);
			}
			System.out.println("\n");
		}

		if ( failedPassed.size() > 0 )
		{
			System.out.println("These " + failedPassed.size() + " query/ies failed in report 1 but passed in report 2 ( FAIL --> PASS ) ");
			for ( String passedInSecond : failedPassed )
			{
				System.out.println(passedInSecond);
			}
			System.out.println("\n");
		}
		
		if ( passedPassed.size() > 0 )
		{
			System.out.println("These " + passedPassed.size() + " query/ies passed in both runs ( PASS --> PASS ): ");
			for ( String passedAlways : passedPassed )
			{
				System.out.println(passedAlways);
			}
			System.out.println("\n");
		}
		
		if ( failedFailed.size() > 0 )
		{
			System.out.println("These " + failedFailed.size() + " query/ies failed in both runs ( FAIL --> FAIL )  : ");
			for( String failedAlways : failedFailed )
			{
				System.out.println(failedAlways);
			}
//			System.out.println("\n");
		}
		
		System.out.println("----------------------------------------------------------------------------------------------------------------");
		System.out.println("Report 1 : " + report1file );
		System.out.println("Report 2 : " + report2file );
		System.out.println();
		System.out.println("++++++++++++++++++++");
		System.out.println("   RUN1     RUN2  ");
		System.out.println("    " + report1passed.length + "P       " + passedPassed.size() + "P");
		System.out.println("             " + passedFailed.size() + "F ");
		System.out.println("    " + report2failed.length + "F       " + failedFailed.size() + "F");
		System.out.println("             " + failedPassed.size() + "P ");
		System.out.println("++++++++++++++++++++");
		System.out.println();

		System.out.println("**************************R E P O R T**************************");
		System.out.println("% PASS then PASS again - " + ( (float)passedPassed.size() / (float) report1passed.length ) * 100  + "(" + passedPassed.size() + ")") ;
		System.out.println("% PASS then FAIL       - " + ( (float)passedFailed.size() / (float) report1passed.length ) * 100  + "(" + passedFailed.size() + ")") ;
		System.out.println();
		System.out.println("% FAIL then PASS       - " + ( (float)failedPassed.size() / (float) report1failed.length ) * 100 + "(" + failedPassed.size() + ")") ;
		System.out.println("% FAIL then FAIL again - " + ( (float)failedFailed.size() / (float) report1failed.length ) * 100 + "(" + failedFailed.size() + ")") ;
		System.out.println("**************************R E P O R T**************************");
		
	}

}
