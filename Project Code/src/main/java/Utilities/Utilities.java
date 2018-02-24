package Utilities;

import com.jogamp.opengl.util.GLBuffers;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by (User name) on 7/28/2017.
 */
public class Utilities {


    public static String fBufToString(FloatBuffer buf){
        if (buf.capacity() == 0)
            return "{}";
        String build = "{";
        String completion = "";
        for(int i=0;i<buf.capacity();i++){

            int temp = (int)((float)i/buf.capacity() * 100);
            String comp = temp + "%";
            if (!completion.equals(comp)){
                System.out.println(comp);
                completion = comp;
            }

            build += buf.get(i);
            build += ", ";
        }
        build = build.substring(0,build.length()-2);
        build += "}";
        return build;
    }
    public static String iBufToString(IntBuffer buf){
        if (buf.capacity() == 0)
            return "{}";
        String build = "{";
        for(int i=0;i<buf.capacity();i++){
            build += buf.get(i);
            build += ", ";
        }
        build = build.substring(0,build.length()-2);
        build += "}";
        return build;
    }
    public static String bBufToString(ByteBuffer buf){
        if (buf.capacity() == 0)
            return "{}";
        String build = "{";
        for(int i=0;i<buf.capacity();i++){
            build += buf.get(i);
            build += ", ";
        }
        build = build.substring(0,build.length()-2);
        build += "}";
        return build;
    }
    public static String bytesToString(ByteBuffer buf){
        String build = "";
        for(int i=0;i<buf.capacity();i++){
            build += (char)buf.get(i);
        }
        return build;
    }
    public static String arrayToString(int[] array){
        String build ="{";
        for (int i=0; i< array.length; i++){
            build += array[i];
            build += ", ";
        }
        build = build.substring(0, build.length() - 2);
        build += "}";
        return build;
    }
    public static FloatBuffer convertF(ArrayList<Float> list){
        float[] array = new float[list.size()];
        for(int i=0; i < list.size(); i++){
            array[i] = list.get(i);
        }
        FloatBuffer f = GLBuffers.newDirectFloatBuffer(array);
        return f;
    }
    public static IntBuffer convertI(ArrayList<Integer> list){
        int[] array = new int[list.size()];
        for(int i=0; i < list.size(); i++){
            array[i] = list.get(i);
        }
        IntBuffer i = GLBuffers.newDirectIntBuffer(array);
        return i;
    }
    public static String Matrix4fToString(Matrix4f m){
        FloatBuffer buf = GLBuffers.newDirectFloatBuffer(16);
        m.get(buf);
        if (buf.capacity() == 0)
            return "{}";
        String build = "\n{";
        for(int i=0;i<buf.capacity();i++){
            build += buf.get(i);
            build += ", ";
            if (i % 4 == 3 ){
                build += "\n";
            }
        }
        build = build.substring(0,build.length()-2);
        build += "}";
        return build;
    }

    public static Matrix4f changeDirection(Vector3f direction){
        float directionAngle = (float)Math.acos(direction.normalize().dot(new Vector3f(0,0,1).normalize()));
        if (direction.x < 0) directionAngle *= -1.0f;
        return new Matrix4f().rotate(directionAngle, new Vector3f(0,1,0));
    }

    public static Matrix4f setLocationAndDirectionManually(Vector3f location, Vector3f direction){
        Matrix4f m = new Matrix4f();
        float lx = location.x;
        float ly = location.y;
        float lz = location.z;

        m.m30(lx);
        m.m31(ly);
        m.m32(lz);

        float dx = direction.x;
        float dy = direction.y;
        float dz = direction.z;

        m.m00(dz);
        m.m02(-dx);
        m.m20(dx);
        m.m22(dz);

        return m;
    }

}
