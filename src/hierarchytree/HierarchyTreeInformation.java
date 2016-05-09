package hierarchytree;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Artem on 07.05.2016.
 */
public class HierarchyTreeInformation {
    private Map<Integer, HierarchyTreeNodeInformation> nodesInformation;
    private int minHierarchyLevel;
    private int maxHierarchyLevel;

    public HierarchyTreeInformation() {
        nodesInformation = new HashMap<>();
        minHierarchyLevel = Integer.MIN_VALUE;
        maxHierarchyLevel = Integer.MAX_VALUE;
    }

    public void addNodeInformation(HierarchyTreeNodeInformation nodeInfo) {
        nodesInformation.put(nodeInfo.getNodeID(), nodeInfo);
    }

    public HierarchyTreeNodeInformation getNodeInformation(Integer nodeID) {
        if (!nodesInformation.containsKey(nodeID)) {
            throw new IllegalArgumentException("LevelTreeInformation: does not contain information about node " + nodeID);
        }

        return nodesInformation.get(nodeID);
    }

    public void setMinHierarchyLevel(int minHierarchyLevel) {
        this.minHierarchyLevel = minHierarchyLevel;
    }

    public int getMinHierarchyLevel() { return this.minHierarchyLevel; }

    public int getMaxHierarchyLevel() {
        return maxHierarchyLevel;
    }

    public void setMaxHierarchyLevel(int maxHierarchyLevel) {
        this.maxHierarchyLevel = maxHierarchyLevel;
    }
}
