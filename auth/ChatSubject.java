package auth;

public interface ChatSubject {
    void addObserver(ChatObserver observer, String username);
    void removeObserver(String username);
    void notifyObservers(String username);
}
