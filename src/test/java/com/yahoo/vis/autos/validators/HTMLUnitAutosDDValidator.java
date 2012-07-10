package com.yahoo.vis.autos.validators;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.yahoo.robot.components.defaultimpl.validators.HTMLUnitTextPresenceValidator;
import com.yahoo.robot.components.interfaces.IConstants;
import com.yahoo.robot.components.interfaces.IJobRequest;
import com.yahoo.robot.components.interfaces.IValidationResponse;
import com.yahoo.robot.libs.HTMLScraper;

public class HTMLUnitAutosDDValidator extends HTMLUnitTextPresenceValidator 
{
	final String QP_DISPATCH_PLAN_START = "<autos_listings_dd";
	final String QP_DISPATCH_PLAN_END = "</autos_listings_dd>";
	
	@Override
	public IValidationResponse validate(Map<String, Object> validationParameters) 
	{
		IValidationResponse validationResponse = super.validate(validationParameters);
		String query = (String) validationParameters.get(IConstants.QUERY);
		
		HtmlPage htmlPage = (HtmlPage) validationParameters.get(IConstants.RESPONSE_OBJECT);	
		StringBuffer actualResponseBuffer = validationResponse.getActualResponseAsBuffer();
		String actualResponse = actualResponseBuffer.toString();
		HTMLScraper.scrapeOut(validationParameters);
		if ( null == actualResponseBuffer || ( null != actualResponseBuffer && actualResponseBuffer.toString().trim().length() < 10) )
		{
			validationResponse.setIsSuccessful(false);
			validationResponse.setFailReason("Empty page, may be site down/connxn error!");
			return validationResponse;
		}
			
		if ( validationResponse.isSuccesful() ) // DD present
		{
			return validateQPDispatchPlan(validationResponse, validationParameters);
		}
		else // DD Not present
		{
			if ( ! actualResponse.contains("Autovi")) // QP Didnt trigger
			{
				validationResponse.setIsSuccessful(false);
				validationResponse.setFailReason("QP Didn't trigger");
			}
			else // QP Triggered, but no DD
			{
				validationResponse.setFailReason("BE Didn't return results");
				validationResponse = validateQPDispatchPlan(validationResponse, validationParameters );
			}
		}
		return validationResponse;
	}

