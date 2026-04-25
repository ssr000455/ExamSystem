package render;

import com.jogamp.opengl.*;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import java.io.*;
import java.nio.file.*;

public class SkyboxRenderer {
    
    private int displayList = -1;
    private Texture texture;
    private boolean initialized = false;
    
    private static final float SIZE = 50.0f;
    
    public void initialize(GL2 gl, String[] texturePaths) throws IOException {
        if (texturePaths != null && texturePaths.length > 0) {
            File texFile = new File(texturePaths[0]);
            if (texFile.exists()) {
                texture = TextureIO.newTexture(texFile, false);
            }
        }
        
        displayList = gl.glGenLists(1);
        gl.glNewList(displayList, GL2.GL_COMPILE);
        
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glDepthMask(false);
        
        if (texture != null) {
            texture.enable(gl);
            texture.bind(gl);
        }
        
        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glBegin(GL2.GL_QUADS);
        
        // 前面
        gl.glTexCoord2f(0, 0); gl.glVertex3f(-SIZE, -SIZE, -SIZE);
        gl.glTexCoord2f(1, 0); gl.glVertex3f( SIZE, -SIZE, -SIZE);
        gl.glTexCoord2f(1, 1); gl.glVertex3f( SIZE,  SIZE, -SIZE);
        gl.glTexCoord2f(0, 1); gl.glVertex3f(-SIZE,  SIZE, -SIZE);
        
        // 后面
        gl.glTexCoord2f(0, 0); gl.glVertex3f( SIZE, -SIZE, SIZE);
        gl.glTexCoord2f(1, 0); gl.glVertex3f(-SIZE, -SIZE, SIZE);
        gl.glTexCoord2f(1, 1); gl.glVertex3f(-SIZE,  SIZE, SIZE);
        gl.glTexCoord2f(0, 1); gl.glVertex3f( SIZE,  SIZE, SIZE);
        
        // 左面
        gl.glTexCoord2f(0, 0); gl.glVertex3f(-SIZE, -SIZE, SIZE);
        gl.glTexCoord2f(1, 0); gl.glVertex3f(-SIZE, -SIZE, -SIZE);
        gl.glTexCoord2f(1, 1); gl.glVertex3f(-SIZE,  SIZE, -SIZE);
        gl.glTexCoord2f(0, 1); gl.glVertex3f(-SIZE,  SIZE, SIZE);
        
        // 右面
        gl.glTexCoord2f(0, 0); gl.glVertex3f(SIZE, -SIZE, -SIZE);
        gl.glTexCoord2f(1, 0); gl.glVertex3f(SIZE, -SIZE, SIZE);
        gl.glTexCoord2f(1, 1); gl.glVertex3f(SIZE,  SIZE, SIZE);
        gl.glTexCoord2f(0, 1); gl.glVertex3f(SIZE,  SIZE, -SIZE);
        
        // 上面
        gl.glTexCoord2f(0, 0); gl.glVertex3f(-SIZE, SIZE, -SIZE);
        gl.glTexCoord2f(1, 0); gl.glVertex3f( SIZE, SIZE, -SIZE);
        gl.glTexCoord2f(1, 1); gl.glVertex3f( SIZE, SIZE,  SIZE);
        gl.glTexCoord2f(0, 1); gl.glVertex3f(-SIZE, SIZE,  SIZE);
        
        // 下面
        gl.glTexCoord2f(0, 0); gl.glVertex3f(-SIZE, -SIZE,  SIZE);
        gl.glTexCoord2f(1, 0); gl.glVertex3f( SIZE, -SIZE,  SIZE);
        gl.glTexCoord2f(1, 1); gl.glVertex3f( SIZE, -SIZE, -SIZE);
        gl.glTexCoord2f(0, 1); gl.glVertex3f(-SIZE, -SIZE, -SIZE);
        
        gl.glEnd();
        
        if (texture != null) {
            texture.disable(gl);
        }
        
        gl.glDepthMask(true);
        gl.glEnable(GL2.GL_LIGHTING);
        
        gl.glEndList();
        initialized = true;
    }
    
    public void render(GL2 gl, double camX, double camY, double camZ) {
        if (!initialized) return;
        
        gl.glPushMatrix();
        gl.glTranslated(camX, camY, camZ);
        gl.glCallList(displayList);
        gl.glPopMatrix();
    }
    
    public void dispose(GL2 gl) {
        if (displayList >= 0) {
            gl.glDeleteLists(displayList, 1);
        }
        if (texture != null) {
            texture.destroy(gl);
        }
    }
}
