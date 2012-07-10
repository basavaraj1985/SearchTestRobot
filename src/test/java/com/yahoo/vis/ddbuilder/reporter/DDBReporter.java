package com.yahoo.vis.ddbuilder.reporter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.yahoo.robot.components.defaultimpl.reporters.DefaultReporter;
import com.yahoo.robot.components.interfaces.IValidationResponse;
import com.yahoo.robot.components.interfaces.Reportable;
import com.yahoo.robot.control.SearchTestRobot;
import com.yahoo.robot.libs.URLHelper;


public class DDBReporter  extends DefaultReporter
{
	//public static Map<String, List<IValidationResponse>> statModelMap = new HashMap<String, List<IValidationResponse>>();
	public static Map<String,Map< String, List<IValidationResponse>>> consoliMap = new HashMap<String, Map<String, List<IValidationResponse>>>();
	public static List<IValidationResponse> passes = new ArrayList<IValidationResponse>();
	public static List<IValidationResponse> fails = new ArrayList<IValidationResponse>();
	private Map<String, List<IValidationResponse>> statModelMap = null;
	/*private Map<String,Map< String, List<IValidationResponse>>> consoliMap = null;
	private List<IValidationResponse> passes = null;
	private List<IValidationResponse> fails = null;*/
	public static String logFolder = null;
	public static String reportFileName = "DDB_AutoMan_Report.html";
	private StringBuffer templateBuffer = null;
	private BufferedWriter reportWriter ;
	private File reportFile;
	public static final String REPORT_FORMAT = "./conf/Report.html";
			
	public DDBReporter(String fileName, URLHelper urlHelper) 
	{
		super(fileName,urlHelper);
		statModelMap = new HashMap<String, List<IValidationResponse>>();
		/*consoliMap = new HashMap<String, Map<String, List<IValidationResponse>>>();
		passes = new ArrayList<IValidationResponse>();
		fails = new ArrayList<IValidationResponse>();*/
		templateBuffer = new StringBuffer() ;
		intializeFiles();
	}

	private void intializeFiles() 
	{
		logFolder = (String)(SearchTestRobot.controllerProperties.get(SearchTestRobot.LOG_DIRECTORY));
		File dir = new File(logFolder);
		dir.mkdirs();
		
		reportFile = new File(dir.getAbsolutePath() + "/" + reportFileName);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(new File(REPORT_FORMAT)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("Could not read the configured default template : " + Reportable.DEFAULT_REPORT_FORMAT );
			System.exit(-1);
		}
		
		String line = null;
		try {
			while ( ( line = reader.readLine() ) != null )
			{
				templateBuffer.append(line);
				templateBuffer.append('\n');
			}
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	/**
	 * May be update a html file everytime ?
	 */
	@Override
	public void report(IValidationResponse response) 
	{
		super.report(response);
		if ( response.getRequestJob() != null && response.getRequestJob().getRequestNumber() != -1 )
		{
			System.out.println("Reporting for job - " + response.getRequestJob().getRequestNumber() );
		}
		if ( response.isSuccesful() )
		{
			passes.add(response);
		}
		else 
		{
			fails.add(response);
			if ( statModelMap.containsKey(response.getFailReason()))
			{
				List<IValidationResponse> existingList = statModelMap.get(response.getFailReason());
				existingList.add(response);
				statModelMap.put(response.getFailReason(), existingList);
			}
			else
			{
				List<IValidationResponse> initedList = new ArrayList<IValidationResponse>();
				initedList.add(response);
				statModelMap.put(response.getFailReason(), initedList );
			}
			String dataFileName = response.getRequestJob().getJobSourceConfigName();
			consoliMap.put(dataFileName,statModelMap);
		}
		
		StringBuffer summary = getSummaryAsHTMLTable();
		StringBuffer statsTable = getStatsAsHTMLTable(response);
		
		String reportToWrite = templateBuffer.toString() ;
		reportToWrite = reportToWrite.replace(Reportable.SUMMARY_DYNAMIC_TEMPLATE, summary.toString());
		reportToWrite = reportToWrite.replace(Reportable.STATS_MODEL_DYNAMIC_TEMPLATE, statsTable.toString());
		
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
		
		
	}

	private StringBuffer getStatsAsHTMLTable(IValidationResponse response) 
	{
		String row_start = "<tr>";
		String row_end = "</tr>";
		String col_start = "<td>";
		String col_end = "</td>";
		
		StringBuffer statsTable = new StringBuffer();
		statsTable.append(row_start);
		statsTable.append("<th>Data File</th>");
		statsTable.append("<th>Failure Reason</th>");
		statsTable.append("<th>Count</th>");
		statsTable.append("<th>Queries</th>");
		statsTable.append(row_end);
		String dataFileName = null;
		Set<Entry<String, Map<String, List<IValidationResponse>>>> consoliSet = consoliMap.entrySet();
		for (Entry<String, Map<String, List<IValidationResponse>>> consoli : consoliSet)
		{
			dataFileName = consoli.getKey();
			Set<Entry<String, List<IValidationResponse>>> entrySet = consoliMap.get(dataFileName).entrySet();
			for ( Entry<String, List<IValidationResponse>> entry : entrySet )
			{
				statsTable.append(row_start);
				statsTable.append(col_start);
				
				statsTable.append(dataFileName);   //data file name
				statsTable.append(col_end);
				statsTable.append(col_start);
				statsTable.append(entry.getKey());   //reason
				statsTable.append(col_end);
				statsTable.append(col_start);
				statsTable.append(entry.getValue().size()); // count
				statsTable.append(col_end);
				
				//cases
				statsTable.append(col_start);
				if ( entry.getValue().size() > 5 )
				{
					String reason = entry.getKey();
					reason = reason.substring(0, 30).trim();
					reason = getValidFileName(reason);
					File file = new File ( reason + ".txt");
					BufferedWriter bfWriter = null;
					try {
						bfWriter = new BufferedWriter(new FileWriter(file, true));
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					if ( ! file.exists() )
					{
						List<IValidationResponse> responses = entry.getValue();
						for ( IValidationResponse eachResp : responses )
						{
							try {
								bfWriter.write(eachResp.getQuery() );
								bfWriter.write('\n');
								bfWriter.flush();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
					else
					{
						try {
							bfWriter.append(response.getQuery() );
							bfWriter.write('\n');
							bfWriter.flush();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
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
			
			
		}

		return statsTable;
	}

	
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


	
	private StringBuffer getSummaryAsHTMLTable() 
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
		summary.append(col_end);
		summary.append(row_end);
		return summary;
	}
	
}
