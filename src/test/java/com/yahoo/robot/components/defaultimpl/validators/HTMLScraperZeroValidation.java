package com.yahoo.robot.components.defaultimpl.validators;

import java.util.Map;

import com.yahoo.robot.components.interfaces.IValidationResponse;
import com.yahoo.robot.components.interfaces.IValidator;
import com.yahoo.robot.messages.DefaultValidationResponse;

public class HTMLScraperZeroValidation implements IValidator 
{

	@Override
	public IValidationResponse validate(Map<String, Object> validationParameters) 
	{
		com.yahoo.robot.libs.HTMLScraper.scrapeOut(validationParameters);
		return new DefaultValidationResponse();
	}
	
}
