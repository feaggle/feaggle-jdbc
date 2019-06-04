package io.feaggle.jdbc.specs;

import io.feaggle.Feaggle;
import io.feaggle.jdbc.JdbcDriver;
import io.feaggle.jdbc.Specification;
import io.feaggle.jdbc.cohorts.PremiumCohort;
import io.feaggle.jdbc.cohorts.segments.PremiumSegment;
import io.feaggle.jdbc.drivers.experiment.SegmentResolver;
import io.feaggle.jdbc.exceptions.JdbcStatusException;
import io.feaggle.toggle.experiment.segment.Rollout;
import io.feaggle.toggle.experiment.segment.Segment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultConfigurationToggleTest extends Specification implements SegmentResolver<PremiumCohort> {
    private JdbcDriver<PremiumCohort> driverLoader;
    private Feaggle<PremiumCohort> feaggle;
    private String toggleName;

    @BeforeEach
    public void setUp() {
        driverLoader = JdbcDriver.<PremiumCohort>from(connection())
                .defaults("KIND, ROLLOUT, PREMIUM", this)
                .build();

        toggleName = UUID.randomUUID().toString();
        feaggle = Feaggle.load(driverLoader);
    }

    @Test
    void shouldFailIfTheConnectionCanNotBeEstablished() throws SQLException {
        connection().close();

        assertThrows(JdbcStatusException.class, () -> {
            driverLoader = JdbcDriver.<PremiumCohort>from(connection())
                .defaults("KIND, ROLLOUT, PREMIUM", this)
                .build();
        });
    }

    @Test
    void shouldBeActivatedWhenCohortIsPartOfTheSegmentAndRolloutIs100Percent() throws SQLException {
        withExperiment(toggleName, true);
        rollout(toggleName, 100);
        beingPremium(toggleName, true);

        boolean isEnabled = feaggle.experiment(toggleName).isEnabledFor(new PremiumCohort(true));
        assertTrue(isEnabled);
    }

    @Test
    void shouldBeDectivatedWhenCohortIsPartOfTheSegmentAndRolloutIs100PercentButExperimentIsDeactivated() throws SQLException {
        withExperiment(toggleName, false);
        rollout(toggleName, 100);
        beingPremium(toggleName, true);

        boolean isEnabled = feaggle.experiment(toggleName).isEnabledFor(new PremiumCohort(true));
        assertFalse(isEnabled);
    }

    @Test
    public void shouldActivateAReleaseIfStoredAsActive() throws SQLException {
        withRelease(toggleName, true);
        assertTrue(feaggle.release(toggleName).isEnabled());
    }

    @Test
    public void shouldDeactivateAReleaseIfStoredAsInactive() throws SQLException {
        withRelease(toggleName, false);
        assertFalse(feaggle.release(toggleName).isEnabled());
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
