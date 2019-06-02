/* Copyright (c) 2019-present, Kevin Mas Ruiz
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.feaggle.jdbc.specs;

import io.feaggle.Feaggle;
import io.feaggle.jdbc.JdbcDriver;
import io.feaggle.jdbc.Specification;
import io.feaggle.jdbc.cohorts.PremiumCohort;
import io.feaggle.jdbc.cohorts.segments.PremiumSegment;
import io.feaggle.jdbc.drivers.experiment.SegmentResolver;
import io.feaggle.toggle.experiment.segment.Rollout;
import io.feaggle.toggle.experiment.segment.Segment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static io.feaggle.jdbc.drivers.experiment.SegmentDefinition.withSegments;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExperimentToggleTest extends Specification implements SegmentResolver<PremiumCohort> {
    private JdbcDriver<PremiumCohort> driverLoader;
    private Feaggle<PremiumCohort> feaggle;
    private String experimentName;

    @BeforeEach
    public void setUp() {
        driverLoader = JdbcDriver.<PremiumCohort>from(connection())
                .experimentsAre(
                        "SELECT STATUS FROM EXPERIMENTS WHERE ID = ?",
                        withSegments(
                                "SELECT KIND, ROLLOUT, PREMIUM FROM SEGMENTS WHERE ID = ?",
                                this
                        )
                )
                .build();

        experimentName = UUID.randomUUID().toString();
        feaggle = Feaggle.load(driverLoader);
    }

    @AfterEach
    void tearDown() throws IOException {
        driverLoader.close();
    }

    @Test
    void shouldBeActivatedWhenCohortIsPartOfTheSegmentAndRolloutIs100Percent() throws SQLException {
        withExperiment(experimentName, true);
        rollout(experimentName, 100);
        beingPremium(experimentName, true);

        boolean isEnabled = feaggle.experiment(experimentName).isEnabledFor(new PremiumCohort(true));
        assertTrue(isEnabled);
    }

    @Test
    void shouldBeDectivatedWhenCohortIsPartOfTheSegmentAndRolloutIs0Percent() throws SQLException {
        withExperiment(experimentName, true);
        rollout(experimentName, 0);
        beingPremium(experimentName, true);

        boolean isEnabled = feaggle.experiment(experimentName).isEnabledFor(new PremiumCohort(true));
        assertFalse(isEnabled);
    }

    @Test
    void shouldBeDectivatedWhenCohortIsPartOfTheSegmentAndRolloutIs100PercentButExperimentIsDeactivated() throws SQLException {
        withExperiment(experimentName, false);
        rollout(experimentName, 100);
        beingPremium(experimentName, true);

        boolean isEnabled = feaggle.experiment(experimentName).isEnabledFor(new PremiumCohort(true));
        assertFalse(isEnabled);
    }

    @Test
    void shouldBeDectivatedWhenCohortIsPartOfTheSegmentAndRolloutIs100PercentButPremiumIsFalseOnCohort() throws SQLException {
        withExperiment(experimentName, true);
        rollout(experimentName, 100);
        beingPremium(experimentName, true);

        boolean isEnabled = feaggle.experiment(experimentName).isEnabledFor(new PremiumCohort(false));
        assertFalse(isEnabled);
    }

    @Override
    public Segment<PremiumCohort> resolveResultSet(ResultSet resultSet) throws SQLException {
        String kind = resultSet.getString(1);
        switch (kind) {
            case "ROLLOUT": return Rollout.<PremiumCohort>builder().percentage(resultSet.getInt(2)).build();
            case "PREMIUM": return new PremiumSegment(resultSet.getBoolean(3));
        }

        return null;
    }
}
