package InMemoryDAO;

import DAO.InventoryDAO;
import entity.PartEntity;
import utils.SessionManager;

import java.util.*;

public class InMemoryInventoryDAO implements InventoryDAO {
    private final List<PartEntity> partStorage = new ArrayList<>();
    private int nextId = 1;

    private String getCurrentUser() {
        return SessionManager.getInstance().getCurrentUser().getUsername();
    }

    @Override
    public void savePart(PartEntity part) {
        // Cerco se il pezzo esiste già nella lista per questo utente
        Optional<PartEntity> existingMatch = partStorage.stream()
                .filter(p -> p.getName().equalsIgnoreCase(part.getName()) &&
                        p.getOwnerUsername().equals(getCurrentUser()))
                .findFirst();

        if (existingMatch.isPresent()) {
            // Ho trovato l'oggetto
            PartEntity p = existingMatch.get();
            p.setQuantity(part.getQuantity());
            p.setThreshold(part.getReorderThreshold());


        } else {
            // Non esiste, lo creo e lo aggiungo in fondo.
            if (part.getId() == 0) {
                part.setId(nextId++);
            }
            part.setOwnerUsername(getCurrentUser());
            partStorage.add(part);
        }
    }

    @Override
    public Optional<PartEntity> getPartByName(String name) {
        return partStorage.stream()
                .filter(p -> p.getOwnerUsername().equals(getCurrentUser())) // Filtro per utente
                .filter(p -> p.getName().equalsIgnoreCase(name)) // Filtro per nome
                .findFirst();
    }

    @Override
    public void updatePart(PartEntity part) {
        savePart(part);
    }

    @Override
    public List<PartEntity> getAllParts() {
        return partStorage.stream()
                .filter(p -> p.getOwnerUsername().equals(getCurrentUser())) // Solo i miei pezzi
                .toList();
    }

    @Override
    public List<PartEntity> getPartsBelowThreshold() {
        return partStorage.stream()
                .filter(p -> p.getOwnerUsername().equals(getCurrentUser())) // Solo i miei pezzi
                .filter(p -> p.getQuantity() <= p.getReorderThreshold())
                .toList();
    }

    @Override
    public void removePart(String name) {
        // Rimuove solo se il nome corrisponde E appartiene all'utente
        partStorage.removeIf(p -> p.getName().equalsIgnoreCase(name) &&
                p.getOwnerUsername().equals(getCurrentUser()));
    }
}