/* Copyright (c) 2019-present, Kevin Mas Ruiz
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.feaggle.jdbc.cohorts.segments;

import io.feaggle.jdbc.cohorts.PremiumCohort;
import io.feaggle.toggle.experiment.segment.Segment;

public class PremiumSegment implements Segment<PremiumCohort> {
    private final boolean mustBePremium;

    public PremiumSegment(boolean mustBePremium) {
        this.mustBePremium = mustBePremium;
    }

    @Override
    public boolean evaluate(PremiumCohort cohort) {
        return cohort.isPremium == mustBePremium;
    }
}
