
package com.yahoo.robot.components;

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
	
	public static String now() {
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    return sdf.format(cal.getTime());
	  }
	
	public static void main(String[] args) 
	{
		System.out.println("Delta reporter running...");
		String report1file = "./Reports/May2012/Autos/consolidatedReport_2012_05_23_23_59_41.html";
		String report2file = "./Reports/May2012/Autos/consolidatedReport_2012_05_24_00_50_10.html";

		if ( args.length >= 2 )
		{
			report1file = args[0];
			report2file = args[1];
		}
		
		File deltaRepLocation = new File("./Reports/DeltaReporter/"+now()+"/");
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
		for ( String eachPassed : report1passed )
		{
			for ( String each2ndTimePassed : report2passed )
			{
				if ( each2ndTimePassed.compareToIgnoreCase(eachPassed) == 0 )
				{
					passedPassed.add(eachPassed);
					break;
				}
			}
		}
		
		// get passed-failed
		for ( String eachPassed : report1passed )
		{
			//check if present in report 2 failed
			for ( String eachFailed : report2failed )
			{
				if ( eachFailed.compareToIgnoreCase(eachPassed) == 0 )
				{
					passedFailed.add(eachPassed);
					break;
				}
			}
		}
		
		
		 //  get failed-then-passed
		for ( String eachFailed : report1failed )
		{
			for ( String eachPassedInSecondRun : report2passed )
			{
				if ( eachFailed.compareToIgnoreCase(eachPassedInSecondRun) == 0 )
				{
					failedPassed.add(eachFailed);                              
					break;
				}
			}
		}
		
		
		// get failed - then again failed
		for ( String eachFailed : report1failed )
		{
			for ( String eachFailedInSecondRun : report2failed )
			{
				if ( eachFailed.compareToIgnoreCase(eachFailedInSecondRun) == 0 )
				{
					failedFailed.add(eachFailed);
					break;
				}
			}
		}
		
		// find not repeated queries ===> report1Queries - report2Queries
		
		
		// find new Queries ==> report2Queries - report1Queries 
		
//		System.out.println(report1MaxLengthQuery + " , " + report2MaxLenghtQuery );
		
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
