#!/bin/bash
#set -x

## This script will deploy and start the we-services application.
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
if [[ -z "${WESERVICES_ADMIN_PORT}" ]]; then env_alert WESERVICES_ADMIN_PORT; fi
if [[ -z "${WESERVICES_MQ_HOST}" ]]; then env_alert WESERVICES_MQ_HOST; fi
if [[ -z "${WESERVICES_MQ_PORT}" ]]; then env_alert WESERVICES_MQ_PORT; fi
if [[ -z "${WESERVICES_DB_HOST}" ]]; then env_alert WESERVICES_DB_HOST; fi
if [[ -z "${WESERVICES_DB_PORT}" ]]; then env_alert WESERVICES_DB_PORT; fi
if [[ -z "${WESERVICES_DB_NAME}" ]]; then env_alert WESERVICES_DB_NAME; fi
if [[ -z "${WESERVICES_DB_USER}" ]]; then env_alert WESERVICES_DB_USER; fi
if [[ -z "${WESERVICES_DB_PASSWD}" ]]; then env_alert WESERVICES_DB_PASSWD; fi


## Stop previously running we-services.
echo "Stopping old we-services."
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


## Create a new release directory.
DATESTAMP=`date +%Y.%m.%d-%H.%M`
RELEASE_DIR="we-services-${DATESTAMP}"
echo "Creating new release directory ${RELEASE_DIR}"
mkdir "${WESERVICES_HOME}/${RELEASE_DIR}"
cd "${WESERVICES_HOME}"
if [ -d "${WESERVICES_HOME}/live" ]; then
  rm live
fi
ln -s "${RELEASE_DIR}" live


## Create sub-directories to deploy into.
if [ -d "${WESERVICES_HOME}" ]; then
  if [ -w "${WESERVICES_HOME}" ]; then
    for DIR in bin conf logs webapps; do
      if [ ! -d "${WESERVICES_HOME}/${RELEASE_DIR}/${DIR}" ]; then
        echo "Creating directory: ${WESERVICES_HOME}/${RELEASE_DIR}/${DIR}" 
        mkdir "${WESERVICES_HOME}/${RELEASE_DIR}/${DIR}" 
      fi
    done
  else
    echo "ERROR: Unable to write to ${WESERVICES_HOME}. Exiting now."
    exit 1
  fi
else
  echo "ERROR: ${WESERVICES_HOME} does not exist."
  exit 1
fi


## Deploy the bin scripts.
if [ ! -d "${WESERVICES_SOURCE}/bin" ]; then
  echo "ERROR: Unable to locate ${WESERVICES_SOURCE}/bin"
  echo "       Exiting now."
  echo ""
  exit 1
fi
cp "${WESERVICES_SOURCE}/bin/README.deploy" "${WESERVICES_HOME}/${RELEASE_DIR}/bin/"
cp "${WESERVICES_SOURCE}/bin/stop.sh" "${WESERVICES_HOME}/${RELEASE_DIR}/bin/stop.sh"
chmod 744 "${WESERVICES_HOME}/${RELEASE_DIR}/bin/stop.sh"
cp "${WESERVICES_SOURCE}/bin/start.sh" "${WESERVICES_HOME}/${RELEASE_DIR}/bin/start.sh"
chmod 744 "${WESERVICES_HOME}/${RELEASE_DIR}/bin/start.sh"
cp "${WESERVICES_SOURCE}/bin/deploy.sh" "${WESERVICES_HOME}/${RELEASE_DIR}/bin/deploy.sh"
chmod 744 "${WESERVICES_HOME}/${RELEASE_DIR}/bin/deploy.sh"


## Deploy the most recent jar file.
WESERVICES_JAR=`ls -tr "${WESERVICES_SOURCE}/target" | grep '\<waste-exemplar-services.*jar\>' | tail -1`
if [[ -z "${WESERVICES_JAR}" ]]; then
  echo "ERROR: Unable to locate waste-exemplar-services jar file in ${WESERVICES_SOURCE}/target"
  echo "       Exiting now."
  echo ""
  exit 1
fi
echo "Copying ${WESERVICES_JAR} to ${WESERVICES_HOME}/${RELEASE_DIR}/webapps/"
cp "${WESERVICES_SOURCE}/target/${WESERVICES_JAR}" "${WESERVICES_HOME}/${RELEASE_DIR}/webapps/"


## Deploy the configuration file and set environment variables.
if [[ ! -f "${WESERVICES_SOURCE}/configuration.yml" ]]; then
  echo "ERROR: Unable to locate ${WESERVICES_SOURCE}/configuration.yml"
  echo "       Exiting now."
  echo ""
  exit 1
fi
cp "${WESERVICES_SOURCE}/configuration.yml" "${WESERVICES_HOME}/${RELEASE_DIR}/conf/"
echo "Setting environment variables in ${WESERVICES_HOME}/${RELEASE_DIR}/conf/configuration.yml"
sed -i "s/WESERVICES_MQ_HOST/${WESERVICES_MQ_HOST}/g" \
       "${WESERVICES_HOME}/${RELEASE_DIR}/conf/configuration.yml"
sed -i "s/WESERVICES_MQ_PORT/${WESERVICES_MQ_PORT}/g" \
       "${WESERVICES_HOME}/${RELEASE_DIR}/conf/configuration.yml"
sed -i "s/WESERVICES_DB_HOST/${WESERVICES_DB_HOST}/g" \
       "${WESERVICES_HOME}/${RELEASE_DIR}/conf/configuration.yml"
sed -i "s/WESERVICES_DB_PORT/${WESERVICES_DB_PORT}/g" \
       "${WESERVICES_HOME}/${RELEASE_DIR}/conf/configuration.yml"
sed -i "s/WESERVICES_DB_NAME/${WESERVICES_DB_NAME}/g" \
       "${WESERVICES_HOME}/${RELEASE_DIR}/conf/configuration.yml"
sed -i "s/WESERVICES_DB_USER/${WESERVICES_DB_USER}/g" \
       "${WESERVICES_HOME}/${RELEASE_DIR}/conf/configuration.yml"
sed -i "s/WESERVICES_DB_PASSWD/${WESERVICES_DB_PASSWD}/g" \
       "${WESERVICES_HOME}/${RELEASE_DIR}/conf/configuration.yml"


## Create live symlink.
echo "Creating symlink: ${WESERVICES_HOME}/live"
cd "${WESERVICES_HOME}"
if [ -d "${WESERVICES_HOME}/live" ]; then
  rm live
fi
ln -s "${RELEASE_DIR}" live


## Start we-services.
echo "Starting we-services on port ${WESERVICES_PORT}."
cd "${WESERVICES_HOME}/live/logs"
if [ -f "${WESERVICES_HOME}/live/logs/we-services.log" ]; then
  mv we-services.log we-services.log.${DATESTAMP}
fi
nohup "${WESERVICES_JAVA_HOME}/bin/java" -Ddw.http.port=${WESERVICES_PORT} \
      -jar "${WESERVICES_HOME}/live/webapps/${WESERVICES_JAR}" \
      server "${WESERVICES_HOME}/live/conf/configuration.yml" > "${WESERVICES_HOME}/live/logs/we-services.log" &
echo $! > "${WESERVICES_HOME}/live/logs/pid"


echo "Deploy complete."
echo ""
exit 0

