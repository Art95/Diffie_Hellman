package participants;


import utils.LinkedList;
import utils.Pair;

import java.util.List;

/**
 * Created by Artem on 11.04.2016.
 */
public abstract class AbstractClient implements Client {
    @Override
    abstract public void joinGroup(HierarchyLevel group);

    @Override
    abstract public void leaveGroup(HierarchyLevel group);

    abstract protected Pair<Long, Long> sendGroupParameters();

    abstract protected LinkedList<Client> sendGroupClients();

    abstract protected void addClient(Client client);

    abstract protected void greetGroup();

    abstract protected void sendUpdatedBranch();

    abstract protected void updateKeys(List<Pair<Pair<Integer, Integer>, Long>> changedBranch);

    abstract protected void sayGoodbye();

    abstract protected void removeClient(Client client);

    abstract protected void resetParameters();

    abstract protected void resetKeys();
}
