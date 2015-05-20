/**
 * COMS W 4160 Computer Graphics
 * Programming Assignment 2
 * Part A
 * 
 * Animatics Joint Class
 *
 * @author Ernesto Sandoval Castillo (es3187)
 * @version 1.0
 */

import java.util.ArrayList;
import org.lwjgl.util.vector.Vector3f;

public class Joint {
	//public Vector3f pos;	/* Joint's local origin. */
	public float polar;		/* Polar angle.   */
	public float azimuth;	/* Azimuth angle. */
	public String type; 	/* Type of joint. */
	public Joint parent;	/* Parent joint. */
	public int name;		/* GL Name ID */
	public ArrayList<Joint> children;

	public Joint(float azm, float pol, String jnt, int nm) {
		//this.pos = new Vector3f(x,y,z);
		this.azimuth = azm;
		this.polar = pol;
		this.type = jnt;
		this.name = nm;
		this.parent = null;
		this.children = new ArrayList<Joint>();
	}

	public void setParent(Joint par) {
		this.parent = par;
	}

	public void addChild(Joint chd) {
		this.children.add(chd);
	}
}