package hierarchytree;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Artem on 07.05.2016.
 */
public class HierarchyTreeNodeInformation {
    private int ID;

    private int hierarchyLevel;

    private Integer parentID;
    private Integer leftChildID;
    private Integer rightChildID;

    private BigInteger publicKey;

    private Set<Integer> responsibility;
    private Set<Integer> clientsIDs;

    public HierarchyTreeNodeInformation() {
        ID = Integer.MIN_VALUE;

        hierarchyLevel = Integer.MIN_VALUE;

        parentID = null;
        leftChildID = null;
        rightChildID = null;

        publicKey = null;

        responsibility = new HashSet<>();
        clientsIDs = new HashSet<>();
    }

    public int getNodeID() {
        return ID;
    }

    public void setNodeID(int id) {
        this.ID = id;
    }

    public void setHierarchyLevel(int hierarchyLevel) {
        this.hierarchyLevel = hierarchyLevel;
    }

    public int getHierarchyLevel() { return this.hierarchyLevel; }

    public Integer getParentNodeID() {
        return parentID;
    }

    public void setParentNodeID(Integer parentID) {
        this.parentID = parentID;
    }

    public Integer getLeftChildNodeID() {
        return leftChildID;
    }

    public void setLeftChildNodeID(Integer leftChildID) {
        this.leftChildID = leftChildID;
    }

    public Integer getRightChildNodeID() {
        return rightChildID;
    }

    public void setRightChildNodeID(Integer rightChildID) {
        this.rightChildID = rightChildID;
    }

    public BigInteger getNodePublicKey() {
        return publicKey;
    }

    public void setNodePublicKey(BigInteger publicKey) {
        this.publicKey = publicKey;
    }

    public Set<Integer> getResponsibility() {
        return responsibility;
    }

    public void addResponisibility(Integer hierarchyLevel) {
        this.responsibility.add(hierarchyLevel);
    }

    public void addResponsibilities(Set<Integer> hierarchyLevels) {
        this.responsibility.addAll(hierarchyLevels);
    }

    public Set<Integer> getClientsIDs() {
        return this.clientsIDs;
    }

    public void addClientID(Integer ID) {
        this.clientsIDs.add(ID);
    }

    public void addClientsIDs(Set<Integer> IDs) {
        this.clientsIDs.addAll(IDs);
    }
}
