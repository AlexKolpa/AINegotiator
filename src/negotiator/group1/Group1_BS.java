package negotiator.group1;

import negotiator.bidding.BidDetails;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OMStrategy;
import negotiator.boaframework.OfferingStrategy;
import negotiator.boaframework.OpponentModel;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Group1_BS extends OfferingStrategy {

	private static final Logger LOG = Logger.getLogger(Group1_BS.class.getName());
	private NegotiationSession session;
	private OpponentModel opponentModel;
	private OMStrategy opponentModelStrategy;
	private Double minUtility, maxUtility, concessionRate, offset;

	@Override
	public void init(NegotiationSession negotiationSession, OpponentModel opponentModel, OMStrategy omStrategy, HashMap<String, Double> parameters) {
		// LOG should post every single log message for debugging.
		LOG.setLevel(Level.ALL);

		this.session = negotiationSession;
		this.opponentModel = opponentModel;
		this.opponentModelStrategy = omStrategy;

		if (!Double.isNaN(parameters.get("e"))) {
			this.concessionRate = parameters.get("e");
		} else{
			LOG.severe("Concession rate value 'e' not set in paramaters");
		}
		if (!Double.isNaN(parameters.get("k"))) {
			this.offset = parameters.get("k");
		}
		if (!Double.isNaN(parameters.get("min"))) {
			this.minUtility = parameters.get("min");
		}
		if (!Double.isNaN(parameters.get("max"))) {
			this.maxUtility = parameters.get("max");
		}
	}

	@Override
	public BidDetails determineNextBid() {

		LOG.info("Determining next bid..");

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BidDetails determineOpeningBid() {

		LOG.info("Determining opening bid..");


		/* Base bid on:
		Time - Less time left, more concession steps; perhaps nice steps
		Utility - Maximize!
		Opponent Model - Hopefully be able to determine what a bid means for the opponent's utility.
		 */

		// TODO Auto-generated method stub
		return null;
	}

}
