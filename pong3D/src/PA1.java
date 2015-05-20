import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.Random;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.glu.*;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

public class PA1 {

    String windowTitle = "3D Pong";  
    public boolean closeRequested = false;

    long lastFrameTime; // used to calculate delta
    
    int globe;     // globe display list instance
    int padplay;   // paddle display list instance
    int pong;      // ball display list instance
        static float p1_theta = 0.0f;   // p1 paddle angle
        static float p2_theta = 0.0f;   // p2 paddle angle
        static float p_theta = 0.0f;    // ball angle
        // paddle and ball position balls
        static Vector3f p_pos = new Vector3f(0.0f, 0.0f, 0.0f);
        static Vector3f p1_pos = new Vector3f(10.0f, 0.0f, 0.0f);
        static Vector3f p2_pos = new Vector3f(10.0f, 0.0f, 0.0f);

    // game state trackers
    static final int P1_TURN = 0;
    static final int P2_TURN = 1;
    int curstate = P2_TURN; // current state tracker
    public boolean outOfBounds = false;
    double angle;

    Texture back; // texture holder

    // lighting buffers
    private static float[] ambient = {0.63f,0.63f,0.63f,1.0f};
    private static float[] diffuse = {1.0f,1.0f,1.0f,1.0f};
    private static float[] specule = {1.0f,1.0f,1.0f,1.0f};

    public void run() {

        createWindow();
        getDelta(); // Initialise delta timer
        initGL();
        angle = rand(0,2*Math.PI);
        int i = 0;
        while (!closeRequested) {
            i++;
            //System.out.println(p_pos.x + " " + p_pos.z);
            float del = getDelta(); // measure frame time interval
            pollInput(del, angle); // read player input and exceute game logic
            if (outOfBounds) {
                p_pos.x = 0.0f;
                p_pos.y = 0.0f;
                p_pos.z = 0.0f;
                outOfBounds = false;
            }
            renderGL(del);  // render current game state
            Display.update(); // update display
        }
        
        cleanup();
    }
    
    // generate a random number
    private static double rand(double min, double max) {
        Random rd = new Random();
        double rnd = rd.nextDouble() * (max - min) + min;
        return rnd;
    }

