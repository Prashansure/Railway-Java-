import java.util.List;
import java.util.Scanner;

/**
 * Console entry point for the beginner-friendly railway project.
 *
 * Main is responsible for reading input and displaying menus. The actual
 * railway work is handled by RailwaySystem and the model classes.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        RailwaySystem railway = new RailwaySystem();

        System.out.println("=======================================");
        System.out.println("   SIMPLE RAILWAY RESERVATION SYSTEM");
        System.out.println("=======================================");
        System.out.println("Demo admin login: admin / admin123");

        while (true) {
            System.out.println("\n1. Login");
            System.out.println("2. Register");
            System.out.println("0. Exit");

            int choice = readInt("Choose an option: ");

            if (choice == 1) {
                login(railway);
            } else if (choice == 2) {
                register(railway);
            } else if (choice == 0) {
                System.out.println("Thank you for using the railway system.");
                break;
            } else {
                System.out.println("Please choose a valid option.");
            }
        }
    }

    private static void register(RailwaySystem railway) {
        System.out.println("\n--- Register New User ---");
        String username = readText("Enter username: ");
        String password = readText("Enter password: ");

        if (railway.registerUser(username, password)) {
            System.out.println("Registration successful.");
        } else {
            System.out.println("Username already exists.");
        }
    }

    private static void login(RailwaySystem railway) {
        System.out.println("\n--- Login ---");
        String username = readText("Username: ");
        String password = readText("Password: ");
        User user = railway.login(username, password);

        if (user == null) {
            System.out.println("Invalid username or password.");
            return;
        }

        System.out.println("Login successful. Welcome, " + user.getUsername() + "!");
        userMenu(railway, user);
    }

    private static void userMenu(RailwaySystem railway, User user) {
        while (true) {
            System.out.println("\n--- User Menu ---");
            System.out.println("1. Search trains");
            System.out.println("2. Book ticket");
            System.out.println("3. Check PNR status");
            System.out.println("4. Cancel ticket");
            System.out.println("5. View my bookings");

            if (user.isAdmin()) {
                System.out.println("6. Admin report");
            }

            System.out.println("0. Logout");
            int choice = readInt("Choose an option: ");

            switch (choice) {
                case 1 -> searchTrains(railway);
                case 2 -> bookTicket(railway, user);
                case 3 -> checkPnr(railway);
                case 4 -> cancelTicket(railway, user);
                case 5 -> viewMyBookings(railway, user);
                case 6 -> {
                    if (user.isAdmin()) {
                        railway.printReport();
                    } else {
                        System.out.println("That option is available only to the admin.");
                    }
                }
                case 0 -> {
                    System.out.println("Logged out.");
                    return;
                }
                default -> System.out.println("Please choose a valid option.");
            }
        }
    }

    private static void searchTrains(RailwaySystem railway) {
        System.out.println("\n--- Search Trains ---");
        String source = readText("From: ");
        String destination = readText("To: ");
        List<Train> results = railway.searchTrains(source, destination);

        if (results.isEmpty()) {
            System.out.println("No trains found for this route.");
            return;
        }

        for (Train train : results) {
            train.printSummary();
        }
    }

    private static void bookTicket(RailwaySystem railway, User user) {
        System.out.println("\n--- Book Ticket ---");
        String source = readText("From: ");
        String destination = readText("To: ");
        List<Train> results = railway.searchTrains(source, destination);

        if (results.isEmpty()) {
            System.out.println("No trains found for this route.");
            return;
        }

        for (Train train : results) {
            train.printSummary();
        }

        int trainNumber = readInt("Enter train number: ");
        Train selectedTrain = railway.findTrain(trainNumber);
        if (selectedTrain == null
                || !selectedTrain.matchesRoute(source, destination)) {
            System.out.println("Invalid train selection.");
            return;
        }

        String passenger = readText("Passenger name: ");
        int age = readInt("Passenger age: ");
        String travelClass = readText("Class (1A, 2A, SL, or CC): ").toUpperCase();
        int seats = readInt("Number of seats: ");

        if (seats <= 0 || seats > selectedTrain.getAvailableSeats()) {
            System.out.println("Requested seats are not available.");
            return;
        }

        Ticket ticket = railway.bookTicket(
                user.getUsername(), passenger, age, selectedTrain,
                travelClass, seats);

        if (ticket == null) {
            System.out.println("Booking failed. Please check the details.");
        } else {
            System.out.println("Booking successful.");
            System.out.println("Your PNR is: " + ticket.getPnr());
            System.out.println("Total fare: INR " + ticket.getFare());
        }
    }

    private static void checkPnr(RailwaySystem railway) {
        System.out.println("\n--- Check PNR ---");
        String pnr = readText("Enter PNR: ");
        Ticket ticket = railway.findTicket(pnr);

        if (ticket == null) {
            System.out.println("PNR not found.");
        } else {
            ticket.printDetails();
        }
    }

    private static void cancelTicket(RailwaySystem railway, User user) {
        System.out.println("\n--- Cancel Ticket ---");
        String pnr = readText("Enter PNR: ");
        Ticket ticket = railway.findTicket(pnr);

        if (ticket == null) {
            System.out.println("PNR not found.");
        } else if (!ticket.getUsername().equals(user.getUsername())) {
            System.out.println("You can cancel only your own tickets.");
        } else if (ticket.isCancelled()) {
            System.out.println("This ticket is already cancelled.");
        } else if (railway.cancelTicket(ticket)) {
            System.out.println("Ticket cancelled successfully.");
            System.out.println("Estimated refund: INR " + ticket.getRefund());
        }
    }

    private static void viewMyBookings(RailwaySystem railway, User user) {
        System.out.println("\n--- My Bookings ---");
        List<Ticket> tickets = railway.getTicketsForUser(user.getUsername());

        if (tickets.isEmpty()) {
            System.out.println("You do not have any bookings.");
            return;
        }

        for (Ticket ticket : tickets) {
            ticket.printDetails();
        }
    }

    private static String readText(String message) {
        while (true) {
            System.out.print(message);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("This field cannot be empty.");
        }
    }

    private static int readInt(String message) {
        while (true) {
            System.out.print(message);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException exception) {
                System.out.println("Please enter a number.");
            }
        }
    }
}
