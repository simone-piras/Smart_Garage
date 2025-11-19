package InMemoryDAO;

import DAO.InventoryDAO;
import entity.PartEntity;
import java.util.*;

public class InMemoryInventoryDAO implements InventoryDAO {
    private final Map<String, PartEntity> partStorage = new HashMap<>();
    private int nextId = 1;

    @Override
    public void savePart(PartEntity part) {
        if (part.getId() == 0) {
            PartEntity newPart = new PartEntity(nextId++, part.getName(), part.getQuantity(), part.getReorderThreshold());
            partStorage.put(newPart.getName(), newPart);
        } else {
            partStorage.put(part.getName(), part);
        }
    }

    @Override
    public Optional<PartEntity> getPartByName(String name) {
        return Optional.ofNullable(partStorage.get(name));
    }

    @Override
    public void updatePart(PartEntity part) {
        if (part.getId() == 0) {
            savePart(part);
        } else {
            partStorage.put(part.getName(), part);
        }
    }

    @Override
    public List<PartEntity> getAllParts() {
        return new ArrayList<>(partStorage.values());
    }

    @Override
    public List<PartEntity> getPartsBelowThreshold() {
        List<PartEntity> lowStock = new ArrayList<>();
        for (PartEntity part : partStorage.values()) {
            if (part.getQuantity() <= part.getReorderThreshold()) {
                lowStock.add(part);
            }
        }
        return lowStock;
    }

    @Override
    public void removePart(String name) {
        partStorage.remove(name);
    }
}