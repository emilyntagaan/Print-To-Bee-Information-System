    package com.println.dao;

    import com.println.config.DBConnection;
    import com.println.model.Product;
    import java.sql.*;
    import java.util.ArrayList;
    import java.util.List;

    public class ProductDAO {

        // --- CREATE ---
        public boolean addProduct(Product product) {
            String sql = "INSERT INTO products (product_name, description, category, price, unit, material_used, " +
                        "quantity_used, reorder_level, status, added_by, print_time, size, notes, inventory_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, product.getProductName());
                ps.setString(2, product.getDescription());
                ps.setString(3, product.getCategory());
                ps.setBigDecimal(4, product.getPrice());
                ps.setString(5, product.getUnit());
                ps.setString(6, product.getMaterialUsed());
                ps.setInt(7, product.getQuantityUsed());
                ps.setInt(8, product.getReorderLevel());
                ps.setString(9, product.getStatus() != null ? product.getStatus() : "Active");

                if (product.getAddedBy() != null)
                    ps.setInt(10, product.getAddedBy());
                else
                    ps.setNull(10, Types.INTEGER);

                ps.setString(11, product.getPrintTime());
                ps.setString(12, product.getSize());
                ps.setString(13, product.getNotes());

                // inventory_id
                if (product.getInventoryId() != null)
                    ps.setInt(14, product.getInventoryId());
                else
                    ps.setNull(14, Types.INTEGER);

                return ps.executeUpdate() > 0;

            } catch (SQLException e) {
                System.err.println("Error adding product: " + e.getMessage());
                return false;
            }
        }

        // --- READ ALL ---
        public List<Product> getAllProducts() {
            List<Product> list = new ArrayList<>();
            String sql = "SELECT * FROM products ORDER BY product_id DESC";

            try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    list.add(extractProduct(rs));
                }

            } catch (SQLException e) {
                System.err.println("Error retrieving products: " + e.getMessage());
            }

            return list;
        }

        // --- READ ONE ---
        public Product getProductById(int id) {
            String sql = "SELECT * FROM products WHERE product_id = ?";

            try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return extractProduct(rs);
                    }
                }

            } catch (SQLException e) {
                System.err.println("Error fetching product by ID: " + e.getMessage());
            }

            return null;
        }

        // --- UPDATE ---
        public boolean updateProduct(Product product) {
            String sql = "UPDATE products SET product_name=?, description=?, category=?, price=?, unit=?, material_used=?, " +
                        "quantity_used=?, reorder_level=?, status=?, print_time=?, size=?, notes=?, inventory_id=? " +
                        "WHERE product_id=?";

            try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, product.getProductName());
                ps.setString(2, product.getDescription());
                ps.setString(3, product.getCategory());
                ps.setBigDecimal(4, product.getPrice());
                ps.setString(5, product.getUnit());
                ps.setString(6, product.getMaterialUsed());
                ps.setInt(7, product.getQuantityUsed());
                ps.setInt(8, product.getReorderLevel());
                ps.setString(9, product.getStatus());
                ps.setString(10, product.getPrintTime());
                ps.setString(11, product.getSize());
                ps.setString(12, product.getNotes());

                // inventory_id
                if (product.getInventoryId() != null)
                    ps.setInt(13, product.getInventoryId());
                else
                    ps.setNull(13, Types.INTEGER);

                ps.setInt(14, product.getProductId());

                return ps.executeUpdate() > 0;

            } catch (SQLException e) {
                System.err.println("Error updating product: " + e.getMessage());
                return false;
            }
        }

        // --- DELETE ---
        public boolean deleteProduct(int id) {
            String sql = "DELETE FROM products WHERE product_id = ?";

            try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, id);
                return ps.executeUpdate() > 0;

            } catch (SQLException e) {
                System.err.println("Error deleting product: " + e.getMessage());
                return false;
            }
        }

        // --- SEARCH ---
        public List<Product> searchProducts(String keyword) {
            List<Product> list = new ArrayList<>();
            String sql = "SELECT * FROM products WHERE product_name LIKE ? OR category LIKE ?";

            try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

                String like = "%" + keyword + "%";
                ps.setString(1, like);
                ps.setString(2, like);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(extractProduct(rs));
                    }
                }

            } catch (SQLException e) {
                System.err.println("Error searching products: " + e.getMessage());
            }

            return list;
        }

        // --- Helper Method ---
        private Product extractProduct(ResultSet rs) throws SQLException {
            Product p = new Product();
            p.setProductId(rs.getInt("product_id"));
            p.setProductName(rs.getString("product_name"));
            p.setDescription(rs.getString("description"));
            p.setCategory(rs.getString("category"));
            p.setPrice(rs.getBigDecimal("price"));
            p.setUnit(rs.getString("unit"));
            p.setMaterialUsed(rs.getString("material_used"));
            p.setQuantityUsed(rs.getInt("quantity_used"));
            p.setReorderLevel(rs.getInt("reorder_level"));
            p.setStatus(rs.getString("status"));

            // date_added
            Timestamp ts = rs.getTimestamp("date_added");
            if (ts != null) {
                p.setDateAdded(ts.toLocalDateTime());
            }

            // Safe nullable read for added_by
            Object addedByObj = rs.getObject("added_by");
            p.setAddedBy(addedByObj != null ? rs.getInt("added_by") : null);

            p.setPrintTime(rs.getString("print_time"));
            p.setSize(rs.getString("size"));
            p.setNotes(rs.getString("notes"));

            // inventory_id
            Object invObj = rs.getObject("inventory_id");
            p.setInventoryId(invObj != null ? rs.getInt("inventory_id") : null);

            return p;
        }
    }
