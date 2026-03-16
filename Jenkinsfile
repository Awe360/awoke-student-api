pipeline {
    agent none

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
            agent any
            steps {
                checkout scm
            }
        }

        stage('Maven Build') {
            agent {
                docker {
                    image 'maven:3.9.9-eclipse-temurin-21'
                    reuseNode true
                }
            }
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Build & Push with Kaniko') {
            agent {
                docker {
                    image 'gcr.io/kaniko-project/executor:v1.23.2'
                    args '-v ${PWD}:/workspace --entrypoint=""'
                    reuseNode true
                }
            }
            steps {
                withCredentials([usernamePassword(
                    credentialsId: DOCKER_CRED_ID,
                    usernameVariable: 'USER',
                    passwordVariable: 'PASS'
                )]) {
                    sh '''
                        mkdir -p /kaniko/.docker
                        echo "{\"auths\":{\"https://index.docker.io/v1/\":{\"auth\":\"$(echo -n ${USER}:${PASS} | base64)\"}}}" > /kaniko/.docker/config.json

                        executor \
                            --context /workspace \
                            --dockerfile /workspace/Dockerfile \
                            --destination ${DOCKER_IMAGE}:${IMAGE_TAG} \
                            --destination ${DOCKER_IMAGE}:latest \
                            --cache=true
                    '''
                }
            }
        }

        stage('Deploy to Kubernetes') {
            agent any
            steps {
                withCredentials([file(credentialsId: KUBE_CRED_ID, variable: 'KUBECONFIG')]) {
                    sh '''
                        kubectl set image deployment/${DEPLOYMENT_NAME} \
                            awoke-student-api=${DOCKER_IMAGE}:${IMAGE_TAG} \
                            -n ${NAMESPACE} --record

                        kubectl apply -f k8s/deployment.yaml -n ${NAMESPACE}
                        kubectl apply -f k8s/service.yaml   -n ${NAMESPACE}

                        kubectl rollout status deployment/${DEPLOYMENT_NAME} \
                            -n ${NAMESPACE} --timeout=180s
                    '''
                }
            }
        }
    }

    post {
        success {
            echo "Deployment of ${IMAGE_TAG} completed successfully"
        }
        failure {
            echo "Pipeline failed"
        }
        always {
            archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: true
        }
    }
}