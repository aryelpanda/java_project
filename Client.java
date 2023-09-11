

import java.io.*;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client  {
    private Socket socket;

    private String username;

    private BufferedReader in;
    private PrintWriter out;

    private String serverAddress;
    private int serverPort;

    private BufferedReader userIn;

    public Client(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.userIn = new BufferedReader(new InputStreamReader(System.in));
    }

    private void showMenu() {
        System.out.println("=========================================");
        System.out.println("Choose an action:");
        System.out.println("1. Logout");
        System.out.println("2. Start chat");
        System.out.println("3. Show Branch Data (according to role)");
        System.out.println("4. Customer purchase");
        System.out.println("5. Add new Employee");
        System.out.println("6. Add new Customer");
        System.out.println("7. Show Branch inventory");
        System.out.println("8. Change Password");
        System.out.println("9. Buy product for store");
        System.out.println("10. Show reports");
        System.out.println("=========================================");

    }

    private boolean authenticate() throws IOException {
        System.out.println("Enter your username:");
        String username = userIn.readLine();
        System.out.println("Enter your password:");
        String password = userIn.readLine();
        socket = new Socket(serverAddress, serverPort);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        out.println("LOGIN:" + username + ":" + password); // Sending credentials to the server
        String response = in.readLine(); // Waiting for server response

        if ("SUCCESS".equals(response)) {
            System.out.println("Login successful!");
            this.username = username;
            return true;
        } else {
            System.out.println("Login failed. " + response.split(":")[1]); // Expecting "ERROR:<reason>"
            socket.close();
            return false;
        }
    }

    private boolean logOut() {
        out.println("LOGOUT");
        System.out.println("Logged out successfully. Goodbye!");
        return false;
    }

    private void fetchBranchData() throws IOException {
        System.out.println("Fetching data...");
        out.println("FETCH");
        String response = in.readLine();
        System.out.println(response);
    }

    private void makeCustomerPurchase() throws IOException {
        System.out.println("type customer ID:");
        String customerId = userIn.readLine();
        System.out.println("Choose product");
        out.println("GET_PRODUCTS");
        String response;
        while (!(response = in.readLine()).equals("null")) {
            System.out.println(response);
        }
        String productType = userIn.readLine();
        out.println("SELL:" + productType + ',' + customerId);
        response = in.readLine();
        System.out.println(response);
    }

    private void createNewCustomer() throws IOException {
        String response;
        System.out.println("Enter customer data: full-name,id,phone-number,type");
        String customerData = userIn.readLine();
        out.println("NEW_C:" + customerData);
        response = in.readLine();
        System.out.println(response);
    }

    private void getBranchInventory() throws IOException {
        out.println("GET_PRODUCTS");
        String response;
        while (!(response = in.readLine()).equals("null")) {
            System.out.println(response);
        }
    }

    private void changePassword() throws IOException {
        System.out.println("Type username:");
        String user = userIn.readLine();
        out.println("UPDATE_PASSWORD:" + user);
        String response = in.readLine();

        if (response.startsWith("NEW_PASS")) {
            System.out.println("type new password:");
            String newPass = userIn.readLine();
            out.println(newPass);
            response = in.readLine();
            System.out.println(response);
        } else {
            System.out.println(response);

        }
    }

    public void buyProduct() throws IOException {
        out.println("BUY_PRODUCT:");
        System.out.println("choose product to buy");

        String response;
        System.out.println("Products List:");
        while (!(response = in.readLine()).equals("null")) {
            System.out.println(response);
        }
        String request = userIn.readLine();
        out.println(request.toLowerCase());
        response = in.readLine();
        System.out.println(response);
    }

    private void showReport() throws IOException {
        String response;
        String request;
        System.out.println("Show report by:");
        System.out.println("1. Branch");
        System.out.println("2. Product type");
        System.out.println("3. Product category");
        int c = Integer.parseInt(userIn.readLine());

        switch (c) {
            case 1:
                out.println("REPORT:BRANCH");
                response = in.readLine();
                System.out.println(response);
                break;
            case 2:
                System.out.println("Choose Product");
                out.println("REPORT:PRODUCT");
                // product list
                System.out.println(in.readLine());
                request = userIn.readLine();
                out.println(request);
                response = in.readLine();

                System.out.println(response);
                break;
            case 3:
                System.out.println("Choose Category");
                // categories
                out.println("REPORT:CATEGORY");
                System.out.println(in.readLine());
                request = userIn.readLine();
                out.println(request);
                response = in.readLine();
                System.out.println(response);
                break;
            default:
//                out.println("REPORT:invalid");
//                response = in.readLine();
                System.out.println("Invalid choice");
                return;
        }

        if (response.equalsIgnoreCase("invalid") || response.startsWith("Error:")) {
            out.println("CLOSE");
            return;
        }

        System.out.println("Wanna save data on a file?");
        request = userIn.readLine();
        if (request.equalsIgnoreCase("yes")) {
            out.println("MAKE_FILE");
            System.out.println(in.readLine());
        } else {
            out.println("CLOSE");
        }

    }

    private boolean handleUserChoice(String choice) throws IOException {
        String response;
        String request;
        switch (choice) {
            case "1":
                return logOut();
            case "2":
    // Step 1: Send a message to the server indicating the desire to start a chat
    out.println("INIT_CHAT");
    response = in.readLine();
    if (response.startsWith("branches:")) {
        System.out.println(response);
        System.out.println("Enter the branch number you want to chat with:");
        request = userIn.readLine();
        out.println(request);

        // Wait for a message from the server that starts with "Starting chat"
        String chatInitiationMessage = in.readLine();
        if (chatInitiationMessage.startsWith("Starting chat")) {
            System.out.println("Chat session started. Type 'EXIT' to end the chat.");

            final boolean[] chatEnded = new boolean[1]; // Shared flag

            Thread readThread = new Thread(() -> {
                try {
                    while (!chatEnded[0]) {  // Use the flag
                        if (in.ready()) {
                            String res = in.readLine();
                            if (res != null) {
                                System.out.println("response: " + res);
                                if ("Chat ended.".equals(res)) {
                                    showMenu();
                                    //System.out.println("press enter to return to menu:");
                                    chatEnded[0] = true;  // Set the flag
                                    break;
                                }
                            }
                        } else {
                            Thread.sleep(100);
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });

            Thread writeThread = new Thread(() -> {
                try {
                    while (!chatEnded[0]) {  // Use the flag
                        String res = userIn.readLine();
                        
                        out.println(res);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            readThread.start();
            writeThread.start();

            try {
                readThread.join();
                writeThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    break;

            case "3":
                fetchBranchData();
                break;
            case "4":
                makeCustomerPurchase();
                break;
            case "5"://admin
                addNewEmployeeAccount();
                break;
            case "6":
                createNewCustomer();
                break;
            case "7":
                getBranchInventory();
                break;
            case "8":
                changePassword();
                break;
            case "9":
                buyProduct();
                break;
            case "10":
                showReport();
                break;
            default:
                System.out.println("Invalid choice");

        }
        return true;

    }

    private void addNewEmployeeAccount() throws IOException {
        String response;
        out.println("NEW_E");
        response = in.readLine();
        System.out.println(response);
        if (response.startsWith("ERROR")) {
            return;
        }
        System.out.println("full-name,id,username,password,phone-number,account number,branch number,employeeNumber,role");
        String data = userIn.readLine();
        out.println("NEW_E:" + data);
        response = in.readLine();
        System.out.println(response);
    }
    class IncomingMessageHandler extends Thread {
        private BufferedReader in;
        public IncomingMessageHandler(BufferedReader in) {
            this.in = in;
        }
    
        @Override
        public void run() {
            try {
                String incomingMessage;
                while ((incomingMessage = in.readLine()) != null) {
                    System.out.println(incomingMessage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void connect() {
        boolean authenticated = false;
        boolean printmenu = true;
       AtomicBoolean chatActive = new AtomicBoolean(false);
    
        try {
            while (!authenticated) {
                authenticated = authenticate();
            }
    
            boolean running = true;
    
            while (running) {
                if (printmenu) {
                    showMenu();
                    printmenu = false;
                }
    
                if (!chatActive.get() && in.ready()) {
                    String serverMessage = in.readLine();
                    System.out.println(serverMessage);
    
                    if (serverMessage.startsWith("Starting chat")) {
                        chatActive.set(true);// Add this line
                        System.out.println("Chat session started. Type 'EXIT' to end the chat.");
    
                        Thread readThread = new Thread(() -> {
                            try {
                                while (true) {
                                    if (in.ready()) {
                                        String chatMessage = in.readLine();
                                        System.out.println(chatMessage);
                                        if ("Chat ended.".equals(chatMessage)) {
                                           //System.out.println("press enter to return to menu:");
                                            chatActive.set(false);  // Add this line
                                            
                                            break;
                                        }
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
    
                        Thread writeThread = new Thread(() -> {
                            try {
                                while (chatActive.get()) {
                                    String userMessage = userIn.readLine();
                                    out.println(userMessage);
                                    if ("EXIT".equalsIgnoreCase(userMessage)) {
                                        chatActive.set(false);
                                        break;
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
    
                        readThread.start();
                        writeThread.start();
    
                        readThread.join();
                        writeThread.join();
    
                        printmenu = true;
                    }
                }
    
                if (!chatActive.get() ) { 
                    if( System.in.available() > 0) {
                    String choice = userIn.readLine();
                    running = handleUserChoice(choice);
                    printmenu = true;
                    }
                } else {
                    Thread.sleep(100);
                }
            }
        } catch (IOException | InterruptedException e) {
            closeEverything(socket, in, out);
        }
    }
    
    
    public void closeEverything(Socket socket, BufferedReader bufferedReader, PrintWriter bufferedWriter) {

        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }

            if (bufferedWriter != null) {
                bufferedWriter.close();
            }

            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Client client = new Client("localhost", 8080);
        client.connect();

    }

 
}