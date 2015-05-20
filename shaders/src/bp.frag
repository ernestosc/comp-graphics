#version 120
/**
 * COMS W 4160 Computer Graphics
 * Programming Assignment 3
 *
 * Choice #1: Blinn-Phong Fragment Shader
 * 
 * Receives the intensities output from the
 * vertex shader and applies it to the color
 * of the current fragment.
 *
 * Ernesto Sandoval Castillo (es3187)
 */

/* The Blinn-Phong fragment shader is identical to Gouraud. */
varying vec3 v_i, color; /* Color and intensity vertex info. */
void main() {
	/* Interpolate fragment corner colors. */
	gl_FragColor = vec4(v_i * color, 1.0);
}