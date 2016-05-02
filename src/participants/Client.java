package participants;

import messages.ClientJoinAnswerMessage;
import messages.ClientJoinRequestMessage;
import messages.KeysUpdateMessage;

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
    private LevelTree lTree;

    public Client() {
        id = -1;
        levelInHierarchy = -1;

        p = -1;
        g = -1;

        secretKey = -1;
        publicKey = -1;

        hTree = null;
        lTree = null;
    }

    public Client(int id, int levelInHierarchy) {
        this.id = id;
        this.levelInHierarchy = levelInHierarchy;

        generateParameters();
        generateKeys();

        lTree = new LevelTree();
        hTree = new HierarchyTree();
    }

    public void joinGroup(Group group) {
        Client contact = sendGroupJoinRequest(group);
        ClientJoinAnswerMessage groupInfo = sendClientJoinRequest(contact);

        setGroupParameters(groupInfo);

        updateLevelTreeStructure();
        updateLevelTreeKeys();
        updateHierarchyTreeKeys();

        sendKeysUpdateMessage(group);
    }

    public int getID() {
        return this.id;
    }

    private Client sendGroupJoinRequest(Group group) {
        ClientJoinRequestMessage clientInformation = new ClientJoinRequestMessage();
        clientInformation.clientId = this.id;
        clientInformation.levelInHierarchy = this.levelInHierarchy;

        return group.receiveJoinRequest(clientInformation);
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

        group.receiveKeysUpdateMessage(updateMessage);
    }

    private void setGroupParameters(ClientJoinAnswerMessage parameters) {
        this.p = parameters.p;
        this.g = parameters.g;
        this.lTree = new LevelTree(parameters.levelTreeInfo);
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
