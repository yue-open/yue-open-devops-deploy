# yue-open-devops-deploy
Rancher持续部署通知，通知Rancher进行工作负载重新部署（用于docker镜像更新后，配置不变更拉取新镜像运行容器）。

yue-open-devops-deploy基于yue-library开发，所有配置项皆可通过docker环境变量注入

## 配置项说明
|配置项												|配置项说明						|示例配置																	|
|--													|--								|--																			|
|`server.port`										|容器内部端口号					|默认：9999																	|
|`yue.devops.deploy.workload-api-url`				|Rancher 工作负载API地址		|https://10.100.3.153/p/c-prnj6:p-dfrng/workloads							|
|`yue.devops.deploy.dev-bearer-token`				|Rancher 开发环境 bearerToken	|Bearer token-47f49:s8jvl9hcvrcg9r9jcrft7cgdzf2qrngfm4s9sf6xs75qtx7zhhb5xg	|
|`yue.devops.deploy.pretest-bearer-token`			|Rancher 预发环境 bearerToken	|Bearer token-47f49:s8jvl9hcvrcg9r9jcrft7cgdzf2qrngfm4s9sf6xs75qtx7zhhb5xg	|
|`yue.devops.deploy.master-bearer-token`			|Rancher 生产环境 bearerToken	|Bearer token-47f49:s8jvl9hcvrcg9r9jcrft7cgdzf2qrngfm4s9sf6xs75qtx7zhhb5xg	|
|`yue.devops.deploy.dingtalk-devops-robot-webhook`	|钉钉DevOps机器人Webhook		|https://oapi.dingtalk.com/robot/send?access_token=**						|
|`yue.devops.deploy.dingtalk-at-mobiles`			|钉钉通知@群成员手机号			|18511111111（多个`,`分割）													|

yml配置示例：
```yml
server:
  port: 9999
yue:
  devops:
    deploy:
        workload-api-url: https://10.100.3.153/p/c-prnj6:p-dfrng/workloads
        dev-bearer-token: Bearer token-47f49:s8jvl9hcvrcg9r9jcrft7cgdzf2qrngfm4s9sf6xs75qtx7zhhb5xg
        pretest-bearer-token: Bearer token-4www5:7ckp26rkxb8nfzgv2ms6vw7zzb4tlfgtxknpksj7bfkm6pgtqd77l2
        master-bearer-token: Bearer token-4www5:7ckp26rkxb8nfzgv2ms6vw7zzb4tlfgtxknpksj7bfkm6pgtqd77l2
        dingtalk-devops-robot-webhook: https://oapi.dingtalk.com/robot/send?access_token=**
        dingtalk-at-mobiles:
        - 18511111111
```