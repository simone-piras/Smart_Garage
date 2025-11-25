package FileDAO;

import DAO.InventoryDAO;
import entity.PartEntity;
import exception.FileFormatException;
import exception.FilePersistenceException;

import java.io.*;
import java.util.*;

public class FileInventoryDAO implements InventoryDAO {

    private static final String FILE_PATH = "data/parts.txt";


    @Override
    public void savePart(PartEntity part) {
        List<PartEntity> allParts = getAllParts();

        // 👇 CALCOLA nextId LOCALMENTE invece di usare campo
        int nextId = allParts.stream()
                .mapToInt(PartEntity::getId)
                .max()
                .orElse(0) + 1;

        //Se la parte non ha ID, gliene assegno uno
        if (part.getId() == 0) {
            part.setId(nextId);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(formatPart(part));
            writer.newLine();
        } catch (IOException e) {
            throw new FilePersistenceException("Errore scrittura parte su file: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<PartEntity> getPartByName(String name) {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                PartEntity p = parsePart(line);
                if (p.getName().equals(name)) return Optional.of(p);
            }
        } catch (IOException e) {
            throw new FilePersistenceException("Errore lettura parte da file: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public void updatePart(PartEntity updatedPart) {
        List<PartEntity> parts = getAllParts();
        for (int i = 0; i < parts.size(); i++) {
            if (parts.get(i).getName().equals(updatedPart.getName())) {
                parts.set(i, updatedPart);
                break;
            }
        }
        rewriteAll(parts);
    }

    @Override
    public List<PartEntity> getAllParts() {
        List<PartEntity> parts = new ArrayList<>();
        File file = new File(FILE_PATH);

        //Se il file non esiste, ritorna lista vuota
        if (!file.exists()) {
            return parts;
        }

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
    public List<PartEntity> getPartsBelowThreshold() {
        List<PartEntity> lowStock = new ArrayList<>();
        for (PartEntity p : getAllParts()) {
            if (p.getQuantity() <= p.getThreshold()) lowStock.add(p);
        }
        return lowStock;
    }

    @Override
    public void removePart(String name) {
        List<PartEntity> parts = getAllParts();
        parts.removeIf(p -> p.getName().equals(name));
        rewriteAll(parts);
    }

    private String formatPart(PartEntity p) {
        return p.getId() + "|" + p.getName() + "|" + p.getQuantity() + "|" + p.getThreshold();
    }

    private PartEntity parsePart(String line) {
        try {
            String[] parts = line.split("\\|");
            //CONTROLLA se ci sono almeno 4 parti (id|name|quantity|threshold)
            if (parts.length >= 4) {
                int id = Integer.parseInt(parts[0]);
                String name = parts[1];
                int quantity = Integer.parseInt(parts[2]);
                int threshold = Integer.parseInt(parts[3]);
                return new PartEntity(id, name, quantity, threshold);
            }
            // SE MANCA L'ID
            else if (parts.length == 3) {
                String name = parts[0];
                int quantity = Integer.parseInt(parts[1]);
                int threshold = Integer.parseInt(parts[2]);
                // 👇 USA name.hashCode() DIRETTAMENTE invece di Math.abs()
                int tempId = name.hashCode();
                return new PartEntity(tempId, name, quantity, threshold);
            } else {
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