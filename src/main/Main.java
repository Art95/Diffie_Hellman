package main;

import participants.Client;
import participants.Group;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Artem on 09.05.2016.
 */
public class Main {
    private static Group group;

    public static void main(String[] args) {
        group = new Group();

        int[] tests = {100};
        int levelsOfHierarchy = 6;

        List<Client> clients = new ArrayList<>();

        for (int test : tests) {
            clients = joinTime(test, levelsOfHierarchy);

            Client testClient1 = new Client(Integer.MAX_VALUE - 5, 1);
            Client testClient2 = new Client(Integer.MAX_VALUE - 6, levelsOfHierarchy);

            testClient1.joinGroup(group);
            testClient2.joinGroup(group);
        }

        leaveDebug(clients);
    }

    private static List<Client> joinTime(int clientsNum, int hierarchyLevels) {
        List<Client> clients = new ArrayList<>();
        int[] clientsAtLevel = new int[hierarchyLevels + 1];
        clientsAtLevel[hierarchyLevels] = clientsNum / 2;
        int totalClients = clientsNum / 2;

        for (int i = hierarchyLevels - 1; i > 0; --i) {
            double clientsAtThisLevel = Math.ceil(clientsAtLevel[i + 1] / 2.0);
            totalClients += (int)clientsAtThisLevel;
            clientsAtLevel[i] = (int)clientsAtThisLevel;
        }

        if (totalClients < clientsNum) {
            clientsAtLevel[hierarchyLevels] += clientsNum - totalClients;
        }

        int id = 1;

        for (int i = 1; i <= hierarchyLevels; ++i) {
            for (int j = 0; j < clientsAtLevel[i]; ++j) {
                Client client = new Client(id++, i);
                clients.add(client);
            }
        }

        for (Client client : clients) {
            client.joinGroup(group);
        }

        return clients;
    }

    private static void joinDebug(List<Client> clients) {
        System.out.println("Join");

        printClients(clients);

        for (Client client : clients) {
            client.joinGroup(group);
            printClients(clients);
        }
    }

    private static void leaveDebug(List<Client> clients) {
        Random rand = new Random();
        while (!clients.isEmpty()) {
            int client = rand.nextInt(clients.size());
            clients.get(client).leaveGroup(group);
            clients.remove(client);
        }
    }

    private static void printClients(List<Client> clients) {
        System.out.println("\n-----------------------\n");

        for (Client client : clients) {
            System.out.println(client);
        }

        System.out.println("\n-----------------------\n");
    }
}
