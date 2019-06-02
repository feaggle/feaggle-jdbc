# ![feaggle_jvm](https://user-images.githubusercontent.com/3071208/53872344-531a8200-3ffe-11e9-8563-f581f83c1697.png)

[![Build Status](https://travis-ci.org/feaggle/feaggle-jdbc.svg?branch=master)](https://travis-ci.org/feaggle/feaggle-jdbc)
[![Coverage Status](https://coveralls.io/repos/github/feaggle/feaggle-jdbc/badge.svg?branch=master)](https://coveralls.io/github/feaggle/feaggle-jdbc?branch=master)
[ ![Download](https://api.bintray.com/packages/kmruiz/feaggle/feaggle-jdbc/images/download.svg) ](https://bintray.com/kmruiz/feaggle/feaggle-jdbc/_latestVersion)

*feaggle* is a feature toggle library that aims to be comfortable and versatile, in a way that
it can grow with your project.

It supports three kind of toggles:

* *Release Toggles*: Enables behaviour from a simple configuration. It can be used for enabling features under
continuous delivery.
* *Experiment Toggles*: Enable switching behaviour depending on the cohort. A cohort is a subset of people, 
defined from one to several segments.
* *Operational Toggles*: Supports changing the behaviour of an application depending on the outcome of
different sensors.

You can read more information about feaggle itself on [its main repository](https://github.com/feaggle/feaggle) or
the [webpage](https://www.feaggle.org/).

This document only explains how to configure feaggle with the jdbc backend, about the actual usage please go to the
[main repository page](https://github.com/feaggle/feaggle) or to the [webpage](https://www.feaggle.org/).

## How To Configure

feaggle-jdbc requires a jdbc connection to get the information from your jdbc store. There is a builder that will let
you create your own JdbcDriver that can be loaded into feaggle.

```java
JdbcDriver driver = JdbcDriver.from(connection())
    // ... more configuration
    .build();

Feaggle feaggle = Feaggle.load(driver); // Use feaggle as always
```

Depending on which features you want to use on feaggle-jdbc, you will need to set up one or more drivers.

### Releases

Releases can be stored in any way in your database, the only requirement that feaggle-jdbc has is a SELECT query
that will return a single boolean field and can be filtered by the release name. Something like:

```sql
SELECT STATUS FROM RELEASES WHERE ID = ?
```

With that query in hand, you can create a JdbcDriver:

```java
JdbcDriver.from(connection())
        .releasesAre("SELECT STATUS FROM RELEASES WHERE ID = ?")
        .build()
```

### Experiments

Experiments are a bit more complex, as they need segmentation. However, feaggle-jdbc is not opinionated on how you store
the information about experiments, but only on how to retrieve it.

#### Experiment Information
To retrieve the experiment information, we need a similar query as in releases, but for experiments. For example:

```sql
SELECT STATUS FROM EXPERIMENTS WHERE ID = ?
```

#### Segmentation

Segmentation requires three steps:

##### Cohorts

A cohort represents the information of a user. This cohort can be evaluated under a segment to verify if the cohort
can access a concrete experiment.

A cohort is a basic Java class that implements ExperimentCohort:

```java
public class PremiumCohort implements ExperimentCohort {
    public final boolean isPremium;

    public PremiumCohort(boolean isPremium) {
        this.isPremium = isPremium;
    }

    @Override
    public String identifier() {
        return UUID.randomUUID().toString(); // or your user id
    }
}
```

Cohorts will be used by feaggle to make sure that the user can access the experiment:

```java
feaggle.experiment("my-experiment").isEnabledFor(new PremiumCohort(myUserInfo.isPremium));
```

##### Segments

Segments represent a subset of the users that can apply to an experiment. You can read more detailed information
in the [main repository page](https://github.com/feaggle/feaggle). For now on, there is a single standard cohort:
the rollout cohort. It let's you roll out a feature to a subset of the customers, by percentage.

You can create custom segments creating a class that implements `Segment<YourCohort>`. For example, the following
segment will only allow premium users:

```java
public class PremiumSegment implements Segment<PremiumCohort> {
    public PremiumSegment() {
    }

    @Override
    public boolean evaluate(PremiumCohort cohort) {
        return cohort.isPremium;
    }
}
```


