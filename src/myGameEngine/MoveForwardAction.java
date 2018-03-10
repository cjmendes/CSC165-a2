package myGameEngine;

import a2.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Node;

public class MoveForwardAction extends AbstractInputAction {

	private Node avN;
	private MyGame game;
	
	public MoveForwardAction(Node n, MyGame g) {
		avN = n;
		game = g;
	}
	
	public void performAction(float time, Event event) {
		if (avN.getName().equals("dolphinNode")) {
			if(game.getSprint(1))
				avN.moveForward(game.getSpeed() * 0.02f);
			else
				avN.moveForward(game.getSpeed());
		}
		else {
			if(game.getSprint(2))
				avN.moveForward(game.getSpeed() * 0.02f);
			else
				avN.moveForward(game.getSpeed());
		}
	}
}
