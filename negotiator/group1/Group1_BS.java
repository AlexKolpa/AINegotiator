package negotiator.group1;

import negotiator.Bid;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OMStrategy;
import negotiator.boaframework.OfferingStrategy;
import negotiator.boaframework.OpponentModel;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Group1_BS extends OfferingStrategy {

	//	private static Logger LOG;
	private NegotiationSession session;
	private OpponentModel opponentModel;
	private OMStrategy opponentModelStrategy;
	private Double minUtility, maxUtility, concessionRate, offset;

	@Override
	public void init(NegotiationSession negotiationSession, OpponentModel opponentModel, OMStrategy omStrategy, HashMap<String, Double> parameters) {
		//		LOG = Logger.getAnonymousLogger();
		// LOG should post every single log message for debugging.
		//		LOG.setLevel(Level.ALL);

		this.session = negotiationSession;
		this.opponentModel = opponentModel;
		this.opponentModelStrategy = omStrategy;

		if (checkParameter(parameters.get("e"))) {
			this.concessionRate = parameters.get("e");
		}
		else {
			//			LOG.severe("Concession rate value 'e' not set in paramaters");
		}
		if (checkParameter(parameters.get(""))) {
			this.offset = parameters.get("k");
		}
		if (checkParameter(parameters.get("min"))) {
			this.minUtility = parameters.get("min");
		}
		if (checkParameter(parameters.get("max"))) {
			this.maxUtility = parameters.get("max");
		}
	}

	/**
	 * We want parameters not to be null or NaN. This method checks this, to improve cleanliness
	 *
	 * @param parameterValue The value to be checked
	 * @return True if the parameter suffices the demands, false otherwise.
	 */
	private boolean checkParameter(Double parameterValue) {
		if (parameterValue != null && !Double.isNaN(parameterValue)) {
			return true;
		}
		return false;
	}

	/**
	 * Determines the next bid based on the Utility, OpponentModel, and Time.
	 *
	 * @return The BidDetails that has the best utility value product.
	 */
	@Override
	public BidDetails determineNextBid() {

		//		LOG.info("Determining next bid.");

		/* Base bid on:
		TODO Time - Less time left, more concession steps; perhaps nice steps
		Utility - Maximize!
		Opponent Model - Hopefully be able to determine what a bid means for the opponent's utility.
		 */

		// Utility calculation
		double lastOwnUtility = session.getDiscountedUtility(session.getOwnBidHistory().getLastBid(), session.getTime());


			/* Pick three bids, based on lastOwnUtility:
				- 1.1 times lastOwnUtility if it's not over 1, else pick 1
				- lastOwnUtility
				- 0.9*lastOwnUtility, if it's not under the offset (which is assumed to be the reservation value
			 */

		BidDetails fortunate = session.getOutcomeSpace().getBidNearUtility((1.1 * lastOwnUtility < 1) ? 1.1 * lastOwnUtility : 1);
		BidDetails nice = session.getOutcomeSpace().getBidNearUtility(lastOwnUtility);
		BidDetails concession = session.getOutcomeSpace().getBidNearUtility((0.9 * lastOwnUtility > offset) ? 0.9 * lastOwnUtility : offset);

		try {
			return pickNashBestBid(fortunate, nice, concession);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Determines the bid closest to the utility of '1'.
	 *
	 * @return A BidDetails object with an utility close to '1'.
	 */
	@Override
	public BidDetails determineOpeningBid() {

		//		LOG.info("Determining opening bid.");

		return session.getOutcomeSpace().getBidNearUtility(1);
	}

	/**
	 * Returns the best bid given in bids, based on the product of the utility of the bid for this agent and for the opponent.
	 * The latter utility is based on the opponentModel.
	 * An arbitrary number of BidDetails can be given.
	 *
	 * @param bids An arbitrary number of BidDetails.
	 * @return The BidDetails object with the highest product of the utility for the agent and its opponent
	 * @throws Exception if the bestBid was not set in the inspection of bids. Indicates further issues.
	 */
	private BidDetails pickNashBestBid(BidDetails... bids) throws Exception {

		BidDetails bestBid = null;
		double bestUtil = -1;

		for (BidDetails bid : bids) {
			double oppUtil = opponentModel.getBidEvaluation(bid.getBid());
			double ownUtil = session.getDiscountedUtility(bid.getBid(), session.getTime());

			double nash = oppUtil * ownUtil;
			if (nash > bestUtil) {
				bestUtil = nash;
				bestBid = bid;
			}
		}

		if (bestBid == null) {
			throw new Exception("Hide yo kids, hide you wife"); // FIXME better error message
		}

		return bestBid;
	}
}
