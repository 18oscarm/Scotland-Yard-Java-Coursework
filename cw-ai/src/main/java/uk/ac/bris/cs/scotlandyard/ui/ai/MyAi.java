package uk.ac.bris.cs.scotlandyard.ui.ai;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import com.google.common.collect.ImmutableSet;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;

public class MyAi implements Ai {

	@Nonnull @Override public String name() { return "ACABn't"; }
	private double score(Board board, List<Integer> distances, int destination, Move move) {
		double currentscore;
		currentscore = (scoreDegreeOfNode(board, destination))
				+ scoreAverageDetectiveDistance(board, distances)
				+ (3 * scoreClosestDetectiveDistance(board, distances))
				+ scoreDoubleDove(move)
				+ scoreSecretMove(move)
				+ scoreDistanceToEdge(distances);
		return currentscore;
	} //Calculates a score for a given move based on the distances from the detectives, the tickets used, the distance
	  //from the edge and how many nodes are adjacent to the destination.

	private int scoreClosestDetectiveDistance(Board board, List<Integer> distances){
		int minDistance = 200;
		ImmutableSet<Piece> Players = board.getPlayers();
		for (Piece i : Players){
			if (!i.isMrX()){
				if (distances.get(board.getDetectiveLocation((Piece.Detective) i).get()) < minDistance){
					minDistance = board.getDetectiveLocation((Piece.Detective) i).get();
				}
			}
		}
		if (minDistance == 1) {return -10;}
		else {return minDistance;}
	} //Returns the distance from the closest detective if it's more than 1, otherwise returns -10 as a penalty to the
	  //score for being next to a detective.

	private int scoreAverageDetectiveDistance(Board board, List<Integer> distances){
		ImmutableSet<Piece> players = board.getPlayers();
		int sum = 0;
		int average;
		for (Piece i: players){
			if (i.isDetective()) {sum += distances.get(board.getDetectiveLocation((Piece.Detective) i).get());}
		}
		average = sum/ (players.size() - 1);
		return average;
	} //Returns the average distance from the detectives.

	private int scoreDoubleDove(Move move){
		Move.Visitor<Boolean> visitor = new MoveDoubleVisitor();
		boolean isDouble = move.accept(visitor); //Visitor pattern used to find if the non-specific move is a single or
												 //double.
		if (isDouble) {return 0;}
		else {return 3;}
	} //Returns 0 if the move is a double move, returns 3 if not to increase score of single moves.

	private int scoreSecretMove(Move move){
		boolean isSecret = false;
		for (ScotlandYard.Ticket t : move.tickets()){
			if (t == ScotlandYard.Ticket.SECRET) {
				isSecret = true;
				break;
			}
		}
		if (isSecret) {return 0;}
		else {return 3;}
	} //Returns 0 if the move is a secret move, returns 3 if not to increase score of non-secret moves.

	private int scoreDistanceToEdge(List<Integer> distances){
		ImmutableSet<Integer> edgeset =
				ImmutableSet.of(1,2,3,4,5,6,7,8,18,43,57,73,92,93,120,144,176,189,190,192,194,195,197,185,195,199,171,175,112,136,114,107,91,56,30,17);
		//This is the all the nodes that are at the edges of the board.
		int minDistance = 200;
		for (int i:edgeset){
			if (distances.get(i) < minDistance){minDistance = distances.get(i);}
		}
		return minDistance;
	} //Returns the minimum distance away from nodes at the edges of the board.

	private int scoreDegreeOfNode(Board board, int destination){
		int adjNodes = board.getSetup().graph.degree(destination);
		return Math.min(adjNodes, 6);
	} //Returns the amount of adjacent nodes to the input destination.


	private List<Integer> findDistancesFromNode(Board board, int source){
		List<Integer> distanceList = new ArrayList<>();
		List<Integer> finishedNodes = new ArrayList<>();
		distanceList.add(0); //Adds irrelevant value to index 0 to line up n in the follwing for loop with the .add
							 //function of the array.
		for (int n : board.getSetup().graph.nodes()){
			if (n != source){distanceList.add(board.getSetup().graph.nodes().size());}
			else {distanceList.add(0);} //Distance to the source node from the source node is 0.
		}
		while (finishedNodes.size() < board.getSetup().graph.nodes().size()){
			int minDistance = board.getSetup().graph.nodes().size();
			int minNode = 0;
			for(int i :board.getSetup().graph.nodes()){
				if (distanceList.get(i) < minDistance && !finishedNodes.contains(i)) {
					minDistance = distanceList.get(i);
					minNode = i;
				}
			} //Finds the node with the shortest distance that hasn't been finalised yet.
			finishedNodes.add(minNode);
			for(int j : board.getSetup().graph.adjacentNodes(minNode)){
				int altDistance = minDistance + 1;
				if (altDistance < distanceList.get(j)){distanceList.set(j, altDistance);}
			} //Checks all adjacent nodes to the current smallest node and updates their distance if it is smaller by
			  //moving to the current smallest node and then to it.
		}
		return distanceList;
	} //Uses Dijkstra's algorithm to find the shortest route to each node in the graph from a specific node.

	@Nonnull @Override public Move pickMove(
			@Nonnull Board board,
			Pair<Long, TimeUnit> timeoutPair) {
		var moves = board.getAvailableMoves().asList();
		double maxScore = -99;
		Move maxMove = null;
		Move defaultMove = moves.get(0);
		Move.Visitor<Integer> visitor = new MoveDestinationVisitor();
		for (Move i : moves){
			int moveDestination = i.accept(visitor); //Uses the visitor pattern to get the final destination for each
													 //move.
			List<Integer> distanceList = findDistancesFromNode(board, moveDestination);
			double moveScore = score(board,distanceList, moveDestination, i);
			if (moveScore > maxScore){
				maxScore = moveScore;
				maxMove = i;
			} //If the score of the current move is larger than the current highest score, then the best move is set to
			  //the current move.
		}
		return maxMove; //Return the best scoring move.
	}//Takes all possible moves and scores them, then returns the best move.
}
