package dhtree;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Artem on 05.05.2016.
 */
public class TreeInformation {
    private Map<Integer, TreeNodeInformation> nodesInformation;
    private int treeHeight;
    private int numberOfClients;

    public TreeInformation() {
        nodesInformation = new HashMap<>();
        treeHeight = 0;
        numberOfClients = 0;
    }

    public void addNodeInformation(TreeNodeInformation nodeInfo) {
        nodesInformation.put(nodeInfo.getNodeID(), nodeInfo);
    }

    public TreeNodeInformation getNodeInformation(Integer nodeID) {
        if (!nodesInformation.containsKey(nodeID)) {
            throw new IllegalArgumentException("TreeInformation: does not contain information about node " + nodeID);
        }

        return nodesInformation.get(nodeID);
    }

    public void setTreeHeight(int treeHeight) {
        this.treeHeight = treeHeight;
    }

    public int getTreeHeight() { return this.treeHeight; }

    public int getNumberOfClients() {
        return numberOfClients;
    }

    public void setNumberOfClients(int numberOfClients) {
        this.numberOfClients = numberOfClients;
    }
}
