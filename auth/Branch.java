package auth;

import java.util.*;

public class Branch {


    private final String branchId;
    private String branchName;
    private Inventory inventory;

    private int numOfSales;
    private double branchBalance;

    private Map<String, Employee> employeeMap;

    private int numOfEmployees;

    private Map<String,Integer> categories;
    Map<String,Integer> soldProducts;
    public Branch(String branchId, String branchName) {
        this.branchId = branchId;
        this.branchName = branchName;
        this.branchBalance = 10000;
        this.numOfSales = 0;
        this.numOfEmployees = 0;
        this.employeeMap = new HashMap<>();
        this.soldProducts = new HashMap<>();
        this.categories = new HashMap<>();
        this.inventory = new Inventory(branchId);
    }

    public int showNumberOfSales(){
        return numOfSales;
    }
    public int getSoldProduct(String  productId)
    {
        if (!soldProducts.containsKey(productId))
        {
            return  -1;
        }
        return soldProducts.get(productId);
    }





    public boolean makePurchase(String productId, Customer customer)
    {
        if (inventory.contains(productId)<=0)
        {
            return false;
        }

        soldProducts.put(productId,soldProducts.get(productId)+1);
        categories.put(inventory.getProductCategory(productId),categories.get(inventory.getProductCategory(productId))+1);
        branchBalance += customer.purchase(inventory.sellProduct(productId));
        numOfSales++;
        return true;
    }

    public boolean sellProduct(String product,String customerType)
    {
        if (inventory.contains(product)<=0)
        {
            return false;
        }

        numOfSales++;
        branchBalance+=inventory.sellProduct(product);

        return true;
    }

    public boolean addEmployee(Employee employee) {
        if (employeeMap.containsKey(employee.getUsername())) {
            return false;
        }
        numOfEmployees ++;
        employeeMap.put(employee.getUsername(), employee);
        return true;
    }



    public void increment(Map<Product, Integer> map, Product key) {
        map.putIfAbsent(key, 0);
        map.put(key, map.get(key) + 1);
    }

    public String getSalesData() {
        return "Branch: "+ branchName+" Sales: " + Integer.toString(numOfSales)+".";
    }

    public String getCashierData() {
        return getSalesData() + " Balance: " + Double.toString(branchBalance) + ".";
    }

    public String getAllData() {
        return getCashierData() + " Num of Employees: " + numOfEmployees + ".";
    }


    public boolean addProduct(Product product,int amount) {
        if (!soldProducts.containsKey(product.getProductId()))
        {
            soldProducts.put(product.getProductId(),0);
        }
        if (!categories.containsKey(product.getCategory()))
        {
            categories.put(product.getCategory(),0);
        }

        return inventory.addProduct(product,amount);
    }


    public boolean deleteProduct(String productId) {
        if (inventory.contains(productId)==-1) {
            return false;
        }

        inventory.removeProduct(productId);
        return true;
    }

    public Product getProductById(String productId) {
        return inventory.getProductById(productId);
    }

    public int getProductAmount(String productId)
    {
        return inventory.contains(productId);
    }


    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getBranchId() {
        return branchId;
    }

    public ArrayList<String> getProducts() {
        return inventory.getInventory();
    }


    public boolean buyProduct(String productID)

    {
        if (inventory.getProductById(productID) == null)
        {
            return false;
        }
        if (branchBalance -inventory.getProductById(productID).getPrice()>0)
        {
            branchBalance-=inventory.getProductById(productID).getPrice();
            return inventory.buyProduct(productID);
        }
        return false;
    }

    public ArrayList<String> getProductsList()
    {
        return inventory.getProductsList();
    }

    public ArrayList<String> getSoldProductsList()
    {
        return new ArrayList<>(soldProducts.keySet());
    }
    public  ArrayList<String> getCategoryList()
    {
        return new ArrayList<>(categories.keySet());
    }

    public int getSoldByCategory(String category) {
        if (!categories.containsKey(category))
        {
            return -1;
        }
        return categories.get(category);
    }

//    public String getReportByCategory(String category)
//    {
//
//    }


}

