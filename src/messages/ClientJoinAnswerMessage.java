package messages;

import dhtree.LevelTreeInformation;
import hierarchytree.HierarchyTreeInformation;

/**
 * Created by Artem on 02.05.2016.
 */
public class ClientJoinAnswerMessage {
    public int ID;
    public int levelInHierarchy;
    public long p;
    public long g;
    public LevelTreeInformation levelTreeInfo;
    public HierarchyTreeInformation hierarchyTreeInfo;
}
