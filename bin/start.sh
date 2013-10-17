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
  if [ -w "${WESERVICES_HOME}" ]; then
    for DIR in bin conf logs webapps; do
      if [ ! -d "${WESERVICES_HOME}/${DIR}" ]; then
        echo "Creating directory: ${WESERVICES_HOME}/${DIR}" 
        mkdir "${WESERVICES_HOME}/${DIR}" 
      fi
    done
  else
    echo "ERROR: Unable to write to ${WESERVICES_HOME}"
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
WESERVICES_JAR=`ls -tr "${WESERVICES_HOME}/webapps/" | grep '\<waste-exemplar-services.*jar\>' | tail -1`


## Start we-services.
echo "Starting we-services on port ${WESERVICES_PORT}."
cd "${WESERVICES_HOME}/logs"
if [ -f "${WESERVICES_HOME}/logs/nohup.out" ]; then
  mv nohup.out nohup.out.old
fi
nohup "${WESERVICES_JAVA_HOME}/bin/java" -Ddw.http.port=${WESERVICES_PORT} -jar "${WESERVICES_HOME}/webapps/${WESERVICES_JAR}" server "${WESERVICES_HOME}/conf/configuration.yml" &
echo $! > "${WESERVICES_HOME}/logs/pid"

echo ""
exit 0

