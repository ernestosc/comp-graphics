#version 120
/**
 * COMS W 4160 Computer Graphics
 * Programming Assignment 3
 *
 * Bonus #1: Toon Vertex Shader
 * 
 * Computes vertex intensity indicators and
 * passes these to the fragment shader.
 *
 * Ernesto Sandoval Castillo (es3187)
 */

varying float avg;
float coss[4];
float dif_dot, spc_dot, maxc, nxtc, prec, lowc;
vec3 v_pos, v_norm, view, in_light;
void main() {
	/* Initialize cosine-tracker. */
	coss[0] = 0.0;
	coss[1] = 0.0;
	avg = 0.0;

	/* Compute vertex position and normal in camera space. */
	v_pos = vec3(gl_ModelViewMatrix * gl_Vertex);
	v_norm = normalize(gl_NormalMatrix * gl_Normal);

	for (int i = 0; i < 4; i++) {
		/* Compute incoming light direction. */
		in_light = normalize(gl_LightSource[i].position.xyz - v_pos);
		/* Compute the cosine between light and vertex normal. */
		coss[i] = dot(in_light, v_norm);
	}

	/* Average the value over all light sources to find
	   best vertex intensity indicator. Use weights. */
	maxc = coss[0];
	/* Find most direct angled light. */
	for (int j = 1; j < 4; j++) {
		maxc = max(maxc, coss[j]);
	}
	avg += maxc * 0.9;

	/* Find next most direct. */
	for (int k = 0; k < 4; k++) {
		if (coss[k] != maxc) {
			nxtc = coss[k]; 
			break;
		}
	}

	for (int m = 0; m < 4; m++) {
		if (coss[m] != maxc) {
			nxtc = max(nxtc, coss[m]);
		}
	}
	avg += nxtc * 0.05;

	/* Find penultimate most direct. */
	for (int n = 0; n < 4; n++) {
		if (coss[n] != maxc && coss[n] != nxtc) {
			prec = coss[n];
			break;
		}
	}

	for (int p = 0; p < 4; p++) {
		if (coss[p] != maxc && coss[p] != nxtc) {
			prec = max(prec, coss[p]);
		}
	}
	avg += prec * 0.03;

	/* Get least direct light. */
	for (int q = 0; q < 4; q++) {
		if (coss[q] != maxc && coss[q] != nxtc && coss[q] != prec) {
			lowc = coss[q];
		}
	}
	avg += lowc * 0.02;

	gl_Position = ftransform();
}