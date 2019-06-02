/* Copyright (c) 2019-present, Kevin Mas Ruiz
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.feaggle.jdbc.drivers.experiment;

import io.feaggle.toggle.experiment.ExperimentCohort;
import io.feaggle.toggle.experiment.segment.Segment;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface SegmentResolver<Cohort extends ExperimentCohort> {
    Segment<Cohort> resolveResultSet(ResultSet resultSet) throws SQLException;
}
