package cli;

import bean.*;
import boundary.*;
import exception.InsufficientStockException;
import exception.PartNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GarageManagerViewCLI {
    //Singleton delle boundary per tutta la CLI, ogni boundary usa il costruttore senza parametri che si appoggia a SHaredManagers
    private static final InventoryBoundary inventoryBoundary = new InventoryBoundary();
    private static final NotificationBoundary notificationBoundary = new NotificationBoundary();
    private static final OrderBoundary orderBoundary = new OrderBoundary();
    private static final SupplierBoundary supplierBoundary = new SupplierBoundary();
    private static final UserBoundary userBoundary = new UserBoundary();
    private static final Scanner SCANNER = new Scanner(System.in);

    private static final String MENU_TITLE = "========== GESTORE OFFICINA ==========";
    private static final String[] MENU_OPTIONS = {
            "1. Visualizza inventario",
            "2. Modifica quantità scorta",
            "3. Effettua ordine",
            "4. Visualizza fornitori",
            "5. Visualizza notifiche",
            "6. Visualizza ordini effettuati",
            "7. Imposta fornitore predefinito",
            "8. Torna al Menu Principale"
    };

    private GarageManagerViewCLI() {}

    public static void start(String username) {
        boolean exit = false;
        while (!exit) {
            printMenu();
            String choice = SCANNER.nextLine();
            switch (choice) {
                case "1" -> handleVisualizzaInventario();
                case "2" -> handleModificaQuantita();
                case "3" -> handleEffettuaOrdine(username);
                case "4" -> handleVisualizzaFornitori();
                case "5" -> handleVisualizzaNotifiche(username);
                case "6" -> handleVisualizzaOrdini();
                case "7" -> handleImpostaFornitorePredefinito(username);
                case "8" -> exit = true;
                default -> System.out.println("Opzione non valida.");
            }
        }
    }

    private static void printMenu() {
        System.out.println(MENU_TITLE);
        for (String option : MENU_OPTIONS) {
            System.out.println(option);
        }
        System.out.print("Scegli un'opzione: ");
    }

    private static void handleVisualizzaInventario() {
        List<PartBean> parts = inventoryBoundary.getAllParts();
        if (parts.isEmpty()) {
            System.out.println("Inventario vuoto.");
        } else {
            System.out.println("--- INVENTARIO ---");
            for (PartBean part : parts) {
                System.out.printf("Nome: %s | Quantità: %d | Soglia: %d%n",
                        part.getName(), part.getQuantity(), part.getReorderThreshold());
            }
        }
    }

    private static void handleModificaQuantita() {
        System.out.print("Nome parte da modificare: ");
        String nomeParte = SCANNER.nextLine();
        System.out.print("Quantità da aggiungere (+) o togliere (-): ");

        try {
            int delta = Integer.parseInt(SCANNER.nextLine());
            boolean success = inventoryBoundary.updatePartQuantity(nomeParte, delta);

            if (success) {
                System.out.println("Quantità aggiornata con successo.");
            } else {
                System.out.println("Parte non trovata.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Quantità non valida.");
        } catch (InsufficientStockException e) {
            System.out.println("Errore: quantità insufficiente per la parte \"" + nomeParte + "\".");
        } catch (PartNotFoundException e) {
            System.out.println("Errore: parte \"" + nomeParte + "\" non trovata.");
        }
    }

    private static void handleEffettuaOrdine(String username) {
        System.out.println("--- CREA ORDINE ---");
        UserBean user = userBoundary.getUser(username);

        List<OrderItemBean> items = new ArrayList<>();
        while (true) {
            System.out.print("Nome parte (vuoto per terminare): ");
            String partName = SCANNER.nextLine();
            if (partName.isBlank()) break;

            System.out.print("Quantità: ");
            try {
                int qty = Integer.parseInt(SCANNER.nextLine());
                OrderItemBean item = new OrderItemBean();
                item.setPartName(partName);
                item.setQuantity(qty);
                items.add(item);
            } catch (NumberFormatException e) {
                System.out.println("Quantità non valida.");
            }
        }

        if (items.isEmpty()) {
            System.out.println("Nessun articolo inserito. Ordine annullato.");
            return;
        }

        String supplierName = selezionaFornitore(user);
        if (supplierName == null) {
            System.out.println("Ordine annullato.");
            return;
        }

        OrderBean order = orderBoundary.createOrder(username, supplierName, items);
        System.out.println("Ordine creato con ID: " + order.getOrderID());
    }

    private static String selezionaFornitore(UserBean user) {
        String supplierName = user.getDefaultSupplierName();

        if (supplierName != null) {
            System.out.printf("Vuoi usare il fornitore predefinito (%s)? (s/n): ", supplierName);
            if (!SCANNER.nextLine().equalsIgnoreCase("s")) {
                supplierName = null;
            }
        }

        if (supplierName == null) {
            List<SupplierBean> suppliers = supplierBoundary.getAllSuppliers();
            if (suppliers.isEmpty()) {
                System.out.println("Nessun fornitore disponibile.");
                return null;
            }

            System.out.println("Fornitori disponibili:");
            for (SupplierBean s : suppliers) {
                System.out.println("- " + s.getName());
            }
            System.out.print("Scegli fornitore: ");
            supplierName = SCANNER.nextLine();
        }

        return supplierName;
    }

    private static void handleVisualizzaFornitori() {
        List<SupplierBean> suppliers = supplierBoundary.getAllSuppliers();
        if (suppliers.isEmpty()) {
            System.out.println("Nessun fornitore disponibile.");
            return;
        }

        System.out.println("--- FORNITORI ---");
        for (SupplierBean s : suppliers) {
            System.out.println("Nome: " + s.getName());
            System.out.println("Email: " + s.getEmail());
            System.out.println("Telefono: " + s.getPhone());
            System.out.println("----------------");
        }
    }

    private static void handleVisualizzaNotifiche(String username) {
        List<NotificationBean> notifications = notificationBoundary.getAllNotifications();
        List<OrderItemBean> suggestedItems = notificationBoundary.getSuggestedOrderItems();

        if (notifications.isEmpty() && suggestedItems.isEmpty()) {
            System.out.println("Nessuna notifica.");
            return;
        }

        // Notifiche Ordini
        boolean hasOrderNotifications = false;
        for (NotificationBean n : notifications) {
            if (n.getPartName() == null ||
                    (n.getMessage() != null &&
                            (n.getMessage().contains("Ordine") || n.getMessage().contains("ORDINE CONSEGNATO")))) {
                if (!hasOrderNotifications) {
                    System.out.println("--- NOTIFICHE ORDINI ---");
                    hasOrderNotifications = true;
                }
                System.out.println("- " + n.getMessage());
            }
        }

        // Notifiche Scorte
        boolean hasStockNotifications = false;
        for (NotificationBean n : notifications) {
            if (n.getPartName() != null && n.getMessage() != null && n.getMessage().contains("Scorte basse")) {
                if (!hasStockNotifications) {
                    System.out.println("\n--- NOTIFICHE SCORTE ---");
                    hasStockNotifications = true;
                }
                System.out.println("- " + n.getMessage());
            }
        }

        // Ordine Suggerito
        if (!suggestedItems.isEmpty()) {
            System.out.println("\n--- ORDINE SUGGERITO ---");
            for (OrderItemBean item : suggestedItems) {
                System.out.println("- " + item.getPartName() + " x " + item.getQuantity());
            }

            System.out.println("\nOpzioni:");
            System.out.println("1. Conferma ordine suggerito");
            System.out.println("2. Modifica ordine suggerito");
            System.out.println("3. Annulla");
            System.out.print("Scegli un'opzione: ");

            String choice = SCANNER.nextLine();

            switch (choice) {
                case "1" -> {
                    // Conferma ordine suggerito (comportamento originale)
                    String supplierName = selezionaFornitore(userBoundary.getUser(username));
                    if (supplierName != null) {
                        orderBoundary.createSuggestedOrder(username, suggestedItems, supplierName);
                        System.out.println("Ordine suggerito creato con successo.");
                    }
                }
                case "2" -> {
                    // Modifica ordine suggerito (nuovo comportamento)
                    List<OrderItemBean> modifiedItems = modificaOrdineSuggerito(suggestedItems);
                    if (!modifiedItems.isEmpty()) {
                        String supplierName = selezionaFornitore(userBoundary.getUser(username));
                        if (supplierName != null) {
                            orderBoundary.createSuggestedOrder(username, modifiedItems, supplierName);
                            System.out.println("Ordine modificato creato con successo.");
                        }
                    } else {
                        System.out.println("Nessun articolo nell'ordine. Ordine annullato.");
                    }
                }
                case "3" -> System.out.println("Operazione annullata.");
                default -> System.out.println("Opzione non valida.");
            }
        }
    }


    private static List<OrderItemBean> modificaOrdineSuggerito(List<OrderItemBean> suggestedItems) {
        List<OrderItemBean> modifiedItems = new ArrayList<>();
        List<PartBean> availableParts = inventoryBoundary.getAllParts();

        System.out.println("\n--- MODIFICA ORDINE SUGGERITO ---");
        System.out.println("Per ogni articolo, inserisci la nuova quantità (0 per rimuovere):");

        for (OrderItemBean originalItem : suggestedItems) {
            while (true) {
                System.out.printf("Articolo: %s | Quantità suggerita: %d | Nuova quantità: ",
                        originalItem.getPartName(), originalItem.getQuantity());

                try {
                    int newQty = Integer.parseInt(SCANNER.nextLine());

                    if (newQty > 0) {
                        // Verifica che la parte esista
                        boolean partExists = availableParts.stream()
                                .anyMatch(part -> part.getName().equals(originalItem.getPartName()));

                        if (!partExists) {
                            System.out.println("ATTENZIONE: La parte '" + originalItem.getPartName() + "' non esiste più nell'inventario. Articolo rimosso.");
                            break;
                        }

                        OrderItemBean modifiedItem = new OrderItemBean();
                        modifiedItem.setPartName(originalItem.getPartName());
                        modifiedItem.setQuantity(newQty);
                        modifiedItems.add(modifiedItem);
                        break;
                    } else if (newQty == 0) {
                        System.out.println("Articolo rimosso dall'ordine.");
                        break;
                    } else {
                        System.out.println("La quantità deve essere >= 0.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Quantità non valida. Inserisci un numero.");
                }
            }
        }

        // Possibilità di aggiungere nuovi articoli
        System.out.println("\nVuoi aggiungere nuovi articoli all'ordine? (s/n): ");
        if (SCANNER.nextLine().equalsIgnoreCase("s")) {
            System.out.println("Parti disponibili nell'inventario:");
            for (PartBean part : availableParts) {
                System.out.println("  - " + part.getName());
            }
            System.out.println("Inserisci i nuovi articoli (nome vuoto per terminare):");

            while (true) {
                System.out.print("Nome parte: ");
                String partName = SCANNER.nextLine().trim();
                if (partName.isBlank()) break;

                // Verifica che la parte esista
                boolean partExists = availableParts.stream()
                        .anyMatch(part -> part.getName().equals(partName));

                if (!partExists) {
                    System.out.println("ERRORE: La parte '" + partName + "' non esiste nell'inventario.");
                    continue; // Riprova con un altro nome
                }

                System.out.print("Quantità: ");
                try {
                    int qty = Integer.parseInt(SCANNER.nextLine());
                    if (qty > 0) {
                        OrderItemBean newItem = new OrderItemBean();
                        newItem.setPartName(partName);
                        newItem.setQuantity(qty);
                        modifiedItems.add(newItem);
                        System.out.println("Articolo aggiunto: " + partName + " x " + qty);
                    } else {
                        System.out.println("La quantità deve essere > 0.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Quantità non valida.");
                }
            }
        }

        return modifiedItems;
    }

    private static void handleVisualizzaOrdini() {
        List<OrderBean> orders = orderBoundary.getAllOrders();
        if (orders.isEmpty()) {
            System.out.println("Nessun ordine disponibile.");
            return;
        }

        System.out.println("\n--- ORDINI EFFETTUATI ---");
        for (OrderBean order : orders) {
            System.out.println("ID Ordine: " + order.getOrderID());
            System.out.println("Fornitore: " + order.getSupplierName());
            System.out.println("Stato: " + order.getStatus());
            System.out.println("Articoli:");
            for (OrderItemBean item : order.getItems()) {
                System.out.println("  - " + item.getPartName() + " x " + item.getQuantity());
            }
            System.out.println("-----------------------------");
        }
    }

    private static void handleImpostaFornitorePredefinito(String username) {
        List<SupplierBean> suppliers = supplierBoundary.getAllSuppliers();
        if (suppliers.isEmpty()) {
            System.out.println("Nessun fornitore disponibile.");
            return;
        }

        System.out.println("Fornitori disponibili:");
        for (SupplierBean s : suppliers) {
            System.out.println("- " + s.getName());
        }

        System.out.print("Inserisci nome del fornitore da impostare come predefinito: ");
        String supplierName = SCANNER.nextLine();

        boolean success = supplierBoundary.setDefaultSupplier(username, supplierName);
        if (success) {
            System.out.println("Fornitore predefinito aggiornato con successo.");
        } else {
            System.out.println("Errore nell'impostare il fornitore predefinito.");
        }
    }
}