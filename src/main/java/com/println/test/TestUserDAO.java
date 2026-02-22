package com.println.test;

import com.println.dao.UserDAO;
import com.println.model.User;
import java.util.List;

public class TestUserDAO {
    public static void main(String[] args) {

        UserDAO dao = new UserDAO();

        // -----------------------------------------------------
        // 🔹 1. Create Staff User (username: staff / password: staff123)
        // -----------------------------------------------------
        User staff = new User();
        staff.setUsername("staff");
        staff.setPassword("staff123");  // make sure UserDAO hashes this if needed
        staff.setRole("Staff");
        staff.setFirstName("Juan");
        staff.setLastName("Dela Cruz");

        boolean inserted = dao.addUser(staff);
        if (inserted) {
            System.out.println("Staff user created successfully!");
        } else {
            System.out.println("Staff user may already exist or failed to insert.");
        }


        // -----------------------------------------------------
        // 🔹 2. List all users
        // -----------------------------------------------------
        System.out.println("\n--- All Users ---");
        List<User> users = dao.getAllUsers();
        for (User u : users) {
            System.out.println(u);
        }


        // -----------------------------------------------------
        // 🔹 3. Test login for the Staff account
        // -----------------------------------------------------
        System.out.println("\n--- Testing Staff Login ---");
        User staffLogin = dao.validateLogin("staff", "staff123");

        if (staffLogin != null) {
            System.out.println("Logged in as: " +
                staffLogin.getFirstName() + " " + staffLogin.getLastName() +
                " (" + staffLogin.getRole() + ")");
        } else {
            System.out.println("Staff login failed.");
        }
    }
}
