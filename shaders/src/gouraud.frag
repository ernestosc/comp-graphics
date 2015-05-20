#version 120
/**
 * COMS W 4160 Computer Graphics
 * Programming Assignment 3
 *
 * Required Gouraud Fragment Shader
 * 
 * Receives the intensities output from the
 * vertex shader and applies it to the color
 * of the current fragment.
 *
 * Ernesto Sandoval Castillo (es3187)
 */
varying vec3 v_i, color; /* Color and intensity vertex info. */
void main() {
	/* Interpolate fragment corner colors. */
	gl_FragColor = vec4(v_i * color ,1.0);
}