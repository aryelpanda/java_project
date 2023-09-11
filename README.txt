1. run server.java
2. run client.java 


Admin: alice01 , pass  = alice01

Users (Employees):

Users can be authenticated (via the Authenticable interface).
Employee classe inherit from the User class.
Employees have roles such as "shift manager", "cashier", and "salesperson".
Customers come in different types: NewCustomer, ReturningCustomer, and VIPCustomer.

Products and Inventory:

Product: Represents individual items for sale.
Inventory: Manages products in a specific branch. It can tell if a product is available, sell a product, buy a product, etc.
ProductLoader: Loads product data ( from a file) and populates the inventory of branches.

Branch Management:

Branch: Represents individual store branches. Each branch has its own inventory, employees, sales data, and balance.
BranchLoader: Loads branch data ( from a file).

Logging and Reporting:

Log: Handles the logging of various activities, such as employee actions, customer actions, and product sales.
Employees can create reports based on branch data, products, and categories.

Chat System:

ChatManager, ChatObserver, ChatSession, and ChatSubject: These classes are likely related to a chat system where users can send and receive messages. This system seems to follow the Observer design pattern, allowing entities to subscribe to updates.

Queue Management:

QueueManager: Manages queues. Given the context, this could be related to managing the order of customer service requests or similar functionalities.

Server and Client Communication:

Server: Waits for client connections and handles their requests. It can authenticate users, manage chat sessions, handle product purchases, generate reports, and more.
Client: Connects to the server, sends requests, and receives responses.

Factories and Loaders:

EmployeeFactory: Likely creates Employee objects based on some data source.
Other loader classes, such as ProductLoader and CustomerLoader, load data from files and populate respective objects in the system.



VIPCustomer: Represents a special type of customer with VIP status.
NewCustomer and ReturningCustomer: Represent new and returning customers, respectively, each with its specific behaviors.