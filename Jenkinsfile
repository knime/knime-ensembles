#!groovy
def BN = BRANCH_NAME == "master" || BRANCH_NAME.startsWith("releases/") ? BRANCH_NAME : "master"

library "knime-pipeline@$BN"

properties([
    pipelineTriggers([
        upstream('knime-base/' + env.BRANCH_NAME.replaceAll('/', '%2F')),
        upstream('knime-svg/' + env.BRANCH_NAME.replaceAll('/', '%2F')),
        upstream('knime-distance/' + env.BRANCH_NAME.replaceAll('/', '%2F')),
        upstream('knime-javasnippet/' + env.BRANCH_NAME.replaceAll('/', '%2F'))
    ]),
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
                "knime-distance", "knime-database", "knime-kerberos"
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
