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

	HashMap<Issue, HashMap<ValueDiscrete, Integer>> issueValueCount;
	
	private int numberOfIssues;
	
	@Override
	public void init(NegotiationSession negotiationSession, HashMap<String, Double> parameters) throws Exception {
		this.negotiationSession = negotiationSession;		
		
		issueValueCount = new HashMap<Issue, HashMap<ValueDiscrete, Integer>>();
		
		setupModel();
	}
	
	/**
	 * Default values normalized to 1.
	 */
	private void setupModel(){
		opponentUtilitySpace = new UtilitySpace(negotiationSession.getUtilitySpace());
		numberOfIssues = opponentUtilitySpace.getDomain().getIssues().size();
		double sharedWeight = 1D / (double)numberOfIssues;    
		
		// initialize all the weights
		for(Entry<Objective, Evaluator> e: opponentUtilitySpace.getEvaluators()){
			// set issue weights for each entry
			opponentUtilitySpace.unlock(e.getKey());
			
			e.getValue().setWeight(sharedWeight);
			
			HashMap<ValueDiscrete, Integer> valueMap = new HashMap<ValueDiscrete, Integer>();
			try {				
				for(ValueDiscrete value : ((IssueDiscrete)e.getKey()).getValues())
				{
					((EvaluatorDiscrete)e.getValue()).setEvaluation(value,1);
					//initialize all counts at 0
					valueMap.put(value, new Integer(0));
				}
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			//store value counts for each issue in the map
			issueValueCount.put((IssueDiscrete)e.getKey(), valueMap);
		}
	}
	
	@Override
	public void updateModel(Bid opponentBid, double time) {
		insertBid(opponentBid);
	}	
		
	//Inserts the bid into the counter
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
	
	@Override	
	public double getBidEvaluation(Bid bid) {
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
