package FileDAO;

import DAO.NotificationDAO;
import entity.NotificationEntity;
import exception.FilePersistenceException;
import utils.SessionManager;

import java.io.*;
import java.util.*;

public class FileNotificationDAO implements NotificationDAO {

    private static final String FILE_PATH = "data/notifications.txt";
    private static final String NEWLINE_PLACEHOLDER = "@@@NEWLINE@@@";

    private String getCurrentUser() {
        return SessionManager.getInstance().getCurrentUser().getUsername();
    }

    private List<NotificationEntity> loadAllRaw() {
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
            throw new FilePersistenceException("Errore lettura notifiche da file: " + e.getMessage(), e);
        }
        return notifications;
    }

    @Override
    public void saveNotification(NotificationEntity n) {
        List<NotificationEntity> allNotifications = loadAllRaw();

        // Calcolo ID globale
        int nextId;
        if (allNotifications.isEmpty()) {
            nextId = 1;
        } else {
            nextId = allNotifications.stream().mapToInt(NotificationEntity::getId).max().orElse(0) + 1;
        }

        if (n.getId() == 0) {
            n.setId(nextId);
        }

        // Imposto Owner se non c'è
        if (n.getOwnerUsername() == null || n.getOwnerUsername().isEmpty()) {
            n.setOwnerUsername(getCurrentUser());
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(formatNotification(n));
            writer.newLine();
        } catch (IOException e) {
            throw new FilePersistenceException("Errore scrittura notifica su file: " + e.getMessage(), e);
        }
    }

    @Override
    public List<NotificationEntity> getAllNotifications() {
        // Solo le mie notifiche
        return loadAllRaw().stream()
                .filter(n -> n.getOwnerUsername().equals(getCurrentUser()))
                .toList();
    }

    @Override
    public void clearNotifications() {
        List<NotificationEntity> allNotifications = loadAllRaw();
        // Rimuovo SOLO le mie
        allNotifications.removeIf(n -> n.getOwnerUsername().equals(getCurrentUser()));
        rewriteAll(allNotifications);
    }

    @Override
    public void removeNotification(NotificationEntity notification) {
        List<NotificationEntity> allNotifications = loadAllRaw();

        // Rimuovo se corrisponde ID E owner
        allNotifications.removeIf(n ->
                n.getId() == notification.getId() &&
                        n.getOwnerUsername().equals(getCurrentUser()));

        rewriteAll(allNotifications);
    }

    private String formatNotification(NotificationEntity n) {
        String escapedMessage = n.getMessage().replace("\n", NEWLINE_PLACEHOLDER).replace("|", "\\|");

        return n.getId() + "|" +
                escapedMessage + "|" +
                (n.getPartName() != null ? n.getPartName() : "null") + "|" +
                n.getDate() + "|" +
                n.isHasSuggestedOrder() + "|" +
                n.getSuggestedQuantity() + "|" +
                n.getOwnerUsername(); // Aggiunto OWNER alla fine
    }

    @SuppressWarnings("java:S106")
    private NotificationEntity parseNotification(String line) {
        try {
            String[] parts = line.split("(?<!\\\\)\\|");


            if (parts.length >= 7) {
                int id = Integer.parseInt(parts[0]);
                String message = parts[1].replace(NEWLINE_PLACEHOLDER, "\n").replace("\\|", "|");
                String partName = parts[2].equals("null") ? null : parts[2];
                String date = parts[3];
                boolean hasSuggestedOrder = Boolean.parseBoolean(parts[4]);
                int suggestedQuantity = Integer.parseInt(parts[5]);
                String owner = parts[6];

                return new NotificationEntity(id, message, date, partName,
                        hasSuggestedOrder, suggestedQuantity, null, owner);
            } else {
                System.err.println("Formato notifica non valido: " + line);
                return null;
            }
        } catch (Exception _) {
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
            throw new FilePersistenceException("Errore scrittura notifiche su file: " + e.getMessage(), e);
        }
    }
}
