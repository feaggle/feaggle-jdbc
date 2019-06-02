/* Copyright (c) 2019-present, Kevin Mas Ruiz
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.feaggle.jdbc.specs;

import io.feaggle.Feaggle;
import io.feaggle.jdbc.JdbcDriver;
import io.feaggle.jdbc.Specification;
import io.feaggle.toggle.experiment.ExperimentCohort;
import io.feaggle.toggle.operational.BasicOperationalDriver;
import io.feaggle.toggle.operational.Rule;
import io.feaggle.toggle.operational.sensor.Cpu;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class OperationalToggleTest extends Specification {
    private JdbcDriver<ExperimentCohort> driverLoader;
    private Feaggle<ExperimentCohort> feaggle;
    private String operationalName;

    @BeforeEach
    public void setUp() {
        operationalName = UUID.randomUUID().toString();

        driverLoader = JdbcDriver.from(connection())
                .operationalTogglesAre(
                        BasicOperationalDriver.builder()
                            .rule(Rule.builder()
                                    .toggle(operationalName)
                                    .enabled(true)
                                    .sensor(
                                            Cpu.builder().predicate(Cpu.usageIsGreaterThan(0)).build()
                                    )
                                    .build()
                            ).build()
                )
                .build();

        feaggle = Feaggle.load(driverLoader);
    }

    @AfterEach
    void tearDown() throws IOException {
        driverLoader.close();
    }

    @Test
    void shouldDelegateToTheProvidedOperationalDriver() throws SQLException {
        boolean isEnabled = feaggle.operational(operationalName).isEnabled();
        assertTrue(isEnabled);
    }
}
