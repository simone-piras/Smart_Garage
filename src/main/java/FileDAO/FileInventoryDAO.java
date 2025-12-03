package FileDAO;

import DAO.InventoryDAO;
import entity.PartEntity;
import exception.FileFormatException;
import exception.FilePersistenceException;
import utils.SessionManager;

import java.io.*;
import java.util.*;


public class FileInventoryDAO implements InventoryDAO {

    private static final String FILE_PATH = "data/parts.txt";

    private String getCurrentUser() {
        return SessionManager.getInstance().getCurrentUser().getUsername();
    }

    // Metodo PRIVATO che legge TUTTO il file (tutti gli utenti)
    // Serve per non perdere i dati degli altri quando riscriviamo il file
    private List<PartEntity> loadAllRaw() {
        List<PartEntity> parts = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return parts;

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    parts.add(parsePart(line));
                }
            }
        } catch (IOException e) {
            throw new FilePersistenceException("Errore lettura parti da file: " + e.getMessage(), e);
        }
        return parts;
    }

    @Override
    public void savePart(PartEntity part) {
        List<PartEntity> allParts = loadAllRaw(); // Leggo TUTTI per calcolare ID univoco globale

        // Calcola nextId basato su TUTTI gli utenti per evitare conflitti nel file
        int nextId = allParts.stream()
                .mapToInt(PartEntity::getId)
                .max()
                .orElse(0) + 1;

        if (part.getId() == 0) {
            part.setId(nextId);
        }

        // Assegno l'utente corrente
        part.setOwnerUsername(getCurrentUser());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(formatPart(part));
            writer.newLine();
        } catch (IOException e) {
            throw new FilePersistenceException("Errore scrittura parte su file: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<PartEntity> getPartByName(String name) {
        // Leggo tutto, ma restituisco solo se è mio e ha quel nome
        return loadAllRaw().stream()
                .filter(p -> p.getOwnerUsername().equals(getCurrentUser()))
                .filter(p -> p.getName().equals(name))
                .findFirst();
    }

    @Override
    public void updatePart(PartEntity updatedPart) {
        List<PartEntity> allParts = loadAllRaw(); // Leggo TUTTI
        boolean found = false;

        for (int i = 0; i < allParts.size(); i++) {
            PartEntity p = allParts.get(i);
            // Modifico solo se il nome coincide e appartiene all'utente corrente
            if (p.getName().equals(updatedPart.getName()) &&
                    p.getOwnerUsername().equals(getCurrentUser())) {

                // Mantengo l'owner originale (o lo reimposto per sicurezza)
                updatedPart.setOwnerUsername(getCurrentUser());
                allParts.set(i, updatedPart);
                found = true;
                break;
            }
        }

        if (found) {
            rewriteAll(allParts); // Riscrivo TUTTI i dati (miei e degli altri)
        }
    }

    @Override
    public List<PartEntity> getAllParts() {
        // Restituisco solo i pezzi dell'utente loggato
        return loadAllRaw().stream()
                .filter(p -> p.getOwnerUsername().equals(getCurrentUser()))
                .toList();
    }

    @Override
    public List<PartEntity> getPartsBelowThreshold() {
        return getAllParts().stream()
                .filter(p -> p.getQuantity() <= p.getThreshold())
                .toList();
    }

    @Override
    public void removePart(String name) {
        List<PartEntity> allParts = loadAllRaw();
        // Rimuovo solo se nome coincide E utente coincide
        boolean removed = allParts.removeIf(p ->
                p.getName().equals(name) &&
                        p.getOwnerUsername().equals(getCurrentUser()));

        if (removed) {
            rewriteAll(allParts);
        }
    }

    private String formatPart(PartEntity p) {
        // Aggiungo ownerUsername alla fine
        return p.getId() + "|" + p.getName() + "|" + p.getQuantity() + "|" + p.getThreshold() + "|" + p.getOwnerUsername();
    }

    private PartEntity parsePart(String line) {
        try {
            String[] parts = line.split("\\|");


            if (parts.length >= 5) {
                int id = Integer.parseInt(parts[0]);
                String name = parts[1];
                int quantity = Integer.parseInt(parts[2]);
                int threshold = Integer.parseInt(parts[3]);
                String owner = parts[4];
                return new PartEntity(id, name, quantity, threshold, owner);
            }
            else if (parts.length == 4) {
                int id = Integer.parseInt(parts[0]);
                String name = parts[1];
                int quantity = Integer.parseInt(parts[2]);
                int threshold = Integer.parseInt(parts[3]);
                return new PartEntity(id, name, quantity, threshold, getCurrentUser());
            }
            else {
                throw new FileFormatException("Formato file non valido: " + line);
            }
        } catch (NumberFormatException e) {
            throw new FileFormatException("Errore parsing numero nel file: " + line, e);
        }
    }

    private void rewriteAll(List<PartEntity> parts) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (PartEntity p : parts) {
                writer.write(formatPart(p));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new FilePersistenceException("Errore riscrittura file parti: " + e.getMessage(), e);
        }
    }
}
