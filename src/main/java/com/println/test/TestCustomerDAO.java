// package com.println.test;

// import com.println.dao.CustomerDAO;
// import com.println.model.Customer;
// import java.util.List;

// public class TestCustomerDAO {
//     public static void main(String[] args) {
//         CustomerDAO dao = new CustomerDAO();

//         // --- Add a new customer ---
//         Customer c = new Customer();
//         c.setName("Juan Dela Cruz");
//         c.setContactNo("09123456789");
//         c.setEmail("juan@example.com");
//         c.setAddress("123 Mabini St., Daet");
//         c.setCity("Camarines Norte");
//         c.setGender("Male");
//         c.setNotes("Test customer record");
//         c.setStatus("Active");
//         c.setCustomerType("New");
//         c.setCreatedBy(1); // assuming admin user_id = 1

//         boolean added = dao.addCustomer(c);
//         System.out.println(added ? "✅ Customer added!" : "Failed to add customer.");

//         // --- Read all customers ---
//         List<Customer> list = dao.getAllCustomers();
//         for (Customer cust : list) {
//             System.out.println(cust);
//         }

//         // --- Search customers ---
//         List<Customer> searchResults = dao.searchCustomers("Juan");
//         System.out.println("\nSearch Results:");
//         for (Customer cust : searchResults) {
//             System.out.println(cust);
//         }
//     }
// }
