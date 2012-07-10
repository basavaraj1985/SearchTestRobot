package com.yahoo.robot.libs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author basavar
 *
 */
public class FileUtils 
{

	/**
	 * 
	 * @param file
	 * @return
	 */
	public static Properties loadFileIntoProperties(String file)
	{
		Properties props = new Properties();
		try {
			props.load(new FileReader(new File(file)));
		} catch (FileNotFoundException e) {
			System.out.println("Could not find file : " + file);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Could not load file : " + file);
			e.printStackTrace();
		}
		return props;
	}
	
	/**
	 * Loads the properties file into properties, with added functionality of loading the files itself for values of the properties mentioned in 
	 * the file
	 * 
	 * i.e. if file=/home/hello.properties which contains
	 *         key1=val1,,val2,,file::test.properties,val3
	 *         key2=a,,b,,c,,file::test.properties
	 *         and test.properties contains - 
	 *         x
	 *         y
	 *         z..
	 *         
	 *         then effecive properties returned will be -
	 *         key1=val1,,val2,,x,,y,,z,,val3
	 *         key2=a,,b,,c,,x,,y,,z
	 *         
	 * @param file
	 * @param fileValueIndicator
	 * @param valueSeparator
	 * @return
	 */
	public static Properties loadProperties(String file, String fileValueIndicator, String valueSeparator )
	{
		Properties props = FileUtils.loadFileIntoProperties(file);
		Set<Entry<Object, Object>> entrySet = props.entrySet();
		for ( Entry<Object, Object> entry : entrySet )
		{
  			String value = (String) entry.getValue();
			if (  value.contains(fileValueIndicator) )
			{
				// the value might have multiple file urls, load each of them append, and remove these file urls on the go
				String[] allValues = value.split(valueSeparator);
				for ( String each : allValues )
				{
					if ( each.contains(fileValueIndicator))
					{
						String fileToBeLoaded = each.split(fileValueIndicator)[1];
						String newValue = getValueListFormatted(fileToBeLoaded,valueSeparator);
						entry.setValue( value.replace(each, newValue));
					}
				}
			}
		}
		return props;
	}
	
