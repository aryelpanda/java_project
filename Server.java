
import auth.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public class Server implements Observer {

    private final Map<String, ClientHandler> clientHandlers = new HashMap<>();
    // Member variables related to chat and user management
    private static HashMap<String, Socket> activeUsers = new HashMap<>();
    private static Queue<String> freQueue = new LinkedList<>();
    private static List<String> missedCalls = new ArrayList<>();
   
    private Map<String,Product> allProducts;
 private ConcurrentMap<String, Queue<ClientHandler>> freeEmployeesByBranch = new ConcurrentHashMap<>();
    // Member variables related to server functionality
    private final ServerSocket serverSocket;
    private EmployeeFactory employeeFactory = new EmployeeFactory("employeeData.txt");
    private Map<String, Employee> employeesMap;
    private Set<String> loggedUsers = Collections.synchronizedSet(new HashSet<>());
    private Set<String> admins = new HashSet<>();
    private Map<String, Branch> branches;
    private Log employeeLog, customerLog, buySellLog, chatLog = null;
    private List<Customer> customerLoader;
    public void updateFreeEmployeesQueue(ClientHandler clientHandler) {
        String branch = clientHandler.getBranch();
        String status = clientHandler.getStatus();
        if (branch == null || status == null) {
            System.out.println("Branch or status is null");
            return;
        }
        if ("free".equals(status)) {
            // If the branch doesn't exist in the map, create an entry for it.
            if (!freeEmployeesByBranch.containsKey(branch)) {
                freeEmployeesByBranch.put(branch, new ConcurrentLinkedQueue<>());
            }
            // Add the client handler to the queue of free employees for that branch.
            freeEmployeesByBranch.get(branch).add(clientHandler);
        } else {
            // If the status is 'busy', remove the client handler from the queue.
            Queue<ClientHandler> queue = freeEmployeesByBranch.get(branch);
            if (queue != null) {
                queue.remove(clientHandler);
            }
        }
    }
     public boolean checkFreeUserForTargetBranch(String targetBranch) {
        // Access the freeEmployeesByBranch map from the server reference
        Queue<ClientHandler> freeEmployees = freeEmployeesByBranch.get(targetBranch);
    
        // Check if there are any free employees in the target branch
        return freeEmployees != null && !freeEmployees.isEmpty();
    }
    
    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof ClientHandler) {
            ClientHandler clientHandler = (ClientHandler) o;
            updateFreeEmployeesQueue(clientHandler);
        }
    }

    // Constructor
    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;

        // Initialization procedures
        employeesMap = Collections.synchronizedMap(employeeFactory.createEmployeesFromFile());
        branches = new BranchLoader("branches.txt").loadBranchesFromFile();
         // Initialize the freeEmployeesByBranch with empty queues for each branch
         for (String branchKey : branches.keySet()) {
            freeEmployeesByBranch.put(branchKey, new ConcurrentLinkedQueue<>());
        }
        for (Map.Entry<String, Employee> entry : employeesMap.entrySet()) {
            branches.get(entry.getValue().getBranchNumber()).addEmployee(entry.getValue());
            entry.getValue().setBranch(branches.get(entry.getValue().getBranchNumber()));
        }

        // Admins and logs initialization
        admins.add("alice01");
        employeeLog = new Log("employeeLog.txt");
        customerLog = new Log("customerLog.txt");
        buySellLog = new Log("buySellLog.txt");
        ProductLoader pL = new ProductLoader("products.txt");
        pL.loadData(branches);
        allProducts = pL.getAllProducts();
        customerLoader = new CustomerLoader("customers.txt").loadCustomers();
        for (Customer customer : customerLoader) {
            Employee.addCustomer(customer);
        }
    }

    // Start the server
    public void startServer() {
        try {
            System.out.println("Server has started");
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket,this);
                clientHandler.addObserver(this);
                Thread thread = new Thread(clientHandler);
                thread.start();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                updateFreeEmployeesQueue(clientHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
            closeServerSocket();
        }
    }

    // Close the server socket safely
    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Main entry point
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        Server server = new Server(serverSocket);
        server.startServer();
    }

    // ClientHandler inner class
    public class ClientHandler extends Observable implements Runnable{
        private Server myserver;
        private static final String LOGOUT = "LOGOUT";
        private static final String FETCH = "FETCH";
        private static final String NEW_E = "NEW_E";
        private static final String NEW_C = "NEW_C";
        private static final String UPDATE_PASSWORD = "UPDATE_PASSWORD";
        private static final String GET_PRODUCTS = "GET_PRODUCTS";
        private static final String BUY_PRODUCT = "BUY_PRODUCT";
        private static final String SELL = "SELL";
        private static final String REPORT = "REPORT";
        private String status; // "free" or "busy"
        private String branch; // Branch ID or name
        
        private Socket socket;
        private String username;
        
        private volatile boolean paused = false;

        public synchronized void pauseHandler() {
            paused = true;
        }

        public synchronized void resumeHandler() {
            paused = false;
            notifyAll();
        }
        private BufferedReader in;
        private PrintWriter out;
        private Employee employee;
        private boolean inChat = false;
        private ClientHandler chatPartner;
        public void setStatus(String newStatus) {
            if (!newStatus.equals(this.status)) {
                this.status = newStatus;
                setChanged();  // Mark this object as changed
                notifyObservers();  // Notify all observers
            }
        }
        public String getBranch() {
            return this.branch;
        }
        public String getStatus() {
            return this.status;
        }
        public ClientHandler(Socket socket,Server server) {
            this.socket = socket;
            try {
                this.status = "free";
                   this.myserver = server;
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public synchronized void waitForResume() {
            while (paused) {
                try {
                    wait(); // Wait until resumeHandler() is called
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        // Close all resources
        public void closeEverything() {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Main logic of the client handler
        @Override
        public void run() {
            try {
                while (true) {
                    waitForResume(); // This will block if the handler is paused
                    handleClientConnection();
                }
            } catch (IOException e) {
                if (username != null) {
                    clientHandlers.remove(username);
                    loggedUsers.remove(username);
                    System.out.println(username + " disconnected");
                    employeeLog.addToLog(username + " disconnected");
                    e.printStackTrace();
                }
                closeEverything();
            }
        }

        private void handleClientConnection() throws IOException {
            if (!authenticateUser()) {
                return;
            }
            boolean running = true;
            while (running) {
                waitForResume();  // Wait if the handler is paused

                // Check if we have any incoming data from the client
                if (in.ready()) {
                    String choice = in.readLine();
                    System.out.println("Choice is: " + choice);

                    // Your handling code
                    running = processClientChoices(choice);
                }
                else {
                    // Optional: pause for a short moment to avoid busy-wait
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        
        

        private boolean authenticateUser() throws IOException {
            String loginDetails = in.readLine();
            String[] loginParts = loginDetails.split(":");
            if (loginParts.length != 3 || !loginParts[0].equals("LOGIN")) {
                out.println("ERROR:Invalid protocol");
                return false;
            }
            username = loginParts[1];
            String password = loginParts[2];
            if (employeesMap.containsKey(username) && employeesMap.get(username).getPassword().equals(password)) {
                if (loggedUsers.contains(username)) {
                    out.println("ERROR:User already logged in");
                    return false;
                } else {
                    loggedUsers.add(username);
                    employee = employeesMap.get(username);
                    out.println("SUCCESS");
                    this.branch = employee.getBranchNumber();
                    activeUsers.put(username, socket); // todo change to branch
                    System.out.println(username + " logged in");
                    employeeLog.addToLog(username + " logged in");
                    
                    clientHandlers.put(username, this);
                    return true;
                }
            } else {
                out.println("ERROR:invalid credentials");
                return false;
            }
        }

        public void onRequestForChat(Socket clientSocket1, String username1, String targetBranch) {
            // Check if there are any free employees in the target branch
            if (myserver.checkFreeUserForTargetBranch(targetBranch)) {
                // If there are free employees, get the first one from the queue
                ClientHandler clientHandler2 = freeEmployeesByBranch.get(targetBranch).poll();
        
                // Pause this ClientHandler before initiating chat
                this.pauseHandler();
        
                // Pause the other ClientHandler involved in the chat
                clientHandler2.pauseHandler();
        
                // Start the chat
                startChat(clientSocket1, clientHandler2.socket, username1, clientHandler2.username, clientHandler2);
        
                // Once chat is over, you will call resumeHandler() for both ClientHandler instances
            } else {
                // If there are no free employees, add the caller to the missed calls list
                missedCalls.add(username1);
            }
        }
        
        
        
        public void startChat(Socket socket1, Socket socket2, String username1, String username2,ClientHandler handler2) {
            ChatSession chatSession = new ChatSession(socket1, socket2, username1, username2,this,handler2);
            new Thread(chatSession).start();
        }
        
        private boolean processClientChoices(String choice) throws IOException {

            String[] choices = choice.split(":");
            
            switch (choices[0]) {
                
                case "INIT_CHAT":
                    Set<String> keySet = branches.keySet();
                    String joinedKeys = String.join(":", keySet);
                    out.println("branches:"+joinedKeys);
                    String branch = in.readLine();
                    if (branch.equals("null")) {
                        out.println("ERROR:Invalid branch");
                        return true;
                    }
                    onRequestForChat(socket, this.username, branch);
                    return true;
                case "CHAT":
                    onRequestForChat(socket, choice, choice);
                    return true;
                case LOGOUT:
                    return handleLogout();
                case FETCH:
                    handleFetch();
                    return true;
                case NEW_E:
                    handleNewEmployee();
                    return true;
                case NEW_C:
                    handleNewCustomer(choices);
                    return true;
                case UPDATE_PASSWORD:
                    handleUpdatePassword(choices);
                    return true;
                case GET_PRODUCTS:
                    handleGetProducts();
                    return true;
                case BUY_PRODUCT:
                    handleBuyProduct();
                    return true;
                case SELL:
                    handleSell(choices);
                    return true;
                case REPORT:
                    handleReport(choices);
                    return true;
                default:
                    out.println("Invalid choice");
                    return true;
            }
        }

        private boolean handleLogout()  {
            loggedUsers.remove(username);
            closeEverything();
            out.println("Logged out successfully. Closing connection");
            System.out.println(username + " logged out");
            employeeLog.addToLog(username + " logged out");
            return false; // to stop the loop
        }

        private void handleFetch() throws IOException {
            String data = employeesMap.get(username).getBranchData();
            out.println(data);
        }

        private void handleNewEmployee() throws IOException {
            // ... The code specific to NEW_E command ...
            if (admins.contains(username)) {
                out.println("Enter employee data:");
                String choice = in.readLine();
                String[] inputData = choice.split(",");

                if (inputData.length != 9) {
                    out.println("ERROR: invalid input");
                    return;
                }
                Employee newEmployee = new Employee(inputData[0], inputData[1], inputData[2], inputData[3], inputData[4], inputData[5], inputData[6], inputData[7], inputData[8]);
                employeesMap.put(newEmployee.getUsername(), newEmployee);
                branches.get(newEmployee.getBranchNumber()).addEmployee(newEmployee);
                newEmployee.setBranch(branches.get(newEmployee.getBranchNumber()));
                out.println("Added new employee successfully");
                employeeLog.addToLog(newEmployee.getUsername() + " was added by " + username + ".");
            } else {
                out.println("ERROR:Access denied");
            }
        }

        private void handleNewCustomer(String[] choices) {
            // ... The code specific to NEW_C command ...
            String[] customerInfo = choices[1].split(",");
            if (customerInfo.length != 4) {
                out.println("ERROR: invalid input");
                return;
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
        }

        private void handleUpdatePassword(String[] choices) throws IOException {
            // ... The code specific to UPDATE_PASSWORD command ...
            if (employeesMap.containsKey(choices[1]) && (admins.contains(username) || choices[1].equalsIgnoreCase(employee.getUsername()))) {
                out.println("NEW_PASS");
                employeesMap.get(choices[1]).setPassword(in.readLine());
                out.println("Password changed successfully");
            } else {
                out.println("ERROR: cannot change password");
            }
        }

        private void handleGetProducts() throws IOException {
            // ... The code specific to GET_PRODUCTS command ...
            List<String> lines = employee.getBranch().getProducts();
            if (lines.size() == 0) {
                out.println("EMPTY inventory");
                return;
            }
            for (String line : lines) {
                out.println(line);
            }
            out.println("null");
        }

        private void handleBuyProduct() throws IOException {
            // ... The code specific to BUY_PRODUCT command ...
            List<Product> products = new ArrayList<>(allProducts.values());
            for (Product product : products) {
                out.println(product.getProductId() + " : " + product.getPrice());
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
        }

        private void handleSell(String[] choices) throws IOException {
            // ... The code specific to SELL command ...
            String[] sell = choices[1].split(",");
            if (sell.length != 2) {
                out.println("ERROR: invalid input");
                return;
            }
            boolean transaction = employee.makePurchase(sell[0], sell[1]);
            if (!transaction) {
                out.println("ERROR: purchase failed");

            } else {
                buySellLog.addToLog(sell[1] + " bought " + sell[0] + " from branch " + employee.getBranchNumber());
                out.println("purchase succeeded");
            }
        }

        private void handleReport(String[] choices) throws IOException {
            // ... The code specific to REPORT command ...
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
        }

    }
    

public class ChatSession implements Runnable {
    private final Socket socket1;
    private final Socket socket2;
    private final String username1;
    private final String username2;
    private ClientHandler handler1;
    private ClientHandler handler2;
    public ChatSession(Socket socket1, Socket socket2, String username1, String username2,ClientHandler handler1,ClientHandler handler2) {
        this.socket1 = socket1;
        this.socket2 = socket2;
        this.username1 = username1;
        this.username2 = username2;
        this.handler1 = handler1;
        this.handler2 = handler2;
    }

    @Override
    public void run() {
        try {
            PrintWriter out1 = new PrintWriter(socket1.getOutputStream(), true);
            PrintWriter out2 = new PrintWriter(socket2.getOutputStream(), true);
            BufferedReader in1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
            BufferedReader in2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
            // Notify both clients that the chat is starting
            out1.println("Starting chat...");
            out2.println("Starting chat...");

            String receivedMessage;

            // Loop to manage the chat
            while (true) {
                if (in1.ready()) {
                    receivedMessage = in1.readLine();
                    if ("EXIT".equals(receivedMessage)) {
                        break;
                    }
                    out2.println(username1 + ": " + receivedMessage);
                }

                if (in2.ready()) {
                    receivedMessage = in2.readLine();
                    if ("EXIT".equals(receivedMessage)) {
                        break;
                    }
                    out1.println(username2 + ": " + receivedMessage);
                }
            }

            // Once the loop breaks, close resources and notify both clients the chat ended
            out1.println("Chat ended.");
            out2.println("Chat ended.");
            handler1.resumeHandler();
            handler2.resumeHandler();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


}

