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

    public InventoryManager() {
        this.inventoryDAO = ApplicationContext.getInstance().getDAOFactory().getInventoryDAO();
        this.notificationManager = new NotificationManager();
    }

    public InventoryManager(NotificationManager notificationManager) {
        this.inventoryDAO = ApplicationContext.getInstance().getDAOFactory().getInventoryDAO();
        this.notificationManager = notificationManager;
    }

    // ✅ OBSERVER PATTERN - INVARIATO
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

    // 🔄 METODI CON MAPPER
    public void addPart(PartBean partBean) {
        PartEntity entity = mapperFactory.toEntity(partBean, PartEntity.class);
        inventoryDAO.savePart(entity);

        // Converti per la logica business
        PartBean savedPart = mapperFactory.toBean(entity, PartBean.class);
        checkThreshold(savedPart);
        notificationManager.refreshLowStockNotifications();
    }

    public void updatePart(PartBean partBean) {
        PartEntity entity = mapperFactory.toEntity(partBean, PartEntity.class);
        inventoryDAO.updatePart(entity);

        PartBean updatedPart = mapperFactory.toBean(entity, PartBean.class);
        checkThreshold(updatedPart);
        notificationManager.refreshLowStockNotifications();
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
        notificationManager.refreshLowStockNotifications();
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
            notificationManager.refreshLowStockNotifications();
            return true;
        }
        return false;
    }

    public boolean addOrUpdatePart(PartBean partBean) {
        PartEntity entity = mapperFactory.toEntity(partBean, PartEntity.class);
        inventoryDAO.savePart(entity);

        PartBean savedPart = mapperFactory.toBean(entity, PartBean.class);
        checkThreshold(savedPart);
        notificationManager.refreshLowStockNotifications();
        return true;
    }

    private void checkThreshold(PartBean part) {
        if (part.getQuantity() <= part.getReorderThreshold()) {
            NotificationBean notification = new NotificationBean("Scorte basse per: " + part.getName() +
                    " (Quantità: " + part.getQuantity() + ", Soglia minima: " + part.getReorderThreshold() + ")", null,
                    LocalDate.now().toString(), part.getName());
            notification.setHasSuggestedOrder(true);
            notification.setSuggestedQuantity((part.getReorderThreshold() + 10) - part.getQuantity());
            notifyObserver(notification);
            notificationManager.addNotification(notification);
        } else {
            notificationManager.removeNotificationsByPartName(part.getName());
        }
    }
}
