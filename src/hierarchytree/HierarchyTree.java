package hierarchytree;

import dhtree.BranchInformation;
import dhtree.BranchNodeInformation;
import participants.Client;
import util.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Artem on 06.05.2016.
 */
public class HierarchyTree {
    class Node {
        private Integer hierarchyLevel;

        private Set<Integer> responsibility; // contains hierarchy levels that can be contacted using this node's secret key

        private Long publicKey;
        private Long secretKey;

        private Node left;
        private Node right;
        private Node parent;

        private Set<Integer> clientsIDs;

        public Node() {
            hierarchyLevel = -1;

            responsibility = new HashSet<>();

            publicKey = null;
            secretKey = null;

            left = null;
            right = null;
            parent = null;

            clientsIDs = new HashSet<>();
        }

        public boolean hasResponsibilities() {
            return !responsibility.isEmpty();
        }
    }

    private Node root;

    private Integer masterClientHierarchyLevel;

    private Map<Integer, Node> hierarchyLevels;

    private int minHierarchyLevel;
    private int maxHierarchyLevel;

    private Long p, g;

    public HierarchyTree() {
        root = null;

        masterClientHierarchyLevel = -1;

        hierarchyLevels = new HashMap<>();

        minHierarchyLevel = Integer.MIN_VALUE;
        maxHierarchyLevel = Integer.MAX_VALUE;

        p = null;
        g = null;
    }

    public HierarchyTree(Client client) {
        initialize(client);
    }

    public void addClient(Client client) {
        if (root == null) {
            initialize(client);
            return;
        }

        int clientLevel = client.getLevelInHierarchy();

        if (hierarchyLevels.containsKey(clientLevel)) {
            Node clientLeaf = hierarchyLevels.get(clientLevel);
            clientLeaf.clientsIDs.add(client.getID());
        } else {
            Pair<Integer, Integer> neighbours = findNeighbours(clientLevel);

            if (clientLevel < maxHierarchyLevel) {
                pushFront(client, neighbours.getSecond());
                maxHierarchyLevel = clientLevel;
            } else if (clientLevel > minHierarchyLevel) {
                pushBack(client, neighbours.getFirst());
                minHierarchyLevel = clientLevel;
            } else {
                insertAfter(neighbours.getFirst(), client);
            }
        }
    }

    public void removeClient(Client client) {
        if (root == null)
            throw new NullPointerException("HierarchyTree: root = null!");

        Integer clientLevel = client.getLevelInHierarchy();

        if (!hierarchyLevels.containsKey(clientLevel))
            throw new IllegalArgumentException("HierarchyTree: no such hierarchy level " + clientLevel);

        Node holdingLeaf = hierarchyLevels.get(clientLevel);

        if (!holdingLeaf.clientsIDs.contains(client.getID()))
            throw new NullPointerException("HierarchyTree: Hierarchy level " + clientLevel + " does not contain client " + client);

        holdingLeaf.clientsIDs.remove(client.getID());

        if (holdingLeaf.clientsIDs.isEmpty()) {
            holdingLeaf.secretKey = null;
            holdingLeaf.publicKey = null;
        }
    }

    public void updateKeys() {
        if (root == null)
            throw new NullPointerException("DHTree: root = null!");

        if (!hierarchyLevels.containsKey(masterClientHierarchyLevel))
            throw new IllegalArgumentException("DHTree: Tree does not contain hierarchy + " + masterClientHierarchyLevel);

        Node clientNode = hierarchyLevels.get(masterClientHierarchyLevel);

        updateKeysInBranch(clientNode.parent);
    }

    public void updateKeys(BranchInformation branchInfo) {
        Node sponsorNode = setChangedKeys(branchInfo);
        Node masterClientNode = hierarchyLevels.get(masterClientHierarchyLevel);

        Node lca = findLCA(sponsorNode, masterClientNode);

        updateKeysInBranch(lca);
    }

    public BranchInformation getBranchInformation(Integer hierarchyLevel) {
        if (root == null)
            throw new NullPointerException("HierarchyTree: root = null!");

        if (!hierarchyLevels.containsKey(hierarchyLevel))
            throw new IllegalArgumentException("Hierarchy: Tree does not contain hierarchy level + " + hierarchyLevel);

        Node levelLeaf = hierarchyLevels.get(hierarchyLevel);

        return collectBranchInformation(levelLeaf);
    }

    private void pushFront(Client client, Integer nextLevel) {
        Node newInnerNode = new Node();

        Node newLeaf = new Node();
        newLeaf.hierarchyLevel = client.getLevelInHierarchy();
        newLeaf.clientsIDs.add(client.getID());
        newLeaf.responsibility.add(client.getLevelInHierarchy());
        newLeaf.parent = newInnerNode;

        Node nextLevelLeaf = hierarchyLevels.get(nextLevel);

        newInnerNode.left = newLeaf;
        newInnerNode.right = nextLevelLeaf;
        newInnerNode.parent = nextLevelLeaf.parent;

        nextLevelLeaf.parent = newInnerNode;

        if (newInnerNode.parent != null)
            newInnerNode.parent.left = newInnerNode;

        updateNodesResponsibilities(newInnerNode);
        nextLevelLeaf.responsibility.clear();
    }

