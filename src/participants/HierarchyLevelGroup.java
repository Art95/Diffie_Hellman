package participants;

import messages.KeysUpdateMessage;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import util.ActionType;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Artem on 02.05.2016.
 */
public class HierarchyLevelGroup {
    public Set<Client> clients;

    public HierarchyLevelGroup() {
        this.clients = new HashSet<>();
    }

    public void receiveJoinRequest(Client new_client) {
        for (Client client : clients) {
            client.updateLevelTreeStructure(new_client, ActionType.JOIN);
        }

        clients.add(new_client);
    }

    public void receiveLeaveRequest(Client leaving_client) {
        clients.remove(leaving_client);

        Client sponsor = getContactClient().findSiblingClient(leaving_client);

        for (Client client : clients) {
            client.updateLevelTreeStructure(leaving_client, ActionType.LEAVE);
        }

        sponsor.updateKeys();
    }

    public void receiveKeysUpdateRequest(KeysUpdateMessage message) {
        for (Client client : clients) {
            if (client.getID() != message.clientId) {
                client.updateKeys(message.changedBranchInformation);
            }
        }
    }

    public Client getContactClient() {
        Iterator<Client> it = clients.iterator();

        if (it.hasNext())
            return it.next();
        else
            throw new NotImplementedException();
    }
}
