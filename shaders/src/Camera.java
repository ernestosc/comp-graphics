/**
 * COMS W 4160 Computer Graphics
 * Programming Assignment 3
 * 
 * Starter Camera Class
 *
 * @author COMS W 4160 Instruction Team
 * @version 1.0
 */

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class Camera {
    public static float moveSpeed = 0.02f;

    private static float maxLook = 85;

    private static float mouseSensitivity = 0.05f;

    public static Vector3f pos;
    private static Vector3f rotation;

    public static void create() {
        pos = new Vector3f(0, 0, 20);
        rotation = new Vector3f(0, 0, 0);
    }

    public static void apply() {
        if (rotation.y / 360 > 1) {
            rotation.y -= 360;
        } else if (rotation.y / 360 < -1) {
            rotation.y += 360;
        }

        GL11.glRotatef(rotation.x, 1, 0, 0);
        GL11.glRotatef(rotation.y, 0, 1, 0);
        GL11.glRotatef(rotation.z, 0, 0, 1);
        GL11.glTranslatef(-pos.x, -pos.y, -pos.z);
    }

    public static void acceptInput(float delta) {
        acceptInputRotate(delta);
        acceptInputMove(delta);
    }

    public static void acceptInputRotate(float delta) {
        if (Mouse.isInsideWindow() && Mouse.isButtonDown(0)) {
            float mouseDX = Mouse.getDX();
            float mouseDY = -Mouse.getDY();
            rotation.y += mouseDX * mouseSensitivity * delta;
            rotation.x += mouseDY * mouseSensitivity * delta;
            rotation.x = Math.max(-maxLook, Math.min(maxLook, rotation.x));
        }
    }

    public static void acceptInputMove(float delta) {
        boolean keyUp = Keyboard.isKeyDown(Keyboard.KEY_UP);
        boolean keyDown = Keyboard.isKeyDown(Keyboard.KEY_DOWN);
        boolean keyRight = Keyboard.isKeyDown(Keyboard.KEY_RIGHT);
        boolean keyLeft = Keyboard.isKeyDown(Keyboard.KEY_LEFT);
        boolean keyFast = Keyboard.isKeyDown(Keyboard.KEY_EQUALS);
        boolean keySlow = Keyboard.isKeyDown(Keyboard.KEY_MINUS);
        boolean keyFlyUp = Keyboard.isKeyDown(Keyboard.KEY_PERIOD);
        boolean keyFlyDown = Keyboard.isKeyDown(Keyboard.KEY_COMMA);

        float speed;

        if (keyFast) {
            speed = moveSpeed * 5;
        } else if (keySlow) {
            speed = moveSpeed / 2;
        } else {
            speed = moveSpeed;
        }

        speed *= delta;

        if (keyFlyUp) {
            pos.y += speed;
        }
        if (keyFlyDown) {
            pos.y -= speed;
        }

        if (keyDown) {
            pos.x -= Math.sin(Math.toRadians(rotation.y)) * speed;
            pos.z += Math.cos(Math.toRadians(rotation.y)) * speed;
        }
        if (keyUp) {
            pos.x += Math.sin(Math.toRadians(rotation.y)) * speed;
            pos.z -= Math.cos(Math.toRadians(rotation.y)) * speed;
        }
        if (keyLeft) {
            pos.x += Math.sin(Math.toRadians(rotation.y - 90)) * speed;
            pos.z -= Math.cos(Math.toRadians(rotation.y - 90)) * speed;
        }
        if (keyRight) {
            pos.x += Math.sin(Math.toRadians(rotation.y + 90)) * speed;
            pos.z -= Math.cos(Math.toRadians(rotation.y + 90)) * speed;
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
        Camera.rotation = rotation;
    }

    public static Vector3f getRotation() {
        return rotation;
    }

    public static void setRotationX(float x) {
        rotation.x = x;
    }

    public static float getRotationX() {
        return rotation.x;
    }

    public static void addToRotationX(float x) {
        rotation.x += x;
    }

    public static void setRotationY(float y) {
        rotation.y = y;
    }

    public static float getRotationY() {
        return rotation.y;
    }

    public static void addToRotationY(float y) {
        rotation.y += y;
    }

    public static void setRotationZ(float z) {
        rotation.z = z;
    }

    public static float getRotationZ() {
        return rotation.z;
    }

    public static void addToRotationZ(float z) {
        rotation.z += z;
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