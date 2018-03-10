package myGameEngine;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Node;
import ray.rml.Degreef;

public class RotateCameraUp extends AbstractInputAction {

	private Node avN;
	
	public RotateCameraUp(Node n) {
		avN = n;
	}
	
	public void performAction(float time, Event event) {
		avN.pitch(Degreef.createFrom(-3f));
	}
}
