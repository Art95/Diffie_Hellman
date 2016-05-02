package participants;

import messages.ClientJoinRequestMessage;
import messages.KeysUpdateMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Artem on 02.05.2016.
 */
public class Group {
    private Map<Integer, HierarchyLevelGroup> hierarchy;

    public Group() {
        hierarchy = new HashMap<>();
    }

    public Client receiveJoinRequest(ClientJoinRequestMessage message) {
        HierarchyLevelGroup clientGroup = hierarchy.get(message.levelInHierarchy);
        return clientGroup.getContactClient();
    }

    public void receiveKeysUpdateMessage(KeysUpdateMessage message) {
        HierarchyLevelGroup level = hierarchy.get(message.levelInHierarchy);

        for (Client client : level.clients) {
            if (client.getID() != message.clientId) {
                client.updateKeys(message.changedBranchInformation);
            }
        }
    }
}