    private void pushBack(Client client, Integer previousLevel) {
        Node newInnerNode = new Node();

        Node newLeaf = new Node();
        newLeaf.hierarchyLevel = client.getLevelInHierarchy();
        newLeaf.clientsIDs.add(client.getID());
        newLeaf.responsibility.add(client.getLevelInHierarchy());
        newLeaf.parent = newInnerNode;

        Node previousLevelLeaf = hierarchyLevels.get(previousLevel);

        newInnerNode.right = newLeaf;

        if (previousLevel == maxHierarchyLevel) {  // previous leaf is responsible for highest level in hierarchy
            newInnerNode.left = previousLevelLeaf;
            previousLevelLeaf.parent = newInnerNode;
        } else {
            newInnerNode.left = previousLevelLeaf.parent;
            newInnerNode.left.parent = newInnerNode;
        }

        updateNodesResponsibilities(newInnerNode);
        newLeaf.responsibility.clear();
    }

    private void insertAfter(Integer previousLevel, Client client) {
        Node newInnerNode = new Node();

        Node newLeaf = new Node();
        newLeaf.hierarchyLevel = client.getLevelInHierarchy();
        newLeaf.clientsIDs.add(client.getID());
        newLeaf.parent = newInnerNode;

        newInnerNode.right = newLeaf;

        Node previousLevelLeaf = hierarchyLevels.get(previousLevel);

        if (previousLevel == maxHierarchyLevel) { // previous leaf is responsible for highest level in hierarchy
            newInnerNode.left = previousLevelLeaf;
            newInnerNode.parent = previousLevelLeaf.parent;
            newInnerNode.parent.left = newInnerNode;
            previousLevelLeaf.parent = newInnerNode;
        } else {
            newInnerNode.left = previousLevelLeaf.parent;
            newInnerNode.parent = previousLevelLeaf.parent.parent;
            newInnerNode.parent.left = newInnerNode;
            newInnerNode.left.parent = newInnerNode;
        }

        updateNodesResponsibilities(newInnerNode);
    }

    private Pair<Integer, Integer> findNeighbours(Integer hierarchyLevel) {
        Integer previousLevel = Integer.MIN_VALUE;
        Integer nextLevel = Integer.MAX_VALUE;

        for (Integer level : hierarchyLevels.keySet()) {
            if (level > hierarchyLevel && level < nextLevel) {
                nextLevel = level;
            }

            if (level < hierarchyLevel && level > previousLevel) {
                previousLevel = level;
            }
        }

        previousLevel = (previousLevel == Integer.MIN_VALUE) ? null : previousLevel;
        nextLevel = (nextLevel == Integer.MAX_VALUE) ? null : nextLevel;

        return new Pair<>(previousLevel, nextLevel);
    }

    private void updateNodesResponsibilities(Node startingNode) {
        Node current = startingNode;

        while (current != null) {
            current.responsibility.addAll(current.left.responsibility);
            current.responsibility.addAll(current.right.responsibility);

            current = current.parent;
        }
    }

    private Node setChangedKeys(BranchInformation branchInfo) {
        int sponsorHierarchyLevel = branchInfo.getBranchMasterClientNodeID();
        int currentNodeInfoID = sponsorHierarchyLevel;

        Node sponsorLeaf = hierarchyLevels.get(sponsorHierarchyLevel);
        Node current = sponsorLeaf;

        while (current != null) {
            BranchNodeInformation nodeInfo = branchInfo.getNodeInformation(currentNodeInfoID);

            current.publicKey = nodeInfo.getPublicKey();

            currentNodeInfoID = nodeInfo.getParentNodeID();
            current = current.parent;
        }

        return sponsorLeaf;
    }

    private void updateKeysInBranch(Node node) {
        Node currentNode = node;
        Node left, right;

        while (currentNode != null) {
            left = currentNode.left;
            right = currentNode.right;

            if (left == null || right == null)
                throw new NullPointerException("HierarchyTree: failed to update keys.");

            Long secretKey = (left.secretKey != null) ? left.secretKey : right.secretKey;
            Long publicKey = (left.secretKey != null) ? right.publicKey : left.publicKey;

            if (secretKey == null) {            // both children are nulls -> set nulls
                currentNode.secretKey = null;
                currentNode.publicKey = null;
            } else if (publicKey == null) {     // one of children is null -> copy non null child
                currentNode.secretKey = secretKey;
                currentNode.publicKey = (left.publicKey != null) ? left.publicKey : right.publicKey;
            } else {                            // both children are present -> calculate new keys
                currentNode.secretKey = (long) Math.pow(publicKey, secretKey) % p;
                currentNode.publicKey = (long) Math.pow(g, currentNode.secretKey) % p;
            }

            currentNode = currentNode.parent;
        }
    }

    private Node findLCA(Node leaf1, Node leaf2) {
        Node p1 = leaf1;
        Node p2 = leaf2;

        while (p1 != p2) {
            p1 = p1.parent;
            p2 = p2.parent;
        }

        return p1;
    }

    private BranchInformation collectBranchInformation(Node leaf) {
        BranchInformation branchInfo = new BranchInformation();
        Node currentNode = leaf;
        int index = -1;

        while (currentNode != root) {
            int id = (currentNode.hierarchyLevel > 0) ? currentNode.hierarchyLevel : index--;

            branchInfo.addNodeInfo(id, index, currentNode.publicKey);

            currentNode = currentNode.parent;
        }

        branchInfo.addNodeInfo(0, 0, root.publicKey);

        return branchInfo;
    }

    private void initialize(Client client) {
        root = new Node();

        root.hierarchyLevel = client.getLevelInHierarchy();
        masterClientHierarchyLevel = client.getLevelInHierarchy();

        root.publicKey = client.getPublicKey();

        root.clientsIDs.add(client.getID());
        root.responsibility.add(masterClientHierarchyLevel);

        hierarchyLevels = new HashMap<>();
        hierarchyLevels.put(masterClientHierarchyLevel, root);

        minHierarchyLevel = masterClientHierarchyLevel;
        maxHierarchyLevel = masterClientHierarchyLevel;

        p = client.getP();
        g = client.getG();
    }
}
