package com.yahoo.robot.libs;

import java.io.File;

/**
 * @author basavar
 *
 */
public class FileWriteRequest {
	
	StringBuffer bufferToBeWritten ;
	File fileToWrite;
	
	public FileWriteRequest( File file, StringBuffer buff)
	{
		bufferToBeWritten = buff;
		fileToWrite = file;
	}

	public StringBuffer getBufferToBeWritten() 
	{
		return bufferToBeWritten;
	}
	
	public File getFile() 
	{
		return fileToWrite;
	}
	
}
