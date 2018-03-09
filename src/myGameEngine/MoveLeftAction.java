package myGameEngine;

import a2.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Node;

public class MoveLeftAction extends AbstractInputAction {

	private Node avN;
	private MyGame game;
	
	public MoveLeftAction(Node n, MyGame g) {
		avN = n;
		game = g;
	}
	
	public void performAction(float time, Event event) {
		if(game.getSprint())
			avN.moveRight(game.getSpeed() * 0.02f);
		else
			avN.moveRight(game.getSpeed());
	}
}
