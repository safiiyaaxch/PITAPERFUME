package com.scentify;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class DatabaseTest implements CommandLineRunner {
    
    private final DataSource dataSource;
    
    public DatabaseTest(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== DATABASE TEST ===");
        try {
            Connection connection = dataSource.getConnection();
            System.out.println("✅ Database connected successfully!");
            System.out.println("URL: " + connection.getMetaData().getURL());
            connection.close();
        } catch (Exception e) {
            System.out.println("❌ Database connection FAILED!");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
