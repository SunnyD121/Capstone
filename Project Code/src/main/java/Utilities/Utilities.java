package Utilities;

import Core.CollisionDetectionSystem.BoundingBox;
import Core.CollisionDetectionSystem.FixedBoundingBox;
import World.Enemy;
import World.Player;
import World.SceneEntity;
import com.jogamp.opengl.util.GLBuffers;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import sun.plugin2.os.windows.FLASHWINFO;

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

    public static Matrix4f changeDirection(Player p, Vector3f direction){
        //find angle between z-axis and current direction (in xz plane?)
        float directionAngle = (float)Math.acos(direction.normalize().dot(new Vector3f(0,0,1).normalize()));
        //negate accordingly
        if (direction.x < 0) directionAngle *= -1.0f;
        //return matrix with data rotated by angle around y-axis
        Matrix4f temp = new Matrix4f().rotate(directionAngle, new Vector3f(0,1,0));
        return temp;
        //return new Matrix4f().rotate(directionAngle, new Vector3f(0,1,0));
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

    public static Vector3f lineIntersect(Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4){
        //what a mess. But, it should check out.
        //System.out.println("Testing Line Segment:");
        //System.out.println(p1 +" to "+p2 + " against");
        //System.out.println(p3 + " to "+p4);
        float denom1 = (p2.y*p4.x - p2.y*p3.x - p1.y*p4.x + p1.y*p3.x - p2.x*p4.y + p2.x*p3.y + p1.x*p4.y - p1.x*p3.y);
        float denom2 = (p2.z*p4.y - p2.z*p3.y - p1.z*p4.y + p1.z*p3.y - p2.y*p4.z + p2.y*p3.z + p1.y*p4.z - p1.y*p3.z);
        float denom3 = (p2.z*p4.x - p2.z*p3.x - p1.z*p4.x + p1.z*p3.x - p2.x*p4.z + p2.x*p3.z + p1.x*p4.z - p1.x*p3.z);
        if (denom1 == 0 && denom2 == 0 && denom3 == 0) {
            if (collinearCheck(p1, p2, p3, p4))
                return new Vector3f(1.0f / 0.0f, 1.0f / 0.0f, 1.0f / 0.0f);   //collinear, intersect is a set of points (not necessarily infinite)
            else return null;   //simply parallel.
        }
        float t1 = (p2.x*p3.y - p2.x*p1.y - p1.x*p3.y + p1.x*p1.y - p2.y*p3.x + p2.y*p1.x + p1.y*p3.x - p1.y*p1.x) / denom1;
        float t2 = (p2.y*p3.z - p2.y*p1.z - p1.y*p3.z + p1.y*p1.z - p2.z*p3.y + p2.z*p1.y + p1.z*p3.y - p1.z*p1.y) / denom2;
        float t3 = (p2.x*p3.z - p2.x*p1.z - p1.x*p3.z + p1.x*p1.z - p2.z*p3.x + p2.z*p1.x + p1.z*p3.x - p1.z*p1.x) / denom3;

        //System.out.println("t1: "+t1);
        //System.out.println("t2: "+t2);
        //System.out.println("t3: "+t3);

        float t = Float.NaN;

        //Check: 0 <= t1==t2==t3 <= 1 and by extension t1,t2,t3 is real
        if (Float.isFinite(t1)){    //t1 finite, t1,t2 ?
            t = t1;
            if (Float.isFinite(t2) && Float.isFinite(t3)){  //t1,t2,t3 finite
                if (t1 == t2 && t1 == t3){
                    if (t1 > 1 || t1 < 0) return null;    //since equal, checking t1 for if inbounds, checks for all
                }
                else return null;
            }
            else if (Float.isFinite(t2)){   //t1,t2 finite, t3 nonfinite
                if (t1 == t2){
                    if (t1 > 1 || t1 < 0) return null;
                }
                else return null;
            }
            else if (Float.isFinite(t3)){  //t1,t3 finite, t2 nonfinite
                if (t1 == t3){
                    if (t1 > 1 || t1 < 0) return null;
                }
                else return null;
            }
            else {  //t1 finite, t2,t3 nonfinite
                if (t1 > 1 || t1 < 0) return null;
            }
        }
        else if (Float.isFinite(t2)){   //t1 nonfinite, t2 finite, t3 ?
            t = t2;
            if (Float.isFinite(t3)){    //t2,t3 finite, t1 nonfinite
                if (t2 == t3){
                    if (t2 > 1 || t2 < 0) return null;
                }
                else return null;
            }
            else {  //t2 finite, t1,t3 nonfinite
                if (t2 > 1 || t2 < 0) return null;
            }
        }
        else if (Float.isFinite(t3)){   //t3 finite, t1,t2 nonfinite
            t = t3;
            if (t3 > 1 || t3 < 0) return null;
        }
        else {  //t1,t2,t3 nonfinite
            return null;
        }



        float s = ( p3.x-p1.x + t*(p4.x-p3.x) ) / (p2.x - p1.x);
        if (s > 1 || s < 0) return null;

        float s2 = ( p3.y-p1.y + t*(p4.y-p3.y) ) / (p2.y - p1.y);
        float s3 = ( p3.z-p1.z + t*(p4.z-p3.z) ) / (p2.z - p1.z);

        //System.out.println("s: "+s);
        //System.out.println("s2: "+s2);
        //System.out.println("s3: "+s3);


        Vector3f temp = new Vector3f(); p2.sub(p1, temp);
        temp.mul(s, temp);
        temp.add(p1,temp);
        //System.out.println("Intersection at: " + temp);
        return temp;
    }

    private static boolean collinearCheck(Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4){
        //check if point 3 is collinear
        float scalarX3 = (p3.x-p1.x) / (p2.x-p1.x);
        float scalarY3 = (p3.y-p1.y) / (p2.y-p1.y);
        float scalarZ3 = (p3.z-p1.z) / (p2.z-p1.z);

        if (!Float.isFinite(scalarX3)){
            if (Float.isFinite(scalarY3) && Float.isFinite(scalarZ3)) if (scalarY3 != scalarZ3) return false;
        }
        else if (!Float.isFinite(scalarY3)){
            if (Float.isFinite(scalarZ3)) if (scalarX3 != scalarZ3) return false;
        }
        else if (!Float.isFinite(scalarZ3)){
            if (scalarX3 != scalarY3) return false;
        }
        if (Float.isFinite(scalarX3) && Float.isFinite(scalarY3) && Float.isFinite(scalarZ3))
            if (!((scalarX3 == scalarY3) && (scalarX3 == scalarZ3) && (scalarY3 == scalarZ3))) return false;

        //check if point 4 is also collinear
        float scalarX4 = (p4.x-p1.x) / (p2.x-p1.x);
        float scalarY4 = (p4.y-p1.y) / (p2.y-p1.y);
        float scalarZ4 = (p4.z-p1.z) / (p2.z-p1.z);

        if (!Float.isFinite(scalarX4)){
            if (Float.isFinite(scalarY4) && Float.isFinite(scalarZ4)) if (scalarY4 != scalarZ4) return false;
        }
        else if (!Float.isFinite(scalarY4)){
            if (Float.isFinite(scalarZ4)) if (scalarX4 != scalarZ4) return false;
        }
        else if (!Float.isFinite(scalarZ4)){
            if (scalarX4 != scalarY4) return false;
        }
        if (Float.isFinite(scalarX4) && Float.isFinite(scalarY4) && Float.isFinite(scalarZ4))
            if (!((scalarX4 == scalarY4) && (scalarX4 == scalarZ4) && (scalarY4 == scalarZ4))) return false;

        return true;
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

    //Separating Axis Theorem
    public static boolean boxLineIntersection(BoundingBox box, Vector3f A, Vector3f B){
        Vector3f d = new Vector3f(); B.sub(A, d); d.mul(0.5f);      //half the distance between A and B
        Vector3f e = new Vector3f(); box.getMaxPoint().sub(box.getMinPoint(), e); e.mul(0.5f);      //distance from corner to center of box
        Vector3f c = new Vector3f();
        Vector3f temp = new Vector3f();
        Vector3f temp2 = new Vector3f();
        A.add(d, temp);     //midpoint of AB
        box.getMinPoint().add(box.getMaxPoint(), temp2); temp2.mul(0.5f);   //center of box
        temp.sub(temp2, c);     //midpoint of AB - centerpoint
        Vector3f ad = makePositive(d);

        if (Math.abs(c.x) > (e.x + ad.x)) return false;
        if (Math.abs(c.y) > (e.y + ad.y)) return false;
        if (Math.abs(c.z) > (e.z + ad.z)) return false;

        float epsilon = 0f;
        if(Math.abs(d.y * c.z - d.z * c.y) > e.y * ad.z + e.z * ad.y + epsilon) return false;
        if(Math.abs(d.z * c.x - d.x * c.z) > e.z * ad.x + e.x * ad.z + epsilon) return false;
        if(Math.abs(d.x * c.y - d.y * c.x) > e.x * ad.y + e.y * ad.x + epsilon) return false;
        return true;
    }


    private static Vector3f makePositive(Vector3f v){
        return new Vector3f((float)Math.abs(v.x),(float)Math.abs(v.y),(float)Math.abs(v.z));
    }

    private static float findValidNumber(float...nums){
        for (float num : nums) if (Float.isFinite(num)) return num;
        return Float.NaN;
    }

}
