package com.gmail.bluballsman.themaze;

import org.bukkit.util.Vector;

public class TrigUtils {
	
	public static Vector getFixedDistanceCongruent(Vector start, Vector end, double fixedDistance) {
		Vector difference = end.clone().subtract(start);
		double currentDistance = end.distance(start);
		double factorToFix = fixedDistance / currentDistance;
		return difference.multiply(factorToFix);
	}
	
	
	
}
