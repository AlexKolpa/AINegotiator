package negotiator.group1;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import negotiator.bidding.BidDetails;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OMStrategy;
import negotiator.boaframework.OfferingStrategy;
import negotiator.boaframework.OpponentModel;
import negotiator.boaframework.SortedOutcomeSpace;

public class Group1_BS extends OfferingStrategy {

	private static Logger LOG;
	private NegotiationSession session;
	private OpponentModel opponentModel;
	private OMStrategy opponentModelStrategy;
	private SortedOutcomeSpace outcomeSpace;
	private Double minUtility, maxUtility, concessionRate, reservationValue;

	@Override
	public void init(NegotiationSession negotiationSession,
			OpponentModel opponentModel, OMStrategy omStrategy,
			HashMap<String, Double> parameters) {

		LOG = Logger.getGlobal();
		// LOG should post every single log message for debugging.
		LOG.setLevel(Level.SEVERE);

		LOG.info("\n Setting up bidding strategy");

		this.session = negotiationSession;
		this.opponentModel = opponentModel;
		this.opponentModelStrategy = omStrategy;

		outcomeSpace = new SortedOutcomeSpace(
				negotiationSession.getUtilitySpace());
		negotiationSession.setOutcomeSpace(outcomeSpace);

		if (this.session == null) {
			LOG.severe("Negotiation session unavailable");
		}
		if (this.opponentModel == null) {
			LOG.severe("Opponent Model unavailable");
		}
		if (this.opponentModelStrategy == null) {
			LOG.severe("Opponent Model Strategy unavailable");
		}

		if (checkParameter(parameters.get("e"))) {
			this.concessionRate = parameters.get("e");
		} else {
			LOG.severe("Concession rate value 'e' not set in paramaters");
			this.concessionRate = 1.0;
		}
		if (checkParameter(parameters.get(""))) {
			this.reservationValue = parameters.get("k");
		} else {
			LOG.severe("Offset value 'k' not set in paramaters");
			this.reservationValue = 0.0;
		}
		if (checkParameter(parameters.get("min"))) {
			this.minUtility = parameters.get("min");
		}
		if (checkParameter(parameters.get("max"))) {
			this.maxUtility = parameters.get("max");
		}
	}

	/**
	 * We want parameters not to be null or NaN. This method checks this, to
	 * improve cleanliness
	 * 
	 * @param parameterValue
	 *            The value to be checked
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

		boolean outcomeSpaceAvailable = outcomeSpace != null;

		LOG.info("Determining next bid.");

		/*
		 * Base bid on: TODO Time - Less time left, more concession steps;
		 * perhaps nice steps Utility - Maximize! Opponent Model - Hopefully be
		 * able to determine what a bid means for the opponent's utility.
		 */

		// Utility calculation
		double lastOwnUtility = session.getDiscountedUtility(session
				.getOwnBidHistory().getLastBid(), session.getTime());

		LOG.info("LastOwnUtility is " + lastOwnUtility);
		/*
		 * Pick three bids, based on lastOwnUtility: - 1.1 times lastOwnUtility
		 * if it's not over 1, else pick 1 - lastOwnUtility -
		 * 0.9*lastOwnUtility, if it's not under the offset (which is assumed to
		 * be the reservation value
		 */

		misc.Range range = new misc.Range(
				0.9 * calculateTimeInfluencedUtilityGoal(session.getTime()),
				1.2 * calculateTimeInfluencedUtilityGoal(session.getTime()));
		List<BidDetails> possibleBids = outcomeSpace.getBidsinRange(range);

		BidDetails bidWithTopOpponentUtility = opponentModel
				.getBid(possibleBids);

		return bidWithTopOpponentUtility;
		// if (outcomeSpaceAvailable) {
		// LOG.info("Calculating BidDetails for several utility values");
		//
		// Double fortunateFactor = 1.3;
		// Double concessionFactor = 0.7;
		// Double fortunateUtility = (fortunateFactor * lastOwnUtility < 1) ?
		// fortunateFactor
		// * lastOwnUtility
		// : 1;
		// Double concessionUtility = (concessionFactor * lastOwnUtility >
		// reservationValue) ? concessionFactor
		// * lastOwnUtility
		// : reservationValue;
		// BidDetails fortunate = outcomeSpace
		// .getBidNearUtility(fortunateUtility);
		// BidDetails nice = outcomeSpace.getBidNearUtility(lastOwnUtility);
		// BidDetails concession = outcomeSpace
		// .getBidNearUtility(concessionUtility);
		//
		// if (fortunate.equals(nice) || fortunate.equals(concession)
		// || nice.equals(concession)) {
		// LOG.warning("Fortunate bid, nice bid and concession bit not unique");
		// }
		//
		// LOG.info("Calculations done");
		// try {
		// LOG.info("Trying to choose Nash best");
		// BidDetails outcome = pickNashBestBid(fortunate, nice,
		// concession);
		// LOG.info("Returning " + outcome.getBid().toString()
		// + " as next bid.");
		// return outcome;
		// } catch (Exception e) {
		// LOG.severe("Error in picking best bid: " + e.toString());
		// return null;
		// }
		// } else {
		// LOG.severe("No outcome space available!");
		// return null;
		// }
	}

	/**
	 * Determines the bid closest to the utility of '1' if the outcome space is
	 * available. Otherwise, it returns the max bid in the domain.
	 * 
	 * @return A BidDetails object with an utility close to '1'.
	 */
	@Override
	public BidDetails determineOpeningBid() {

		LOG.info("Determining opening bid.");

		BidDetails bid = null;

		if (outcomeSpace != null) {
			bid = outcomeSpace.getBidNearUtility(1);
		} else {
			LOG.severe("Outcomespace was unavailable");
			bid = session.getMaxBidinDomain();
		}

		LOG.info("Returning " + bid.getBid().toString() + " as opening bid.");

		return bid;
	}

	/**
	 * Returns the best bid given in bids, based on the product of the utility
	 * of the bid for this agent and for the opponent. The latter utility is
	 * based on the opponentModel. An arbitrary number of BidDetails can be
	 * given.
	 * 
	 * @param bids
	 *            An arbitrary number of BidDetails.
	 * @return The BidDetails object with the highest product of the utility for
	 *         the agent and its opponent
	 * @throws Exception
	 *             if the bestBid was not set in the inspection of bids.
	 *             Indicates further issues.
	 */
	private BidDetails pickNashBestBid(BidDetails... bids) throws Exception {

		BidDetails bestBid = null;
		double bestUtil = -1;

		for (BidDetails bid : bids) {
			double oppUtil = opponentModel.getBidEvaluation(bid.getBid());
			double ownUtil = session.getDiscountedUtility(bid.getBid(),
					session.getTime());

			double nash = oppUtil * ownUtil;
			if (nash > bestUtil) {
				bestUtil = nash;
				bestBid = bid;
			}
		}

		if (bestBid == null) {
			LOG.severe("BestBid was null, using nice bid");
			return bids[2];
		}

		return bestBid;
	}

	/**
	 * Returns a factor between the reservation value and 1 that represents the
	 * time influence. This is a linear function.
	 * 
	 * @param time
	 *            for which to calculate the influence.
	 * @return a double which is the factor.
	 */
	private Double calculateTimeInfluencedUtilityGoal(double time) {
		return reservationValue + (1 - reservationValue) * (1 - time);
	}
}
