node {
    try {
        def mvn = docker.image("maven:3.5.0-jdk-7-alpine")
            mvn.inside() {
                checkout scm
                step([$class: 'StashNotifier'])

                stage('Build') {
                    sh 'mvn package'
                    archiveArtifacts artifacts: 'target/environment-dashboard.hpi'
                }
                if (env.BRANCH_NAME == 'master') {
                    stage('Deploy to S3') {
                        withAWS(credentials: '1c60c387-a550-407f-bd34-1ec0f6da1a4c') {
                            s3Upload(
                                    file: 'target/environment-dashboard.hpi',
                                    bucket: 'distributions.devops.namecheap.net',
                                    path: 'jenkins/plugins/environment-dashboard.hpi'
                            )
                        }
                    }
                }
            }
            currentBuild.result = 'SUCCESS'
    } catch(e) {
        currentBuild.result = 'FAILED'
    } finally {
        step([$class: 'StashNotifier'])
    }
}