#!/usr/bin/env bash

## Make all registrations searchable via postcode, then drop and re-create the
## "registrations" index in ElasticSearch, then re-index all registrations in
## the Mongo database.

if [[ -z "${WCRS_SERVICES_ADMIN_PORT}" ]]; then
    echo "Environment variable WCRS_SERVICES_ADMIN_PORT is not set"
    exit 1
fi

if [[ ! -e "registration_mapping.json" ]]; then
    echo "Can't find registration_mapping.json"
    echo "Make sure you run this script from within the bin directory"
    exit 1
fi

# First, make records searchable via postcode.
echo "Adding post-code lookup information"
curl --ipv4 -X POST http://localhost:${WCRS_SERVICES_ADMIN_PORT}/tasks/location

# Then index all registrations in Elastic Search.
echo "Indexing registrations in Elastic Search"
curl --ipv4 -X POST http://localhost:${WCRS_SERVICES_ADMIN_PORT}/tasks/indexer -d '@registration_mapping.json'

exit 0
