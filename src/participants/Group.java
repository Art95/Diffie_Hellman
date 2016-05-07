package participants;

import messages.KeysUpdateMessage;
import util.ActionType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Artem on 02.05.2016.
 */
public class Group {
    private Set<Client> clients;

    public Group() {
        clients = new HashSet<>();
    }

    public void receiveJoinRequest(Client new_client) {
        if (clients.contains(new_client))
            throw new IllegalArgumentException("Group already contains client " + new_client);

        for (Client client : clients) {
            client.updateTreesStructure(new_client, ActionType.JOIN);
        }

        clients.add(new_client);
    }

    public void receiveLeaveRequest(Client leaving_client) {
        if (!clients.contains(leaving_client))
            return;
        else
            clients.remove(leaving_client);

        for (Client client : clients) {
            client.updateTreesStructure(leaving_client, ActionType.LEAVE);
        }
    }

    public void receiveKeysUpdateRequest(KeysUpdateMessage message) {
        for (Client client : clients) {
            client.updateKeys(message);
        }
    }
}
