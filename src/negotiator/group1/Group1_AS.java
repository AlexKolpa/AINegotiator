package negotiator.group1;

import java.util.HashMap;

import negotiator.BidHistory;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.AcceptanceStrategy;
import negotiator.boaframework.Actions;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OfferingStrategy;

public class Group1_AS extends AcceptanceStrategy {
	
	// Context in which the negotiation takes place.
	NegotiationSession negotiationSession;
	OfferingStrategy offeringStrategy;
	
	// Parameters for the acceptance strategy.
	// Deadline is the time after which the agent should accept less favorable bids.
	double deadline = 0.99;
	// UtilityFactor and utilityGap are used to determine if this agent's next bid is better than the opponent's last bid, see isACnextAcceptable().
	double utilityFactor = 1.0;
	double utilityGap = 0.0;
	
	@Override
	public void init(NegotiationSession negotiationSession,	OfferingStrategy offeringStrategy, HashMap<String,Double> parameters) {
		// init stores information about the current negotiation session, and stores the parameters for the acceptance strategy.
		
		// Store the information about the current negotiation session.
		this.negotiationSession = negotiationSession;
		this.offeringStrategy = offeringStrategy;
		// Check the supplied parameters, and overwrite the default values if the parameter is supplied by the user.
		if(parameters.get("T") != null)
			deadline = parameters.get("T");
		if(parameters.get("a") != null)
			utilityFactor = parameters.get("a");
		if(parameters.get("b") != null)
			utilityGap = parameters.get("b");
	}
	
	@Override
	public Actions determineAcceptability() {
		// FIXME Remove debug output
		
		// determineAcceptability returns Actions.Accept if the last bid from the opponent is deemed acceptable. If it is not,
		// the function returns Actions.Reject.
		//
		// The opponent's bid is acceptable when either of the following cases is true:
		// - The opponent's last bid is larger than this agent's next bid (see isACnextAcceptable());
		// - The deadline has passed and the opponent's last bid is larger than expected (see isACtimeAcceptable() and getMaxW())
		
		System.out.println("AS: Determine acceptability");
		
		// Get the current time
		double time = negotiationSession.getTime();
		// Get the discounted utility of the opponent's last bid
		double opponentBidUtility = negotiationSession.getDiscountedUtility(negotiationSession.getOpponentBidHistory().getLastBid(), time);
		// Get MaxW
		double maxW = getMaxW();
		
		// Decide on the action that should be returned
		if( isACnextAcceptable() || (isACtimeAcceptable() && opponentBidUtility > maxW)) {
			System.out.println("AS: Accept");
			return Actions.Accept;
		} else {
			System.out.println("AS: Reject");
			return Actions.Reject;
		}
	}
	
	boolean isACnextAcceptable() {
		// FIXME Remove debug output
		
		// isACnextAcceptable returns true when a*Uopp+b>=Unext, where Uopp is the utility of the opponent's last bid,
		// and Unext is the utility of this agent's next bid.
		
		// Get the current time
		double time = negotiationSession.getTime();
		// Get the utility of the opponent's last bid.
		double opponentBidUtility = negotiationSession.getDiscountedUtility(negotiationSession.getOpponentBidHistory().getLastBid(), time);
		// Get the utility of this agent's next bid.
		double nextBidUtility = negotiationSession.getDiscountedUtility(offeringStrategy.getNextBid().getBid(),time);
		
		System.out.println("AS ACnext: time="+time);
		System.out.println("AS ACnext: Uopp="+opponentBidUtility);
		System.out.println("AS ACnext: Unext="+nextBidUtility);
		System.out.println("AS ACnext: "+(utilityFactor*opponentBidUtility+utilityGap >= nextBidUtility));
		
		// Return whether the opponent's bid is acceptable.
		return utilityFactor*opponentBidUtility+utilityGap >= nextBidUtility;
	}
	
	boolean isACtimeAcceptable() {
		// FIXME Remove debug output
		
		// isACtimeAcceptable returns true when the current time is larger than the deadline.
		
		// Get the current time
		double time = negotiationSession.getTime();
		
		System.out.println("AS ACtime: time="+time);
		System.out.println("AS ACtime: "+(time>deadline));
		
		// Return true if the current time is larger than the deadline.
		return time > deadline;
	}
	
	double getMaxW() {
		// FIXME Remove debug output
		
		// getMaxW returns the discounted utility of the best bid of the opponent in the time window (2*t-1,t] when t>0.5.
		// If t<0.5, the function returns 1.
		
		// Get the current time.
		double time = negotiationSession.getTime();
		// Get the opponent's bid history.
		BidHistory opponentBidHistory = negotiationSession.getOpponentBidHistory();
		
		// Return 1 if t<0.5.
		if(time<0.5) {
			System.out.println("AS maxW: time < 0.5");
			System.out.println("AS maxW: 1");
			return 1;
		}
		// Calculate the start of the window.
		double windowStart = 2*time-1;
		// Find the bids placed by the opponent during the time window.
		BidHistory filteredBidHistory = opponentBidHistory.filterBetweenTime(windowStart, time);
		// Find the best undiscounted bid.
		BidDetails bestUndiscountedBid = filteredBidHistory.getBestBidDetails();
		// Check if a bid is found. Otherwise, return 1.
		if(bestUndiscountedBid==null) {
			System.out.println("AS maxW: No best bid found in window ("+windowStart+","+time+")");
			System.out.println("AS maxW: 1");
			return 1;			
		}
		// Apply the discount factor to the utility of the best bid
		double bestDiscountedUtil = negotiationSession.getDiscountedUtility(bestUndiscountedBid.getBid(),time);
		
		System.out.println("AS maxW: bestDiscountedUtil="+bestDiscountedUtil+" in window ("+windowStart+","+time+")");
		System.out.println("AS maxW: "+bestDiscountedUtil);
		
		return bestDiscountedUtil;
	}

}
