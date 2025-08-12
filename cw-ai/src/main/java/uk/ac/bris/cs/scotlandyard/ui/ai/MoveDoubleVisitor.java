package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Move;

public class MoveDoubleVisitor implements Move.Visitor<Boolean>{
    @Override
    public Boolean visit(Move.SingleMove move) {return false;} //Returns false if the move is a single move.

    @Override
    public Boolean visit(Move.DoubleMove move) {return true;} //Returns true if the move is a double move.
}
