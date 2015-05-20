/**
 * COMS W 4160 Computer Graphics
 * Programming Assignment 2
 * Part A
 * 
 * Animatics Camera Class
 *
 * @author Ernesto Sandoval Castillo (es3187)
 * @version 1.0
 */

import java.nio.*;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class Camera {
	/* User-Interaction Parameters */
	private static float moveSpeed = 0.02f;
	private static float mouseSensitivity = 0.02f;
	private static float maxLook = 85;

	/* Positional Parameters */
	private static Vector3f pos;
	private static Vector3f rot;

	/* Tracks IK or FK. */
	public static byte mode; 

	public static void create() {
		pos = new Vector3f(0, 1, 17);
		rot = new Vector3f(0, 0, 0);
	}

	/* Update camera orientation. */
	public static void apply() {
		if (rot.y / 360 > 1) {
			rot.y -= 360;
		} else if (rot.y / 360  < -1) {
			rot.y += 360;
		}
		glRotatef(rot.x, 1, 0, 0);
		glRotatef(rot.y, 0, 1, 0);
		glRotatef(rot.z, 0, 0, 1);
		glTranslatef(-pos.x, -pos.y, -pos.z);
	}

	public static void acceptInput(float delta) {
		if (mode == 0)
			acceptInputRotate(delta);
		acceptInputMove(delta);
	}

	private static void acceptInputRotate(float delta) {
		if (Mouse.isInsideWindow() && Mouse.isButtonDown(0)) {
			float mouseDX = Mouse.getDX();
			float mouseDY = Mouse.getDY();

			rot.y += mouseDX * mouseSensitivity * delta;
			rot.x += mouseDY * mouseSensitivity * delta;
			rot.x = Math.max(-maxLook, Math.min(maxLook, rot.x));
		}
	}

	private static void acceptInputMove(float delta) {
		/* Poll camera controls. */
		boolean keyUp      = Keyboard.isKeyDown(Keyboard.KEY_UP);
		boolean keyDown    = Keyboard.isKeyDown(Keyboard.KEY_DOWN);
		boolean keyRight   = Keyboard.isKeyDown(Keyboard.KEY_RIGHT);
		boolean keyLeft    = Keyboard.isKeyDown(Keyboard.KEY_LEFT);
		boolean keyFast    = Keyboard.isKeyDown(Keyboard.KEY_PERIOD);
		boolean keySlow    = Keyboard.isKeyDown(Keyboard.KEY_COMMA);
		boolean keyFlyUp   = Keyboard.isKeyDown(Keyboard.KEY_SPACE);
		boolean keyFlyDown = Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);

		float speed;

		if (keyFast) {
			speed = moveSpeed * 5; /* Speed up cam moves. */
		} else if (keySlow) {
			speed = moveSpeed / 2; /* Slow down cam moves. */
		} else {
			speed = moveSpeed; /* Keep current cam move speed. */
		}

		speed *= delta; /* Scale cam move speed with frame interval. */

		if (keyFlyUp) {
			pos.y += speed; /* Move up. */
		}
		if (keyFlyDown) {
			pos.y -= speed; /* Move down. */
		}
		if (keyDown) { /* Move backward. */
			pos.x -= Math.sin(Math.toRadians(rot.y)) * speed;
			pos.z += Math.cos(Math.toRadians(rot.y)) * speed;
		}
        if (keyUp) {   /* Move forward. */
            pos.x += Math.sin(Math.toRadians(rot.y)) * speed;
            pos.z -= Math.cos(Math.toRadians(rot.y)) * speed;
        }
        if (keyLeft) { /* Move left. */
            pos.x += Math.sin(Math.toRadians(rot.y - 90)) * speed;
            pos.z -= Math.cos(Math.toRadians(rot.y - 90)) * speed;
        }
        if (keyRight) { /* Move right. */
            pos.x += Math.sin(Math.toRadians(rot.y + 90)) * speed;
            pos.z -= Math.cos(Math.toRadians(rot.y + 90)) * speed;
        }
	}

    public static void setSpeed(float speed) {
        moveSpeed = speed;
    }

    public static void setPos(Vector3f pos) {
        Camera.pos = pos;
    }

    public static Vector3f getPos() {
        return pos;
    }

    public static void setX(float x) {
        pos.x = x;
    }

    public static float getX() {
        return pos.x;
    }

    public static void addToX(float x) {
        pos.x += x;
    }

    public static void setY(float y) {
        pos.y = y;
    }

    public static float getY() {
        return pos.y;
    }

    public static void addToY(float y) {
        pos.y += y;
    }

    public static void setZ(float z) {
        pos.z = z;
    }

    public static float getZ() {
        return pos.z;
    }

    public static void addToZ(float z) {
        pos.z += z;
    }

    public static void setRotation(Vector3f rotation) {
        Camera.rot = rot;
    }

    public static Vector3f getRotation() {
        return rot;
    }

    public static void setRotationX(float x) {
        rot.x = x;
    }

    public static float getRotationX() {
        return rot.x;
    }

    public static void addToRotationX(float x) {
        rot.x += x;
    }

    public static void setRotationY(float y) {
        rot.y = y;
    }

    public static float getRotationY() {
        return rot.y;
    }

    public static void addToRotationY(float y) {
        rot.y += y;
    }

    public static void setRotationZ(float z) {
        rot.z = z;
    }

    public static float getRotationZ() {
        return rot.z;
    }

    public static void addToRotationZ(float z) {
        rot.z += z;
    }

    public static void setMaxLook(float maxLook) {
        Camera.maxLook = maxLook;
    }

    public static float getMaxLook() {
        return maxLook;
    }

    public static void setMouseSensitivity(float mouseSensitivity) {
        Camera.mouseSensitivity = mouseSensitivity;
    }

    public static float getMouseSensitivity() {
        return mouseSensitivity;
    }
}