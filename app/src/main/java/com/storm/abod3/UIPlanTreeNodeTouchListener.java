//package com.storm.abod3;
//
//import android.view.View;
//
//class UIPlanTreeNodeTouchListener implements View.OnClickListener {
//
//    private UIPlanTree uiPlanTree;
//    private UIPlanTree.Node<ARPlanElement> node;
//
//
//    public UIPlanTreeNodeTouchListener(UIPlanTree uiPlanTree, UIPlanTree.Node<ARPlanElement> node) {
//        this.uiPlanTree = uiPlanTree;
//        this.node = node;
//    }
//
//    @Override
//    public void onClick(View view) {
//
//        uiPlanTree.hideNodes(uiPlanTree.getRoot());
//        uiPlanTree.setFocusedNode(node);
//
//        uiPlanTree.setAllVisibleNodes();
//    }
//}
