
package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class config {
    //Connection Method to SQLITE
public static Connection connectDB() {
        Connection con = null;
        try {
            Class.forName("org.sqlite.JDBC"); // Load the SQLite JDBC driver
            con = DriverManager.getConnection("jdbc:sqlite:vaccineDB.db"); // Establish connection
            System.out.println("Connection Successful");
        } catch (Exception e) {
            System.out.println("Connection Failed: " + e);
        }
        return con;
    }

public void addRecord(String sql, Object... values) {
    try (Connection conn = this.connectDB(); // Use the connectDB method
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        // Loop through the values and set them in the prepared statement dynamically
        for (int i = 0; i < values.length; i++) {
            if (values[i] instanceof Integer) {
                pstmt.setInt(i + 1, (Integer) values[i]); // If the value is Integer
            } else if (values[i] instanceof Double) {
                pstmt.setDouble(i + 1, (Double) values[i]); // If the value is Double
            } else if (values[i] instanceof Float) {
                pstmt.setFloat(i + 1, (Float) values[i]); // If the value is Float
            } else if (values[i] instanceof Long) {
                pstmt.setLong(i + 1, (Long) values[i]); // If the value is Long
            } else if (values[i] instanceof Boolean) {
                pstmt.setBoolean(i + 1, (Boolean) values[i]); // If the value is Boolean
            } else if (values[i] instanceof java.util.Date) {
                pstmt.setDate(i + 1, new java.sql.Date(((java.util.Date) values[i]).getTime())); // If the value is Date
            } else if (values[i] instanceof java.sql.Date) {
                pstmt.setDate(i + 1, (java.sql.Date) values[i]); // If it's already a SQL Date
            } else if (values[i] instanceof java.sql.Timestamp) {
                pstmt.setTimestamp(i + 1, (java.sql.Timestamp) values[i]); // If the value is Timestamp
            } else {
                pstmt.setString(i + 1, values[i].toString()); // Default to String for other types
            }
        }

        pstmt.executeUpdate();
        System.out.println("Record added successfully!");
    } catch (SQLException e) {
        System.out.println("Error adding record: " + e.getMessage());
    }
}

// Dynamic view method to display records from any table
public void viewRecords(String sqlQuery, String[] columnHeaders, String[] columnNames) {
        // Check that columnHeaders and columnNames arrays are the same length
        if (columnHeaders.length != columnNames.length) {
            System.out.println("Error: Mismatch between column headers and column names.");
            return;
        }

        try (Connection conn = this.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery);
             ResultSet rs = pstmt.executeQuery()) {

            // Print the headers dynamically
            StringBuilder headerLine = new StringBuilder();
            headerLine.append("--------------------------------------------------------------------------------\n| ");
            for (String header : columnHeaders) {
                headerLine.append(String.format("%-20s | ", header)); // Adjust formatting as needed
            }
            headerLine.append("\n--------------------------------------------------------------------------------");

            System.out.println(headerLine.toString());

            // Print the rows dynamically based on the provided column names
            while (rs.next()) {
                StringBuilder row = new StringBuilder("| ");
                for (String colName : columnNames) {
                    String value = rs.getString(colName);
                    row.append(String.format("%-20s | ", value != null ? value : "")); // Adjust formatting
                }
                System.out.println(row.toString());
            }
            System.out.println("--------------------------------------------------------------------------------");

        } catch (SQLException e) {
            System.out.println("Error retrieving records: " + e.getMessage());
        }
    }
    
