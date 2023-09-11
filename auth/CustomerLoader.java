package auth;

import java.nio.file.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CustomerLoader {

    private Path filePath;

    public CustomerLoader(String path) {
        this.filePath = Paths.get(path);
    }

    public List<Customer> loadCustomers() {
        List<Customer> customers = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(filePath);

            for (String line : lines) {
                String[] parts = line.split(",");

                if (parts.length == 4) {
                    String name = parts[0].trim();
                    String id = parts[1].trim();
                    String phoneNumber = parts[2].trim();
                    String type = parts[3].trim().toLowerCase();

                    switch (type) {
                        case "new":
                            customers.add(new NewCustomer(name, id, phoneNumber));
                            break;
                        case "vip":
                            customers.add(new VIPCustomer(name, id, phoneNumber));
                            break;
                        case "returning":
                            customers.add(new ReturningCustomer(name, id, phoneNumber));
                            break;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }

        return customers;
    }
}