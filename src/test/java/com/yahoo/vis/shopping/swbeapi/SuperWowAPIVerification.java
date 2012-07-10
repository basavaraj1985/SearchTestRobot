package com.yahoo.vis.shopping.swbeapi;

import java.util.Map;

import com.yahoo.robot.components.interfaces.IConstants;
import com.yahoo.robot.components.interfaces.IValidationResponse;
import com.yahoo.robot.components.interfaces.IValidator;

public class SuperWowAPIVerification implements IValidator
{

	@Override
	public IValidationResponse validate(Map<String, Object> validationParameters) 
	{
		Object object = validationParameters.get(IConstants.RESPONSE_OBJECT);
		
		// logic to verify
		
		return null;
	}

}
