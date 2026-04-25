package render;

import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class BBModelParser {
    
    public static class BBModel {
        public String name;
        public List<Element> elements = new ArrayList<>();
        public Map<String, TextureInfo> textures = new HashMap<>();
        public List<Animation> animations = new ArrayList<>();
    }
    
    public static class Element {
        public String name;
        public double[] from = new double[3];
        public double[] to = new double[3];
        public Map<String, Face> faces = new HashMap<>();
        public double[] rotation;
        public double[] pivot;
    }
    
    public static class Face {
        public double[] uv;
        public String texture;
        public int rotation;
    }
    
    public static class TextureInfo {
        public String source;
        public String path;
    }
    
    public static class Animation {
        public String name;
        public double length;
        public Map<String, List<Keyframe>> keyframes = new HashMap<>();
    }
    
    public static class Keyframe {
        public double time;
        public String channel;
        public Object value;
    }
    
    public static BBModel load(Path bblFile) throws IOException {
        String json = Files.readString(bblFile);
        return parse(json);
    }
    
    public static BBModel load(byte[] data) throws IOException {
        return parse(new String(data, "UTF-8"));
    }
    
    private static BBModel parse(String json) {
        BBModel model = new BBModel();
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        
        if (root.has("name")) {
            model.name = root.get("name").getAsString();
        }
        
        // 纹理
        if (root.has("textures")) {
            JsonArray texArray = root.getAsJsonArray("textures");
            for (JsonElement e : texArray) {
                JsonObject tex = e.getAsJsonObject();
                TextureInfo info = new TextureInfo();
                info.source = tex.get("source").getAsString();
                if (tex.has("path")) info.path = tex.get("path").getAsString();
                model.textures.put(info.source, info);
            }
        }
        
        // 元素
        if (root.has("elements")) {
            JsonArray elemArray = root.getAsJsonArray("elements");
            for (JsonElement e : elemArray) {
                JsonObject elem = e.getAsJsonObject();
                Element el = new Element();
                el.name = elem.has("name") ? elem.get("name").getAsString() : "";
                JsonArray from = elem.getAsJsonArray("from");
                for (int i = 0; i < 3; i++) el.from[i] = from.get(i).getAsDouble();
                JsonArray to = elem.getAsJsonArray("to");
                for (int i = 0; i < 3; i++) el.to[i] = to.get(i).getAsDouble();
                
                if (elem.has("rotation")) {
                    JsonArray rot = elem.getAsJsonArray("rotation");
                    el.rotation = new double[3];
                    for (int i = 0; i < 3; i++) el.rotation[i] = rot.get(i).getAsDouble();
                }
                if (elem.has("pivot")) {
                    JsonArray piv = elem.getAsJsonArray("pivot");
                    el.pivot = new double[3];
                    for (int i = 0; i < 3; i++) el.pivot[i] = piv.get(i).getAsDouble();
                }
                
                JsonObject facesObj = elem.getAsJsonObject("faces");
                for (Map.Entry<String, JsonElement> entry : facesObj.entrySet()) {
                    JsonObject faceObj = entry.getValue().getAsJsonObject();
                    Face face = new Face();
                    if (faceObj.has("uv")) {
                        JsonArray uvArr = faceObj.getAsJsonArray("uv");
                        face.uv = new double[4];
                        for (int i = 0; i < 4; i++) face.uv[i] = uvArr.get(i).getAsDouble();
                    }
                    if (faceObj.has("texture")) {
                        face.texture = faceObj.get("texture").getAsString();
                    }
                    if (faceObj.has("rotation")) {
                        face.rotation = faceObj.get("rotation").getAsInt();
                    }
                    el.faces.put(entry.getKey(), face);
                }
                model.elements.add(el);
            }
        }
        
        // 动画
        if (root.has("animations")) {
            JsonArray animArray = root.getAsJsonArray("animations");
            for (JsonElement e : animArray) {
                JsonObject anim = e.getAsJsonObject();
                Animation a = new Animation();
                a.name = anim.get("name").getAsString();
                a.length = anim.has("length") ? anim.get("length").getAsDouble() : 0;
                model.animations.add(a);
            }
        }
        
        return model;
    }
}
