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
