#!/usr/bin/env bash

## Make all registrations searchable via postcode

if [[ -z "${WCRS_SERVICES_ADMIN_PORT}" ]]; then
    echo "Environment variable WCRS_SERVICES_ADMIN_PORT is not set"
    exit 1
fi

# Make records searchable via postcode.
echo "Adding post-code lookup information"
curl --ipv4 --silent -X POST http://localhost:${WCRS_SERVICES_ADMIN_PORT}/tasks/location

exit 0
