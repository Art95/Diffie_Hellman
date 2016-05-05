package dhtree;

import participants.Client;
import util.Pair;

import java.util.*;

/**
 * Created by Artem on 03.05.2016.
 */
public class DHTree {
    private class DHNode {
        private int id;

        private Long secretKey;
        private Long publicKey;

        private DHNode parent, left, right;

        private Client client;

        public DHNode() {
            id = -1;

            secretKey = null;
            publicKey = null;

            parent = null;
            left = null;
            right = null;

            client = null;
        }

        public DHNode(TreeNodeInformation nodeInfo) {
            id = nodeInfo.getNodeID();

            secretKey = null;
            publicKey = nodeInfo.getNodePublicKey();

            parent = null;
            left = null;
            right = null;

            client = nodeInfo.getNodeClient();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DHNode)) return false;

            DHNode dhNode = (DHNode) o;

            return id == dhNode.id;

        }

        @Override
        public int hashCode() {
            return id;
        }

        public void clear() {
            client = null;

            secretKey = null;
            publicKey = null;
        }
    }

    private DHNode root;
    private int height;

    private Map<DHNode, Client> nodesClients;
    private Map<Client, DHNode> clientsNodes;

    private int numberOfClients;

    private Client masterClient;

    public DHTree() {
        root = null;
        height = 0;

        nodesClients = new HashMap<>();
        clientsNodes = new HashMap<>();

        numberOfClients = 0;

        masterClient = null;
    }

    public DHTree(Client client) {
        initialize(client);
    }

    public DHTree(TreeInformation treeInfo) {
        this.clientsNodes = new HashMap<>();
        this.nodesClients = new HashMap<>();

        this.height = treeInfo.getTreeHeight();
        this.numberOfClients = treeInfo.getNumberOfClients();

        TreeNodeInformation rootInfo = treeInfo.getNodeInformation(0);
        root = new DHNode(rootInfo);

        if (rootInfo.getLeftChildNodeID() != null) {
            root.left = buildSubTree(treeInfo, rootInfo.getLeftChildNodeID(), root);
        }

        if (rootInfo.getRightChildNodeID() != null) {
            root.right = buildSubTree(treeInfo, rootInfo.getRightChildNodeID(), root);
        }

        if (rootInfo.getNodeID() > 0) {
            clientsNodes.put(rootInfo.getNodeClient(), root);
            nodesClients.put(root, rootInfo.getNodeClient());
        }
    }

    public void addClient(Client client) {
        if (root == null) {
            initialize(client);
            return;
        }

        if (numberOfClients >= Math.pow(2, height - 1)) {
            expandTree();
        }

        for (DHNode node : nodesClients.keySet()) {
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
            throw new NullPointerException("DHTree: root = null!");

        if (!clientsNodes.containsKey(leaving_client))
            throw new IllegalArgumentException("DHTree: does not contain client " + leaving_client);

        DHNode holdingNode = clientsNodes.get(leaving_client);

        nodesClients.put(holdingNode, null);
        clientsNodes.remove(leaving_client);

        holdingNode.clear();

        --numberOfClients;
    }
    
    public void updateKeys(Client client) {
        if (root == null)
            throw new NullPointerException("DHTree: root = null!");

        if (!clientsNodes.containsKey(client))
            throw new IllegalArgumentException("DHTree: Tree does not contain client + " + client);

        DHNode clientNode = clientsNodes.get(client);

        updateKeysInBranch(clientNode.parent);
    }

    public void updateKeys(BranchInformation branchInfo) {
        DHNode sponsorNode = setChangedKeys(branchInfo);
        DHNode masterClientNode = clientsNodes.get(masterClient);

        DHNode lca = findLCA(sponsorNode, masterClientNode);

        updateKeysInBranch(lca);
    }

    public Client findSiblingClient(Client client) {
        int separator = (int) Math.pow(2, height - 1) / 2;
        DHNode clientNode = clientsNodes.get(client);
        DHNode alternativeSibling = null;

        boolean leftHalf = clientNode.id <= separator;

        Map<Integer, Pair<DHNode, DHNode>> neighbours = new TreeMap<>();

        for (Client clnt : clientsNodes.keySet()) {
            DHNode node = clientsNodes.get(clnt);

            if ((leftHalf && node.id <= separator) || (!leftHalf && node.id > separator)) {
                Integer distance = Math.abs(node.id - clientNode.id);

                if (!neighbours.containsKey(distance))
                    neighbours.put(distance, new Pair<>(node, null));
                else
                    neighbours.get(distance).setSecond(node);
            } else if (alternativeSibling == null)
                alternativeSibling = node;
        }

        DHNode nearestNode = null;

        if (neighbours.containsKey(1)) {
            DHNode right = neighbours.get(1).getFirst();
            DHNode left = neighbours.get(1).getSecond();

            if (right != null && left != null) {
                return (right.parent == clientNode.parent) ? right.client : left.client;
            }

            nearestNode = (right != null) ? right : left;
        }

        Iterator<Integer> iterator = neighbours.keySet().iterator();
        DHNode secondNearest = null, thirdNearest = null;

        while (iterator.hasNext() && (secondNearest == null || thirdNearest == null)) {
            Integer key = iterator.next();

            secondNearest = (secondNearest == null) ? neighbours.get(key).getFirst() : secondNearest;
            thirdNearest = (thirdNearest == null) ? neighbours.get(key).getSecond() : thirdNearest;
        }

        List<DHNode> candidates = new ArrayList<>();
        candidates.add(nearestNode);
        candidates.add(secondNearest);
        candidates.add(thirdNearest);

        DHNode nearest = findNearestNode(clientNode, candidates);

        if (nearest == null) {
            if (alternativeSibling == null)
                throw new UnknownError("DHTree: Can't find sibling client");

            return alternativeSibling.client;
        } else {
            return nearest.client;
        }
    }

    public TreeInformation getTreeInformation() {
        if (root == null)
            throw new NullPointerException("DHTree: root = null!");

        TreeInformation treeInfo = new TreeInformation();
        treeInfo.setTreeHeight(this.height);
        treeInfo.setNumberOfClients(this.numberOfClients);

        TreeNodeInformation rootInfo = new TreeNodeInformation();

        rootInfo.setNodeID(0);
        rootInfo.setParentNodeID(null);
        rootInfo.setNodePublicKey(root.publicKey);
        rootInfo.setNodeClient(root.client);

        Integer leftChildID = null;
        Integer rightChildID = null;

        if (root.left != null) {
            leftChildID = (root.left.id > 0) ? root.left.id : -1;
            collectTreeInformation(root.left, leftChildID, 0, treeInfo);
        }

        if (root.right != null) {
            rightChildID = (root.right.id > 0) ? root.right.id : -2;
            collectTreeInformation(root.right, rightChildID, 0, treeInfo);
        }

        rootInfo.setLeftChildNodeID(leftChildID);
        rootInfo.setRightChildNodeID(rightChildID);

        treeInfo.addNodeInformation(rootInfo);

        return treeInfo;
    }

    public BranchInformation getBranchInformation(Client client) {
        if (root == null)
            throw new NullPointerException("DHTree: root = null!");

        if (!clientsNodes.containsKey(client))
            throw new IllegalArgumentException("DHTree: Tree does not contain client + " + client);

        DHNode clientNode = clientsNodes.get(client);

        return collectBranchInformation(clientNode);
    }

    public void setMasterClientKeys(Long secretKey, Long publicKey) {
        DHNode masterClientNode = clientsNodes.get(masterClient);

        masterClientNode.secretKey = secretKey;
        masterClientNode.publicKey = publicKey;
    }

    public void clear() {
        root = null;

        numberOfClients = 0;
        height = 0;

        clientsNodes.clear();
        nodesClients.clear();
    }

    private void collectTreeInformation(DHNode node, Integer ID, Integer parentNodeID, TreeInformation treeInfo) {
        TreeNodeInformation nodeInfo = new TreeNodeInformation();

        nodeInfo.setNodeID(ID);
        nodeInfo.setParentNodeID(parentNodeID);
        nodeInfo.setNodePublicKey(node.publicKey);
        nodeInfo.setNodeClient(node.client);

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

    private DHNode setChangedKeys(BranchInformation branchInfo) {
        int sponsorNodeID = branchInfo.getBranchMasterClientNodeID();
        DHNode current = null;
        int currentNodeInfoID = sponsorNodeID;

        for (DHNode node : nodesClients.keySet()) {
            if (sponsorNodeID == node.id)
                current = node;
        }

        DHNode sponsorNode = current;

        while (current != null) {
            BranchNodeInformation nodeInfo = branchInfo.getNodeInformation(currentNodeInfoID);

            current.publicKey = nodeInfo.getPublicKey();

            currentNodeInfoID = nodeInfo.getParentNodeID();
            current = current.parent;
        }

        return sponsorNode;
    }

    private BranchInformation collectBranchInformation(DHNode leaf) {
        BranchInformation branchInfo = new BranchInformation();
        DHNode currentNode = leaf;
        int index = -1;

        while (currentNode != root) {
            int id = (currentNode.id > 0) ? currentNode.id : index--;

            branchInfo.addNodeInfo(id, index, currentNode.publicKey);

            currentNode = currentNode.parent;
        }

        branchInfo.addNodeInfo(0, 0, root.publicKey);

        return branchInfo;
    }

    private int getLCALevel(DHNode leaf1, DHNode leaf2) {
        if (leaf1 == null || leaf2 == null)
            return Integer.MAX_VALUE;

        DHNode p1 = leaf1;
        DHNode p2 = leaf2;
        int level = 0;

        while (p1 != p2) {
            p1 = p1.parent;
            p2 = p2.parent;
            ++level;
        }

        return level;
    }

    private DHNode findLCA(DHNode node1, DHNode node2) {
        DHNode p1 = node1;
        DHNode p2 = node2;

        if (p1.client == null && p2.client == null)  { // nodes are not leafs
            putPointersOnEqualHeight(p1, p2);
        }

        while (p1 != p2) {
            p1 = p1.parent;
            p2 = p2.parent;
        }

        return p1;
    }

    private void putPointersOnEqualHeight(DHNode p1, DHNode p2) {
        int p1_depth = findDepth(p1);
        int p2_depth = findDepth(p2);

        if (p1_depth == p2_depth)
            return;

        if (p1_depth > p2_depth)
            movePointerUp(p1, p1_depth - p2_depth);
        else
            movePointerUp(p2, p2_depth - p1_depth);
    }

    private int findDepth(DHNode node) {
        int depth = findDepth(root, node, 0);

        if (depth == Integer.MAX_VALUE)
            throw new IllegalArgumentException("DHTree: can't find depth of node " + node);

        return depth;
    }

    private int findDepth(DHNode currentNode, DHNode targetNode, int currentDepth) {
        if (currentNode == null)
            return Integer.MAX_VALUE;

        if (currentNode == targetNode)
            return currentDepth;

        int leftSubtreeResult = findDepth(currentNode.left, targetNode, currentDepth + 1);
        int rightSubtreeResult = findDepth(currentNode.right, targetNode, currentDepth + 1);

        return Math.min(leftSubtreeResult, rightSubtreeResult);
    }

    private void movePointerUp(DHNode p, int steps) {
        while (steps > 0) {
            p = p.parent;
            --steps;
        }
    }

    private DHNode findNearestNode(DHNode baseNode, List<DHNode> nodes) {
        Integer minLCALevel = Integer.MAX_VALUE;
        DHNode nearestNode = null;

        for (DHNode node : nodes) {
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
            throw new NullPointerException("DHTree: root = null!");

        DHNode new_root = new DHNode();
        new_root.left = root;
        new_root.right = cloneSubTree(root);

        new_root.left.parent = new_root;
        new_root.right.parent = new_root;

        root = new_root;
        ++height;
    }

    private DHNode cloneSubTree(DHNode node) {
        if (node == null)
            return null;

        DHNode new_node = new DHNode();

        if (node.id > 0) {
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

    private void updateKeysInBranch(DHNode node) {
        DHNode currentNode = node;
        DHNode left, right;

        while (currentNode != null) {
            left = currentNode.left;
            right = currentNode.right;

            if (left == null || right == null)
                throw new NullPointerException("DHTree: failed to update keys.");

            Long secretKey = (left.secretKey != null) ? left.secretKey : right.secretKey;
            Long publicKey = (left.secretKey != null) ? right.publicKey : left.publicKey;

            if (secretKey == null) {            // both children are nulls -> set nulls
                currentNode.secretKey = null;
                currentNode.publicKey = null;
            } else if (publicKey == null) {     // one of children is null -> copy non null child
                currentNode.secretKey = secretKey;
                currentNode.publicKey = (left.publicKey != null) ? left.publicKey : right.publicKey;
            } else {                            // both children are present -> calculate new keys
                currentNode.secretKey = (long) Math.pow(publicKey, secretKey) % masterClient.getP();
                currentNode.secretKey = (long) Math.pow(masterClient.getG(), currentNode.secretKey) % masterClient.getP();
            }

            currentNode = currentNode.parent;
        }
    }

    private void initialize(Client client) {
        root = new DHNode();

        root.id = 1;
        root.publicKey = client.getPublicKey();
        root.client = client;

        height = 1;

        nodesClients = new HashMap<>();
        nodesClients.put(root, client);

        clientsNodes = new HashMap<>();
        clientsNodes.put(client, root);

        numberOfClients = 1;

        masterClient = client;
    }

    private DHNode buildSubTree(TreeInformation treeInfo, Integer currentID, DHNode parent) {
        TreeNodeInformation nodeInfo = treeInfo.getNodeInformation(currentID);

        DHNode node = new DHNode(nodeInfo);
        node.parent = parent;

        if (nodeInfo.getLeftChildNodeID() != null) {
            node.left = buildSubTree(treeInfo, nodeInfo.getLeftChildNodeID(), node);
        }

        if (nodeInfo.getRightChildNodeID() != null) {
            node.right = buildSubTree(treeInfo, nodeInfo.getRightChildNodeID(), node);
        }

        if (nodeInfo.getNodeID() > 0) {
            clientsNodes.put(nodeInfo.getNodeClient(), node);
            nodesClients.put(node, nodeInfo.getNodeClient());
        }

        return node;
    }
}
