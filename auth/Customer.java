package auth;

public abstract class Customer {
    protected String customerType;

    private String fullName;

    private String id;

    private String phoneNumber;
    public Customer(String fullName, String idNumber, String phone, String customerType) {
        this.fullName = fullName;
        this.id = idNumber;
        this.phoneNumber = phone;
        this.customerType = customerType;
    }
    public double purchase(double productPrice)
    {
        return productPrice - productPrice*getPromotionDiscount();
    }
    public String getId()
    {
        return id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getCustomerType() {
        return customerType;
    }

    public String getFullName() {
        return fullName;
    }
    @Override
    public String toString() {
        return "Customer [name=" + fullName + ", id=" + id + ", phoneNumber=" + phoneNumber + ", type=" + customerType + "]";
    }


    public abstract double getPromotionDiscount();

}

