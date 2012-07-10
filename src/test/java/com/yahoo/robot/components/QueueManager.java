package com.yahoo.robot.components;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import com.yahoo.robot.components.interfaces.IJobRequest;
import com.yahoo.robot.components.interfaces.IValidationResponse;

public class QueueManager 
{
	private static Queue<IJobRequest> requestQ ;
	private static Queue<IValidationResponse> responseQ ;
	
	/**
	 * 
	 * @param requestQueueSize - IJobRequest - same as number of requests to be processed
	 * @param responseQueueSize - IValidationResponse -  max to be set as requestQueueSize
	 */
	public static void Initialize(int requestQueueSize, int responseQueueSize) 
	{
		requestQ = new ArrayBlockingQueue<IJobRequest>(requestQueueSize);
		responseQ = new ArrayBlockingQueue<IValidationResponse>(responseQueueSize);
	}
	
	/**
	 * 
	 * @param job
	 */
	public static void addJobToQueue(IJobRequest job)
	{
		synchronized (requestQ) 
		{
			requestQ.add(job);
		}
	}
	
	/**
	 * returns the size of job q - represent how many 
	 * more cases to be executed
	 * @return
	 */
	public static int getJobQSize()
	{
		return requestQ.size();
	}

	/**
	 *  
	 * @return
	 */
	public static boolean isJobQEmpty()
	{
		return requestQ.isEmpty();
	}
	
	/**
	 * 
	 * @return
	 */
	public static IJobRequest peekJobRequestQ()
	{
		synchronized (requestQ) 
		{
			return requestQ.peek();
		}
	}
	
	/**
	 * Removes head of the queue and returns, null if empty
	 * @return
	 */
	public static IJobRequest getJobRequest()
	{
		synchronized (requestQ)
		{
			return requestQ.poll();
		}
	}

	/**
	 * Adds response to response queue 
	 * @param resp
	 */
	public static void addResponseToQ(IValidationResponse resp)
	{
		synchronized (responseQ) 
		{
			responseQ.add(resp);
		}
	}
	
	/**
	 * returns the head of the queue without removing
	 * @return
	 */
	public static IValidationResponse peekValidationResponseQ()
	{
		synchronized (responseQ) 
		{
			return responseQ.peek();
		}
	}
	
	/**
	 * Removes head of the queue and retursn, null if empty
	 * @return
	 */
	public static IValidationResponse getValidationResponse()
	{
		synchronized (responseQ) 
		{
			return responseQ.poll();
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public static int getValidationResponseQSize()
	{
		return responseQ.size();
	}
	
	/**
	 * 
	 * @return
	 */
	public static boolean isValidationResponseQEmpty()
	{
		return responseQ.isEmpty();
	}
}
