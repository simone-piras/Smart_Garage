package observer;

import bean.NotificationBean;

public interface Observer {
    void update(NotificationBean notification);
}
