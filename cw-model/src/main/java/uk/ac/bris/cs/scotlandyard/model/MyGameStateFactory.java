package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import javax.annotation.Nonnull;
import java.io.IOException;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;
import java.util.*;

import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.standardGraph;


/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {
	private final class MyGameState implements GameState {
		private final GameSetup setup;
		private final ImmutableSet<Piece> remaining;
		private ImmutableList<LogEntry> log;
		private Player mrX;
		private final List<Player> detectives;
		private ImmutableSet<Move> moves;
		private final ImmutableSet<Piece> winner;
		private final ImmutableSet<Piece> players;

		private MyGameState(
				final GameSetup setup,
				final ImmutableSet<Piece> remaining,
				final ImmutableList<LogEntry> log,
				final Player mrX,
				final List<Player> detectives) {
			this.setup = initialiseSetup(setup);
			this.remaining = initialiseRemaining(remaining);
			this.log = initialiseLog(log);
			this.mrX = initialiseMrX(mrX);
			this.detectives = initialiseDetectives(detectives);
			this.players = initialisePlayerSet();
			this.moves = ImmutableSet.copyOf(initialiseAvailableMoves());
			this.winner = initialiseWinner(); //All attributes of GameState initialised with their own method.
			if (!winner.isEmpty()){moves = ImmutableSet.of();} //Moves must be empty when there is a winner.
			try {
				assert (this.setup.graph != standardGraph());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Nonnull
		@Override
		public GameSetup getSetup() {
			return this.setup;
		} //Returns game setup.

		@Nonnull
		@Override
		public ImmutableSet<Piece> getPlayers() {
			return this.players;
		} //Returns a set of all the players.

		@Nonnull
		@Override
		public Optional<Integer> getDetectiveLocation(Piece.Detective inputDetective) {
			Player playerDetective = pieceToPlayer(inputDetective);
			if (playerDetective != null) return Optional.of(playerDetective.location());
			else return Optional.empty();
		} //Returns the location of the Player with the inputted piece, or empty if the piece does not correspond to a
		  // player.

		@Nonnull
		@Override
		public Optional<TicketBoard> getPlayerTickets(Piece piece) {
			Player player = pieceToPlayer(piece);
			if (player != null) {
				return Optional.of(player.tickets()).map(tickets -> ticket -> tickets.getOrDefault(ticket, 0));
			} else return Optional.empty();
		} //Returns the tickets belonging to the player corresponding to the inputted piece.

		@Nonnull
		@Override
		public ImmutableList<LogEntry> getMrXTravelLog() {return this.log;} //Returns Mr X's travel log.

		@Nonnull
		@Override
		public ImmutableSet<Piece> getWinner() {return winner;} //Returns the set of winners at the current point in the
																//game. Output is an empty set if there is not yet a
																//winner.

		@Nonnull
		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			return moves;
		} //Returns all available moves at the current
																		//point in the game.

		private GameSetup initialiseSetup(GameSetup inputSetup){
			if(inputSetup == null){throw new NullPointerException("setup parameter is null!");}
			else if(inputSetup.moves.isEmpty()){throw new IllegalArgumentException("Moves is empty!");}
			else if(inputSetup.graph.nodes().isEmpty()){throw new IllegalArgumentException("Graph is empty!");}
			else {return inputSetup;}
		} // Checks the input parameter is valid and then returns the parameter.

		private ImmutableSet<Piece> initialiseRemaining(ImmutableSet<Piece> inputRemaining){
			if (inputRemaining == null){throw new NullPointerException("remaining parameter is null!");}
			else {return inputRemaining;}
		} // Checks the input parameter is valid and then returns the parameter.

		private ImmutableList<LogEntry> initialiseLog(ImmutableList<LogEntry> inputLog){
			if (inputLog == null){throw new NullPointerException("log parameter is null!");}
			else {return inputLog;}
		} // Checks the input parameter is valid and then returns the parameter.

		private Player initialiseMrX(Player inputMrX){
			if (inputMrX == null){throw new NullPointerException("mrX parameter is null!");}
			else if (inputMrX.isDetective()) {throw new IllegalArgumentException("mrX parameter is not Mr X!");}
			else {return inputMrX;}
		} // Checks the input parameter is valid and then returns the parameter.

		private List<Player> initialiseDetectives(List<Player> inputDetectives){
			if (inputDetectives == null) {throw new NullPointerException("detectives parameter is null!");}
			for (Player detective1 : inputDetectives){
				if (detective1.isMrX()){throw new IllegalArgumentException("A detective is Mr X!");}
				else if (detective1.has(ScotlandYard.Ticket.SECRET)){throw new IllegalArgumentException("Detective has secret ticket!");}
				else if (detective1.has(ScotlandYard.Ticket.DOUBLE)){throw new IllegalArgumentException("Detective has double ticket!");}
				for (Player detective2 : inputDetectives){
					if (detective1.piece() == detective2.piece() && detective1 != detective2){throw new IllegalArgumentException("Two detectives are the same piece!");}
					else if (detective1.location() == detective2.location() && detective1 != detective2){throw new IllegalArgumentException("Two detectives are in the same location!");}
				}
			}
			return inputDetectives;
		} // Checks the input parameter is valid and then returns the parameter.

		private ImmutableSet<Piece> initialisePlayerSet() {
			HashSet<Piece> playerSet = new HashSet<>();
			ImmutableSet<Piece> immutablePlayerSet;
			int i;
			for (i = 0; i < detectives.size(); i += 1) playerSet.add(detectives.get(i).piece());
			playerSet.add(mrX.piece());
			immutablePlayerSet = ImmutableSet.copyOf(playerSet);
			return immutablePlayerSet;
		} //Makes and returns a set with the pieces of all players in it from Mr X and the detectives.

		private Set<Move> initialiseAvailableMoves(){
			Player currentPlayer = null;
			Set<Move.SingleMove> currentPlayerSingleMoves;
			Set<Move> totalPlayerMoves = new HashSet<>();
			if (this.remaining.contains(this.mrX.piece())){
				currentPlayerSingleMoves = makeSingleMoves(this.setup, this.detectives, this.mrX, this.mrX.location());
				totalPlayerMoves.addAll(makeSingleMoves(this.setup, this.detectives, this.mrX, this.mrX.location()));
				if(this.mrX.has(ScotlandYard.Ticket.DOUBLE) && this.setup.moves.size() - log.size() >= 2){
					totalPlayerMoves.addAll(makeDoubleMoves(this.setup, this.detectives, this.mrX, this.mrX.location(), currentPlayerSingleMoves));
				}
			} //If Mr X is in remaining, finds all single moves he can make and then, if he has any double tickets,
			  //finds all the double moves he can make as well.
			else {
				for (Piece p : this.remaining) {
					for (Player q : this.detectives) {
						if (p == q.piece()) {currentPlayer = q;}
					}
					if (currentPlayer == null){throw new NullPointerException("On detectives turn, a piece in remaining does not belong to a detective");}
					totalPlayerMoves.addAll(makeSingleMoves(this.setup, this.detectives, currentPlayer, currentPlayer.location()));
				}
			}//If Mr X is not in remaining, finds all the single moves for all remaining detectives.
			return totalPlayerMoves;
		} //Finds and returns all possible moves at the current point in the game.

		private ImmutableSet<Piece> initialiseWinner(){
			HashSet<Piece> detectivesPieces = new HashSet<>();
			ImmutableSet<Piece> foundWinner;
			boolean MrXCaught = false;
			int DetectivesWithTickets = this.detectives.size();
			for (Player p : this.detectives) {
				detectivesPieces.add(p.piece());
			} //Adds the pieces of all detectives to the
			for (Player i : this.detectives) {
				if (!i.has(ScotlandYard.Ticket.TAXI)
						&& !i.has(ScotlandYard.Ticket.UNDERGROUND)
						&& !i.has(ScotlandYard.Ticket.BUS)){
					DetectivesWithTickets = DetectivesWithTickets - 1;
				} //Checks if each detective has any tickets left.
				if (i.location() == this.mrX.location()) {
					MrXCaught = true;
				} //Checks if each detective is on Mr X's node.
			}
			if (MrXCaught) {foundWinner = ImmutableSet.copyOf(detectivesPieces);}
			else if (DetectivesWithTickets == 0){foundWinner = ImmutableSet.of(this.mrX.piece());}
			else if ((getMrXTravelLog().size() == this.setup.moves.size()) && this.remaining.contains(this.mrX.piece())) {
				foundWinner = ImmutableSet.of(this.mrX.piece());
			}
			else if (getAvailableMoves().isEmpty() && this.remaining.contains(this.mrX.piece())){foundWinner = ImmutableSet.copyOf(detectivesPieces);}
			else {foundWinner = ImmutableSet.of();}
			return foundWinner;
		} //Deduces the winner/winners at the current point in the game.

		private static Set<Move.SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source) {
			boolean noDetective;
			HashSet<Move.SingleMove> totalSingleMoves = new HashSet<>();
			for (int destination : setup.graph.adjacentNodes(source)) {
				noDetective = true;
				for (Player d : detectives) {
					if (destination == d.location()) {noDetective = false;}
				} //For all possible destinations, checks if there is a detective at that node.
				for (ScotlandYard.Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of())) {
					if (player.has(t.requiredTicket()) && noDetective) {
						totalSingleMoves.add(new Move.SingleMove(player.piece(), source, t.requiredTicket(), destination));
					}
				} //For all possible destinations and modes of transport, checks if Mr X has the needed ticket and adds
				  //the move if no detective is at that node.
				for (ScotlandYard.Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of())) {
					if (noDetective && player.has(ScotlandYard.Ticket.SECRET) && t != ScotlandYard.Transport.FERRY) {
						totalSingleMoves.add(new Move.SingleMove(player.piece(), source, ScotlandYard.Ticket.SECRET, destination));
					}
				} //For all possible destinations, checks if Mr X has a secret ticket and adds the move if no detective
				  //is at that node.
			}
			return totalSingleMoves;
		} //Finds all possible single moves an input player can make from the node they are on.

		private static Set<Move.DoubleMove> makeDoubleMoves(GameSetup setup, List<Player> detectives, Player player, int source, Set<Move.SingleMove> singleMoves){
			Set<Move.DoubleMove> doubleMoves = new HashSet<>();
			Set<Move.SingleMove> secondMoves;
			Map<ScotlandYard.Ticket, Integer> ticketMap;
			for(Move.SingleMove m : singleMoves){
				secondMoves = makeSingleMoves(setup, detectives, player, m.destination);
				for(Move.SingleMove n : secondMoves){
					if(m.ticket == n.ticket){
						ticketMap = player.tickets();
						if(ticketMap.get(m.ticket) > 1){doubleMoves.add(new Move.DoubleMove(player.piece(), source, m.ticket, m.destination, n.ticket, n.destination));}
					}
					else{doubleMoves.add(new Move.DoubleMove(player.piece(), source, m.ticket, m.destination, n.ticket, n.destination));}
				}
			} //For every single move the player can make, uses makeSingleMove to find all second moves they could make
			  //and, provided they have enough tickets, adds the double move.
			return doubleMoves;
		} //Finds all possible double moves an input player can make from the node they are on.
		@Nonnull
		@Override
		public GameState advance(Move move) {
			ImmutableSet<Piece> newRemaining;
			ImmutableList<LogEntry> newLog = this.log;
			Player newMrX;
			List<Player> newDetectives = this.detectives;
			ArrayList<Move.SingleMove> moveArray;
			Player updatedPlayer;
			if(!this.moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);
			else{
				Move.Visitor<ArrayList<Move.SingleMove>> visitor = new MoveVisitor();
				moveArray = move.accept(visitor); //Implements the visitor pattern to return an array containing either
												  //one or two moves depending on the type of move.
				for (Move.SingleMove i : moveArray){
					newLog = updateLog(i, newLog);
				} //Updates the log for each move in the array from the visitor.
				if(moveArray.size() == 1){updatedPlayer = resolveSingleMove(moveArray.get(0));}
				else{updatedPlayer = resolveDoubleMove(moveArray);}
				if(move.commencedBy() == this.mrX.piece()){newMrX = updatedPlayer;}
				else{
					newDetectives = new ArrayList<>();
					newMrX = this.mrX;
					for(Player d : this.detectives){
						if(d.piece() == move.commencedBy()){
							newDetectives.add(updatedPlayer);
						}
						else{newDetectives.add(d);}
					}
				}//Deduces which player made the move and ensures they end up being returned new.
				newRemaining = updateRemaining(move.commencedBy());
			}
			return new MyGameState(this.setup, newRemaining, newLog, newMrX, newDetectives);
		} //Takes a move being made and updates the gamestate accordingly.

		private ImmutableList<LogEntry> updateLog(Move.SingleMove move , ImmutableList<LogEntry> oldlog) {
			if (move.commencedBy() == mrX.piece()) {
				ArrayList<LogEntry> newLog = new ArrayList<>(oldlog);
				if (this.setup.moves.get(log.size())) {newLog.add(LogEntry.reveal(move.ticket, move.destination));}
				else {newLog.add(LogEntry.hidden(move.ticket));}
				this.log = ImmutableList.copyOf(newLog);
			}
			return this.log;
		} //Updates the log for a single move.

		private Player resolveSingleMove(Move.SingleMove move){
			Player movingPlayer = pieceToPlayer(move.commencedBy());
			movingPlayer = movingPlayer.at(move.destination);
			movingPlayer = movingPlayer.use(move.ticket);
			if(movingPlayer.isDetective()) {mrX = mrX.give(move.ticket);}
			return movingPlayer;
		} //Changes the location of the moving player and uses/gives the required tickets.

		private Player resolveDoubleMove(ArrayList<Move.SingleMove> moves) {
			if (moves.size() != 2){throw new IllegalArgumentException("Moves in array do not make up a double move!");}
			else {
				Move.SingleMove secondMove = moves.get(moves.size() -1);
				Player movingPlayer = pieceToPlayer(secondMove.commencedBy());
				movingPlayer = movingPlayer.at(secondMove.destination);
				movingPlayer = movingPlayer.use(ScotlandYard.Ticket.DOUBLE);
				for (Move.SingleMove m :moves){movingPlayer = movingPlayer.use(m.tickets());}
				return movingPlayer;
			}
		} //Changes the location of the moving player and uses/gives the required tickets. Takes an array of 2 moves
		  //rather than a double move.

		private ImmutableSet<Piece> updateRemaining(Piece justMoved){
			HashSet<Piece> newRemaining = new HashSet<>();
			ImmutableSet<Piece> immutableNewRemaining;
			if(justMoved.isMrX()){
				for(Player p : this.detectives){
					if(p.has(ScotlandYard.Ticket.TAXI) || p.has(ScotlandYard.Ticket.BUS) || p.has(ScotlandYard.Ticket.UNDERGROUND)){newRemaining.add(p.piece());}
				}
			} //If Mr X has just moved, makes all detectives with tickets the new remaining players.
			else{
				if(this.remaining.size() > 1){
					for(Piece p : this.remaining){
						if(p != justMoved){newRemaining.add(p);}
					}
				} //Adds all detectives that have not moved yet to the new remaining players if there was multiple
				  //detectives left to go.
				else{newRemaining.add(this.mrX.piece());} //If there is only one detective left, then Mr X is the new
													 //remaining player.
			}
			immutableNewRemaining = ImmutableSet.copyOf(newRemaining);
			return  immutableNewRemaining;
		} //Decides from the player who just moved who is now left to go.

		private Player pieceToPlayer(Piece piece) {
			if(piece.isMrX()){return mrX;}
			else {
				for (Player p : detectives) {
					if (piece == p.piece()) {
						return p;
					}
				}
			}
			return null;
		} //Returns the player using an inputted piece.
	}
	@Nonnull @Override public GameState build(GameSetup setup,
											  Player mrX,
											  ImmutableList<Player> detectives) {
		return new MyGameState(setup, ImmutableSet.of(Piece.MrX.MRX), ImmutableList.of(), mrX, detectives);
	} //Creates and returns a new gamestate.

}
