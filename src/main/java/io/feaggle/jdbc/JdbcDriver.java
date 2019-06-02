package io.feaggle.jdbc;

import io.feaggle.DriverLoader;
import io.feaggle.jdbc.drivers.release.JdbcReleaseDriver;
import io.feaggle.toggle.experiment.ExperimentCohort;
import io.feaggle.toggle.experiment.ExperimentDriver;
import io.feaggle.toggle.operational.OperationalDriver;
import io.feaggle.toggle.release.ReleaseDriver;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;

public class JdbcDriver<T extends ExperimentCohort> implements DriverLoader<T>, Closeable {
    private JdbcReleaseDriver releaseDriver;

    JdbcDriver(Connection connection, String releaseQueryDefinition) {
        this.releaseDriver = new JdbcReleaseDriver(connection, releaseQueryDefinition);
    }

    public static JdbcDriverBuilder from(Connection connection) {
        return new JdbcDriverBuilder(connection);
    }

    @Override
    public ExperimentDriver<T> loadExperimentDriver() {
        return null;
    }

    @Override
    public OperationalDriver loadOperationalDriver() {
        return null;
    }

    @Override
    public ReleaseDriver loadReleaseDriver() {
        return releaseDriver;
    }

    @Override
    public void close() throws IOException {
        this.releaseDriver.close();
    }
}
