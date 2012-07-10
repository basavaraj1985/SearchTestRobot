package com.yahoo.robot.libs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.yahoo.robot.components.interfaces.IConstants;
import com.yahoo.robot.control.SearchTestRobot;

/**
 * @author basavar
 *
 * Generally speaking, its a string processor, generating all the combinations possible, with fixed places for each parameter in an expression/pattern.
 * 
 * This takes an input file having pattern (url) which contains 'n' parameters, and in turn each parameter can have any number of values specified in the same file.
 * With this input, it generates a*b*c*...*n --, each being number of values for each parameter. 
 *
 */
public class URLHelper 
{
	public static final String URL_START = "url";
	public static final String VALUE_SEPARATOR = ",,";
	public static final String PARAMETER_IDENTIFIER = "\\$\\[";
	public static final String PARAMETER_IDENTIFIER_ACTUAL = "$[";
	public static final String PARAMETER_END = "\\]";
	public static final String PARAMETER_END_ACTUAL = "]";
	public static final String FIXED_VALIDATOR="validator";
	
	public static String NOW_TIME = now();

	/**
	 *  special keywords to be used in url.properties file
	 */
	public static final String FILE_VALUE_INDICATOR = "file::"; // Indicates the parameter values have to be read from a file located in x location - 'file::x'
	public static final String DONT_ENCODE = "dontEncode::";    // Indicates the 'x' parameter values should not be encoded in generated values where - url=....$[dontEncode::x]..
	
	public static final String DATA_FILE = "valuesFromDataFile";
	public static final String DATA_FILE_TRIM_WHITE_SPACES_IN_RECORDS = "trimWhiteSpacesInDataFile";

	public static final char DATA_FILE_COLUMN_DELIMITER = ',';
	public static final char DATA_FILE_COMMENT_SIGNAL = '#';
	public static final String DEBUG_FAILURES = "debugFailures";
	public static final String URL_PARAM_VALUE_SEPARATOR = "____";
	public static final String IS_WRITE_FILES = "writeFiles";
	public static final String IS_WRITE_PASS_FILES = "writePassFiles";
	
	private String configFileName;
	
	private Properties properties;
	
	private CsvReader dataFileReader ;
	
	private List<String> parameterList ;
	
	public static String now() {
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
//	    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMMdd_HH_mm_ss");
	    return sdf.format(cal.getTime());
	  }
	
