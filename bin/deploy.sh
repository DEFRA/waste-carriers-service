#!/bin/bash
#set -x

## This script will deploy and start the wcrs-services application.
## Please refer to README.deploy for futher details.

## This script should only be started by the user wcrs-services.
USER=`/usr/bin/whoami`
if [ "$USER" != "wcrs-services" ]; then
    echo "ERROR: This script should only be run by the user wcrs-services."
    exit 1
fi

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
if [[ -z "${WCRS_SERVICES_SOURCE}" ]]; then env_alert WCRS_SERVICES_SOURCE; fi
if [[ -z "${WCRS_SERVICES_PORT}" ]]; then env_alert WCRS_SERVICES_PORT; fi
if [[ -z "${WCRS_SERVICES_ADMIN_PORT}" ]]; then env_alert WCRS_SERVICES_ADMIN_PORT; fi
if [[ -z "${WCRS_SERVICES_MQ_HOST}" ]]; then env_alert WCRS_SERVICES_MQ_HOST; fi
if [[ -z "${WCRS_SERVICES_MQ_PORT}" ]]; then env_alert WCRS_SERVICES_MQ_PORT; fi
if [[ -z "${WCRS_REGSDB_HOST}" ]]; then env_alert WCRS_REGSDB_HOST; fi
if [[ -z "${WCRS_REGSDB_PORT1}" ]]; then env_alert WCRS_REGSDB_PORT1; fi
if [[ -z "${WCRS_REGSDB_NAME}" ]]; then env_alert WCRS_REGSDB_NAME; fi
if [[ -z "${WCRS_REGSDB_USERNAME}" ]]; then env_alert WCRS_REGSDB_USERNAME; fi
if [[ -z "${WCRS_REGSDB_PASSWORD}" ]]; then env_alert WCRS_REGSDB_PASSWORD; fi
if [[ -z "${WCRS_USERSDB_HOST}" ]]; then env_alert WCRS_USERSDB_HOST; fi
if [[ -z "${WCRS_USERSDB_PORT1}" ]]; then env_alert WCRS_USERSDB_PORT1; fi
if [[ -z "${WCRS_USERSDB_NAME}" ]]; then env_alert WCRS_USERSDB_NAME; fi
if [[ -z "${WCRS_USERSDB_USERNAME}" ]]; then env_alert WCRS_SERVICES_USERSDB_USER; fi
if [[ -z "${WCRS_USERSDB_PASSWORD}" ]]; then env_alert WCRS_SERVICES_USERSDB_PASSWD; fi
if [[ -z "${WCRS_ELASDB_HOST}" ]]; then env_alert WCRS_ELASDB_HOST; fi
if [[ -z "${WCRS_ELASDB_PORT_JAVA}" ]]; then env_alert WCRS_ELASDB_PORT_JAVA; fi

if [[ -z "${WCRS_SERVICES_IR_FOLDERPATH}" ]]; then env_alert WCRS_SERVICES_IR_FOLDERPATH; fi


## Stop previously running wcrs-services.
echo "Stopping old wcrs-services."
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


## Create a new release directory.
if [ -f ${WCRS_SERVICES_SOURCE}/jenkins_build_number ]; then
  JENKINS_BUILD_NUMBER=`cat ${WCRS_SERVICES_SOURCE}/jenkins_build_number`
else
  JENKINS_BUILD_NUMBER="j"
fi
DATESTAMP=`date +%Y.%m.%d-%H.%M`
RELEASE_DIR="wcrs-services-${JENKINS_BUILD_NUMBER}-${DATESTAMP}"
echo "Creating new release directory ${RELEASE_DIR}"
mkdir "${WCRS_SERVICES_HOME}/${RELEASE_DIR}"
cd "${WCRS_SERVICES_HOME}"
if [ -d "${WCRS_SERVICES_HOME}/live" ]; then
  rm live
fi
ln -s "${RELEASE_DIR}" live


