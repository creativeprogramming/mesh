// The GIT repository for this pipeline lib is defined in the global Jenkins setting
@Library('jenkins-pipeline-library')
import com.gentics.*

// Make the helpers aware of this jobs environment
JobContext.set(this)

final def dockerRegistry       = "gentics-docker-jenkinsbuilds.docker.apa-it.at"
final def dockerImageName      = dockerRegistry + "/gentics/jenkinsbuilds/mesh-slave-openjdk8"

properties([
	parameters([
		booleanParam(name: 'runTests',            defaultValue: true,  description: "Whether to run the unit tests"),
		booleanParam(name: 'splitTests',          defaultValue: true, description: "Whether to split tests or not"),
		booleanParam(name: 'runSonar',            defaultValue: false, description: "Whether to run the sonarqube checks"),
		booleanParam(name: 'runPerformanceTests', defaultValue: false, description: "Whether to run performance tests."),
		booleanParam(name: 'runClusterTests',     defaultValue: false, description: "Whether to run cluster tests."),
		booleanParam(name: 'runDeploy',           defaultValue: false, description: "Whether to run the deploy steps."),
		booleanParam(name: 'runDocker',           defaultValue: false, description: "Whether to run the docker steps."),
		booleanParam(name: 'runMavenBuild',       defaultValue: false, description: "Whether to run the maven build steps."),
		booleanParam(name: 'runIntegrationTests', defaultValue: false, description: "Whether to run integration tests."),
		booleanParam(name: 'release',             defaultValue: false, description: "Releases the built version on github and getmesh.io")
	])
])

final def gitCommitTag         = '[Jenkins | ' + env.JOB_BASE_NAME + ']';
final def splits = 20;
final def runs = 20;

