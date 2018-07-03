// Requires the Workspace Cleanup Plugin to be installed before use
// https://jenkins.io/doc/pipeline/steps/ws-cleanup/
pipeline {
    agent { label 'master' }
    stages {
        stage('Build project artifacts') {
            steps {
                // Options are
                // -B: Run in non-interactive (batch) mode
                // -D: Define a system property (in this case skip compiling and running tests
                // -T 1C: Thread count which means run single threaded
                // clean: remove files from a previous build
                // package: take the compiled code and package it into a fat JAR
                sh "./mvnw -B -Dmaven.test.skip=true -T 1C clean package"
            }
        }
    }
    post {
        always {
            archiveArtifacts artifacts: 'target/*.jar', onlyIfSuccessful: true
            archiveArtifacts artifacts: 'configuration.yml', onlyIfSuccessful: true
            archiveArtifacts artifacts: 'src/test/resources/entity_data/entities.csv', onlyIfSuccessful: true
            archiveArtifacts artifacts: 'src/test/resources/ir_renewal_data/irdata_company.csv', onlyIfSuccessful: true
            archiveArtifacts artifacts: 'src/test/resources/ir_renewal_data/irdata_individual.csv', onlyIfSuccessful: true
            archiveArtifacts artifacts: 'src/test/resources/ir_renewal_data/irdata_partners.csv', onlyIfSuccessful: true
            archiveArtifacts artifacts: 'src/test/resources/ir_renewal_data/irdata_publicbody.csv', onlyIfSuccessful: true
            cleanWs cleanWhenFailure: false
        }
    }
}
