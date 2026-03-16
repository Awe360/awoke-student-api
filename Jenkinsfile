pipeline {
    agent any

    environment {
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
            agent {
                docker {
                    image 'maven:3.9.9-eclipse-temurin-21'
                }
            }
            steps {
                echo "Building Spring Boot application with Maven (JDK 21)..."
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Build & Push Docker Image with Kaniko') {
            agent {
                docker {
                    image 'gcr.io/kaniko-project/executor:debug-v1.5.2'
                    args '-v ${PWD}:/workspace'
                }
            }
            steps {
                script {
                    echo "Building & pushing Docker image → ${DOCKER_IMAGE}:${IMAGE_TAG} using Kaniko"

                    withCredentials([usernamePassword(
                        credentialsId: DOCKER_CRED_ID,
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )]) {
                        sh '''
                            echo "{\"auths\":{\"https://index.docker.io/v1/\":{\"username\":\"${DOCKER_USER}\",\"password\":\"${DOCKER_PASS}\"}}}" > /kaniko/.docker/config.json

                            /kaniko/executor \
                                --context "${PWD}" \
                                --dockerfile "${PWD}/Dockerfile" \
                                --destination "${DOCKER_IMAGE}:${IMAGE_TAG}" \
                                --destination "${DOCKER_IMAGE}:latest" \
                                --cache=true \
                                --verbosity=debug
                        '''
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            agent any
            steps {
                script {
                    echo "Deploying to Kubernetes (namespace: ${NAMESPACE})"

                    withCredentials([file(credentialsId: KUBE_CRED_ID, variable: 'KUBECONFIG')]) {
                        sh """
                            kubectl set image deployment/${DEPLOYMENT_NAME} \
                                awoke-student-api=${DOCKER_IMAGE}:${IMAGE_TAG} \
                                -n ${NAMESPACE} --record

                            kubectl apply -f k8s/deployment.yaml -n ${NAMESPACE}
                            kubectl apply -f k8s/service.yaml   -n ${NAMESPACE}

                            kubectl rollout status deployment/${DEPLOYMENT_NAME} \
                                -n ${NAMESPACE} --timeout=180s
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
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true, allowEmptyArchive: true
        }
    }
}