	/**
	 * @param validationResponse
	 * @param validationParameters 
	 */
	private IValidationResponse validateQPDispatchPlan(IValidationResponse validationResponse,
			Map<String, Object> validationParameters) {
		
		// lat, long, raw query, make, city, implicit location,
		// verify QP Dispatch plan
		String GSM_API_XPATH = "//p[@align='left']/b[text()='GSM']/../a";
		String query = (String) validationParameters.get(IConstants.QUERY);
		String url = (String) validationParameters.get(IConstants.STRING_URL);
		IJobRequest job = (IJobRequest) validationParameters.get(IConstants.JOB_REQUEST);
		HtmlPage htmlPage = (HtmlPage) validationParameters.get(IConstants.RESPONSE_OBJECT);
		WebClient client = (WebClient) validationParameters.get(IConstants.CLIENT_OBJECT);
		
		List<HtmlAnchor> link = (List<HtmlAnchor>) htmlPage.getByXPath(GSM_API_XPATH);
		if ( link  == null || ( link  != null && link .size() < 1 ) )
		{
			System.out.println("Couldnt get gsm api from xpath - " + GSM_API_XPATH );
			System.err.println("Couldnt get gsm api from xpath - " + GSM_API_XPATH );
			return validationResponse;
		}
		
		Page page = null;
		System.out.println( Thread.currentThread().getId() + " " + Thread.currentThread().getName() 
				+ "Invoking GSM API : " + job.getRequestNumber() + " " + url);
		try {
			page = link.get(0).click();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if ( page == null )
		{
			System.out.println("GSM API response was null! - gsm url: \n" + link.get(0).asText() );
		}
		
		String gsmResponseString = page.getWebResponse().getContentAsString();
		System.out.println(gsmResponseString);
		int startQPPlan = gsmResponseString.indexOf(QP_DISPATCH_PLAN_START);
		int endQPPlan = gsmResponseString.indexOf(QP_DISPATCH_PLAN_END);
		if ( ! ( startQPPlan > 0 && endQPPlan > 0 ) && ! validationResponse.isSuccesful() )
		{
			validationResponse.setIsSuccessful(false);
			validationResponse.setFailReason("BE Didn't return results");
			return validationResponse;
		}
		else if ( startQPPlan < 0 || endQPPlan <= 0)
		{
			System.err.println("Couldnt derive QP Dispatch plan! start: " + startQPPlan + " End: " + endQPPlan + " Query: " + query);
			return validationResponse;
		}
		String qpDispatchPlan = gsmResponseString.substring(startQPPlan, endQPPlan);
		
		// verify lat is non empty
		String lats = "<lat>", late = "</lat>";
		String latitude = qpDispatchPlan.substring( qpDispatchPlan.indexOf(lats) + lats.length(), qpDispatchPlan.indexOf(late));
		if ( qpDispatchPlan.contains("<lat/>") ||  null == latitude || ( null != latitude && latitude.trim().length() < 3 ))
		{
			validationResponse.setIsSuccessful(false);
			validationResponse.setFailReason("Empty <lat> tag in QP Dispatch plan!");
			return validationResponse;
		}
		
		// verify lon is non empty
		String lons = "<lon>", lone = "</lon>";
		String longitude = qpDispatchPlan.substring( qpDispatchPlan.indexOf(lons) + lons.length(), qpDispatchPlan.indexOf(lone));
		if ( qpDispatchPlan.contains("<lon/>") ||  null == longitude || ( null != longitude && longitude.trim().length() < 3 ))
		{
			validationResponse.setIsSuccessful(false);
			validationResponse.setFailReason("Empty <lon> tag in QP Dispatch plan!");
			return validationResponse;
		}
		
		// verify raw_query is non empty
		String raw_query_s = "<raw_query>", raw_query_e = "</raw_query>";
		String raw_query = qpDispatchPlan.substring( qpDispatchPlan.indexOf(raw_query_s) + raw_query_s.length(), qpDispatchPlan.indexOf(raw_query_e));
		if ( qpDispatchPlan.contains("<raw_query/>") ||  null == raw_query || ( null != raw_query && raw_query.trim().length() < 3 ))
		{
			validationResponse.setIsSuccessful(false);
			validationResponse.setFailReason("Empty <raw_query> tag in QP Dispatch plan!");
			return validationResponse;
		}
		
		// verify make is non empty
		String make_s = "<make>", make_e = "</make>";
		String make = qpDispatchPlan.substring( qpDispatchPlan.indexOf(make_s) + make_s.length(), qpDispatchPlan.indexOf(make_e));
		if ( qpDispatchPlan.contains("<make/>") ||  null == make || ( null != make && make.trim().length() < 2 ))
		{
			validationResponse.setIsSuccessful(false);
			validationResponse.setFailReason("Empty <make> tag in QP Dispatch plan!");
			return validationResponse;
		}
		
		// verify city is non empty
		String city_s = "<city>", city_e = "</city>";
		String city = qpDispatchPlan.substring( qpDispatchPlan.indexOf(city_s) + city_s.length(), qpDispatchPlan.indexOf(city_e));
		if ( qpDispatchPlan.contains("<city/>") ||  null == city || ( null != city && city.trim().length() < 2 ))
		{
			validationResponse.setIsSuccessful(false);
			validationResponse.setFailReason("Empty <city> tag in QP Dispatch plan!");
			return validationResponse;
		}
		
		// verify implicit_location is non empty
		String implicit_location_s = "<implicit_location>", implicit_location_e = "</implicit_location>";
		String implicit_location = qpDispatchPlan.substring( qpDispatchPlan.indexOf(implicit_location_s) + implicit_location_s.length(), qpDispatchPlan.indexOf(implicit_location_e));
		if ( qpDispatchPlan.contains("<implicit_location/>") ||  null == implicit_location || ( null != implicit_location && implicit_location.trim().length() < 4 ))
		{
			validationResponse.setIsSuccessful(false);
			validationResponse.setFailReason("Empty <implicit_location> tag in QP Dispatch plan!");
			return validationResponse;
		}
		
		if ( ! query.toLowerCase().contains( make.toLowerCase() ))
		{
			validationResponse.setIsSuccessful(false);
			validationResponse.setFailReason("Query Didnt Contain the <make> returned by QP");
			return validationResponse;
		}
		return validationResponse;
	}
	
}
