package auth;

public class Product {
    private final String productId;

    private String category;
    private double price;

    public Product(String productId, String category, double price) {
        this.productId = productId.toLowerCase();
        this.category = category;
        this.price = price;
    }

    public String getProductId() {
        return this.productId;
}


    public double getPrice() {
        return price;
    }


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }


    public void setPrice(double price) {
        this.price = price;
    }




}
