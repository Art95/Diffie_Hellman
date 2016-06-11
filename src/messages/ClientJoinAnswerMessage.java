package messages;

import dhtree.LevelTreeInformation;
import hierarchytree.HierarchyTreeInformation;

import java.math.BigInteger;

/**
 * Created by Artem on 02.05.2016.
 */
public class ClientJoinAnswerMessage {
    public int ID;
    public int levelInHierarchy;
    public BigInteger p;
    public BigInteger g;
    public LevelTreeInformation levelTreeInfo;
    public HierarchyTreeInformation hierarchyTreeInfo;
}
