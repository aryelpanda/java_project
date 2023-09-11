package auth;


public class ReturningCustomer extends Customer {

    public ReturningCustomer(String fullName, String idNumber, String phone) {
        super(fullName, idNumber, phone, "returning");
    }


    @Override
    public double getPromotionDiscount() {
        return 0.10; // Assuming a 10% discount for new customers
    }
}