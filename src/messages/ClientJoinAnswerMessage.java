package messages;

import dhtree.TreeInformation;
import hierarchytree.HierarchyTreeInformation;

/**
 * Created by Artem on 02.05.2016.
 */
public class ClientJoinAnswerMessage {
    public long p;
    public long g;
    public TreeInformation levelTreeInfo;
    public HierarchyTreeInformation hierarchyTreeInfo;
}
