package hierarchytree;

import leveltree.BranchInformation;
import leveltree.BranchNodeInformation;
import participants.Client;
import util.Pair;

import java.math.BigInteger;
import java.util.*;

/**
 * Created by Artem on 06.05.2016.
 */
public class HierarchyTree {
    class Node {
        private Integer hierarchyLevel;

        private Set<Integer> responsibility; // contains hierarchy levels that can be contacted using this node's secret key

        private BigInteger publicKey;
        private BigInteger secretKey;

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

        public Node(HierarchyTreeNodeInformation nodeInfo) {
            hierarchyLevel = nodeInfo.getHierarchyLevel();

            secretKey = null;
            publicKey = nodeInfo.getNodePublicKey();

            parent = null;
            left = null;
            right = null;

            clientsIDs = new HashSet<>(nodeInfo.getClientsIDs());
            responsibility = new HashSet<>(nodeInfo.getResponsibility());
        }

        public boolean hasResponsibilities() {
            return !responsibility.isEmpty();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Node node = (Node) o;

            if (!hierarchyLevel.equals(node.hierarchyLevel)) return false;
            if (left != null ? !left.equals(node.left) : node.left != null) return false;
            if (right != null ? !right.equals(node.right) : node.right != null) return false;
            return parent != null ? parent.equals(node.parent) : node.parent == null;

        }

        @Override
        public int hashCode() {
            int result = hierarchyLevel.hashCode();
            result = 31 * result + (left != null ? left.hashCode() : 0);
            result = 31 * result + (right != null ? right.hashCode() : 0);
            result = 31 * result + (parent != null ? parent.hashCode() : 0);
            return result;
        }
    }

    private Node root;

    private Integer masterClientHierarchyLevel;

    private Map<Integer, Node> hierarchyLevels;

    private int minHierarchyLevel;
    private int maxHierarchyLevel;

    private BigInteger p, g;

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

    public HierarchyTree(HierarchyTreeInformation treeInfo) {
        if (treeInfo == null)
            throw new NullPointerException("HierarchyTree: HierarchyTreeInformation = null!");

        this.hierarchyLevels = new HashMap<>();

        this.minHierarchyLevel = treeInfo.getMinHierarchyLevel();
        this.maxHierarchyLevel = treeInfo.getMaxHierarchyLevel();

        HierarchyTreeNodeInformation rootInfo = treeInfo.getNodeInformation(0);
        root = new Node(rootInfo);

        if (rootInfo.getLeftChildNodeID() != null) {
            root.left = buildSubTree(treeInfo, rootInfo.getLeftChildNodeID(), root);
        }

        if (rootInfo.getRightChildNodeID() != null) {
            root.right = buildSubTree(treeInfo, rootInfo.getRightChildNodeID(), root);
        }

        if (root.hierarchyLevel > 0) {
            hierarchyLevels.put(root.hierarchyLevel, root);
        }
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
            throw new NullPointerException("HierarchyTree: root = null!");

        if (!hierarchyLevels.containsKey(masterClientHierarchyLevel))
            throw new IllegalArgumentException("HierarchyTree: Tree does not contain hierarchy + " + masterClientHierarchyLevel);

        Node clientNode = hierarchyLevels.get(masterClientHierarchyLevel);

        updateKeysInBranch(clientNode.parent);
    }

    public void updateKeys(BranchInformation branchInfo) {
        Node sponsorNode = setChangedKeys(branchInfo);
        Node masterClientNode = hierarchyLevels.get(masterClientHierarchyLevel);

        Node lca = findLCA(sponsorNode, masterClientNode);

        if (lca == null) {
            System.out.println(this.masterClientHierarchyLevel + " " + branchInfo.getBranchMasterClientNodeID());
            throw new UnknownError("HierarchyTree: no least common ancestor found");
        }

        updateKeysInBranch(lca);
    }

