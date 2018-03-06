package myGameEngine;

import a2.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;

public class SprintAction extends AbstractInputAction {

	private MyGame game;
	
	public SprintAction(MyGame g) {
		game = g;
		
	}
	
	public void performAction(float time, Event event) {
		if(game.getBoost() > 0) {
			game.changeSprint();
			game.consumeBoost();
		}
	}
}