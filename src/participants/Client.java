package participants;

import dhtree.BranchInformation;
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

    private Long secretKey;
    private Long publicKey;

    private HierarchyTree hTree;
    private DHTree levelTree;

    public Client() {
        id = -1;
        levelInHierarchy = -1;

        p = -1;
        g = -1;

        secretKey = null;
        publicKey = null;

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

        levelTree.setMasterClientKeys(secretKey, publicKey);
    }

    public void joinGroup(Group group) {
        Client contact = sendGroupJoinRequest(group);
        ClientJoinAnswerMessage groupInfo = sendClientJoinRequest(contact);

        setGroupParameters(groupInfo);

        updateLevelTreeStructure(this, ActionType.JOIN);

        levelTree.setMasterClientKeys(secretKey, publicKey);

        updateKeys();

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

    public long getP() {
        return p;
    }

    public long getG() {
        return g;
    }

    public void updateLevelTreeStructure(Client client, ActionType action) {
        if (action == ActionType.JOIN)
            levelTree.addClient(client);
        else if (action == ActionType.LEAVE)
            levelTree.removeClient(client);
    }

    public void updateKeys() {
        levelTree.updateKeys(this);
    }

    public void updateKeys(BranchInformation branchInfo) {
        this.levelTree.updateKeys(branchInfo);
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
        answer.levelTreeInfo = levelTree.getTreeInformation();
        answer.hierarchyTreeInfo = generateHierarchyTreeInformation();

        return answer;
    }

    private void sendKeysUpdateMessage(Group group) {
        KeysUpdateMessage updateMessage = new KeysUpdateMessage();
        updateMessage.clientId = this.id;
        updateMessage.levelInHierarchy = this.levelInHierarchy;
        updateMessage.changedBranchInformation = levelTree.getBranchInformation(this);

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

        secretKey = (long) rand.nextInt(10);
        publicKey = (long) Math.pow(g, secretKey) % p;
    }
}