// Update or Edit
public void updateRecord(String sql, Object... values) {
        try (Connection conn = this.connectDB(); // Use the connectDB method
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Loop through the values and set them in the prepared statement dynamically
            for (int i = 0; i < values.length; i++) {
                if (values[i] instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) values[i]); // If the value is Integer
                } else if (values[i] instanceof Double) {
                    pstmt.setDouble(i + 1, (Double) values[i]); // If the value is Double
                } else if (values[i] instanceof Float) {
                    pstmt.setFloat(i + 1, (Float) values[i]); // If the value is Float
                } else if (values[i] instanceof Long) {
                    pstmt.setLong(i + 1, (Long) values[i]); // If the value is Long
                } else if (values[i] instanceof Boolean) {
                    pstmt.setBoolean(i + 1, (Boolean) values[i]); // If the value is Boolean
                } else if (values[i] instanceof java.util.Date) {
                    pstmt.setDate(i + 1, new java.sql.Date(((java.util.Date) values[i]).getTime())); // If the value is Date
                } else if (values[i] instanceof java.sql.Date) {
                    pstmt.setDate(i + 1, (java.sql.Date) values[i]); // If it's already a SQL Date
                } else if (values[i] instanceof java.sql.Timestamp) {
                    pstmt.setTimestamp(i + 1, (java.sql.Timestamp) values[i]); // If the value is Timestamp
                } else {
                    pstmt.setString(i + 1, values[i].toString()); // Default to String for other types
                }
            }

            pstmt.executeUpdate();
            System.out.println("Record updated successfully!");
        } catch (SQLException e) {
            System.out.println("Error updating record: " + e.getMessage());
        }
    }
    
// Add this method in the config class
public void deleteRecord(String sql, Object... values) {
    try (Connection conn = this.connectDB();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        // Loop through the values and set them in the prepared statement dynamically
        for (int i = 0; i < values.length; i++) {
            if (values[i] instanceof Integer) {
                pstmt.setInt(i + 1, (Integer) values[i]); // If the value is Integer
            } else {
                pstmt.setString(i + 1, values[i].toString()); // Default to String for other types
            }
        }

        pstmt.executeUpdate();
        System.out.println("Record deleted successfully!");
    } catch (SQLException e) {
        System.out.println("Error deleting record: " + e.getMessage());
    }
}
    
// Fetch Records
public java.util.List<java.util.Map<String, Object>> fetchRecords(String sqlQuery, Object... values) {
    // 1. Fixed List capitalization and 2. Fixed ArrayList instantiation
    java.util.List<java.util.Map<String, Object>> records = new java.util.ArrayList<>();
    
    // 3. Fixed try-with-resources syntax: replaced { with )
    try (java.sql.Connection conn = this.connectDB();
          java.sql.PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) { 
        
        for (int i = 0; i < values.length; i++) {
            pstmt.setObject(i + 1, values[i]);
        }
        
        java.sql.ResultSet rs = pstmt.executeQuery();
        java.sql.ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        while (rs.next()){
            // 4. Added missing 'new' keyword for HashMap
            java.util.Map<String, Object> row = new java.util.HashMap<>(); 
            for(int i=1; i <= columnCount; i++){
                row.put(metaData.getColumnName(i), rs.getObject(i));
            }
            records.add(row);
        }
        
    } catch (java.sql.SQLException e) {
        System.out.println("Error fetching records: " + e.getMessage());
    }
    return records;
}

// Same as Fetch
public int getVaccineStock(int vId) {
    String sql = "SELECT v_stock FROM tbl_vaccine WHERE v_id = ?";
    
    try (java.sql.Connection conn = connectDB();
         java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setInt(1, vId);
        
        try (java.sql.ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                // Return the stock value found
                return rs.getInt("v_stock");
            } else {
                // Vaccine ID not found (treat as zero stock for safety)
                System.out.println("⚠️ Warning: Vaccine ID " + vId + " not found.");
                return 0; 
            }
        }
    } catch (java.sql.SQLException e) {
        System.out.println("❌ Database Error fetching stock: " + e.getMessage());
        return -1; // Indicate a serious error
    }
}

// Method to hash passwords using SHA-256
public static String hashPassword(String password) {
    try {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        
        // Convert byte array to hex string
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashedBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    } catch (java.security.NoSuchAlgorithmException e) {
        System.out.println("Error hashing password: " + e.getMessage());
        return null;
    }
}

public boolean emailExists(String email) {
    // We only need the ID, but SELECT * is fine too if you prefer
    String sql = "SELECT id FROM tbl_users WHERE email = ?"; 
    
    // Use the existing fetchRecords method
    java.util.List<java.util.Map<String, Object>> result = fetchRecords(sql, email);
    
    // Check if the list of maps is empty. If it's not empty, the email exists.
    return !result.isEmpty();
}

}
