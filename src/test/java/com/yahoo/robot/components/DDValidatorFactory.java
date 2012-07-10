package com.yahoo.robot.components;

import java.util.Properties;

import com.yahoo.robot.components.interfaces.IValidator;
import com.yahoo.robot.libs.FileUtils;
import com.yahoo.robot.libs.URLHelper;

/**
 * @author basavar
 *
 */
public class DDValidatorFactory 
{
	public static void main(String[] args) 
	{
//		Class<?> classToBeLoaded = null;
//		try {
//			classToBeLoaded = Class.forName("com.yahoo.robot.components.JobRequest");
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		}
//		
////		Object objectInstance = null;
////		try {
////			 objectInstance = classToBeLoaded.newInstance();
////		} catch (InstantiationException e) {
////			e.printStackTrace();
////		} catch (IllegalAccessException e) {
////			e.printStackTrace();
////		}
//		
//		Object objectInstance = null;
//		try {
//	           Class cls = Class.forName("com.yahoo.robot.components.JobRequest");
//	           Class partypes[] = new Class[2];
//	            partypes[0] = String.class;
//	            partypes[1] = Properties.class;
//	            Constructor ct 
//	              = cls.getConstructor(partypes);
//	            Object arglist[] = new Object[2];
//	            arglist[0] = new String("hello");
//	            arglist[1] = new Properties();
//	            objectInstance = ct.newInstance(arglist);
//	         }
//	         catch (Throwable e) {
//	            System.err.println(e);
//	         }
//		
//		Method methodToBeInvoked = null;
//		try {
//			methodToBeInvoked = classToBeLoaded.getMethod("getRequestURL");
//		} catch (SecurityException e) {
//			e.printStackTrace();
//			System.out.println(Thread.currentThread().getId() + " Security Exception while 'reflective invocation' for validation method!");
//		} catch (NoSuchMethodException e) {
//			e.printStackTrace();
//			System.out.println(Thread.currentThread().getId() + " NoSuchMethod Exception while 'reflective invocation' for validation method!");
//		}
//		
//		
//		try {
//			Object result = methodToBeInvoked.invoke(objectInstance);
//			System.err.println("C wt Did I get - " + result);
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			e.printStackTrace();
//		}
//		System.err.println("----------------------");
//		
	}
	
	/**
	 * Returns a validator based on urlInputs and the the classes configured
	 * @author basavar
	 * @param urlInputz - url + ____ + "values used to form this url"
	 * @return
	 */
	public static Class<?> getValidator(String urlInputz, Properties props)
	{
		Class<?> resultingValidator = null; 

		String inputValues = urlInputz.substring(urlInputz.indexOf(URLHelper.URL_PARAM_VALUE_SEPARATOR) + URLHelper.URL_PARAM_VALUE_SEPARATOR.length());
		String[] paramValues = inputValues.split(",,");
		String validatorClass = null;
		
		if ( props.containsKey(IValidator.VALIDATOR_KEY))
		{
			validatorClass = props.getProperty(IValidator.VALIDATOR_KEY);
		}
		else 
		{
			for ( String paramValue : paramValues )
			{
				validatorClass = FileUtils.getKeyFromPropertyValue(props, paramValue);
				if ( null != validatorClass && validatorClass.length() > 1 && validatorClass.contains("."))
				{
					break;
				}
			}
		}
		
		try {
			resultingValidator = Class.forName(validatorClass);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.out.println(Thread.currentThread().getId() + " ClassNotFound Exception while 'reflective loading' of validator class - " + validatorClass );
			System.err.println("No Validation Could be done for " + urlInputz + " 'Cuz, the validator class couldnt be loaded!");
			return null;
		}
		return resultingValidator;
	}
}
