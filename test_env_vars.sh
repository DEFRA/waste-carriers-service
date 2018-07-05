#!/bin/bash

# source test_env_vars.sh

export WCRS_TEST_REGSDB_URI="mongodb://mongoUser:password1234@localhost:27017/waste-carriers-test"
export WCRS_TEST_USERSDB_URI="mongodb://mongoUser:password1234@localhost:27017/waste-carriers-users-test"

export WCRS_TEST_MONGODB_SERVER_SEL_TIMEOUT=1000
