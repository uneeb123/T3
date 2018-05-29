# Treasury Core

## Setup

1. Java SDK

Download: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html

Ensure that you have Java SDK installed on your machine. And environment variable $JAVA_HOME is set correctly.
To verify, open up terminal and execute: `echo $JAVA_HOME`. It should return a path to the SDK.

Note: React Native have problems with Java 9, so we will be using Java 1.8

2. Maven

Maven is a build automation tool that allows us to specify how to build the project. It also specifies the dependencies.

Download: https://maven.apache.org/download.cgi
Install: https://maven.apache.org/install.html

We are using Maven 3.2.2

Now try executing `mvn -v`. If everything is in place, it should display the version of your Maven.

## Dependencies

1. bitcoinj 0.14.7
2. slf4j-simple 1.7.25
