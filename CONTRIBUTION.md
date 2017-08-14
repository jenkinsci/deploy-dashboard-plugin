# Contribution

* [Jenkins Plugin Development Guild](https://wiki.jenkins.io/display/JENKINS/Plugin+tutorial#Plugintutorial-DistributingaPlugin)
* [Original Repository](https://github.com/vipinsthename/environment-dashboard)
* [Based on Pull Request](https://github.com/vipinsthename/environment-dashboard/pull/135)

## Development

### Requirements

* JDK 6.0 or later
* [Apache Maven 3](https://maven.apache.org/)

### Development

[Debugging a Plugin](https://wiki.jenkins.io/display/JENKINS/Plugin+tutorial#Plugintutorial-DebuggingaPlugin)

`NOTE: findbugs is skipped, plugin has some code analysis issues. See pom.xml`

```bash
export MAVEN_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=n"
$ mvn hpi:run
```

### Build

```bash
$ mvn package
# This should create target/*.hpi file. Other users can use Jenkins' web UI to upload this plugin to Jenkins (or place it in $JENKINS_HOME/plugins.)
```
