package agent;

import java.util.HashMap;
import negotiator.boaframework.AcceptanceStrategy;
import negotiator.boaframework.Actions;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OfferingStrategy;

public class Group1_AS extends AcceptanceStrategy {
	
	NegotiationSession negotiationSession;
	OfferingStrategy offeringStrategy;
	
	double deadline = 0.99;
	double threshold = 0.8;
	
	@Override
	public void init(NegotiationSession negotiationSession,
			OfferingStrategy offeringStrategy,
			HashMap<String,Double> parameters) {
		// TODO
		this.negotiationSession = negotiationSession;
		this.offeringStrategy = offeringStrategy;
		// TODO interpret and store values from 'parameters'
		// Overwrite the default deadline of 0.99 if it is specified by the user.
		if(parameters.get("T") != null)
			deadline = parameters.get("T");
		// Overwrite the default threshold of 0.8 if it is specified by the user.
		if(parameters.get("alpha") != null)
			threshold = parameters.get("alpha");
		
		return;
	}
	
	@Override
	public Actions determineAcceptability() {
		// TODO Auto-generated method stub
		
		// FIXME Remove debug output
		System.out.println("AS: determineAcceptability()");
		
		/*
		 * Get information about the current state of the negotiation
		 */
		
		// Get the current time
		double timeNow = negotiationSession.getTime();
		// Get the discounted utility of the opponent's last bid
		double opponentBidUtility = negotiationSession.getDiscountedUtility(negotiationSession.getOpponentBidHistory().getLastBid(), timeNow);
		
		// FIXME Remove debug output
		System.out.println("AS: timeNow="+timeNow);
		System.out.println("AS: opponentBidUtility="+opponentBidUtility);
		
		/*
		 * Decide what to do now
		 */
		
		// FIXME Remove debug output
		if(isACcombiAcceptable(timeNow,opponentBidUtility)) {
			System.out.println("AS: Accept");
			return Actions.Accept;
		} else {
			System.out.println("AS: Reject");
			return Actions.Reject;
		}
	}
	
	boolean isACcombiAcceptable(double time, double opponentBidUtility) {
		// TODO Check ACcombi as described in the paper
		return isACtimeAcceptable(time) || isACconstAcceptable(opponentBidUtility);
	}
	
	boolean isACnextAcceptable() {
		// TODO Check ACnext as described in the paper
		return false;
	}
	
	boolean isACtimeAcceptable(double time) {
		// isACtimeAcceptable returns true when the current time is larger than the deadline.
		return time > deadline;
	}
	
	boolean isACconstAcceptable(double opponentBidUtility) {
		// isACconstAcceptable returns true when the discounted utility of the opponent's last bid is larger than the threshold.
		return opponentBidUtility > threshold;
	}

}
