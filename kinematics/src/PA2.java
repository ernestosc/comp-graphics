/**
 * COMS W 4160 Computer Graphics
 * Programming Assignment 2
 * Part A
 * 
 * Animatics
 *
 * Animates a 3-D serial-chain manipulator and
 * a custom 3D character model by implementing
 * forward kinematics methods.
 *
 * @author Ernesto Sandoval Castillo (es3187)
 * @version 1.0
 */

import java.nio.*;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.Iterator;

/* Import LWJGL classes. */
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.glu.*;
import org.lwjgl.util.vector.*;

/* Import slick library classes for text and textures. */
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

/* Import EJML library classes for inverse kinematics. */
import org.ejml.factory.SingularMatrixException;
import org.ejml.simple.SimpleMatrix;

/* Main Animatics Project Class */
public class PA2 {

	/* Window Parameters */
	private String windowTitle = "Animatics";
	private boolean closeRequested = false;

	private SimpleMatrix target = new SimpleMatrix(2,1);
	private boolean reaching = false;
	private int mtries = 0;
	private int wtries = 0;

	/* Texture Holder */
	private Texture text;

	/* Lighting Buffers */
	private float[] amb = {0.4f,0.4f,0.4f,1.0f}; /* Ambient Light */
	private float[] pos = {0.0f,0.0f,1.0f,1.0f}; /* Pos. of Light */
	private float[] dif = {0.8f,0.8f,0.8f,1.0f}; /* Diffuse Material */
	private float shn = 25.0f;				/* Shininess of Material */

	/* Program Control Flow Parameters */
	private long lastFrameTime;  /* used to compute delta */
	private byte FWDKIN = 0; /* use forward kinematics */
	private byte INVKIN = 1; /* use inverse kinematics */
	private byte CURRENT_KTYPE = FWDKIN; /* defaults to forward kinemtics  */
	/* Forward Kinematics Models */
	private byte LINK03 = 0; /* draw 3-link manipulator */
	private byte LINK10 = 1; /* draw 10-link manipulator */
	private byte CUSTOM = 2; /* draw custom model state */
	private byte CURRENT_FK_MODEL = LINK03; /* defaults to 3-link manipulator */
	private int CURRENT_TRE_JOINT = 0; /* defaults to base joint */
	private int CURRENT_TEN_JOINT = 0; /* defaults to base joint */
	private int CURRENT_CUS_JOINT = 0; /* defaults to base joint */
	/* Inverse Kinematics Models */
	private byte LINK07 = 0; /* draw 7-revolute manipulator */
	private byte LEHAND = 1; /* draw a revolute-joint hand  */
	private byte CURRENT_IK_MODEL = LINK07; /* defaults to 7-link manipulator */
	private int CURRENT_SEV_JOINT = 0;
	private int CURRENT_WEB_JOINT = 0;
	private int CURRENT_JOINT = CURRENT_TRE_JOINT;

	/* Display List IDs */
	private int env; /* texturized environment  */
	private int sml; /* serial manipulator link */
	private int sph; /* manipulator joint ball  */
	private int axs; /* useful pos octant axes  */

	/* Sets of Joints for Models */
	private ArrayList<Joint> tre = new ArrayList<Joint>();
	private ArrayList<Joint> ten = new ArrayList<Joint>();
	private ArrayList<Joint> cus = new ArrayList<Joint>();
	private ArrayList<Joint> sev = new ArrayList<Joint>();
	private ArrayList<Joint> web = new ArrayList<Joint>();

	/* Main Project Loop */
	public void run() {
		/* Initialize window, delta timer, and image. */
		createWindow();
		getDelta();
		init();
		initGL();
		/* Repeatedly read and process input. */
		while (!closeRequested) {
			if (CURRENT_KTYPE == FWDKIN) {
				if (CURRENT_FK_MODEL == LINK03)
					pollInput(tre);
				else if (CURRENT_FK_MODEL == LINK10)
					pollInput(ten);
				else if (CURRENT_FK_MODEL == CUSTOM)
					pollInput(cus);
			} else if (CURRENT_KTYPE == INVKIN) {
				if (CURRENT_IK_MODEL == LINK07)
					pollInput(sev);
				else if (CURRENT_IK_MODEL == LEHAND)
					pollInput(web);
			}
			renderGL();
			Display.update();
		}

		Display.destroy();
	}

