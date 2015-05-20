#version 120
/**
 * COMS W 4160 Computer Graphics
 * Programming Assignment 3
 *
 * Bonus #3: Brick Procedural Texture in a
 * 			 Blinn-Phong Fragment Shader
 * 
 * Receives the lighting intensities output 
 * and texture coordinates attribute from the
 * vertex shader and applies it to a brick-like
 * color in the current fragment.
 *
 * Ernesto Sandoval Castillo (es3187)
 */
varying vec3 v_i;
vec2 new_st;
vec3 brick_col;
float fcolor, new_s, new_t;
void main() {
	/* When considering the layout of a brick wall,
	 * there is clearly two aspects:
	 *   - the brick
	 *   - its thin surrounding layer
	 * The first thing to do is once more scale the
	 * texture so we have multiple brick entities.
	 * Here the scaling factor is abitrarily chosen
	 * to be 70, so our wall is 70x70 bricks.
	 */
	new_st = 70.0 * gl_TexCoord[0].st;

	/* Second, rectangularize the brick.
	 *
     * Make some "bricks" thinner than 
     * others so as to obtain the thin layer.
     * Accordingly, the color of thin "bricks"
     * and the actual thick bricks should 
     * differ to highlight the brick-wall layout.
     *
     * Here every other brick is thinned (done 
     * using mod 2) (also note that any of
     * s or t could decrease) to have a desired 
     * brick-layer-brick layout.
     * 
     * Here s is shortened by half.
     */
	new_st.y = new_st.y - 0.5 * mod(floor(new_st.x), 2.0);

	/* Like with the dots texture, we shift
     * texel origin to 0.
     * All being specific to a brick locally.
     */
    new_st = abs(fract(new_st) - 0.5);

    /* Last, use smoothstep like with the dots 
     * texture to smoothly binarize the coloring
     * of brick and layer. This color-picking
     * should discriminate based on the greater 
     * texel side. Also note that this colors only
     * values within 0.4 of the readjusted texel
     * origin. Beyond 0.5 is dark. In between
     * smoothing is done.
     */
	fcolor = smoothstep(0.5, 0.4, max(new_st.x, new_st.y));

	/* Choose a nice reddish color for the actual bricks. */
 	brick_col = vec3(207.0/255.0,54.0/255.0,54.0/255.0);

	gl_FragColor = vec4(fcolor, fcolor, 1.0, 1.0) * 
				   vec4(v_i*brick_col,1.0);
}
