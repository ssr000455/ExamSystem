package render;

import com.jogamp.opengl.*;
import java.nio.*;

public class FramebufferManager {
    
    private int framebuffer;
    private int colorTexture;
    private int depthRenderbuffer;
    private int width;
    private int height;
    
    public void initialize(GL2 gl, int w, int h) {
        this.width = w;
        this.height = h;
        
        int[] fb = new int[1];
        gl.glGenFramebuffers(1, fb, 0);
        framebuffer = fb[0];
        gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, framebuffer);
        
        int[] tex = new int[1];
        gl.glGenTextures(1, tex, 0);
        colorTexture = tex[0];
        gl.glBindTexture(GL2.GL_TEXTURE_2D, colorTexture);
        gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA, width, height, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, null);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
        gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_COLOR_ATTACHMENT0, GL2.GL_TEXTURE_2D, colorTexture, 0);
        
        int[] rbo = new int[1];
        gl.glGenRenderbuffers(1, rbo, 0);
        depthRenderbuffer = rbo[0];
        gl.glBindRenderbuffer(GL2.GL_RENDERBUFFER, depthRenderbuffer);
        gl.glRenderbufferStorage(GL2.GL_RENDERBUFFER, GL2.GL_DEPTH_COMPONENT24, width, height);
        gl.glFramebufferRenderbuffer(GL2.GL_FRAMEBUFFER, GL2.GL_DEPTH_ATTACHMENT, GL2.GL_RENDERBUFFER, depthRenderbuffer);
        
        int status = gl.glCheckFramebufferStatus(GL2.GL_FRAMEBUFFER);
        if (status != GL2.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("Framebuffer incomplete: " + status);
        }
        
        gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
    }
    
    public void bind(GL2 gl) {
        gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, framebuffer);
        gl.glViewport(0, 0, width, height);
    }
    
    public void unbind(GL2 gl) {
        gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
    }
    
    public int getColorTexture() {
        return colorTexture;
    }
    
    public void dispose(GL2 gl) {
        int[] fb = {framebuffer};
        gl.glDeleteFramebuffers(1, fb, 0);
        int[] tex = {colorTexture};
        gl.glDeleteTextures(1, tex, 0);
        int[] rbo = {depthRenderbuffer};
        gl.glDeleteRenderbuffers(1, rbo, 0);
    }
    
    public void resize(GL2 gl, int w, int h) {
        dispose(gl);
        initialize(gl, w, h);
    }
}
