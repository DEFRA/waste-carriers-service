#!/bin/bash

# source test_env_vars.sh

export WCRS_SERVICES_DB_HOST_TEST="localhost"
export WCRS_SERVICES_DB_PORT_TEST=27017
export WCRS_SERVICES_DB_NAME_TEST="waste-carriers-test"
export WCRS_SERVICES_DB_USER_TEST="mongoUser"
export WCRS_SERVICES_DB_PASSWD_TEST="password1234"

export WCRS_SERVICES_EM_HOST_TEST="localhost"
export WCRS_SERVICES_EM_PORT_TEST=27017
export WCRS_SERVICES_EM_NAME_TEST="entity-matching-test"
export WCRS_SERVICES_EM_USER_TEST="mongoUser"
export WCRS_SERVICES_EM_PASSWD_TEST="password1234"
