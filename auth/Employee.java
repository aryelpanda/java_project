package auth;


import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Employee extends User {

    private static ArrayList<Customer> customerList = new ArrayList<>();

    private static Map<String ,Customer> customerMap = new HashMap<>();
    private String accountNumber;
    private String branchAffiliation;
    private String employeeNumber;
    private String role; // 'shift manager', 'cashier', 'salesperson'

    private Branch branch;

    private String branchNumber;
    String EmployeeNumber;

    public Employee(String fullName, String id, String username, String password, String phoneNumber, String accountNumber, String branchNumber, String employeeNumber, String role) {
        super(fullName, id, username, password, phoneNumber);
        this.accountNumber = accountNumber;
        this.branchNumber = branchNumber;
        this.employeeNumber = employeeNumber;
        this.role = role;

    }

    public static boolean addCustomer(Customer customer)
    {
        if(customer == null || customerMap.containsKey(customer.getId()))
        {
            return false;
        }
        customerMap.put(customer.getId(), customer);
        return true;
    }

    public static boolean removeCustomer(String customerId)
    {
        if(!customerMap.containsKey(customerId))
        {
            return false;
        }

        customerMap.remove(customerId);
        return true;

    }

    public ArrayList<String> getSoldProductsList()
    {
        return branch.getSoldProductsList();
    }

    public  ArrayList<String> getCategoryList()
    {
        return branch.getCategoryList();
    }
    public boolean makePurchase(String productId, String customerID)
    {


        if (!customerMap.containsKey(customerID))
        {
            return false;
        }

        return branch.makePurchase(productId,customerMap.get(customerID));
    }
    public String getBranchNumber() {
        return branchNumber;
    }

    public int getSoldProductsReport(String productId)
    {
        return  branch.getSoldProduct(productId);
    }

    public int getReportByCategory(String category)
    {
        return branch.getSoldByCategory(category);
    }
    public void setBranch(Branch branch)
    {
        this.branch = branch;
    }

    public String getBranchData() {
        String data = null;
        switch (role.toLowerCase()) {
            case "shift manager":
                data = branch.getAllData();
                break;
            case "cashier":
                data = branch.getCashierData();
                break;
            case "salesperson":
                data = branch.getSalesData();
                break;
        }
        return data;

    }

    public String getAccountNumber() {
        return accountNumber;
    }



    public String getRole() {
        return role;
    }

    public Branch getBranch() {
        return branch;
    }
    public boolean buyProduct(String productID)
    {
        return branch.buyProduct(productID);
    }
    public void createReportsFile(String type,String item) throws IOException {
        File file = new File(type.toLowerCase()+"-report-"+branchNumber+".txt");
        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

        if (type.equalsIgnoreCase("branch"))
        {
            bufferedWriter.write(getBranchData());
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }
        else if (type.equalsIgnoreCase("PRODUCT"))
        {
            bufferedWriter.write(item + " sold: " +getSoldProductsReport(item));
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }

        else
        {
            bufferedWriter.write(item+ " sold: " +getReportByCategory(item));
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }
    }

}
