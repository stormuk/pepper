//package com.storm.posh.plan.nodes;
//
//import com.storm.posh.plan.nodes.plannodes.DriveCollectionNode;
//import com.storm.posh.plan.nodes.plannodes.RouteElementNode;
//import com.storm.posh.plan.planelements.PlanElement;
//import javafx.scene.paint.Color;
//
//import java.util.*;
//
///**
// * <p>
// * Holds lists with all the nodes.
// * </p>
// *
// * @author :   Andreas Theodorou - www.recklesscoding.com
// * @version :   %G%
// */
//public class NodesHolder {
//
//    private NodesHolderHelper nodesHolderHelper;
//
//    private PlanElementNode graphParent = new PlanElementNode(null, Color.DODGERBLUE);
//
//    private RouteElementNode driveCollection;
//
//    private List<PlanElementNode> drives;
//
//    private Map<PlanElement, PlanElementNode> nodesMap; // <id,nodes>
//
//    private volatile List<PlanElementNode> allPlanElementNodes;
//    private List<PlanElementNode> addedPlanElementNodes;
//    private List<PlanElementNode> removedPlanElementNodes;
//
//    private List<NodesConnector> allNodesConnectors;
//    private List<NodesConnector> addedEdges;
//    private List<NodesConnector> removedEdges;
//    private PlanElement rootPlanElement;
//
//    public NodesHolder(PlanElement rootPlanElement) {
//        this.rootPlanElement = rootPlanElement;
//        // clear plan, create lists
//        clear();
//        nodesHolderHelper = new NodesHolderHelper(this);
//        nodesHolderHelper.populateNodesHolder();
//    }
//
//    public void refresh() {
//        clearLists();
//        nodesHolderHelper.populateNodesHolder();
//    }
//
//    public void clear() {
//        clearLists();
//    }
//
//    private void clearLists() {
//        driveCollection = new RouteElementNode(rootPlanElement);
//        drives = new ArrayList<>();
//
//        allPlanElementNodes = new ArrayList<>();
//        addedPlanElementNodes = new ArrayList<>();
//        removedPlanElementNodes = new ArrayList<>();
//
//        allNodesConnectors = new ArrayList<>();
//        addedEdges = new ArrayList<>();
//        removedEdges = new ArrayList<>();
//
//        nodesMap = new HashMap<>(); // <id,nodes>
//    }
//
//    public void clearAddedLists() {
//        addedPlanElementNodes.clear();
//        addedEdges.clear();
//    }
//
//    /**
//     * Remove the graphParent reference if it is set
//     *
//     * @param planElementNodeList
//     */
//    public void disconnectFromGraphParent(List<PlanElementNode> planElementNodeList) {
//        for (PlanElementNode planElementNode : planElementNodeList) {
//            graphParent.removeChild(planElementNode);
//        }
//    }
//
//    public void merge() {
//        // nodes
//        allPlanElementNodes.addAll(addedPlanElementNodes);
//        allPlanElementNodes.removeAll(removedPlanElementNodes);
//
//        addedPlanElementNodes.clear();
//        removedPlanElementNodes.clear();
//
//        // edges
//        allNodesConnectors.addAll(addedEdges);
//        allNodesConnectors.removeAll(removedEdges);
//
//        addedEdges.clear();
//        removedEdges.clear();
//    }
//
//    public List<PlanElementNode> getAddedPlanElementNodes() {
//        return addedPlanElementNodes;
//    }
//
//    public List<PlanElementNode> getRemovedPlanElementNodes() {
//        return removedPlanElementNodes;
//    }
//
//    public synchronized List<PlanElementNode> getAllPlanElementNodes() {
//        return allPlanElementNodes;
//    }
//
//    public List<PlanElementNode> getDrives() {
//        return drives;
//    }
//
//    public List<NodesConnector> getAddedEdges() {
//        return addedEdges;
//    }
//
//    public List<NodesConnector> getRemovedEdges() {
//        return removedEdges;
//    }
//
//    public List<NodesConnector> getAllNodesConnectors() {
//        return allNodesConnectors;
//    }
//
//    public RouteElementNode getDriveCollection() {
//        return driveCollection;
//    }
//
//    public Map<PlanElement, PlanElementNode> getNodesMap() {
//        return nodesMap;
//    }
//}