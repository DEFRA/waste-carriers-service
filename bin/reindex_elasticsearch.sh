#!/usr/bin/env bash

## Drop and re-create the "registrations" index in ElasticSearch, then re-index
## all registrations in the Mongo database.

if [[ -z "${WCRS_SERVICES_ADMIN_PORT}" ]]; then
    echo "Environment variable WCRS_SERVICES_ADMIN_PORT is not set"
    exit 1
fi

if [[ ! -e "registration_mapping.json" ]]; then
    echo "Can't find registration_mapping.json"
    echo "Make sure you run this script from within the bin directory"
    exit 1
fi

curl --ipv4 -X POST http://localhost:${WCRS_SERVICES_ADMIN_PORT}/tasks/indexer -d '@registration_mapping.json'

exit 0
