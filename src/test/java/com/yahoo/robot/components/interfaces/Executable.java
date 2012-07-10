package com.yahoo.robot.components.interfaces;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import com.yahoo.robot.components.DDValidatorFactory;
import com.yahoo.robot.components.QueueManager;
import com.yahoo.robot.libs.URLHelper;
import com.yahoo.robot.messages.DefaultValidationResponse;


public abstract class Executable implements Runnable
{
	public Executable() 
	{
	}
	
	public abstract Map<String,Object> execute(IJobRequest job);
	
	@Override
	final public void run() 
	{
		Map<String, Object> executionResponse = null;
		while (! QueueManager.isJobQEmpty() )
		{
			/*
			 *  1. Execute the URL, get the response
			 */
			IJobRequest job = QueueManager.getJobRequest();
			if ( null == job )
			{
				continue;
			}
			executionResponse = execute(job);
			/*
			 *  2. Get the validator dynamically based on inputs
			 */
			Class<?> dynamicallyDetererminedValidator = DDValidatorFactory.getValidator(job.getRequestURL(), job.getInputProperties());

			/*
			 *  3. Invoke 
			 *     		IValidateResponse validate(Map<String, Object> validationParameters); 
			 *     
			 */
			Object objectInstance = null;
			try {
//		            Constructor<?> constructor = dynamicallyDetererminedValidator.getConstructor();
		            objectInstance = dynamicallyDetererminedValidator.newInstance();
		         }
		         catch (Throwable e) {
		            System.err.println(e);
		         }
			
			Method methodToBeInvoked = null;
			Method[] declaredMethods = dynamicallyDetererminedValidator.getDeclaredMethods();
			for ( int i = 0 ; i < declaredMethods.length ; i ++ )
			{
				if ( declaredMethods[i].getName().compareTo(IValidator.METHOD_REFLN_INVOCN) == 0 )
				{
					methodToBeInvoked = declaredMethods[i];
					break;
				}
			}
			
			Object[] argsList = new Object[1];
			argsList[0] = executionResponse;
			Object validationResponse = null;
			String errorMessage = null; 
			try 
			{
				validationResponse = methodToBeInvoked.invoke(objectInstance, argsList);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				errorMessage = e.getLocalizedMessage();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				errorMessage = e.getLocalizedMessage();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				errorMessage = e.getLocalizedMessage();
			} catch ( Exception e ) {
				e.printStackTrace();
				errorMessage = e.getLocalizedMessage();
			}
			
			/*
			 *  4. Put the response to validation Response Q
			 */
			IValidationResponse responseToBProcessed = null;
			if ( validationResponse != null && validationResponse instanceof IValidationResponse )
			{
				responseToBProcessed = (IValidationResponse) validationResponse;
				if ( responseToBProcessed.getRequestJob() == null )
				{
					responseToBProcessed.setRequestJob(job);
				}
				if ( responseToBProcessed.getErrorComponent() == null )
				{
					responseToBProcessed.setErrorComponent( job.getJobSourceConfigName());
				}
				String requestURL = job.getRequestURL();
				String[] splits = requestURL.split(URLHelper.URL_PARAM_VALUE_SEPARATOR);
				if ( null != splits && splits.length > 1 )
				{
					responseToBProcessed.setQuery(splits[1]);
				}
				QueueManager.addResponseToQ(responseToBProcessed);
			}
			else if ( null == validationResponse )
			{
				IValidationResponse validnResponse = new DefaultValidationResponse();
				validnResponse.setFailReason(errorMessage);
				validnResponse.setIsSuccessful(false);
				validnResponse.setErrorComponent("ERROR");
				validnResponse.setRequestJob(job);
				String requestURL = job.getRequestURL();
				String[] splits = requestURL.split(URLHelper.URL_PARAM_VALUE_SEPARATOR);
				if ( null != splits && splits.length > 1 )
				{
					validnResponse.setQuery(splits[1]);
				}
				QueueManager.addResponseToQ(validnResponse);
			}
			else 
			{
				System.out.println(Thread.currentThread().getId() + " validation response caught could not be put in Q " +
										"for processing, may be type mismatch " + validationResponse.toString() );
			}
			System.out.println("Executed job - " + job.getRequestNumber() );
		}
	}
}
