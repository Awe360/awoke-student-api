pipeline {
    agent any

    environment {
        DOCKER_REGISTRY          = "docker.io"
        DOCKER_IMAGE             = "awoke/awoke-student-api"
        IMAGE_TAG                = "${env.BUILD_NUMBER}"
        NAMESPACE                = "default"
        KUBECONFIG_CREDENTIAL_ID = "minikube-kubeconfig"
        DOCKER_HUB_CREDENTIAL_ID = "dockerhub-credentials"
    }

    stages {
        stage('Install Dependencies') {
            steps {
                echo 'Installing dependencies...'
                sh 'mvn dependency:go-offline -B'
            }
        }

        stage('Verify App') {
            steps {
                echo 'Verifying app (build & skip tests for now)...'
                sh '''
                    mvn clean verify -DskipTests=true
                    echo "Skipping full smoke test for now – in-memory API"
                '''
            }
        }

        stage('Docker Build & Push') {
            steps {
                script {
                    echo "Building & pushing Docker image: ${DOCKER_IMAGE}:${IMAGE_TAG}"
                    docker.withRegistry('https://index.docker.io/v1/', DOCKER_HUB_CREDENTIAL_ID) {
                        def customImage = docker.build("${DOCKER_IMAGE}:${IMAGE_TAG}")
                        customImage.push()          // pushes :${BUILD_NUMBER}
                        customImage.push('latest')  // also pushes :latest
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                script {
                    echo "Deploying to Kubernetes namespace: ${NAMESPACE}"
                    withCredentials([file(credentialsId: KUBECONFIG_CREDENTIAL_ID, variable: 'KUBECONFIG')]) {
                        sh """
                            kubectl set image deployment/awoke-student-api \
                                awoke-student-api=${DOCKER_IMAGE}:${IMAGE_TAG} -n ${NAMESPACE}

                            kubectl apply -f k8s/ -n ${NAMESPACE}

                            kubectl rollout status deployment/awoke-student-api \
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
            echo "🚀 Deployment of version ${IMAGE_TAG} succeeded!"
        }
        failure {
            echo "❌ Build or Deployment failed. Check the logs."
        }
        always {
            sh 'docker system prune -f || true'
        }
    }
}