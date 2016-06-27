package leveltree;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Artem on 05.05.2016.
 */
public class LevelTreeInformation {
    private Map<Integer, LevelTreeNodeInformation> nodesInformation;
    private int treeHeight;
    private int numberOfClients;

    public LevelTreeInformation() {
        nodesInformation = new HashMap<>();
        treeHeight = 0;
        numberOfClients = 0;
    }

    public void addNodeInformation(LevelTreeNodeInformation nodeInfo) {
        nodesInformation.put(nodeInfo.getNodeID(), nodeInfo);
    }

    public LevelTreeNodeInformation getNodeInformation(Integer nodeID) {
        if (!nodesInformation.containsKey(nodeID)) {
            throw new IllegalArgumentException("LevelTreeInformation: does not contain information about node " + nodeID);
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
