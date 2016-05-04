package dhtree;

/**
 * Created by Artem on 04.05.2016.
 */
public class BranchNodeInformation {
    public int id;
    public int parentId;
    public long publicKey;

    public BranchNodeInformation() {
        id = Integer.MIN_VALUE;
        parentId = Integer.MIN_VALUE;
        publicKey = Long.MIN_VALUE;
    }

    public BranchNodeInformation(int id, int parentId, long publicKey) {
        this.id = id;
        this.parentId = parentId;
        this.publicKey = publicKey;
    }
}
