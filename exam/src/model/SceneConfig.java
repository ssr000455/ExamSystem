package model;

import java.util.List;
import java.util.Map;

public class SceneConfig {
    public String sceneId;
    public String sceneName;
    public String description;
    public List<ModelDef> models;
    public EnvironmentConfig environment = new EnvironmentConfig();
    public CameraConfig camera = new CameraConfig();
    public List<InteractionPoint> interactions;
    public List<Waypoint> waypoints;
    public String arrowTexture;
    public double arrowSize = 0.5;
    public Map<String, Object> renderParams;
    
    public static class ModelDef {
        public String id;
        public String source;
        public String texture;
        public double[] color;
        public List<double[]> instances;
        public GridDef grid;
        public double[] position;
        public double[] rotation;
        public double[] scale = {1.0, 1.0, 1.0};
    }
    
    public static class GridDef {
        public double[] origin = {0, 0, 0};
        public int[] size = {1, 1, 1};
        public double spacing = 1.0;
    }
    
    public static class Waypoint {
        public double[] pos;
        public String label;
    }
    
    public static class CameraConfig {
        public double[] position = {8.0, 4.0, 8.0};
        public double[] target = {0.0, 0.0, 0.0};
        public double[] up = {0.0, 1.0, 0.0};
        public double fov = 60.0;
        public double near = 0.1;
        public double far = 100.0;
        public String mode = "orbit";
    }
    
    public static class EnvironmentConfig {
        public double[] backgroundColor = {0.2, 0.25, 0.35, 1.0};
        public String skybox;
        public boolean showGrid = true;
        public double gridSize = 20.0;
        public double gridSpacing = 1.0;
        public double[] gridColor = {0.5, 0.5, 0.5, 0.5};
        public double[] ambientLight = {0.3, 0.3, 0.3, 1.0};
        public List<LightConfig> lights;
    }
    
    public static class LightConfig {
        public String type = "directional";
        public double[] color = {1.0, 1.0, 1.0, 1.0};
        public double[] position = {5.0, 10.0, 5.0};
        public double[] direction = {-0.5, -1.0, -0.5};
        public double intensity = 1.0;
    }
    
    public static class InteractionPoint {
        public String id;
        public String name;
        public double[] position;
        public double radius = 0.5;
        public String type;
        public Map<String, Object> data;
    }
}
