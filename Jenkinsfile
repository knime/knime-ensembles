#!groovy
def BN = BRANCH_NAME == "master" || BRANCH_NAME.startsWith("releases/") ? BRANCH_NAME : "master"

library "knime-pipeline@$BN"

properties([
    pipelineTriggers([
        // knime-base -> knime-distance -> knime-ensembles
        // knime-base -> knime-javasnippet -> knime-distance -> knime-ensembles
        upstream("knime-svg/${env.BRANCH_NAME.replaceAll('/', '%2F')}" +
            ", knime-distance/${env.BRANCH_NAME.replaceAll('/', '%2F')}")
    ]),
    parameters(workflowTests.getConfigurationsAsParameters()),
    buildDiscarder(logRotator(numToKeepStr: '5')),
    disableConcurrentBuilds()
])


try {
    knimetools.defaultTychoBuild('org.knime.update.ensembles')

    workflowTests.runTests(
        dependencies: [
            repositories: [
                "knime-ensembles", "knime-streaming","knime-jep", "knime-datageneration", 
                "knime-r", "knime-pmml", "knime-wide-data", "knime-js-core", "knime-js-base",
                "knime-distance", "knime-database", "knime-kerberos", "knime-filehandling"
            ]
        ]
    )

    stage('Sonarqube analysis') {
    env.lastStage = env.STAGE_NAME
    workflowTests.runSonar()
    }
} catch (ex) {
    currentBuild.result = 'FAILURE'
    throw ex
} finally {
    notifications.notifyBuild(currentBuild.result);
}
/* vim: set shiftwidth=4 expandtab smarttab: */
