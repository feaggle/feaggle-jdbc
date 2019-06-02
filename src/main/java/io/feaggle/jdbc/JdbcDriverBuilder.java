package io.feaggle.jdbc;

import io.feaggle.jdbc.drivers.experiment.SegmentDefinition;
import io.feaggle.toggle.experiment.ExperimentCohort;

import java.sql.Connection;

public class JdbcDriverBuilder<T extends ExperimentCohort> {
    private final Connection connection;
    private String releaseQueryDefinition;
    private String experimentQueryDefinition;
    private SegmentDefinition<T> segmentDefinition;

    JdbcDriverBuilder(Connection connection) {
        this.connection = connection;
    }

    public JdbcDriverBuilder releasesAre(String queryDefinition) {
        this.releaseQueryDefinition = queryDefinition;
        return this;
    }

    public JdbcDriverBuilder experimentsAre(String queryDefinition, SegmentDefinition<T> segments) {
        this.experimentQueryDefinition = queryDefinition;
        this.segmentDefinition = segments;
        return this;
    }

    public JdbcDriver<T> build() {
        return new JdbcDriver<>(connection, releaseQueryDefinition, experimentQueryDefinition, segmentDefinition);
    }
}
