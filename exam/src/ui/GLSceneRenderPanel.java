package ui;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;

import model.SceneConfig;
import data.SceneLoader;
import render.ScenePackageLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class GLSceneRenderPanel extends GLJPanel implements GLEventListener {
    
    private GLU glu;
    private FPSAnimator animator;
    private SceneConfig config;
    
    private double cameraDistance = 8.0;
    private double cameraAzimuth = 45.0;
    private double cameraElevation = 30.0;
    private double[] cameraTarget = {0.0, 0.0, 0.0};
    
    private Point lastMousePos;
    private boolean dragging = false;
    private boolean panning = false;
    
    private int gridDisplayList = -1;
    private int arrowDisplayList = -1;
    private Map<String, Boolean> interactionStates = new HashMap<>();
    private boolean showWireframe = false;
    
    public GLSceneRenderPanel() {
        super(new GLCapabilities(GLProfile.getDefault()));
        setPreferredSize(new Dimension(800, 600));
        addGLEventListener(this);
        animator = new FPSAnimator(this, 60);
        animator.start();
        setupMouseControls();
        setFocusable(true);
    }
    
    private void setupMouseControls() {
        MouseAdapter adapter = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                lastMousePos = e.getPoint();
                if (SwingUtilities.isLeftMouseButton(e)) dragging = true;
                else if (SwingUtilities.isRightMouseButton(e)) panning = true;
            }
            public void mouseDragged(MouseEvent e) {
                if (lastMousePos == null) return;
                int dx = e.getX() - lastMousePos.x;
                int dy = e.getY() - lastMousePos.y;
                if (dragging) {
                    cameraAzimuth += dx * 0.3;
                    cameraElevation = Math.max(-85, Math.min(85, cameraElevation - dy * 0.3));
                } else if (panning) {
                    double scale = cameraDistance * 0.003;
                    double forwardX = Math.sin(Math.toRadians(cameraAzimuth));
                    double forwardZ = Math.cos(Math.toRadians(cameraAzimuth));
                    double rightX = Math.cos(Math.toRadians(cameraAzimuth));
                    double rightZ = -Math.sin(Math.toRadians(cameraAzimuth));
                    cameraTarget[0] += (rightX * dx + forwardX * dy) * scale;
                    cameraTarget[2] += (rightZ * dx + forwardZ * dy) * scale;
                }
                lastMousePos = e.getPoint();
            }
            public void mouseReleased(MouseEvent e) { dragging = false; panning = false; }
            public void mouseWheelMoved(MouseWheelEvent e) {
                cameraDistance = Math.max(2.0, Math.min(30.0, cameraDistance + e.getWheelRotation() * 0.5));
            }
        };
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
        addMouseWheelListener(adapter);
    }
    
    public void loadScene(String scenePath) {
        new Thread(() -> {
            try {
                if (scenePath.endsWith(".zip")) {
                    ScenePackageLoader.ScenePackage pkg = ScenePackageLoader.loadPackage(scenePath);
                    config = pkg.config;
                } else {
                    SceneLoader.SceneContext ctx = SceneLoader.loadScene(scenePath);
                    config = ctx.config;
                }
                SwingUtilities.invokeLater(() -> {
                    interactionStates.clear();
                    if (config != null && config.interactions != null) {
                        for (SceneConfig.InteractionPoint ip : config.interactions) {
                            interactionStates.put(ip.id, false);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        glu = new GLU();
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);
        gl.glEnable(GL2.GL_COLOR_MATERIAL);
        gl.glEnable(GL2.GL_NORMALIZE);
        gl.glClearColor(0.2f, 0.25f, 0.35f, 1.0f);
        
        gridDisplayList = gl.glGenLists(2);
        arrowDisplayList = gridDisplayList + 1;
        
        gl.glNewList(gridDisplayList, GL2.GL_COMPILE);
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glColor4f(0.4f, 0.4f, 0.4f, 0.5f);
        gl.glBegin(GL2.GL_LINES);
        float size = 20.0f, spacing = 1.0f;
        int lines = (int)(size / spacing);
        for (int i = -lines/2; i <= lines/2; i++) {
            gl.glVertex3f(i * spacing, 0, -lines/2 * spacing);
            gl.glVertex3f(i * spacing, 0, lines/2 * spacing);
            gl.glVertex3f(-lines/2 * spacing, 0, i * spacing);
            gl.glVertex3f(lines/2 * spacing, 0, i * spacing);
        }
        gl.glEnd();
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEndList();
        
        gl.glNewList(arrowDisplayList, GL2.GL_COMPILE);
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glColor3f(1.0f, 0.8f, 0.0f);
        gl.glBegin(GL2.GL_TRIANGLES);
        gl.glVertex3f(0, 0.3f, 0);
        gl.glVertex3f(0.15f, -0.15f, 0);
        gl.glVertex3f(-0.15f, -0.15f, 0);
        gl.glEnd();
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEndList();
    }
    
    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        
        double camX = cameraTarget[0] + cameraDistance * Math.cos(Math.toRadians(cameraElevation)) * Math.sin(Math.toRadians(cameraAzimuth));
        double camY = cameraTarget[1] + cameraDistance * Math.sin(Math.toRadians(cameraElevation));
        double camZ = cameraTarget[2] + cameraDistance * Math.cos(Math.toRadians(cameraElevation)) * Math.cos(Math.toRadians(cameraAzimuth));
        glu.gluLookAt(camX, camY, camZ, cameraTarget[0], cameraTarget[1], cameraTarget[2], 0.0, 1.0, 0.0);
        
        float[] lightPos = {10.0f, 20.0f, 10.0f, 1.0f};
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPos, 0);
        gl.glCallList(gridDisplayList);
        
        if (config != null && config.models != null) renderModels(gl);
        if (config != null && config.waypoints != null) renderWaypoints(gl);
        if (config != null && config.interactions != null) renderInteractions(gl);
    }
    
    private void renderModels(GL2 gl) {
        for (SceneConfig.ModelDef def : config.models) {
            setMaterial(gl, def);
            if (showWireframe) gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
            
            java.util.List<double[]> positions = expandPositions(def);
            double[] scale = def.scale != null ? def.scale : new double[]{1, 1, 1};
            
            for (double[] pos : positions) {
                gl.glPushMatrix();
                gl.glTranslated(pos[0], pos[1], pos[2]);
                gl.glScaled(scale[0], scale[1], scale[2]);
                drawCube(gl);
                gl.glPopMatrix();
            }
            
            if (showWireframe) gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
        }
    }
    
    private void setMaterial(GL2 gl, SceneConfig.ModelDef def) {
        float[] ambient = {0.2f, 0.2f, 0.2f, 1.0f};
        float[] diffuse = {0.6f, 0.6f, 0.6f, 1.0f};
        if (def.id != null && def.id.contains("steve")) diffuse = new float[]{0.3f, 0.5f, 0.8f, 1.0f};
        else if (def.id != null && def.id.contains("goal")) diffuse = new float[]{0.2f, 0.8f, 0.2f, 1.0f};
        if (def.color != null && def.color.length >= 3) {
            diffuse = new float[]{(float)def.color[0], (float)def.color[1], (float)def.color[2], 1.0f};
        }
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, ambient, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, diffuse, 0);
    }
    
    private void drawCube(GL2 gl) {
        gl.glBegin(GL2.GL_QUADS);
        gl.glNormal3f(0, 0, 1);
        gl.glVertex3f(-0.5f, -0.5f, 0.5f); gl.glVertex3f(0.5f, -0.5f, 0.5f);
        gl.glVertex3f(0.5f, 0.5f, 0.5f); gl.glVertex3f(-0.5f, 0.5f, 0.5f);
        gl.glNormal3f(0, 0, -1);
        gl.glVertex3f(-0.5f, -0.5f, -0.5f); gl.glVertex3f(-0.5f, 0.5f, -0.5f);
        gl.glVertex3f(0.5f, 0.5f, -0.5f); gl.glVertex3f(0.5f, -0.5f, -0.5f);
        gl.glNormal3f(-1, 0, 0);
        gl.glVertex3f(-0.5f, -0.5f, -0.5f); gl.glVertex3f(-0.5f, -0.5f, 0.5f);
        gl.glVertex3f(-0.5f, 0.5f, 0.5f); gl.glVertex3f(-0.5f, 0.5f, -0.5f);
        gl.glNormal3f(1, 0, 0);
        gl.glVertex3f(0.5f, -0.5f, -0.5f); gl.glVertex3f(0.5f, 0.5f, -0.5f);
        gl.glVertex3f(0.5f, 0.5f, 0.5f); gl.glVertex3f(0.5f, -0.5f, 0.5f);
        gl.glNormal3f(0, 1, 0);
        gl.glVertex3f(-0.5f, 0.5f, -0.5f); gl.glVertex3f(-0.5f, 0.5f, 0.5f);
        gl.glVertex3f(0.5f, 0.5f, 0.5f); gl.glVertex3f(0.5f, 0.5f, -0.5f);
        gl.glNormal3f(0, -1, 0);
        gl.glVertex3f(-0.5f, -0.5f, -0.5f); gl.glVertex3f(0.5f, -0.5f, -0.5f);
        gl.glVertex3f(0.5f, -0.5f, 0.5f); gl.glVertex3f(-0.5f, -0.5f, 0.5f);
        gl.glEnd();
    }
    
    private java.util.List<double[]> expandPositions(SceneConfig.ModelDef def) {
        java.util.List<double[]> positions = new ArrayList<>();
        if (def.instances != null) positions.addAll(def.instances);
        if (def.grid != null) {
            for (int x = 0; x < def.grid.size[0]; x++)
                for (int y = 0; y < def.grid.size[1]; y++)
                    for (int z = 0; z < def.grid.size[2]; z++)
                        positions.add(new double[]{def.grid.origin[0] + x * def.grid.spacing,
                                                   def.grid.origin[1] + y * def.grid.spacing,
                                                   def.grid.origin[2] + z * def.grid.spacing});
        }
        if (positions.isEmpty() && def.position != null) positions.add(def.position);
        return positions;
    }
    
    private void renderWaypoints(GL2 gl) {
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glLineWidth(2.0f);
        gl.glColor3f(1.0f, 0.8f, 0.0f);
        gl.glBegin(GL2.GL_LINE_STRIP);
        for (SceneConfig.Waypoint wp : config.waypoints) gl.glVertex3d(wp.pos[0], wp.pos[1] + 0.1, wp.pos[2]);
        gl.glEnd();
        for (SceneConfig.Waypoint wp : config.waypoints) {
            gl.glPushMatrix();
            gl.glTranslated(wp.pos[0], wp.pos[1] + 0.5, wp.pos[2]);
            gl.glScaled(0.5, 0.5, 0.5);
            gl.glCallList(arrowDisplayList);
            gl.glPopMatrix();
        }
        gl.glEnable(GL2.GL_LIGHTING);
    }
    
    private void renderInteractions(GL2 gl) {
        gl.glDisable(GL2.GL_LIGHTING);
        for (SceneConfig.InteractionPoint ip : config.interactions) {
            boolean active = interactionStates.getOrDefault(ip.id, false);
            gl.glColor4f(active ? 0.0f : 0.8f, active ? 1.0f : 0.8f, active ? 0.0f : 0.8f, 0.5f);
            gl.glPushMatrix();
            gl.glTranslated(ip.position[0], ip.position[1], ip.position[2]);
            gl.glBegin(GL2.GL_TRIANGLE_FAN);
            for (int i = 0; i <= 16; i++) {
                double angle = 2 * Math.PI * i / 16;
                gl.glVertex3d(ip.radius * Math.cos(angle), ip.radius * Math.sin(angle), 0);
            }
            gl.glEnd();
            gl.glPopMatrix();
        }
        gl.glEnable(GL2.GL_LIGHTING);
    }
    
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(60.0, (double)width / height, 0.1, 100.0);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
    }
    
    @Override
    public void dispose(GLAutoDrawable drawable) {
        if (animator != null) animator.stop();
    }
    
    public void toggleWireframe() { showWireframe = !showWireframe; }
    public void triggerInteraction(String id) { if (interactionStates.containsKey(id)) interactionStates.put(id, true); }
    public boolean isInteractionTriggered(String id) { return interactionStates.getOrDefault(id, false); }
}
