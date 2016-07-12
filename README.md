
#waste-carriers-service

Waste Carriers Registration Service application.

The Waste Carrier Registrations Service allows businesses, who deal with waste and thus have to register according to the regulations, to register online. Once registered, businesses can sign in again to edit their registrations if needed.

The service also allows authorised agency users and NCCC contact centre staff to create and manage registrations on other users behalf, e.g. to support 'Assisted Digital' registrations. The service provides an internal user account management facility which allows authorised administrators to create and manage other agency user accounts.

The service is implemented as a frontend web application, with a service API and a document-oriented database (MongoDB) underneath.

This application and its associated Maven and Eclipse project implement the service layer and the associated RESTful services API.

##Installation


Clone the repository, copying the project into a working directory:

	$ git clone https://github.com/EnvironmentAgency/waste-carriers-service.git


##Prerequisites


* Git
* Access to GitHub
* Java 7 JDK - for building the services layer
* Maven (version 3.0 or above) - for building the services layer
* MongoDB (version 2.4.6 or above) - to store registrations and user accounts
* ElasticSearch (version 0.90.5 or above) - for full-text search

##Dependents

* The waste-carriers-frontend application, which is implemented in Ruby on Rails, is the client of the services exposed by this application.

##Configuration

The service uses a Dropwizard configuration file (configuration.yml) which in turn refers to environment variables.
You may want or need to set the following environment variables, e.g. in your ~/.bash_profile (if you are a Mac or Linux user):

export WCRS_SERVICES_DB_HOST="localhost"
export WCRS_SERVICES_DB_PORT=27017
export WCRS_SERVICES_DB_NAME="waste-carriers"
export WCRS_SERVICES_DB_USER="mongoUser"
export WCRS_SERVICES_DB_PASSWD="<your-mongo-password>"

Alternatively, you can create another local configuration file with your values in it, and refer to this file when starting up the service.

##Build and Deploy

Using Maven:

	$ mvn clean package


##Startup

Start the service by providing the name of the jar file, the command 'server', and the name of the configuration file.
You can also override parameters such as https port numbers using the Java '-D' option. Example:

    $ java -Ddw.http.port=9090 -Ddw.http.adminPort=9091 -jar target/waste-exemplar-services-1.1.2.jar server configuration_gmueller.yml

For more details on how to start a Dropwizard service and configuration and startup options, please see the Dropwizard documentation.

Once the application server is started you should be able to access the services application in your browser on

	http://localhost:9090/registrations.json


##Setting up prerequisites

###Setting up the MongoDB database

The service uses a MongoDB database to be set up with authentication. Please see See http://docs.mongodb.org/manual/tutorial/enable-authentication/ for details.

To set up a Mongo administrator, follow http://docs.mongodb.org/manual/tutorial/add-user-administrator/.

Start MongoDB with authentication:

	$ mongod --auth

###Setting up ElasticSearch

The service uses an ElasticSearch database for complex and flexible search operations.

Startup ElasticSearch (0.90.5) for local development:

	$ bin/elasticsearch -f

Note: more recent versions of ElasticSearch are started without the '-f' parameter.

However, always make sure that when the ElasticSearch server version is upgraded, also upgrade the client libraries
as well to use the same version.



##Run Tests

See JUnit tests in src/test/java (more to follow in phase 2).

##Postcode data

The service uses postcode data with associated latitude and longitude coordinates, uploaded from a postcodes CSV file available here:
http://www.freemaptools.com/download/outcode-postcodes/postcodes.csv

Contains Ordnance Survey data © Crown copyright and database right 2013

Contains Royal Mail data © Royal Mail copyright and database right 2013

Contains National Statistics data © Crown copyright and database right 2013


##Related Resources

Ruby on Rails: http://rubyonrails.org

MongoDB: http://www.mongodb.org

ElasticSearch: http://www.elasticsearch.org

Apache Maven: http://maven.apache.org/


##License

MIT License
