pipeline {
    agent any

    options {
        timestamps()
        timeout(time: 20, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    environment {
        IMAGE = "taskboard:${env.BUILD_NUMBER}"
        MAVEN_SETTINGS = '/opt/maven-settings.xml'
    }

    stages {
        stage('编译与测试') {
            steps {
                sh 'mvn -B -s $MAVEN_SETTINGS clean verify'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('构建镜像') {
            steps {
                sh 'docker build -t "$IMAGE" .'
            }
        }

        stage('部署') {
            steps {
                sh '''
                    set -e
                    APP_IMAGE="$IMAGE" docker compose up -d --no-build
                '''
            }
        }

        stage('健康检查') {
            steps {
                sh '''
                    for i in $(seq 1 30); do
                        if curl -sf http://host.docker.internal:8081/actuator/health | grep -q UP; then
                            echo "部署成功,应用健康"
                            exit 0
                        fi
                        echo "等待应用启动 (${i}/30)"
                        sleep 5
                    done
                    echo "健康检查失败,最近日志:"
                    docker compose logs --tail 50 app
                    exit 1
                '''
            }
        }
    }

    post {
        success {
            echo "流水线成功，镜像: ${env.IMAGE}"
        }
        failure {
            echo "流水线失败，请查看上方红色步骤的日志"
        }
    }
}
