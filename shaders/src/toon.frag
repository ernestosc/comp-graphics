#version 120
/**
 * COMS W 4160 Computer Graphics
 * Programming Assignment 3
 *
 * Bonus #1: Toon Shader
 * 
 * Receives the intensities output from the
 * vertex shader and applies a color 
 * depending on that indicator.
 *
 * Ernesto Sandoval Castillo (es3187)
 */
varying float avg;
vec3 color;
void main() {
	/* Depending on the value of the weighted average,
	   determine the right color for a fragment. */
	if (avg > 0.85) {
		color = vec3(1,1,1);
	} else if (avg > 0.8) {
		color = vec3(168.0/223.0,200.0/255.0,1.0);
	} else if (avg > 0.68) {
		color = vec3(110.0/255.0,205.0/255.0,1.0);
	} else if (avg > 0.55) {
		color = vec3(0.0,97.0/255.0,140.0/255.0);
	} else {
		color = vec3(0.0,65.0/255.0,94.0/255.0);
	}
	gl_FragColor = vec4(color, 1.0);
}