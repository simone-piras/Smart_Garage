package FileDAO;

import DAO.SupplierDAO;
import entity.SupplierEntity;
import java.io.*;
import java.util.*;

public class FileSuppliersDAO implements SupplierDAO {

    private static final String FILE_PATH = "data/suppliers.txt";
    private int nextId = 1;

    @Override
    public void saveSupplier(SupplierEntity supplier) {
        List<SupplierEntity> allSuppliers = getAllSuppliers();

        //Trova il prossimo ID disponibile
        if (allSuppliers.isEmpty()) {
            nextId = 1;
        } else {
            nextId = allSuppliers.stream().mapToInt(SupplierEntity::getId).max().orElse(0) + 1;
        }

        //Se il fornitore non ha ID, gliene assegno uno
        if (supplier.getId() == 0) {
            supplier.setId(nextId);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(formatSupplier(supplier));
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Errore scrittura fornitore su file: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<SupplierEntity> getSupplierByName(String name) {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                SupplierEntity supplier = parseSupplier(line);
                if (supplier.getName().equals(name)) return Optional.of(supplier);
            }
        } catch (IOException e) {
            throw new RuntimeException("Errore lettura fornitore da file: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<SupplierEntity> getAllSuppliers() {
        List<SupplierEntity> suppliers = new ArrayList<>();
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return suppliers;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    suppliers.add(parseSupplier(line));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Errore lettura fornitori da file: " + e.getMessage(), e);
        }
        return suppliers;
    }

    private String formatSupplier(SupplierEntity s) {
        return s.getId() + "|" + s.getName() + "|" +
                (s.getEmail() == null ? "" : s.getEmail()) + "|" +
                (s.getPhone() == null ? "" : s.getPhone()) + "|" +
                s.isDefault();
    }

    private SupplierEntity parseSupplier(String line) {
        try {
            String[] parts = line.split("\\|");
            //CONTROLLA se ci sono almeno 5 parti
            if (parts.length >= 5) {
                int id = Integer.parseInt(parts[0]);
                String name = parts[1];
                String email = parts.length > 2 && !parts[2].isEmpty() ? parts[2] : null;
                String phone = parts.length > 3 && !parts[3].isEmpty() ? parts[3] : null;
                boolean isDefault = parts.length > 4 && Boolean.parseBoolean(parts[4]);
                return new SupplierEntity(id, name, email, phone, isDefault);
            }
            //SE MANCA L'ID
            else if (parts.length == 4) {
                String name = parts[0];
                String email = !parts[1].isEmpty() ? parts[1] : null;
                String phone = !parts[2].isEmpty() ? parts[2] : null;
                boolean isDefault = Boolean.parseBoolean(parts[3]);
                //Genera un ID temporaneo basato sull'hash del nome
                int tempId = Math.abs(name.hashCode());
                return new SupplierEntity(tempId, name, email, phone, isDefault);
            } else {
                throw new RuntimeException("Formato file fornitori non valido: " + line);
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("Errore parsing ID nel file fornitori: " + line, e);
        }
    }
}