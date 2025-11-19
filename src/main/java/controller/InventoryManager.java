package controller;

import bean.NotificationBean;
import bean.PartBean;
import DAO.InventoryDAO;
import entity.PartEntity;
import exception.InsufficientStockException;
import exception.PartNotFoundException;
import mapper.BeanEntityMapperFactory;
import observer.Observer;
import observer.Subject;
import utils.ApplicationContext;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

public class InventoryManager implements Subject {
    private final InventoryDAO inventoryDAO;
    private final List<Observer> observers = new ArrayList<>();
    private final NotificationManager notificationManager;
    private final BeanEntityMapperFactory mapperFactory = BeanEntityMapperFactory.getInstance();

    /*
    Costruttore di default per CLI, crea autonomamente le sue dipendenze e configura
    Observer pattern
     */
    public InventoryManager() {
        this.inventoryDAO = ApplicationContext.getInstance().getDAOFactory().getInventoryDAO();
        this.notificationManager = new NotificationManager();
        //REGISTRA NotificationManager COME OBSERVER
        this.addObserver(notificationManager);
    }

    /*
    Costruttore con dipendenze, accetta un NotificationManager già configurato dall'esterno, è necessario perchè garantisce
    la condivisione della stessa NotificationManager tra tutti i componenti dell'applicazione, così che tutti vedano le stesse notifiche
     */
    public InventoryManager(NotificationManager notificationManager) {
        //Binding dinamico, polimorfismo: a runtime viene scelto il DAO relativo alla persistenza scelta nella sessione
        this.inventoryDAO = ApplicationContext.getInstance().getDAOFactory().getInventoryDAO();
        this.notificationManager = notificationManager;
        //REGISTRA NotificationManager COME OBSERVER
        this.addObserver(notificationManager);
    }

    //OBSERVER PATTERN
    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObserver(NotificationBean notification) {
        for (Observer o : observers) {
            o.update(notification);
        }
    }

    // METODI CON MAPPER
    public void addPart(PartBean partBean) {
        PartEntity entity = mapperFactory.toEntity(partBean, PartEntity.class);
        inventoryDAO.savePart(entity);

        PartBean savedPart = mapperFactory.toBean(entity, PartBean.class);
        checkThreshold(savedPart);
    }

    public void updatePart(PartBean partBean) {
        PartEntity entity = mapperFactory.toEntity(partBean, PartEntity.class);
        inventoryDAO.updatePart(entity);

        PartBean updatedPart = mapperFactory.toBean(entity, PartBean.class);
        checkThreshold(updatedPart);
    }

    public boolean usePart(String partName, int quantityUsed) throws InsufficientStockException, PartNotFoundException {
        PartEntity entity = inventoryDAO.getPartByName(partName)
                .orElseThrow(() -> new PartNotFoundException("Parte non trovata: " + partName));

        PartBean part = mapperFactory.toBean(entity, PartBean.class);

        if (part.getQuantity() < quantityUsed) {
            throw new InsufficientStockException("Scorte insufficienti per " + partName);
        }

        PartBean updated = new PartBean(part.getName(), part.getQuantity() - quantityUsed, part.getReorderThreshold());
        PartEntity updatedEntity = mapperFactory.toEntity(updated, PartEntity.class);
        inventoryDAO.updatePart(updatedEntity);
        checkThreshold(updated);
        return true;
    }

    public List<PartBean> getAllParts() {
        return inventoryDAO.getAllParts().stream()
                .map(entity -> mapperFactory.toBean(entity, PartBean.class))
                .toList();
    }

    public List<PartBean> getPartsBelowThreshold() {
        return inventoryDAO.getPartsBelowThreshold().stream()
                .map(entity -> mapperFactory.toBean(entity, PartBean.class))
                .toList();
    }

    public List<PartBean> getLowStockParts() {
        return inventoryDAO.getPartsBelowThreshold().stream()
                .map(entity -> mapperFactory.toBean(entity, PartBean.class))
                .toList();
    }

    public void removePart(String name) {
        inventoryDAO.removePart(name);
    }

    public boolean addQuantityToPart(String partName, int quantityToAdd) {
        Optional<PartEntity> partOpt = inventoryDAO.getPartByName(partName);
        if (partOpt.isPresent()) {
            PartEntity entity = partOpt.get();
            PartBean part = mapperFactory.toBean(entity, PartBean.class);

            int newQuantity = part.getQuantity() + quantityToAdd;
            PartBean updated = new PartBean(partName, newQuantity, part.getReorderThreshold());
            PartEntity updatedEntity = mapperFactory.toEntity(updated, PartEntity.class);

            inventoryDAO.updatePart(updatedEntity);
            checkThreshold(updated);
            return true;
        }
        return false;
    }

    public boolean addOrUpdatePart(PartBean partBean) {
        PartEntity entity = mapperFactory.toEntity(partBean, PartEntity.class);
        inventoryDAO.savePart(entity);

        PartBean savedPart = mapperFactory.toBean(entity, PartBean.class);
        checkThreshold(savedPart);
        return true;
    }

    private void checkThreshold(PartBean part) {
        if (part.getQuantity() <= part.getReorderThreshold()) {
            NotificationBean notification = new NotificationBean("Scorte basse per: " + part.getName() +
                    " (Quantità: " + part.getQuantity() + ", Soglia minima: " + part.getReorderThreshold() + ")", null,
                    LocalDate.now().toString(), part.getName());
            notification.setHasSuggestedOrder(true);
            notification.setSuggestedQuantity((part.getReorderThreshold() + 10) - part.getQuantity());
            notifyObserver(notification); //NOTIFICA TUTTI GLI OBSERVER
        } else {
            notificationManager.removeNotificationsByPartName(part.getName());//se sopra soglia rimuove le notifiche esitenti per tale parte dalle notifiche
        }
    }
}