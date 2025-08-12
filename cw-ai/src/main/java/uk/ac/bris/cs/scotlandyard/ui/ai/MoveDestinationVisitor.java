package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Move;

public class MoveDestinationVisitor implements Move.Visitor<Integer>{
    @Override
    public Integer visit(Move.SingleMove move) {return move.destination;}

    @Override
    public Integer visit(Move.DoubleMove move) {return move.destination2;}
} //Single and double moves store the final destination of the move in different variable, so are accessed with this
  //implementation of the visitor pattern.