package com.yahoo.robot.components.interfaces;

import java.util.Properties;

public interface IJobRequest 
{
	/**
	 * Will return, <br>
	 * url___paramValuesReplaced<br>
	 * <br>
	 * Example :
	 * http://search.yahoo.com?p=hotel+in+london___hotels+in,,london
	 * 
	 * @return
	 */
	String getRequestURL();
	
	/**
	 * An auto generated request tracking number 
	 * @return
	 */
	int getRequestNumber();
	
	/**
	 * Returns the config properties file name <br>
	 * from which this job is generated from <br>
	 * i.e. map this request to source data file <br> 
	 * @return
	 */
	String getJobSourceConfigName();
	
	/**
	 * Returns the input url properties
	 * @return
	 */
	Properties getInputProperties();
}
