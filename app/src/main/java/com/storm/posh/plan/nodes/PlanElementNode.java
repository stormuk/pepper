//package com.storm.posh.plan.nodes;
//
//import android.graphics.Color;
//
//import com.storm.posh.plan.nodes.plannodes.ActionNode;
//import com.storm.posh.plan.nodes.plannodes.ActionPatternNode;
//import com.storm.posh.plan.nodes.plannodes.DriveCollectionNode;
//import com.storm.posh.plan.nodes.plannodes.RouteElementNode;
//import com.storm.posh.plan.planelements.PlanElement;
//import javafx.application.Platform;
//import javafx.scene.Node;
//import javafx.scene.effect.Glow;
//import javafx.scene.layout.Pane;
//import javafx.scene.layout.StackPane;
//import javafx.scene.shape.Rectangle;
//import javafx.scene.shape.StrokeType;
//import javafx.scene.text.Text;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * <p>
// * Each node represents an individual {@link PlanElement} by containing a reference to it. The node extends {@link Pane}
// * that can be placed and dragged around the diagram tree canvas. Nodes hold a reference of their "glow" level, which changes
// * based on how many times it was called in debugger.
// * </p>
// *
// * @author :   Andreas Theodorou - www.recklesscoding.com
// * @version :   %G%
// */
//public class PlanElementNode extends Pane {
//
//    private final PlanElement planElement;
//
//    private List<PlanElementNode> children = new ArrayList<>();
//
//    private PlanElementNode parent;
//
//    private Rectangle rectangle = new Rectangle(175, 50);
//
//    private Text text;
//
//    private Glow glow = new Glow(0);
//
//    private boolean isSubtreeCollapsed;
//
//    public PlanElementNode(PlanElement planElement, Color color) {
//        if (planElement != null) {
//            this.planElement = planElement;
//            this.text = new Text(planElement.getNameOfElement());
//            this.planElement.encloseToNode(this);
//        } else {
//            this.planElement = new PlanElement("Error");
//            this.text = new Text("Element error");
//        }
//        initView(color);
//    }
//
//    public void decreaseGlow() {
//        setGlow(false);
//    }
//
//    public void increaseGlow() {
//        boolean isCorrectElement = true;
//        List<PlanElement> planElements = new ArrayList<>();
//        planElements.add(this.planElement);
//        PlanElementNode planNode = this;
//
//        if (!(planNode instanceof DriveCollectionNode)) {
//            while (((planNode = planNode.getNodeParent()) != null)) {
//                if (planNode instanceof RouteElementNode) {
//                    isCorrectElement = true;
//                    break;
//                } else {
//                    if (!planNode.getPlanElement().isSetToUpdate() && !(planNode instanceof ActionPatternNode)) {
//                        isCorrectElement = false;
//                        break;
//                    } else if (!planNode.getPlanElement().isSetToUpdate()) {
//                        isCorrectElement = false;
//                        break;
//                    } else if (planNode instanceof ActionPatternNode) {
//                        if (((ActionPatternNode) planNode).isActivated()) {
//                            isCorrectElement = true;
//                            planElements.add(planNode.planElement);
//                        }
//                    } else {
//                        isCorrectElement = true;
//                        planElements.add(planNode.planElement);
//                    }
//                }
//            }
//        }
//
//        if (isCorrectElement) {
//            setGlow(true);
//
//            Platform.runLater(() -> {
//                if (this instanceof ActionNode) {
//                    if (this.getNodeParent() instanceof ActionPatternNode) {
//                        ((ActionPatternNode) this.getNodeParent()).activate();
//                    }
//                    for (PlanElement planElement : planElements) {
//                        planElement.setFinishUpdate();
//                    }
//                }
//            });
//        }
//    }
//
//    public PlanElementNode getNodeParent() {
//        return parent;
//    }
//
//    private void setView(Node view) {
//        getChildren().add(view);
//    }
//
//    public void addCellChild(PlanElementNode planElementNode) {
//        children.add(planElementNode);
//    }
//
//    public List<PlanElementNode> getCellChildren() {
//        return children;
//    }
//
//    public void addCellParent(PlanElementNode planElementNode) {
//        parent = planElementNode;
//    }
//
//    public void removeChild(PlanElementNode planElementNode) {
//        children.remove(planElementNode);
//    }
//
//    public Rectangle getRectangle() {
//        return rectangle;
//    }
//
//    public Text getText() {
//        return text;
//    }
//
//    public PlanElement getPlanElement() {
//        return planElement;
//    }
//
//    public boolean isSubtreeCollapsed() {
//        return isSubtreeCollapsed;
//    }
//
//    public void setSubtreeCollapsed(boolean subtreeCollapsed) {
//        isSubtreeCollapsed = subtreeCollapsed;
//    }
//
//    private void initView(Color color) {
//        initRectangle(color);
//        initText(color);
//
//        StackPane stack = new StackPane();
//        stack.getChildren().addAll(getRectangle(), text);
//        setView(stack);
//    }
//
//    private void initRectangle(Color color) {
//        rectangle.setFill(Color.DARKSLATEGRAY);
//        rectangle.setStroke(color);
//        rectangle.setStrokeType(StrokeType.OUTSIDE);
////        rectangle.setStrokeMiterLimit(5.0);
//    }
//
//    private void initText(Color color) {
//        text.setStyle("-fx-font-size: 16px;");
//        text.setFill(color);
//        text.setFill(Color.WHITE);
//    }
//
//    private void setGlow(boolean increase) {
//        double glowLevel = glow.getLevel();
//        if (increase) {
//            glowLevel = glowLevel + 0.3;
//        } else {
//            glowLevel = glowLevel - 0.1;
//        }
//        if (glowLevel > 1) {
//            glow.setLevel(1);
//        } else if (glowLevel < 0) {
//            glow.setLevel(0);
//        } else {
//            glow.setLevel(glowLevel);
//        }
//
//        rectangle.setEffect(glow);
//    }
//}