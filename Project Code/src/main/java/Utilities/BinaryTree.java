package Utilities;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class BinaryTree<T> {

    Node<T> root;
    public BinaryTree<T> leftChild;
    public BinaryTree<T> rightChild;

    public BinaryTree(){
        root = null;
        leftChild = null;
        rightChild = null;
    }
    public BinaryTree(T rootData){
        root = new Node<T>();
        root.data = rootData;
        leftChild = null;
        rightChild = null;
    }

    public int height(BinaryTree tree){
        if (tree == null || tree.root == null) return 0;
        int leftHeight = height(tree.leftChild);
        int rightHeight = height(tree.rightChild);
        return (leftHeight > rightHeight) ? (leftHeight + 1) : (rightHeight + 1);
    }

    public void addLeaf(T data){
        if (leftChild == null) leftChild = new BinaryTree<T>(data);
        else if (rightChild == null) rightChild = new BinaryTree<T>(data);
        else if (height(leftChild) < height(rightChild)) leftChild.addLeaf(data);
        else rightChild.addLeaf(data);
    }

    public ArrayList<T> getTreeAsSetUnordered(){
        ArrayList<T> set = new ArrayList<>();
        set.add(root.data);
        set = recursiveIterate(this.leftChild, set);
        set = recursiveIterate(this.rightChild, set);
        return set;
    }

    private ArrayList<T> recursiveIterate(BinaryTree tree, ArrayList<T> set){
        if (tree == null) return set;
        set.add((T)tree.root.data);
        set = recursiveIterate(tree.leftChild, set);
        set = recursiveIterate(tree.rightChild, set);
        return set;
    }

    public boolean isLeaf(){
        if (leftChild == null && rightChild == null) return true;
        return false;
    }

    public T getRoot(){ return root.data;}

    private static class Node<T>{
        T data;
    }


}
