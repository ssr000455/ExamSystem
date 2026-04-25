package render;

import com.jogamp.opengl.*;
import java.util.*;

public class ParticleSystem {
    
    public static class Particle {
        public double[] position = new double[3];
        public double[] velocity = new double[3];
        public float[] color = {1.0f, 1.0f, 1.0f, 1.0f};
        public float size = 0.1f;
        public float life = 1.0f;
        public float maxLife = 1.0f;
    }
    
    private List<Particle> particles = new ArrayList<>();
    private Random random = new Random();
    private int displayList = -1;
    
    public void emit(double x, double y, double z, int count) {
        for (int i = 0; i < count; i++) {
            Particle p = new Particle();
            p.position[0] = x + (random.nextDouble() - 0.5) * 0.5;
            p.position[1] = y + random.nextDouble() * 0.5;
            p.position[2] = z + (random.nextDouble() - 0.5) * 0.5;
            p.velocity[0] = (random.nextDouble() - 0.5) * 0.05;
            p.velocity[1] = random.nextDouble() * 0.1;
            p.velocity[2] = (random.nextDouble() - 0.5) * 0.05;
            p.life = 1.0f;
            p.maxLife = 1.0f;
            particles.add(p);
        }
    }
    
    public void update() {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.position[0] += p.velocity[0];
            p.position[1] += p.velocity[1];
            p.position[2] += p.velocity[2];
            p.velocity[1] -= 0.002;
            p.life -= 0.01f;
            p.color[3] = p.life;
            
            if (p.life <= 0) {
                it.remove();
            }
        }
    }
    
    public void render(GL2 gl) {
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
        gl.glDepthMask(false);
        
        for (Particle p : particles) {
            gl.glColor4fv(p.color, 0);
            gl.glPushMatrix();
            gl.glTranslated(p.position[0], p.position[1], p.position[2]);
            
            float size = p.size * p.life;
            gl.glBegin(GL2.GL_QUADS);
            gl.glTexCoord2f(0, 0); gl.glVertex3f(-size, -size, 0);
            gl.glTexCoord2f(1, 0); gl.glVertex3f( size, -size, 0);
            gl.glTexCoord2f(1, 1); gl.glVertex3f( size,  size, 0);
            gl.glTexCoord2f(0, 1); gl.glVertex3f(-size,  size, 0);
            gl.glEnd();
            
            gl.glPopMatrix();
        }
        
        gl.glDepthMask(true);
        gl.glDisable(GL2.GL_BLEND);
        gl.glEnable(GL2.GL_LIGHTING);
    }
    
    public int getCount() {
        return particles.size();
    }
    
    public void clear() {
        particles.clear();
    }
}
