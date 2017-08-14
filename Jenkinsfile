node {
    step([$class: 'StashNotifier'])
    stage('Build') {
        def mvn = docker.image("maven:3.5.0-jdk-7-alpine")

        mvn.inside() {
            sh 'mvn package'
        }
    }
    step([$class: 'StashNotifier'])
}