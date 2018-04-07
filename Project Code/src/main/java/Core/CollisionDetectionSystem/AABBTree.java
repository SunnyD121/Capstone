package Core.CollisionDetectionSystem;

import World.SceneEntity;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;

public class AABBTree {

    private HashMap<SceneEntity, Node> map;
    private ArrayList<Node> nodes;

    private Node root;
//    private int allocationSize;
    private Node nextFreeNode;
//    private int NodeCapacity;
    private int growthSize;


    public AABBTree(/*int initialSize */){
        this.root = null;
        this.nextFreeNode = null;
        map = new HashMap<>();
        nodes = new ArrayList<>();
        /*
        for (i=0; i < initialSize; i++){
            //fill ArrayList with Node instances
            //(Node instance).nextNodeIndex = i+1   //Linking the std::vector together
        }
        //set the Node instance at initialSize-1 = null
         */
    }

    public void insertObject(SceneEntity e){
        Node n = new Node();
        nodes.add(n);
        //TODO: fix this next statement so it is accurate.
        n.box = new FixedBoundingBox(new Vector3f(-2,-2,-2),new Vector3f(2,2,2), null);
        n.entity = e;

        insertLeaf(n);
        map.put(e, n);
    }
    public void removeObject(SceneEntity e){
        Node n = map.get(e);
        removeLeaf(n);
        map.remove(e);
    }
    public void updateObject(SceneEntity e){
        Node n = map.get(e);
        updateLeaf(n, new MovableBoundingBox(new Vector3f(-2,-2,-2), new Vector3f(2,2,2), null));
    }
    public Object queryOverlaps(SceneEntity e){/* algorithm */return null;}


//    private int allocateNode(){}
//    private void deallocateNode(){}
    private void insertLeaf(Node node){
        if (node.parent != null) error(".insertLeaf(): insertions must have null parent.");
        if (node.leftChild != null) error(".insertLeaf(): insertions must have null left child.");
        if (node.rightChild != null) error(".insertLeaf(): insertions must have null right child.");

        if (this.root == null){
            this.root = node;
            return;
        }

        //Find "best" Node to insert leaf.
        //...algorithm...
        //spits out leafSiblingNode
        Node leafSiblingNode = null;

        nodes.add(leafSiblingNode);
        Node oldParent = leafSiblingNode.parent;
        Node newParent = new Node();
        nodes.add(newParent);
        newParent.parent = oldParent;
    }
    private void removeLeaf(Node node){}
    private void updateLeaf(Node n, BoundingBox newBox){}
    private void fixUpwardsTree(Node treeNode){}



    private void error(String msg){
        System.err.println("CustomTree" + msg);
        System.exit(-1);
    }

    private class Node{
        BoundingBox box;
        SceneEntity entity;

        Node parent;
        Node leftChild;
        Node rightChild;
        Node nextNode;

        public Node(){
            box = null;
            entity = null;
            parent = null;
            leftChild = null;
            rightChild = null;
            nextNode = null;
        }

        private boolean isLeaf(){
            return (leftChild == null);
        }


    }

}
