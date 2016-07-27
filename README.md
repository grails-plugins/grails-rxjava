# RxJava for Grails

[RxJava](https://github.com/ReactiveX/RxJava) is a popular library for composing asynchronous and event-based programs by using observable sequences.

RxJava helps you build reactive applications and an increasing number of libraries take advantage of RxJava as the defacto standard for building Reactive applications.

In [GORM 6.0](http://gorm.grails.org/6.0.x), a new implementation of GORM called [RxGORM](http://gorm.grails.org/6.0.x/rx/manual) has been introduced that builds on RxJava helping you building reactive data access logic using the familiar GORM API combined with RxJava.

This plugin helps integrate RxJava with the controller layer of Grails to complete the picture and enable complete end-to-end integration of RxJava with Grails.

## Installation

To install the plugin declare a dependency in `build.gradle`:

```groovy
dependencies {
    ...
    compile 'org.grails.plugins:rxjava:{version}'
}
```

Where `{version}` is the version of the plugin.

## Documentation 

For further information see the the [User guide](http://grails-plugins.github.io/grails-rxjava/latest/). 
