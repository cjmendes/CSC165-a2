package myGameEngine;

import a2.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Node;

public class MoveBackwardAction extends AbstractInputAction {

	private Node avN;
	private MyGame game;
	
	public MoveBackwardAction(Node n, MyGame g) {
		avN = n;
		game = g;
	}
	
	public void performAction(float time, Event event) {
		if(game.getSprint())
			avN.moveBackward(game.getSpeed() * 0.02f);
		else
			avN.moveBackward(game.getSpeed());
	}
}
