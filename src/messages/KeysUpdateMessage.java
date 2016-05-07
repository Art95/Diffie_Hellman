package messages;

import dhtree.BranchInformation;

/**
 * Created by Artem on 02.05.2016.
 */
public class KeysUpdateMessage {
    public int clientID;
    public int levelInHierarchy;
    public BranchInformation levelTreeChanges;
    public BranchInformation hierarchyTreeChanges;
}
