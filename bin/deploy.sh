#!/bin/bash
#set -x

## This script will deploy the we-services application.
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


## Deploy the bin scripts but rename them .orig so as not to overwrite custom changes.
cp "${WESERVICES_SOURCE}/bin/README.deploy" "${WESERVICES_HOME}/bin/"
cp "${WESERVICES_SOURCE}/bin/stop.sh" "${WESERVICES_HOME}/bin/stop.sh.orig"
cp "${WESERVICES_SOURCE}/bin/start.sh" "${WESERVICES_HOME}/bin/start.sh.orig"
cp "${WESERVICES_SOURCE}/bin/deploy.sh" "${WESERVICES_HOME}/bin/deploy.sh.orig"


## Deploy the most recent jar file.
WESERVICES_JAR=`ls -tr "${WESERVICES_SOURCE}/target" | grep '\<waste-exemplar-services.*jar\>' | tail -1`
echo "Copying ${WESERVICES_JAR} to ${WESERVICES_HOME}/webapps/"
cp "${WESERVICES_SOURCE}/target/${WESERVICES_JAR}" "${WESERVICES_HOME}/webapps/"


## Deploy the configuration file and set environment variables.
cp "${WESERVICES_SOURCE}/configuration.yml" "${WESERVICES_HOME}/conf/"
echo "Setting environment variables in ${WESERVICES_HOME}/conf/configuration.yml"
sed -i "s/WESERVICES_MQ_HOST/${WESERVICES_MQ_HOST}/g" "${WESERVICES_HOME}/conf/configuration.yml"
sed -i "s/WESERVICES_MQ_PORT/${WESERVICES_MQ_PORT}/g" "${WESERVICES_HOME}/conf/configuration.yml"
sed -i "s/WESERVICES_DB_HOST/${WESERVICES_DB_HOST}/g" "${WESERVICES_HOME}/conf/configuration.yml"
sed -i "s/WESERVICES_DB_PORT/${WESERVICES_DB_PORT}/g" "${WESERVICES_HOME}/conf/configuration.yml"
sed -i "s/WESERVICES_DB_NAME/${WESERVICES_DB_NAME}/g" "${WESERVICES_HOME}/conf/configuration.yml"
sed -i "s/WESERVICES_DB_USER/${WESERVICES_DB_USER}/g" "${WESERVICES_HOME}/conf/configuration.yml"
sed -i "s/WESERVICES_DB_PASSWD/${WESERVICES_DB_PASSWD}/g" "${WESERVICES_HOME}/conf/configuration.yml"


## Stop previously running we-services.
echo "Stopping old we-services."
if [ -f "${WESERVICES_HOME}/logs/pid" ]; then
  WESERVICES_PID=`cat "${WESERVICES_HOME}/logs/pid"`
  kill -0 ${WESERVICES_PID} > /dev/null 2>&1
  if [ $? -eq 0 ]; then
    kill ${WESERVICES_PID} > /dev/null 2>&1
  fi
  rm "${WESERVICES_HOME}/logs/pid"
fi
## A second check in case the pid file was out of date.
WESERVICES_PID=`ps -ef | grep java | grep ${WESERVICES_PORT} | awk '{print $2}'`
if [[ ! -z "${WESERVICES_PID}" ]]; then
  kill ${WESERVICES_PID} 
fi


## Start we-services.
echo "Starting we-services on port ${WESERVICES_PORT}."
cd "${WESERVICES_HOME}/logs"
if [ -f "${WESERVICES_HOME}/logs/nohup.out" ]; then
  mv nohup.out nohup.out.old
fi
nohup "${WESERVICES_JAVA_HOME}/bin/java" -Ddw.http.port=${WESERVICES_PORT} -jar "${WESERVICES_HOME}/webapps/${WESERVICES_JAR}" server "${WESERVICES_HOME}/conf/configuration.yml" &
echo $! > "${WESERVICES_HOME}/logs/pid"

echo "Deploy complete."
echo ""
exit 0

