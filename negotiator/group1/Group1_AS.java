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
	double deadline = 0.99;
	double a = 1.0;
	double b = 0.0;

	@Override
	public void init(NegotiationSession negotiationSession,
			OfferingStrategy offeringStrategy,
			HashMap<String, Double> parameters) {
		// init stores information about the current negotiation session, and
		// stores the parameters for the acceptance strategy.

		// Store the information about the current negotiation session.
		this.negotiationSession = negotiationSession;
		this.offeringStrategy = offeringStrategy;
		// Check the supplied parameters, and overwrite the default values if
		// the parameter is supplied by the user.
		if (parameters.get("T") != null)
			deadline = parameters.get("T");
		if (parameters.get("a") != null)
			a = parameters.get("a");
		if (parameters.get("b") != null)
			b = parameters.get("b");
	}

	/**
	 * Determines whether the last bid from the opponent is acceptable. The bid
	 * is deemed acceptable if the bid is larger than this agent's next bid, or
	 * when the deadline has passed and the opponent's last bid is higher than
	 * expected.
	 * 
	 * @return Actions.Accept if the opponent's bid is acceptable, else
	 *         Actions.Reject.
	 */
	@Override
	public Actions determineAcceptability() {
		// FIXME Remove debug output

		// determineAcceptability returns Actions.Accept if the last bid from
		// the opponent is deemed acceptable. If it is not,
		// the function returns Actions.Reject.
		//
		// The opponent's bid is acceptable when either of the following cases
		// is true:
		// - The opponent's last bid is larger than this agent's next bid (see
		// isACnextAcceptable());
		// - The deadline has passed and the opponent's last bid is larger than
		// expected (see getMaxW())

		// System.out.println("AS: Determine acceptability");

		// Get the current time
		double time = negotiationSession.getTime();
		// Get the discounted utility of the opponent's last bid
		double opponentBidUtility = negotiationSession.getDiscountedUtility(
				negotiationSession.getOpponentBidHistory().getLastBid(), time);
		// Get MaxW
		double maxW = getMaxW();

		// Decide on the action that should be returned
		if (isACnextAcceptable()
				|| (isACtimeAcceptable() && opponentBidUtility > maxW)) {
			// System.out.println("AS: Accept");
			return Actions.Accept;
		} else {
			// System.out.println("AS: Reject");
			return Actions.Reject;
		}
	}

	/**
	 * Determines whether a * U_opp + b >= U_next. U_opp is the utility of the
	 * opponents last bid, U_next is the utility of the agent's next bid.
	 * 
	 * @return true if (a * U_opp + b >= U_next)
	 */
	boolean isACnextAcceptable() {
		// FIXME Remove debug output

		// Get the current time
		double time = negotiationSession.getTime();
		// Get the utility of the opponent's last bid.
		double opponentBidUtility = negotiationSession.getDiscountedUtility(
				negotiationSession.getOpponentBidHistory().getLastBid(), time);
		// Get the utility of this agent's next bid.
		double nextBidUtility = negotiationSession.getDiscountedUtility(
				offeringStrategy.getNextBid().getBid(), time);

		// System.out.println("AS ACnext: time=" + time);
		// System.out.println("AS ACnext: Uopp=" + opponentBidUtility);
		// System.out.println("AS ACnext: Unext=" + nextBidUtility);
		// System.out.println("AS ACnext: " + (a * opponentBidUtility + b >=
		// nextBidUtility));

		// Return whether the opponent's bid is acceptable.
		return a * opponentBidUtility + b >= nextBidUtility;
	}

	/**
	 * Returns true when (current time > deadline).
	 * 
	 * @return true if (current time > deadline).
	 */
	boolean isACtimeAcceptable() {
		// FIXME Remove debug output

		// Get the current time
		double time = negotiationSession.getTime();

		// System.out.println("AS ACtime: time=" + time);
		// System.out.println("AS ACtime: " + (time > deadline));

		// Return true if the current time is larger than the deadline.
		return time > deadline;
	}

	/**
	 * Returns the discounted utility of the best bid of the opponent in the
	 * time window (2*t - 1, t] when t > 0.5, else returns 1.
	 * 
	 * @return 1 if t < 0.5, discounted utility of opp's best bid in time window
	 *         (2*t - 1, t].
	 */
	double getMaxW() {
		// FIXME Remove debug output

		// Get the current time.
		double time = negotiationSession.getTime();
		// Get the opponent's bid history.
		BidHistory opponentBidHistory = negotiationSession
				.getOpponentBidHistory();

		// Return 1 if t<0.5.
		if (time < 0.5) {
			// System.out.println("AS maxW: time < 0.5");
			// System.out.println("AS maxW: 1");
			return 1;
		}
		// Calculate the start of the window.
		double windowStart = 2 * time - 1;
		// Find the bids placed by the opponent during the time window.
		BidHistory filteredBidHistory = opponentBidHistory.filterBetweenTime(
				windowStart, time);
		// Find the best undiscounted bid.
		BidDetails bestUndiscountedBid = filteredBidHistory.getBestBidDetails();
		// Check if a bid is found. Otherwise, return 1.
		if (bestUndiscountedBid == null) {
			// System.out.println("AS maxW: No best bid found in window ("
			// + windowStart + "," + time + ")");
			// System.out.println("AS maxW: 1");
			return 1;
		}
		// Apply the discount factor to the utility of the best bid
		double bestDiscountedUtil = negotiationSession.getDiscountedUtility(
				bestUndiscountedBid.getBid(), time);

		// System.out.println("AS maxW: bestDiscountedUtil=" +
		// bestDiscountedUtil + " in window (" + windowStart + "," + time +
		// ")");
		// System.out.println("AS maxW: " + bestDiscountedUtil);

		return bestDiscountedUtil;
	}

}