    private void initGL() {

        /* OpenGL */
        int width = Display.getDisplayMode().getWidth();
        int height = Display.getDisplayMode().getHeight();

        glViewport(0, 0, width, height); // Reset The Current Viewport
        glMatrixMode(GL_PROJECTION); // Select The Projection Matrix
        glLoadIdentity(); // Reset The Projection Matrix
        GLU.gluPerspective(45.0f, ((float) width / (float) height), 0.1f, 100.0f); // Calculate The Aspect Ratio Of The Window
        glMatrixMode(GL_MODELVIEW); // Select The Modelview Matrix
        glLoadIdentity(); // Reset The Modelview Matrix

        glShadeModel(GL_SMOOTH); // Enables Smooth Shading
        glClearColor(0.0f/255, 0.0f/255, 0.0f/255, 0.0f); // Black Background
        glClearDepth(1.0f); // Depth Buffer Setup
        glEnable(GL_DEPTH_TEST); // Enables Depth Testing
        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);
        glEnable(GL_COLOR_MATERIAL);
        glDepthFunc(GL_LEQUAL); // The Type Of Depth Test To Do
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST); // Really Nice Perspective Calculations
        Camera.create();

        try {
            back = TextureLoader.getTexture("JPG", ResourceLoader.getResourceAsStream("world.jpg"));
        } catch (IOException e) {
            e.getMessage();
        }
        padplay = makePaddle(); 
        globe = makeWorld(); 
        pong = makeBall();      
    }

    private void renderGL(float delta) {

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Clear The Screen And The Depth Buffer

        // render Cartesian axes
        glPushMatrix();
            glLoadIdentity();
            Camera.apply();
            glBegin(GL_LINES);
            glColor3f(1.0f, 1.0f, 0.0f);
            glVertex3f(0.0f, 0.0f, 0.0f);
            glVertex3f(50.0f, 0.0f, 0.0f);

            glColor3f(1.0f, 0.0f, 0.0f);
            glVertex3f(0.0f, 0.0f, 0.0f);
            glVertex3f(0.0f, 50.0f, 0.0f);

            glColor3f(0.0f, 0.0f, 1.0f);
            glVertex3f(0.0f, 0.0f, 0.0f);
            glVertex3f(0.0f, 0.0f, 50.0f);
            glEnd();
        glPopMatrix();

        // render player 1 paddle
        glPushMatrix();
            glLoadIdentity();
            Camera.apply();
            glRotatef(180.0f, 0.0f, 1.0f, 0.0f);
            glTranslatef(p1_pos.x, p1_pos.y, p1_pos.z);
            glRotatef(-p1_theta*((float)(180.0f/Math.PI)), 0.0f, 1.0f, 0.0f);
            glScalef(0.1f, 0.5f, 1.0f);
            glCallList(padplay);
        glPopMatrix();

        // render player 2 paddle
        glPushMatrix();
            glLoadIdentity();
            Camera.apply();
            glTranslatef(p2_pos.x, p2_pos.y, p2_pos.z);
            glRotatef(-p2_theta*((float)(180.0f/Math.PI)), 0.0f, 1.0f, 0.0f);
            glScalef(0.1f, 0.5f, 1.0f);
            glCallList(padplay);
        glPopMatrix();

        // render ball
        glPushMatrix();
            glLoadIdentity();
            Camera.apply();
            glTranslatef(p_pos.x, p_pos.y, p_pos.z);
            if (curstate == P1_TURN) {
                diffuse[0] = 0.0f;
                diffuse[2] = 0.0f;
                specule[0] = 0.0f;
                specule[2] = 0.0f;
                glColor3f(10.0f/255, 242.0f/255, 52.0f/255);
            }
            if (curstate == P2_TURN) {
                diffuse[2] = 0.0f;
                specule[2] = 0.0f;
                glColor3f(1.0f, 234.0f/255, 0.0f);
            }
            glCallList(pong);
        glPopMatrix();

        // render lighting
        glPushMatrix();
            glLoadIdentity();
            Camera.apply();
            lightme(p_pos);
        glPopMatrix();

        // render texturized globe environment
        glPushMatrix();
            glLoadIdentity();
            Camera.apply();
            glEnable(GL_TEXTURE_2D);
            back.bind();
            glCallList(globe);
            glDisable(GL_TEXTURE_2D);
        glPopMatrix();

    }

    /**
     * Poll Input
     */
    public void pollInput(float delta, double ang) {
        acceptP1();
        acceptP2();
        Camera.acceptInput(delta);
        double dist = Math.sqrt(Math.pow(p_pos.x,2) + Math.pow(p_pos.z,2));
        //System.out.println("dist was " + dist);
        /*
        boolean p1xmin = 
        boolean p1xmax
        boolean p1zmin
        boolean p1zmax
        boolean p2xmin
        boolean p2xmax
        boolean p2zmin
        boolean p2zmax
        */
        if (curstate == P1_TURN) {
            if (dist > 9.7) {
                if ((Math.abs(p1_pos.x) < Math.abs(p_pos.x - 3) || Math.abs(p1_pos.x) > Math.abs(p_pos.x + 3)) && 
                    (Math.abs(p1_pos.z) < Math.abs(p_pos.z - 3) || Math.abs(p1_pos.z) > Math.abs(p_pos.z + 3))) 
                {
                    outOfBounds = true;
                    return;
                } else {
                    angle = rand(0, 2*Math.PI);
                    moveOut(delta, angle);
                    curstate = P2_TURN;
                }
            } else {
                moveOut(delta, ang);
            }
        } else {
            if (dist > 9.7) {
                if ((Math.abs(p2_pos.x) < Math.abs(p_pos.x - 3) || Math.abs(p2_pos.x) > Math.abs(p_pos.x + 3)) && 
                    (Math.abs(p2_pos.z) < Math.abs(p_pos.z - 3) || Math.abs(p2_pos.z) > Math.abs(p_pos.z + 3)))
                {
                    //System.out.println("OUT OF BOUNDS");
                    outOfBounds = true;
                    return;
                } else {
                    angle = rand(0, 2*Math.PI);
                    //System.out.println("dist was bigger");
                    moveOut(delta, angle);
                    curstate = P1_TURN;
                }
            } else {
                //System.out.println("dist was smaller");
                moveOut(delta, ang);
            }
        }
        // scroll through key events
        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState()) {
                if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE)
                    closeRequested = true;
                else if (Keyboard.getEventKey() == Keyboard.KEY_P)
                    snapshot();
            }
        }

        if (Display.isCloseRequested()) {
            closeRequested = true;
        }
    }

    public void moveOut(float delta, double ang) {
        //System.out.println("calling moveout");
        p_pos.x += (float) Math.cos(ang)*(delta/1000)*1.2;
        p_pos.z += (float) Math.cos(ang-(Math.PI/2))*(delta/1000)*1.2;

    }
    
    public void acceptP1() {
        //boolean keyUp = Keyboard.isKeyDown(Keyboard.KEY_W);
        //boolean keyDown = Keyboard.isKeyDown(Keyboard.KEY_S);
        boolean keyRight = Keyboard.isKeyDown(Keyboard.KEY_D);
        boolean keyLeft = Keyboard.isKeyDown(Keyboard.KEY_A);

        float speed = 0.131f;

        if (keyRight) {
            p1_theta += speed;
        } 
        else if (keyLeft) {
            p1_theta -= speed;
        }

        p1_pos.x = (float) (6 * Math.cos(p1_theta));
        p1_pos.z = (float) (6 * Math.sin(p1_theta));
    }

    public void acceptP2() {
        //boolean keyUp = Keyboard.isKeyDown(Keyboard.KEY_UP);
        //boolean keyDown = Keyboard.isKeyDown(Keyboard.KEY_DOWN);
        boolean keyRight = Keyboard.isKeyDown(Keyboard.KEY_RIGHT);
        boolean keyLeft = Keyboard.isKeyDown(Keyboard.KEY_LEFT);

        float speed = 0.075f;

        if (keyRight) {
            p2_theta += speed;
        } 
        else if (keyLeft) {
            p2_theta -= speed;
        }

        p2_pos.x = (float) (6 * Math.cos(p2_theta));
        p2_pos.z = (float) (6 * Math.sin(p2_theta));

    }

    public void snapshot() {
        System.out.println("Taking a snapshot ... snapshot.png");

        glReadBuffer(GL_FRONT);

        int width = Display.getDisplayMode().getWidth();
        int height= Display.getDisplayMode().getHeight();
        int bpp = 4; // Assuming a 32-bit display with a byte each for red, green, blue, and alpha.
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bpp);
        glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, buffer );

        File file = new File("snapshot.png"); // The file to save to.
        String format = "PNG"; // Example: "PNG" or "JPG"
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
   
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                int i = (x + (width * y)) * bpp;
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
            }
        }
           
        try {
            ImageIO.write(image, format, file);
        } catch (IOException e) { e.printStackTrace(); }
    }
    
    // world map display list
    public int makeWorld() {
        int worldlist = glGenLists(1);
        glNewList(worldlist, GL_COMPILE);
        {
            Sphere world = new Sphere();
            world.setTextureFlag(true);
            world.setDrawStyle(GLU.GLU_FILL);
            world.setNormals(GLU.GLU_SMOOTH);
            glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            world.draw(17.0f, 300, 300);
        }
        glEndList();
        return worldlist;
    }

    // paddle display list
    public int makePaddle() {
        int paddlist = glGenLists(1);
        glNewList(paddlist, GL_COMPILE);
        {
            glBegin(GL_QUADS); // Start Drawing The Cube
            {
                glColor3f(100.0f/255, 106.0f/255, 112.0f/255); // Set The Color To Gray
                glNormal3f(0.0f, 1.0f, 0.0f);
                glVertex3f(1.0f, 1.0f, -1.0f); // Top Right Of The Quad (Top)
                glNormal3f(0.0f, 1.0f, 0.0f);
                glVertex3f(-1.0f, 1.0f, -1.0f); // Top Left Of The Quad (Top)
                glNormal3f(0.0f, 1.0f, 0.0f);
                glVertex3f(-1.0f, 1.0f, 1.0f); // Bottom Left Of The Quad (Top)
                glNormal3f(0.0f, 1.0f, 0.0f);
                glVertex3f(1.0f, 1.0f, 1.0f); // Bottom Right Of The Quad (Top)

                glColor3f(100.0f/255, 106.0f/255, 112.0f/255); // Set The Color To Gray
                glNormal3f(0.0f, -1.0f, 0.0f);
                glVertex3f(1.0f, -1.0f, 1.0f); // Top Right Of The Quad (Bottom)
                glNormal3f(0.0f, -1.0f, 0.0f);
                glVertex3f(-1.0f, -1.0f, 1.0f); // Top Left Of The Quad (Bottom)
                glNormal3f(0.0f, -1.0f, 0.0f);
                glVertex3f(-1.0f, -1.0f, -1.0f); // Bottom Left Of The Quad (Bottom)
                glNormal3f(0.0f, -1.0f, 0.0f);
                glVertex3f(1.0f, -1.0f, -1.0f); // Bottom Right Of The Quad (Bottom)

                glColor3f(100.0f/255, 106.0f/255, 112.0f/255); // Set The Color To Gray
                glNormal3f(0.0f, 0.0f, 1.0f);
                glVertex3f(1.0f, 1.0f, 1.0f); // Top Right Of The Quad (Front)
                glNormal3f(0.0f, 0.0f, 1.0f);
                glVertex3f(-1.0f, 1.0f, 1.0f); // Top Left Of The Quad (Front)
                glNormal3f(0.0f, 0.0f, 1.0f);
                glVertex3f(-1.0f, -1.0f, 1.0f); // Bottom Left Of The Quad (Front)
                glNormal3f(0.0f, 0.0f, 1.0f);
                glVertex3f(1.0f, -1.0f, 1.0f); // Bottom Right Of The Quad (Front)

                glColor3f(100.0f/255, 106.0f/255, 112.0f/255); // Set The Color To Gray
                glNormal3f(0.0f, 0.0f, -1.0f);
                glVertex3f(1.0f, -1.0f, -1.0f); // Bottom Left Of The Quad (Back)
                glNormal3f(0.0f, 0.0f, -1.0f);
                glVertex3f(-1.0f, -1.0f, -1.0f); // Bottom Right Of The Quad (Back)
                glNormal3f(0.0f, 0.0f, -1.0f);
                glVertex3f(-1.0f, 1.0f, -1.0f); // Top Right Of The Quad (Back)
                glNormal3f(0.0f, 0.0f, -1.0f);
                glVertex3f(1.0f, 1.0f, -1.0f); // Top Left Of The Quad (Back)

                glColor3f(242.0f/255, 29.0f/255, 65.0f/255); // Set The Color To Red
                glNormal3f(1.0f, 0.0f, 0.0f);
                glVertex3f(-1.0f, 1.0f, 1.0f); // Top Right Of The Quad (Left)
                glNormal3f(1.0f, 0.0f, 0.0f);
                glVertex3f(-1.0f, 1.0f, -1.0f); // Top Left Of The Quad (Left)
                glNormal3f(1.0f, 0.0f, 0.0f);
                glVertex3f(-1.0f, -1.0f, -1.0f); // Bottom Left Of The Quad (Left)
                glNormal3f(1.0f, 0.0f, 0.0f);
                glVertex3f(-1.0f, -1.0f, 1.0f); // Bottom Right Of The Quad (Left)

                glColor3f(48.0f/255, 108.0f/255, 207.0f/255); // Set The Color To Red
                glVertex3f(1.0f, 1.0f, -1.0f); // Top Right Of The Quad (Right)
                glVertex3f(1.0f, 1.0f, 1.0f); // Top Left Of The Quad (Right)
                glVertex3f(1.0f, -1.0f, 1.0f); // Bottom Left Of The Quad (Right)
                glVertex3f(1.0f, -1.0f, -1.0f); // Bottom Right Of The Quad (Right)
            }
            glEnd(); // Done Drawing The Quad
        }
        glEndList();
        return paddlist;
    }

    public int makeBall() {
        int ballist = glGenLists(1);
        glNewList(ballist, GL_COMPILE);
        {
            Sphere ball = new Sphere();
            glColor3f(1.0f, 1.0f, 1.0f);
            ball.draw(0.3f, 300, 300);
        }
        glEndList();
        return ballist;
    }

    public void lightme(Vector3f ballpos) {
        FloatBuffer amb = BufferUtils.createFloatBuffer(ambient.length);
                    amb.put(ambient);
                    amb.flip();
        FloatBuffer dif = BufferUtils.createFloatBuffer(diffuse.length);
                    dif.put(diffuse);
                    dif.flip();
        FloatBuffer spc = BufferUtils.createFloatBuffer(specule.length);
                    spc.put(specule);
                    spc.flip();
        FloatBuffer pos = BufferUtils.createFloatBuffer(4);
        pos.put(ballpos.x).put(ballpos.y+1.0f).put(ballpos.z).put(1.0f).flip();

        glColorMaterial(GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE);
        glLight(GL_LIGHT0, GL_POSITION, pos);
        glLight(GL_LIGHT0, GL_DIFFUSE, dif);
        glLight(GL_LIGHT0, GL_AMBIENT, amb);
        glLight(GL_LIGHT0, GL_SPECULAR, spc);
        glMaterial(GL_FRONT, GL_DIFFUSE, dif);
        glMateriali(GL_FRONT, GL_SHININESS, 0);        
    }

    /** 
     * Calculate how many milliseconds have passed 
     * since last frame.
     * 
     * @return milliseconds passed since last frame 
     */
    public int getDelta() {
        long time = (Sys.getTime() * 1000) / Sys.getTimerResolution();
        int delta = (int) (time - lastFrameTime);
        lastFrameTime = time;
     
        return delta;
    }

    private void createWindow() {
        try {
            Display.setDisplayMode(new DisplayMode(1152, 648));
            Display.setVSyncEnabled(true); // TURN OFF THO?
            Display.setTitle(windowTitle);
            Display.create();
        } catch (LWJGLException e) {
            Sys.alert("Error", "Initialization failed!\n\n" + e.getMessage());
            System.exit(0);
        }
    }
    
    /**
     * Destroy and clean up resources
     */
    private void cleanup() {
        Display.destroy();
    }
    
    public static void main(String[] args) {
        new PA1().run();
    }
    
    public static class Camera {
        public static float moveSpeed = 0.01f;

        private static float maxLook = 85;

        private static float mouseSensitivity = 0.05f;

        private static Vector3f pos;
        private static Vector3f rotation;

        public static void create() {
            pos = new Vector3f(13.0f, 1.7f, 0);
            rotation = new Vector3f(0.0f, -90.0f, 0);
        }

        public static void apply() {
            if (rotation.y / 360 > 1) {
                rotation.y -= 360;
            } else if (rotation.y / 360 < -1) {
                rotation.y += 360;
            }

            //System.out.println(rotation);
            glRotatef(rotation.x, 1, 0, 0);
            glRotatef(rotation.y, 0, 1, 0);
            glRotatef(rotation.z, 0, 0, 1);
            glTranslatef(-pos.x, -pos.y, -pos.z);
        }

        public static void acceptInput(float delta) {
            //System.out.println("delta="+delta);
            acceptInputRotate(delta);
            acceptInputMove(delta);
        }

        public static void acceptInputRotate(float delta) {
            rotation.y = p2_theta*(float)(180/Math.PI) - 90.0f;
        }

        public static void acceptInputMove(float delta) {

            float speed = 0.05f;

            pos.x = ((float) (6 * Math.cos(p2_theta))) * (17.0f/10.0f);
            pos.z = ((float) (6 * Math.sin(p2_theta))) * (17.0f/10.0f);

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
}
