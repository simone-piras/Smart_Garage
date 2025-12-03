package cli;

import bean.*;
import boundary.*;
import exception.InsufficientStockException;
import exception.PartNotFoundException;
import utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/*
 Utilizza System.out per l'interazione utente in ambiente CLI.
 In un'applicazione CLI, System.out è lo standard per l'output utente.
 L'uso di logger sarebbe inappropriato per l'interazione diretta con l'utente.
 */
@SuppressWarnings("java:S106")
public class GarageManagerViewCLI {
    //Singleton delle boundary per tutta la CLI, ogni boundary usa il costruttore senza parametri che si appoggia a SHaredManagers
    private static final InventoryBoundary inventoryBoundary = new InventoryBoundary();
    private static final NotificationBoundary notificationBoundary = new NotificationBoundary();
    private static final OrderBoundary orderBoundary = new OrderBoundary();
    private static final SupplierBoundary supplierBoundary = new SupplierBoundary();
    private static final UserBoundary userBoundary = new UserBoundary();
    private static final Scanner SCANNER = new Scanner(System.in);

    //COSTANTI PER RISOLVERE GLI ISSUE
    private static final String MSG_QUANTITA_NON_VALIDA = "Quantità non valida.";
    private static final String MSG_NESSUN_FORNITORE = "Nessun fornitore disponibile.";
    private static final String MSG_OPZIONE_NON_VALIDA = "Opzione non valida.";
    private static final String MSG_OPERAZIONE_ANNULLATA = "Operazione annullata.";
    private static final String MSG_ARTICOLO_RIMOSSO = "Articolo rimosso dall'ordine.";

    private static final String MENU_TITLE = "========== GESTORE OFFICINA ==========";
    private static final String[] MENU_OPTIONS = {
            "1. Visualizza inventario",
            "2. Modifica quantità scorta",
            "3. Effettua ordine",
            "4. Visualizza fornitori",
            "5. Visualizza notifiche",
            "6. Visualizza ordini effettuati",
            "7. Imposta fornitore predefinito",
            "8. Logout e Torna al Menu Principale"
    };

    private GarageManagerViewCLI() {}

