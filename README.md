# yue-open-devops-deploy
提供Rancher持续部署能力，通知Rancher进行工作负载重新部署（用于docker镜像版本更新后，在Pod配置不变的情况下，更新拉取最新（或指定版本）的镜像）。

yue-open-devops-deploy基于yue-library开发，所有配置项皆可通过docker `-e` 环境变量注入

## docker启动
### 配置项说明
|<div style="width:300px">配置项</div>					|必填	|<div style="width:250px">配置项说明</div>																										|示例配置																					|
|--														|--		|--																																				|--																							|
|`server.port`											|否		|容器内部端口号																																	|默认：9999																					|
|`yue.devops.deploy.bearer-tokens`						|是		|Rancher bearerToken（需自行在Rancher中设置） <br> ，用于调用Rancher API（格式：`key: value`）<br> 可填多个，发起 `/devops/redeploy` 请求时必要	|`dev: Bearer token-47f49:s8jvl9hcvrcg9r9jcrft7cgdzf2qrngfm4s9sf6xs75qtx7zhhb5xg`			|
|`yue.devops.deploy.yue-open-devops-deploy-workload-url`|否		|`yue-open-devops-deploy`在Rancher中的工作负载访问地址，用于钉钉通知时点击																		|https://192.168.3.50/p/c-jrhf8:p-rg7dh/workload/deployment:default:yue-open-devops-deploy	|
|`yue.devops.deploy.dingtalk-devops-robot-webhook`		|否		|钉钉DevOps机器人Webhook																														|https://oapi.dingtalk.com/robot/send?access_token=**										|
|`yue.devops.deploy.dingtalk-devops-robot-sign-secret`	|否		|钉钉DevOps机器人密钥																															|SECcb73a2de6d5176a2226df1157413f1d207e380f024351c04a46f1850dde14b22						|
|`yue.devops.deploy.dingtalk-at-mobiles`				|否		|钉钉通知@群成员手机号																															|18511111111（多个`,`分割）																	|

yml配置示例：
```yml
server:
  port: 9999
yue:
  devops:
    deploy:
      bearer-tokens:
         dev: Bearer token-47f49:s8jvl9hcvrcg9r9jcrft7cgdzf2qrngfm4s9sf6xs75qtx7zhhb5xg
         test: Bearer token-47f49:s8jvl9hcvrcg9r9jcrft7cgdzf2qrngfm4s9sf6xs75qtx7zhhb5xg
      yue-open-devops-deploy-workload-url: https://192.168.3.50/p/c-jrhf8:p-rg7dh/workload/deployment:default:yue-open-devops-deploy
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
docker run -d -e key=value -p9999:9999 --name=yue-open-devops-deploy ylyue/yue-open-devops-deploy:1.1.0
```

## 如何使用
1. 确保yue-open-devops-deploy已部署在可访问Rancher Service API的环境中（推荐直接部署在Rancher管理的k8s集群中）
2. 触发yue-open-devops-deploy通知

> HTTP接口地址：PUT /devops/redeploy

|<div style="width:110px">参数名</div>	|必填	|<div style="width:250px">参数作用说明</div>						|参数值示例																					|
|--										|--		|--																	|--																							|
|workloadApiUrl							|是		|Rancher工作负载的ApiUrl（需要通知Rancher进行重新部署的工作负载）	|https://192.168.3.50/v3/project/c-nc2j5:p-rfgdc/workloads/deployment:pretest-sc-mdp:mdp-log|
|bearerTokenName						|是		|配置在yue-open-devops-deploy中，所对应的bearerToken名				|dev																						|
|imageTag								|否		|镜像版本															|2.0.1																						|

完整请求示例：
```shell
curl -X PUT http://192.168.0.11:9999/devops/redeploy -d 'workloadApiUrl=https://192.168.3.50/v3/project/c-nc2j5:p-rfgdc/workloads/deployment:pretest-sc-mdp:mdp-log&bearerTokenName=dev&imageTag=2.0.1
```

环境变量请求示例（如：云效等场景）：
```shell
curl -X PUT ${CD_URL} -d ${workloadApiUrl}'&bearerTokenName='${bearerTokenName}'&imageTag='${DATETIME}
```

## 历史推荐版本
[1.0.2](https://gitee.com/yue-open/yue-open-devops-deploy/tree/master/docs)