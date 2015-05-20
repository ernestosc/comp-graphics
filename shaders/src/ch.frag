#version 120
/**
 * COMS W 4160 Computer Graphics
 * Programming Assignment 3
 *
 * Choice #2: Checkerboard Procedural Texture
 * 			  in a Blinn-Phong Fragment Shader
 * 
 * Receives the lighting intensities output 
 * and texture coordinates attribute from the
 * vertex shader and applies it to the color
 * of the current fragment.
 *
 * Ernesto Sandoval Castillo (es3187)
 */
varying vec3 v_i;
float fcolor, scaled_s, scaled_t;
void main() {
	/* Map out the checkerboard pattern. */

	/* First scale the coordinates to repeat
	 * the image n amount of times. Here, 100
	 * is the scaling factor. Note that for
	 * PA3.java, the texture image is empty,
	 * so a 100x100 grid is made to overlay
	 * on the object. As such, one must also
	 * discretize the scaled values to maintain
	 * a square pattern.
	 */
	scaled_s = floor(100.0 * gl_TexCoord[0].s);
	scaled_t = floor(100.0 * gl_TexCoord[0].t);

	/* Maintain a binary coloring by modulating
	 * the sum of the scaled coord.s over mod 2.
	 * Use the sum of the coord.s to account for
	 * both dimensions of the position when 
	 * choosing the binarized intensity.
	 */
	fcolor = mod(scaled_s + scaled_t, 2.0);

	/* Interpolate the checker pattern with vertex light
	 * intensities and the user-specified glColor().
	 */
	gl_FragColor = vec4(fcolor, fcolor, fcolor, 1.0) * vec4(v_i,1.0);
}