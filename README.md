# yue-open-devops-deploy
提供Rancher持续部署能力，通知Rancher进行工作负载重新部署（用于docker镜像版本更新后，在Pod配置不变的情况下，更新拉取最新的镜像（或指定版本））。

yue-open-devops-deploy基于yue-library开发，所有配置项皆可通过docker环境变量注入

## docker启动
### 配置项说明
|<div style="width:300px">配置项</div>					|<div style="width:250px">配置项说明</div>									|示例配置																					|
|--														|--																			|--																							|
|`server.port`											|容器内部端口号																|默认：9999																					|
|`yue-open-devops-deploy-workload-url`					|`yue-open-devops-deploy`在Rancher中的工作负载访问地址，用于钉钉通知时点击	|https://192.168.3.50/p/c-jrhf8:p-rg7dh/workload/deployment:default:yue-open-devops-deploy	|
|`yue.devops.deploy.dev-bearer-token`					|Rancher 开发环境 bearerToken												|Bearer token-47f49:s8jvl9hcvrcg9r9jcrft7cgdzf2qrngfm4s9sf6xs75qtx7zhhb5xg					|
|`yue.devops.deploy.pretest-bearer-token`				|Rancher 预发环境 bearerToken												|Bearer token-47f49:s8jvl9hcvrcg9r9jcrft7cgdzf2qrngfm4s9sf6xs75qtx7zhhb5xg					|
|`yue.devops.deploy.master-bearer-token`				|Rancher 生产环境 bearerToken												|Bearer token-47f49:s8jvl9hcvrcg9r9jcrft7cgdzf2qrngfm4s9sf6xs75qtx7zhhb5xg					|
|`yue.devops.deploy.dingtalk-devops-robot-webhook`		|钉钉DevOps机器人Webhook													|https://oapi.dingtalk.com/robot/send?access_token=**										|
|`yue.devops.deploy.dingtalk-devops-robot-sign-secret`	|钉钉DevOps机器人密钥														|SECcb73a2de6d5176a2226df1157413f1d207e380f024351c04a46f1850dde14b22						|
|`yue.devops.deploy.dingtalk-at-mobiles`				|钉钉通知@群成员手机号														|18511111111（多个`,`分割）																	|

yml配置示例：
```yml
server:
  port: 9999
yue:
  devops:
    deploy:
        yue-open-devops-deploy-workload-url: https://192.168.3.50/p/c-jrhf8:p-rg7dh/workload/deployment:default:yue-open-devops-deploy
        dev-bearer-token: Bearer token-47f49:s8jvl9hcvrcg9r9jcrft7cgdzf2qrngfm4s9sf6xs75qtx7zhhb5xg
        pretest-bearer-token: Bearer token-4www5:7ckp26rkxb8nfzgv2ms6vw7zzb4tlfgtxknpksj7bfkm6pgtqd77l2
        master-bearer-token: Bearer token-4www5:7ckp26rkxb8nfzgv2ms6vw7zzb4tlfgtxknpksj7bfkm6pgtqd77l2
        dingtalk-devops-robot-webhook: https://oapi.dingtalk.com/robot/send?access_token=**
        dingtalk-devops-robot-sign-secret: SECcb73a2de6d5176a2226df1157413f1d207e380f024351c04a46f1850dde14b22
        dingtalk-at-mobiles:
        - 18511111111
```

### docker启动命令示例
[👉dockerhub](https://hub.docker.com/r/ylyue/yue-open-devops-deploy)

国内镜像加速地址：registry.cn-hangzhou.aliyuncs.com/yl-yue/yue-open-devops-deploy

docker启动命令示例（注意替换`-e`环境变量中的值）：
```docker
docker run -d -e key=value -p9999:9999 --name=yue-open-devops-deploy ylyue/yue-open-devops-deploy:1.0.2
```

## 如何使用
1. 确保yue-open-devops-deploy已部署在可访问Rancher Service API的环境中（推荐直接部署在Rancher管理的k8s集群中）
2. 触发yue-open-devops-deploy通知

> 接口地址：PUT /devops/redeploy

|<div style="width:110px">参数名</div>	|是否必填参数	|<div style="width:250px">参数作用说明</div>							|参数值示例																					|
|--										|--				|--																		|--																							|
|workloadApiUrl							|是				|Rancher工作负载的ApiUrl（需要通知Rancher进行重新部署的工作负载）		|https://192.168.3.50/v3/project/c-nc2j5:p-rfgdc/workloads/deployment:pretest-sc-mdp:mdp-log|
|envEnum								|是				|环境（`DEV`, `PRETEST`, `MASTER`），需要配置对应的Rancher bearerToken	|PRETEST																					|
|tag									|否				|镜像版本																|2.0.1																						|

请求示例：
```shell
curl -X PUT ${CD_URL} -d 'workloadApiUrl=https://192.168.3.50/v3/project/c-nc2j5:p-rfgdc/workloads/deployment:pretest-sc-mdp:mdp-log&envEnum='${ENV}'&tag='${DATETIME}
```