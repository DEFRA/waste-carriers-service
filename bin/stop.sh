#!/bin/bash
#set -x

## This script will stop the we-services application.
## Please refer to README.deploy for futher details.

function env_alert() {
    echo "Environment variable $1 is not set."
    echo "Refer to the README.deploy file for more details."
    echo "Please set the required environment variable and try again. Exiting now."
    echo ""
    exit 2
}

echo ""

## Ensure required environment variables have been set.
if [[ -z "${WCRS_SERVICES_JAVA_HOME}" ]]; then env_alert WCRS_SERVICES_JAVA_HOME; fi
if [[ -z "${WCRS_SERVICES_HOME}" ]]; then env_alert WCRS_SERVICES_HOME; fi
if [[ -z "${WCRS_SERVICES_PORT}" ]]; then env_alert WCRS_SERVICES_PORT; fi

## Ensure directory structure is in place.
if [ -d "${WCRS_SERVICES_HOME}" ]; then
  if [ ! -w "${WCRS_SERVICES_HOME}/live/logs" ]; then
    echo "ERROR: Unable to write to ${WCRS_SERVICES_HOME}/live/logs"
    echo "       Exiting now."
    echo ""
    exit 1
  fi
else
  echo "ERROR: ${WCRS_SERVICES_HOME} does not exist."
  exit 1
fi


## Stop previously running we-services.
echo "Stopping wcrs-services."
if [ -f "${WCRS_SERVICES_HOME}/live/logs/pid" ]; then
  WCRS_SERVICES_PID=`cat "${WCRS_SERVICES_HOME}/live/logs/pid"`
  kill -0 ${WCRS_SERVICES_PID} > /dev/null 2>&1
  if [ $? -eq 0 ]; then
    kill ${WCRS_SERVICES_PID} > /dev/null 2>&1
  fi
  rm "${WCRS_SERVICES_HOME}/live/logs/pid"
fi
## A second check in case the pid file was out of date.
WCRS_SERVICES_PID=`ps -ef | grep java | grep ${WCRS_SERVICES_PORT} | awk '{print $2}'`
if [[ ! -z "${WCRS_SERVICES_PID}" ]]; then
  kill ${WCRS_SERVICES_PID} 
fi

echo ""
exit 0

