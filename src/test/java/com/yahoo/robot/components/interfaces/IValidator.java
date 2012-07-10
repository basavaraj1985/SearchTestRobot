package com.yahoo.robot.components.interfaces;

import java.util.Map;


/**
 * @author basavar
 *
 */
public interface IValidator 
{
	/**
	 * 
	 * @param validationParameters - map of ( paramName, paramValue ), which will be used in validators for validation
	 * 								 Framework user can return any number of parameters put in a map from executors <br>
	 * 								 These will be passed to validators at runtime.
	 * 								 
	 * @return
	 */
	IValidationResponse validate(Map<String, Object> validationParameters);
	
	String METHOD_REFLN_INVOCN = "validate";
	String VALIDATOR_KEY = "ValidatorClass";

}
