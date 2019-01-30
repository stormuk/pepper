//package com.storm.posh.plan.nodes;
//
//import javafx.beans.binding.DoubleBinding;
//import javafx.scene.Group;
//import javafx.scene.paint.Color;
//import javafx.scene.shape.Line;
//
///**
// * Created by Andreas on 28/12/2015.
// */
//public class NodesConnector extends Group {
//
//    private PlanElementNode source;
//
//    private PlanElementNode target;
//
//    private Line line = new Line();
//
//    public NodesConnector(PlanElementNode source, PlanElementNode target) {
//
//        this.source = source;
//        this.target = target;
//
//        initEdge(source, target);
//    }
//
//    private void initEdge(PlanElementNode source, PlanElementNode target) {
//        source.addCellChild(target);
//        target.addCellParent(source);
//
//        line.setFill(Color.WHITE);
//        line.setStroke(Color.WHITE);
//
//        DoubleBinding startLocationX = source.layoutXProperty().add(source.getBoundsInParent().getWidth() / 2.0);
//        DoubleBinding startLocationY = source.layoutYProperty().add(source.getBoundsInParent().getHeight() / 2.0);
//
//        DoubleBinding endLocationX = target.layoutXProperty().add(target.getBoundsInParent().getWidth() / 2.0);
//        DoubleBinding endLocationY = target.layoutYProperty().add(target.getBoundsInParent().getHeight() / 2.0);
//
//        line.startXProperty().bind(startLocationX);
//        line.startYProperty().bind(startLocationY);
//        line.endXProperty().bind(endLocationX);
//        line.endYProperty().bind(endLocationY);
//
//        getChildren().add(line);
//    }
//
//    public PlanElementNode getSource() {
//        return source;
//    }
//
//    public PlanElementNode getTarget() {
//        return target;
//    }
//}
