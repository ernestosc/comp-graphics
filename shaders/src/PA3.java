/**
 * COMS W 4160 Computer Graphics
 * Programming Assignment 3
 * 
 * Shader Fun - Main Project Class
 *
 * @author Ernesto Sandoval Castillo (es3187)
 * @version 1.0
 */

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.io.File;
import java.io.*;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

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


public class PA3 {

    private String windowTitle = "Shader Fun";
    public boolean closeRequested = false;

    private long lastFrameTime; /* Used to calculate delta. */

    /* Shader Code Variables */
    public ShaderProgram gshader;  /* Gouraud      */
    public ShaderProgram bpshader; /* Blinn-Phong  */
    public ShaderProgram chshader; /* Checkerboard */
    public ShaderProgram tshader;  /* Toon         */
    public ShaderProgram dshader;  /* Dots         */
    public ShaderProgram brshader; /* Bricks       */
    /* Gouraud Shader Pair */
    public String gvshader;
    public String gfshader;
    /* Blinn-Phong Shader Pair */
    public String bpvshader;
    public String bpfshader;
    /* Checkboard Texture Shader */
    public String chvshader;
    public String chfshader;
    /* Toon Shader */
    public String tvshader;
    public String tfshader;
    /* Dots Texture Shader */
    public String dvshader;
    public String dfshader;
    /* Brick Texture Shader */
    public String brvshader;
    public String brfshader;

    /* Program Control Flow Parameters */
    private byte GVF  = 0; /* Gouraud Shader */
    private byte BPVF = 1; /* Blinn-Phong Shader */
    private byte CHVF = 2; /* Checkboard Texture */
    private byte TVF  = 3; /* Toon Shader */
    private byte DVF  = 4; /* Dots Texture */
    private byte BRVF = 5; /* Brick Texture */
    private byte CURRENT_SHADER = GVF; /* Defaults to Gouraud */

    private int sph; /* Sphere display list. */
    private int sim; /* Simpler sphere list. */
    
    /* Lighting Buffers */

    /* Global Ambient Attribute */
    private float[] gam = {1.0f,1.0f,1.0f,1.0f};
    /* Light Positions */
    private float[] pos1 = {25.0f,25.0f,25.0f,1.0f}; 
    private float[] pos2 = {-25.0f,25.0f,25.0f,1.0f}; 
    private float[] pos3 = {0.0f,0.0f,-25.0f,1.0f};
    private float[] pos4 = {0.0f,-25.0f,0.0f,1.0f};
    /* Ambient Light Intensities */
    private float[] amb1 = {0.3f,0.3f,0.23f,1.0f}; 
    private float[] amb2 = {0.1f,0.0f,0.0f,1.0f};
    private float[] amb3 = {0.2f,0.2f,0.0f,1.0f};
    private float[] amb4 = {0.0f,0.0f,0.2f,1.0f};
    /* Diffuse Light Intensities */
    private float[] dif1 = {0.7f,0.7f,0.55f,1.0f};
    private float[] dif2 = {0.2f,0.0f,0.0f,1.0f};
    private float[] dif3 = {0.3f,0.3f,0.0f,1.0f};
    private float[] dif4 = {0.0f,0.0f,0.3f,1.0f};
    /* Specular Light Intensities */
    private float[] spc1 = {1.0f,1.0f,0.78f,1.0f}; 
    private float[] spc2 = {0.1f,0.0f,0.0f,1.0f};
    private float[] spc3 = {0.1f,0.1f,0.0f,1.0f};
    private float[] spc4 = {0.0f,0.0f,0.1f,1.0f};

    /* Material Attributes */
    private float[] mat_amb = {0.4f,0.4f,0.4f,1.0f}; /* Amb. Material */
    private float[] mat_dif = {0.5f,0.5f,0.5f,1.0f}; /* Dif. Material */
    private float[] mat_spc = {0.5f,0.5f,0.5f,1.0f}; /* Spc. Material */
    private float shn = 78.0f;               /* Shininess of Material */

    public void run() {

        createWindow();
        getDelta(); /* Initialise delta timer. */
        initGL();
        initShaders();
        
        while (!closeRequested) {
            pollInput();
            renderGL();

            Display.update();
        }
        
        Display.destroy();
    }
    
    private String readFile(String filepath) 
        throws IOException, FileNotFoundException {
        String file = ""; String line;
        BufferedReader br =
            new BufferedReader(new FileReader(filepath));
        while((line = br.readLine()) != null) {
            file += line + "\n";
        }
        return file;
    }

