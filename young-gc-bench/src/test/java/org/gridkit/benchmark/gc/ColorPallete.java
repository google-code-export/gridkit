package org.gridkit.benchmark.gc;

import java.awt.Color;

public class ColorPallete {

	private float sat;
	private float bright;
	private float hue;
	private float hueStep;
	
	public ColorPallete(float sat, float bright, float hue, float hueStep) {
		this.sat = sat;
		this.bright = bright;
		this.hue = hue;
		this.hueStep = hueStep;
	}

	public ColorPallete(double sat, double bright, double hue, double hueStep) {
		this.sat = (float) sat;
		this.bright = (float) bright;
		this.hue = (float) hue;
		this.hueStep = (float) hueStep;
	}


	public Color nextColor() {
		float h = hue;
		hue += hueStep;
		return new Color(Color.HSBtoRGB(h, sat, bright));
	}
}
