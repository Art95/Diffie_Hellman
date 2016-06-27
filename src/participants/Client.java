package participants;

import leveltree.LevelTree;
import hierarchytree.HierarchyTree;
import messages.ClientJoinAnswerMessage;
import messages.KeysUpdateMessage;
import util.ActionType;
import util.Pair;

import java.math.BigInteger;
import java.security.*;

/**
 * Created by Artem on 02.05.2016.
 */
public class Client {
    private static final int BITS = 256;
    private static final int G_BOUND = 100;

    private int ID;
    private int levelInHierarchy;

    private BigInteger p;
    private BigInteger g;

    private BigInteger secretKey;
    private BigInteger publicKey;

    private HierarchyTree hierarchyTree;
    private LevelTree levelTree;

    private Group group;

    public Client() {
        ID = -1;
        levelInHierarchy = -1;

        p = null;
        g = null;

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

        levelTree = new LevelTree(this);
        hierarchyTree = new HierarchyTree(this);

        levelTree.setMasterClientData(this, secretKey, publicKey);

        Pair<BigInteger, BigInteger> hierarchyLevelKeys = levelTree.getLevelGroupKeys();
        hierarchyTree.setMasterClientHierarchyLevelData(this, hierarchyLevelKeys.getFirst(), hierarchyLevelKeys.getSecond());

        group = null;
    }


    private double seconds = 0;

    public void joinGroup(Group group) {
        sendGroupJoinRequest(group);

        updateTreesStructure(this, ActionType.JOIN);
        updateKeys();

        System.out.println("ID: " + ID + ". Level: " + levelInHierarchy + ". Bits: " + BITS + ". Seconds: " + seconds);

        sendKeysUpdateMessage(group);

        this.group = group;
    }

    public void leaveGroup(Group group) {
        sendGroupLeaveRequest(group);

        generateParameters();
        generateKeys();

        levelTree.clear();
        levelTree = new LevelTree(this);

        levelTree.setMasterClientData(this, secretKey, publicKey);

        hierarchyTree.clear();
        hierarchyTree = new HierarchyTree(this);

        hierarchyTree.setMasterClientHierarchyLevelData(this, secretKey, publicKey);

        this.group = null;
    }

    public int getID() {
        return this.ID;
    }

    public int getLevelInHierarchy() {
        return this.levelInHierarchy;
    }

    public BigInteger getPublicKey() {
        return this.publicKey;
    }

    public BigInteger getP() {
        return p;
    }

    public BigInteger getG() {
        return g;
    }

    public void updateTreesStructure(Client client, ActionType action) {
        if (action == ActionType.JOIN) {
            Client sponsor = findJoinSponsor(client);

            if (this.equals(sponsor)) {
                sendGroupInformation(client);
            }

            if (client.levelInHierarchy == this.levelInHierarchy) {
                levelTree.addClient(client);
            }

            long start = System.nanoTime();
            hierarchyTree.addClient(client);
            long end = System.nanoTime();
            seconds += (end - start) / 1000000000.0;
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

        Pair<BigInteger, BigInteger> hierarchyLevelKeys = levelTree.getLevelGroupKeys();
        hierarchyTree.setMasterClientHierarchyLevelData(this, hierarchyLevelKeys.getFirst(), hierarchyLevelKeys.getSecond());

        long start = System.nanoTime();
        hierarchyTree.updateKeys();
        long end = System.nanoTime();
        seconds += (end - start) / 1000000000.0;
    }

    public void updateKeys(KeysUpdateMessage message) {
        if (message.clientID == this.ID)
            return;

        if (message.levelInHierarchy == this.levelInHierarchy) {
            this.levelTree.updateKeys(message.levelTreeChanges);

            Pair<BigInteger, BigInteger> groupKeys = levelTree.getLevelGroupKeys();
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

    @Override
    public String toString() {
        return "(Client " + ID + ": level = " + levelInHierarchy + ", P = " + p + ", G = " + g +
                ", sKey = " + secretKey + ", pKey = " + publicKey +
                ", " + levelTree + ", " + hierarchyTree + ")";
    }

    private void sendGroupJoinRequest(Group group) {
        group.receiveJoinRequest(this);
    }

    private void sendGroupLeaveRequest(Group group) {
        group.receiveLeaveRequest(this);
    }

    private void sendGroupInformation(Client client) {
        ClientJoinAnswerMessage answer = new ClientJoinAnswerMessage();

        answer.ID = this.ID;
        answer.levelInHierarchy = this.levelInHierarchy;

        answer.p = this.p;
        answer.g = this.g;

        if (client.levelInHierarchy == this.levelInHierarchy)
            answer.levelTreeInfo = levelTree.getTreeInformation();
        else
            answer.levelTreeInfo = null;

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

    private void setGroupParameters(ClientJoinAnswerMessage answerParameters) {
        this.p = answerParameters.p;
        this.g = answerParameters.g;

        generateKeys();

        if (answerParameters.levelInHierarchy == this.levelInHierarchy)
            this.levelTree = new LevelTree(answerParameters.levelTreeInfo);

        this.hierarchyTree = new HierarchyTree(answerParameters.hierarchyTreeInfo);
    }

    private Client findJoinSponsor(Client joiningClient) {
        if (hierarchyTree.numberOfParticipantsOnLevel(joiningClient.getLevelInHierarchy()) > 0) {
            if (this.levelInHierarchy == joiningClient.levelInHierarchy) {
                Client sponsor = levelTree.findSponsor(joiningClient);

                if (sponsor == null)
                    return new Client();
                else
                    return sponsor;
            } else
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
        p = BigInteger.probablePrime(BITS, new SecureRandom());
        g = BigInteger.valueOf(new SecureRandom().nextInt(G_BOUND) + 2);
    }

    private void generateKeys() {
        secretKey = randomBigIntegerInRange(p.subtract(BigInteger.ONE));
        publicKey = g.modPow(secretKey, p);
    }

    private BigInteger randomBigIntegerInRange(BigInteger bound) {
        BigInteger result;

        do {
            result = new BigInteger(bound.bitLength(), new SecureRandom()).add(BigInteger.ONE);
        } while (result.compareTo(bound) >= 0);

        return result;
    }
}
