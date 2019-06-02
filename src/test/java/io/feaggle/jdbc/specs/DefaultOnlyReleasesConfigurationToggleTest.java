package io.feaggle.jdbc.specs;

import io.feaggle.Feaggle;
import io.feaggle.jdbc.JdbcDriver;
import io.feaggle.jdbc.Specification;
import io.feaggle.toggle.experiment.ExperimentCohort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultOnlyReleasesConfigurationToggleTest extends Specification {
    private JdbcDriver<ExperimentCohort> driverLoader;
    private Feaggle<ExperimentCohort> feaggle;
    private String toggleName;

    @BeforeEach
    public void setUp() {
        driverLoader = JdbcDriver.from(connection())
                .defaults()
                .build();

        toggleName = UUID.randomUUID().toString();
        feaggle = Feaggle.load(driverLoader);
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

}
