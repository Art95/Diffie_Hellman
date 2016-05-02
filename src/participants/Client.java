package participants;

/**
 * Created by Artem on 11.04.2016.
 */
public interface Client {
    void joinGroup(HierarchyLevel group);
    void leaveGroup(HierarchyLevel group);
}
