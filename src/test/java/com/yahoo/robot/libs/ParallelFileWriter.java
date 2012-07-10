package com.yahoo.robot.libs;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class ParallelFileWriter implements Runnable 
{
	
	private Queue<FileWriteRequest> fileQ = null;

	public ParallelFileWriter(int maxQueueSize) 
	{
		fileQ = new ArrayBlockingQueue<FileWriteRequest>(maxQueueSize);
	}
	
	public synchronized void addFileToQueue(FileWriteRequest request)
	{
		fileQ.add(request);
	}
	
	public boolean isRequestQEmpty()
	{
		return fileQ.isEmpty();
	}
	
	@Override
	public void run() 
	{
		FileWriter writer = null;
		FileWriteRequest request = null;
		while ( true  )
		{
			while ( fileQ.isEmpty() )
			{
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			request = fileQ.poll();
			
			try {
				writer = new FileWriter( request.getFile() );
				writer.write( request.getBufferToBeWritten().toString() );
				writer.flush();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

}
