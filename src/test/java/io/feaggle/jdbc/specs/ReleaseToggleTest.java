/* Copyright (c) 2019-present, Kevin Mas Ruiz
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.feaggle.jdbc.specs;

import io.feaggle.Feaggle;
import io.feaggle.jdbc.JdbcDriver;
import io.feaggle.jdbc.Specification;
import io.feaggle.jdbc.cohorts.NoopCohort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReleaseToggleTest extends Specification {
    private JdbcDriver<NoopCohort> driverLoader;
    private Feaggle<NoopCohort> feaggle;
    private String releaseName;

    @BeforeEach
    public void setUp() {
        driverLoader = JdbcDriver.<NoopCohort>from(connection())
                            .releasesAre(
                                    "SELECT STATUS FROM RELEASES WHERE ID = ?"
                            ).build();

        releaseName = UUID.randomUUID().toString();
        feaggle = Feaggle.load(driverLoader);
    }

    @AfterEach
    void tearDown() throws IOException {
        driverLoader.close();
    }

    @Test
    public void shouldActivateAReleaseIfStoredAsActive() throws SQLException {
        withRelease(releaseName, true);
        assertTrue(feaggle.release(releaseName).isEnabled());
    }

    @Test
    public void shouldDeactivateAReleaseIfStoredAsInactive() throws SQLException {
        withRelease(releaseName, false);
        assertFalse(feaggle.release(releaseName).isEnabled());
    }

    @Test
    public void shouldDeactivateAReleaseIfDoesNotExist() throws SQLException {
        assertFalse(feaggle.release(releaseName).isEnabled());
    }
}
