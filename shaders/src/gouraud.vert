#version 120
/**
 * COMS W 4160 Computer Graphics
 * Programming Assignment 3
 *
 * Required Gouraud Vertex Shader
 * 
 * Implements the Phong reflection model
 * formula for illumination to compute the
 * lighting color for each model vertex.
 *
 * Ernesto Sandoval Castillo (es3187)
 */
varying vec3 v_i, color;
float dif_dot, spc_dot;
vec3 lit_amb, v_pos, v_norm, view_dir, lit_dir, ref_dir;
vec4 dif, spec; 
void main() {

	color = vec3(gl_Color); /* Set current color. */
	v_i = vec3(0.0);
	lit_amb = vec3(0.0);

	/* Compute vertex position and normal in camera space. */
	v_pos = vec3(gl_ModelViewMatrix * gl_Vertex);
	v_norm = normalize(gl_NormalMatrix * gl_Normal);

	for (int i = 0; i < 4; i++) {
		/* Compute incoming light direction. */
		lit_dir = normalize(gl_LightSource[i].position.xyz - v_pos);
		dif_dot = dot(lit_dir, v_norm);

		/* Only include specular and diffuse intensities
		 * if the angle between light source and normal
		 * is positive. 
		 */
		if (dif_dot > 0.0) {
			dif = gl_FrontMaterial.diffuse * dif_dot *
				  gl_LightSource[i].diffuse;

			/* Compute reflected light and viewing directions. */
			ref_dir = -reflect(lit_dir, v_norm);
			view_dir = normalize(-v_pos);
			spc_dot = dot(ref_dir, view_dir);

			/* Only include specular intensities if the
			 * angle between reflected light and viewer
			 * is positive.
			 */
			if (spc_dot > 0.0) {
				spec = gl_FrontMaterial.specular *
					   pow(spc_dot, gl_FrontMaterial.shininess) *
					   gl_LightSource[0].specular;
				v_i += vec3(dif) + vec3(spec);
			} else {
				v_i += vec3(dif);
			}
		}

		/* Sum up the ambiences of all the light sources. */
		lit_amb += vec3(gl_LightSource[i].ambient);
	}

	/* Compute final intensity sum and apply transformation. */
	v_i += (lit_amb/4.0) * vec3(gl_FrontMaterial.ambient);
	gl_Position = ftransform();
}