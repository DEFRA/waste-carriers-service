#!/bin/bash
#set -x

## This script will start the wcrs-services application.
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
    echo "ERROR: Unable to write to ${WCRS_SERVICES_HOME}/live/logs. Exiting now."
    exit 1
  fi
else
  echo "ERROR: ${WCRS_SERVICES_HOME} does not exist."
  exit 1
fi


## Verify that we-services isn't currently running.
WESERVICES_PID=`ps -ef | grep java | grep ${WCRS_SERVICES_PORT} | awk '{print $2}'`
if [[ ! -z "${WCRS_SERVICES_PID}" ]]; then
  echo "wcrs-services is already running or some other daemon is already using port ${WCRS_SERVICES_PORT}."
  echo ""
  exit 0
fi


## Use the jar file with the most recent timestamp.
WESERVICES_JAR=`ls -tr "${WCRS_SERVICES_HOME}/live/webapps/" | grep '\<waste-exemplar-services.*jar\>' | tail -1`


## Start we-services.
echo "Starting wcrs-services on port ${WCRS_SERVICES_PORT}."
cd "${WCRS_SERVICES_HOME}/live/logs"
if [ -f "${WCRS_SERVICES_HOME}/live/logs/nohup.out" ]; then
  DATESTAMP=`date +%Y.%m.%d-%H.%M`
  mv nohup.out nohup.out.${DATESTAMP}
fi
nohup "${WCRS_SERVICES_JAVA_HOME}/bin/java" -Ddw.http.port=${WCRS_SERVICES_PORT} -jar "${WCRS_SERVICES_HOME}/live/webapps/${WCRS_SERVICES_JAR}" server "${WCRS_SERVICES_HOME}/live/conf/configuration.yml" &
echo $! > "${WCRS_SERVICES_HOME}/live/logs/pid"

echo ""
exit 0

