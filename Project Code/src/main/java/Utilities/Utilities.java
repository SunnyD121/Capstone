package Utilities;

import Core.CollisionDetectionSystem.BoundingBox;
import Core.CollisionDetectionSystem.FixedBoundingBox;
import com.jogamp.opengl.util.GLBuffers;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;

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
    public static <T> ArrayList<ArrayList<T>>  split(ArrayList<ArrayList<T>> parent, ArrayList<T> splitter){
        int index = parent.indexOf(splitter);
        if (index == -1) error("Utilities.split(): element was not contained in parent array.");
        int cutoffIndex = splitter.size()/2;    //integer division for truncation.
        ArrayList<T> leftHalf = new ArrayList<>();
        ArrayList<T> rightHalf = new ArrayList<>();

        for (int i = 0; i < cutoffIndex; i++){
            leftHalf.add(splitter.get(i));
            rightHalf.add(splitter.get(i+cutoffIndex));
        }
        if (splitter.size() % 2 == 1) rightHalf.add(splitter.get(splitter.size()-1));

        ArrayList<ArrayList<T>> temp = new ArrayList<>();
        for (int i = 0; i < index; i++) temp.add(parent.get(i));
        temp.add(leftHalf);
        temp.add(rightHalf);
        for (int i = index+1; i < parent.size(); i++) temp.add(parent.get(i));
        return temp;
    }

    public static <T extends BoundingBox> ArrayList<T> splitByAxis(ArrayList<T> list, Vector3f axis, boolean leftHalf){
        int splitIndex = list.size() / 2;   //this value *should* always be updated below.
        if (axis.z == 0) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getCenterPoint().x > axis.x){
                    splitIndex = i;
                    break;
                }
            }
        }
        else { //axis.x == 0
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getCenterPoint().z > axis.z){
                    splitIndex = i;
                    break;
                }
            }
        }
        if (leftHalf){
            ArrayList<T> subList = new ArrayList<>();
            for (int i = 0; i < splitIndex; i++) subList.add(list.get(i));
            return subList;
        }
        else {//(!leftHalf)
            ArrayList<T> subList = new ArrayList<>();
            for (int i = splitIndex; i < list.size(); i++) subList.add(list.get(i));
            return subList;
        }
    }

    public static <T extends BoundingBox> ArrayList<T> sortByLocation(Vector3f alignment, ArrayList<T> boxes) {
        if (alignment.z == 0) {
            boxes.sort(new Comparator<T>() {
                @Override
                public int compare(T o1, T o2) {
                    if (o1.getCenterPoint().x < o2.getCenterPoint().x) return -1;
                    else if (o1.getCenterPoint().x == o2.getCenterPoint().x) return 0;
                    else return 1;
                }
            });
        }
        else {//alignment.x == 0
            boxes.sort(new Comparator<T>() {
                @Override
                public int compare(T o1, T o2) {
                    if (o1.getCenterPoint().z < o2.getCenterPoint().z) return -1;
                    else if (o1.getCenterPoint().z == o2.getCenterPoint().z) return 0;
                    else return 1;
                }
            });
        }
        return boxes;
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
        float dy = direction.y;     //NOTE: dy isnt used, this function only works on the xz plane
        float dz = direction.z;

        m.m00(dz);
        m.m02(-dx);
        m.m20(dx);
        m.m22(dz);

        return m;
    }

    public static float dist(Vector3f p1, Vector3f p2){
        float x = (p1.x - p2.x) * (p1.x - p2.x);
        float y = (p1.y - p2.y) * (p1.y - p2.y);
        float z = (p1.z - p2.z) * (p1.z - p2.z);
        return (float)Math.sqrt(x + y + z);
    }

    public static float dist(float f1, float f2){
        return Math.abs(f1-f2);
    }

    public static float min(float a, float b, float c){
        if (a <= b && a <= c) return a;
        if (b < c) return b;
        else return c;
    }

    public static float min(float...floats){
        float min = floats[0];
        for (float f : floats) if (min > f) min = f;
        return min;
    }

    public static float max(float...floats){
        float max = floats[0];
        for(float f : floats) if (max < f) max = f;
        return max;
    }

    public static Matrix4f addDistance(Vector3f change, Matrix4f original){
        original.m30(original.m30() + change.x);
        original.m31(original.m31() + change.y);
        original.m32(original.m32() + change.z);
        return original;
    }

    public static Vector3f getPositionData(Matrix4f m){
        return new Vector3f(m.m30(), m.m31(), m.m32());
    }

    public static Vector3f rotateYInPlace(Vector3f location, float angle){
        Vector3f temp = new Vector3f(location);
        location.rotateY(angle);
        location.sub((location.x - temp.x), (location.y - temp.y), (location.z - temp.z));
        return location;
    }

    public static Vector3f findPerpendicular(Vector3f p1, Vector3f p2, Vector3f v){
        Vector3f u = new Vector3f();
        p1.sub(p2, u);

        Vector3f n = new Vector3f();
        u.cross(v, n);
        return n;
    }

    private static void error(String msg){
        System.err.println(msg);
        System.exit(-1);
    }

}
