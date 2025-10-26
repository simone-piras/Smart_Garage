package utils;
import utils.DAOFactory;
import enumerations.PersistenceType;

public class ApplicationContext {

    private static ApplicationContext instance;

    private DAOFactory daoFactory;
    private PersistenceType persistenceType;

    // Costruttore privato per impedire creazione di istanze esterne
    private ApplicationContext() {
        // Default  IN_MEMORY se non settato
        this.persistenceType = PersistenceType.IN_MEMORY;
        this.daoFactory = DAOFactory.getDAOFactory(this.persistenceType);
    }

    // Metodo pubblico per accedere all'istanza Singleton
    public static synchronized ApplicationContext getInstance() {
        if (instance == null) {
            instance = new ApplicationContext();
        }
        return instance;
    }

    // Imposta il tipo di persistenza (va fatto nel main, all'avvio)
    public void setPersistenceType(PersistenceType persistenceType) {
        this.persistenceType = persistenceType;
        this.daoFactory = DAOFactory.getDAOFactory(persistenceType);
    }

    // Ottieni il tipo di persistenza corrente
    public PersistenceType getPersistenceType() {
        return this.persistenceType;
    }

    // Ottieni la DAOFactory configurata
    public DAOFactory getDAOFactory() {
        return this.daoFactory;
    }
}
/*> “Garantisce che una classe abbia una sola istanza e fornisce un punto globale di accesso a essa.”



Nel nostro caso:

public class ApplicationContext {
    private static ApplicationContext instance;
    private DAOFactory daoFactory;

    private ApplicationContext() {}

    public static synchronized ApplicationContext getInstance() {
        if (instance == null) {
            instance = new ApplicationContext();
        }
        return instance;
    }

    public void setDaoFactory(DAOFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    public DAOFactory getDaoFactory() {
        return daoFactory;
    }
}

Confronto con il pattern Singleton del GoF

Questa implementazione:

Ha un costruttore privato (private ApplicationContext()), che impedisce la creazione di istanze dall'esterno → ✔️

Ha una variabile statica instance per tenere l’unica istanza → ✔️

Ha un metodo getInstance() che controlla e restituisce sempre la stessa istanza → ✔️*/