    private void initGL() {

        int width = Display.getDisplayMode().getWidth();
        int height = Display.getDisplayMode().getHeight();

        glViewport(0, 0, width, height); /* Reset the viewport. */
        glMatrixMode(GL_PROJECTION); /* Select projection matrix. */
        glLoadIdentity(); /* Reset projection matrix. */
        GLU.gluPerspective(45.0f, ((float)width/(float)height), 0.1f, 10000.0f);
        glMatrixMode(GL_MODELVIEW); /* Select modelview matrix. */
        glLoadIdentity(); /* Reset modelview matrix. */

        glShadeModel(GL_SMOOTH); /* Enables smooth shading. */
        /* Black background is best to show off lighting. */
        glClearColor(0,0,0,0);
        glClearDepth(1.0f); /* Depth buffer setup. */
        glEnable(GL_DEPTH_TEST); /* Enables depth testing */
        glDepthFunc(GL_LEQUAL); /* The type of depth test to do. */
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

        Camera.create(); 
        Camera.apply();
        initLights(); 
        sph = makeSphere();
        sim = makeBall();
        /* Read in shader code from files. */ 
        try {
            gvshader = readFile("src/gouraud.vert");
            gfshader = readFile("src/gouraud.frag");
            bpvshader = readFile("src/bp.vert");
            bpfshader = readFile("src/bp.frag"); 
            chvshader = readFile("src/ch.vert");
            chfshader = readFile("src/ch.frag");
            tvshader = readFile("src/toon.vert");
            tfshader = readFile("src/toon.frag");
            dvshader = readFile("src/dots.vert");
            dfshader = readFile("src/dots.frag");
            brvshader = readFile("src/brick.vert");
            brfshader = readFile("src/brick.frag");
        } catch (IOException e) {
            e.getMessage();
        } 
    }
    
    /* Initialize lighting features. */
    private void initLights() {
        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);
        glEnable(GL_LIGHT1);
        glEnable(GL_LIGHT2);
        glEnable(GL_LIGHT3);
        glEnable(GL_COLOR_MATERIAL);
        glLightModel(GL_LIGHT_MODEL_AMBIENT, toFloatBuffer(gam));

        glColorMaterial(GL_FRONT_AND_BACK, GL_AMBIENT);
        glColorMaterial(GL_FRONT_AND_BACK, GL_DIFFUSE);
        glColorMaterial(GL_FRONT_AND_BACK, GL_SPECULAR);

        glMaterial(GL_FRONT, GL_AMBIENT, toFloatBuffer(mat_amb));
        glMaterial(GL_FRONT, GL_DIFFUSE, toFloatBuffer(mat_dif));
        glMaterial(GL_FRONT, GL_SPECULAR, toFloatBuffer(mat_spc));
        glMaterialf(GL_FRONT, GL_SHININESS, shn);

