@echo off
REM 声明采用UTF-8编码
chcp 65001

call mvn clean package

set /p version=请输入docker镜像版本号:

echo 请输入阿里云docker镜像仓库登录密码

docker login --username=862618889@qq.com registry.cn-hangzhou.aliyuncs.com

docker build --tag registry.cn-hangzhou.aliyuncs.com/yl-yue/yue-open-devops-deploy:%version% .

docker push registry.cn-hangzhou.aliyuncs.com/yl-yue/yue-open-devops-deploy:%version%

echo.
goto end

:end
echo 成功，请按任意键退出。
@Pause>nul