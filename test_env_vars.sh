#!/bin/bash

# source test_env_vars.sh

export WCRS_TEST_REGSDB_URL1="localhost:27017"
export WCRS_TEST_REGSDB_NAME=waste-carriers-test
export WCRS_TEST_REGSDB_USERNAME=mongoUser
export WCRS_TEST_REGSDB_PASSWORD=password1234
export WCRS_TEST_REGSDB_SERVER_SEL_TIMEOUT=1000

export WCRS_TEST_USERSDB_URL1="localhost:27017"
export WCRS_TEST_USERSDB_NAME=waste-carriers-users-test
export WCRS_TEST_USERSDB_USERNAME=mongoUser
export WCRS_TEST_USERSDB_PASSWORD=password1234
export WCRS_TEST_USERSDB_SERVER_SEL_TIMEOUT=1000
