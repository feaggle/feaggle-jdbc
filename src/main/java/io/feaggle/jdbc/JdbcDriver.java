/* Copyright (c) 2019-present, Kevin Mas Ruiz
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.feaggle.jdbc;

import io.feaggle.DriverLoader;
import io.feaggle.jdbc.drivers.experiment.JdbcExperimentDriver;
import io.feaggle.jdbc.drivers.experiment.SegmentDefinition;
import io.feaggle.jdbc.drivers.release.JdbcReleaseDriver;
import io.feaggle.toggle.experiment.ExperimentCohort;
import io.feaggle.toggle.experiment.ExperimentDriver;
import io.feaggle.toggle.operational.OperationalDriver;
import io.feaggle.toggle.release.ReleaseDriver;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;

public class JdbcDriver<T extends ExperimentCohort> implements DriverLoader<T>, Closeable {
    private final JdbcReleaseDriver releaseDriver;
    private final JdbcExperimentDriver<T> experimentDriver;
    private final OperationalDriver operationalDriver;

    JdbcDriver(Connection connection, String releaseQueryDefinition, String experimentQueryDefinition, SegmentDefinition<T> segments, OperationalDriver operationalDriver) {
        if (releaseQueryDefinition != null) {
            this.releaseDriver = new JdbcReleaseDriver(connection, releaseQueryDefinition);
        } else {
            this.releaseDriver = null;
        }

        if (!(experimentQueryDefinition == null || segments == null)) {
            this.experimentDriver = new JdbcExperimentDriver<>(connection, experimentQueryDefinition, segments);
        } else {
            this.experimentDriver = null;
        }

        this.operationalDriver = operationalDriver;
    }

    public static <T extends ExperimentCohort> JdbcDriverBuilder<T> from(Connection connection) {
        return new JdbcDriverBuilder<>(connection);
    }

    @Override
    public ExperimentDriver<T> loadExperimentDriver() {
        return experimentDriver;
    }

    @Override
    public OperationalDriver loadOperationalDriver() {
        return operationalDriver;
    }

    @Override
    public ReleaseDriver loadReleaseDriver() {
        return releaseDriver;
    }

    @Override
    public void close() throws IOException {
        if (releaseDriver != null) {
            releaseDriver.close();
        }

        if (experimentDriver != null) {
            experimentDriver.close();
        }
    }
}
