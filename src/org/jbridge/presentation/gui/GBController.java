package org.jbridge.presentation.gui;

import org.gnubridge.core.Direction;
import org.gnubridge.core.East;
import org.gnubridge.core.Game;
import org.gnubridge.core.Hand;
import org.gnubridge.core.North;
import org.gnubridge.core.Player;
import org.gnubridge.core.South;
import org.gnubridge.core.West;
import org.gnubridge.core.bidding.Auctioneer;
import org.gnubridge.core.bidding.Bid;
import org.gnubridge.core.bidding.BiddingAgent;
import org.gnubridge.core.deck.NoTrump;
import org.gnubridge.core.deck.Trump;
import org.gnubridge.presentation.GameUtils;

public class GBController {

	private MainWindow view;
	private Auctioneer auction;
	Player human;
	Game holdPlayerCards;
	private Game game;

	public GBController(MainWindow view) {
		this.view = view;
		auction = new Auctioneer(West.i());
		holdPlayerCards = new Game(null);
		GameUtils.initializeRandom(holdPlayerCards.getPlayers(), 13);
		human = holdPlayerCards.getSouth();
		view.setCards(new Hand(human.getHand()));
		view.setAuction(auction);
		doAutomatedBidding();
		// fake bidding to get to the other page
//		auction.bid(new Bid(7, NoTrump.i()));
//		doAutomatedBidding();
//        playGame();
	}

	private void doAutomatedBidding() {
		while (!auction.biddingFinished()
				&& !auction.getNextToBid().equals(human.getDirection2())) {
			Hand hand = new Hand(holdPlayerCards.getPlayer(
					auction.getNextToBid().getValue()).getHand());
			BiddingAgent ba = new BiddingAgent(auction, hand);
			auction.bid(ba.getBid());
			view.auctionStateChanged();
		}

	}

	public void placeBid(int bidSize, String trump) {
		if (!auction.biddingFinished()) {
			if (!auction.getNextToBid().equals(human.getDirection2())) {
				view.getBiddingDisplay().display("Not your turn to bid");
				return;
			}
			Bid candidate = Bid.makeBid(bidSize, trump);
			if (!auction.isValid(candidate)) {
				view.getBiddingDisplay().display("Invalid bid");
				return;
			}
			auction.bid(candidate);
			view.getBiddingDisplay().display("Bid placed:" + candidate);
			doAutomatedBidding();
		}
		if (auction.biddingFinished()) {
			view.getBiddingDisplay().display(
					"BIDDING COMPLETE. High bid: " + auction.getHighBid());
		}
	}

	public void playGame() {
		view.getBiddingDisplay().display("Play game not implemented");
		game = makeGame(auction, holdPlayerCards);
		
		
		view.setGame(game, allowHumanToPlayIfDummy() );
//		doAutomatedPlay();
	}



	private Direction allowHumanToPlayIfDummy() {
		Direction newHuman = auction.getDummyOffsetDirection(human.getDirection2());
		if (North.i().equals(newHuman)) {
			newHuman = South.i();
		}
		return newHuman;
	}

	private Game makeGame(Auctioneer a, Game cardHolder) {
		Game result = new Game(a.getHighBid().getTrump());
		
		result.getNorth().init(cardHolder.getPlayer(a.getDummyOffsetDirection(North.i())).getHand());
		result.getEast().init(cardHolder.getPlayer(a.getDummyOffsetDirection(East.i())).getHand());
		result.getSouth().init(cardHolder.getPlayer(a.getDummyOffsetDirection(South.i())).getHand());
		result.getWest().init(cardHolder.getPlayer(a.getDummyOffsetDirection(West.i())).getHand());
		result.setNextToPlay(West.i().getValue());
		return result;
	}

}
