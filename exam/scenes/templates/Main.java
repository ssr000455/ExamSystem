import java.util.*;

/**
 * 场景渲染主类
 * 由 ScenePackageLoader 动态编译加载
 */
public class Main {
    
    private Map<String, Object> context;
    private List<GameObject> objects = new ArrayList<>();
    private Camera camera;
    
    public static class GameObject {
        public String id;
        public String modelSource;
        public double[] position;
        public double[] rotation;
        public double[] scale;
        public String textureSource;
        public Map<String, Object> properties = new HashMap<>();
    }
    
    public static class Camera {
        public double[] position = {5.0, 3.0, 5.0};
        public double[] target = {0.0, 0.0, 0.0};
        public double[] up = {0.0, 1.0, 0.0};
    }
    
    /**
     * 初始化方法，由加载器调用
     */
    public void init(Map<String, Object> context) {
        this.context = context;
        this.camera = new Camera();
        
        // 从 context 读取配置
        if (context.containsKey("camera")) {
            Map<String, Object> camConfig = (Map<String, Object>) context.get("camera");
            // 应用相机配置
        }
        
        setupScene();
    }
    
    /**
     * 设置场景
     */
    private void setupScene() {
        // 添加史蒂夫模型（使用内置库）
        GameObject steve = new GameObject();
        steve.id = "steve";
        steve.modelSource = "builtin:steve.obj";
        steve.position = new double[]{0.0, 0.0, 0.0};
        steve.scale = new double[]{1.0, 1.0, 1.0};
        steve.textureSource = "builtin:steve.png";
        objects.add(steve);
        
        // 添加自定义模型
        if (context.containsKey("models")) {
            List<Map<String, Object>> models = (List<Map<String, Object>>) context.get("models");
            for (Map<String, Object> modelConfig : models) {
                GameObject obj = new GameObject();
                obj.id = (String) modelConfig.get("id");
                obj.modelSource = (String) modelConfig.get("source");
                obj.position = convertToDoubleArray(modelConfig.get("position"));
                obj.rotation = convertToDoubleArray(modelConfig.get("rotation"));
                obj.scale = convertToDoubleArray(modelConfig.get("scale"));
                obj.textureSource = (String) modelConfig.get("textureSource");
                objects.add(obj);
            }
        }
    }
    
    private double[] convertToDoubleArray(Object obj) {
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            double[] arr = new double[list.size()];
            for (int i = 0; i < list.size(); i++) {
                arr[i] = ((Number) list.get(i)).doubleValue();
            }
            return arr;
        }
        return new double[]{0.0, 0.0, 0.0};
    }
    
    /**
     * 每帧更新
     */
    public void update(float deltaTime) {
        // 动画更新逻辑
    }
    
    /**
     * 渲染场景
     */
    public void render() {
        // 渲染逻辑由宿主程序调用 OpenGL 执行
        // 这里只返回渲染数据
    }
    
    /**
     * 获取场景中的所有物体
     */
    public List<GameObject> getObjects() {
        return objects;
    }
    
    /**
     * 获取相机
     */
    public Camera getCamera() {
        return camera;
    }
    
    /**
     * 处理交互
     */
    public void onInteract(String interactionId, double[] hitPoint) {
        System.out.println("交互触发: " + interactionId);
        // 自定义交互逻辑
    }
    
    /**
     * 获取自定义数据
     */
    public Object getData(String key) {
        return context.get(key);
    }
}
