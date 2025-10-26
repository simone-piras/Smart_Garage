package utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Scheduler {
    /*si è scelto di eseguire una prima connessione al database tramite root, in quanto per poter attivare l'event scheduler
     è necessaria l'attivazione da parte di un super-user,seguita dalla sua disconnessione e la successiva connessione da parte
     del loginuser all'interno del main. Abbiamo scelto di attivare lo scheduler tramite una classe supplementare in java
     dato che altrimenti dovrebbe essere attivato ad ogni riavvio del server mysql.
     */
    public static void attivaEventSchedulerComeRoot(){
        try ( Connection conn = DBConnection.getConnection();
              Statement stmt = conn.createStatement()){
            stmt.execute("set global event_scheduler = on");
            System.out.println("Event Scheduler attivato");
        } catch (SQLException e) {
            System.err.println("Errore attivazione event scheduler: " + e.getMessage());
        }
    }
}
