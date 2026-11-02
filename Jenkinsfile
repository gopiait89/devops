pipeline {
    agent {
        kubernetes {
            defaultContainer 'maven'

            yaml '''
apiVersion: v1
kind: Pod
spec:
  containers:
    - name: maven
      image: maven:3.9.9-eclipse-temurin-21
      command: ["cat"]
      tty: true

    - name: docker
      image: docker:27.5.1-cli
      command: ["cat"]
      tty: true
      env:
        - name: DOCKER_HOST
          value: tcp://localhost:2375
        - name: DOCKER_TLS_CERTDIR
          value: ""

    - name: docker-daemon
      image: docker:27.5.1-dind
      securityContext:
        privileged: true
      env:
        - name: DOCKER_TLS_CERTDIR
          value: ""
'''
        }
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        DOCKER_IMAGE = 'gopinathka/hello-world'
    }

    stages {

        stage('Git Checkout') {
            steps {
                echo 'Source code checkout is handled by Jenkins SCM.'
                sh 'pwd'
                sh 'ls -la'
            }
        }

        stage('Maven Build and Test') {
            steps {
                container('maven') {
                    echo 'Building Spring Boot application using Maven'

                    sh 'java -version'
                    sh 'mvn -version'
                    sh 'mvn -B clean verify'
                    sh 'ls -lh target/*.jar'
                }
            }
        }

        stage('Docker Connectivity') {
            steps {
                container('docker') {
                    sh '''
                        echo "Waiting for Docker daemon..."

                        for i in $(seq 1 30); do
                            if docker info >/dev/null 2>&1; then
                                echo "Docker daemon is ready"
                                docker version
                                exit 0
                            fi

                            echo "Docker daemon is not ready yet..."
                            sleep 2
                        done

                        echo "ERROR: Docker daemon failed to start"
                        exit 1
                    '''
                }
            }
        }

        stage('Docker Build') {
            steps {
                container('docker') {
                    sh '''
                        echo "Building Docker image..."

                        docker build \
                          -t $DOCKER_IMAGE:$BUILD_NUMBER \
                          -t $DOCKER_IMAGE:latest .

                        docker images
                    '''
                }
            }
        }

        stage('Docker Hub Push') {
            steps {
                container('docker') {
                    withCredentials([
                        usernamePassword(
                            credentialsId: 'dockerhub-creds',
                            usernameVariable: 'DOCKERHUB_USER',
                            passwordVariable: 'DOCKERHUB_TOKEN'
                        )
                    ]) {
                        sh '''
                            set +x

                            echo "$DOCKERHUB_TOKEN" |
                              docker login \
                                --username "$DOCKERHUB_USER" \
                                --password-stdin

                            docker push $DOCKER_IMAGE:$BUILD_NUMBER
                            docker push $DOCKER_IMAGE:latest

                            docker logout
                        '''
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'SUCCESS: Maven build, Docker image creation and Docker Hub push completed!'
        }

        failure {
            echo 'FAILED: Check the failed stage in Jenkins Console Output.'
        }

        always {
            echo 'Pipeline execution completed.'
        }
    }
}