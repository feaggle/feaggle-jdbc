package io.feaggle.jdbc;

import io.feaggle.toggle.experiment.ExperimentCohort;

import java.sql.Connection;

public class JdbcDriverBuilder {
    private final Connection connection;
    private String releaseQueryDefinition;

    JdbcDriverBuilder(Connection connection) {
        this.connection = connection;
    }

    public JdbcDriverBuilder releasesAre(String queryDefinition) {
        this.releaseQueryDefinition = queryDefinition;
        return this;
    }

    public <T extends ExperimentCohort> JdbcDriver<T> build() {
        return new JdbcDriver<>(connection, releaseQueryDefinition);
    }
}
