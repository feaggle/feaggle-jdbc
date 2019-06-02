package io.feaggle.jdbc.drivers.experiment;

import io.feaggle.toggle.experiment.ExperimentCohort;

public class SegmentDefinition<Cohort extends ExperimentCohort> {
    public final String query;
    public final SegmentResolver<Cohort> resolver;

    private SegmentDefinition(String query, SegmentResolver<Cohort> resolver) {
        this.query = query;
        this.resolver = resolver;
    }

    public static <Cohort extends ExperimentCohort> SegmentDefinition<Cohort> withSegments(String query, SegmentResolver<Cohort> resolver) {
        return new SegmentDefinition<>(query, resolver);

    }
}
