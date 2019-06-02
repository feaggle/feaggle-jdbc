/* Copyright (c) 2019-present, Kevin Mas Ruiz
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.feaggle.jdbc.drivers.experiment;

import io.feaggle.jdbc.exceptions.JdbcStatusException;
import io.feaggle.toggle.experiment.ExperimentCohort;
import io.feaggle.toggle.experiment.ExperimentDriver;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JdbcExperimentDriver<T extends ExperimentCohort> implements ExperimentDriver<T>, Closeable {
    private final PreparedStatement queryExperiment;
    private final PreparedStatement querySegments;
    private final SegmentResolver<T> resolver;

    public JdbcExperimentDriver(Connection connection, String experimentQueryDefinition, SegmentDefinition<T> segments) {
        try {
            queryExperiment = connection.prepareStatement(experimentQueryDefinition);
        } catch (SQLException e) {
            throw new JdbcStatusException(
                    "Could not create prepared statement for querying the experiment status." +
                            "Please make sure that the provided query is valid." +
                            "\nQuery: " + experimentQueryDefinition,
                    e
            );
        }

        try {
            querySegments = connection.prepareStatement(segments.query);
        } catch (SQLException e) {
            throw new JdbcStatusException(
                    "Could not create prepared statement for querying the experiment segments." +
                            "Please make sure that the provided query is valid." +
                            "\nQuery: " + segments.query,
                    e
            );
        }

        resolver = segments.resolver;
    }

    @Override
    public boolean isEnabledForCohort(String experimentName, T cohort) {
        try {
            queryExperiment.setString(1, experimentName);
            try (var experimentRs = queryExperiment.executeQuery()) {
                if (!experimentRs.next()) {
                    return false;
                }

                var isEnabled = experimentRs.getBoolean(1);
                if (!isEnabled) {
                    return false;
                }

                querySegments.setString(1, experimentName);
                try (var segmentsRs = querySegments.executeQuery()) {
                    while (segmentsRs.next()) {
                        var segment = resolver.resolveResultSet(segmentsRs);
                        if (!segment.evaluate(cohort)) {
                            return false;
                        }
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            queryExperiment.close();
            querySegments.close();
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }
}
