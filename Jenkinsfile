def services = [
    'Banking-System-2',
    'accountservice',
    'api-gateway',
    'authservice',
    'eureka-server',
    'transactionservice'
]


pipeline {
    agent any

    environment {
        VITE_API_GATEWAY_URL = "http://localhost:9192"
    }

    tools {
        nodejs 'NodeJS'
        maven 'maven'
        jdk 'JAVA'
    }

    stages {

        // Checkout code
        stage('Checkout') {
            steps {
                git branch: 'harshitha',
                    url: 'https://github.com/HarshaVardhan-Padmanabhuni/Banking-System-Frontend.git'
            }
        }

        // Create .env file for frontend
        stage('Create Env File') {
            steps {
                dir('frontend') {
                    bat 'echo VITE_API_GATEWAY_URL=%VITE_API_GATEWAY_URL% > .env'
                }
            }
        }

        // Install frontend dependencies
        stage('Install Frontend Dependencies') {
            steps {
                dir('frontend') {
                    bat 'npm install'
                }
            }
        }

        // Build frontend
        stage('Build Frontend') {
            steps {
                dir('frontend') {
                    bat 'npm run build'
                }
            }
        }

        // Build ALL backend services
        stage('Build Backend Services') {
            steps {
                script {
                    services.each { svc ->
                        echo "Building ${svc}..."
                        dir("Backend/${svc}") {
                            bat 'mvn clean install -Dmaven.test.skip=true'
                        }
                    }
                }
            }
        }

        // Start Eureka Server FIRST
        stage('Start Eureka Server') {
            steps {
                dir('Backend/eureka-server') {
                    bat 'start cmd /c mvn spring-boot:run'
                }
            }
        }

        // Start Auth Service (optional but recommended)
        stage('Start Auth Service') {
            steps {
                dir('Backend/authservice') {
                    bat 'start cmd /c mvn spring-boot:run'
                }
            }
        }

        // Start Account Service
        stage('Start Account Service') {
            steps {
                dir('Backend/accountservice') {
                    bat 'start cmd /c mvn spring-boot:run'
                }
            }
        }

        // Start Transaction Service
        stage('Start Transaction Service') {
            steps {
                dir('Backend/transactionservice') {
                    bat 'start cmd /c mvn spring-boot:run'
                }
            }
        }

        // Start API Gateway LAST
        stage('Start API Gateway') {
            steps {
                dir('Backend/api-gateway') {
                    bat 'start cmd /c mvn spring-boot:run'
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}
