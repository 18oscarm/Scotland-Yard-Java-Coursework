package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.HashSet;

/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory implements Factory<Model> {
	private final class MyModel implements Model{
		private Board.GameState state;
		private ImmutableSet<Observer> observerSet;
		private MyModel(GameSetup setup,
						Player mrX,
						ImmutableList<Player> detectives){
			ScotlandYard.Factory<Board.GameState> stateFactory = new MyGameStateFactory();
			this.state = stateFactory.build(setup, mrX, detectives);
			this.observerSet =  ImmutableSet.of(); //At initialisation there are no observers.
		}
		@Nonnull
		@Override
		public Board getCurrentBoard() {return this.state;} //Returns the current gamestate.


		@Override
		public void registerObserver(@Nonnull Observer observer) {
			HashSet<Observer> newObserverSet = new HashSet<>(this.observerSet);
			if (observer == null){throw new NullPointerException("Observer parameter is null");}
			else if(observerSet.contains(observer)){throw new IllegalArgumentException("observer is already registered");}
			else{newObserverSet.add(observer);}
			this.observerSet = ImmutableSet.copyOf(newObserverSet);
		} //Checks the input observer is not null and not already registered and then adds the observer to the set of
		  //observers

		@Override
		public void unregisterObserver(@Nonnull Observer observer) {
			HashSet<Observer> newObserverSet = new HashSet<>(this.observerSet);
			if (observer == null){throw new NullPointerException("observer parameter is null");}
			else if(observerSet.contains(observer)){newObserverSet.remove(observer);}
			else{throw new IllegalArgumentException("observer is not registered");}
			this.observerSet = ImmutableSet.copyOf(newObserverSet);
		} //Checks the input observer is not null and is already registered and then removes the observer from the set
		  //of observers.

		@Nonnull
		@Override
		public ImmutableSet<Observer> getObservers() {return observerSet;} //Returns the set of observers.

		@Override
		public void chooseMove(@Nonnull Move move) {
			this.state = state.advance(move);
			ImmutableSet<Piece> winner = this.state.getWinner();
			if (winner.isEmpty()){
				for(Observer i : observerSet){i.onModelChanged(this.state, Observer.Event.MOVE_MADE);}
			} //If there is not yet a winner, notifies all observers that a move has been made.
			else{
				for(Observer i : observerSet){i.onModelChanged(this.state, Observer.Event.GAME_OVER);}
			} //If there is a winner, notifies all the observers that the game is over.
		} //Advances the gamestate with one move and notifies observers accordingly.
	}
	@Nonnull @Override public Model build(GameSetup setup,
										  Player mrX,
										  ImmutableList<Player> detectives) {
		return new MyModel(setup, mrX, detectives);
	} //Creates and returns a new model.
}
