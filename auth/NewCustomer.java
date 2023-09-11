package auth;


public class NewCustomer extends Customer {

    public NewCustomer(String fullName, String idNumber, String phone) {
        super(fullName, idNumber, phone, "new");
    }


    @Override
    public double getPromotionDiscount() {
        return 0.05; // Assuming a 5% discount for new customers
    }
}

// Similarly for Returning and VIP customers
