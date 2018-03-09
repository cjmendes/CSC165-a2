package myGameEngine;

import a2.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Node;
import ray.rml.Degreef;

public class RotateCameraLeft extends AbstractInputAction {

	private Node avN;
	private MyGame game;
	
	public RotateCameraLeft(Node n, MyGame g) {
		game = g;
		avN = n;
	}
	
	public void performAction(float time, Event event) {
		avN.yaw(Degreef.createFrom(3f));
		//game.getEngine().getSceneManager().getSceneNode(game.getActiveNode().getName()).yaw(Degreef.createFrom(3f));
	}
}
