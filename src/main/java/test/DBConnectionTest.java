package test;

import utils.DBConnection;

import java.sql.Connection;

public class DBConnectionTest {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("✅ Connessione al database riuscita!");
        } catch (Exception e) {
            System.err.println("❌ Errore di connessione: " + e.getMessage());
        }
    }
}
