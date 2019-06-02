/* Copyright (c) 2019-present, Kevin Mas Ruiz
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.feaggle.jdbc.drivers.release;

import io.feaggle.jdbc.exceptions.JdbcStatusException;
import io.feaggle.toggle.release.ReleaseDriver;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcReleaseDriver implements ReleaseDriver, Closeable {
    private final PreparedStatement statement;

    public JdbcReleaseDriver(Connection connection, String releaseQueryDefinition) {
        try {
            statement = connection.prepareStatement(releaseQueryDefinition);
        } catch (SQLException e) {
            throw new JdbcStatusException(
                    "Could not create prepared statement for querying releases. " +
                            "Please make sure that the provided query is valid." +
                            "\nQuery: " + releaseQueryDefinition,
                    e);
        }
    }

    @Override
    public boolean isFlaggedForRelease(String feature) {
        try {
            statement.setString(1, feature);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return false;
                }

                return resultSet.getBoolean(1);
            }

        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            statement.close();
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }
}
