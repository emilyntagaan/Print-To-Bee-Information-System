    package com.println.model;

    import java.time.LocalDate;
    import java.time.LocalDateTime;

    public class Customer {
        private int customerId;
        private String name;
        private String contactNo;
        private String email;
        private String address;
        private LocalDateTime dateRegistered;
        private int totalOrders;
        private LocalDate lastOrderDate;
        private String customerType;
        private String status;
        private String gender;
        private String notes;
        private String city;
        private int createdBy;
        private LocalDateTime dataUpdated;

        // --- Constructors ---
        public Customer() {}

        public Customer(String name, String contactNo, String email, String address, String city, String status) {
            this.name = name;
            this.contactNo = contactNo;
            this.email = email;
            this.address = address;
            this.city = city;
            this.status = status;
        }

        // --- Getters and Setters ---
        public int getCustomerId() { return customerId; }
        public void setCustomerId(int customerId) { this.customerId = customerId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getContactNo() { return contactNo; }
        public void setContactNo(String contactNo) { this.contactNo = contactNo; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public LocalDateTime getDateRegistered() { return dateRegistered; }
        public void setDateRegistered(LocalDateTime dateRegistered) { this.dateRegistered = dateRegistered; }

        public int getTotalOrders() { return totalOrders; }
        public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

        public LocalDate getLastOrderDate() { return lastOrderDate; }
        public void setLastOrderDate(LocalDate lastOrderDate) { this.lastOrderDate = lastOrderDate; }

        public String getCustomerType() { return customerType; }
        public void setCustomerType(String customerType) { this.customerType = customerType; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public int getCreatedBy() { return createdBy; }
        public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

        public LocalDateTime getDataUpdated() { return dataUpdated; }
        public void setDataUpdated(LocalDateTime dataUpdated) { this.dataUpdated = dataUpdated; }

        @Override
        public String toString() {
            return "Customer [customerId=" + customerId + ", name=" + name + ", email=" + email +
                ", contactNo=" + contactNo + ", city=" + city + ", status=" + status + "]";
        }
    }
