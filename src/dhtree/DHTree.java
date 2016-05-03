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

        private long secretKey;
        private long publicKey;

        private DHNode parent, left, right;

        private Client client;

        public DHNode() {
            id = -1;
            secretKey = -1;
            publicKey = -1;

            parent = null;
            left = null;
            right = null;

            client = null;
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
    }

    private DHNode root;
    private int height;
    private Map<DHNode, Client> nodesClients;
    private Map<Client, DHNode> clientsNodes;
    private int clientsNumber;

    public DHTree() {
        root = null;
        height = 0;
        nodesClients = new HashMap<>();
        clientsNodes = new HashMap<>();
        clientsNumber = 0;
    }

    public DHTree(Client client) {
        initialize(client);
    }

    public void addClient(Client client) {
        if (root == null) {
            initialize(client);
            return;
        }

        if (clientsNumber >= Math.pow(2, height - 1)) {
            expandTree();
        }

        for (DHNode node : nodesClients.keySet()) {
            if (nodesClients.get(node) == null) {
                node.client = client;

                nodesClients.put(node, client);
                clientsNodes.put(client, node);

                ++clientsNumber;

                return;
            }
        }
    }

    public void removeClient(Client leaving_client) {
        if (root == null)
            throw new NullPointerException("DHTree: root has not been initialized!");

        if (!clientsNodes.containsKey(leaving_client))
            throw new IllegalArgumentException("DHTree: does not contain client " + leaving_client);

        DHNode holdingNode = clientsNodes.get(leaving_client);

        nodesClients.put(holdingNode, null);
        clientsNodes.remove(leaving_client);

        --clientsNumber;
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

    public void clear() {
        root = null;

        clientsNumber = 0;
        height = 0;

        clientsNodes.clear();
        nodesClients.clear();
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
            throw new NullPointerException("DHTree: root has not been initialized!");

        DHNode new_root = new DHNode();
        new_root.left = root;
        new_root.right = cloneTree(root);

        new_root.left.parent = new_root;
        new_root.right.parent = new_root;

        root = new_root;
        ++height;
    }

    private DHNode cloneTree(DHNode node) {
        if (node == null)
            return null;

        DHNode new_node = new DHNode();

        if (node.id > 0) {
            new_node.id = (int)(node.id + Math.pow(2, height - 1));
            nodesClients.put(new_node, null);
        }

        new_node.left = cloneTree(node.left);
        new_node.right = cloneTree(node.right);

        if (new_node.left != null)
            new_node.left.parent = new_node;

        if (new_node.right != null)
            new_node.right.parent = new_node;

        return new_node;
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

        clientsNumber = 1;
    }
}
