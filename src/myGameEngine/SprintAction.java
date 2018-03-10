package myGameEngine;

import a2.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Node;

public class SprintAction extends AbstractInputAction {

	private MyGame game;
	private Node avN;
	
	public SprintAction(Node n, MyGame g) {
		game = g;
		avN = n;
	}
	
	public void performAction(float time, Event event) {
		if (avN.getName().equals("dolphinNode")) {
			if (game.getBoost(1) > 0) {
				game.changeSprint(game.getSprint(1));
				game.consumeBoost(1);
			}
		}
		else {
			if (game.getBoost(2) > 0) {
				game.changeSprint(game.getSprint(2));
				game.consumeBoost(2);
			}
		}
	}
}