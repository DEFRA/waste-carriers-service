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
if [[ -z "${WESERVICES_JAVA_HOME}" ]]; then env_alert WESERVICES_JAVA_HOME; fi
if [[ -z "${WESERVICES_HOME}" ]]; then env_alert WESERVICES_HOME; fi
if [[ -z "${WESERVICES_SOURCE}" ]]; then env_alert WESERVICES_SOURCE; fi
if [[ -z "${WESERVICES_PORT}" ]]; then env_alert WESERVICES_PORT; fi
if [[ -z "${WESERVICES_MQ_HOST}" ]]; then env_alert WESERVICES_MQ_HOST; fi
if [[ -z "${WESERVICES_MQ_PORT}" ]]; then env_alert WESERVICES_MQ_PORT; fi
if [[ -z "${WESERVICES_DB_HOST}" ]]; then env_alert WESERVICES_DB_HOST; fi
if [[ -z "${WESERVICES_DB_PORT}" ]]; then env_alert WESERVICES_DB_PORT; fi
if [[ -z "${WESERVICES_DB_NAME}" ]]; then env_alert WESERVICES_DB_NAME; fi
if [[ -z "${WESERVICES_DB_USER}" ]]; then env_alert WESERVICES_DB_USER; fi
if [[ -z "${WESERVICES_DB_PASSWD}" ]]; then env_alert WESERVICES_DB_PASSWD; fi

## Ensure directory structure is in place.
if [ -d "${WESERVICES_HOME}" ]; then
  if [ ! -w "${WESERVICES_HOME}/live/logs" ]; then
    echo "ERROR: Unable to write to ${WESERVICES_HOME}/live/logs"
    echo "       Exiting now."
    echo ""
    exit 1
  fi
else
  echo "ERROR: ${WESERVICES_HOME} does not exist."
  exit 1
fi


## Stop previously running we-services.
echo "Stopping we-services."
if [ -f "${WESERVICES_HOME}/live/logs/pid" ]; then
  WESERVICES_PID=`cat "${WESERVICES_HOME}/live/logs/pid"`
  kill -0 ${WESERVICES_PID} > /dev/null 2>&1
  if [ $? -eq 0 ]; then
    kill ${WESERVICES_PID} > /dev/null 2>&1
  fi
  rm "${WESERVICES_HOME}/live/logs/pid"
fi
## A second check in case the pid file was out of date.
WESERVICES_PID=`ps -ef | grep java | grep ${WESERVICES_PORT} | awk '{print $2}'`
if [[ ! -z "${WESERVICES_PID}" ]]; then
  kill ${WESERVICES_PID} 
fi

echo ""
exit 0

