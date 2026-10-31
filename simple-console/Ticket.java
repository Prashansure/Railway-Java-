/** Represents one railway reservation made by a user. */
public class Ticket {

    private final String pnr;
    private final String username;
    private final String passenger;
    private final int age;
    private final Train train;
    private final String travelClass;
    private final int seats;
    private final int fare;
    private String status;

    public Ticket(String pnr, String username, String passenger, int age,
                  Train train, String travelClass, int seats, int fare) {
        this.pnr = pnr;
        this.username = username;
        this.passenger = passenger;
        this.age = age;
        this.train = train;
        this.travelClass = travelClass;
        this.seats = seats;
        this.fare = fare;
        this.status = "CONFIRMED";
    }

    public void cancel() {
        status = "CANCELLED";
    }

    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }

    public int getRefund() {
        return fare * 75 / 100;
    }

    public void printDetails() {
        System.out.println("PNR: " + pnr
                + " | Passenger: " + passenger
                + " | Train: " + train.getName()
                + " | Class: " + travelClass
                + " | Seats: " + seats
                + " | Fare: INR " + fare
                + " | Status: " + status);
    }

    public String getPnr() {
        return pnr;
    }

    public String getUsername() {
        return username;
    }

    public int getFare() {
        return fare;
    }

    public int getSeats() {
        return seats;
    }

    public Train getTrain() {
        return train;
    }
}
