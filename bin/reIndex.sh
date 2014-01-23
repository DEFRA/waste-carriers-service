#!/bin/bash
#set -x

## This script will re-index the wcrs-services application.
## -- More specifically it:
## 1. Update the database with location XY coordinates
## 2. Remove any existing elastic search indexes and replace them
##    with clean index data and structure derived from the format
##    in the XXX file.
## Please refer to README.deploy for further details.

echo "Start Re-Index"

esHost=${WCRS_SERVICES_ES_HOST}
if [ $esHost ]
then
  echo "String length not zero"
else
  echo "Using local setting for WCRS_SERVICES_ES_HOST"
  esHost="localhost"
fi
echo "esHost: $esHost"
esPort="9200"
echo "TODO esPort: No ENV var set, using default 9200"

wcrsServicesName="localhost"
echo "TODO wcrsServicesName: No ENV var set, using default localhost"
wcrsServicesPort=${WCRS_SERVICES_PORT}
if [ $wcrsServicesPort ]
then
  echo "String length not zero"
else
  echo "Using local setting for WCRS_SERVICES_PORT"
  wcrsServicesPort="9091"
fi
echo "wcrsServicesPort: $wcrsServicesPort"

echo "Generate XY Coordinates"
curl -X POST http://$wcrsServicesName:$wcrsServicesPort/tasks/location

echo "Delete all Elastic Search indexes"
curl -X POST http://$wcrsServicesName:$wcrsServicesPort/tasks/indexer -d 'deleteAll'

echo "Create a clean registrations index"
curl -X POST http://$esHost:$esPort/registrations

echo "Set the index structure in Elastic Search"
curl -X PUT http://$esHost:$esPort/registrations/registration/_mapping -d '@elasticSearchIndex.json'

echo "Re-populate the elastic search index"
curl -X POST http://$wcrsServicesName:$wcrsServicesPort/tasks/indexer

echo "End Re-Index"


 
