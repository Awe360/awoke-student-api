pipeline {
    agent none
    environment {
        DOCKER_IMAGE    = "awoke/awoke-student-api"
        IMAGE_TAG       = "${env.BUILD_NUMBER}"
        DOCKER_CRED_ID  = "dockerhub-credentials"
        KUBE_CRED_ID    = "minikube-kubeconfig"
        NAMESPACE       = "default"
        DEPLOYMENT_NAME = "awoke-student-api"
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
                    args '-v /var/run/docker.sock:/var/run/docker.sock'   // ← mount docker socket
                }
            }
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Build & Push with Buildx') {
            agent any   // run on same node as previous (reuseNode was used before)
            steps {
                withCredentials([usernamePassword(
                    credentialsId: DOCKER_CRED_ID,
                    usernameVariable: 'USER',
                    passwordVariable: 'PASS'
                )]) {
                    sh '''
                        echo "${PASS}" | docker login -u "${USER}" --password-stdin

                        docker buildx create --use --name mybuilder || true
                        docker buildx inspect --bootstrap

                        docker buildx build \
                            --push \
                            --cache-to=type=inline \
                            --tag ${DOCKER_IMAGE}:${IMAGE_TAG} \
                            --tag ${DOCKER_IMAGE}:latest \
                            --platform linux/amd64 \
                            -f Dockerfile .
                    '''
                }
            }
        }

        stage('Deploy to Kubernetes') {
            agent any
            steps {
                withCredentials([file(credentialsId: KUBE_CRED_ID, variable: 'KUBECONFIG')]) {
                    sh '''
                        export KUBECONFIG=$KUBECONFIG
                        kubectl set image deployment/${DEPLOYMENT_NAME} \
                            awoke-student-api=${DOCKER_IMAGE}:${IMAGE_TAG} \
                            -n ${NAMESPACE} --record
                        kubectl apply -f k8s/deployment.yaml -n ${NAMESPACE}
                        kubectl apply -f k8s/service.yaml -n ${NAMESPACE}
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

    }
}