package com.phylotree.model;

import java.util.ArrayList;
import java.util.List;

/**
 * One node in the phylogenetic tree.
 *  - LEAF  → has a name, no children.
 *  - INTERNAL → has children, name may be null.
 *
 * branchLength = distance from this node up to its parent.
 */
public class TreeNode {

    private String name;
    private double branchLength;
    private List<TreeNode> children = new ArrayList<>();

    public TreeNode(String name) { this.name = name; }

    public TreeNode(String name, double branchLength) {
        this.name = name;
        this.branchLength = branchLength;
    }

    public void addChild(TreeNode child) { children.add(child); }

    public String          getName()                       { return name; }
    public void            setName(String name)            { this.name = name; }
    public double          getBranchLength()               { return branchLength; }
    public void            setBranchLength(double b)       { this.branchLength = b; }
    public List<TreeNode>  getChildren()                   { return children; }
    public void            setChildren(List<TreeNode> c)   { this.children = c; }
    public boolean         isLeaf()                        { return children.isEmpty(); }
}