        glLight(GL_LIGHT0, GL_POSITION, toFloatBuffer(pos1));
        glLight(GL_LIGHT0, GL_AMBIENT , toFloatBuffer(amb1));
        glLight(GL_LIGHT0, GL_DIFFUSE , toFloatBuffer(dif1));
        glLight(GL_LIGHT0, GL_SPECULAR, toFloatBuffer(spc1));
        glLight(GL_LIGHT1, GL_POSITION, toFloatBuffer(pos2));
        glLight(GL_LIGHT1, GL_AMBIENT , toFloatBuffer(amb2));
        glLight(GL_LIGHT1, GL_DIFFUSE , toFloatBuffer(dif2));
        glLight(GL_LIGHT1, GL_SPECULAR, toFloatBuffer(spc2));
        glLight(GL_LIGHT2, GL_POSITION, toFloatBuffer(pos3));
        glLight(GL_LIGHT2, GL_AMBIENT , toFloatBuffer(amb3));
        glLight(GL_LIGHT2, GL_DIFFUSE , toFloatBuffer(dif3));
        glLight(GL_LIGHT2, GL_SPECULAR, toFloatBuffer(spc3));
        glLight(GL_LIGHT3, GL_POSITION, toFloatBuffer(pos4));
        glLight(GL_LIGHT3, GL_AMBIENT , toFloatBuffer(amb4));
        glLight(GL_LIGHT3, GL_DIFFUSE , toFloatBuffer(dif4));
        glLight(GL_LIGHT3, GL_SPECULAR, toFloatBuffer(spc4));
    }

    /* Reset lighting parameters. */
    private void setLights() {
        glLight(GL_LIGHT0, GL_POSITION, toFloatBuffer(pos1));
        glLight(GL_LIGHT1, GL_POSITION, toFloatBuffer(pos2));
        glLight(GL_LIGHT2, GL_POSITION, toFloatBuffer(pos3));
        glLight(GL_LIGHT3, GL_POSITION, toFloatBuffer(pos4));
    }

    /* Format float arrays into OpenGL-friendly float buffers. */
    private FloatBuffer toFloatBuffer(float[] fs) {
        FloatBuffer fb = BufferUtils.createFloatBuffer(fs.length);
        fb.put(fs);
        fb.flip();
        return fb;
    }

    private void initShaders() {
        try {
            gshader = new ShaderProgram(gvshader, gfshader);
            bpshader = new ShaderProgram(bpvshader, bpfshader);
            chshader = new ShaderProgram(chvshader, chfshader);
            tshader = new ShaderProgram(tvshader,tfshader);
            dshader = new ShaderProgram(dvshader, dfshader);
            brshader = new ShaderProgram(brvshader, brfshader);
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void renderGL() {

        /* Start to use shaders. */
        if (CURRENT_SHADER == GVF)
            gshader.begin();
        else if (CURRENT_SHADER == BPVF)
            bpshader.begin();
        else if (CURRENT_SHADER == CHVF)
            chshader.begin();
        else if (CURRENT_SHADER == TVF)
            tshader.begin();
        else if (CURRENT_SHADER == DVF)
            dshader.begin();
        else if (CURRENT_SHADER == BRVF)
            brshader.begin();

        /* Clear the screen and depth buffers. */
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity(); /* Reset the view. */
        Camera.apply();
        setLights(); /* Reset light position. */

        /* Draw object. */
        if (CURRENT_SHADER != TVF) {
            glPushMatrix();
            glRotatef(90,1,0,0);
            glCallList(sph);
            glPopMatrix();
        } else {
            glCallList(sim);
        }

        if (CURRENT_SHADER == GVF)
            gshader.end();
        else if (CURRENT_SHADER == BPVF)
            bpshader.end();
        else if (CURRENT_SHADER == CHVF)
            chshader.end();
        else if (CURRENT_SHADER == TVF)
            tshader.end();
        else if (CURRENT_SHADER == DVF)
            dshader.end();
        else if (CURRENT_SHADER == BRVF)
            brshader.end();
    }

    /* Sphere display list. */
    private int makeSphere() {
        int sph = glGenLists(1);
        glNewList(sph, GL_COMPILE);
            Sphere obj = new Sphere();
            obj.setTextureFlag(true);
            obj.setDrawStyle(GLU.GLU_FILL);
            obj.setNormals(GLU.GLU_SMOOTH);
            obj.draw(10,500,500);
        glEndList();
        return sph;
    }

    /* Less-refined sphere. */
    private int makeBall() {
        int bib = glGenLists(1);
        glNewList(bib, GL_COMPILE);
            Sphere bab = new Sphere();
            bab.setTextureFlag(true);
            bab.setDrawStyle(GLU.GLU_FILL);
            bab.setNormals(GLU.GLU_SMOOTH);
            bab.draw(10,50,50);
        glEndList();
        return bib;
    }

    public void pollInput() {
        Camera.acceptInput(getDelta());
        /* Scroll through key events. */
        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState()) {
                if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE)
                    closeRequested = true;
                else if (Keyboard.getEventKey() == Keyboard.KEY_P)
                    snapshot();
                else if (Keyboard.getEventKey() == Keyboard.KEY_1 && CURRENT_SHADER != GVF)
                    CURRENT_SHADER = GVF;
                else if (Keyboard.getEventKey() == Keyboard.KEY_2 && CURRENT_SHADER != BPVF)
                    CURRENT_SHADER = BPVF;
                else if (Keyboard.getEventKey() == Keyboard.KEY_3 && CURRENT_SHADER != CHVF)
                    CURRENT_SHADER = CHVF;
                else if (Keyboard.getEventKey() == Keyboard.KEY_4 && CURRENT_SHADER != TVF)
                    CURRENT_SHADER = TVF;
                else if (Keyboard.getEventKey() == Keyboard.KEY_5 && CURRENT_SHADER != DVF)
                    CURRENT_SHADER = DVF;
                else if (Keyboard.getEventKey() == Keyboard.KEY_6 && CURRENT_SHADER != BRVF)
                    CURRENT_SHADER = BRVF;
            }
        }

        if (Display.isCloseRequested()) {
            closeRequested = true;
        }
    }

    /* Saves a snapshot of the current display. */
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
            Display.setDisplayMode(new DisplayMode(960, 540));
            Display.setVSyncEnabled(true);
            Display.setTitle(windowTitle);
            Display.create();
        } catch (LWJGLException e) {
            Sys.alert("Error", "Initialization failed!\n\n" + e.getMessage());
            System.exit(0);
        }
    }
    
    public static void main(String[] args) {
        new PA3().run();
    }
}
