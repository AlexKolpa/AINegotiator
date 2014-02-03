package negotiator.group1;
import java.util.HashMap;
import java.util.Map.Entry;

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
	
	//ease of access field so we dont have to request the UtilitySpace constantly
	private int numberOfIssues;
	
	//No parameters necessary. Simply set up the model.	
	@Override
	public void init(NegotiationSession negotiationSession) {
		this.negotiationSession = negotiationSession;
		
		setupModel();
	}
	
	/**
	 * Normalize
	 */
	private void setupModel(){
		issueValueCount = new HashMap<Issue, HashMap<ValueDiscrete, Integer>>();		
		opponentUtilitySpace = new UtilitySpace(negotiationSession.getUtilitySpace());
		numberOfIssues = opponentUtilitySpace.getDomain().getIssues().size();
				
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
	 * Only update the counting table
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
		if(negotiationSession.getOpponentBidHistory().size() < 3)
			return 1 -negotiationSession.getDiscountedUtility(bid, negotiationSession.getTime());
		
		double result = 0;
		try {
			int totalBids = negotiationSession.getOpponentBidHistory().size();
			
			int countScore = 0;
			
			for(Issue i : opponentUtilitySpace.getDomain().getIssues()){
				
				//retrieve value from Bid through issueNumber
				ValueDiscrete value = (ValueDiscrete)bid.getValue(i.getNumber());
				//use issue to get the Value-Integer mapping
				HashMap<ValueDiscrete, Integer> valueMap = issueValueCount.get(i);
				Integer count = valueMap.get(value);
				
				countScore += count;
			}
			
			result = countScore / (double) totalBids; 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public String getName() {	
		return "Counting Opponent Model";
	}
}
