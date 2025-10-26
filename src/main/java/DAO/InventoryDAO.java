package DAO;

import entity.PartEntity;
import java.util.List;
import java.util.Optional;

public interface InventoryDAO {
    void savePart(PartEntity part);
    Optional<PartEntity> getPartByName(String name);
    void updatePart(PartEntity part);
    List<PartEntity> getAllParts();
    List<PartEntity> getPartsBelowThreshold();
    void removePart(String name);
}