package com.kingpivot.common.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TreeInfo implements Serializable, Comparator<TreeInfo> {

    private String id;

    private String pId;

    private String name;

    private String smallIcon;

    private String largeIcon;

    private Integer depth;

    private Integer orderSeq;

    private Integer isLeaf;//0-parent,1-child

    private List nodes = new ArrayList();

    public TreeInfo() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSmallIcon() {
        return smallIcon;
    }

    public void setSmallIcon(String smallIcon) {
        this.smallIcon = smallIcon;
    }

    public String getLargeIcon() {
        return largeIcon;
    }

    public void setLargeIcon(String largeIcon) {
        this.largeIcon = largeIcon;
    }

    public Integer getDepth() {
        return depth;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    public Integer getOrderSeq() {
        return orderSeq;
    }

    public void setOrderSeq(Integer orderSeq) {
        this.orderSeq = orderSeq;
    }

    public Integer getIsLeaf() {
        return isLeaf;
    }

    public void setIsLeaf(Integer isLeaf) {
        this.isLeaf = isLeaf;
    }

    public List getNodes() {
        return nodes;
    }

    public void setNodes(List nodes) {
        this.nodes = nodes;
    }

    @Override
    public int compare(TreeInfo o1, TreeInfo o2) {
        return o1.getOrderSeq().compareTo(o2.getOrderSeq());
    }

}
