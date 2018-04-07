package Core.CollisionDetectionSystem;

import Core.Shader;
import Utilities.BiMap;
import Utilities.Utilities;
import Utilities.BinaryTree;
import World.Particles.Laser;
import World.Particles.Particle;
import World.Player;
import World.Shapes.Building;
import World.Character;
import World.SceneEntity;

import World.Shapes.Ground;
import jogamp.graph.font.typecast.ot.Fixed;
import org.joml.Vector3f;

import java.util.ArrayList;



public class CollisionDetectionSystem {

    //need a datastructure to hold all object's collision boxes.
    //need a map to map the box to the object, or some way to link the two.
    //efficiency: categorizing into subsets the objects
    static BiMap<BoundingBox, SceneEntity> map;
    static BinaryTree<FixedBoundingBox> bTree;

    private static CollisionDetectionSystem singleton;

    private CollisionDetectionSystem(){
        map = new BiMap<BoundingBox, SceneEntity>();
    }

    public static CollisionDetectionSystem getInstance(){
        if (singleton == null) singleton = new CollisionDetectionSystem();
        return singleton;
    }

    public void addCollisionBox(SceneEntity object, Vector3f minPoint, Vector3f maxPoint, Vector3f transform){
        BoundingBox b;
        if (object instanceof Character || object instanceof Particle) b = new MovableBoundingBox(minPoint, maxPoint, transform);
        else b = new FixedBoundingBox(minPoint, maxPoint, transform);
        map.put(b, object);
    }

    public void addCollisionBox(SceneEntity object, Vector3f center, float length, float height, float width, Vector3f transform){
        Vector3f minPoint = new Vector3f(
                center.x - length / 2.0f,
                center.y - height / 2.0f,
                center.z - width /2.0f);
        Vector3f maxPoint = new Vector3f(
                center.x + length / 2.0f,
                center.y + height / 2.0f,
                center.z + width /2.0f);
        addCollisionBox(object, minPoint, maxPoint, transform);
    }

    public void removeCollisionBox(SceneEntity entity){
        map.removeValue(entity);    //note this may remove out of order
    }

    public static BoundingBox mergeMany(BoundingBox...group){
        float[] mins_x = new float[group.length];
        float[] maxes_x = new float[group.length];
        float[] mins_y = new float[group.length];
        float[] maxes_y = new float[group.length];
        float[] mins_z = new float[group.length];
        float[] maxes_z = new float[group.length];

        for (int i = 0; i < group.length; i++){
            mins_x[i] = group[i].minPoint.x;
            maxes_x[i] = group[i].maxPoint.x;
            mins_y[i] = group[i].minPoint.y;
            maxes_y[i] = group[i].maxPoint.y;
            mins_z[i] = group[i].minPoint.z;
            maxes_z[i] = group[i].maxPoint.z;
        }

        return new FixedBoundingBox(
                new Vector3f(Utilities.min(mins_x), Utilities.min(mins_y), Utilities.min(mins_z)),
                new Vector3f(Utilities.max(maxes_x), Utilities.max(maxes_y), Utilities.max(maxes_z)),
                null
        );
    }

    public <T extends SceneEntity> void formConglomerates(Class<T> classtype){
        ArrayList<SceneEntity> group = new ArrayList<>();
        for (SceneEntity e : map.valueSet()) {
            if (e.getClass() == classtype) group.add(e);
        }
        BoundingBox[] array = new BoundingBox[group.size()];
        for (int i = 0; i < array.length; i++) array[i] = map.getKeyFromValue(group.get(i));
        FixedBoundingBox box = (FixedBoundingBox)mergeMany(array);
        map.put(box, null);
    }

    public void postInit(){
        ArrayList<FixedBoundingBox> fixedBoxHierarchy = new ArrayList<>();
        ArrayList<ArrayList<FixedBoundingBox>> parentArray = new ArrayList<>();
        parentArray.add(fixedBoxHierarchy);
        for (BoundingBox box : map.keySet()){
            if (box instanceof FixedBoundingBox) fixedBoxHierarchy.add((FixedBoundingBox)box);
        }
        FixedBoundingBox[] tempArray = new FixedBoundingBox[fixedBoxHierarchy.size()];
        fixedBoxHierarchy.toArray(tempArray);
        bTree = new BinaryTree<FixedBoundingBox>((FixedBoundingBox)mergeMany(tempArray));
        recursiveBuildTree(bTree, fixedBoxHierarchy);
        /*
        //parentArray.remove(fixedBoxHierarchy); //handled in the Utilities function.
        parentArray = Utilities.split(parentArray, fixedBoxHierarchy);

        {
            //bTree lefft child and bTree rightChild = new BinaryTrees with root node the mergemany of
            for (ArrayList<FixedBoundingBox> list : parentArray){

            }
        }
        */

    }
    private void recursiveBuildTree(BinaryTree tree, ArrayList<FixedBoundingBox> boxes){
        if (boxes.size() == 1) return;
        else {
            Vector3f axisSplit = findSplitAxis(boxes);
            boxes = Utilities.sortByLocation(axisSplit, boxes);
            ArrayList<FixedBoundingBox> leftHalf = Utilities.splitByAxis(boxes, axisSplit, true);
            ArrayList<FixedBoundingBox> rightHalf = Utilities.splitByAxis(boxes, axisSplit, false);
            FixedBoundingBox[] temp1 = new FixedBoundingBox[leftHalf.size()];
            FixedBoundingBox[] temp2 = new FixedBoundingBox[rightHalf.size()];
            leftHalf.toArray(temp1);
            rightHalf.toArray(temp2);
            tree.leftChild = new BinaryTree<FixedBoundingBox>((FixedBoundingBox) mergeMany(temp1));
            tree.rightChild = new BinaryTree<FixedBoundingBox>((FixedBoundingBox) mergeMany(temp2));
            recursiveBuildTree(tree.leftChild, leftHalf);
            recursiveBuildTree(tree.rightChild, rightHalf);
        }
    }