    public HierarchyTreeInformation getTreeInformation() {
        if (root == null)
            throw new NullPointerException("HierarchyTree: root = null!");

        HierarchyTreeInformation treeInfo = new HierarchyTreeInformation();
        treeInfo.setMaxHierarchyLevel(maxHierarchyLevel);
        treeInfo.setMinHierarchyLevel(minHierarchyLevel);

        HierarchyTreeNodeInformation rootInfo = new HierarchyTreeNodeInformation();

        rootInfo.setNodeID(0);
        rootInfo.setHierarchyLevel(root.hierarchyLevel);
        rootInfo.setParentNodeID(null);
        rootInfo.setNodePublicKey(root.publicKey);
        rootInfo.addClientsIDs(root.clientsIDs);
        rootInfo.addResponsibilities(root.responsibility);

        Integer leftChildID = null;
        Integer rightChildID = null;

        if (root.left != null) {
            leftChildID = (root.left.hierarchyLevel > 0) ? root.left.hierarchyLevel : -1;
            collectTreeInformation(root.left, leftChildID, 0, treeInfo);
        }

        if (root.right != null) {
            rightChildID = (root.right.hierarchyLevel > 0) ? root.right.hierarchyLevel : -2;
            collectTreeInformation(root.right, rightChildID, 0, treeInfo);
        }

        rootInfo.setLeftChildNodeID(leftChildID);
        rootInfo.setRightChildNodeID(rightChildID);

        treeInfo.addNodeInformation(rootInfo);

        return treeInfo;
    }

    public BranchInformation getBranchInformation(Integer hierarchyLevel) {
        if (root == null)
            throw new NullPointerException("HierarchyTree: root = null!");

        if (!hierarchyLevels.containsKey(hierarchyLevel))
            throw new IllegalArgumentException("Hierarchy: Tree does not contain hierarchy level + " + hierarchyLevel);

        Node levelLeaf = hierarchyLevels.get(hierarchyLevel);

        return collectBranchInformation(levelLeaf);
    }

    public void setMasterClientHierarchyLevelData(Client client, BigInteger secretKey, BigInteger publicKey) {
        this.masterClientHierarchyLevel = client.getLevelInHierarchy();

        Node masterClientLeaf = hierarchyLevels.get(masterClientHierarchyLevel);

        masterClientLeaf.secretKey = secretKey;
        masterClientLeaf.publicKey = publicKey;

        p = client.getP();
        g = client.getG();
    }

    public int numberOfParticipantsOnLevel(Integer hierarchyLevel) {
        if (!hierarchyLevels.containsKey(hierarchyLevel))
            return 0;

        return hierarchyLevels.get(hierarchyLevel).clientsIDs.size();
    }

    public Integer findSponsorID(Integer changingLevel) {
        Set<Integer> levels = new TreeSet<>(hierarchyLevels.keySet());

        for (Integer level : levels) {
            if (!level.equals(changingLevel) && !hierarchyLevels.get(level).clientsIDs.isEmpty())
                return hierarchyLevels.get(level).clientsIDs.iterator().next();
        }

        return null;
    }

    public void clear() {
        root = null;
        masterClientHierarchyLevel = null;

        maxHierarchyLevel = Integer.MAX_VALUE;
        minHierarchyLevel = Integer.MIN_VALUE;

        hierarchyLevels.clear();

        p = null;
        g = null;
    }

