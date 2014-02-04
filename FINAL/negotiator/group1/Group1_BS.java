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
	private Double concessionRate;

	@Override
	public void init(NegotiationSession negotiationSession,
			OpponentModel opponentModel, OMStrategy omStrategy,
			HashMap<String, Double> parameters) {

		LOG = Logger.getGlobal();
		LOG.setLevel(Level.SEVERE);

		this.session = negotiationSession;
		this.opponentModel = opponentModel;
		this.opponentModelStrategy = omStrategy;

		outcomeSpace = new SortedOutcomeSpace(
				negotiationSession.getUtilitySpace());
		negotiationSession.setOutcomeSpace(outcomeSpace);

		// Check arguments
		if (this.session == null) {
			LOG.severe("Negotiation session unavailable");
		}
		if (this.opponentModel == null) {
			LOG.severe("Opponent Model unavailable");
		}
		if (this.opponentModelStrategy == null) {
			LOG.severe("Opponent Model Strategy unavailable");
		}

		// Retrieve concession rate parameter
		if (checkParameter(parameters.get("concessionRate"))) {
			this.concessionRate = parameters.get("concessionRate");
		} else {
			this.concessionRate = 0.3;
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
	 * Determines the next bid based on the UtilityGoal, OpponentModel, and
	 * Time.
	 * 
	 * @return The BidDetails that has the best utility value product.
	 */
	@Override
	public BidDetails determineNextBid() {

		// Utility goal to base range upon
		double utilityGoal = calculateTimeInfluencedUtilityGoal(session
				.getTime());

		// Utility range in which bids are sought
		misc.Range range = new misc.Range(0.9 * utilityGoal, 1.1 * utilityGoal);

		List<BidDetails> possibleBids = outcomeSpace.getBidsinRange(range);
		if (possibleBids == null || possibleBids.isEmpty()) {
			LOG.severe("Possible bids is emtpy or null: "
					+ possibleBids.toString());
		}

		BidDetails bidWithTopOpponentUtility = opponentModelStrategy
				.getBid(possibleBids);

		if (bidWithTopOpponentUtility != null) {
			return bidWithTopOpponentUtility;
		} else {
			LOG.severe("BidWithTopOpponentUtility was null");
		}
		return bidWithTopOpponentUtility;
	}

	/**
	 * Determines the bid closest to the utility of '1' if the outcome space is
	 * available. Otherwise, it returns the max bid in the domain.
	 * 
	 * @return A BidDetails object with an utility close to '1'.
	 */
	@Override
	public BidDetails determineOpeningBid() {

		BidDetails bid = null;

		if (outcomeSpace != null) {
			bid = outcomeSpace.getBidNearUtility(1);
		} else {
			LOG.severe("Outcomespace was unavailable");
			bid = session.getMaxBidinDomain();
		}

		return bid;
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

		// The moment until which we do not budge.
		double hardheadedUntil = 0.2;

		if (time < hardheadedUntil) {
			return 1.0;
		} else {
			double result = 1 - Math.pow(time, 1 / concessionRate);

			// LOG.info("utilityGoal, time: " + result + ", " + time);

			return result;
		}
	}
}
