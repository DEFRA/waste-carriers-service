#!/bin/bash
#set -x

## This script is designed to be run by the jenkins user after a successful build.
## It will create a deployable tarball tagged with the jenkins build number and a timestamp.
## It will deploy the build to ea-dev.

## This script should only be started by the user jenkins
USER=`/usr/bin/whoami`
if [ "$USER" != "jenkins" ]; then
    echo "ERROR: This script should only be run by the user jenkins."
    exit 1
fi

build_dir="/caci/jenkins/jobs/waste-exemplar-services/builds"
datestamp=`date +%Y.%m.%d-%H.%M`
local_baseline_dir="/caci/baselines/wcrs-services"
remote_baseline_dir="/caci/deploys/wcrs-services/baselines"
remote_codedrop_dir="/caci/deploys/wcrs-services/codedrop"
workspace_dir="/caci/jenkins/jobs/waste-exemplar-services/workspace"

echo ""
## Make a record of the jenkins build number.
jenkins_build_number=`ls -l $build_dir | grep ^l | grep -v last | awk '{print $9}' | sort -n | tail -1`
echo "j$jenkins_build_number" > $workspace_dir/jenkins_build_number
echo "jenkins_build_number = $jenkins_build_number"

## Find the latest jar file.
cd $workspace_dir/target/
jar_file=`ls -tr waste-exemplar-services*.jar | tail -1`
echo "Using latest jar file: $jar_file"

## Create a list of files to be included in the tarball.
cat << EOF > $workspace_dir/bin/codedrop_include
bin
configuration.yml
jenkins_build_number
LICENSE
pom.xml
README.md
EOF
echo "target/$jar_file" >> $workspace_dir/bin/codedrop_include 

## Tar up a deployable codedrop.
tarball_name="codedrop-wcrs-services-j${jenkins_build_number}-${datestamp}.tgz"
echo "Tarring up this codedrop for deploys to other servers. You can find it here:"
echo "    $local_baseline_dir/$tarball_name" 
cd $workspace_dir
tar -zcf "$local_baseline_dir/$tarball_name" -T bin/codedrop_include

## Deploy to ea-dev.
echo "Deploying $tarball_name to ea-dev."
ssh wcrs-services@ea-dev "rm -fr /caci/deploys/wcrs-services/codedrop/*"
scp $local_baseline_dir/$tarball_name wcrs-services@ea-dev:$remote_codedrop_dir/
ssh wcrs-services@ea-dev "source /home/wcrs-services/.bash_wcrs-services_config; \
                          cd /caci/deploys/wcrs-services/codedrop; \
                          tar zxf *.tgz; \
                          rm *tgz; \
                          bin/deploy.sh"

echo ""
exit 0

