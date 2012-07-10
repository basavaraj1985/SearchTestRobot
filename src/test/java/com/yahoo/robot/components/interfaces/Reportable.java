package com.yahoo.robot.components.interfaces;
/**
 * @author basavar
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yahoo.robot.components.QueueManager;


public abstract class Reportable implements Runnable
{
	public static final String DEFAULT_REPORT_FORMAT = "./conf/Report.html";
	public static final String SUMMARY_DYNAMIC_TEMPLATE = "$summary";
	public static final String STATS_MODEL_DYNAMIC_TEMPLATE = "$stats";
	public static final  String PASS_LIST_DYNAMIC_TEMPLATE = "$passList";
	public static final  String FAIL_LIST_DYNAMIC_TEMPLATE = "$failList";
	public static final String  PASS_LIST_KEY = "passList";
	public static final String  FAIL_LIST_KEY = "failList";
	
	private static boolean stopIt = false;
	public static int i = 0 ;

	private List<IValidationResponse> passes;
	private List<IValidationResponse> fails;
	private Map<String, List<IValidationResponse>> statModelMap = null;

	private static List<IValidationResponse> consolidatedPasses;
	private static List<IValidationResponse> consolidatedFails;
	private static Map<String, List<IValidationResponse>> consolidatedStatModelMap = null;
	
	static 
	{
		consolidatedPasses = new ArrayList<IValidationResponse>();
		consolidatedFails = new ArrayList<IValidationResponse>();
		consolidatedStatModelMap = new HashMap<String, List<IValidationResponse>>();
	}

	public Reportable()
	{
		stopIt = false;
		passes = new ArrayList<IValidationResponse>();
		fails = new ArrayList<IValidationResponse>();
		statModelMap = new HashMap<String, List<IValidationResponse>>();
	}

	public abstract void report(IValidationResponse response);
	
	public static void setStopIt(boolean flag)
	{
		stopIt = flag;
	}
	
	/**
	 * @return the passes
	 */
	public List<IValidationResponse> getPasses()
	{
		return passes;
	}

	/**
	 * @return the fails
	 */
	public List<IValidationResponse> getFails()
	{
		return fails;
	}

	/**
	 * @return the statModelMap
	 */
	public Map<String, List<IValidationResponse>> getStatModelMap()
	{
		return statModelMap;
	}
	
	/**
	 * @return the consolidatedPasses
	 */
	public List<IValidationResponse> getConsolidatedPasses()
	{
		return consolidatedPasses;
	}

	/**
	 * @return the consolidatedFails
	 */
	public List<IValidationResponse> getConsolidatedFails()
	{
		return consolidatedFails;
	}

	/**
	 * @return the consolidatedStatModelMap
	 */
	public Map<String, List<IValidationResponse>> getConsolidatedStatModelMap()
	{
		return consolidatedStatModelMap;
	}
	
	@Override
	public void run() 
	{
		while ( ! stopIt )
		{
			IValidationResponse toBeProcessed = QueueManager.getValidationResponse();
			if ( null != toBeProcessed )
			{
				if ( toBeProcessed.isSuccesful() )
				{
					passes.add(toBeProcessed);
					consolidatedPasses.add(toBeProcessed);
				}
				else 
				{
					fails.add(toBeProcessed);
					consolidatedFails.add(toBeProcessed);
					if ( statModelMap.containsKey(toBeProcessed.getFailReason()))
					{
						List<IValidationResponse> existingList = statModelMap.get(toBeProcessed.getFailReason());
						existingList.add(toBeProcessed);
						statModelMap.put(toBeProcessed.getFailReason(), existingList);
					}
					else
					{
						List<IValidationResponse> initedList = new ArrayList<IValidationResponse>();
						initedList.add(toBeProcessed);
						statModelMap.put(toBeProcessed.getFailReason(), initedList );
					}
					
					if ( consolidatedStatModelMap.containsKey(toBeProcessed.getFailReason() ) )
					{
						List<IValidationResponse> consolidatedExistingList = consolidatedStatModelMap.get(toBeProcessed.getFailReason());
						consolidatedExistingList.add(toBeProcessed)
;						consolidatedStatModelMap.put(toBeProcessed.getFailReason(), consolidatedExistingList);
					}
					else
					{
						List<IValidationResponse> initedList = new ArrayList<IValidationResponse>();
						initedList.add(toBeProcessed);
						consolidatedStatModelMap.put(toBeProcessed.getFailReason(), initedList);
					}
				}
				report( toBeProcessed );
			}
			else
			{
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
