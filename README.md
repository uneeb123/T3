# Treasury Core

## Setup

### 1. Java SDK

Download: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html

Ensure that you have Java SDK installed on your machine. And environment variable $JAVA_HOME is set correctly.
To verify, open up terminal and execute: `echo $JAVA_HOME`. It should return a path to the SDK.

Note: React Native have problems with Java 9, so we will be using Java 1.8

### 2. Maven

Maven is a build automation tool that allows us to specify how to build the project. It also specifies the dependencies.

Download: https://maven.apache.org/download.cgi
Install: https://maven.apache.org/install.html

We are using Maven 3.2.2

Now try executing `mvn -v`. If everything is in place, it should display the version of your Maven.

### [OPTIONAL] IntelliJ

## Architecture

Treasury Core is a bitcoin wallet that entirely rests on the client-side with the user. However, it communicates with 
the Treasury Server is post some important information. Such as updating balance, posting incoming and outgoing transactions and 
syncing the server to ensure that there are no discrepancies.

Treasury Core ensures that any transaction made with treasury complies with certain properties as expressed in the server, declared 
during the creation of a particular treasury.
