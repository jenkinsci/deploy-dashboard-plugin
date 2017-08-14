node {
    step([$class: 'StashNotifier'])
    stage('Build') {
        def mvn = docker.image("maven:3.5.0-jdk-7-alpine")

        mvn.inside() {
            checkout scm
            sh 'mvn package'
        }
    }
    step([$class: 'StashNotifier'])
}