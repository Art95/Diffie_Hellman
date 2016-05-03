package participants;

import dhtree.DHTree;
import messages.ClientJoinAnswerMessage;
import messages.KeysUpdateMessage;
import util.ActionType;

import java.util.Random;

/**
 * Created by Artem on 02.05.2016.
 */
public class Client {
    private int id;
    private int levelInHierarchy;

    private long p;
    private long g;

    private long secretKey;
    private long publicKey;

    private HierarchyTree hTree;
    private DHTree levelTree;

    public Client() {
        id = -1;
        levelInHierarchy = -1;

        p = -1;
        g = -1;

        secretKey = -1;
        publicKey = -1;

        hTree = null;
        levelTree = null;
    }

    public Client(int id, int levelInHierarchy) {
        this.id = id;
        this.levelInHierarchy = levelInHierarchy;

        generateParameters();
        generateKeys();

        levelTree = new DHTree(this);
        hTree = new HierarchyTree();
    }

    public void joinGroup(Group group) {
        Client contact = sendGroupJoinRequest(group);
        ClientJoinAnswerMessage groupInfo = sendClientJoinRequest(contact);

        setGroupParameters(groupInfo);

        updateLevelTreeStructure(this, ActionType.JOIN);
        updateKeys(ActionType.JOIN);

        sendKeysUpdateMessage(group);
    }

    public void leaveGroup(Group group) {
        sendGroupLeaveRequest(group);

        generateParameters();
        generateKeys();

        levelTree = new DHTree(this);
    }

    public int getID() {
        return this.id;
    }

    public int getLevelInHierarchy() {
        return this.levelInHierarchy;
    }

    public long getPublicKey() {
        return this.publicKey;
    }

    public void updateLevelTreeStructure(Client client, ActionType action) {
        if (action == ActionType.JOIN)
            levelTree.addClient(client);
        else if (action == ActionType.LEAVE)
            levelTree.removeClient(client);
    }

    public Client findSiblingClient(Client client) {
        return levelTree.findSiblingClient(client);
    }

    private Client sendGroupJoinRequest(Group group) {
        return group.receiveJoinRequest(this);
    }

    private void sendGroupLeaveRequest(Group group) {
        group.receiveLeaveRequest(this);
    }

    private ClientJoinAnswerMessage sendClientJoinRequest(Client client) {
        ClientJoinAnswerMessage answer = client.answerOnJoinRequest();
        return answer;
    }

    private ClientJoinAnswerMessage answerOnJoinRequest() {
        ClientJoinAnswerMessage answer = new ClientJoinAnswerMessage();
        answer.p = this.p;
        answer.g = this.g;
        answer.levelTreeInfo = generateLevelTreeInformation();
        answer.hierarchyTreeInfo = generateHierarchyTreeInformation();

        return answer;
    }

    private void sendKeysUpdateMessage(Group group) {
        KeysUpdateMessage updateMessage = new KeysUpdateMessage();
        updateMessage.clientId = this.id;
        updateMessage.levelInHierarchy = this.levelInHierarchy;
        updateMessage.changedBranchInformation = generateBranchInformation();

        group.receiveKeysUpdateRequest(updateMessage);
    }

    private void setGroupParameters(ClientJoinAnswerMessage parameters) {
        this.p = parameters.p;
        this.g = parameters.g;
        this.levelTree = new DHTree(parameters.levelTreeInfo);
        this.hTree = new HierarchyTree(parameters.hierarchyTreeInfo);
    }

    private void generateParameters() {
        p = 17;
        g = 3;
    }

    private void generateKeys() {
        Random rand = new Random();

        secretKey = rand.nextInt(10);
        publicKey = (long)Math.pow(g, secretKey) % p;
    }
}
