package messages;

import dhtree.BranchInformation;

/**
 * Created by Artem on 02.05.2016.
 */
public class KeysUpdateMessage {
    public int clientId;
    public int levelInHierarchy;
    public BranchInformation changedBranchInformation;
}
