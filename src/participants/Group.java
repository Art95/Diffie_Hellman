package participants;

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

    public Client receiveJoinRequest(Client new_client) {
        HierarchyLevelGroup clientGroup = hierarchy.get(new_client.getLevelInHierarchy());
        clientGroup.receiveJoinRequest(new_client);

        return clientGroup.getContactClient();
    }

    public void receiveLeaveRequest(Client leaving_client) {
        HierarchyLevelGroup clientGroup = hierarchy.get(leaving_client.getLevelInHierarchy());
        clientGroup.receiveLeaveRequest(leaving_client);
    }

    public void receiveKeysUpdateRequest(KeysUpdateMessage message) {
        HierarchyLevelGroup clientGroup = hierarchy.get(message.levelInHierarchy);
        clientGroup.receiveKeysUpdateRequest(message);
    }
}
