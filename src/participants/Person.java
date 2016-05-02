package participants;

import utils.DHTree;
import utils.LinkedList;
import utils.Pair;

import java.util.List;

/**
 * Created by Artem on 10.04.2016.
 */
public class Person implements Client {
    private DHTree tree;

    private long p, g;

    private long public_key;
    private long secret_key;

    private LinkedList<Client> groupClients;

    public Person() {
        tree = new DHTree(this);
        groupClients = new LinkedList<>();
        groupClients.add(this);

        p = 13;
        g = 5;

        secret_key = 7;
        public_key = (long) Math.pow(g, secret_key);
    }

    public void joinGroup(HierarchyLevel group) {
        Person contact = group.sendContactClient();

        Pair<Long, Long> parameters = contact.sendGroupParameters();
        p = parameters.getFirst();
        g = parameters.getSecond();

        this.groupClients = contact.sendGroupClients();

        resetKeys();

        greetGroup();

        groupClients.add(this);

        tree.buildTree(groupClients);
        tree.updateKeys();

        sendUpdatedBranch();
    }

    public void leaveGroup(HierarchyLevel group) {
        group.removeClient(this);
        groupClients.remove(this);

        sayGoodbye();

        groupClients.clear();
        tree.clear();

        resetParameters();
        resetKeys();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;

        Person client = (Person) o;

        if (p != client.p) return false;
        if (g != client.g) return false;
        if (public_key != client.public_key) return false;
        if (secret_key != client.secret_key) return false;
        if (tree != null ? !tree.equals(client.tree) : client.tree != null) return false;
        return groupClients != null ? groupClients.equals(client.groupClients) : client.groupClients == null;

    }

    @Override
    public int hashCode() {
        int result = tree != null ? tree.hashCode() : 0;
        result = 31 * result + (int) (p ^ (p >>> 32));
        result = 31 * result + (int) (g ^ (g >>> 32));
        result = 31 * result + (int) (public_key ^ (public_key >>> 32));
        result = 31 * result + (int) (secret_key ^ (secret_key >>> 32));
        result = 31 * result + (groupClients != null ? groupClients.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "participants.Person{" +
                "p=" + p +
                ", g=" + g +
                ", public_key=" + public_key +
                ", secret_key=" + secret_key +
                '}';
    }

    protected Pair<Long, Long> sendGroupParameters() {
        return new Pair<>(p, g);
    }

    protected LinkedList<Client> sendGroupClients() {
        return this.groupClients;
    }

    protected void addClient(Client client) {
        groupClients.add(client);
        tree.addClient(client);
    }

    protected void greetGroup() {
        for (Client client : groupClients) {
            client.addClient(this);
        }
    }

    protected void sendUpdatedBranch() {
        List<Pair<Pair<Integer, Integer>, Long>> branchInfo = tree.getHostingBranch();

        for (Client client : groupClients) {
            if (client != this)
                client.updateKeys(branchInfo);
        }
    }

    protected void updateKeys(List<Pair<Pair<Integer, Integer>, Long>> changedBranch) {
        tree.updateKeys(changedBranch);
    }

    protected void sayGoodbye() {
        for (Client client : groupClients) {
            client.removeClient(this);
        }
    }

    protected void removeClient(Client client) {
        Client sponsor = tree.removeClient(client);
        groupClients.remove(client);

        if (sponsor.equals(this)) {
            tree.updateKeys();
            sendUpdatedBranch();
        }
    }

    protected void resetParameters() {
        p = 11;
        g = 3;
    }

    protected void resetKeys() {
        secret_key = 7;
        public_key = (long) Math.pow(g, secret_key);
    }
}
