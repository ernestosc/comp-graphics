#version 120
/**
 * COMS W 4160 Computer Graphics
 * Programming Assignment 3
 *
 * Bonus #2: Dots Procedural Texture in
 * 			 a Blinn-Phong Fragment Shader
 * 
 * Receives the lighting intensities output 
 * and texture coordinates attribute from the
 * vertex shader and applies it to the color
 * for the current fragment.
 *
 * Ernesto Sandoval Castillo (es3187)
 */
varying vec3 v_i;
vec2 scaled_st, frac_st;
float fcolor;
void main() {

	/* As in the checkerboard, one must scale the texture.
	 * Here the sclaing factor is 42. 
	 */ 
	scaled_st = 42.0 * gl_TexCoord[0].st;

	/* Only need the fractional part of the scaled components.
	   Adjust the coordinates to put texel origin at 0. 
	   Draws quarter-circles otherwise, since the half-unit
	   texture coordinate sense halves the square region
	   spread. */
	frac_st = fract(scaled_st) - 0.5;

	/* Smooth out, rather than binarize, the patterning of
	   the texture. Give a circular sense by taking the 
	   length of the coordinate vector. This equates to a
	   radius. Smoothstep will whiten all values within
	   0.3 radius and smooths/blackens all else. */
	fcolor = smoothstep(0.4, 0.3,length(frac_st));
	
	gl_FragColor = vec4(fcolor, fcolor, fcolor, 1.0) * 
				   vec4(v_i,1.0);
}