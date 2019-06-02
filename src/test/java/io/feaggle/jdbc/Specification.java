/* Copyright (c) 2019-present, Kevin Mas Ruiz
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.feaggle.jdbc;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
        PreparedStatement statement = connection.prepareStatement("INSERT INTO RELEASES(ID, STATUS) VALUES(?, ?)");
        statement.setString(1, name);
        statement.setBoolean(2, status);

        statement.executeUpdate();
    }

    protected final void withExperiment(String name, boolean status) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO EXPERIMENTS(ID, STATUS) VALUES(?, ?)");
        statement.setString(1, name);
        statement.setBoolean(2, status);

        statement.executeUpdate();
    }

    protected final void rollout(String name, int percentage) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO SEGMENTS(ID, KIND, ROLLOUT, PREMIUM) VALUES(?, ?, ?, ?)");
        statement.setString(1, name);
        statement.setString(2, "ROLLOUT");
        statement.setInt(3, percentage);
        statement.setObject(4, null);

        statement.executeUpdate();
    }

    protected final void beingPremium(String name, boolean premium) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO SEGMENTS(ID, KIND, ROLLOUT, PREMIUM) VALUES(?, ?, ?, ?)");
        statement.setString(1, name);
        statement.setString(2, "PREMIUM");
        statement.setObject(3, null);
        statement.setBoolean(4, premium);

        statement.executeUpdate();
    }

    protected final Connection connection() {
        return connection;
    }
}
