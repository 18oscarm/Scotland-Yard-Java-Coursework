package uk.ac.bris.cs.scotlandyard.model;

import java.util.ArrayList;

public class MoveVisitor implements Move.Visitor<ArrayList<Move.SingleMove>>{
    @Override
    public ArrayList<Move.SingleMove> visit(Move.SingleMove move) {
        ArrayList<Move.SingleMove> array = new ArrayList<>();
        array.add(move);
        return array;
    } //If the input move is a single move, returns that move in an array.

    @Override
    public ArrayList<Move.SingleMove> visit(Move.DoubleMove move) {
        ArrayList<Move.SingleMove> array = new ArrayList<>();
        array.add(new Move.SingleMove(move.commencedBy(), move.source(), move.ticket1, move.destination1));
        array.add(new Move.SingleMove(move.commencedBy(), move.destination1, move.ticket2, move.destination2));
        return array;
    } //If the input move is a double move, returns both moves in an array.
}