    @Override
    public String toString() {
        return "HTree: (" + root.secretKey + ", " + root.publicKey + ")";
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

        hierarchyLevels.put(client.getLevelInHierarchy(), newLeaf);

        if (root.parent != null) {
            root = root.parent;
        }
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

        root = newInnerNode;
        hierarchyLevels.put(client.getLevelInHierarchy(), newLeaf);
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
        hierarchyLevels.put(client.getLevelInHierarchy(), newLeaf);
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

            BigInteger secretKey = (left.secretKey != null) ? left.secretKey : right.secretKey;
            BigInteger publicKey = (left.secretKey != null) ? right.publicKey : left.publicKey;

            if (secretKey == null) {            // both children are nulls -> set nulls
                currentNode.secretKey = null;
                currentNode.publicKey = null;
            } else if (publicKey == null) {     // one of children is null -> copy non null child
                currentNode.secretKey = secretKey;
                currentNode.publicKey = (left.publicKey != null) ? left.publicKey : right.publicKey;
            } else {                            // both children are present -> calculate new keys
                currentNode.secretKey = publicKey.modPow(secretKey, p);
                currentNode.publicKey = g.modPow(currentNode.secretKey, p);
            }

            currentNode = currentNode.parent;
        }
    }

    private Node findLCA(Node leaf1, Node leaf2) {
        int hierarchy1 = leaf1.hierarchyLevel;
        int hierarchy2 = leaf2.hierarchyLevel;

        if (hierarchy1 < hierarchy2) {
            return leaf2.parent;
        } else {
            return leaf1.parent;
        }
    }

    private BranchInformation collectBranchInformation(Node leaf) {
        BranchInformation branchInfo = new BranchInformation();
        Node currentNode = leaf;
        int index = -1;

        while (currentNode != root) {
            int id = Integer.MIN_VALUE;

            try {
                id = (currentNode.hierarchyLevel > 0) ? currentNode.hierarchyLevel : index--;
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(-1010);
            }

            int parentNodeID = (currentNode.parent == root) ? 0 : index;

            branchInfo.addNodeInfo(id, parentNodeID, currentNode.publicKey);

            currentNode = currentNode.parent;
        }

        branchInfo.addNodeInfo(0, 0, root.publicKey);

        return branchInfo;
    }

    private void collectTreeInformation(Node node, Integer ID, Integer parentNodeID, HierarchyTreeInformation treeInfo) {
        HierarchyTreeNodeInformation nodeInfo = new HierarchyTreeNodeInformation();

        nodeInfo.setNodeID(ID);
        nodeInfo.setParentNodeID(parentNodeID);
        nodeInfo.setHierarchyLevel(node.hierarchyLevel);
        nodeInfo.setNodePublicKey(node.publicKey);
        nodeInfo.addResponsibilities(node.responsibility);
        nodeInfo.addClientsIDs(node.clientsIDs);

        Integer leftChildNodeID = null;
        Integer rightChildNodeID = null;

        if (node.left != null) {
            leftChildNodeID = (node.left.hierarchyLevel > 0) ? node.left.hierarchyLevel : 2 * ID - 1;
            collectTreeInformation(node.left, leftChildNodeID, ID, treeInfo);
        }

        if (node.right != null) {
            rightChildNodeID = (node.right.hierarchyLevel > 0) ? node.right.hierarchyLevel : 2 * ID - 2;
            collectTreeInformation(node.right, rightChildNodeID, ID, treeInfo);
        }

        nodeInfo.setLeftChildNodeID(leftChildNodeID);
        nodeInfo.setRightChildNodeID(rightChildNodeID);

        treeInfo.addNodeInformation(nodeInfo);
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

    private Node buildSubTree(HierarchyTreeInformation treeInfo, Integer currentID, Node parent) {
        HierarchyTreeNodeInformation nodeInfo = treeInfo.getNodeInformation(currentID);

        Node node = new Node(nodeInfo);
        node.parent = parent;

        if (nodeInfo.getLeftChildNodeID() != null) {
            node.left = buildSubTree(treeInfo, nodeInfo.getLeftChildNodeID(), node);
        }

        if (nodeInfo.getRightChildNodeID() != null) {
            node.right = buildSubTree(treeInfo, nodeInfo.getRightChildNodeID(), node);
        }

        if (node.hierarchyLevel > 0)
            hierarchyLevels.put(node.hierarchyLevel, node);

        return node;
    }
}
