#!/bin/bash
#set -x

## This script will start the we-services application.
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
if [[ -z "${WESERVICES_PORT}" ]]; then env_alert WESERVICES_PORT; fi

## Ensure directory structure is in place.
if [ -d "${WESERVICES_HOME}" ]; then
  if [ ! -w "${WESERVICES_HOME}/live/logs" ]; then
    echo "ERROR: Unable to write to ${WESERVICES_HOME}/live/logs. Exiting now."
    exit 1
  fi
else
  echo "ERROR: ${WESERVICES_HOME} does not exist."
  exit 1
fi


## Verify that we-services isn't currently running.
WESERVICES_PID=`ps -ef | grep java | grep ${WESERVICES_PORT} | awk '{print $2}'`
if [[ ! -z "${WESERVICES_PID}" ]]; then
  echo "we-services is already running or some other daemon is already using port ${WESERVICES_PORT}."
  echo ""
  exit 0
fi


## Use the jar file with the most recent timestamp.
WESERVICES_JAR=`ls -tr "${WESERVICES_HOME}/live/webapps/" | grep '\<waste-exemplar-services.*jar\>' | tail -1`


## Start we-services.
echo "Starting we-services on port ${WESERVICES_PORT}."
cd "${WESERVICES_HOME}/live/logs"
if [ -f "${WESERVICES_HOME}/live/logs/nohup.out" ]; then
  DATESTAMP=`date +%Y.%m.%d-%H.%M`
  mv nohup.out nohup.out.${DATESTAMP}
fi
nohup "${WESERVICES_JAVA_HOME}/bin/java" -Ddw.http.port=${WESERVICES_PORT} -jar "${WESERVICES_HOME}/live/webapps/${WESERVICES_JAR}" server "${WESERVICES_HOME}/live/conf/configuration.yml" &
echo $! > "${WESERVICES_HOME}/live/logs/pid"

echo ""
exit 0

