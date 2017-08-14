node {
    def mvn = docker.image("maven:3.5.0-jdk-7-alpine")
    mvn.inside() {
        checkout scm
        step([$class: 'StashNotifier'])

        stage('Build') {
            sh 'mvn package'
        }

        stage('Deploy to S3') {
            s3Upload(file: 'target/environment-dashboard.hpi', bucket: 'distributions.devops.namecheap.net', path: 'jenkins/plugins/environment-dashboard.hpi')
            archiveArtifacts artifacts: 'target/environment-dashboard.hpi'
        }

        step([$class: 'StashNotifier'])
    }
}