package leveltree;

import java.math.BigInteger;

/**
 * Created by Artem on 04.05.2016.
 */
public class BranchNodeInformation {
    private int id;
    private int parentId;
    private BigInteger publicKey;

    public BranchNodeInformation() {
        id = Integer.MIN_VALUE;
        parentId = Integer.MIN_VALUE;
        publicKey = null;
    }

    public BranchNodeInformation(int id, int parentId, BigInteger publicKey) {
        this.id = id;
        this.parentId = parentId;
        this.publicKey = publicKey;
    }

    public int getNodeID() {
        return this.id;
    }

    public int getParentNodeID() {
        return parentId;
    }

    public BigInteger getPublicKey() {
        return publicKey;
    }
}
