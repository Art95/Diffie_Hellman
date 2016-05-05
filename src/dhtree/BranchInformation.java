package dhtree;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Artem on 04.05.2016.
 */
public class BranchInformation {
    private Map<Integer, BranchNodeInformation> nodes;
    private int branchMasterClientNodeID;

    public BranchInformation() {
        nodes = new HashMap<>();
        branchMasterClientNodeID = Integer.MIN_VALUE;
    }

    public void addNodeInfo(int id, int parentId, long publicKey) {
        if (id > 0)
            branchMasterClientNodeID = id;

        nodes.put(id, new BranchNodeInformation(id, parentId, publicKey));
    }

    public BranchNodeInformation getNodeInformation(Integer nodeID) {
        if (!nodes.containsKey(nodeID))
            throw new IllegalArgumentException("BranchInformation: does not contain information about node " + nodeID);

        return this.nodes.get(nodeID);
    }

    public int getBranchMasterClientNodeID() {
        return this.branchMasterClientNodeID;
    }
}
