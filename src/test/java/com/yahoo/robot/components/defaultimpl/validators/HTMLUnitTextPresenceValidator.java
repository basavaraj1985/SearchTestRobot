package com.yahoo.robot.components.defaultimpl.validators;

import java.util.Map;
import java.util.Properties;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.yahoo.robot.components.interfaces.IConstants;
import com.yahoo.robot.components.interfaces.IValidationResponse;
import com.yahoo.robot.components.interfaces.IValidator;
import com.yahoo.robot.libs.URLHelper;
import com.yahoo.robot.messages.DefaultValidationResponse;

public class HTMLUnitTextPresenceValidator implements IValidator 
{

	@Override
	public IValidationResponse validate(Map<String, Object> validationParameters) 
	{
		IValidationResponse validationResponse = new DefaultValidationResponse();

		HtmlPage htmlPage = (HtmlPage) validationParameters.get(IConstants.RESPONSE_OBJECT);
		String query = (String) validationParameters.get(IConstants.QUERY);
		String url = (String) validationParameters.get(IConstants.STRING_URL);
		validationResponse.setQuery(query);
		validationResponse.setURL(url);
		validationResponse.setIsSuccessful(true);
		
		if ( null == htmlPage )
		{
			validationResponse.setFailReason("Probably : Connection Error!, null/empty page");
			validationResponse.setIsSuccessful(false);
			return validationResponse;
		}
		
		String pageContent = htmlPage.asXml();
		validationResponse.setActualResponseAsBuffer(new StringBuffer(pageContent));
		
		Properties props = (Properties) validationParameters.get(IConstants.PROPERTIES_INPUTS);
		String mustPresentText = (String) props.get(IConstants.MUST_PRESENT_TEXT_TOKENS);
		
		String[] splits = mustPresentText.split(URLHelper.VALUE_SEPARATOR);
		boolean elementsPresent = true;
		StringBuilder absentTags = new StringBuilder();
		for ( String split : splits )
		{
			if ( ! pageContent.contains(split) )
			{
				absentTags.append(split);
				elementsPresent = false;
				System.out.println(split + " was not present in the page, for query : " + query );
			}
		}
		
		if ( !elementsPresent )
		{
			validationResponse.setIsSuccessful(false);
			validationResponse.setFailReason("Expected elements weren't present in the page - " 
																		+ absentTags.toString() );
		}
		return validationResponse;
	}

}
