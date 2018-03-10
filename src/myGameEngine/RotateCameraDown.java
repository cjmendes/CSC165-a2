package myGameEngine;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Node;
import ray.rml.Degreef;

public class RotateCameraDown extends AbstractInputAction {

	private Node avN;
	
	public RotateCameraDown(Node n) {
		avN = n;
	}
	
	public void performAction(float time, Event event) {
		avN.pitch(Degreef.createFrom(3f));
		//game.getEngine().getSceneManager().getSceneNode(game.getActiveNode().getName()).pitch(Degreef.createFrom(3f));
	}
}