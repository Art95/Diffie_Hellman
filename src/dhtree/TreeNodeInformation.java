package dhtree;

import participants.Client;

/**
 * Created by Artem on 05.05.2016.
 */
public class TreeNodeInformation {
    private int id;

    private Integer parentID;
    private Integer leftChildID;
    private Integer rightChildID;

    private Long publicKey;

    private Client client;

    public TreeNodeInformation() {
        id = Integer.MIN_VALUE;

        parentID = null;
        leftChildID = null;
        rightChildID = null;

        publicKey = null;

        client = null;
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

    public Client getNodeClient() {
        return client;
    }

    public void setNodeClient(Client client) {
        this.client = client;
    }
}
