pipeline {
    agent any

    parameters {
        choice(name: 'ENVIRONMENT', choices: ['test', 'prod'], description: '目标环境')
    }

    options {
        timestamps()
        timeout(time: 20, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    environment {
        IMAGE = "taskboard:${env.BUILD_NUMBER}"
        MAVEN_SETTINGS = '/opt/maven-settings.xml'
        APP_PORT = "${params.ENVIRONMENT == 'prod' ? '8091' : '8090'}"
        SPRING_PROFILE = "${params.ENVIRONMENT}"

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

        stage('质量门禁') {
            steps {
                sh '''
                    SIZE=$(docker image inspect "$IMAGE" --format "{{.Size}}")
                    MB=$((SIZE / 1024 / 1024))
                    echo "镜像体积: ${MB} MB"

                    LIMIT=700
                    if [ "$MB" -gt "$LIMIT" ]; then
                        echo "镜像体积 ${MB} MB 超过阈值 ${LIMIT} MB，终止发布"
                        exit 1
                    fi
                    echo "门禁通过，阈值 ${LIMIT} MB"
                '''
            }
        }

        stage('环境校验') {
            steps {
                script {
                    if (!(params.ENVIRONMENT in ['test', 'prod'])) {
                        error "非法环境: ${params.ENVIRONMENT}"
                    }
                    echo "目标环境 ${params.ENVIRONMENT}，端口 ${env.APP_PORT}"
                }
            }
        }

        stage('生产确认') {
            when { expression { params.ENVIRONMENT == 'prod' } }
            steps {
                input message: "确认发布到生产环境？镜像 ${env.IMAGE}", ok: '确认发布'
            }
        }

        stage('部署') {
            steps {
                sh 'APP_IMAGE="$IMAGE" APP_PORT="$APP_PORT" docker compose up -d --no-build'
            }
        }


        stage('健康检查') {
            steps {
                sh '''
                    for i in $(seq 1 30); do
                            if curl -sf "http://host.docker.internal:${APP_PORT}/actuator/health" | grep -q UP; then
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
