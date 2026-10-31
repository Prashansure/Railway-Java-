/** Stores the basic details of one train. */
public class Train {

    private final int number;
    private final String name;
    private final String source;
    private final String destination;
    private final int fare;
    private int availableSeats;

    public Train(int number, String name, String source, String destination,
                 int availableSeats, int fare) {
        this.number = number;
        this.name = name;
        this.source = source;
        this.destination = destination;
        this.availableSeats = availableSeats;
        this.fare = fare;
    }

    public boolean matchesRoute(String source, String destination) {
        return this.source.equalsIgnoreCase(source)
                && this.destination.equalsIgnoreCase(destination);
    }

    public void bookSeats(int seats) {
        availableSeats -= seats;
    }

    public void releaseSeats(int seats) {
        availableSeats += seats;
    }

    public int getClassFare(String travelClass) {
        return switch (travelClass) {
            case "1A" -> fare * 2;
            case "2A" -> fare * 3 / 2;
            case "CC" -> fare * 4 / 5;
            default -> fare;
        };
    }

    public void printSummary() {
        System.out.println(number + " - " + name
                + " | " + source + " to " + destination
                + " | seats: " + availableSeats
                + " | starting fare: INR " + fare);
    }

    public int getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }
}
