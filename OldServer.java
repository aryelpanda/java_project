//import auth.Customer;

import auth.*;
import auth.ProductLoader;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class OldServer implements ChatSubject {

    private static HashMap<String, Socket> activeUsers = new HashMap<>();
    private static Queue<String> waitingQueue = new LinkedList<>();
    private static List<String> missedCalls = new ArrayList<>();
    private static HashMap<String, ChatObserver> observers = new HashMap<>();
    private static HashMap<String, ChatSession> chatSessions = new HashMap<>();
    private Set<String> admins = new HashSet<>();

    private ServerSocket serverSocket;

    EmployeeFactory employeeFactory = new EmployeeFactory("employeeData.txt");
    private Map<String, Employee> employeesMap;

    private Set<String> loggedUsers = Collections.synchronizedSet(new HashSet<>());

    private Map<String, Branch> branches;

    private Log employeeLog;

    private Log customerLog;

    private Log buySellLog;

    private Log chatLog = null;

    private List<Customer> customerLoader;

    public OldServer(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        employeesMap = Collections.synchronizedMap(employeeFactory.createEmployeesFromFile());

        branches = new BranchLoader("branches.txt").loadBranchesFromFile();

        for (Map.Entry<String, Employee> entry : employeesMap.entrySet()) {
            branches.get(entry.getValue().getBranchNumber()).addEmployee(entry.getValue());
            entry.getValue().setBranch(branches.get(entry.getValue().getBranchNumber()));
        }

        admins.add("alice01");
        employeeLog = new Log("employeeLog.txt");
        customerLog = new Log("customerLog.txt");
        buySellLog = new Log("buySellLog.txt");
        ProductLoader pL = new ProductLoader("products.txt");
        pL.loadData(branches);
        customerLoader = new CustomerLoader("customers.txt").loadCustomers();
        for (Customer customer : customerLoader) {
            Employee.addCustomer(customer);
        }
    }


    public void startServer() {
        try {
            System.out.println("Server has started");
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }

        } catch (IOException e) {
            closeServerSocket();
        }

    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        OldServer server = new OldServer(serverSocket);
        server.startServer();
    }

    @Override
    public void addObserver(ChatObserver observer, String username) {
        observers.put(username, observer);
    }

    @Override
    public void removeObserver(String username) {
        observers.remove(username);
    }

    @Override
    public void notifyObservers(String username) {
        if (observers.containsKey(username)) {
            observers.get(username).notifyAvailable(username);
        }

    }

    private class ClientHandler implements Runnable {

        //keep track of all clients to send them messages
//        public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
        public static Map<String, ClientHandler> clientHandlers = new HashMap<>();
        private Socket socket;
        private String username;

        private String clientUsername;

        private BufferedReader in;

        private PrintWriter out;
        private Employee employee;


        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {

                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                clientHandlers.put(username, this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void closeEverything() {

            try {
                if (in != null) {
                    in.close();
                }

                if (out != null) {
                    out.close();
                }

                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
//            String username = null;
            try {
                String loginDetails = in.readLine();

                String[] loginParts = loginDetails.split(":");
                if (loginParts.length != 3 || !loginParts[0].equals("LOGIN")) {
                    out.println("ERROR:Invalid protocol");
                    return;
                }
                username = loginParts[1];
                String password = loginParts[2];
                if (employeesMap.containsKey(username) && employeesMap.get(username).getPassword().equals(password)) {
                    if (loggedUsers.contains(username)) {
                        out.println("ERROR:User already logged in");
                        return; // new
                    } else {
                        loggedUsers.add(username);
                        employee = employeesMap.get(username);
                        out.println("SUCCESS");
                        activeUsers.put(username, socket); // new
                        System.out.println(username + " logged in");
                        employeeLog.addToLog(username + " logged in");
                    }
                } else {
                    out.println("ERROR:invalid credentials");
                    return;
                }

                boolean running = true;
                while (running) {
                    String choice = in.readLine();
                    System.out.println("Choice is: " + choice);
                    String[] choices = choice.split(":");
                    switch (choices[0]) {

                        case "LOGOUT":
                            loggedUsers.remove(username);
                            closeEverything();
                            out.println("Logged out successfully. Closing connection");
                            System.out.println(username + "logged out");
                            employeeLog.addToLog(username + " logged out");
                            running = false;
                            break;
                        case "FETCH":
                            String data = employeesMap.get(username).getBranchData();
                            out.println(data);
                            break;
                        case "NEW_E":
                            if (admins.contains(username)) {
                                out.println("Enter employee data:");
                                choice = in.readLine();
                                String[] inputData = choice.split(",");

                                if (inputData.length != 9) {
                                    out.println("ERROR: invalid input");
                                    break;
                                }
                                Employee newEmployee = new Employee(inputData[0], inputData[1], inputData[2], inputData[3], inputData[4], inputData[5], inputData[6], inputData[7], inputData[8]);
                                employeesMap.put(newEmployee.getUsername(), newEmployee);
                                branches.get(newEmployee.getBranchNumber()).addEmployee(newEmployee);
                                out.println("Added new employee successfully");
                                employeeLog.addToLog(newEmployee.getUsername() + " was added by " + username + ".");
                            } else {
                                out.println("ERROR:Access denied");
                            }
                            break;
                        case "NEW_C":
                            String[] customerInfo = choices[1].split(",");
                            if (customerInfo.length != 4) {
                                out.println("ERROR: invalid input");
                                break;
                            }
                            Customer customer = null;
                            switch (customerInfo[3]) {
                                case "new":
                                    customer = new NewCustomer(customerInfo[0], customerInfo[1], customerInfo[2]);

                                    break;
                                case "vip":
                                    customer = new VIPCustomer(customerInfo[0], customerInfo[1], customerInfo[2]);
                                    break;
                                case "return":
                                    customer = new ReturningCustomer(customerInfo[0], customerInfo[1], customerInfo[2]);
                                    break;
                                default:
                                    out.println("ERROR: invalid input");
                            }
                            if (Employee.addCustomer(customer)) {
                                out.println("New customer added successfully");
                                customerLog.addToLog(customer.getFullName() + " was added by " + username + ".");
                            } else {
                                out.println("ERROR: invalid input");
                            }
                            break;
                        case "UPDATE_PASSWORD":
                            if (admins.contains(username) || choices[1].equalsIgnoreCase(employee.getUsername())) {
                                out.println("NEW_PASS");
                                employeesMap.get(choices[1]).setPassword(in.readLine());
                                out.println("Password changed successfully");
                            } else {
                                out.println("ERROR: cannot change password");
                            }
                            break;

                        case "GET_PRODUCTS":
                            List<String> lines = employee.getBranch().getProducts();
                            if (lines.size() == 0) {
                                out.println("EMPTY inventory");
                                break;
                            }
                            for (String line : lines) {
                                out.println(line);
                            }
                            out.println("null");
                            break;

                        case "BUY_PRODUCT":
                            List<String> products = employee.getBranch().getProductsList();
                            for (String product : products) {
                                out.println(product);
                            }
                            out.println("null");
                            String prod = in.readLine();
                            boolean res = employee.buyProduct(prod);

                            if (res) {
                                buySellLog.addToLog(employee.getUsername() + " bought " + prod + " for branch " + employee.getBranchNumber());
                                out.println("bought new product succeeded");
                            } else {
                                out.println("no enough balance in branch or product unavailable");
                            }

                            break;
                        case "SELL":
                            String[] sell = choices[1].split(",");
                            if (sell.length != 2) {
                                out.println("ERROR: invalid input");
                                break;
                            }
                            boolean transaction = employee.makePurchase(sell[0], sell[1]);
                            if (!transaction) {
                                out.println("ERROR: purchase failed");

                            } else {
                                buySellLog.addToLog(sell[1] + " bought " + sell[0] + " from branch " + employee.getBranchNumber());
                                out.println("purchase succeeded");
                            }
                            break;


                        case "REPORT":
                            String order = choices[1];
                            String type = "branch";
                            if (order.equalsIgnoreCase("BRANCH")) {
                                out.println(employee.getBranchData());
                                type = "branch";
                            } else if (order.equalsIgnoreCase("PRODUCT")) {
                                out.println(employee.getSoldProductsList());
                                type = in.readLine();
                                int soldProductsReport = employee.getSoldProductsReport(type);
                                if (soldProductsReport < 0) {
                                    out.println("Error: product unavailable");
                                } else {
                                    out.println(soldProductsReport);
                                }

                            } else if (order.equalsIgnoreCase("CATEGORY")) {
                                out.println(employee.getCategoryList());
                                type = in.readLine();
                                int categoryReport = employee.getReportByCategory(type);
                                if (categoryReport < 0) {
                                    out.println("Error: category unavailable");
                                } else {
                                    out.println(categoryReport);
                                }
                            }


                            if (in.readLine().equalsIgnoreCase("MAKE_FILE")) {
                                employee.createReportsFile(order, type);
                                out.println("File was created");
                            }


                            break;
                        default:
                            out.println("Invalid choice");

                    }
                }
            } catch (IOException e) {
                if (username != null) {
                    loggedUsers.remove(username);
                    employeeLog.addToLog(username + " disconnected");
//                closeEverything(socket,bufferedReader,bufferedWriter);
                    e.printStackTrace();

                }
            }

//        public void run1()
//        {
//            String messageFromClient;
//
//            while (socket.isConnected())
//            {
//                try
//                {
//                    messageFromClient = bufferedReader.readLine();
//                    broadcastMessage(messageFromClient);
//                }
//                catch (IOException e)
//                {
//                    closeEverything(socket,bufferedReader,bufferedWriter);
//                    break;
//                }
//            }
//
//        }

//        public void broadcastMessage(String messageToSend) {
//            for (ClientHandler clientHandler : clientHandlers)
//            {
//                try
//                {
//                    if (!clientHandler.clientUsername.equals(clientUsername))
//                    {
//                        clientHandler.bufferedWriter.write(messageToSend);
//                        clientHandler.bufferedWriter.newLine();
//                        clientHandler.bufferedWriter.flush();
//
//                    }
//                }
//
//                catch (IOException e)
//                {
//                    closeEverything(socket,bufferedReader,bufferedWriter);
//                }
//            }
//        }


//        public void removeClientHandler()
//        {
//            clientHandlers.remove(this);
//            broadcastMessage("SERVER: "+clientUsername+" has left the chat");
//        }

//        public void closeEverything(Socket socket,BufferedReader bufferedReader, BufferedWriter bufferedWriter)
//        {
//            removeClientHandler();
//            try
//            {
//                if (bufferedReader!= null)
//                {
//                    bufferedReader.close();
//                }
//
//                if (bufferedWriter!= null)
//                {
//                    bufferedWriter.close();
//                }
//
//                if (socket != null)
//                {
//                    socket.close();
//                }
//            }
//
//            catch (IOException e)
//            {
//                e.printStackTrace();
//            }
//        }
        }
    }
}



