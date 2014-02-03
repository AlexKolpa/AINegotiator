package negotiator.group1;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import negotiator.bidding.BidDetails;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OMStrategy;
import negotiator.boaframework.OpponentModel;

public class Group1_OMS extends OMStrategy {
	
	//epsilon for the evaluation since evals below this value aren't interesting in the first place
	private static final float Epsilon = 0.0001f;
	
	public Group1_OMS() {}
	
	public Group1_OMS(NegotiationSession negotiationSession, OpponentModel model) {
		try {
			super.init(negotiationSession, model);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	/**
	 * OpponentModel can always be updated. 
	 */
	@Override
	public boolean canUpdateOM() {		
		return true;
	}

	/**
	 * Returns the best bid out of a list of preferred bids
	 * 
	 * @param bids all the bids that are up for consideration
	 * @return bid to be offered
	 */
	@Override
	public BidDetails getBid(List<BidDetails> bids) {
		
		//This low of a value is totally not needed, since evaluations shouldn't 
		//go below 0 anyway, but assuming they can doesn't hurt.
		double bestBidScore = Double.MIN_VALUE;
		BidDetails bestBid = null;
		
		//We want to make sure the OpponentModel didn't fail completely
		boolean OMFailed = true;
		
		double time = negotiationSession.getTime();
		for(BidDetails bid : bids) {
			//use the OM to check the bid evaluation
			double opponentBidEval = model.getBidEvaluation(bid.getBid());
			//get our own bid evaluation
			double agentBidEval = negotiationSession.getDiscountedUtility(bid.getBid(), time);
						
			//Apparently OM didn't fail for at least one bid evaluation
			if(opponentBidEval > Epsilon)
				OMFailed = false;
			
			//use the multiplication of the scores to find a combined score. This emulates finding the Nash product.
			double bidScore = agentBidEval * opponentBidEval;
			
			//The bid is better than our previously found bid. Take it!
			if(bidScore > bestBidScore) {
				bestBid = bid;
				bestBidScore = bidScore;
			}
		}		
		
		//If bestBid is still null, the OM seriously failed (all were Double.MIN_VALUE)
		if(OMFailed || bestBid == null) {
			//we'll select a random bid since according to the OM, it doesn't matter anyway.
			bestBid = bids.get(new Random().nextInt(bids.size()));
		}
		
		return bestBid;
	}
}
