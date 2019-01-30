//package com.storm.abod3;
//
//import android.content.Context;
//import android.graphics.Color;
//import android.support.constraint.ConstraintLayout;
//import android.view.View;
//
//import com.storm.posh.plan.planelements.action.ActionEvent;
//import com.storm.posh.plan.planelements.action.ActionPattern;
//import com.storm.posh.plan.planelements.competence.Competence;
//import com.storm.posh.plan.planelements.competence.CompetenceElement;
//import com.storm.posh.plan.planelements.drives.DriveCollection;
//
//import java.util.ArrayList;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Stack;
//
//import georegression.struct.point.Point2D_F64;
//
//public class UIPlanTree {
//
//    private final ConstraintLayout overlayLayout;
//    public Node<ARPlanElement> root;
//    private Node<ARPlanElement> focusedNode = null;
//    private Stack<Node<ARPlanElement>> historyNodes = new Stack<>();
//    private int nodeWidthSeperation = 24;
//    private int nodeHeightSeperation = 16;
//    private int flashColor = Color.CYAN;
//    private int backgroundColor = Color.parseColor("#2f4f4f");
//    private ArrayList<Node<ARPlanElement>> allVisibleNodes = new ArrayList<Node<ARPlanElement>>();
//
//    public UIPlanTree(List<DriveCollection> driveCollections, Context context, ConstraintLayout overlayLayout) {
//
//        this.root = new Node<>(new ARPlanElement(context, 0,"Drives", Color.YELLOW));
//        this.overlayLayout = overlayLayout;
//
//        createNodes(root,driveCollections,context);
//    }
//
//    public Node<ARPlanElement> getRoot() {
//        return root;
//    }
//
//    public void setUpTreeRender(UIPlanTree.Node<ARPlanElement> node, int widthAppender, int heightAppender, Point2D_F64 viewCenter) {
//
//        if(focusedNode.getParent() != null) {
//            focusedNode.getParent().getData().getView().setX((float) (viewCenter.x - focusedNode.getParent().getData().getView().getWidth()) - 40);
//            focusedNode.getParent().getData().getView().setY((float) (viewCenter.y - focusedNode.getParent().getData().getView().getHeight()) - 40);
//        }
//
//        if(node.getParent() == null ){
//
//            node.getData().getView().setX((float) ( viewCenter.x + widthAppender  ));
//            node.getData().getView().setY((float) ( viewCenter.y + heightAppender ));
//
//            widthAppender = widthAppender + node.getData().getView().getWidth() + nodeWidthSeperation;
//
//            int childrenTotalHeight = 0;
//
//            for (int i = 0; i < node.getChildren().size(); i++){
//                childrenTotalHeight += node.getChildren().get(i).getData().getView().getHeight();
//            }
//
//            int heightOffset = 0;
//
//            if(node.getChildren().size() == 1){
//                heightOffset = 0;
//            }else if(node.getChildren().size() % 2 == 0){
//                heightOffset = childrenTotalHeight/2;
//            }else if(node.getChildren().size() % 2 != 0){
//                heightOffset = childrenTotalHeight/3;
//            }
//
//            for (int i = 0; i < node.getChildren().size(); i++){
//                setUpTreeRender(node.getChildren().get(i), widthAppender, heightAppender - heightOffset,viewCenter);
//                heightAppender = heightAppender + node.getData().getView().getHeight() + nodeHeightSeperation;
//            }
//        }else{
//
//            node.getData().getView().setX((float) (viewCenter.x + widthAppender));
//            node.getData().getView().setY((float) (viewCenter.y + heightAppender));
//
//            widthAppender = widthAppender + node.getData().getView().getWidth() + nodeWidthSeperation;
//
//            int childrenTotalHeight = 0;
//
//            for (int i = 0; i < node.getChildren().size(); i++) {
//                childrenTotalHeight += node.getChildren().get(i).getData().getView().getHeight();
//            }
//
//            int heightOffset = 0;
//
//            if (node.getChildren().size() == 1) {
//                heightOffset = 0;
//            } else if (node.getChildren().size() % 2 == 0) {
//                heightOffset = (int) (childrenTotalHeight / 2.6);
//            } else if (node.getChildren().size() % 2 != 0) {
//                heightOffset = childrenTotalHeight / 3;
//            }
//
//            for (int i = 0; i < node.getChildren().size(); i++) {
//                setUpTreeRender(node.getChildren().get(i), widthAppender, heightAppender - heightOffset, viewCenter);
//                heightAppender = heightAppender + node.getData().getView().getHeight() + nodeHeightSeperation;
//            }
//        }
//
//    }
//
//
//    public void removeNodesFromUI(ConstraintLayout rootLayout, Node<ARPlanElement> node) {
//        rootLayout.removeView(node.getData().getView());
//        for (Node it : node.getChildren()) {
//            removeNodesFromUI(rootLayout, it);
//        }
////        node.getChildren().forEach(it -> removeNodesFromUI(rootLayout, it));
//    }
//
//    public void hideNodes(Node<ARPlanElement> node) {
//        node.getData().getView().setVisibility(View.INVISIBLE);
//        for (Node it : node.getChildren()) {
//            hideNodes(it);
//        }
////        node.getChildren().forEach(it -> hideNodes(it));
//    }
//
//    public void updateNodesVisuals(String planElementName) { //focusedNode
//
//        for(Node<ARPlanElement> visibleNode : allVisibleNodes ){
//
//            if(visibleNode.getData().getName().equals(planElementName)){
//                visibleNode.getData().setBackgroundColor(flashColor);
//            }
//
//        }
//
//    }
//
//    public void setAllVisibleNodes() {
//
//        System.out.println("setAllVisibleNodes called!");
//
//        allVisibleNodes.clear();
//
//        if(getFocusedNode().getParent() != null) {
//            allVisibleNodes.add(getFocusedNode().getParent());
//        }
//
//        allVisibleNodes.add(getFocusedNode());
//
//        for(Node<ARPlanElement> child : getFocusedNode().getChildren()){
//            allVisibleNodes.add(child);
//        }
//    }
//
//    public void createNodes(Node<ARPlanElement> node, Object obj, Context context) {
//
//        node.getData().getView().setOnClickListener(new UIPlanTreeNodeTouchListener(this,node));
//
//        if(obj instanceof ActionEvent){
//
//            Node<ARPlanElement> child = new Node<>(new ARPlanElement(context, 0, ((ActionEvent) obj).getNameOfElement(), Color.MAGENTA));
//            node.addChild(child);
//
//        }
//
//        if(obj instanceof ActionPattern){
//
//            Node<ARPlanElement> child = new Node<>(new ARPlanElement(context, 0, ((ActionPattern) obj).getNameOfElement(), Color.GREEN));
//            node.addChild(child);
//
//            createNodes(child, ((ActionPattern) obj).getActionEvents(),context);
//        }
//
//        if(obj instanceof Competence){
//
//            Node<ARPlanElement> child = new Node<>(new ARPlanElement(context, 0, ((Competence) obj).getNameOfElement(), Color.CYAN));
//            node.addChild(child);
//
//            createNodes(child, ((Competence) obj).getCompetenceElements(),context);
//        }
//
//        if(obj == null){
//            return;
//        }
//
//        if (obj instanceof LinkedList) {
//
//            for (int i = 0; i < ((LinkedList) obj).size(); i++) {
//
//                if(((LinkedList) obj).get(i) instanceof DriveCollection){
//
//                    DriveCollection driveCollection = (DriveCollection) ((LinkedList) obj).get(i);
//                    Node<ARPlanElement> child = new Node<>(new ARPlanElement(context, i + 1, driveCollection.getNameOfElement(), Color.RED));
//                    node.addChild(child);
//
//                    if(driveCollection.getTriggeredElement() != null) {
//                        createNodes(child, driveCollection.getTriggeredElement(),context);
//                    }else{
//                        createNodes(child, null, context);
//                    }
//                }
//
//                if(((LinkedList) obj).get(i) instanceof ActionEvent){
//
//                    ActionEvent actionEvent = (ActionEvent) ((LinkedList) obj).get(i);
//                    Node<ARPlanElement> child = new Node<>(new ARPlanElement(context, 0, actionEvent.getNameOfElement(), Color.YELLOW));
//                    node.addChild(child);
//
//                }
//
//                if(((LinkedList) obj).get(i) instanceof CompetenceElement){
//
//                    CompetenceElement competenceElement = (CompetenceElement) ((LinkedList) obj).get(i);
//                    Node<ARPlanElement> child = new Node<>(new ARPlanElement(context, 0, competenceElement.getNameOfElement(), Color.BLACK));
//                    node.addChild(child);
//
//                    if(competenceElement.getTriggeredElement() != null) {
//                        createNodes(child, competenceElement.getTriggeredElement(),context);
//                    }
//                }
//            }
//        }
//
//    }
//
//    public void setDefaultBackgroundColorNodes() {
//
//        for(Node<ARPlanElement> visibleNode : allVisibleNodes ){
//            visibleNode.getData().setBackgroundColor(backgroundColor);
//        }
//    }
//
//    public Node<ARPlanElement> getFocusedNode() {
//        return focusedNode;
//    }
//
//    public void setFocusedNode(Node<ARPlanElement> focusedNode) {
//
//        if(focusedNode.getParent() != null) {
//            focusedNode.getParent().getData().getView().setVisibility(View.VISIBLE);
//        }
//
//        hideNodeSiblings(focusedNode);
//        showNodeChildren(focusedNode);
//
//        this.focusedNode = focusedNode;
//    }
//
//    public void setUpTree(int startingXPoint, int startingYPoint, Point2D_F64 viewCenter) {
//        this.setUpTreeRender(this.getFocusedNode(),startingXPoint,startingYPoint,viewCenter);
//    }
//
//    private void showNodeChildren(Node<ARPlanElement> node) {
//
//        hideNodes(node);
//
//        node.getData().getView().setVisibility(View.VISIBLE);
//
//        for(Node<ARPlanElement> child : node.getChildren()){
//            child.getData().getView().setVisibility(View.VISIBLE);
//        }
//
//    }
//
//    private void hideNodeSiblings(Node<ARPlanElement> node) {
//
//        if(node.getParent() != null) {
//            for (int i = 0; i < node.getParent().getChildren().size(); i++) {
//                if(!node.getParent().getChildren().get(i).getData().getName().equals(node.getData().getName())){
//                    hideNodes(node.getParent().getChildren().get(i));
//                }
//            }
//        }
//    }
//
//    public void addNodesToUI(Node<ARPlanElement> node) {
//
//        node.getData().getView().setVisibility(View.INVISIBLE);
//        overlayLayout.addView(node.getData().getView());
//        for (Node it : node.getChildren()) {
//            addNodesToUI(it);
//        }
//
////        node.getChildren().forEach(it -> addNodesToUI(it));
//    }
//
//
//    public void initState() {
//
//        addNodesToUI(root);
//
//        if(historyNodes.empty()) { //first time
//
//            setFocusedNode(root);
//
//            root.getData().getView().setVisibility(View.VISIBLE);
//
//            for (Node<ARPlanElement> child : root.getChildren()) {
//                child.getData().getView().setVisibility(View.VISIBLE);
//
//            }
//
//        }
//
//        setAllVisibleNodes();
//
//    }
//
//    public boolean isFocusedNode(Node<ARPlanElement> node) {
//        return focusedNode.getData().getName().equals(node.getData().getName());
//    }
//
//    public boolean isNodeFocusedNodeParent(Node<ARPlanElement> node) {
//
//        return getFocusedNode().getParent().getData().getName().equals(node.getData().getName());
//
//    }
//
//    public class Node<T>{
//
//        private T data = null;
//        private List<Node<T>> children = new ArrayList<>();
//        private Node<T> parent = null;
//
//        public Node(T data){
//            this.data = data;
//        }
//
//        public Node<T> addChild(Node<T> child){
//            child.setParent(this);
//            this.children.add(child);
//            return child;
//        }
//
//        public List<Node<T>> getChildren() {
//            return children;
//        }
//
//        public T getData(){
//            return data;
//        }
//
//        public void setData(T Data){
//            this.data = data;
//        }
//
//        private void setParent(Node<T> parent){
//            this.parent = parent;
//        }
//
//        public Node<T> getParent(){
//            return parent;
//        }
//    }
//}
