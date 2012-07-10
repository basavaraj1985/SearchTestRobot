/**
 * @author basavar
 * 
 */
package com.yahoo.robot.components.defaultimpl.reporters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.yahoo.robot.components.interfaces.IConstants;
import com.yahoo.robot.components.interfaces.IValidationResponse;
import com.yahoo.robot.components.interfaces.Reportable;
import com.yahoo.robot.control.SearchTestRobot;
import com.yahoo.robot.libs.FileUtils;
import com.yahoo.robot.libs.URLHelper;

public class DefaultReporter extends Reportable 
{
	private File reportFile;
	private BufferedWriter reportWriter ;
	
	private static File consolidatedReportFile ;
	private BufferedWriter consolidatedReportWriter ;
	
	private File failDirectory;
	private File passDirectory;
	private StringBuffer templateBuffer = null;
	private URLHelper urlHelper ;
	
	static 
	{
		consolidatedReportFile = new File ( SearchTestRobot.controllerProperties.getProperty(SearchTestRobot.LOG_DIRECTORY) + "/" + IConstants.CONSOLIDATED_REPORT_FILE_NAME + URLHelper.NOW_TIME + ".html" );
	}

	/**
	 * 
	 * @param fileName
	 * @param urlHelper
	 * @param validationRespQ
	 * @param jobRequestQ
	 */
	public DefaultReporter( String fileName, URLHelper urlHelper ) 
	{
		this.urlHelper = urlHelper;
		templateBuffer = new StringBuffer() ;
		try {
			initializeFiles(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void initializeFiles(String htmlReportFile) throws IOException 
	{
		File dir = new File(SearchTestRobot.controllerProperties.getProperty(SearchTestRobot.LOG_DIRECTORY) + urlHelper.getConfigFileName().split("\\.")[0] + "_" + URLHelper.NOW_TIME +"/");
		dir.mkdirs();
		
		reportFile = new File(dir.getAbsolutePath() + "/" + htmlReportFile);

		failDirectory = new File(dir.getAbsolutePath() + "/failedQueries/" );
		failDirectory.mkdirs();
		passDirectory = new File( dir.getAbsolutePath() + "/passedQueries/" );
		passDirectory.mkdirs();
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(new File(Reportable.DEFAULT_REPORT_FORMAT)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("Could not read the configured default template : " + Reportable.DEFAULT_REPORT_FORMAT );
			System.exit(-1);
		}
		
		String line = null;
		while ( ( line = reader.readLine() ) != null )
		{
			templateBuffer.append(line);
			templateBuffer.append('\n');
		}
		
	}


	/**
	 * May be update a html file everytime ?
	 */
	@Override
	public void report(IValidationResponse response) 
	{
		if ( response.getRequestJob() != null && response.getRequestJob().getRequestNumber() != -1 )
		{
			System.out.println("Reporting for job - " + response.getRequestJob().getRequestNumber() );
		}
		
		StringBuffer summary = getSummaryAsHTMLTable( getPasses(), getFails() );
		StringBuffer statsTable = getStatsAsHTMLTable(response, getStatModelMap());
		
		StringBuffer consolidatedSummary = getSummaryAsHTMLTable( getConsolidatedPasses(), getConsolidatedFails() );
		StringBuffer consolidatedStatsTable = getStatsAsHTMLTable(response, getConsolidatedStatModelMap());
		
		String reportToWrite = templateBuffer.toString() ;
		reportToWrite = reportToWrite.replace(Reportable.SUMMARY_DYNAMIC_TEMPLATE, summary.toString());
		reportToWrite = reportToWrite.replace(Reportable.STATS_MODEL_DYNAMIC_TEMPLATE, statsTable.toString());
		reportToWrite = reportToWrite.replace(Reportable.PASS_LIST_DYNAMIC_TEMPLATE, Reportable.PASS_LIST_KEY + "=" + getListOfQueriesAsBuffer(getPasses()).toString());
		reportToWrite = reportToWrite.replace(Reportable.FAIL_LIST_DYNAMIC_TEMPLATE, Reportable.FAIL_LIST_KEY + "=" + getListOfQueriesAsBuffer(getFails()).toString());
		
		String consolidatedReportToWrite = templateBuffer.toString() ;
		consolidatedReportToWrite = consolidatedReportToWrite.replace(Reportable.SUMMARY_DYNAMIC_TEMPLATE, consolidatedSummary.toString());
		consolidatedReportToWrite = consolidatedReportToWrite.replace(Reportable.STATS_MODEL_DYNAMIC_TEMPLATE, consolidatedStatsTable.toString());
		consolidatedReportToWrite = consolidatedReportToWrite.replace(Reportable.PASS_LIST_DYNAMIC_TEMPLATE, Reportable.PASS_LIST_KEY + "=" + getListOfQueriesAsBuffer(getConsolidatedPasses()).toString());
		consolidatedReportToWrite = consolidatedReportToWrite.replace(Reportable.FAIL_LIST_DYNAMIC_TEMPLATE, Reportable.FAIL_LIST_KEY + "=" + getListOfQueriesAsBuffer(getConsolidatedFails()).toString());
		
		try {
			reportWriter = new BufferedWriter( new FileWriter(reportFile, false));
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Could not write report! " + e.getLocalizedMessage());
			System.exit(-1);
		}
		
		try {
			reportWriter.write(reportToWrite.toString());
			reportWriter.flush();
			reportWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			consolidatedReportWriter = new BufferedWriter( new FileWriter(consolidatedReportFile, false)); 
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Could not write report! " + e.getLocalizedMessage());
			System.exit(-1);
		}
		
		try {
			consolidatedReportWriter.write(consolidatedReportToWrite.toString());
			consolidatedReportWriter.flush();
			consolidatedReportWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		FileWriter writer = null;
		if ( urlHelper.getProperty(URLHelper.IS_WRITE_FILES) == null || 
				( 
				   urlHelper.getProperty(URLHelper.IS_WRITE_FILES) != null && 
						urlHelper.getProperty(URLHelper.IS_WRITE_FILES).compareToIgnoreCase("false") != 0 
				)
			)
		{
			/*
			 * Write response files, for success cases also,
			 * if the URL props input file has "writePassFiles=true"
			 */
			if ( response.isSuccesful() && urlHelper.getProperty(URLHelper.IS_WRITE_PASS_FILES) != null &&
					urlHelper.getProperty(URLHelper.IS_WRITE_PASS_FILES).compareToIgnoreCase("true") == 0 )
			{
				try {
					writer = new FileWriter(new File(passDirectory.getAbsolutePath() + "/"+ response.getQuery()+".html"));
					writer.write( response.getActualResponseAsBuffer().toString());
					writer.flush();
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			/*
			 * Else, 
			 * write only failure files
			 */
			else if ( ! response.isSuccesful() )
			{
				try {
					String fileName = response.getQuery();
					if ( null != fileName )
					{
						fileName = fileName.replaceAll(URLHelper.VALUE_SEPARATOR, "_");
					}
					writer = new FileWriter(new File(failDirectory.getAbsolutePath() + "/"+ fileName +".html"));
					writer.write( response.getActualResponseAsBuffer().toString());
					writer.flush();
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * @param list 
	 * @return
	 */
	private StringBuffer getListOfQueriesAsBuffer(List<IValidationResponse> list)
	{
		StringBuffer resulting = new StringBuffer();
		for ( IValidationResponse resp : list )
		{
			resulting.append(resp.getQuery());
//			resulting.append(",,");
		}
		return resulting;
	}

	private StringBuffer getStatsAsHTMLTable(IValidationResponse response, Map<String, List<IValidationResponse>> statModel) 
	{
		String row_start = "<tr>";
		String row_end = "</tr>";
		String col_start = "<td>";
		String col_end = "</td>";
		
		StringBuffer statsTable = new StringBuffer();
		statsTable.append(row_start);
		statsTable.append("<th>Failure Reason</th>");
		statsTable.append("<th>Count</th>");
		statsTable.append("<th>Component</th>");
		statsTable.append("<th>Cases</th>");
		statsTable.append(row_end);
		
		Set<Entry<String, List<IValidationResponse>>> entrySet = statModel.entrySet();
		for ( Entry<String, List<IValidationResponse>> entry : entrySet )
		{
			statsTable.append(row_start);
			statsTable.append(col_start);
			statsTable.append(entry.getKey());   //reason
			statsTable.append(col_end);
			statsTable.append(col_start);
			statsTable.append(entry.getValue().size()); // count
			statsTable.append(col_end);
			
			//component
			statsTable.append(col_start);
			String errorComponent = entry.getValue().get(0).getErrorComponent();
			statsTable.append(errorComponent);
			statsTable.append(col_end);
			
			//cases
			statsTable.append(col_start);
			if ( entry.getValue().size() > 5 )
			{
				String reason = entry.getKey();
				if ( reason.length() > 30 )
				{
					reason = reason.substring(0, 30).trim();
				}
				reason = getValidFileName(reason);
				
				StringBuffer toWrite = new StringBuffer();
				List<IValidationResponse> responses = entry.getValue();
				for ( IValidationResponse eachResp : responses )
				{
					toWrite.append('\n');
					toWrite.append(eachResp.getQuery());
					toWrite.append('\n');
				}
				
				File file = new File ( failDirectory.getAbsolutePath() + "/" + reason + ".txt");
				FileUtils.writeToFile( file, toWrite.toString(), false);
				statsTable.append("<a href=\"file://localhost"+file.getAbsolutePath() + "\">");
				statsTable.append(file.getName());
			}
			else 
			{
				List<IValidationResponse> responses = entry.getValue();
				for ( IValidationResponse eachResp : responses )
				{
					statsTable.append("<br>");
					statsTable.append(eachResp.getQuery());
					statsTable.append("</br>");
				}
			}
			statsTable.append(col_end);
			statsTable.append(row_end);
		}

		return statsTable;
	}

	/**
	 * Returns a manipulated string which has only english letters
	 * and '_'
	 * @param reason - input string
	 * @return
	 */
	private String getValidFileName(String reason)
	{
		char[] charArray = new char[reason.length()];
		reason.getChars(0, reason.length(), charArray, 0);
		for ( int i = 0 ; i < charArray.length ; i++ )
		{
			if ( ! Character.isLetter(charArray[i]))
			{
				charArray[i] = '_';
			}
		}
		String result = new String(charArray);
		return result;
	}

	/**
	 * Returns summary table as 
	 * html table 
	 * @param fails 
	 * @param passes 
	 * @return
	 */
	private StringBuffer getSummaryAsHTMLTable(List<IValidationResponse> passes, List<IValidationResponse> fails) 
	{
		String row_start = "<tr>";
		String row_end = "</tr>";
		String col_start = "<td>";
		String col_end = "</td>";
		
		// $summary
		// classification    :  stat
		StringBuffer summary = new StringBuffer();
		summary.append(row_start);
		summary.append("<th>Remark</th>");
		summary.append("<th>Stat</th>");
		summary.append(row_end);
		
		summary.append("<tr bgcolor=\"green\">");
		summary.append(col_start);
		summary.append("Total number of tests passed");
		summary.append(col_end);
		summary.append(col_start);
		summary.append(passes.size());
		summary.append(col_end);
		summary.append(row_end);
		
		summary.append("<tr bgcolor=\"red\">");
		summary.append(col_start);
		summary.append("Total number of tests failed");
		summary.append(col_end);
		summary.append(col_start);
		summary.append(fails.size());
		summary.append(col_end);
		summary.append(row_end);
		
		summary.append(row_start);
		summary.append(col_start);
		summary.append("Total number of tests executed");
		summary.append(col_end);
		summary.append(col_start);
		summary.append(fails.size() + passes.size() );
		summary.append(col_end);
		summary.append(row_end);
		
		summary.append(row_start);
		summary.append(col_start);
		summary.append("Pass percentage");
		summary.append(col_end);
		summary.append(col_start);
		summary.append( (passes.size()*100) / (fails.size() + passes.size()) );
		summary.append("%");
		summary.append(col_end);
		summary.append(row_end);
		return summary;
	}
}
