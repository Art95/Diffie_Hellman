package participants;

import utils.LinkedList;

/**
 * Created by Artem on 10.04.2016.
 */
public class HierarchyLevel {
    private LinkedList<Person> clients;

    public HierarchyLevel(Person client) {
        clients = new LinkedList<>();
        clients.add(client);
    }

    public Person sendContactClient() {
        return clients.getLast();
    }

    public void removeClient(Person person) {
        /* TODO: what if it is last client? */
        clients.remove(person);
    }
}
