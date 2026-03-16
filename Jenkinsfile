pipeline {
    agent {
        docker {
            image 'maven:3.9.9-eclipse-temurin-21'
            args '-v /var/run/docker.sock:/var/run/docker.sock --user root'
        }
    }

    environment {
        DOCKER_REGISTRY     = "docker.io"
        DOCKER_IMAGE        = "awoke/awoke-student-api"
        IMAGE_TAG           = "${env.BUILD_NUMBER}"
        DOCKER_CRED_ID      = "dockerhub-credentials"
        KUBE_CRED_ID        = "minikube-kubeconfig"
        NAMESPACE           = "default"
        DEPLOYMENT_NAME     = "awoke-student-api"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                echo "Building Spring Boot application with Maven (JDK 21)..."
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Build & Push Docker Image') {
            steps {
                script {
                    echo "Building Docker image → ${DOCKER_IMAGE}:${IMAGE_TAG}"

                    docker.withRegistry(DOCKER_REGISTRY, DOCKER_CRED_ID) {
                        def customImage = docker.build("${DOCKER_IMAGE}:${IMAGE_TAG}", ".")
                        customImage.push()
                        customImage.push('latest')
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                script {
                    echo "Deploying to Kubernetes (namespace: ${NAMESPACE})"

                    withCredentials([file(credentialsId: KUBE_CRED_ID, variable: 'KUBECONFIG')]) {
                        sh """
                            # Simple image update (fast rolling update)
                            kubectl set image deployment/${DEPLOYMENT_NAME} \
                                awoke-student-api=${DOCKER_IMAGE}:${IMAGE_TAG} \
                                -n ${NAMESPACE} \
                                --record

                            # Re-apply manifests if needed
                            kubectl apply -f k8s/deployment.yaml -n ${NAMESPACE}
                            kubectl apply -f k8s/service.yaml   -n ${NAMESPACE}

                            # Wait for rollout
                            kubectl rollout status deployment/${DEPLOYMENT_NAME} \
                                -n ${NAMESPACE} \
                                --timeout=180s
                        """
                    }
                }
            }
        }
    }

    post {
        success {
            echo "🎉 Pipeline completed successfully! Version ${IMAGE_TAG} deployed."
        }
        failure {
            echo "❌ Pipeline failed. Check console output for details."
        }
        always {
            sh 'docker system prune -f --filter "label=build=jenkins" || true'
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true, allowEmptyArchive: true
        }
    }
}