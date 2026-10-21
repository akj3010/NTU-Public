package restaurant.domain;

/** A table. It stays occupied until its customer pays or leaves. */
public class Table {
    private final int id;
    private Customer occupant;

    public Table(int id) {
        this.id = id;
    }

    public boolean isFree() {
        return occupant == null;
    }

    public void seat(Customer customer) {
        if (!isFree()) {
            throw new IllegalStateException("Table " + id + " is already occupied.");
        }
        this.occupant = customer;
    }

    public void vacate() {
        this.occupant = null;
    }

    public Customer getOccupant() {
        return occupant;
    }

    public int getId() {
        return id;
    }
}
