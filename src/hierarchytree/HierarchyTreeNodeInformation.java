package hierarchytree;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Artem on 07.05.2016.
 */
public class HierarchyTreeNodeInformation {
    private int id;

    private Integer parentID;
    private Integer leftChildID;
    private Integer rightChildID;

    private Long publicKey;

    private Set<Integer> responsibility;
    private Set<Integer> clientsIDs;

    public HierarchyTreeNodeInformation() {
        id = Integer.MIN_VALUE;

        parentID = null;
        leftChildID = null;
        rightChildID = null;

        publicKey = null;

        responsibility = new HashSet<>();
        clientsIDs = new HashSet<>();
    }

    public int getNodeID() {
        return id;
    }

    public void setNodeID(int id) {
        this.id = id;
    }

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

    public Long getNodePublicKey() {
        return publicKey;
    }

    public void setNodePublicKey(Long publicKey) {
        this.publicKey = publicKey;
    }

    public Set<Integer> getResponsibility() {
        return responsibility;
    }

    public void addResponisibility(Integer hierarchyLevel) {
        this.responsibility.add(hierarchyLevel);
    }

    public void addResponsibilties(Set<Integer> hierarchyLevels) {
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
