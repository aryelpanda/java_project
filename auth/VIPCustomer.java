package auth;


public class VIPCustomer extends Customer {
    public VIPCustomer(String fullName, String idNumber, String phone) {
        super(fullName, idNumber, phone, "vip");
    }


    @Override
    public double getPromotionDiscount() {
        return 0.15; // Assuming a 15% discount for new customers
    }
}