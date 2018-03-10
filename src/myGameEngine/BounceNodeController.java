package myGameEngine;

import ray.rage.scene.*;
import ray.rage.scene.controllers.*;
import ray.rml.*;

public class BounceNodeController extends AbstractController {
	private float cycleTime = 5000.0f;
	private float totalTime = 0.0f;
	private float direction = 1.0f;
	
	@Override
	protected void updateImpl(float elapsedTimeMills) {
		totalTime += elapsedTimeMills;
		
		if (totalTime > cycleTime) {
			direction = -direction;
			totalTime = 0.0f;
		}
		
		for( Node n: super.controlledNodesList ) {
			Vector3 curBounce = n.getLocalPosition();
			curBounce = Vector3f.createFrom(curBounce.x(), curBounce.y() + 0.01f * direction, curBounce.z());
			n.setLocalPosition(curBounce);
		}
	}

}