    private <T extends BoundingBox> Vector3f findSplitAxis(ArrayList<T> things){
        float x_value = 0;
        float z_value = 0;
        float max_x = things.get(0).getCenterPoint().x;
        float max_z = things.get(0).getCenterPoint().z;
        //y_value purposefully omitted since most objects lie on the xz plane

        for (T thing : things){
            x_value += thing.getCenterPoint().x;
            z_value += thing.getCenterPoint().z;
            if (max_x < Math.abs(thing.getCenterPoint().x)) max_x = Math.abs(thing.getCenterPoint().x);
            if (max_z < Math.abs(thing.getCenterPoint().z)) max_z = Math.abs(thing.getCenterPoint().z);
        }
        x_value /= things.size();
        z_value /= things.size();

        Vector3f returnee = (max_x > max_z) ? new Vector3f(x_value, 0 , 0) : new Vector3f(0,0, z_value);
        return returnee;
    }

    public void testCollisions(){
        updateMovingHitBoxes();
        //TODO: Test for collision.
        for (BoundingBox box : map.keySet()){
            if (!(box instanceof MovableBoundingBox)){} //Fixed boxes won't collide by their own power.
            else{
                MovableBoundingBox mBox = ((MovableBoundingBox) box);   //cast
                for (BoundingBox box2 : map.keySet()) {  //TODO: here is where we optimize by organizing scene into hierarchies.
                    if (isColliding(mBox, box2)) {
                        SceneEntity entity1 = map.getValueFromKey(mBox);
                        SceneEntity entity2 = map.getValueFromKey(box2);
                        entity1.move(shortestDistanceOut(mBox.minPoint, mBox.maxPoint, box2.minPoint, box2.maxPoint));
                        if (entity1 instanceof Character) {
//                            ((Player) ((Character) entity)).killPlayer();
                            if (entity2 instanceof Ground) entity1.isOnGround = true;
                        }
                        else if (entity1 instanceof Laser) {
                            ;   //TODO
                        }
                        else {
                            System.err.println("Unhandled Collision Object Type.");
                            System.exit(-1);
                        }
                    }
                }
            }
        }
    }

    public void testCollisions2(){
        updateMovingHitBoxes();
        ArrayList<MovableBoundingBox> movableBoxes = new ArrayList<>();
        for (BoundingBox b : map.keySet()) if (b instanceof MovableBoundingBox) movableBoxes.add((MovableBoundingBox)b);
        for(MovableBoundingBox mBox : movableBoxes) {
            FixedBoundingBox box2 = recursiveTreeCollide(bTree, mBox);
            if (box2 == null) break; //no collision happened

            SceneEntity entity1 = map.getValueFromKey(mBox);
            SceneEntity entity2 = map.getValueFromKey(box2);
            System.out.println(entity1.getClass());
            System.out.println(entity2.getClass());
            //rewrite this algorithm for triangle to triangle (cylinder?) collision
            entity1.move(shortestDistanceOut(mBox.minPoint, mBox.maxPoint, box2.minPoint, box2.maxPoint));
            if (entity1 instanceof Character) {
//                            ((Player) ((Character) entity)).killPlayer();
                if (entity2 instanceof Ground) entity1.isOnGround = true;
            }
            else if (entity1 instanceof Laser) {
                ;   //TODO
            }
            else {
                System.err.println("Unhandled Collision Object Type.");
                System.exit(-1);
            }
        }
    }

    private FixedBoundingBox recursiveTreeCollide(BinaryTree<FixedBoundingBox> tree, MovableBoundingBox collidee){
        //base case
        if (tree.isLeaf()) return tree.getRoot();

        FixedBoundingBox returnee = null;
        if (isColliding(collidee, tree.getRoot())) {
            returnee = recursiveTreeCollide(tree.leftChild, collidee);
            if (returnee == null) returnee = recursiveTreeCollide(tree.rightChild, collidee);
        }
        return returnee;
    }

