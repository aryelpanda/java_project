package auth;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductLoader {
    private Path filePath;

    private Map<String, Product> products ;
    public ProductLoader(String path) {
        this.filePath = Paths.get(path);
        this.products = new HashMap<>();
    }

    public void loadData(Map<String, Branch> branches) {

        try {
            List<String> lines = Files.readAllLines(filePath);

            for (String line : lines) {
                if (line.startsWith("Product:")) {
                    String[] parts = line.substring(8).split(",");
                    Product product = new Product(parts[0], parts[1], Integer.parseInt(parts[2]));
                    products.put(parts[0], product);
                } else if (!line.isEmpty()) {
                    String[] parts = line.split(":");
                    String branchId = parts[0];
                    String[] productData = parts[1].split(",");
                    Product product = products.get(productData[0]);
                    int quantity = Integer.parseInt(productData[1]);
                    branches.get(branchId).addProduct(product, quantity);
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());

        }
    }

    public Map<String,Product> getAllProducts()
    {
        return products;
    }
}