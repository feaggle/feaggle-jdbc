package io.feaggle.jdbc;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class Specification {
    private final String DATABASE_CONNECTION = "jdbc:h2:mem:testdb";
    private Connection connection;

    @BeforeEach
    public void setUpDatabaseConnection() throws SQLException {
        connection = DriverManager.getConnection(DATABASE_CONNECTION);
        Flyway.configure()
                .dataSource(DATABASE_CONNECTION, "", "")
                .load()
                .migrate();
    }

    @AfterEach
    public void tearDownDatabaseConnection() throws SQLException {
        connection.close();
    }

    protected final void withRelease(String name, boolean status) throws SQLException {
        var statement = connection.prepareStatement("INSERT INTO RELEASES(ID, STATUS) VALUES(?, ?)");
        statement.setString(1, name);
        statement.setBoolean(2, status);

        statement.executeUpdate();
    }

    protected final Connection connection() {
        return connection;
    }
}
