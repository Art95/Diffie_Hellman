package leveltree;

import participants.Client;

import java.math.BigInteger;

/**
 * Created by Artem on 05.05.2016.
 */
public class LevelTreeNodeInformation {
    private int id;

    private Integer parentID;
    private Integer leftChildID;
    private Integer rightChildID;

    private BigInteger publicKey;

    private Client client;

    private int index; // index of leaf

    public LevelTreeNodeInformation() {
        id = Integer.MIN_VALUE;

        index = -1;

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

    public int getIndex() { return this.index; }

    public void setIndex(int index) { this.index = index; }

    public Integer getParentNodeID() { return parentID; }

    public void setParentNodeID(Integer parentID) {
        this.parentID = parentID;
    }

    public Integer getLeftChildNodeID() { return leftChildID; }

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

    public Client getNodeClient() {
        return client;
    }

    public void setNodeClient(Client client) {
        this.client = client;
    }
}
