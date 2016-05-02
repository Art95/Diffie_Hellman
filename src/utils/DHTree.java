package utils;

import participants.Client;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Artem on 10.04.2016.
 */
public class DHTree {
    private class DHNode {
        int level;
        int index;

        DHNode left, right, parent;

        private Client client;

        private long secret_key;
        private long public_key;

        public DHNode() {
            level = -1;
            index = -1;

            left = null;
            right = null;
            parent = null;

            client = null;

            secret_key = -1;
            public_key = -1;
        }

        public boolean isLeaf() {
            if (left == null && right == null) {
                if (client != null)
                    return true;
                else
                    throw new NullPointerException("Leaf with no participants.Person owner");
            }

            return false;
        }
    }

    private DHNode root;

    private Client owner;
    private DHNode ownerLeaf;

    private List<LinkedList<DHNode>> levels;

    public DHTree(Client owner) {
        this.owner = owner;

        root = new DHNode();
        root.level = 0;
        root.index = 0;
        root.client = owner;
        ownerLeaf = root;

        levels = new ArrayList<>();
        levels.add(new LinkedList<>());
        levels.get(0).add(root);
    }

    public void buildTree(LinkedList<Client> clients) {
        root = null;
        levels.clear();

        for (Client client : clients) {
            addClient(client);
        }

        ownerLeaf = findClientLeaf(owner);
    }

    public void addClient(Client client) {
        DHTree client_tree = new DHTree(client);
        this.merge(client_tree);
    }

    public Client removeClient(Client client) {
        DHNode leavingNode = findClientLeaf(client);
        DHNode parent = leavingNode.parent;
        DHNode sibling = (leavingNode == parent.left) ? parent.right : parent.left;

        if (sibling.client == null) {
            throw new NullPointerException("Leaf with no client");
        }

        leavingNode.level = -1;
        parent.level = -1;

        if (parent == root) {
            root = sibling;
        } else {
            if (parent == parent.parent.left) {
                parent.parent.left = sibling;
            } else {
                parent.parent.right = sibling;
            }
        }

        decreaseLevels(sibling);
        removalLevelsUpdate();

        return sibling.client;
    }

    public List<Pair<Pair<Integer, Integer>, Long>> getHostingBranch() {
        List<Pair<Pair<Integer, Integer>, Long>> branch = new ArrayList<>();
        DHNode current = ownerLeaf;

        while (!current.equals(root)) {
            branch.add(new Pair(new Pair(current.level, current.index), current.public_key));
            current = current.parent;
        }

        return branch;
    }

    public void clear() {
        root = null;
        owner = null;
        ownerLeaf = null;
        levels.clear();
    }

    private DHNode findClientLeaf(Client client) {
        DHNode leaf = traverse(root, client);

        if (leaf == null)
            throw new NullPointerException("Tree does not contain client " + owner);

        return leaf;
    }

    private DHNode traverse(DHNode node, Client client) {
        if (node.isLeaf()) {
            if (node.client.equals(client))
                return node;
            else
                return null;
        }

        DHNode left_result = traverse(node.left, client);

        if (left_result != null)
            return left_result;

        return traverse(node.right, client);
    }

    private void merge(DHTree another_tree) {
        if (another_tree.root == null)
            return;

        if (root == null) {
            root = another_tree.root;
            levels = another_tree.levels;
            owner = another_tree.owner;
            ownerLeaf = another_tree.ownerLeaf;

            another_tree.clear();
            return;
        }

        DHNode insertion_node = findInsertionNode(another_tree);

        DHNode new_node = new DHNode();
        new_node.left = insertion_node;
        new_node.right = another_tree.root;
        new_node.parent = insertion_node.parent;
        new_node.level = insertion_node.level;
        new_node.index = insertion_node.index;
        
        another_tree.root.parent = new_node;

        if (insertion_node == root) {
            root = new_node;
        } else {
            if (insertion_node == insertion_node.parent.left)
                insertion_node.parent.left = new_node;
            else
                insertion_node.parent.right = new_node;
        }

        insertion_node.parent = new_node;

        updateLevel(new_node);

        another_tree.clear();
    }

    private DHNode findInsertionNode(DHTree another_tree) {
        DHNode insertion_node = root;

        int i = 0;
        LinkedList<DHNode> level = levels.get(i);
        DHNode possible_insertion_node = level.getLast();

        while (true) {
            DHNode current = level.getLast();

            int max_height = 0;
            int another_tree_height = another_tree.levels.size() - another_tree.root.level;

            do {
                int this_height = levels.size() - current.level;
                max_height = Math.max(max_height, this_height);

                if (max_height > another_tree_height)
                    break;

            } while (!current.equals(level.getLast()));

            if (another_tree_height >= max_height) {
                insertion_node = root;
                break;
            }

            if (isJoinable(possible_insertion_node)) {
                insertion_node = possible_insertion_node;
                break;
            } else {
                possible_insertion_node = level.previous(possible_insertion_node);

                if (possible_insertion_node.equals(level.getLast())) {
                    ++i;

                    if (i >= levels.size())
                        break;

                    level = levels.get(i);
                    possible_insertion_node = level.getLast();
                }
            }
        }

        return insertion_node;
    }

    private boolean isJoinable(DHNode node) {
        int node_height = levels.size() - node.level;

        return node_height + 1 <= levels.size() && node.left == null;
    }

    private void updateLevel(DHNode node) {
        if (node.level >= levels.size())
            levels.add(new LinkedList<>());

        if (node.left != null) {
            levels.get(node.level).replace(node.left, node);
        } else {
            DHNode newNeighbour = findNewNeighbor(node);

            if (newNeighbour == null)
                levels.get(node.level).add(node);
            else
                levels.get(node.level).insertBefore(newNeighbour, node);
        }

        if (node.right != null) {
            node.right.level = node.level + 1;
            updateLevel(node.right);
        }

        if (node.left != null) {
            node.left.level = node.level + 1;
            updateLevel(node.left);
        }
    }

    private void decreaseLevels(DHNode node) {
        if (node == null)
            return;

        node.level = node.level - 1;

        decreaseLevels(node.left);
        decreaseLevels(node.right);
    }

    private void removalLevelsUpdate() {
        LinkedList<DHNode> level;

        for (int i = levels.size() - 1; i >= 0; --i) {
            level = levels.get(i);
            update(level, i);

            if (level.isEmpty())
                levels.remove(i);
        }
    }

    private void update(LinkedList<DHNode> level, int levelIndex) {
        DHNode current = level.getLast();

        while (!level.isEmpty() && !current.equals(level.getFirst())) {
            if (current.level < levelIndex) {
                DHNode newNeighbour = findNewNeighbor(current);

                if (newNeighbour == null) {
                    levels.get(current.level).add(current);
                } else {
                    levels.get(newNeighbour.level).insertBefore(newNeighbour, current);
                }

                level.remove(current);
            }
        }
    }

    private DHNode findNewNeighbor(DHNode node) {
        if (node == null)
            throw new NullPointerException("Can't find neighbour of null");

        if (node == root)
            return null;

        DHNode current = node.parent;

        if (levels.get(current.level).isEmpty() || levels.get(current.level + 1).isEmpty())
            return null;

        if (node == current.left) {
            if (current.right != null) {
                return current.right;
            }
        }

        while (true) {
            current = levels.get(current.level).next(current);

            if (current.left != null)
                return current.left;
            else if (current.right != null)
                return current.right;
        }
    }
}