    /**
     *
     * @param mBox one Movable Axis Aligned Bounding Box
     * @param box one Generic Axis Aligned Bounding Box
     * @return Vector3f(-1) if not colliding, otherwise, a Vector3f containing a translation to un-collide
     */
    private boolean isColliding(MovableBoundingBox mBox, BoundingBox box){
        if (checkEquivalence(mBox, box)) return false;
        return (
                (mBox.minPoint.x <= box.maxPoint.x && mBox.maxPoint.x >= box.minPoint.x) &&
                (mBox.minPoint.y <= box.maxPoint.y && mBox.maxPoint.y >= box.minPoint.y) &&
                (mBox.minPoint.z <= box.maxPoint.z && mBox.maxPoint.z >= box.minPoint.z)
        );
    }

    private Vector3f shortestDistanceOut(Vector3f min1, Vector3f max1, Vector3f min2, Vector3f max2){
        float x1 = Utilities.dist(min1.x, max2.x);
        float x2 = Utilities.dist(max1.x, min2.x);
        boolean negX = (x1 < x2 ? min1.x > max2.x : max1.x > min2.x);

        float y1 = Utilities.dist(min1.y, max2.y);
        float y2 = Utilities.dist(max1.y, min2.y);
        boolean negY = (y1 < y2 ? min1.y > max2.y : max1.y > min2.y);

        float z1 = Utilities.dist(min1.z, max2.z);
        float z2 = Utilities.dist(max1.z, min2.z);
        boolean negZ = (z1 < z2 ? min1.z > max2.z : max1.z > min2.z);

        float shortX = (x1 < x2 ? x1 : x2);
        float shortY = (y1 < y2 ? y1 : y2);
        float shortZ = (z1 < z2 ? z1 : z2);

        float shortest = Utilities.min(shortX, shortY, shortZ);

        if (shortest == shortX){
            if (negX) shortX *= -1;
            return new Vector3f(shortX, 0,0);
        }

        else if (shortest == shortY) {
            if (negY) shortY *= -1;
            return new Vector3f(0, shortY, 0);
        }
        else if (shortest == shortZ) {
            if (negZ) shortZ *= -1;
            return new Vector3f(0,0,shortZ);
        }
        else {System.err.println("What the hell?!"); System.exit(-1); return new Vector3f(0);}   //Ha-Ha...code after a exit call.

    }

    private boolean checkEquivalence(MovableBoundingBox mBox, BoundingBox box){
        return map.getKeyIndex(mBox) == map.getKeyIndex(box);
    }

    private void updateMovingHitBoxes(){
        for (BoundingBox box : map.keySet()){
            if (box instanceof MovableBoundingBox)  ((MovableBoundingBox) box).setPosition(map.getValueFromKey(box).getPosition());
        }
    }

    boolean needsInit = true;
    ArrayList<Building> FixedHitBoxVisuals;
    ArrayList<Building> MovableHitBoxVisuals;
    public void drawBoundingBoxes(Shader shader){
//        method1(shader);
        method2(shader);
    }

    private void method1(Shader shader){
        if (needsInit) FixedHitBoxVisuals = new ArrayList<>();
        MovableHitBoxVisuals = new ArrayList<>();

        for(BoundingBox box : map.keySet()) {
            Building shape = null;
            if (box instanceof FixedBoundingBox && needsInit) {
                shape = createBoxObject(box);
                FixedHitBoxVisuals.add(shape);
            } else if (box instanceof MovableBoundingBox) {
                shape = createBoxObject(box);       //NOTE: careful, this calls init() every frame. BAAAAAAD.
                MovableHitBoxVisuals.add(shape);
            }
        }
        needsInit = false;
        for(Building b : FixedHitBoxVisuals) b.renderOnlyLines(shader, new Vector3f(1,1,0));
        for (Building b : MovableHitBoxVisuals) b.renderOnlyLines(shader, new Vector3f(1,0,1));
    }

    private void method2(Shader shader){
        if (needsInit) FixedHitBoxVisuals = new ArrayList<>();
        MovableHitBoxVisuals = new ArrayList<>();
        for (BoundingBox box : map.keySet()) {
            Building shape = null;
            if (box instanceof MovableBoundingBox){
                shape = createBoxObject(box);
                MovableHitBoxVisuals.add(shape);
            }
        }
        if (needsInit) {
            for (BoundingBox box : bTree.getTreeAsSetUnordered()) {
                Building shape = createBoxObject(box);
                FixedHitBoxVisuals.add(shape);
            }
        }
        needsInit = false;
        for (Building b : MovableHitBoxVisuals) b.renderOnlyLines(shader, new Vector3f(1,0,1));
        for (Building b : FixedHitBoxVisuals) b.renderOnlyLines(shader, new Vector3f(1,1,0));

    }

    private Building createBoxObject(BoundingBox box){
        Building shape = new Building();
        Vector3f[] data = box.getVertexData();
        Vector3f[] temp = new Vector3f[]{data[0], data[1], data[2], data[3]};
        float[] temp2 = new float[]{data[4].y, data[5].y, data[6].y, data[7].y};
        shape.setData(temp, temp2);
        shape.init();
        return shape;
    }


}
