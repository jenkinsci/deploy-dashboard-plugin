node {
    stage('Build') {
        def mvn = docker.image("maven:3.5.0-jdk-7-alpine")

        mvn.inside() {
            checkout scm
            step([$class: 'StashNotifier'])
            sh 'mvn package'
            archiveArtifacts artifacts: 'target/environment-dashboard.hpi'
            step([$class: 'StashNotifier'])
        }
    }
}