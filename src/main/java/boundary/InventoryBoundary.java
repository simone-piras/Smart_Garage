package boundary;

import bean.PartBean;
import exception.InsufficientStockException;
import exception.PartNotFoundException;
import controller.InventoryManager;
import controller.NotificationManager;
import utils.SharedManagers;

import java.util.List;

public class InventoryBoundary {
    private final InventoryManager inventoryManager;

    /*
     NotificationManager è mantenuto per garantire che InventoryManager
     utilizzi la stessa istanza condivisa tramite Singleton.
     InventoryManager lo usa internamente via Observer pattern poichè lo registra come osservatore e lo usa
     direttamente in checkThreshold().
     Rimuoverlo causerebbe istanze separate e inconsistenza delle notifiche.
     */
    @SuppressWarnings("java:S1068") // Field is essential for Observer pattern consistency
    private final NotificationManager notificationManager;

    /*
    Costruttore CLI: Per applicazioni a riga di comando, usa Singleton Shared Managers per ottenere istanze condivise
    Le applicazioni CLI non hanno un contesto di dependency injection, quindi usano un approccio service locator
     */
    public InventoryBoundary(){
        this.inventoryManager = SharedManagers.getInstance().getInventoryManager();
        this.notificationManager = SharedManagers.getInstance().getNotificationManager();
    }

    /*
    Costruttore GUI: Per applicazioni con interfaccia grafica, usa la dependency injection-le dipendenze vengono fornite
    dall'esterno, le GUI spesso hanno un lifecycle complesso e necessitano di istanze condivise tra diversi componenti
     */
    public InventoryBoundary(InventoryManager sharedInventorymanager, NotificationManager sharedNotificationManager){
        this.inventoryManager = sharedInventorymanager;
        this.notificationManager = sharedNotificationManager;
    }

    //ritorna una lista di PartBean per essere visualizzati nella UI
    public List<PartBean> getAllParts() {
        return inventoryManager.getAllParts();
    }

    public boolean updatePartQuantity(String partName, int delta) throws InsufficientStockException, PartNotFoundException {
        boolean success;
        if (delta < 0) { //usando parti
            success = inventoryManager.usePart(partName, -delta);
        } else { //aggiungendo parti
            success = inventoryManager.addQuantityToPart(partName, delta);
        }
        return success;
    }

    public void addOrUpdatePart(String name, int quantity, int reorderThreshold) {
        //trasforma i parametri in un oggetto PartBean
        PartBean part = new PartBean();
        part.setName(name);
        part.setQuantity(quantity);
        part.setReorderThreshold(reorderThreshold);
        part.validate();
        inventoryManager.addOrUpdatePart(part); //salva o aggiorna la parte, serve per aggiungere una nuova parte
    }

    //metodo usato per controllare le soglie di scorte nell'inventario di default per i nuovi utenti registrati
    public void scanInventoryForLowStock() {
        inventoryManager.checkAllThresholds();
    }
}
