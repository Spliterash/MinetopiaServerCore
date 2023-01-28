def imageName = "paperweight-image"
pipeline {
    agent {
        dockerfile {
            filename ".ci/Dockerfile"
            args '-v $HOST_HOME/.gradle:/root/.gradle -v $HOST_HOME/.m2:/root/.m2'
        }
    }
    environment {
        ORG_GRADLE_PROJECT_SPLITERASH_NEXUS = credentials("spliterash-repo")
    }

    stages {
        stage("Notify") {
            steps {
                vkSendStart()
            }
        }
        stage("Build") {
            steps {
                sh "sh gradlew applyPatches"
                sh "sh gradlew createReobfPaperclipJar"
            }
        }
    }
    post {
        always {
            vkSendEnd()
        }
        success {
            script {
                archiveArtifacts artifacts: "build/libs/minetopia-paperclip-*.jar", fingerprint: true
            }
        }
    }
}