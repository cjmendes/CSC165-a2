package myGameEngine;

import a2.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Node;

public class MoveRightAction extends AbstractInputAction {

	private Node avN;
	private MyGame game;
	
	public MoveRightAction(Node n, MyGame g) {
		avN = n;
		game = g;
	}
	
	public void performAction(float time, Event event) {
		if(game.getSprint())
			avN.moveLeft(game.getSpeed() * 0.02f);
		else
			avN.moveLeft(game.getSpeed());
	}
}
