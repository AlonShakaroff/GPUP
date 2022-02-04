package users;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class UserManager {

    private final UsersLists usersLists;

    public UserManager() { usersLists = new UsersLists(); }

    public synchronized void addAdmin(String username) {
        usersLists.getAdminsList().add(username);
    }
    public synchronized Set<String> getAdmins() {
        return Collections.unmodifiableSet(usersLists.getAdminsList());
    }

    public synchronized Set<String> getWorkers() {
        return Collections.unmodifiableSet(usersLists.getWorkersList());
    }

    public synchronized void addWorker(String username) {
        usersLists.getWorkersList().add(username);
    }

    public synchronized void removeUser(String username) {
        if(usersLists.getWorkersList().contains(username))
            usersLists.getWorkersList().remove(username);
        else
            usersLists.getAdminsList().remove(username);
    }

    public boolean isUserExists(String username) {
        return (usersLists.getAdminsList().contains(username) || usersLists.getWorkersList().contains(username));
    }

    public UsersLists getUsersLists() { return usersLists; }
}
