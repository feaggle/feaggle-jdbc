package io.feaggle.jdbc.cohorts;

import io.feaggle.toggle.experiment.ExperimentCohort;

import java.util.UUID;

public class PremiumCohort implements ExperimentCohort {
    public final boolean isPremium;

    public PremiumCohort(boolean isPremium) {
        this.isPremium = isPremium;
    }

    @Override
    public String identifier() {
        return UUID.randomUUID().toString();
    }
}
