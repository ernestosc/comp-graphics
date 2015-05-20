#version 120
/**
 * COMS W 4160 Computer Graphics
 * Programming Assignment 3
 *
 * Choice #1: Blinn-Phong Vertex Shader
 * 
 * Implements the Blinn-Phong reflection model
 * formula for illumination to compute the
 * lighting intensities for each model vertex.
 *
 * Ernesto Sandoval Castillo (es3187)
 */

/* Note the close resemblance with Gouraud shading.
 * The only difference is the use of halfway vector
 * H = (L + V)/2 in the angle-calculating dot prods.
 */
varying vec3 v_i, color;
float dif_dot, spc_dot;
vec3 amb_lit, v_pos, v_norm, view, in_light, halfv;
vec4 dif, spec; 
void main() {

	color = vec3(gl_Color); /* Set current color. */
	v_i = vec3(0.0);
	amb_lit = vec3(0.0);

	/* Compute vertex position and normal in camera space. */
	v_pos = vec3(gl_ModelViewMatrix * gl_Vertex);
	v_norm = normalize(gl_NormalMatrix * gl_Normal);

	for (int i = 0; i < 4; i++) {
		/* Compute incoming light direction. */
		in_light = normalize(gl_LightSource[i].position.xyz - v_pos);
		dif_dot = dot(in_light, v_norm);

		/* Only include specular and diffuse intensities
		 * if the angle between light source and normal
		 * is positive. 
		 */
		if (dif_dot > 0.0) {
			dif = gl_FrontMaterial.diffuse * dif_dot * 
				  gl_LightSource[i].diffuse;

			/* Compute halfway vector and viewing directions. */ 
			view = normalize(-v_pos);
			halfv = normalize(in_light + view);
			spc_dot = dot(halfv, v_norm);

			/* Only include specular intensities if the
			 * angle between halfway vector and normal
			 * is positive.
			 */
			if (spc_dot > 0.0) {
				spec = gl_FrontMaterial.specular *
					   pow(spc_dot, 4.0*gl_FrontMaterial.shininess) *
					   gl_LightSource[0].specular;
				v_i += vec3(dif) + vec3(spec);
			} else {
				v_i += vec3(dif);
			}
		}

		/* Sum up the ambiences of all the light sources. */
		amb_lit += vec3(gl_LightSource[i].ambient);
	}

	/* Compute final intensity sum and apply transformation. */
	v_i += (amb_lit/2.0) * vec3(gl_FrontMaterial.ambient);
	gl_Position = ftransform();
}