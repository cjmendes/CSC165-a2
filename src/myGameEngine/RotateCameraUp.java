package myGameEngine;

import a2.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Node;
import ray.rml.Degreef;

public class RotateCameraUp extends AbstractInputAction {

	private Node avN;
	private MyGame game;
	
	public RotateCameraUp(Node n, MyGame g) {
		game = g;
		avN = n;
	}
	
	public void performAction(float time, Event event) {
		avN.pitch(Degreef.createFrom(-3f));
	}
}
