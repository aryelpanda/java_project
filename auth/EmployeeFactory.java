package auth;//package auth;
//
//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//public class EmployeeFactory {
//
//    private String filePath;
//
//    public EmployeeFactory(String filePath) {
//        this.filePath = filePath;
//    }
//
//    public Employee createEmployee(String fullName, String id, String username, String password, String phoneNumber, String accountNumber, String branchNumber, String employeeNumber, String role) {
//        return new Employee(fullName, id, username, password, phoneNumber, accountNumber, branchNumber, employeeNumber, role);
//    }
//
//    public Map<String, Employee> createEmployeesFromFile() {
//        Map<String, Employee> employeeMap = new HashMap<>();
//        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
//            String line;
//            while ((line = br.readLine()) != null) {
//                String[] parts = line.split(", ");
//                String fullName = parts[0].split(": ")[1];
//                String id = parts[1].split(": ")[1];
//                String username = parts[2].split(": ")[1];
//                String password = parts[3].split(": ")[1];
//                String phoneNumber = parts[4].split(": ")[1];
//                String accountNumber = parts[5].split(": ")[1];
////                String branchAffiliation = parts[6].split(": ")[1];
//                String branchNumber = parts[7].split(": ")[1];
//                String employeeNumber = parts[8].split(": ")[1];
//                String role = parts[9].split(": ")[1];
//
//                Employee employee = createEmployee(fullName, id, username, password, phoneNumber, accountNumber, branchNumber, employeeNumber, role);
//                employeeMap.put(username, employee);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return employeeMap;
//    }
//}


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EmployeeFactory {

    private String filePath;

    public EmployeeFactory(String filePath) {
        this.filePath = filePath;
    }

    public Employee createEmployee(String fullName, String id, String username, String password, String phoneNumber, String accountNumber, String branchNumber, String employeeNumber, String role) {
        return new Employee(fullName, id, username, password, phoneNumber, accountNumber, branchNumber, employeeNumber, role);
    }

    public Map<String, Employee> createEmployeesFromFile() {
        Map<String, Employee> employeeMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");

                String fullName = parts[0];
                String id = parts[1];
                String username = parts[2];
                String password = parts[3];
                String phoneNumber = parts[4];
                String accountNumber = parts[5];
                String branchNumber = parts[6];
                String employeeNumber = parts[7];
                String role = parts[8];

                Employee employee = createEmployee(fullName, id, username, password, phoneNumber, accountNumber, branchNumber, employeeNumber, role);
                employeeMap.put(username, employee);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return employeeMap;
    }}