stage("Setup Build Environment") {
	node("mesh-worker") {
		githubBuildStarted()
		try {
			stage("Checkout") {
				checkout scm
			}

			def branchName = GitHelper.fetchCurrentBranchName()
			def version = MavenHelper.getVersion()

			stage("Set Version") {
				if (Boolean.valueOf(params.runDeploy)) {
					if (version) {
						echo "Building version " + version
						version = MavenHelper.transformSnapshotToReleaseVersion(version)
						MavenHelper.setVersion(version)
					}
					//TODO only add pom.xml files
					sh 'git add .'
					sh "git commit -m 'Raise version'"
					GitHelper.addTag(version, 'Release of version ' + version)
				}
			}

			stage("Test") {
				if (Boolean.valueOf(params.runTests)) {
					if (Boolean.valueOf(params.splitTests)) {
						sh ".jenkins/split-tests.sh ${splits}"
						stash includes: '**', name: 'project'
						def branches = [:]
						for (int i = 0; i < runs; i++) {
							def current = i
							branches["split${i}"] = {
								node('mesh-worker') {
									echo "Preparing slave environment for ${current}"
									sh "rm -rf split${i}"
									sh "mkdir split${i}"
									dir("split${i}") {
										unstash 'project'
										def postfix = current;
										if (current <= 9) {
											postfix = "0" + current
										}
										echo "Setting correct inclusions file ${postfix}"
										sh "mv includes-${postfix} inclusions.txt"
										sshagent(["git"]) {
											try {
												sh "mvn -fae -Dmaven.javadoc.skip=true -Dskip.cluster.tests=true -Dmaven.test.failure.ignore=true -B -U -e -P inclusions -pl '!demo,!doc,!performance-tests' clean install"
												stash name: "jacoco" + current, includes: "**/jacoco-partial.exec", allowEmpty: true
											} finally {
												step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/*.xml'])
											}
										}
									}
								}
							}
						}
						try {
							parallel branches
						} catch (err) {
							echo "Failed " + err.getMessage()
							error err.getMessage()
						}
					} else {
						sshagent(["git"]) {
							try {
								sh "mvn -fae -Dmaven.javadoc.skip=true -Dskip.cluster.tests=true -Dmaven.test.failure.ignore=true -B -U -e -pl '!demo,!doc,!performance-tests' clean install"
							} finally {
								step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/*.xml'])
							}
						}
					}
				} else {
					echo "Tests skipped.."
				}
			}

			stage("Maven Build") {
				if (Boolean.valueOf(params.runMavenBuild)) {
					sshagent(["git"]) {
						if (Boolean.valueOf(params.runDeploy)) {
							withCredentials([usernamePassword(credentialsId: 'repo.gentics.com', usernameVariable: 'repoUsername', passwordVariable: 'repoPassword')]) {
								sh "mvn -U -B -DskipTests clean deploy"
							}
						} else {
							sh "mvn -B -DskipTests clean package"
						}
					}
				} else {
					echo "Maven build skipped.."
				}
			}

			stage ("Sonar") {
				if (Boolean.valueOf(params.runTests) && Boolean.valueOf(params.runSonar)) {
					if (Boolean.valueOf(params.splitTests)) {
						for (int i = 0; i < runs; i++) {
							echo "Invoking merge for " + i
							unstash "jacoco" + i
							sh "mvn jacoco:merge@merge-all-jacoco -Dskip.jacoco.merge=false"
							sh "find -name jacoco-partial.exec -exec rm {} \\;"
						}
					}

					// Run the maven build if the build did not run previously
					if (!Boolean.valueOf(params.runMavenBuild)) {
						sh "mvn -B -DskipTests package"
					}
					withCredentials([string(credentialsId: 'sonarcube.key', variable: 'TOKEN')]) {
						sh "mvn sonar:sonar -Dsonar.projectKey=mesh -Dsonar.host.url=https://sonarqube.apa.at -Dsonar.login=$TOKEN"
					}
				}
			}

			stage("Cluster Tests") {
				if (Boolean.valueOf(params.runClusterTests)) {
					node("mesh-cluster-worker") {
						try {
							sh "mvn -B -DskipTests clean install -pl '!demo,!doc'"
							sh "mvn -B test -pl distributed"
						} finally {
							step([$class: 'JUnitResultArchiver', testResults: 'distributed/target/surefire-reports/*.xml'])
						}
					}
				} else {
					echo "Cluster tests skipped.."
				}
			}

			stage("Docker Build") {
				if (Boolean.valueOf(params.runDocker)) {
					// demo
					sh "rm demo/target/*sources.jar"
					sh "cd demo ; docker build --network=host -t gentics/mesh-demo:latest -t gentics/mesh-demo:" + version + " . "

					// server
					sh "rm server/target/*sources.jar"
					sh "cd server ; docker build --network=host -t gentics/mesh:latest -t gentics/mesh:" + version + " . "
				} else {
					echo "Docker build skipped.."
				}
			}

			stage("Performance Tests") {
				if (Boolean.valueOf(params.runPerformanceTests)) {
					node("mesh-performance-worker") {
						try {
							sh "mvn -B -U clean package -pl '!doc,!demo,!distributed,!verticles,!server' -Dskip.unit.tests=true -Dskip.performance.tests=false -Dmaven.test.failure.ignore=true"
						} finally {
							step([$class: 'JUnitResultArchiver', testResults: '**/target/*.performance.xml'])
						}
					}
				} else {
					echo "Performance tests skipped.."
				}
			}

			stage("Integration Tests") {
				if (Boolean.valueOf(params.runIntegrationTests)) {
					withEnv(["MESH_VERSION=" + version]) {
						sh "integration-tests/test.sh"
					}
				} else {
					echo "Performance tests skipped.."
				}
			}

			stage("Deploy") {
				if (Boolean.valueOf(params.runDeploy)) {
					if (Boolean.valueOf(params.runDocker)) {
						withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'dockerhub_login', passwordVariable: 'DOCKER_HUB_PASSWORD', usernameVariable: 'DOCKER_HUB_USERNAME']]) {
							sh 'docker login -u $DOCKER_HUB_USERNAME -p $DOCKER_HUB_PASSWORD'
							sh 'docker push gentics/mesh-demo:latest'
							sh 'docker push gentics/mesh-demo:' + version
							sh 'docker push gentics/mesh:latest'
							sh 'docker push gentics/mesh:' + version
						}
					}
				} else {
					echo "Deploy skipped.."
				}
			}

			stage("Git push") {
				if (Boolean.valueOf(params.runDeploy)) {
					sshagent(["git"]) {
						def snapshotVersion = MavenHelper.getNextSnapShotVersion(version)
						MavenHelper.setVersion(snapshotVersion)
						GitHelper.addCommit('.', gitCommitTag + ' Prepare for the next development iteration (' + snapshotVersion + ')')
						GitHelper.pushBranch(branchName)
						GitHelper.pushTag(version)
					}
				} else {
					echo "Push skipped.."
				}
			}

			stage("Release") {
                if (Boolean.valueof(params.runDeploy) && Boolean.valueOf(params.release)) {
                    mergeToMaster()
                    createGithubRelease version
                    updateWebsite()
                } else {
                    echo "Release skipped.."
                }
			}
		} catch (Exception e) {
			if (currentBuild.result == null || currentBuild.currentResult == "STABLE") {
				currentBuild.result = 'FAILURE'
				currentBuild.currentResult = 'FAILURE'
			}
			throw e
		} finally {
			githubBuildEnded()
			notifyMattermostUsers()
		}
	}
}

def mergeToMaster() {
    sh "git checkout master"
    GitHelper.merge "dev"
    sh "git push"
}

def createGithubRelease(version) {
    // https://developer.github.com/v3/repos/releases/#create-a-release
	GitHub.connectUsingOAuth(credentials("Github-API-Token"))
		.getRepository("gentics/mesh")
		.createRelease(version)
		.name(version)
		.body("""<ul>
<li><a href='https://getmesh.io/docs/changelog#v${version}'>Changelog</a></li>
<li><a href='https://maven.gentics.com/maven2/com/gentics/mesh/mesh-demo/${version}/mesh-demo-${version}.jar'>Download Demo</a></li>
<li><a href='https://maven.gentics.com/maven2/com/gentics/mesh/mesh-server/${version}/mesh-server-${version}.jar'>Download Server</a></li>
</ul>""")
		.create()
}

def updateWebsite() {
	build job: "/Mesh/mesh-website", parameters: [
		booleanParam(name: 'BumpDocs', value: true),
		booleanParam(name: 'deploy', value: true)
	], wait: true
}