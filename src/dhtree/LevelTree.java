package dhtree;

import participants.Client;
import util.Pair;

import java.math.BigInteger;
import java.util.*;

/**
 * Created by Artem on 03.05.2016.
 */
public class LevelTree {
    private class Node {
        private int id;

        private BigInteger secretKey;
        private BigInteger publicKey;

        private Node parent, left, right;

        private Client client;

        public Node() {
            id = -1;

            secretKey = null;
            publicKey = null;

            parent = null;
            left = null;
            right = null;

            client = null;
        }

        public Node(LevelTreeNodeInformation nodeInfo) {
            id = nodeInfo.getIndex();

            publicKey = nodeInfo.getNodePublicKey();

            parent = null;
            left = null;
            right = null;

            client = nodeInfo.getNodeClient();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Node)) return false;

            Node node = (Node) o;

            return id == node.id;

        }

        @Override
        public int hashCode() {
            return id;
        }

        public boolean isLeaf() {
            return left == null && right == null;
        }

        public void clear() {
            client = null;

            secretKey = null;
            publicKey = null;
        }
    }

    private Node root;
    private int height;

    private Map<Node, Client> nodesClients;
    private Map<Client, Node> clientsNodes;

    private int numberOfClients;

    private Client masterClient;

    public LevelTree() {
        root = null;
        height = 0;

        nodesClients = new TreeMap<>((Comparator<Node>) (node1, node2) -> node1.id - node2.id);
        clientsNodes = new HashMap<>();

        numberOfClients = 0;

        masterClient = null;
    }

    public LevelTree(Client client) {
        initialize(client);
    }

    public LevelTree(LevelTreeInformation treeInfo) {
        if (treeInfo == null)
            throw new NullPointerException("LevelTree: LevelTreeInformation = null!");

        this.clientsNodes = new HashMap<>();
        this.nodesClients = new TreeMap<>((Comparator<Node>) (node1, node2) -> node1.id - node2.id);

        this.height = treeInfo.getTreeHeight();
        this.numberOfClients = treeInfo.getNumberOfClients();

        LevelTreeNodeInformation rootInfo = treeInfo.getNodeInformation(0);
        root = new Node(rootInfo);

        if (rootInfo.getLeftChildNodeID() != null) {
            root.left = buildSubTree(treeInfo, rootInfo.getLeftChildNodeID(), root);
        }

        if (rootInfo.getRightChildNodeID() != null) {
            root.right = buildSubTree(treeInfo, rootInfo.getRightChildNodeID(), root);
        }

        if (rootInfo.getNodeClient() != null) {
            clientsNodes.put(rootInfo.getNodeClient(), root);
        }

        if (rootInfo.getIndex() > 0) {
            nodesClients.put(root, rootInfo.getNodeClient());
        }
    }

    public void addClient(Client client) {
        if (root == null) {
            initialize(client);
            return;
        }

        if (clientsNodes.containsKey(client))
            return;

        if (numberOfClients >= Math.pow(2, height - 1)) {
            expandTree();
        }

        for (Node node : nodesClients.keySet()) {
            if (nodesClients.get(node) == null) {
                node.client = client;

                nodesClients.put(node, client);
                clientsNodes.put(client, node);

                ++numberOfClients;

                return;
            }
        }
    }

    public void removeClient(Client leaving_client) {
        if (root == null)
            throw new NullPointerException("LevelTree: root = null!");

        if (!clientsNodes.containsKey(leaving_client))
            throw new IllegalArgumentException("LevelTree: does not contain client " + leaving_client);

        Node holdingNode = clientsNodes.get(leaving_client);

        nodesClients.put(holdingNode, null);
        clientsNodes.remove(leaving_client);

        holdingNode.clear();

        cleanBranchKeys(holdingNode);

        --numberOfClients;
    }
    
    public void updateKeys() {
        if (root == null)
            throw new NullPointerException("LevelTree: root = null!");

        if (!clientsNodes.containsKey(masterClient))
            throw new IllegalArgumentException("LevelTree: Tree does not contain client + " + masterClient);

        Node clientNode = clientsNodes.get(masterClient);

        updateKeysInBranch(clientNode.parent);
    }

    public void updateKeys(BranchInformation branchInfo) {
        Node sponsorNode = setChangedKeys(branchInfo);
        Node masterClientNode = clientsNodes.get(masterClient);

        Node lca = findLCA(sponsorNode, masterClientNode);

        updateKeysInBranch(lca);
    }

    public Client findSponsor(Client changingClient) {
        for (Client client : clientsNodes.keySet()) {
            if (!client.equals(changingClient))
                return client;
        }

        return null;
    }

    public Client findSiblingClient(Client client) {
        int separator = (int) Math.pow(2, height - 1) / 2;
        Node clientNode = clientsNodes.get(client);
        Node alternativeSibling = null;

        boolean leftHalf = clientNode.id <= separator;

        Map<Integer, Pair<Node, Node>> neighbours = new TreeMap<>();

        for (Client clnt : clientsNodes.keySet()) {
            if (clnt.equals(client))
                continue;

            Node node = clientsNodes.get(clnt);

            if ((leftHalf && node.id <= separator) || (!leftHalf && node.id > separator)) {
                Integer distance = Math.abs(node.id - clientNode.id);

                if (!neighbours.containsKey(distance))
                    neighbours.put(distance, new Pair<>(node, null));
                else
                    neighbours.get(distance).setSecond(node);
            } else if (alternativeSibling == null)
                alternativeSibling = node;
        }

        Node nearestNode = null;

        if (neighbours.containsKey(1)) {
            Node right = neighbours.get(1).getFirst();
            Node left = neighbours.get(1).getSecond();

            if (right != null && left != null) {
                return (right.parent == clientNode.parent) ? right.client : left.client;
            }

            nearestNode = (right != null) ? right : left;
        }

        Iterator<Integer> iterator = neighbours.keySet().iterator();
        Node secondNearest = null, thirdNearest = null;

        while (iterator.hasNext() && (secondNearest == null || thirdNearest == null)) {
            Integer key = iterator.next();

            secondNearest = (secondNearest == null) ? neighbours.get(key).getFirst() : secondNearest;
            thirdNearest = (thirdNearest == null) ? neighbours.get(key).getSecond() : thirdNearest;
        }

        List<Node> candidates = new ArrayList<>();
        candidates.add(nearestNode);
        candidates.add(secondNearest);
        candidates.add(thirdNearest);

        Node nearest = findNearestNode(clientNode, candidates);

        if (nearest == null) {
            if (alternativeSibling == null)
                throw new UnknownError("LevelTree: Can't find sibling client");

            return alternativeSibling.client;
        } else {
            return nearest.client;
        }
    }

    public LevelTreeInformation getTreeInformation() {
        if (root == null)
            throw new NullPointerException("LevelTree: root = null!");

        LevelTreeInformation treeInfo = new LevelTreeInformation();
        treeInfo.setTreeHeight(this.height);
        treeInfo.setNumberOfClients(this.numberOfClients);

        LevelTreeNodeInformation rootInfo = new LevelTreeNodeInformation();

        rootInfo.setNodeID(0);
        rootInfo.setParentNodeID(null);
        rootInfo.setNodePublicKey(root.publicKey);
        rootInfo.setNodeClient(root.client);
        rootInfo.setIndex(root.id);

        Integer leftChildID = null;
        Integer rightChildID = null;

        if (root.left != null) {
            leftChildID = (root.left.isLeaf()) ? root.left.id : -1;
            collectTreeInformation(root.left, leftChildID, 0, treeInfo);
        }

        if (root.right != null) {
            rightChildID = (root.right.isLeaf()) ? root.right.id : -2;
            collectTreeInformation(root.right, rightChildID, 0, treeInfo);
        }

        rootInfo.setLeftChildNodeID(leftChildID);
        rootInfo.setRightChildNodeID(rightChildID);

        treeInfo.addNodeInformation(rootInfo);

        return treeInfo;
    }

    public BranchInformation getBranchInformation(Client client) {
        if (root == null)
            throw new NullPointerException("LevelTree: root = null!");

        if (!clientsNodes.containsKey(client))
            throw new IllegalArgumentException("LevelTree: Tree does not contain client + " + client);

        Node clientNode = clientsNodes.get(client);

        return collectBranchInformation(clientNode);
    }

    public void setMasterClientData(Client client, BigInteger secretKey, BigInteger publicKey) {
        this.masterClient = client;

        Node masterClientNode = clientsNodes.get(masterClient);

        masterClientNode.secretKey = secretKey;
        masterClientNode.publicKey = publicKey;
    }

    public Pair<BigInteger, BigInteger> getLevelGroupKeys() {
        return new Pair<>(root.secretKey, root.publicKey);
    }

    public void clear() {
        root = null;

        numberOfClients = 0;
        height = 0;

        clientsNodes.clear();
        nodesClients.clear();
    }


    @Override
    public String toString() {
        return "LevelTree: (" + root.secretKey + ", " + root.publicKey + ")";
    }

    private void collectTreeInformation(Node node, Integer ID, Integer parentNodeID, LevelTreeInformation treeInfo) {
        LevelTreeNodeInformation nodeInfo = new LevelTreeNodeInformation();

        nodeInfo.setNodeID(ID);
        nodeInfo.setParentNodeID(parentNodeID);
        nodeInfo.setNodePublicKey(node.publicKey);
        nodeInfo.setNodeClient(node.client);
        nodeInfo.setIndex(node.id);

        Integer leftChildNodeID = null;
        Integer rightChildNodeID = null;

        if (node.left != null) {
            leftChildNodeID = (node.left.id > 0) ? node.left.id : 2 * ID - 1;
            collectTreeInformation(node.left, leftChildNodeID, ID, treeInfo);
        }

        if (node.right != null) {
            rightChildNodeID = (node.right.id > 0) ? node.right.id : 2 * ID - 2;
            collectTreeInformation(node.right, rightChildNodeID, ID, treeInfo);
        }

        nodeInfo.setLeftChildNodeID(leftChildNodeID);
        nodeInfo.setRightChildNodeID(rightChildNodeID);

        treeInfo.addNodeInformation(nodeInfo);
    }

    private Node setChangedKeys(BranchInformation branchInfo) {
        int sponsorNodeID = branchInfo.getBranchMasterClientNodeID();
        Node current = null;
        int currentNodeInfoID = sponsorNodeID;

        for (Node node : nodesClients.keySet()) {
            if (sponsorNodeID == node.id)
                current = node;
        }

        Node sponsorNode = current;

        while (current != null) {
            BranchNodeInformation nodeInfo = branchInfo.getNodeInformation(currentNodeInfoID);

            current.publicKey = nodeInfo.getPublicKey();
            currentNodeInfoID = nodeInfo.getParentNodeID();
            current = current.parent;
        }

        return sponsorNode;
    }

    private BranchInformation collectBranchInformation(Node leaf) {
        BranchInformation branchInfo = new BranchInformation();
        Node currentNode = leaf;
        int index = -1;

        while (currentNode != root) {
            int id = (currentNode.id > 0) ? currentNode.id : index--;

            int parentNodeID = (currentNode.parent == root) ? 0 : index;

            branchInfo.addNodeInfo(id, parentNodeID, currentNode.publicKey);

            currentNode = currentNode.parent;
        }

        branchInfo.addNodeInfo(0, 0, root.publicKey);

        return branchInfo;
    }

    private int getLCALevel(Node leaf1, Node leaf2) {
        if (leaf1 == null || leaf2 == null)
            return Integer.MAX_VALUE;

        Node p1 = leaf1;
        Node p2 = leaf2;
        int level = 0;

        while (p1 != p2) {
            p1 = p1.parent;
            p2 = p2.parent;
            ++level;
        }

        return level;
    }

    private Node findLCA(Node node1, Node node2) {
        Node p1 = node1;
        Node p2 = node2;

        if (p1.client == null && p2.client == null)  { // nodes are not leafs
            putPointersOnEqualHeight(p1, p2);
        }

        while (p1 != p2) {
            p1 = p1.parent;
            p2 = p2.parent;
        }

        return p1;
    }

    private void putPointersOnEqualHeight(Node p1, Node p2) {
        int p1_depth = findDepth(p1);
        int p2_depth = findDepth(p2);

        if (p1_depth == p2_depth)
            return;

        if (p1_depth > p2_depth)
            movePointerUp(p1, p1_depth - p2_depth);
        else
            movePointerUp(p2, p2_depth - p1_depth);
    }

    private int findDepth(Node node) {
        int depth = findDepth(root, node, 0);

        if (depth == Integer.MAX_VALUE)
            throw new IllegalArgumentException("LevelTree: can't find depth of node " + node);

        return depth;
    }

    private int findDepth(Node currentNode, Node targetNode, int currentDepth) {
        if (currentNode == null)
            return Integer.MAX_VALUE;

        if (currentNode == targetNode)
            return currentDepth;

        int leftSubtreeResult = findDepth(currentNode.left, targetNode, currentDepth + 1);
        int rightSubtreeResult = findDepth(currentNode.right, targetNode, currentDepth + 1);

        return Math.min(leftSubtreeResult, rightSubtreeResult);
    }

    private void movePointerUp(Node p, int steps) {
        while (steps > 0) {
            p = p.parent;
            --steps;
        }
    }

    private Node findNearestNode(Node baseNode, List<Node> nodes) {
        Integer minLCALevel = Integer.MAX_VALUE;
        Node nearestNode = null;

        for (Node node : nodes) {
            int lcaLevel = getLCALevel(baseNode, node);

            if (lcaLevel < minLCALevel) {
                nearestNode = node;
                minLCALevel = lcaLevel;
            }
        }

        return nearestNode;
    }

    private void expandTree() {
        if (root == null)
            throw new NullPointerException("LevelTree: root = null!");

        Node new_root = new Node();
        new_root.left = root;
        new_root.right = cloneSubTree(root);

        new_root.left.parent = new_root;
        new_root.right.parent = new_root;

        root = new_root;
        ++height;
    }

    private Node cloneSubTree(Node node) {
        if (node == null)
            return null;

        Node new_node = new Node();

        if (node.isLeaf()) {
            new_node.id = (int)(node.id + Math.pow(2, height - 1));
            nodesClients.put(new_node, null);
        }

        new_node.left = cloneSubTree(node.left);
        new_node.right = cloneSubTree(node.right);

        if (new_node.left != null)
            new_node.left.parent = new_node;

        if (new_node.right != null)
            new_node.right.parent = new_node;

        return new_node;
    }

    private void updateKeysInBranch(Node node) {
        Node currentNode = node;
        Node left, right;

        while (currentNode != null) {
            left = currentNode.left;
            right = currentNode.right;

            if (left == null || right == null)
                throw new NullPointerException("LevelTree: failed to update keys.");

            BigInteger secretKey = (left.secretKey != null) ? left.secretKey : right.secretKey;
            BigInteger publicKey = (left.secretKey != null) ? right.publicKey : left.publicKey;

            if (secretKey == null) {            // both children are nulls -> set nulls
                currentNode.publicKey = null;
                currentNode.secretKey = null;
            } else if (publicKey == null) {     // one of children is null -> copy non null child
                publicKey = (left.publicKey != null) ? left.publicKey : right.publicKey;
                currentNode.publicKey = publicKey;
                currentNode.secretKey = secretKey;
            } else {                            // both children are present -> calculate new keys
                secretKey = publicKey.modPow(secretKey, masterClient.getP());
                publicKey = masterClient.getG().modPow(secretKey, masterClient.getP());

                currentNode.publicKey = publicKey;
                currentNode.secretKey = secretKey;
            }

            currentNode = currentNode.parent;
        }
    }

    private void cleanBranchKeys(Node leaf) {
        Node current = leaf.parent;

        while (current != null) {
            if (current.left.publicKey == null && current.right.publicKey == null) {
                current.publicKey = null;
                current.secretKey = null;
            }

            current = current.parent;
        }
    }

    private void initialize(Client client) {
        root = new Node();

        root.id = 1;
        root.publicKey = client.getPublicKey();
        root.client = client;

        height = 1;

        nodesClients = new TreeMap<>((Comparator<Node>) (node1, node2) -> node1.id - node2.id);
        nodesClients.put(root, client);

        clientsNodes = new HashMap<>();
        clientsNodes.put(client, root);

        numberOfClients = 1;

        masterClient = client;
    }

    private Node buildSubTree(LevelTreeInformation treeInfo, Integer currentID, Node parent) {
        LevelTreeNodeInformation nodeInfo = treeInfo.getNodeInformation(currentID);

        Node node = new Node(nodeInfo);
        node.parent = parent;

        if (nodeInfo.getLeftChildNodeID() != null) {
            node.left = buildSubTree(treeInfo, nodeInfo.getLeftChildNodeID(), node);
        }

        if (nodeInfo.getRightChildNodeID() != null) {
            node.right = buildSubTree(treeInfo, nodeInfo.getRightChildNodeID(), node);
        }

        if (nodeInfo.getNodeClient() != null) {
            clientsNodes.put(nodeInfo.getNodeClient(), node);
        }

        if (nodeInfo.getIndex() > 0) {
            nodesClients.put(node, nodeInfo.getNodeClient());
        }

        return node;
    }
}
