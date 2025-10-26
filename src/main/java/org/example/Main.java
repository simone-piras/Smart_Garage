package org.example;

import cli.MainViewCLI;
import enumerations.PersistenceType;
import utils.ApplicationContext;
import utils.DataLoader;
import utils.FileDataLoader;
import utils.Scheduler;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== SMART GARAGE ===");
        System.out.println("Seleziona tipo di persistenza:");
        System.out.println("1. In-Memory");
        System.out.println("2. Database");
        System.out.println("3. File");
        System.out.print("Scelta: ");

        Scanner scanner = new Scanner(System.in);
        String scelta = scanner.nextLine();

        switch (scelta) {
            case "1" -> {
                ApplicationContext.getInstance().setPersistenceType(PersistenceType.IN_MEMORY);

                DataLoader loader = new DataLoader();
                loader.load();
            }
            case "2" -> {
                ApplicationContext.getInstance().setPersistenceType(PersistenceType.DATABASE);
                Scheduler.attivaEventSchedulerComeRoot();
            }
            case "3" -> {
                 ApplicationContext.getInstance().setPersistenceType(PersistenceType.FILE);
                FileDataLoader fileDataLoader = new FileDataLoader();
                fileDataLoader.load();
            }
            default -> {
                System.out.println("Scelta non valida. Uscita.");
                return;
            }
        }
        System.out.println("=== SMART GARAGE ===");
        System.out.println("Vuoi usare:");
        System.out.println("1. Interfaccia CLI");
        System.out.println("2. Interfaccia grafica(GUI)");
        System.out.print("Scelta: ");
        String interfaccia=scanner.nextLine();
        if(interfaccia.equals("2")){
            gui.MainGUI.main(args);
        }else {
            new MainViewCLI().start();
        }
    }
}
