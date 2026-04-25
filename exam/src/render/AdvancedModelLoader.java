package render;

import com.jogamp.opengl.*;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import java.io.*;
import java.nio.*;
import java.util.*;

public class AdvancedModelLoader {
    
    public static class ModelData {
        public List<float[]> vertices = new ArrayList<>();
        public List<float[]> normals = new ArrayList<>();
        public List<float[]> texCoords = new ArrayList<>();
        public List<Face> faces = new ArrayList<>();
        public Map<String, Material> materials = new HashMap<>();
        public String currentMaterial;
        
        // VBO 数据
        public FloatBuffer vertexBuffer;
        public FloatBuffer normalBuffer;
        public FloatBuffer texCoordBuffer;
        public int vertexCount;
    }
    
    public static class Face {
        public int[] vertexIndices;
        public int[] normalIndices;
        public int[] texCoordIndices;
        public String material;
        
        public Face(int vertexCount) {
            vertexIndices = new int[vertexCount];
            normalIndices = new int[vertexCount];
            texCoordIndices = new int[vertexCount];
        }
    }
    
    public static class Material {
        public String name;
        public float[] ambient = {0.2f, 0.2f, 0.2f, 1.0f};
        public float[] diffuse = {0.8f, 0.8f, 0.8f, 1.0f};
        public float[] specular = {0.0f, 0.0f, 0.0f, 1.0f};
        public float shininess = 0.0f;
        public String diffuseTexture;
        public transient Texture texture;
    }
    
    public static ModelData loadOBJ(InputStream is) throws IOException {
        ModelData data = new ModelData();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            
            String[] parts = line.split("\\s+");
            switch (parts[0]) {
                case "v":
                    data.vertices.add(new float[]{
                        Float.parseFloat(parts[1]),
                        Float.parseFloat(parts[2]),
                        Float.parseFloat(parts[3])
                    });
                    break;
                case "vn":
                    data.normals.add(new float[]{
                        Float.parseFloat(parts[1]),
                        Float.parseFloat(parts[2]),
                        Float.parseFloat(parts[3])
                    });
                    break;
                case "vt":
                    data.texCoords.add(new float[]{
                        Float.parseFloat(parts[1]),
                        parts.length > 2 ? Float.parseFloat(parts[2]) : 0.0f
                    });
                    break;
                case "f":
                    Face face = new Face(parts.length - 1);
                    for (int i = 1; i < parts.length; i++) {
                        String[] indices = parts[i].split("/");
                        face.vertexIndices[i-1] = indices[0].isEmpty() ? -1 : Integer.parseInt(indices[0]) - 1;
                        face.texCoordIndices[i-1] = (indices.length > 1 && !indices[1].isEmpty()) ? Integer.parseInt(indices[1]) - 1 : -1;
                        face.normalIndices[i-1] = (indices.length > 2 && !indices[2].isEmpty()) ? Integer.parseInt(indices[2]) - 1 : -1;
                    }
                    face.material = data.currentMaterial;
                    data.faces.add(face);
                    break;
                case "usemtl":
                    data.currentMaterial = parts[1];
                    break;
                case "mtllib":
                    // 材质库文件，需要额外处理
                    break;
            }
        }
        reader.close();
        
        buildVBO(data);
        return data;
    }
    
    private static void buildVBO(ModelData data) {
        List<Float> vertices = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Float> texCoords = new ArrayList<>();
        
        for (Face face : data.faces) {
            for (int i = 0; i < face.vertexIndices.length; i++) {
                if (face.vertexIndices[i] >= 0 && face.vertexIndices[i] < data.vertices.size()) {
                    float[] v = data.vertices.get(face.vertexIndices[i]);
                    vertices.add(v[0]); vertices.add(v[1]); vertices.add(v[2]);
                }
                if (face.normalIndices[i] >= 0 && face.normalIndices[i] < data.normals.size()) {
                    float[] n = data.normals.get(face.normalIndices[i]);
                    normals.add(n[0]); normals.add(n[1]); normals.add(n[2]);
                }
                if (face.texCoordIndices[i] >= 0 && face.texCoordIndices[i] < data.texCoords.size()) {
                    float[] t = data.texCoords.get(face.texCoordIndices[i]);
                    texCoords.add(t[0]); texCoords.add(t[1]);
                }
            }
        }
        
        data.vertexCount = vertices.size() / 3;
        data.vertexBuffer = FloatBuffer.wrap(toFloatArray(vertices));
        data.normalBuffer = FloatBuffer.wrap(toFloatArray(normals));
        data.texCoordBuffer = FloatBuffer.wrap(toFloatArray(texCoords));
    }
    
    private static float[] toFloatArray(List<Float> list) {
        float[] arr = new float[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
    }
    
    public static int createDisplayList(GL2 gl, ModelData data) {
        int list = gl.glGenLists(1);
        gl.glNewList(list, GL2.GL_COMPILE);
        
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
        gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        
        gl.glVertexPointer(3, GL2.GL_FLOAT, 0, data.vertexBuffer);
        gl.glNormalPointer(GL2.GL_FLOAT, 0, data.normalBuffer);
        gl.glTexCoordPointer(2, GL2.GL_FLOAT, 0, data.texCoordBuffer);
        
        gl.glDrawArrays(GL2.GL_TRIANGLES, 0, data.vertexCount);
        
        gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
        gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        
        gl.glEndList();
        return list;
    }
}
