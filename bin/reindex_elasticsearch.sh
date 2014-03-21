#!/bin/bash
#set -x

## Make it possible to use the public search against existing records in the database.
## wcrs-services must be running. These commands should be run on an app server.

## This script should only be started by the user wcrs-services.
USER=`/usr/bin/whoami`
if [ "$USER" != "wcrs-services" ]; then
    echo "ERROR: This script should only be run by the user wcrs-services."
    exit 1
fi

if [[ -z "${WCRS_SERVICES_DB_HOST}" ]]; then
  echo "ERROR: The environment variable WCRS_SERVICES_DB_HOST is undefined."
  echo ""
  exit 1
fi

## Display a little white space before we begin.
echo ""

echo "Create accurate XY coordinates in the database for each postcode provided."
curl -X POST http://localhost:9091/tasks/location
echo ""

echo "Delete existing Elastic search records. It may give an exception about"
echo "registrations not found, this should be safe to ignore."
echo "Note: This will throw an error the first time it's run because the index doesn't exist yet."
curl -X POST http://localhost:9091/tasks/indexer -d 'deleteAll'
echo ""

echo "Recreate the empty registrations index."
curl -XPOST http://$WCRS_SERVICES_DB_HOST:9200/registrations
echo ""

echo "Set a custom mapping for the registration object."
curl -XPUT $WCRS_SERVICES_DB_HOST:9200/registrations/registration/_mapping -d '{ registration : { properties : { accessCode : {type:"string"}, accountEmail : {type:"string"}, address : {type:"string"}, administrativeArea : {type:"string"}, businessType : {type:"string"}, companyName : {type:"string"}, contactEmail : {type:"string"}, declaration : {type:"string"}, dependentLocality : {type:"string"}, easting : {type:"string"}, firstName : {type:"string"}, houseNumber : {type:"string"}, id : {type:"string"}, individualsType : {type:"string"}, lastName : {type:"string"}, localAuthorityUpdateDate : {type:"string"}, location : {type:"geo_point"}, metaData : { properties : { anotherString : {type:"string"}, dateActivated : {type:"string"}, dateRegistered : {type:"string"}, distance : {type:"string"}, lastModified : {type:"string"}, revokedReason : {type:"string"}, route : {type:"string"}, status : {type:"string"} }}, northing : {type:"string"}, otherTitle : {type:"string"}, phoneNumber : {type:"string"}, position : {type:"string"}, postcode : {type:"string"}, publicBodyType : {type:"string"}, publicBodyTypeOther : {type:"string"}, regIdentifier : {type:"string"}, royalMailUpdateDate : {type:"string"}, streetLine1 : {type:"string"}, streetLine2 : {type:"string"}, streetLine3 : {type:"string"}, streetLine4 : {type:"string"}, title : {type:"string"}, townCity : {type:"string"}, uprn : {type:"string"}}}}'
echo ""

echo "Re-index the data from the database (without any parameters)"
curl -X POST http://localhost:9091/tasks/indexer
echo ""

echo "Re-indexing is now complete."
echo ""

exit 0