	private void createWindow() {
		try  {
			Display.setDisplayMode(new DisplayMode(960, 540));
			Display.setVSyncEnabled(true);
			Display.setTitle(windowTitle);
			Display.create();
		} catch (LWJGLException e) {
			Sys.alert("Error", "Initialization failed!\n\n" + e.getMessage());
			System.exit(0);
		}
	}

	/**
	 * Calculate how many ms have passed
	 * since the last frame.
	 *
	 * @return # of ms passed since last frame.
	 */
	public int getDelta() {
		long time = (Sys.getTime() * 1000) / Sys.getTimerResolution();
		int delta = (int) (time - lastFrameTime);
		lastFrameTime = time;
		return delta;
	}

	/* Initialize Lighting Features */
	private void initLights() {
		glEnable(GL_LIGHTING);
		glEnable(GL_LIGHT0);
		glEnable(GL_COLOR_MATERIAL);
		glColorMaterial(GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE);
		glLight(GL_LIGHT0, GL_AMBIENT, toFloatBuffer(amb));
		glLight(GL_LIGHT0, GL_POSITION, toFloatBuffer(pos));
		glMaterial(GL_FRONT, GL_DIFFUSE, toFloatBuffer(dif));
		glMaterialf(GL_FRONT, GL_SHININESS, shn);
	}

	/* Format float arrays into OpenGL-friendly float buffers. */
	private FloatBuffer toFloatBuffer(float[] fs) {
		FloatBuffer fb = BufferUtils.createFloatBuffer(fs.length);
		fb.put(fs);
		fb.flip();
		return fb;
	}

	/* Initialize non-GL parameters. */
	private void init() {
		Camera.create();
		try {
			text = TextureLoader.getTexture("JPG", 
				   ResourceLoader.getResourceAsStream("env.jpg"));
		} catch (IOException e) { e.getMessage(); }

		/* Initialize joints and display lists. */
		makeFKManJoints(tre,3);
		makeIKManJoints(sev,7);
		makeFKManJoints(ten,10);
		makeFKManJoints(cus,31);
		makeIKManJoints(web,31);
	}

	private void initGL() {
		int width  = Display.getDisplayMode().getWidth();
		int height = Display.getDisplayMode().getHeight();

		glViewport(0, 0, width, height); /* Reset the viewport.       */
		glMatrixMode(GL_PROJECTION);	 /* Select projection matrix. */
		glLoadIdentity(); 				 /* Reset projection matrix.  */
		GLU.gluPerspective(60.0f,((float)width/(float)height),0.1f,500.0f);
		glMatrixMode(GL_MODELVIEW); 	 /* Select modelview matrix.  */
		glLoadIdentity();			     /* Reset modelview matrix.   */

		glShadeModel(GL_SMOOTH);	/* Enables smooth shading. */
		glClearColor(0,0,0,0);		/* Black background.       */
		glClearDepth(1);			/* Depth buffer setup.     */
		glEnable(GL_DEPTH_TEST); 	/* Enables depth testing.  */
		glDepthFunc(GL_LEQUAL); 	/* Chooses depth test.     */
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
		initLights();

		env = makeEnvironment();
		axs = makeAxes();
		sml = makeLink();
		sph = makeBall();
	}

