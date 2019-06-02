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