	public URLHelper(String filename) 
	{
		configFileName = filename;
		properties = FileUtils.loadProperties(filename,FILE_VALUE_INDICATOR,VALUE_SEPARATOR);
		if ( properties.containsKey(DATA_FILE) )
		{
			try {
				//dataFileReader = new CsvReader( properties.getProperty(DATA_FILE) , DATA_FILE_COLUMN_DELIMITER);
				dataFileReader  = new  CsvReader( properties.getProperty(DATA_FILE) , DATA_FILE_COLUMN_DELIMITER, Charset.forName("UTF-8"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} 
			if ( dataFileReader != null )
			{
				dataFileReader.setComment(DATA_FILE_COMMENT_SIGNAL);
				try {
					dataFileReader.readHeaders();
				} catch (IOException e) {
					e.printStackTrace();
					System.err.println("ERROR : Could not read headers from data file : " + properties.getProperty(DATA_FILE));
				}
			}
		}
	}
	
	public URLHelper(Properties props)
	{
		properties = props;
	}
	
	public Properties getProperties() 
	{
		return properties;
	}

	public String getConfigFileName() 
	{
		File file = new File(configFileName);
		return file.getName();
	}
	
//	public String getUrlListFile() 
//	{
////		return urlListFile;
//	}

	/**
	 * Returns with the given url template, parameters, parameter values, how many possible url's can be constructed
	 * @return
	 */
	public int howManyCombinations() {
		// combinations ll be = numberOfParameters * numberOfValues
		int combinations = 1;
		for ( String key : getParameters() )
		{
			String propertyValue = properties.getProperty(key);
			String[] valuesForParams = null;
			if ( propertyValue != null )
			{
				valuesForParams = propertyValue.split(VALUE_SEPARATOR);
				combinations *= valuesForParams.length ;
			}
		}
		return combinations;
	}
	
	/**
	 * gets list of parameters/keys from the configured properties file
	 * @return
	 */
	public List<String> getParameters()
	{
		if ( parameterList != null )
		{
			return parameterList;
		}
		ArrayList<String> urlParamList = new ArrayList<String>(); 
		String urlTemplate = properties.getProperty(URL_START);
		if ( null == urlTemplate )
		{
			System.out.println("ERROR : Key not found - " + URL_START );
			return null;
		}
		String[] strings = urlTemplate.trim().split(PARAMETER_IDENTIFIER);
		for ( int i = 1; i<strings.length ; i++ )
		{
		  String temp = strings[i];
		  urlParamList.add(temp.split("\\]")[0]);
		}
		parameterList = urlParamList;
		return urlParamList;
	}
	
	/**
	 * Returns url template
	 * @return
	 */
	public String getURLTemplate()
	{
		return properties.getProperty(URL_START);
	}
	
	public String getProperty(String key)
	{
		return properties.getProperty(key);
	}
	
	/**
	 * Returns all the possible urls and also writes the urls to <param>file</param>
	 * @param file - file where the results are to be written
	 * @return
	 */
	public List<String> getAllPossibleURLsToFile(String file)
	{
		List<String> result = getAllPossibleURLs();
		writeResults(result, file);
		return result;
	}
	
	/**
	 * Reads the configured properties file, given in constructor.
	 * Then, using the url template, and provided values for the parameters, this method generates all possible combinations!
	 * <p>
	 * template example :
	 * <br>
	 * url=http://qa.search.yahoo.com/search?p=$[query]&[dontEncode::minMax]&fr=sfp&fr2=&iscqry=&debug=req
	 * If you dont want some parameter value to be url encoded, prefix that parameter with 'dontEncode::' in url template. Thats it.
	 * <br>
	 * query=a,,b,,c
	 * dontEncode::minMax=min=&Max=,,min=100&Max=1000
	 * 
	 * @return
	 */
	public List<String> getAllPossibleURLs()
	{
		List<String> results = new ArrayList<String>();
		if ( dataFileReader != null )
		{
			try {
				results = getAllPossibleURLsFromDataFile();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("ERROR : Could not read records from data file : " + properties.getProperty(DATA_FILE));
			} 
			return results;
		}
		
		int max = howManyCombinations();
		for ( int i = 0 ; i < max ; i++ )
		{
			results.add(getURLTemplate() + URL_PARAM_VALUE_SEPARATOR);
		}
		
		List<String> parameters = getParameters();
		if ( null == parameters )
		{
			return null;
		}
		Map<String,ArrayList<String>> matrix_paramsNValues = new HashMap<String, ArrayList<String>>();		
		//initialize the matrix, this makes sure that there are unique values for each parameter
		for ( String param : parameters )
		{
			ArrayList<String> thisParamValues = new ArrayList<String>();
			String values = properties.getProperty(param);
			if ( values == null )
			{
				System.out.println("NO Values configured for key - " + param);
				continue;
			}
			
			String[] splits = values.split(VALUE_SEPARATOR);
			for ( String value : splits)
			{
				if ( ! thisParamValues.contains(value) )
				{
					thisParamValues.add(value);
				}
				else 
				{
					System.out.println("Duplicate found -  key : " + param + "  value : " + value);
				}
			}
			matrix_paramsNValues.put(param, thisParamValues);
		}
		
		for ( String parameter : parameters )
		{
			ArrayList<String> thisParamValues = matrix_paramsNValues.get(parameter);
			if ( thisParamValues == null &&  parameter.startsWith(DONT_ENCODE))
			{
				thisParamValues = matrix_paramsNValues.get(parameter.substring(DONT_ENCODE.length()));
			}
			
			int indexCorrectionAdd = thisParamValues.size();
			int valueInitIndex = -1 ;
			for ( String thisParamValue : thisParamValues )
			{
				valueInitIndex++;
				int toBeUpdatedAtIndex = valueInitIndex ;
				while ( toBeUpdatedAtIndex < max )
				{
					String updated = null ;
					if ( parameter.startsWith(DONT_ENCODE) )
					{
						updated = replace(results.get(toBeUpdatedAtIndex), parameter, thisParamValue );
					}
					else 
					{
						updated = replace(results.get(toBeUpdatedAtIndex), parameter, encodeStringToURLStandard(thisParamValue) );
					}
					updated = updated.concat(thisParamValue).concat(",,");
					results.remove(toBeUpdatedAtIndex);
					results.add(toBeUpdatedAtIndex, updated);
					toBeUpdatedAtIndex = toBeUpdatedAtIndex + indexCorrectionAdd ;
				}
			}
		}
		return results;
	}

	private String writeResults(List<String> results, String fileWritten) 
	{
		try {
			FileUtils.writeFile( fileWritten, results);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return fileWritten;
	}

	/**
	 * 
	 * @return
	 * @throws IOException 
	 */
	public List<String> getAllPossibleURLsFromDataFile() throws IOException 
	{
		List<String> results = new ArrayList<String>();
		if ( getParameters().size() != dataFileReader.getHeaders().length )
		{
			System.out.println("ERROR : Number of parameters in url template is not matching up with number" +
					" of parameters provided in data file : " + properties.getProperty(DATA_FILE));
			System.out.println("ERROR : Probable reason might be incorrect delimiter, it has to be " + DATA_FILE_COLUMN_DELIMITER);
			return null;
		}
		
		List<String> parameters = null;
		
		while ( dataFileReader.readRecord() )
		{
			parameters = getParameters();
			String url = getURLTemplate();
			StringBuffer parameterValuesReplaced = new StringBuffer();
			for ( String parameter : parameters )
			{
				if ( parameter.startsWith(DONT_ENCODE) )
				{
					url = replace(url, parameter, dataFileReader.get(parameter.replace(DONT_ENCODE, "")));
					parameterValuesReplaced.append(dataFileReader.get(parameter.replace(DONT_ENCODE, "")));
				}
				else
				{
					url = replace(url, parameter, encodeStringToURLStandard(dataFileReader.get(parameter)));
					parameterValuesReplaced.append(dataFileReader.get(parameter));
				}
			}
			results.add(url+URLHelper.URL_PARAM_VALUE_SEPARATOR+parameterValuesReplaced.toString());
		}
		
		return results;
	}

	/**
	 * Replaces 'param' with 'value' in 'url'
	 * @param url
	 * @param param
	 * @param value
	 * @return
	 */
	private String replace(String url, String param, String value) 
	{
		String newURL = null;
		if ( url.contains(param) )
		{
			newURL = url.replace(PARAMETER_IDENTIFIER_ACTUAL+param+PARAMETER_END_ACTUAL, value);
		}
		else 
		{
			System.out.println("ERROR : URL template doesnt contain the paramter - url:" + url + " parameterToBeReplaced:"+param );
		}
		return newURL;
	}
	
	/**
	 * Takes a string, url encodes it and returns.
	 * Do not pass with 'htt://', pass only the string part which needs to be updated 
	 * @param query - ex : hotels near bangalore, "hotels, bangalore",..
	 * @return
	 */
	public static String encodeStringToURLStandard(String query)
	{
		String toReturn = null;
		try {
			toReturn = URLEncoder.encode(query,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.out.println("ERROR : Could not be url encoded " + query);
			System.out.println("WARNING : Returning string without url encoding " + query);
			return query;
		} 
		return toReturn;
	}

	public static int getOptimumNumberOfThreads(int size) 
	{
		int workers = 1 ;
		
		String numberOfThreads = SearchTestRobot.controllerProperties.getProperty(IConstants.NUMBER_OF_THREADS);
		
		if ( null != numberOfThreads && numberOfThreads.trim().length() > 0 )
		{
			try {
				workers = Integer.valueOf(numberOfThreads);
				return workers;
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		
		if ( size <= 20 )
		{
			workers = 2 ;
		}
		else if ( size > 20 && size <= 50 )
		{
			workers = 5;
		}
		else if ( size > 50 && size <= 500 )
		{
			workers = 10 ;
		}
		else if ( size > 500)
		{
			workers = 20 ;
		}
		return workers;
	}

}
