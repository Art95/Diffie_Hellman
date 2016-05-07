package participants;

import dhtree.DHTree;
import hierarchytree.HierarchyTree;
import messages.ClientJoinAnswerMessage;
import messages.KeysUpdateMessage;
import util.ActionType;
import util.Pair;

import java.util.Random;

/**
 * Created by Artem on 02.05.2016.
 */
public class Client {
    private int ID;
    private int levelInHierarchy;

    private long p;
    private long g;

    private Long secretKey;
    private Long publicKey;

    private HierarchyTree hierarchyTree;
    private DHTree levelTree;

    private Group group;

    public Client() {
        ID = -1;
        levelInHierarchy = -1;

        p = -1;
        g = -1;

        secretKey = null;
        publicKey = null;

        hierarchyTree = null;
        levelTree = null;

        group = null;
    }

    public Client(int ID, int levelInHierarchy) {
        this.ID = ID;
        this.levelInHierarchy = levelInHierarchy;

        generateParameters();
        generateKeys();

        levelTree = new DHTree(this);
        hierarchyTree = new HierarchyTree(this);

        levelTree.setMasterClientData(this, secretKey, publicKey);

        Pair<Long, Long> hierarchyLevelKeys = levelTree.getLevelGroupKeys();
        hierarchyTree.setMasterClientHierarchyLevelData(this, hierarchyLevelKeys.getFirst(), hierarchyLevelKeys.getSecond());

        group = null;
    }

    public void joinGroup(Group group) {
        sendGroupJoinRequest(group);

        updateTreesStructure(this, ActionType.JOIN);
        updateKeys();

        sendKeysUpdateMessage(group);

        this.group = group;
    }

    public void leaveGroup(Group group) {
        sendGroupLeaveRequest(group);

        generateParameters();
        generateKeys();

        levelTree.clear();
        levelTree = new DHTree(this);

        hierarchyTree.clear();
        hierarchyTree = new HierarchyTree(this);

        group = null;
    }

    public int getID() {
        return this.ID;
    }

    public int getLevelInHierarchy() {
        return this.levelInHierarchy;
    }

    public long getPublicKey() {
        return this.publicKey;
    }

    public long getP() {
        return p;
    }

    public long getG() {
        return g;
    }

    public void updateTreesStructure(Client client, ActionType action) {
        if (action == ActionType.JOIN) {
            if (client.levelInHierarchy == this.levelInHierarchy) {
                Client sponsor = findJoinSponsor(client);

                if (sponsor.equals(this)) {
                    sendGroupInformation(client);
                }

                levelTree.addClient(client);
            }

            hierarchyTree.addClient(client);
        } else if (action == ActionType.LEAVE) {
            Client sponsor = findLeaveSponsor(client);

            if (client.levelInHierarchy == this.levelInHierarchy) {
                levelTree.removeClient(client);
            }

            hierarchyTree.removeClient(client);

            if (this.equals(sponsor)) {
                updateKeys();
                sendKeysUpdateMessage(group);
            }
        }
    }

    public void updateKeys() {
        levelTree.setMasterClientData(this, secretKey, publicKey);

        levelTree.updateKeys();

        Pair<Long, Long> hierarchyLevelKeys = levelTree.getLevelGroupKeys();
        hierarchyTree.setMasterClientHierarchyLevelData(this, hierarchyLevelKeys.getFirst(), hierarchyLevelKeys.getSecond());

        hierarchyTree.updateKeys();
    }

    public void updateKeys(KeysUpdateMessage message) {
        if (message.clientID == this.ID)
            return;

        if (message.levelInHierarchy == this.levelInHierarchy) {
            this.levelTree.updateKeys(message.levelTreeChanges);

            Pair<Long, Long> groupKeys = levelTree.getLevelGroupKeys();
            hierarchyTree.setMasterClientHierarchyLevelData(this, groupKeys.getFirst(), groupKeys.getSecond());

            hierarchyTree.updateKeys();
        } else {
            this.hierarchyTree.updateKeys(message.hierarchyTreeChanges);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Client)) return false;

        Client client = (Client) o;

        if (ID != client.ID) return false;
        return levelInHierarchy == client.levelInHierarchy;

    }

    @Override
    public int hashCode() {
        int result = ID;
        result = 31 * result + levelInHierarchy;
        return result;
    }

    private void sendGroupJoinRequest(Group group) {
        group.receiveJoinRequest(this);
    }

    private void sendGroupLeaveRequest(Group group) {
        group.receiveLeaveRequest(this);
    }

    private void sendGroupInformation(Client client) {
        ClientJoinAnswerMessage answer = new ClientJoinAnswerMessage();
        answer.p = this.p;
        answer.g = this.g;
        answer.levelTreeInfo = levelTree.getTreeInformation();
        answer.hierarchyTreeInfo = hierarchyTree.getTreeInformation();

        client.receiveGroupInformation(answer);
    }

    private void receiveGroupInformation(ClientJoinAnswerMessage answer) {
        setGroupParameters(answer);
    }

    private void sendKeysUpdateMessage(Group group) {
        KeysUpdateMessage updateMessage = new KeysUpdateMessage();
        updateMessage.clientID = this.ID;
        updateMessage.levelInHierarchy = this.levelInHierarchy;
        updateMessage.levelTreeChanges = levelTree.getBranchInformation(this);
        updateMessage.hierarchyTreeChanges = hierarchyTree.getBranchInformation(levelInHierarchy);

        group.receiveKeysUpdateRequest(updateMessage);
    }

    private void setGroupParameters(ClientJoinAnswerMessage parameters) {
        this.p = parameters.p;
        this.g = parameters.g;
        this.levelTree = new DHTree(parameters.levelTreeInfo);
        this.hierarchyTree = new HierarchyTree(parameters.hierarchyTreeInfo);
    }

    private Client findJoinSponsor(Client joiningClient) {
        if (hierarchyTree.numberOfParticipantsOnLevel(joiningClient.getLevelInHierarchy()) > 0) {
            if (this.levelInHierarchy == joiningClient.levelInHierarchy)
                return levelTree.findSponsor();
            else
                return new Client();
        } else {
            Integer sponsorID = hierarchyTree.findSponsorID(joiningClient.getLevelInHierarchy());

            if (sponsorID == null)
                return new Client();

            if (this.ID == sponsorID)
                return this;
            else
                return new Client();
        }
    }

    private Client findLeaveSponsor(Client leavingClient) {
        // check if there is at least one more participant on leaving client's level of hierarchy except leaving client
        if (hierarchyTree.numberOfParticipantsOnLevel(leavingClient.getLevelInHierarchy()) > 1) {
            if (this.levelInHierarchy == leavingClient.levelInHierarchy)
                return levelTree.findSiblingClient(leavingClient);
            else
                return new Client();
        } else {
            Integer sponsorID = hierarchyTree.findSponsorID(leavingClient.getLevelInHierarchy());

            if (sponsorID == null || sponsorID != this.ID)
                return new Client();

            return this;
        }
    }

    private void generateParameters() {
        p = 17;
        g = 3;
    }

    private void generateKeys() {
        Random rand = new Random();

        secretKey = (long) rand.nextInt(10);
        publicKey = (long) Math.pow(g, secretKey) % p;
    }
}