	/*
	 * Reads a file filled with values separated by new line characters into 
	 * a string value separated by VALUE SEPARATOR - ,,.
	 * It ignores the lines starting with '#'
	 */
	private static String getValueListFormatted(String fileToBeLoaded, String valueSeparator)
	{
		FileInputStream fstream = null;
		try {
			fstream = new FileInputStream(new File( fileToBeLoaded.trim()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		DataInputStream ds = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(ds));
		Pattern p;            
		Matcher m;
		String strLine;
		String inputText = "";
		try {
			while (  (strLine = br.readLine()) != null )
			{
				if ( ! strLine.startsWith("#") )
				{
					inputText = inputText + strLine + "\n";
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		// p = Pattern.compile("(?m)$^|[\\n]+\\z");
		p = Pattern.compile("\n");
		m = p.matcher(inputText);
		String str = m.replaceAll(valueSeparator);
		return str;
	}
	
	
	/**
	 * Gets the key name in the properties, where the 'propertyValue' is found.
	 * @param props
	 * @param propertyValue
	 * @return
	 */
	public static String getKeyFromPropertyValue(Properties props,
			String propertyValue) {
		String keyFound = null;
		Set<Entry<Object, Object>> entrySet = props.entrySet();
		Iterator<Entry<Object, Object>> iterator = entrySet.iterator();
		while ( iterator.hasNext() )
		{
			Entry<Object, Object> next = iterator.next();
			//since query type is url encoded, encode the value also to compare
//			String value = URLHelper.encodeStringToURLStandard ( (String) next.getValue() ); 
			String value = ( (String) next.getValue() );
			if ( value.compareTo(propertyValue) == 0 )
			{
				System.out.println("Value : " + propertyValue + " found in key - " + (String)next.getKey());
				keyFound = (String) next.getKey();
				break;
			}
			else if ( value.startsWith(URLHelper.FILE_VALUE_INDICATOR))
			{
				int valueFoundInFile = -1 ;
				try {
					valueFoundInFile = FileUtils.fileContainsLine(propertyValue, value.substring( value.indexOf(URLHelper.FILE_VALUE_INDICATOR) + URLHelper.FILE_VALUE_INDICATOR.length()));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				if ( valueFoundInFile != -1 )
				{
					System.out.println("Value : " + propertyValue + " found in key - " + (String)next.getKey());
					keyFound = (String) next.getKey();
					break;
				}
			}
		}
		return keyFound;
	}
	
	
	/**
	 * http://stackoverflow.com/questions/5005965/how-to-get-only-10-last-modified-files-from-directory-using-java
	 * 
	 * @param args
	 * @throws FileNotFoundException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws FileNotFoundException, InterruptedException 
	{
		Properties props = loadFileIntoProperties("./conf/DDB/DDBuilderUrl.properties");
		System.out.println(props.getProperty("page_element"));
	}

	/**
	 * 
	 * @param string - to be searched string
	 * @param filePath - in which file
	 * @return - non zero, non negative line number if found, else -1 
	 * @throws FileNotFoundException
	 */
	public static int fileContainsLine(String string, String filePath) throws FileNotFoundException
	{
		File file = new File(filePath);
		if ( ! file.exists() )
		{
			throw new FileNotFoundException("File " + filePath + " Doesnt exist!");
		}
		
		FileInputStream fis = new FileInputStream( file);
		BufferedReader reader = new BufferedReader( new InputStreamReader(fis ));
		int lineCount = 1;
		try 
		{
			String line = null;
			while ((line = reader.readLine()) != null) 
			{
				if (line.compareTo(string) == 0) 
				{
					System.out.println("String - " + string + " found in "
							+ filePath + " @line " + lineCount);
					return lineCount;
				}
				lineCount++;
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Could not read file : " + filePath );
		}
		return -1;
	}

	public static void writeFile(String url_list_file, List<String> results) throws IOException 
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(url_list_file)));
		for ( String line : results )
		{
			writer.write(line);
			writer.write('\n');
		}
		writer.flush();
		writer.close();
	}
	
	
	public static void writeFile(String url_list_file, Map<String,String> results) throws IOException 
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(url_list_file)));

		Set<String> keySet = results.keySet();
		Iterator<String> iterator = keySet.iterator();
		while ( iterator.hasNext() )
		{
			String url = iterator.next();
			String paramValues = results.get(url);
			writer.write(url + "==paramValues==> " + paramValues );
			writer.write('\n');
		}
		writer.flush();
		writer.close();
	}
	
	/**
	 * Remove duplicate lines from a file, and return the number of duplicates found
	 * @param ipFile
	 * @return
	 * @throws Exception
	 */
	public static int removeDuplicateLines(String ipFile) throws Exception
	{
		int duplicateCount = 0;
		BufferedReader reader = new BufferedReader( new FileReader(ipFile));
		StringBuffer toWriteBuffer = new StringBuffer();
		
		String line = null;
		while ( ( line = reader.readLine()) != null )
		{
			if ( toWriteBuffer.indexOf(line.trim())  == -1 )
			{
				toWriteBuffer.append(line.trim());
				toWriteBuffer.append('\n');
			}
			else
			{
				System.out.println("Duplicate : " + line);
				duplicateCount++;
			}
		}
		reader.close();
		
		BufferedWriter writer = new BufferedWriter( new FileWriter(ipFile));
		writer.write(toWriteBuffer.toString());
		writer.flush();
		writer.close();
		return duplicateCount;
	}

	/**
	 * 
	 * @param fileToRead - reads this file and returns the contents as StringBuffer
	 * @return
	 */
	public static StringBuffer readFileAsBuffer(String fileToRead)
	{
		StringBuffer resultBuffer = new StringBuffer();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(new File(fileToRead)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("Could not find the file : " + fileToRead);
			return null;
		}
		
		String line = null;
		try
		{
			while ( ( line = reader.readLine() ) != null )
			{
				resultBuffer.append(line);
				resultBuffer.append('\n');
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return resultBuffer;
	}

	/**
	 * @param fileToWrite - in which file to write - filename with location
	 * @param string - what to write
	 */
	public static void writeToFile(String fileToWrite, String stringToWrite)
	{
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter( new FileWriter(fileToWrite, false));
			writer.write(stringToWrite);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Could not write report! " + e.getLocalizedMessage());
			System.exit(-1);
		}
	}
	
	/**
	 * @param fileToWrite - in which file to write 
	 * @param string - what to write
	 */
	public static void writeToFile(File fileToWrite, String stringToWrite, boolean append)
	{
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter( new FileWriter(fileToWrite, append));
			writer.write(stringToWrite);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Could not write report! " + e.getLocalizedMessage());
			System.exit(-1);
		}
	}
	
}