## Create sub-directories to deploy into.
if [ -d "${WCRS_SERVICES_HOME}" ]; then
  if [ -w "${WCRS_SERVICES_HOME}" ]; then
    for DIR in bin conf logs webapps; do
      if [ ! -d "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/${DIR}" ]; then
        echo "Creating directory: ${WCRS_SERVICES_HOME}/${RELEASE_DIR}/${DIR}" 
        mkdir "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/${DIR}" 
      fi
    done
  else
    echo "ERROR: Unable to write to ${WCRS_SERVICES_HOME}. Exiting now."
    exit 1
  fi
else
  echo "ERROR: ${WCRS_SERVICES_HOME} does not exist."
  exit 1
fi


## Deploy the bin scripts.
if [ ! -d "${WCRS_SERVICES_SOURCE}/bin" ]; then
  echo "ERROR: Unable to locate ${WCRS_SERVICES_SOURCE}/bin"
  echo "       Exiting now."
  echo ""
  exit 1
fi
cp "${WCRS_SERVICES_SOURCE}/bin/README.deploy" "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/bin/"
cp "${WCRS_SERVICES_SOURCE}/bin/stop.sh" "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/bin/stop.sh"
chmod 744 "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/bin/stop.sh"
cp "${WCRS_SERVICES_SOURCE}/bin/start.sh" "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/bin/start.sh"
chmod 744 "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/bin/start.sh"
cp "${WCRS_SERVICES_SOURCE}/bin/deploy.sh" "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/bin/deploy.sh"
chmod 744 "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/bin/deploy.sh"
cp "${WCRS_SERVICES_SOURCE}/bin/reindex_elasticsearch.sh" "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/bin/"
chmod 744 "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/bin/reindex_elasticsearch.sh"
cp "${WCRS_SERVICES_SOURCE}/bin/registration_mapping.json" "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/bin/"
chmod 744 "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/bin/registration_mapping.json"


## Deploy the most recent jar file.
WCRS_SERVICES_JAR=`ls ${WCRS_SERVICES_SOURCE}/target/waste-exemplar-services-*.jar | sort | tail -1`
WCRS_SERVICES_JAR=$(basename ${WCRS_SERVICES_JAR})
if [[ -z "${WCRS_SERVICES_JAR}" ]]; then
  echo "ERROR: Unable to locate waste-exemplar-services jar file in ${WCRS_SERVICES_SOURCE}/target"
  echo "       Exiting now."
  echo ""
  exit 1
fi
echo "Copying ${WCRS_SERVICES_JAR} to ${WCRS_SERVICES_HOME}/${RELEASE_DIR}/webapps/"
cp "${WCRS_SERVICES_SOURCE}/target/${WCRS_SERVICES_JAR}" "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/webapps/"


## Deploy the configuration file and set environment variables.
## Keep a copy of the original config, before variable names have been changed.
if [[ ! -f "${WCRS_SERVICES_SOURCE}/configuration.yml" ]]; then
  echo "ERROR: Unable to locate ${WCRS_SERVICES_SOURCE}/configuration.yml"
  echo "       Exiting now."
  echo ""
  exit 1
fi
cp "${WCRS_SERVICES_SOURCE}/configuration.yml" "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/conf/configuration.yml.orig"
cp "${WCRS_SERVICES_SOURCE}/configuration.yml" "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/conf/"
chmod 600 "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/conf/configuration.yml"
echo "Setting environment variables in ${WCRS_SERVICES_HOME}/${RELEASE_DIR}/conf/configuration.yml"
sed -i "s/WCRS_SERVICES_PORT/${WCRS_SERVICES_PORT}/g" \
       "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/conf/configuration.yml"
sed -i "s/WCRS_SERVICES_ADMIN_PORT/${WCRS_SERVICES_ADMIN_PORT}/g" \
       "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/conf/configuration.yml"
sed -i "s/WCRS_SERVICES_MQ_HOST/${WCRS_SERVICES_MQ_HOST}/g" \
       "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/conf/configuration.yml"
sed -i "s/WCRS_SERVICES_MQ_PORT/${WCRS_SERVICES_MQ_PORT}/g" \
       "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/conf/configuration.yml"
