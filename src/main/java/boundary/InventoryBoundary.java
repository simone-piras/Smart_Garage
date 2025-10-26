package boundary;

import bean.PartBean;
import exception.InsufficientStockException;
import exception.PartNotFoundException;
import controller.InventoryManager;
import controller.NotificationManager;

import java.util.List;

public class InventoryBoundary {
    private final InventoryManager inventoryManager;
    private final NotificationManager notificationManager;

    //CLI
    public InventoryBoundary(){
        this.inventoryManager = new InventoryManager();
        this.notificationManager = new NotificationManager();
    }

    public InventoryBoundary(InventoryManager sharedInventorymanager, NotificationManager sharedNotificationManager){
        this.inventoryManager = sharedInventorymanager;
        this.notificationManager = sharedNotificationManager;
    }

    public List<PartBean> getAllParts() {
        return inventoryManager.getAllParts();
    }

    public boolean updatePartQuantity(String partName, int delta) throws InsufficientStockException, PartNotFoundException {
        boolean success;
        if (delta < 0) {
            success = inventoryManager.usePart(partName, -delta);
        } else {
            success = inventoryManager.addQuantityToPart(partName, delta);
        }
        return success;
    }

    public void addOrUpdatePart(String name, int quantity, int reorderThreshold) {
        PartBean part = new PartBean();
        part.setName(name);
        part.setQuantity(quantity);
        part.setReorderThreshold(reorderThreshold);
        part.validate();
        inventoryManager.addOrUpdatePart(part);
    }
}