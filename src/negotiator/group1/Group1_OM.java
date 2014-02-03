package negotiator.group1;
import java.util.HashMap;
import java.util.Map.Entry;

import org.jfree.util.Log;

import negotiator.Bid;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OpponentModel;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.Objective;
import negotiator.issue.ValueDiscrete;
import negotiator.utility.Evaluator;
import negotiator.utility.EvaluatorDiscrete;
import negotiator.utility.UtilitySpace;

public class Group1_OM extends OpponentModel {

	//Tracks, for each issues, the amount of times each value has been offered
	HashMap<Issue, HashMap<ValueDiscrete, Integer>> issueValueCount;
		
	//No parameters necessary. Simply set up the model.	
	@Override
	public void init(NegotiationSession negotiationSession, HashMap<String, Double> parameters) throws Exception {
		this.negotiationSession = negotiationSession;
		
		setupModel();
	}


	/**
	 * Construct the new utility space.
	 * Stores the numberOfIssues for ease of access.
	 * Initializes the counting table with empty values.
	 */
	private void setupModel(){
		issueValueCount = new HashMap<Issue, HashMap<ValueDiscrete, Integer>>();		
		opponentUtilitySpace = new UtilitySpace(negotiationSession.getUtilitySpace());
				
		// initialize all the weights
		for(Entry<Objective, Evaluator> e: opponentUtilitySpace.getEvaluators()){
			//setup a hashmap for each issue
			HashMap<ValueDiscrete, Integer> valueMap = new HashMap<ValueDiscrete, Integer>();
			
			for(ValueDiscrete value : ((IssueDiscrete)e.getKey()).getValues())
			{
				//initialize all counts at 0
				valueMap.put(value, new Integer(0));
			}
			
			//store value counts for each issue in the map
			issueValueCount.put((IssueDiscrete)e.getKey(), valueMap);
		}
	}
	
	/**
	 * Updates the model, which in our case, is only the counting table.
	 */
	@Override
	public void updateModel(Bid opponentBid, double time) {
		insertBid(opponentBid);
	}	
	
	
	/**
	 * Inserts the bid into the counting table.
	 * @param opponentBid bid made by the opponent this round
	 */
	private void insertBid(Bid opponentBid) {
		try{
			for(Issue i : opponentUtilitySpace.getDomain().getIssues()){
				//retrieve value from Bid through issueNumber
				ValueDiscrete value = (ValueDiscrete)opponentBid.getValue(i.getNumber());
				//use issue to get the Value-Integer mapping
				HashMap<ValueDiscrete, Integer> valueMap = issueValueCount.get(i);
				Integer count = valueMap.get(value);
				//since Integer is stored by reference, we don't need to replace anything
				count++;								
			}
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	/**
	 * the utility is determined by the sum of the amount of times 
	 * each value has been offered divided by the maximum possible amount.
	 * Since we don't know much in the beginning, assume the utility to be
	 * 1 - (our own utility) 
	 */
	@Override	
	public double getBidEvaluation(Bid bid) {
		double result = 0;
		
		Issue issue = null;
		ValueDiscrete value = null;
		HashMap<ValueDiscrete, Integer> valueMap = null;
		Integer count = null;
		boolean passedPoint = false;
		try {
			int totalBids = negotiationSession.getOpponentBidHistory().size();
			
			int countScore = 0;
			
			for(Issue i : opponentUtilitySpace.getDomain().getIssues()){
				passedPoint = false;
				issue = i;
				//retrieve value from Bid through issueNumber
				value = (ValueDiscrete)bid.getValue(issue.getNumber());
				//use issue to get the Value-Integer mapping
				passedPoint = true;
				valueMap = issueValueCount.get(issue);
				count = valueMap.get(value);
				
				countScore += count;
			}
			
			result = countScore / (double) totalBids; 
		} catch (Exception e) {		
			System.out.println("past point: " + passedPoint);
			System.out.println(issue == null? "Issue null" : issue.toString());
			System.out.println(value == null? "value null" : value.toString());
			System.out.println(valueMap == null? "valueMap null" : valueMap.toString());
			System.out.println(count == null? "count null" : count.toString());
			e.printStackTrace();
		}
		
		/* Since we can assume our opponent wont likely make an offer of 0 utility,
		 * our model possibly made a mistake somewhere, or simply doesn't have enough information.
		 * We assume the utility to be the opposite of our own in that case. 
		 */
		if(result < 0.0001d){			
			return 1 -negotiationSession.getDiscountedUtility(bid, negotiationSession.getTime());			
		}
		
		return result;
	}

	@Override
	public String getName() {	
		return "Counting Opponent Model";
	}
}
