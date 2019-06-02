/* Copyright (c) 2019-present, Kevin Mas Ruiz
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.feaggle.jdbc;

import io.feaggle.jdbc.drivers.experiment.SegmentDefinition;
import io.feaggle.jdbc.drivers.experiment.SegmentResolver;
import io.feaggle.toggle.experiment.ExperimentCohort;
import io.feaggle.toggle.operational.OperationalDriver;

import java.sql.Connection;

import static io.feaggle.jdbc.drivers.experiment.SegmentDefinition.withSegments;

public class JdbcDriverBuilder<T extends ExperimentCohort> {
    private final Connection connection;
    private String releaseQueryDefinition;
    private String experimentQueryDefinition;
    private SegmentDefinition<T> segmentDefinition;
    private OperationalDriver operationalDriver;

    JdbcDriverBuilder(Connection connection) {
        this.connection = connection;
    }

    public JdbcDriverBuilder<T> releasesAre(String queryDefinition) {
        this.releaseQueryDefinition = queryDefinition;
        return this;
    }

    public JdbcDriverBuilder<T> defaults(String fields, SegmentResolver<T> segmentResolver) {
        this.releaseQueryDefinition = "SELECT STATUS FROM RELEASES WHERE ID = ?";
        this.experimentQueryDefinition = "SELECT STATUS FROM EXPERIMENTS WHERE ID = ?";
        this.segmentDefinition = withSegments("SELECT " + fields + " FROM SEGMENTS WHERE ID = ?", segmentResolver);

        return this;
    }

    public JdbcDriverBuilder<T> experimentsAre(String queryDefinition, SegmentDefinition<T> segments) {
        this.experimentQueryDefinition = queryDefinition;
        this.segmentDefinition = segments;
        return this;
    }

    public JdbcDriverBuilder<T> operationalTogglesAre(OperationalDriver driver) {
        this.operationalDriver = driver;
        return this;
    }

    public JdbcDriver<T> build() {
        return new JdbcDriver<>(connection, releaseQueryDefinition, experimentQueryDefinition, segmentDefinition, operationalDriver);
    }
}