    public static void start(String username) {
        // Appena l'utente entra, controlliamo se ha scorte basse e generiamo notifiche
        inventoryBoundary.scanInventoryForLowStock();
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
                case "8" ->{
                    System.out.println("Effettuo logout...");
                    SessionManager.getInstance().logout();
                    exit = true;
                }
                default -> System.out.println(MSG_OPZIONE_NON_VALIDA);
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
        } catch (NumberFormatException _) {
            System.out.println(MSG_QUANTITA_NON_VALIDA);
        } catch (InsufficientStockException _) {
            System.out.println("Errore: quantità insufficiente per la parte \"" + nomeParte + "\".");
        } catch (PartNotFoundException _) {
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
            } catch (NumberFormatException _) {
                System.out.println(MSG_QUANTITA_NON_VALIDA);
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
                System.out.println(MSG_NESSUN_FORNITORE);
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
            System.out.println(MSG_NESSUN_FORNITORE);
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

    //METODO SCOMPOSTO per ridurre Cognitive Complexity
    private static void handleVisualizzaNotifiche(String username) {
        List<NotificationBean> notifications = notificationBoundary.getAllNotifications();
        List<OrderItemBean> suggestedItems = notificationBoundary.getSuggestedOrderItems();

        if (notifications.isEmpty() && suggestedItems.isEmpty()) {
            System.out.println("Nessuna notifica.");
            return;
        }

        mostraNotificheOrdini(notifications);
        mostraNotificheScorte(notifications);
        gestisciOrdineSuggerito(username, suggestedItems);
    }

    private static void mostraNotificheOrdini(List<NotificationBean> notifications) {
        boolean hasOrderNotifications = false;
        for (NotificationBean n : notifications) {
            if (isNotificaOrdine(n)) {
                if (!hasOrderNotifications) {
                    System.out.println("--- NOTIFICHE ORDINI ---");
                    hasOrderNotifications = true;
                }
                System.out.println("- " + n.getMessage());
            }
        }
    }

    private static void mostraNotificheScorte(List<NotificationBean> notifications) {
        boolean hasStockNotifications = false;
        for (NotificationBean n : notifications) {
            if (isNotificaScorte(n)) {
                if (!hasStockNotifications) {
                    System.out.println("\n--- NOTIFICHE SCORTE ---");
                    hasStockNotifications = true;
                }
                System.out.println("- " + n.getMessage());
            }
        }
    }

    private static boolean isNotificaOrdine(NotificationBean n) {
        return n.getPartName() == null ||
                (n.getMessage() != null &&
                        (n.getMessage().contains("Ordine") || n.getMessage().contains("ORDINE CONSEGNATO")));
    }

    private static boolean isNotificaScorte(NotificationBean n) {
        return n.getPartName() != null &&
                n.getMessage() != null &&
                n.getMessage().contains("Scorte basse");
    }

    private static void gestisciOrdineSuggerito(String username, List<OrderItemBean> suggestedItems) {
        if (suggestedItems.isEmpty()) return;

        mostraArticoliSuggeriti(suggestedItems);
        processaSceltaOrdineSuggerito(username, suggestedItems);
    }

    private static void mostraArticoliSuggeriti(List<OrderItemBean> suggestedItems) {
        System.out.println("\n--- ORDINE SUGGERITO ---");
        for (OrderItemBean item : suggestedItems) {
            System.out.println("- " + item.getPartName() + " x " + item.getQuantity());
        }

        System.out.println("\nOpzioni:");
        System.out.println("1. Conferma ordine suggerito");
        System.out.println("2. Modifica ordine suggerito");
        System.out.println("3. Annulla");
        System.out.print("Scegli un'opzione: ");
    }

    private static void processaSceltaOrdineSuggerito(String username, List<OrderItemBean> suggestedItems) {
        String choice = SCANNER.nextLine();

        switch (choice) {
            case "1" -> confermaOrdineSuggerito(username, suggestedItems);
            case "2" -> modificaEConfermaOrdine(username, suggestedItems);
            case "3" -> System.out.println(MSG_OPERAZIONE_ANNULLATA);
            default -> System.out.println(MSG_OPZIONE_NON_VALIDA);
        }
    }

    private static void confermaOrdineSuggerito(String username, List<OrderItemBean> suggestedItems) {
        String supplierName = selezionaFornitore(userBoundary.getUser(username));
        if (supplierName != null) {
            orderBoundary.createSuggestedOrder(username, suggestedItems, supplierName);
            System.out.println("Ordine suggerito creato con successo.");
        }
    }

    private static void modificaEConfermaOrdine(String username, List<OrderItemBean> suggestedItems) {
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

    //METODO SCOMPOSTO per ridurre Cognitive Complexity
    private static List<OrderItemBean> modificaOrdineSuggerito(List<OrderItemBean> suggestedItems) {
        List<OrderItemBean> modifiedItems = modificaArticoliEsistenti(suggestedItems);
        return aggiungiNuoviArticoli(modifiedItems);
    }

    private static List<OrderItemBean> modificaArticoliEsistenti(List<OrderItemBean> suggestedItems) {
        List<OrderItemBean> modifiedItems = new ArrayList<>();
        List<PartBean> availableParts = inventoryBoundary.getAllParts();

        System.out.println("\n--- MODIFICA ORDINE SUGGERITO ---");
        System.out.println("Per ogni articolo, inserisci la nuova quantità (0 per rimuovere):");

        for (OrderItemBean originalItem : suggestedItems) {
            OrderItemBean modifiedItem = modificaSingoloArticolo(originalItem, availableParts);
            if (modifiedItem != null) {
                modifiedItems.add(modifiedItem);
            }
        }
        return modifiedItems;
    }

    private static OrderItemBean modificaSingoloArticolo(OrderItemBean originalItem, List<PartBean> availableParts) {
        while (true) {
            System.out.printf("Articolo: %s | Quantità suggerita: %d | Nuova quantità: ",
                    originalItem.getPartName(), originalItem.getQuantity());

            try {
                int newQty = Integer.parseInt(SCANNER.nextLine());
                return gestisciQuantitaArticolo(originalItem, newQty, availableParts);
            } catch (NumberFormatException _) {
                System.out.println("Quantità non valida. Inserisci un numero.");
            }
        }
    }

    private static OrderItemBean gestisciQuantitaArticolo(OrderItemBean item, int newQty, List<PartBean> availableParts) {
        if (newQty > 0) {
            if (!verificaParteEsiste(item.getPartName(), availableParts)) {
                System.out.println("ATTENZIONE: La parte '" + item.getPartName() + "' non esiste più nell'inventario. Articolo rimosso.");
                return null;
            }
            OrderItemBean modifiedItem = new OrderItemBean();
            modifiedItem.setPartName(item.getPartName());
            modifiedItem.setQuantity(newQty);
            return modifiedItem;
        } else if (newQty == 0) {
            System.out.println(MSG_ARTICOLO_RIMOSSO);
            return null;
        } else {
            System.out.println("La quantità deve essere >= 0.");
            return null;
        }
    }

    private static boolean verificaParteEsiste(String partName, List<PartBean> availableParts) {
        return availableParts.stream().anyMatch(part -> part.getName().equals(partName));
    }

    private static List<OrderItemBean> aggiungiNuoviArticoli(List<OrderItemBean> currentItems) {
        System.out.println("\nVuoi aggiungere nuovi articoli all'ordine? (s/n): ");
        if (!SCANNER.nextLine().equalsIgnoreCase("s")) {
            return currentItems;
        }

        mostraPartiDisponibili();
        return raccogliNuoviArticoli(currentItems);
    }

    private static void mostraPartiDisponibili() {
        System.out.println("Parti disponibili nell'inventario:");
        for (PartBean part : inventoryBoundary.getAllParts()) {
            System.out.println("  - " + part.getName());
        }
        System.out.println("Inserisci i nuovi articoli (nome vuoto per terminare):");
    }

    private static List<OrderItemBean> raccogliNuoviArticoli(List<OrderItemBean> currentItems) {
        List<OrderItemBean> result = new ArrayList<>(currentItems);

        while (true) {
            System.out.print("Nome parte: ");
            String partName = SCANNER.nextLine().trim();

            if (partName.isBlank()) {
                break;
            }

            if (verificaParteEsiste(partName, inventoryBoundary.getAllParts())) {
                OrderItemBean newItem = creaNuovoArticolo(partName);
                if (newItem != null) {
                    result.add(newItem);
                }
            } else {
                System.out.println("ERRORE: La parte '" + partName + "' non esiste nell'inventario.");
            }
        }
        return result;
    }

    private static OrderItemBean creaNuovoArticolo(String partName) {
        System.out.print("Quantità: ");
        try {
            int qty = Integer.parseInt(SCANNER.nextLine());
            if (qty > 0) {
                OrderItemBean newItem = new OrderItemBean();
                newItem.setPartName(partName);
                newItem.setQuantity(qty);
                System.out.println("Articolo aggiunto: " + partName + " x " + qty);
                return newItem;
            } else {
                System.out.println("La quantità deve essere > 0.");
            }
        } catch (NumberFormatException _) {
            System.out.println(MSG_QUANTITA_NON_VALIDA);
        }
        return null;
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
            System.out.println(MSG_NESSUN_FORNITORE);
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