sed -i "s/WCRS_REGSDB_HOST/${WCRS_REGSDB_HOST}/g" \
       "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/conf/configuration.yml"
sed -i "s/WCRS_REGSDB_PORT1/${WCRS_REGSDB_PORT1}/g" \
       "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/conf/configuration.yml"
sed -i "s/WCRS_REGSDB_NAME/${WCRS_REGSDB_NAME}/g" \
       "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/conf/configuration.yml"
sed -i "s/WCRS_REGSDB_USERNAME/${WCRS_REGSDB_USERNAME}/g" \
       "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/conf/configuration.yml"
sed -i "s/WCRS_REGSDB_PASSWORD/${WCRS_REGSDB_PASSWORD}/g" \
       "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/conf/configuration.yml"
sed -i "s/WCRS_USERSDB_HOST/${WCRS_USERSDB_HOST}/g" \
       "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/conf/configuration.yml"
sed -i "s/WCRS_USERSDB_PORT1/${WCRS_USERSDB_PORT1}/g" \
       "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/conf/configuration.yml"
sed -i "s/WCRS_USERSDB_NAME/${WCRS_USERSDB_NAME}/g" \
       "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/conf/configuration.yml"
sed -i "s/WCRS_USERSDB_USERNAME/${WCRS_USERSDB_USERNAME}/g" \
       "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/conf/configuration.yml"
sed -i "s/WCRS_USERSDB_PASSWORD/${WCRS_USERSDB_PASSWORD}/g"
sed -i "s/WCRS_ELASDB_HOST/${WCRS_ELASDB_HOST}/g" \
       "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/conf/configuration.yml"
sed -i "s/WCRS_ELASDB_PORT_JAVA/${WCRS_ELASDB_PORT_JAVA}/g" \
       "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/conf/configuration.yml"
sed -i "s/WCRS_SERVICES_EXPIRES_AFTER/${WCRS_SERVICES_EXPIRES_AFTER}/g" \
       "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/conf/configuration.yml"
sed -i "s/WCRS_SERVICES_RENEWAL_WINDOW/${WCRS_SERVICES_RENEWAL_WINDOW}/g" \
       "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/conf/configuration.yml"
sed -i "s|WCRS_SERVICES_IR_FOLDERPATH|${WCRS_SERVICES_IR_FOLDERPATH}|g" \
       "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/conf/configuration.yml"


## Preserve the jenkins build number file.
if [ -f ${WCRS_SERVICES_SOURCE}/jenkins_build_number ]; then
  cp "${WCRS_SERVICES_SOURCE}/jenkins_build_number" "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/conf/"
fi

## Preserve the license and readme files.
cp "${WCRS_SERVICES_SOURCE}/LICENSE" "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/" 
cp "${WCRS_SERVICES_SOURCE}/README.md" "${WCRS_SERVICES_HOME}/${RELEASE_DIR}/" 

## Create live symlink.
echo "Creating symlink: ${WCRS_SERVICES_HOME}/live"
cd "${WCRS_SERVICES_HOME}"
if [ -d "${WCRS_SERVICES_HOME}/live" ]; then
  rm live
fi
ln -s "${RELEASE_DIR}" live

## Start wcrs-services.
echo "Starting wcrs-services on port ${WCRS_SERVICES_PORT}."
cd "${WCRS_SERVICES_HOME}/live/logs"
if [ -f "${WCRS_SERVICES_HOME}/live/logs/wcrs-services.log" ]; then
  mv wcrs-services.log wcrs-services.log.${DATESTAMP}
fi
nohup "${WCRS_SERVICES_JAVA_HOME}/bin/java" -Ddw.http.port=${WCRS_SERVICES_PORT} \
      -jar "${WCRS_SERVICES_HOME}/live/webapps/${WCRS_SERVICES_JAR}" \
      server "${WCRS_SERVICES_HOME}/live/conf/configuration.yml" > "${WCRS_SERVICES_HOME}/live/logs/wcrs-services.log" &
echo $! > "${WCRS_SERVICES_HOME}/live/logs/pid"

echo "Deploy complete."
echo ""
exit 0

