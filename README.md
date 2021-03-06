# Waste Carriers Service

> As of 27 October 2020, this repo is no longer in use and is no longer being maintained. See [waste-carriers-engine](https://github.com/DEFRA/waste-carriers-engine/) and its host apps instead.

[![Build Status](https://travis-ci.com/DEFRA/waste-carriers-service.svg?branch=main)](https://travis-ci.com/DEFRA/waste-carriers-service)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=DEFRA_waste-carriers-service&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=DEFRA_waste-carriers-service)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=DEFRA_waste-carriers-service&metric=coverage)](https://sonarcloud.io/dashboard?id=DEFRA_waste-carriers-service)
[![Licence](https://img.shields.io/badge/Licence-OGLv3-blue.svg)](http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3)

Waste Carriers Registration Service application.

The Waste Carrier Registrations Service allows businesses, who deal with waste and thus have to register according to the regulations, to register online. Once registered, businesses can sign in again to edit their registrations if needed.

The service also allows authorised agency users and NCCC contact centre staff to create and manage registrations on other users behalf, e.g. to support 'Assisted Digital' registrations. The service provides an internal user account management facility which allows authorised administrators to create and manage other agency user accounts.

The service is implemented as a frontend web application, with a service API and a document-oriented database (MongoDB) underneath.

This application and its associated Maven and Eclipse project implement the service layer and the associated RESTful services API.

## Prerequisites

- Java 8 JDK - for building the services layer
- [MongoDB](http://www.mongodb.org) (version 3.6) - to store registrations and user accounts

## Installation

Clone the repository, copying the project into a working directory:

```bash
git clone https://github.com/DEFRA/waste-carriers-service.git
```

## Dependents

The waste-carriers-frontend application, which is implemented in Ruby on Rails, is the client of the services exposed by this application.

## Configuration

The service uses a Dropwizard configuration file (configuration.yml) which in turn refers to environment variables.

You'll need to set the following environment variables before using it. For example

```bash
export WCRS_SERVICES_AIRBRAKE_PROJECT_KEY="d03r78y5372a11111111111"
export WCRS_SERVICES_AIRBRAKE_ENVNAME="pre-production"
```

Other values in the configuration file have a default, however you can override them by setting the environment variable specified.

Alternatively, you can create another local configuration file with your values in it, and refer to this file when starting up the service.

```bash
java -jar target/waste-carriers-service.jar server my_configuration.yml
```

## Build

The project uses [Maven](https://maven.apache.org/) as its build tool, and [Maven Wrapper](https://github.com/takari/maven-wrapper) to handle getting a version of Maven on your machine to build the project.

So to build the project call

```bash
./mvnw -B -Dmaven.test.skip=true -T 1C clean package
```

N.B. If maven was installed all on the machine you would swap `./mvnw` with `mvn`.

## Startup

Start the service by providing the name of the jar file, the command 'server', and the name of the configuration file.

```bash
java -jar target/waste-carriers-service.jar server  configuration.yml
```

You can also override parameters such as https port numbers using the Java '-D' option.

```bash
java -Ddw.http.port=8003 -Ddw.http.adminPort=8004 -jar target/waste-carriers-service.jar server my_configuration.yml
```

For more details on how to start a Dropwizard service and configuration and startup options, please see the Dropwizard documentation.

Once the application server is started you should be able to access the services application in your browser on <http://localhost:8003/registrations.json>

## Run Tests

The project doesn't have an extensive suite of unit tests, but is working to improve its test coverage. They require a working connection to MongoDb, and specific test environment variables existing for them to run. Before running the tests you'll need to have sourced the environment variables. This is because the unit tests can't read from the config file so key details about MongoDb need to be provided as environment variables.

```bash
source test_env_vars.sh
```

Once you've run the environment variable script, you won't need to do it again until you open a new terminal session.

Hence the command to build above includes the option to skip tests. Instead we advise those working on the project should manually run the tests as and when required using

```bash
./mvnw test
```

## Contributing to this project

If you have an idea you'd like to contribute please log an issue.

All contributions should be submitted via a pull request.

## License

THIS INFORMATION IS LICENSED UNDER THE CONDITIONS OF THE OPEN GOVERNMENT LICENCE found at:

<http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3>

The following attribution statement MUST be cited in your products and applications when using this information.

>Contains public sector information licensed under the Open Government license v3

### About the license

The Open Government Licence (OGL) was developed by the Controller of Her Majesty's Stationery Office (HMSO) to enable information providers in the public sector to license the use and re-use of their information under a common open licence.

It is designed to encourage use and re-use of information freely and flexibly, with only a few conditions.
