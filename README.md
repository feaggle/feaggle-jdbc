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

## Gradle Dependency

You will need to use jcenter as a repository:

```groovy
repositories {
    //... 
    jcenter()
    //...
}
```

And the dependency:

```groovy
compile "io.feaggle:feaggle-jdbc:1.0.3"
```

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

## Simple configuration

If you don't have any database set up and you only need release toggles, you can use the default configuration provided
by the JdbcDriver. You will need to run this SQL migration into the database:

```sql
CREATE TABLE RELEASES(
    ID VARCHAR(256) PRIMARY KEY,
    STATUS BIT
);
```

And create the feaggle configuration as in:

```java
Feaggle feaggle = Feaggle.load(
    JdbcDriver.from(yourJdbcConnection)
        .defaults()
        .build()
);
```

And feaggle will be ready for work. If you need more advanced configuration and features (like experiments and operational toggles)
continue reading:

## Advanced configuration

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

Segment information needs to be in a separate table with a 1 to N relationship with your experiments table. You will
need to write a query that gets the information of the segments based on the experiment id, like in:

```sql
SELECT KIND, ROLLOUT, PREMIUM FROM SEGMENTS WHERE ID = ?
```

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

feaggle-jdbc will need also a way to map your information in the database to segments, and the way it nows is by a custom
`SegmentResolver<ExperimentCohort>`. A SegmentResolver will receive a ResultSet with a single row returned by the provided
segmentation SQL query. An example implementation would be:

```java
public class MySegmentResolver implements SegmentResolver<PremiumCohort> {
    @Override
        public Segment<PremiumCohort> resolveResultSet(ResultSet resultSet) throws SQLException {
            String kind = resultSet.getString(1);
            switch (kind) {
                case "ROLLOUT": return Rollout.<PremiumCohort>builder().percentage(resultSet.getInt(2)).build();
                case "PREMIUM": return new PremiumSegment(resultSet.getBoolean(3));
            }
    
            return null;
        }
}
```

For example, if the query returns the following information:

| KIND    | ROLLOUT | PREMIUM |
|---------|---------|---------|
| ROLLOUT | 50      |         |
| PREMIUM |         | 1       |

It will return the following segments:

* Rollout(50 percent of the traffic)
* Premium(only premiums are allowed)

##### Set Up

Now that everything is ready, you can set up your driver to use experiments:

```java
JdbcDriver.<PremiumCohort>from(connection())
        .experimentsAre(
                "SELECT STATUS FROM EXPERIMENTS WHERE ID = ?",
                withSegments(
                        "SELECT KIND, ROLLOUT, PREMIUM FROM SEGMENTS WHERE ID = ?",
                        new MySegmentResolver()
                )
        ).build();
```

You can see a running example in [the ExperimentToggleTest](src/test/java/io/feaggle/jdbc/specs/ExperimentToggleTest.java)
and you can check which migrations are ran in the database [here](src/test/resources/db/migration/V1__Schema.sql).

### Operational Toggles

Operational toggles are not supported yet, so they need to be passed in memory using the BasicOperationalDriver.

```java
JdbcDriver.from(connection())
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
    ).build();
```

## Composition of drivers

You can compose all your drivers in one driver loader, so you can load experiments and releases from the same connection:

```java
JdbcDriver.<PremiumCohort>from(connection())
    .releasesAre("SELECT STATUS FROM RELEASES WHERE ID = ?")
    .experimentsAre(
            "SELECT STATUS FROM EXPERIMENTS WHERE ID = ?",
            withSegments(
                    "SELECT KIND, ROLLOUT, PREMIUM FROM SEGMENTS WHERE ID = ?",
                    new MySegmentResolver()
            )
    )
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
    ).build();
```

## Set Up From Scratch

If you want to use feaggle-jdbc and you don't have any database configuration yet, you can use some sensitive defaults so
it's easier to set up everything. With the defaults, you can build your database with the following schema:

```sql
CREATE TABLE RELEASES(
    ID VARCHAR(256) PRIMARY KEY,
    STATUS BIT
);

CREATE TABLE EXPERIMENTS(
    ID VARCHAR(256) PRIMARY KEY,
    STATUS BIT
);

CREATE TABLE SEGMENTS(
    ID VARCHAR(256),
    KIND VARCHAR(256) NOT NULL,
    ROLLOUT INT,
    PREMIUM BIT -- you can add or remove fields here. Just keep ID, KIND and ROLLOUT
);

CREATE INDEX SEGMENTS_BY_EXPERIMENT ON SEGMENTS (ID);
```

You can optimize it further for your needs.

Then you will only need to build your own SegmentResolver (as explained above) and choose the needed fields from the table:

```java
public class MySegmentResolver implements SegmentResolver<PremiumCohort> {
    @Override
        public Segment<PremiumCohort> resolveResultSet(ResultSet resultSet) throws SQLException {
            String kind = resultSet.getString(1);
            switch (kind) {
                case "ROLLOUT": return Rollout.<PremiumCohort>builder().percentage(resultSet.getInt(2)).build();
                case "PREMIUM": return new PremiumSegment(resultSet.getBoolean(3));
            }
    
            return null;
        }
}
```

```java
driverLoader = JdbcDriver.<PremiumCohort>from(connection())
    .defaults("KIND, ROLLOUT, PREMIUM", this)
    .build();
```