import java.util.ArrayList;
import java.util.List;

/**
 * Main business class for the simple console project.
 *
 * It keeps users, trains, and tickets in memory. This makes the example easy
 * to run without SQLite while still showing the same basic project logic.
 */
public class RailwaySystem {

    private final List<User> users = new ArrayList<>();
    private final List<Train> trains = new ArrayList<>();
    private final List<Ticket> tickets = new ArrayList<>();
    private int nextPnr = 1001;

    public RailwaySystem() {
        addSampleUsers();
        addSampleTrains();
    }

    private void addSampleUsers() {
        users.add(new User("admin", "admin123", "ADMIN"));
    }

    private void addSampleTrains() {
        trains.add(new Train(101, "Rajdhani Express", "Delhi", "Mumbai", 20, 1500));
        trains.add(new Train(102, "Deccan Express", "Pune", "Mumbai", 15, 800));
        trains.add(new Train(103, "Chennai Express", "Chennai", "Bengaluru", 18, 600));
        trains.add(new Train(104, "Ganga Express", "Delhi", "Kolkata", 12, 1200));
    }

    public boolean registerUser(String username, String password) {
        if (findUser(username) != null) {
            return false;
        }
        users.add(new User(username, password, "USER"));
        return true;
    }

    public User login(String username, String password) {
        User user = findUser(username);
        if (user != null && user.matchesPassword(password)) {
            return user;
        }
        return null;
    }

    public List<Train> searchTrains(String source, String destination) {
        List<Train> results = new ArrayList<>();
        for (Train train : trains) {
            if (train.matchesRoute(source, destination)) {
                results.add(train);
            }
        }
        return results;
    }

    public Train findTrain(int trainNumber) {
        for (Train train : trains) {
            if (train.getNumber() == trainNumber) {
                return train;
            }
        }
        return null;
    }

    public Ticket bookTicket(String username, String passenger, int age,
                             Train train, String travelClass, int seats) {
        if (train == null || seats <= 0 || seats > train.getAvailableSeats()) {
            return null;
        }

        int fare = train.getClassFare(travelClass) * seats;
        train.bookSeats(seats);

        Ticket ticket = new Ticket(
                "PNR" + nextPnr++, username, passenger, age,
                train, travelClass, seats, fare);
        tickets.add(ticket);
        return ticket;
    }

    public Ticket findTicket(String pnr) {
        for (Ticket ticket : tickets) {
            if (ticket.getPnr().equalsIgnoreCase(pnr)) {
                return ticket;
            }
        }
        return null;
    }

    public boolean cancelTicket(Ticket ticket) {
        if (ticket == null || ticket.isCancelled()) {
            return false;
        }

        ticket.cancel();
        ticket.getTrain().releaseSeats(ticket.getSeats());
        return true;
    }

    public List<Ticket> getTicketsForUser(String username) {
        List<Ticket> result = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket.getUsername().equalsIgnoreCase(username)) {
                result.add(ticket);
            }
        }
        return result;
    }

    public void printReport() {
        int confirmed = 0;
        int cancelled = 0;
        int passengers = 0;
        int revenue = 0;

        for (Ticket ticket : tickets) {
            if (ticket.isCancelled()) {
                cancelled++;
            } else {
                confirmed++;
                passengers += ticket.getSeats();
                revenue += ticket.getFare();
            }
        }

        System.out.println("\n--- Admin Report ---");
        System.out.println("Confirmed tickets: " + confirmed);
        System.out.println("Cancelled tickets: " + cancelled);
        System.out.println("Passengers: " + passengers);
        System.out.println("Confirmed revenue: INR " + revenue);
    }

    private User findUser(String username) {
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }
}
