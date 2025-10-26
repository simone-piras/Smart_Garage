package FileDAO;

import DAO.NotificationDAO;
import entity.NotificationEntity;
import java.io.*;
import java.util.*;

public class FileNotificationDAO implements NotificationDAO {

    private static final String FILE_PATH = "data/notifications.txt";
    private int nextId = 1;

    @Override
    public void saveNotification(NotificationEntity n) {
        List<NotificationEntity> allNotifications = getAllNotifications();

        // ✅ Trova il prossimo ID disponibile
        if (allNotifications.isEmpty()) {
            nextId = 1;
        } else {
            nextId = allNotifications.stream().mapToInt(NotificationEntity::getId).max().orElse(0) + 1;
        }

        // ✅ Se la notifica non ha ID, gliene assegno uno
        if (n.getId() == 0) {
            n.setId(nextId);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(formatNotification(n));
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Errore scrittura notifica su file: " + e.getMessage(), e);
        }
    }

    @Override
    public List<NotificationEntity> getAllNotifications() {
        List<NotificationEntity> notifications = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return notifications;

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    NotificationEntity n = parseNotification(line);
                    if (n != null) notifications.add(n);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Errore lettura notifiche da file: " + e.getMessage(), e);
        }
        return notifications;
    }

    @Override
    public void clearNotifications() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            // sovrascrive
        } catch (IOException e) {
            throw new RuntimeException("Errore nella cancellazione notifiche: " + e.getMessage(), e);
        }
    }

    @Override
    public void removeNotification(NotificationEntity notification) {
        List<NotificationEntity> notifications = getAllNotifications();
        notifications.removeIf(n ->
                n.getMessage().equals(notification.getMessage()) &&
                        Objects.equals(n.getPartName(), notification.getPartName()) &&
                        n.getDate().equals(notification.getDate()));
        rewriteAll(notifications);
    }

    private String formatNotification(NotificationEntity n) {
        String escapedMessage = n.getMessage().replace("\n", "@@@NEWLINE@@@").replace("|", "\\|");
        return n.getId() + "|" + escapedMessage + "|" +
                (n.getPartName() != null ? n.getPartName() : "null") + "|" +
                n.getDate() + "|" +
                n.isHasSuggestedOrder() + "|" +
                n.getSuggestedQuantity();
    }

    private NotificationEntity parseNotification(String line) {
        try {
            String[] parts = line.split("(?<!\\\\)\\|");
            // ✅ CONTROLLA se ci sono almeno 6 parti
            if (parts.length >= 6) {
                int id = Integer.parseInt(parts[0]);
                String message = parts[1].replace("@@@NEWLINE@@@", "\n").replace("\\|", "|");
                String partName = parts[2].equals("null") ? null : parts[2];
                String date = parts[3];
                boolean hasSuggestedOrder = Boolean.parseBoolean(parts[4]);
                int suggestedQuantity = Integer.parseInt(parts[5]);

                return new NotificationEntity(id, message, date, partName,
                        hasSuggestedOrder, suggestedQuantity, null);
            }
            // ✅ SE MANCA L'ID (formato vecchio: message|partName|date|hasSuggestedOrder|suggestedQuantity)
            else if (parts.length == 5) {
                String message = parts[0].replace("@@@NEWLINE@@@", "\n").replace("\\|", "|");
                String partName = parts[1].equals("null") ? null : parts[1];
                String date = parts[2];
                boolean hasSuggestedOrder = Boolean.parseBoolean(parts[3]);
                int suggestedQuantity = Integer.parseInt(parts[4]);
                // ✅ Genera un ID temporaneo basato sull'hash del messaggio
                int tempId = Math.abs(message.hashCode());

                return new NotificationEntity(tempId, message, date, partName,
                        hasSuggestedOrder, suggestedQuantity, null);
            } else {
                System.err.println("Formato notifica non valido: " + line);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Errore parsing notifica: " + line);
            return null;
        }
    }

    private void rewriteAll(List<NotificationEntity> notifications) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (NotificationEntity n : notifications) {
                writer.write(formatNotification(n));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Errore scrittura notifiche su file: " + e.getMessage(), e);
        }
    }
}