# 基础镜像说明
# openjdk:10.0.2-jre-sid 》》 涉及图形界面UI相关API操作时使用（如：验证码生成），镜像大小 ≈ 230M
# openjdk:10.0.2-jre-slim-sid 》》 极简化的jar包运行环境，镜像大小 ≈ 100M
FROM openjdk:10.0.2-jre-slim-sid

MAINTAINER 孙金川 820674043@qq.com

# 添加时区环境变量，亚洲，上海
ENV TimeZone=Asia/Shanghai

# 使用软连接，并且将时区配置覆盖/etc/timezone
RUN ln -snf /usr/share/zoneinfo/$TimeZone /etc/localtime && echo $TimeZone > /etc/timezone

ADD target/*.jar yue-open-devops-deploy.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/yue-open-devops-deploy.jar"]