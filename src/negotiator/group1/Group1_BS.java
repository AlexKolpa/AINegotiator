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
	
	private static final Logger LOG = Logger.getLogger(Group1_BS.class.getName());
	private NegotiationSession session;
	private OpponentModel opponentModel;
	private OMStrategy opponentModelStrategy;
	private Double minUtility, maxUtility, concessionRate, offset;

	@Override
	public void init(NegotiationSession negotiationSession, OpponentModel opponentModel, OMStrategy omStrategy, HashMap<String,Double> parameters){

		this.session = negotiationSession;
		this.opponentModel = opponentModel;
		this.opponentModelStrategy = omStrategy;

		if(!Double.isNaN(parameters.get("e"))){
			this.concessionRate = parameters.get("e");
		}
		if(!Double.isNaN(parameters.get("k"))){
			this.offset = parameters.get("k");
		}
		if(!Double.isNaN(parameters.get("min"))){
			this.minUtility = parameters.get("min");
		}
		if(!Double.isNaN(parameters.get("max"))){
			this.maxUtility = parameters.get("max");
		}

		// Log should post every single log message for debugging.
		LOG.setLevel(Level.ALL);
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

		Bid bid;



		double undiscountedUtil = session.get

		new BidDetails(bid, undiscountedUtil);


		// TODO Auto-generated method stub
		return null;
	}

}
