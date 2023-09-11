package auth;

import java.util.*;

public class Inventory {

    private String branchNumber;

    Map<String, Integer> productMap;

    Map<String,Product> productSet;

    public Inventory(String branchNumber) {
        this.branchNumber = branchNumber;
        this.productMap = new HashMap<>();
        this.productSet = new HashMap<>();
    }

    public String getBranchNumber() {
        return branchNumber;
    }

    public String getProductCategory(String productId)
    {
        return productSet.get(productId).getCategory();
    }
    public void setBranchNumber(String branchNumber) {
        this.branchNumber = branchNumber;
    }

    public int contains(String productId) {
        if (!productMap.containsKey(productId.toLowerCase())) {
            return -1;
        }
        return productMap.get(productId.toLowerCase());
    }

    public boolean addProduct(Product product,int amount) {
        String key = product.getProductId();
        if (!productSet.containsKey(key))
        {
            productMap.putIfAbsent(key, amount);
            productSet.put(key,product);
        }
        else
        {
            productMap.put(key, productMap.get(key) + 1);
        }
        return true;
    }

    public void removeProduct(String productID) {
        if (!productMap.containsKey(productID.toLowerCase())) {
            return;
        }
        productMap.remove(productID.toLowerCase());
        productSet.remove(productID.toLowerCase());
    }

    public boolean buyProduct(String productId) {
        if (!productMap.containsKey(productId.toLowerCase())) {
            return false;
        }
        productMap.put(productId.toLowerCase(),productMap.get(productId.toLowerCase())+1);
        return true;
    }

    public double sellProduct(String productId) {
        if (!productMap.containsKey(productId.toLowerCase())) {
            return 0;
        }
        productMap.put(productId.toLowerCase(),productMap.get(productId.toLowerCase())-1);
        return productSet.get(productId.toLowerCase()).getPrice();
    }

    public Product getProductById(String productId) {
        if (!productMap.containsKey(productId.toLowerCase())) {
            return null;
        }
        return productSet.get(productId.toLowerCase());
    }

    public ArrayList<String> getInventory()
    {
        ArrayList<String> arrayList= new ArrayList<>();
        for(Map.Entry<String ,Integer>  entry: productMap.entrySet())
        {
            arrayList.add( productSet.get(entry.getKey()).getProductId()+":"+Integer.toString(entry.getValue()));
        }

        return  arrayList;
    }

    public ArrayList<String> getProductsList()
    {
        return new ArrayList<>(productMap.keySet());
    }


//    public ArrayList<String> getByCategory(){}
}
