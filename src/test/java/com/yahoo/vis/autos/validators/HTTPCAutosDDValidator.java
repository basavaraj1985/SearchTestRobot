package com.yahoo.vis.autos.validators;

import java.util.Map;

import com.yahoo.robot.components.defaultimpl.validators.HTTPClientTextPresenceValidator;
import com.yahoo.robot.components.interfaces.IValidationResponse;

public class HTTPCAutosDDValidator extends HTTPClientTextPresenceValidator 
{
	final String QP_DISPATCH_PLAN_START = "&lt;autos_listings_dd cat=&quot;autos&quot;";
	final String QP_DISPATCH_PLAN_END = "&lt;\\/autos_listings_dd&gt;";
	
	@Override
	public IValidationResponse validate(Map<String, Object> validationParameters) 
	{
		IValidationResponse validationResponse = super.validate(validationParameters);
		StringBuffer actualResponseBuffer = validationResponse.getActualResponseAsBuffer();
		String actualResponse = actualResponseBuffer.toString();
		if ( null == actualResponseBuffer || ( null != actualResponseBuffer && actualResponseBuffer.toString().trim().length() < 10) )
		{
			validationResponse.setIsSuccessful(false);
			validationResponse.setErrorComponent("Connection");
			validationResponse.setFailReason("Empty page, may be site down/connxn error!");
			return validationResponse;
		}
			
		if ( validationResponse.isSuccesful() ) // DD present
		{
			try {
				validationResponse = validateQPDispatchPlan(validationResponse, validationParameters, actualResponse);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else // DD Not present
		{
			if ( ! actualResponse.contains("Autovi")) // QP Didnt trigger
			{
				validationResponse.setIsSuccessful(false);
				validationResponse.setErrorComponent("QP");
				validationResponse.setFailReason("QP Didn't trigger");
			}
			else // QP Triggered, but no DD
			{
				validationResponse.setErrorComponent("BE");
				validationResponse.setFailReason("BE Didn't return results");
				try {
					validationResponse = validateQPDispatchPlan(validationResponse, validationParameters,
							actualResponse);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		actualResponse = null;
		actualResponseBuffer = null;
		return validationResponse;
	}

	/**
	 * @param validationResponse
	 * @param validationParameters
	 * @param actualResponse
	 */
	private IValidationResponse validateQPDispatchPlan(IValidationResponse validationResponse,
			Map<String, Object> validationParameters, String actualResponse)  throws Exception
	{
		// lat, long, raw query, make, city, implicit location,
		// verify QP Dispatch plan
		int startQPPlan = actualResponse.indexOf(QP_DISPATCH_PLAN_START);
		int endQPPlan = actualResponse.indexOf(QP_DISPATCH_PLAN_END);
		if ( ! ( startQPPlan > 0 && endQPPlan > 0 ) && ! validationResponse.isSuccesful() )
		{
			validationResponse.setIsSuccessful(false);
			validationResponse.setErrorComponent("BE");
			validationResponse.setFailReason("BE Didn't return results");
			return validationResponse;
		}
		else if ( startQPPlan < 0 || endQPPlan <= 0)
		{
			System.err.println("Couldnt derive QP Dispatch plan! start: " + startQPPlan + " End: " + endQPPlan + " Query: " + validationParameters);
			if ( validationResponse.getURL() != null )
			{
				String url = validationResponse.getURL();
				if ( ! url.contains("&debug") )
				{
					System.err.println("Debug is enabled in url pattern, add &debug=yfedResponse");
				}
			}
			return validationResponse;
		}
		String qpDispatchPlan = actualResponse.substring(startQPPlan, endQPPlan);
		
		// ANYWHERE BELOW DOWN ERROR MEANS, QP ERROR :
		validationResponse.setErrorComponent("QP");
		// verify lat is non empty
		String lats = "&lt;lat&gt;", late = "&lt;\\/lat&gt;";
		if ( ! qpDispatchPlan.contains(lats) || qpDispatchPlan.contains("&lt;lat\\/&gt;") )
		{
			validationResponse.setIsSuccessful(false);
			validationResponse.setFailReason("Empty &lt;lat&gt; tag in QP Dispatch plan!");
			return validationResponse;
		}
		String latitude = qpDispatchPlan.substring( qpDispatchPlan.indexOf(lats) + lats.length(), qpDispatchPlan.indexOf(late));
		if ( null == latitude || ( null != latitude && latitude.trim().length() < 3 ))
		{
			validationResponse.setIsSuccessful(false);
			validationResponse.setFailReason("Empty &lt;lat&gt; tag in QP Dispatch plan!");
			return validationResponse;
		}
		
		// verify lon is non empty
		String lons = "&lt;lon&gt;", lone = "&lt;\\/lon&gt;";
		if ( ! qpDispatchPlan.contains(lons) ||  qpDispatchPlan.contains("&lt;lon\\/&gt;") )
		{
			validationResponse.setIsSuccessful(false);
			validationResponse.setFailReason("Empty &lt;lon&gt; tag in QP Dispatch plan!");
			return validationResponse;
		}
		String longitude = qpDispatchPlan.substring( qpDispatchPlan.indexOf(lons) + lons.length(), qpDispatchPlan.indexOf(lone));
		if ( null == longitude || ( null != longitude && longitude.trim().length() < 3 ))
		{
			validationResponse.setIsSuccessful(false);
			validationResponse.setFailReason("Empty &lt;lon&gt; tag in QP Dispatch plan!");
			return validationResponse;
		}
		
		// verify raw_query is non empty
		String raw_query_s = "&lt;raw_query&gt;", raw_query_e = "&lt;\\/raw_query&gt;";
		if ( ! qpDispatchPlan.contains(raw_query_s) || qpDispatchPlan.contains("&lt;raw_query\\/&gt;"))
		{
			validationResponse.setIsSuccessful(false);
			validationResponse.setFailReason("Empty &lt;raw_query&gt; tag in QP Dispatch plan!");
			return validationResponse;
		}
		String raw_query = qpDispatchPlan.substring( qpDispatchPlan.indexOf(raw_query_s) + raw_query_s.length(), qpDispatchPlan.indexOf(raw_query_e));
		if ( null == raw_query || ( null != raw_query && raw_query.trim().length() < 3 ))
		{
			validationResponse.setIsSuccessful(false);
			validationResponse.setFailReason("Empty &lt;raw_query&gt; tag in QP Dispatch plan!");
			return validationResponse;
		}
		
		// verify make is non empty               &lt;\\/make&gt;
		String make_s = "&lt;make&gt;", make_e = "&lt;\\/make&gt;";
		if (! qpDispatchPlan.contains(make_s) || qpDispatchPlan.contains("&lt;make\\/&gt;") )
		{
			validationResponse.setIsSuccessful(false);
			validationResponse.setFailReason("Empty &lt;make&gt; tag in QP Dispatch plan!");
			return validationResponse;
		}
		String make = qpDispatchPlan.substring( qpDispatchPlan.indexOf(make_s) + make_s.length(), qpDispatchPlan.indexOf(make_e));
		if (  null == make || ( null != make && make.trim().length() < 2 ))
		{
			validationResponse.setIsSuccessful(false);
			validationResponse.setFailReason("Empty &lt;make&gt; tag in QP Dispatch plan!");
			return validationResponse;
		}
		
		// verify city is non empty
		String city_s = "&lt;city&gt;", city_e = "&lt;\\/city&gt;";
		if (! qpDispatchPlan.contains(city_s) ||  qpDispatchPlan.contains("&lt;city\\/&gt;") )
		{
			validationResponse.setIsSuccessful(false);
			validationResponse.setFailReason("Empty &lt;city&gt; tag in QP Dispatch plan!");
			return validationResponse;
		}
		String city = qpDispatchPlan.substring( qpDispatchPlan.indexOf(city_s) + city_s.length(), qpDispatchPlan.indexOf(city_e));
		if ( null == city || ( null != city && city.trim().length() < 2 ))
		{
			validationResponse.setIsSuccessful(false);
			validationResponse.setFailReason("Empty &lt;city&gt; tag in QP Dispatch plan!");
			return validationResponse;
		}
		
		// verify implicit_location is non empty
		String implicit_location_s = "&lt;implicit_location&gt;", implicit_location_e = "&lt;\\/implicit_location&gt;";
		if (! qpDispatchPlan.contains(implicit_location_s) ||  qpDispatchPlan.contains("&lt;implicit_location\\/&gt;") )
		{
			validationResponse.setIsSuccessful(false);
			validationResponse.setFailReason("Empty &lt;implicit_location&gt; tag in QP Dispatch plan!");
			return validationResponse;
		}
		String implicit_location = qpDispatchPlan.substring( qpDispatchPlan.indexOf(implicit_location_s) + implicit_location_s.length(), qpDispatchPlan.indexOf(implicit_location_e));
		if ( null == implicit_location || ( null != implicit_location && implicit_location.trim().length() < 4 ))
		{
			validationResponse.setIsSuccessful(false);
			validationResponse.setFailReason("Empty &lt;implicit_location&gt; tag in QP Dispatch plan!");
			return validationResponse;
		}
		
		if ( ! raw_query.toLowerCase().contains( make.toLowerCase() ))
		{
			validationResponse.setIsSuccessful(false);
			validationResponse.setFailReason("Query Didnt Contain the &lt;make&gt; returned by QP");
			return validationResponse;
		}
		
		System.out.println("Success QP Verification - make=" + make + " lat=" + latitude + " long=" + longitude );
		
		return validationResponse;
	}
	
}
