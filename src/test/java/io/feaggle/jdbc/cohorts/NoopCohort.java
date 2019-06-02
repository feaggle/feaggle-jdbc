package io.feaggle.jdbc.cohorts;

import io.feaggle.toggle.experiment.ExperimentCohort;

public class NoopCohort implements ExperimentCohort {
    @Override
    public String identifier() {
        return "";
    }
}
