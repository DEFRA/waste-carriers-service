
#waste-exemplar-services

Waste Carriers Registration Services application.

The Waste Carrier Registrations Service allows businesses, who deal with waste and thus have to register according to the regulations, to register online. Once registered, businesses can sign in again to edit their registrations if needed.

The service also allows authorised agency users and NCCC contact centre staff to create and manage registrations on other users behalf, e.g. to support 'Assisted Digital' registrations. The service provides an internal user account management facility which allows authorised administrators to create and manage other agency user accounts.

The service is implemented as a frontend web application, with a service API and a document-oriented database (MongoDB) underneath.

This application and its associated Maven and Eclipse project implement the service layer and the associated RESTful services API.

##Installation


Clone the repository, copying the project into a working directory:

	$ git clone https://github.com/EnvironmentAgency/waste-exemplar-services.git


##Prerequisites


* Git
* Access to GitHub
* Java 7 JDK - for building the services layer
* Maven (version 3.0 or above) - for building the services layer
* MongoDB (version 2.4.6 or above) - to store registrations and user accounts
* ElasticSearch (version 0.90.5 or above) - for full-text search

##Dependencies

* The waste-exemplar-frontend application, which is implemented in Ruby on Rails, is the client of the services exposed by this application. 

##Configuration

You may want or need to set the following environment variables, e.g. in your ~/.bash_profile (if you are a Mac or Linux user):

TODO - list environment variables used by services

	export WCRS_FRONTEND_WCRS_SERVICES_URL="http://localhost:9090"
	export WCRS_FRONTEND_PUBLIC_APP_DOMAIN="www.local.wastecarriersregistration.service.gov.uk"
	export WCRS_FRONTEND_ADMIN_APP_DOMAIN="admin.local.wastecarriersregistration.service.gov.uk"


##Build and Deploy

Using Maven...

mvn clean package


##Startup
Once the application server is started you should be able to access the services application in your browser on

	http://localhost:9090/registrations.json

TODO - Healthchecks

##Setting up prerequisites

###Setting up the MongoDB database

TODO - Set up MongoDB database users and database(s) for local development

Start MongoDB with authentication:

mongod --auth

###Setting up ElasticSearch

TODO

Startup ElasticSearch for local development:

##User Guide

TODO - document the API using Swagger or similar

##Run Tests

TODO

##Related Resources

Ruby on Rails: http://rubyonrails.org

MongoDB: http://www.mongodb.org

ElasticSearch: http://www.elasticsearch.org

Apache Maven: http://www.elasticsearch.org


##License

TBD