	public void pollInput(ArrayList<Joint> joints) {
		boolean picked = acceptPick();
		if (CURRENT_KTYPE == FWDKIN) {
			acceptJointMove(joints);
		} else {
			if (!picked) {
				if (Mouse.isInsideWindow() && Mouse.isButtonDown(0)) {
					acceptTargets(Mouse.getX(), Mouse.getY()); 
				}
			}
		}
		Camera.acceptInput(getDelta());
		/* Scroll through key events. */
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
					closeRequested = true;
				} else if (Keyboard.getEventKey() == Keyboard.KEY_P) {
					snapshot();
				} else if (Keyboard.getEventKey() == Keyboard.KEY_TAB && CURRENT_KTYPE == INVKIN) {
					CURRENT_KTYPE = FWDKIN;
					Camera.mode = FWDKIN;
				} else if (Keyboard.getEventKey() == Keyboard.KEY_TAB && CURRENT_KTYPE == FWDKIN) {
					CURRENT_KTYPE = INVKIN;
					Camera.mode = INVKIN;
				} else if (Keyboard.getEventKey() == Keyboard.KEY_1) {
					if (CURRENT_KTYPE == FWDKIN && CURRENT_FK_MODEL != LINK03)
						CURRENT_FK_MODEL = LINK03;
					else if (CURRENT_KTYPE == INVKIN && CURRENT_IK_MODEL != LINK07)
						CURRENT_IK_MODEL = LINK07;
				} else if (Keyboard.getEventKey() == Keyboard.KEY_2) {
					if (CURRENT_KTYPE == FWDKIN && CURRENT_FK_MODEL != LINK10)
						CURRENT_FK_MODEL = LINK10;
					else if (CURRENT_KTYPE == INVKIN && CURRENT_IK_MODEL != LEHAND)
						CURRENT_IK_MODEL = LEHAND;
				} else if (Keyboard.getEventKey() == Keyboard.KEY_3) {
					if (CURRENT_KTYPE == FWDKIN && CURRENT_FK_MODEL != CUSTOM)
						CURRENT_FK_MODEL = CUSTOM;
				}
			}
		}
		if (Display.isCloseRequested()) {
			closeRequested = true;
		}
	}

	private boolean acceptPick() {
		if (Mouse.isInsideWindow() && Mouse.isButtonDown(0)) {
			int mouseX = Mouse.getX();
			int mouseY = Mouse.getY();
			return pickJoint(mouseX, mouseY);
		}
		return false;
	}

	/* Select an object rendered at the given window coordinates. */
	private boolean pickJoint(int x, int y) {
		boolean picked = false;
		IntBuffer selects = BufferUtils.createIntBuffer(64);
		IntBuffer view = BufferUtils.createIntBuffer(16);
		int hits;
		glSelectBuffer(selects);
		glGetInteger(GL_VIEWPORT, view);
		glRenderMode(GL_SELECT);
		glInitNames();
		glPushName(0);
		glPushMatrix();
			glMatrixMode(GL_PROJECTION);
			glLoadIdentity();
			GLU.gluPickMatrix(x, y, 1.0f, 1.0f, view);
			GLU.gluPerspective(60.0f,((float)view.get(2)/(float)view.get(3)),0.1f,500.0f);
			glMatrixMode(GL_MODELVIEW);
			if (CURRENT_KTYPE == FWDKIN) {
				if (CURRENT_FK_MODEL == LINK03) {
					renderManipulator(tre.get(0));
				} else if (CURRENT_FK_MODEL == LINK10) {
					renderManipulator(ten.get(0));
				} else if (CURRENT_FK_MODEL == CUSTOM) {
					renderManipulator(cus.get(0));
				}
			} else if (CURRENT_KTYPE == INVKIN) {
				if (CURRENT_IK_MODEL == LINK07) {
					renderManipulator(sev.get(0));
				} else if (CURRENT_IK_MODEL == LEHAND) {
					renderManipulator(web.get(0));
				}
			}
			initGL();
		glPopMatrix();
		hits = glRenderMode(GL_RENDER);
		for (int i = 0; i < hits; i++) {
			int hit = selects.get(4*i + 3);
			if (CURRENT_KTYPE == FWDKIN) {
				if (CURRENT_FK_MODEL == LINK03) {
					for (int k = 0; k < tre.size(); k++) {
						if (tre.get(k).name == hit) {
							CURRENT_TRE_JOINT = k;
							picked = true;
						}
					}
				} else if (CURRENT_FK_MODEL == LINK10) {
					for (int k = 0; k < ten.size(); k++) {
						if (ten.get(k).name == hit) {
							CURRENT_TEN_JOINT = k;
							picked = true;
						}
					}
				} else if (CURRENT_FK_MODEL == CUSTOM) {
					for (int k = 0; k < cus.size(); k++) {
						if (cus.get(k).name == hit) {
							CURRENT_CUS_JOINT = k;
							picked = true;
						}
					}
				}
			} else if (CURRENT_KTYPE == INVKIN) {
				if (CURRENT_IK_MODEL == LINK07) {
					for (int k = 0; k < sev.size(); k++) {
						if (sev.get(k).name == hit) {
							CURRENT_SEV_JOINT = k;
							picked = true;
						}
					}
				}
				if (CURRENT_IK_MODEL == LEHAND) {
					for (int k = 0; k < web.size(); k++) {
						if (web.get(k).name == hit) {
							CURRENT_WEB_JOINT = k;
							picked = true;
						}
					}
				}
			}
		}
		return picked;
	}

	/* FK - Accepts user control of joint angle currently selected. */
	private void acceptJointMove(ArrayList<Joint> joints) {
		boolean keyPlus = Keyboard.isKeyDown(Keyboard.KEY_EQUALS);
		boolean keyLess = Keyboard.isKeyDown(Keyboard.KEY_MINUS);
		boolean keyClck = Keyboard.isKeyDown(Keyboard.KEY_0);
		boolean keyCclk = Keyboard.isKeyDown(Keyboard.KEY_9);
		if (CURRENT_FK_MODEL == LINK03)
			CURRENT_JOINT = CURRENT_TRE_JOINT;
		else if (CURRENT_FK_MODEL == LINK10)
			CURRENT_JOINT = CURRENT_TEN_JOINT;
		else if (CURRENT_FK_MODEL == CUSTOM)
			CURRENT_JOINT = CURRENT_CUS_JOINT;
		setAngles(joints.get(CURRENT_JOINT), keyPlus, keyLess, keyClck, keyCclk);
	}

	/* FK - Modifies the d.o.f. of the joints. */
	private void setAngles(Joint jnt, boolean plus, boolean less, boolean clck, boolean cclk) {
		float speed = 3.7f;
		float grown = 0.9f;
		if (plus) {
			if (jnt.type.equals("REVOLUTE") || jnt.type.equals("SPHERICAL")) {
				jnt.azimuth += speed;
			} else {
				jnt.azimuth *= jnt.azimuth > 0.5f ? grown : 1;
			}
		} else if (less) {
			if (jnt.type.equals("REVOLUTE") || jnt.type.equals("SPHERICAL")) {
				jnt.azimuth -= speed;
			} else {
				jnt.azimuth /= jnt.azimuth < 1.2f ? grown : 1;
			}
		} else if (clck) {
			if (jnt.type.equals("SPHERICAL"))
				jnt.polar += speed;
		} else if (cclk) {
			if (jnt.type.equals("SPHERICAL"))
				jnt.polar -= speed;
		}
	}

	/* IK - Determines world coordinates from screen coordinates. */
	private void acceptTargets(int x, int y) {
		Vector3f near = new Vector3f();
		Vector3f far  = new Vector3f();
		Vector3f dir  = new Vector3f();
		IntBuffer   view = BufferUtils.createIntBuffer(16);
		FloatBuffer proj = BufferUtils.createFloatBuffer(16);
		FloatBuffer modv = BufferUtils.createFloatBuffer(16);
		FloatBuffer npt  = BufferUtils.createFloatBuffer(4);
		FloatBuffer fpt  = BufferUtils.createFloatBuffer(4);

		glGetInteger(GL_VIEWPORT, view);
		glGetFloat(GL_PROJECTION_MATRIX, proj);
		glGetFloat(GL_MODELVIEW_MATRIX , modv);

		GLU.gluUnProject((float)x, (float)y, 0.0f, modv, proj, view, npt);
		near.set(npt.get(0), npt.get(1), npt.get(2));
		GLU.gluUnProject((float)x, (float)y, 1.0f, modv, proj, view, fpt);
		far.set (fpt.get(0), fpt.get(1), fpt.get(2));

		/* Paramterize a line form near and far plane points.
		   Solve for the the z-axis intercept. */
		Vector3f.sub(far,near,dir);
		float param_t = -near.z/dir.z;
		target.set(0,0,near.x+(dir.x*param_t));
		target.set(1,0,near.y+(dir.y*param_t));
		reaching = true;		
	}

	/* IK - Peform damped least squares. */
	private boolean reachTarget(ArrayList<Joint> joints, SimpleMatrix target) {
		if (joints == sev)
			mtries++;
		else if (joints == web)
			wtries++;
		int max;
		if (CURRENT_IK_MODEL == LINK07) {
			max = CURRENT_SEV_JOINT;
		} else {
			max = CURRENT_WEB_JOINT;
		}
		/* Account for unreachable target choices. */
		if (joints == sev) {
			double rad = (max+1)*3;
			if (target.normF() > rad) {
				target.set(0,0,rad*Math.cos(Math.atan2(target.get(1,0),target.get(0,0))));
				target.set(1,0,rad*Math.sin(Math.atan2(target.get(1,0),target.get(0,0))));
			}
		}
		Joint cur = joints.get(max);	
		ArrayList<Joint> chain = new ArrayList<Joint>();
		int size = 0;
		while (cur != null) {
			chain.add(0,cur);
			size += 1;
			cur = cur.parent;
		}
		/* Set up original angles vector */
		SimpleMatrix angles = new SimpleMatrix(size,1);
		cur = joints.get(max);
		size -= 1;
		while (cur != null) {
			angles.set(size--,0,cur.azimuth);
			cur = cur.parent;
		}
		/* Get current source end-effector position. */
		glPushMatrix();
			SimpleMatrix source = new SimpleMatrix(2,1);
			getSourcePos(chain);
			FloatBuffer scoords = BufferUtils.createFloatBuffer(16);
			glGetFloat(GL_MODELVIEW, scoords);
			source.set(0,0,scoords.get(12));
			source.set(1,0,scoords.get(13));
		glPopMatrix();
		/* Get current error vector e = t - s. */
		SimpleMatrix errors = target.minus(source);
		/* Compute the Jacobian and solve the DLS system. */
		SimpleMatrix jacobian = getJacobian(chain);
		double lambda_squared = 0.5;
		SimpleMatrix jacobian_t = jacobian.transpose();
		SimpleMatrix jacojaco_t = jacobian.mult(jacobian_t);
		SimpleMatrix lambdadiag = SimpleMatrix.identity(2).scale(lambda_squared);
		SimpleMatrix expression = jacojaco_t.plus(lambdadiag);
		SimpleMatrix deltas = (jacobian.transpose()).mult(expression.invert()).mult(errors);
		/* Update the angles. */
		SimpleMatrix new_angs = angles.plus(deltas);
		for (int i = 0; i < chain.size(); i++) {
			for (int j = 0; j < joints.size(); j++) {
				if (chain.get(i) == joints.get(j)) {
					joints.get(j).azimuth = (float) new_angs.get(i);
					break;
				}
			}
		}

		/* Get the new source position. */
		glPushMatrix();
			SimpleMatrix newsource = new SimpleMatrix(2,1);
			getSourcePos(chain);
			scoords = BufferUtils.createFloatBuffer(16);
			glGetFloat(GL_MODELVIEW, scoords);
			newsource.set(0,0,scoords.get(12));
			newsource.set(1,0,scoords.get(13));
		glPopMatrix();
		errors = target.minus(newsource);
		return joints == sev ? errors.normF() > 0.1 && mtries < 750 : 
							   errors.normF() > 0.1 && wtries < 750;
	}

	/* IK - Simulate the FK transform matrix to arrive at source position. */
	private void getSourcePos(ArrayList<Joint> joints) {
		glLoadIdentity();
		for (int i = 0; i < joints.size(); i++) {
			glRotatef(joints.get(i).azimuth, 0, 0, 1);
			glTranslatef(3,0,0);
		}
	}

	/* Compute a Jacobian on the set of angles of hierarchy 'joints'. */
	private SimpleMatrix getJacobian(ArrayList<Joint> joints) {
		SimpleMatrix jacobian = new SimpleMatrix(2,joints.size());
		for (int j = 0; j < joints.size(); j++) {
			double angle = 0;
			for (int k = 0; k < joints.size(); k++) {
				angle += joints.get(k).azimuth;
				if (k >= j) {
					jacobian.set(0,j, jacobian.get(0,j) - 3*Math.sin(Math.toRadians(angle)));
					jacobian.set(1,j, jacobian.get(1,j) + 3*Math.cos(Math.toRadians(angle)));
				}
			}
		}
		return jacobian;
	}

	/* Main Rendering Loop */
	private void renderGL() {
		/* Clear the screen and depth buffers. */
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glLoadIdentity();
		Camera.apply();
		renderAxes(axs);
		if (CURRENT_KTYPE == FWDKIN) {
			if (CURRENT_FK_MODEL == LINK03) {
				renderManipulator(tre.get(0));
			} else if (CURRENT_FK_MODEL == LINK10) {
				renderManipulator(ten.get(0));
			} else if (CURRENT_FK_MODEL == CUSTOM) {
				renderManipulator(cus.get(0));
			}
		} else if (CURRENT_KTYPE == INVKIN) {
			if (CURRENT_IK_MODEL == LINK07) {
				if (reaching) {
					reaching = reachTarget(sev, target);
					renderManipulator(sev.get(0));
					if (!reaching) {
						mtries = 0;
					}
				} else {
					renderManipulator(sev.get(0));
				}
			} else if (CURRENT_IK_MODEL == LEHAND) {
				if (reaching) {
					reaching = reachTarget(web, target);
					renderManipulator(web.get(0));
					if (!reaching) {
						wtries = 0;
					}
				} else {
					renderManipulator(web.get(0));
				}
			}
		}
		renderEnvironment(env, text);
		glFlush();
	}

	/**
	 * Renders the positive Cartesian octant.
	 * @param axsID	 the axes display list 
	 */
	private void renderAxes(int axsID) {
		glPushMatrix();
			glCallList(axsID);
		glPopMatrix();
	}

	/**
	 * Renders the texturized environment.
	 * @param envID  the environment display list 
	 * @param txt    a texture-holder ID 
	 */
	private void renderEnvironment(int envID, Texture txt) {
		glPushMatrix();
			glEnable(GL_TEXTURE_2D);
			txt.bind();
			glCallList(envID);
			glDisable(GL_TEXTURE_2D);
		glPopMatrix();
	}

	private void renderManipulator(Joint jnt) {
		glLoadName(jnt.name);
		glPushMatrix();
			if (jnt.type.equals("REVOLUTE")) {
				glRotatef(jnt.azimuth, 0, 0, 1);
				glPushMatrix();
				if (CURRENT_KTYPE == FWDKIN) {
					if (CURRENT_FK_MODEL == LINK03) {
						if (jnt == tre.get(CURRENT_TRE_JOINT)) {
							glColor3f(1f,225f/255f,0f);
						} else {
							glColor3f(252f/255f,151f/255f,83f/255f);
						}
					} else if (CURRENT_FK_MODEL == LINK10) {
						if (jnt == ten.get(CURRENT_TEN_JOINT)) {
							glColor3f(1f,225f/255f,0f);
						} else {
							glColor3f(252f/255f,151f/255f,83f/255f);
						}
					} else if (CURRENT_FK_MODEL == CUSTOM) {
						if (jnt == cus.get(CURRENT_CUS_JOINT)) {
							glColor3f(1f,225f/255f,0f);
						} else {
							glColor3f(252f/255f,151f/255f,83f/255f);
						}
					}
				} else if (CURRENT_KTYPE == INVKIN) { 
					if (CURRENT_IK_MODEL == LINK07) {
						if (jnt == sev.get(CURRENT_SEV_JOINT)) {
							glColor3f(1f,225f/255f,0f);
						} else {
							glColor3f(252f/255f,151f/255f,83f/255f);
						}
					} else if (CURRENT_IK_MODEL == LEHAND) {
						if (jnt == web.get(CURRENT_WEB_JOINT)) {
							glColor3f(1f,225f/255f,0f);
						} else {
							glColor3f(252f/255f,151f/255f,83f/255f);
						}
					}
				}
				glCallList(sph);
				glPushMatrix();
					glRotatef(90,0,1,0);
					glCallList(sml);
				glPopMatrix();
				glPopMatrix();
				glTranslatef(3,0,0);
			} else if (jnt.type.equals("PRISMATIC")) {
				glPushMatrix();
				if (CURRENT_FK_MODEL == LINK03) {
					if (jnt == tre.get(CURRENT_TRE_JOINT)) {
						glColor3f(1f,225f/255f,0f);
					} else { 
						glColor3f(252f/255f,151f/255f,83f/255f);
					}
				} else if (CURRENT_FK_MODEL == LINK10) {
					if (jnt == ten.get(CURRENT_TEN_JOINT)) {
						glColor3f(1f,225f/255f,0f);
					} else { 
						glColor3f(252f/255f,151f/255f,83f/255f);
					}
				} else if (CURRENT_FK_MODEL == CUSTOM) {
					if (jnt == cus.get(CURRENT_CUS_JOINT)) {
						glColor3f(1f,225f/255f,0f);
					} else {
						glColor3f(252f/255f,151f/255f,83f/255f);
					}
				}
				glCallList(sph);
				glPushMatrix();
					glScalef(1f,1f,1f/jnt.azimuth);
					glCallList(sml);
				glPopMatrix();
				glPopMatrix();
				glTranslatef(0,0,3f/jnt.azimuth);
			} else if (jnt.type.equals("SPHERICAL")) {
				glRotatef(jnt.azimuth, 0, 0, 1);
				glRotatef(jnt.polar, 0, 1, 0);
				glPushMatrix();
				if (CURRENT_FK_MODEL == LINK03) {
					if (jnt == tre.get(CURRENT_TRE_JOINT)) {
						glColor3f(1f,225f/255f,0f);
					} else { 
						glColor3f(252f/255f,151f/255f,83f/255f);
					}
				} else if (CURRENT_FK_MODEL == LINK10) {
					if (jnt == ten.get(CURRENT_TEN_JOINT)) {
						glColor3f(1f,225f/255f,0f);
					} else { 
						glColor3f(252f/255f,151f/255f,83f/255f);
					}
				} else if (CURRENT_FK_MODEL == CUSTOM) {
					if (jnt == cus.get(CURRENT_CUS_JOINT)) {
						glColor3f(1f,225f/255f,0f);
					} else {
						glColor3f(252f/255f,151f/255f,83f/255f);
					}
				}
				glCallList(sph);
				glPushMatrix();
					glRotatef(90,0,1,0);
					glCallList(sml);
				glPopMatrix();
				glPopMatrix();
				glTranslatef(3,0,0);
			}
			for (Joint temp : jnt.children) {
				renderManipulator(temp);
			}
		glPopMatrix();	
	}

	/* Cartesian axes display list. */
	private int makeAxes() {
		int axes = glGenLists(1);
		glNewList(axes, GL_COMPILE);
			glBegin(GL_LINES);
				/* x-axis */
				glColor3f(1.0f,1.0f,0.0f);
				glVertex3f(0.0f,0.0f,0.0f);
				glVertex3f(50.0f,0.0f,0.0f); 
				/* y-axis */
				glColor3f(1.0f,0.0f,0.0f);
				glVertex3f(0.0f,0.0f,0.0f);
				glVertex3f(0.0f,50.0f,0.0f);
				/* z-axis */
				glColor3f(0.0f,0.0f,1.0f);
				glVertex3f(0.0f,0.0f,0.0f);
				glVertex3f(0.0f,0.0f,50.0f);
			glEnd();
		glEndList();
		return axes;
	}

	/* Environment display list. */
	private int makeEnvironment() {
		int environ = glGenLists(1);
		glNewList(environ, GL_COMPILE);
			Sphere world = new Sphere();
			world.setTextureFlag(true);
			world.setDrawStyle(GLU.GLU_FILL);
			world.setNormals(GLU.GLU_SMOOTH);
			glColor4f(1,1,1,1);
			world.draw(100,500,500);
		glEndList();
		return environ;
	}

	/* Spherical ball at each joint. */
	private int makeBall() {
		int ballID = glGenLists(1);
		glNewList(ballID, GL_COMPILE);
			Sphere jnt = new Sphere();
			jnt.setDrawStyle(GLU.GLU_FILL);
			jnt.setNormals(GLU.GLU_SMOOTH);
			jnt.draw(0.37f,100,100);
		glEndList();
		return ballID;
	}

	/* Cylindrical link display list. */
	private int makeLink() {
		int linkID  = glGenLists(1);
		glNewList(linkID, GL_COMPILE);
			Cylinder link = new Cylinder();
			link.setDrawStyle(GLU.GLU_FILL);
			link.setNormals(GLU.GLU_SMOOTH);
			glColor3f(83f/355f,184f/255f,252f/255f);
			link.draw(0.2f,0.2f,3,300,300);
		glEndList();
		return linkID;
	}

	/** 
	 * Initialize a set of joints for a forward
	 * kinematics model of size 'size'.
	 * @param size  the size of the join set
	 */
	private void makeFKManJoints(ArrayList<Joint> joints, int size) {
		for (int i = 0; i < size; i++) {
			if (i % 3 == 0)
				joints.add(new Joint(0f,0f,"SPHERICAL",i));
			else if (i % 3 == 1)
				joints.add(new Joint(1f,0f,"PRISMATIC",i));
			else
				joints.add(new Joint(0f,0f,"REVOLUTE",i));
		}
		if (joints != cus)
			configureManJoints(joints);
		else
			configureWebJoints(joints);
	}

	/** 
	 * Initialize a set of joints for a inverse
	 * kinematics model of size 'size'.
	 * @param size  the size of the join set
	 */
	private void makeIKManJoints(ArrayList<Joint> joints, int size) {
		for (int i = 0; i < size; i++) {
			joints.add(new Joint(0f,0f,"REVOLUTE",i));
		}
		if (joints == sev)
			configureManJoints(joints);
		else if (joints == web)
			configureWebJoints(joints);
	}

	/* Establish serial manipulator hierarchy. */
	private void configureManJoints(ArrayList<Joint> joints) {
		for (int i = 0; i < joints.size(); i++) {
			if (i < joints.size() - 1){
				joints.get(i).addChild(joints.get(i+1)); }
			if (i > 0) {
				joints.get(i).setParent(joints.get(i-1)); }
		}
	}

	/* Establish web manipulator hierarchy. */
	private void configureWebJoints(ArrayList<Joint> joints) {
		for (int i = 0; i < joints.size()/2; i++) {
			joints.get(i).addChild(joints.get(2*i+1));
			joints.get(i).addChild(joints.get(2*i+2));
		}
		for (int i = joints.size() - 1; i > 0 ; i -= 2) {
			joints.get(i).setParent(joints.get(i/2-1));
			joints.get(i-1).setParent(joints.get(i/2-1));
		}
	}

	/* Saves a snapshot image of current display. */
	public void snapshot() {
		System.out.println("Taking a snapshot ... snapshot.png");

		glReadBuffer(GL_FRONT);
		int width  = Display.getDisplayMode().getWidth();
		int height = Display.getDisplayMode().getHeight();
		int bpp	= 4; /* Assume a 32-bit display with a byte for R,G,B,A. */
		ByteBuffer buffer = BufferUtils.createByteBuffer(width*height*bpp);
		glReadPixels(0,0,width,height,GL_RGBA,GL_UNSIGNED_BYTE,buffer);

		File file = new File("snapshot.png"); /* Saving here. */
		BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int i = (x + (width * y)) * bpp;
				int r = buffer.get(i) & 0xFF;
				int g = buffer.get(i+1) & 0xFF;
				int b = buffer.get(i+2) & 0xFF;
				image.setRGB(x, height - (y + 1), (0xFF<<24)|(r<<16)|(g<<8)|b);
			}
		}

		try {
			ImageIO.write(image, "PNG", file);
		} catch (IOException e) { e.printStackTrace(); }
	}

    public static void main(String[] args) {
        new PA2().run();
    }
}
