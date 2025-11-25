package FileDAO;

import DAO.UserDAO;
import entity.UserEntity;
import exception.FilePersistenceException;
import java.io.*;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUserDAO implements UserDAO {

    private static final String FILE_PATH = "data/users.txt";

    @Override
    public void saveUser(UserEntity user) {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                boolean fileCreated = file.createNewFile();
                if (!fileCreated) {
                    throw new FilePersistenceException("Impossibile creare il file utenti: " + FILE_PATH);
                }
            } catch (IOException e) {
                throw new FilePersistenceException("Errore creazione file utenti: " + e.getMessage(), e);
            }
        }

        List<UserEntity> allUsers = getAllUsers();
        int nextId = 1;
        if (!allUsers.isEmpty()) {
            nextId = allUsers.stream().mapToInt(UserEntity::getId).max().orElse(0) + 1;
        }

        if (user.getId() == 0) {
            user.setId(nextId);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(formatUser(user));
            writer.newLine();
        } catch (IOException e) {
            throw new FilePersistenceException("Errore durante il salvataggio dell'utente su file: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<UserEntity> getUserByUsername(String username) {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return Optional.empty();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    UserEntity user = parseUser(line);
                    if (user != null && user.getUsername().equals(username)) {
                        return Optional.of(user);
                    }
                }
            }
        } catch (IOException e) {
            throw new FilePersistenceException("Errore durante la lettura dell'utente da file: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public boolean userExists(String username) {
        return getUserByUsername(username).isPresent();
    }

    @Override
    public boolean validateCredentials(String username, String password) {
        Optional<UserEntity> user = getUserByUsername(username);
        return user.map(u -> u.getPassword().equals(password)).orElse(false);
    }

    @Override
    public void updateDefaultSupplier(String username, String newSupplierName) {
        File inputFile = new File(FILE_PATH);
        if (!inputFile.exists()) {
            throw new FilePersistenceException("File utenti non trovato: " + FILE_PATH);
        }

        File tempFile = new File("data/temp_users.txt");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            boolean updated = false;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    UserEntity user = parseUser(line);
                    if (user != null) {
                        if (user.getUsername().equals(username)) {
                            user.setDefaultSupplierName(newSupplierName);
                            updated = true;
                        }
                        writer.write(formatUser(user));
                        writer.newLine();
                    }
                }
            }
            if (!updated) throw new FilePersistenceException("Utente non trovato: " + username);
        } catch (IOException e) {
            throw new FilePersistenceException("Errore durante l'aggiornamento del fornitore predefinito: " + e.getMessage(), e);
        }

        try {
            Path inputFilePath = Paths.get(inputFile.getPath());
            Path tempFilePath = Paths.get(tempFile.getPath());
            Files.delete(inputFilePath);
            Files.move(tempFilePath, inputFilePath);
        } catch (IOException e) {
            throw new FilePersistenceException("Errore nel salvataggio del file aggiornato: " + e.getMessage(), e);
        }
    }

    private List<UserEntity> getAllUsers() {
        List<UserEntity> users = new ArrayList<>();
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return users;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    UserEntity user = parseUser(line);
                    if (user != null) {
                        users.add(user);
                    }
                }
            }
        } catch (IOException e) {
            throw new FilePersistenceException("Errore lettura utenti da file: " + e.getMessage(), e);
        }
        return users;
    }

    private String formatUser(UserEntity u) {
        return u.getId() + "|" + u.getUsername() + "|" + u.getPassword() + "|" +
                u.getEmail() + "|" + (u.getDefaultSupplierName() != null ? u.getDefaultSupplierName() : "");
    }

    @SuppressWarnings("java:S106") // Soppressione warning per System.err
    private UserEntity parseUser(String line) {
        try {
            String[] parts = line.split("\\|", -1); // Usa -1 per preservare campi vuoti

            if (parts.length >= 4) {
                int id = Integer.parseInt(parts[0]);
                String username = parts[1];
                String password = parts[2];
                String email = parts[3];
                String defaultSupplier = parts.length > 4 && !parts[4].isEmpty() ? parts[4] : null;

                return new UserEntity(id, username, email, password, defaultSupplier);
            } else {
                System.err.println(" Formato file utenti non valido: " + line);
                return null;
            }
        } catch (NumberFormatException _) {
            System.err.println(" Errore parsing ID nel file utenti: " + line);
            return null;
        }
    }
}
