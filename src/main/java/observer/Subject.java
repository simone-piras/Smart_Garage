package observer;
import bean.NotificationBean;


public interface Subject {
    void addObserver(Observer observer);
    void removeObserver(Observer observer);
    void notifyObserver(NotificationBean notification);
}
