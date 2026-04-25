package render;

import com.jogamp.opengl.*;
import java.io.*;
import java.nio.*;
import java.util.*;

public class ShaderManager {
    
    private Map<String, Integer> programs = new HashMap<>();
    private Map<String, Integer> uniforms = new HashMap<>();
    
    public int createProgram(GL2 gl, String vertexSource, String fragmentSource) {
        int vs = compileShader(gl, GL2.GL_VERTEX_SHADER, vertexSource);
        int fs = compileShader(gl, GL2.GL_FRAGMENT_SHADER, fragmentSource);
        
        int program = gl.glCreateProgram();
        gl.glAttachShader(program, vs);
        gl.glAttachShader(program, fs);
        gl.glLinkProgram(program);
        
        checkLinkStatus(gl, program);
        
        gl.glDeleteShader(vs);
        gl.glDeleteShader(fs);
        
        return program;
    }
    
    private int compileShader(GL2 gl, int type, String source) {
        int shader = gl.glCreateShader(type);
        gl.glShaderSource(shader, 1, new String[]{source}, null);
        gl.glCompileShader(shader);
        
        int[] compiled = new int[1];
        gl.glGetShaderiv(shader, GL2.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            int[] logLength = new int[1];
            gl.glGetShaderiv(shader, GL2.GL_INFO_LOG_LENGTH, logLength, 0);
            byte[] log = new byte[logLength[0]];
            gl.glGetShaderInfoLog(shader, logLength[0], null, 0, log, 0);
            System.err.println("Shader compile error: " + new String(log));
            gl.glDeleteShader(shader);
            return 0;
        }
        return shader;
    }
    
    private void checkLinkStatus(GL2 gl, int program) {
        int[] linked = new int[1];
        gl.glGetProgramiv(program, GL2.GL_LINK_STATUS, linked, 0);
        if (linked[0] == 0) {
            int[] logLength = new int[1];
            gl.glGetProgramiv(program, GL2.GL_INFO_LOG_LENGTH, logLength, 0);
            byte[] log = new byte[logLength[0]];
            gl.glGetProgramInfoLog(program, logLength[0], null, 0, log, 0);
            System.err.println("Program link error: " + new String(log));
        }
    }
    
    public static String getDefaultVertexShader() {
        return "#version 120\n" +
               "varying vec3 normal;\n" +
               "varying vec4 vertex;\n" +
               "void main() {\n" +
               "    vertex = gl_ModelViewMatrix * gl_Vertex;\n" +
               "    normal = normalize(gl_NormalMatrix * gl_Normal);\n" +
               "    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n" +
               "    gl_TexCoord[0] = gl_MultiTexCoord0;\n" +
               "}\n";
    }
    
    public static String getDefaultFragmentShader() {
        return "#version 120\n" +
               "varying vec3 normal;\n" +
               "varying vec4 vertex;\n" +
               "uniform sampler2D texture;\n" +
               "void main() {\n" +
               "    vec3 lightDir = normalize(vec3(gl_LightSource[0].position));\n" +
               "    vec3 n = normalize(normal);\n" +
               "    float diff = max(dot(n, lightDir), 0.0);\n" +
               "    vec4 texColor = texture2D(texture, gl_TexCoord[0].st);\n" +
               "    vec4 ambient = gl_FrontLightProduct[0].ambient;\n" +
               "    vec4 diffuse = gl_FrontLightProduct[0].diffuse * diff;\n" +
               "    gl_FragColor = texColor * (ambient + diffuse);\n" +
               "}\n";
    }
}
