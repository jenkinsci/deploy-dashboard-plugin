# Contribution
Thank you for your interest in making [Deploy Dashboard Plugin](https://github.com/jenkinsci/deploy-dashboard-plugin) even better and more awesome. Your contributions are highly welcome.

Plugin source code is hosted on [GitHub](https://github.com/jenkinsci/deploy-dashboard-plugin). New feature proposals and bug fix proposals should be submitted as [GitHub pull requests](https://help.github.com/en/github/collaborating-with-issues-and-pull-requests/creating-a-pull-request). Your pull request will be evaluated by the [Jenkins job](https://jenkins.devops.namecheap.net/job/RND/job/jenkins-deploy-dashboard-plugin/).


## Development

There is an official [Jenkins Plugin Development Guild](https://wiki.jenkins.io/display/JENKINS/Plugin+tutorial) by Jenkins. All the details you will find there.

In short, you have to be familiar with java (jdk 1.8 is required) and maven build tool.

```bash
./mvnw clean install
```
This command will build the plugin. The `hpi` file you can find in the `target` folder.

## Release (Only for Plugin's maintainers)

Official documentation: [Performing a Plugin Release](https://jenkins.io/doc/developer/publishing/releasing/)

There is `Jenkinsfile.release` file in the root directory which you can use as jenkins pipeline

**For Namecheap employees only:** There is Jenkins job `https://{{NC_JENKINS_DOMAIN}}/job/RND/job/jenkins-deploy-dashboard-plugin/`.
By running this job the new version (taken from [pom.xml](pom.xml) file) will be published.

P.S. It usually takes time when the new version appears in the jenkins registry search.