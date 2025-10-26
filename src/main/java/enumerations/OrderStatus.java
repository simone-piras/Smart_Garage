package enumerations;

public enum OrderStatus {
    CREATING,
    IN_PROCESS,
    SHIPPED,
    DELIVERED,
    CANCELLED; //da levare

    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase().replace("_", " ");

    }
}
