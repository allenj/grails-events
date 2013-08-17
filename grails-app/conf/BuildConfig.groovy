grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

grails.project.fork = [
		// configure settings for compilation JVM, note that if you alter the Groovy version forked compilation is required
		//  compile: [maxMemory: 256, minMemory: 64, debug: false, maxPerm: 256, daemon:true],

		// configure settings for the test-app JVM, uses the daemon by default
		test: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, daemon: true],
		// configure settings for the Console UI JVM
		console: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256]
]

grails.project.dependency.resolver = "maven"
grails.project.dependency.resolution = {
	inherits("global") {
	}

	log "warn"
	repositories {
		grailsCentral()
		mavenLocal()
		mavenCentral()
		mavenRepo "http://repo.springsource.org/libs-milestone"
	}

	dependencies {
		runtime 'org.projectreactor:reactor-groovy:1.0.0.BUILD-SNAPSHOT'
		runtime 'org.projectreactor:reactor-spring:1.0.0.BUILD-SNAPSHOT'

	}

	plugins {
		build(
				":release:3.0.0", ":rest-client-builder:1.0.3") {
			export = false
		}
	}
}
