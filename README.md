<p align="center">
	<a target="_blank" href="https://ylyue.cn/">
		<img src="https://dcloud.ylyue.cn/yue-library/_images/logo.png" width="400">
	</a>
</p>
<p align="center">
	<strong>提供Rancher持续部署能力</strong>
</p>
<p align="center">
	<a target="_blank" href="https://ylyue.cn/">
		<img src="https://img.shields.io/badge/文档-yue-blue.svg?style=flat-square" alt="yue-library官网">
	</a>
	<a target="_blank" href="https://www.apache.org/licenses/LICENSE-2.0.html">
		<img alt="GitHub" src="https://img.shields.io/github/license/yue-open/yue-open-devops-deploy?style=flat-square">
	</a>
	<a target="_blank" href="https://gitee.com/yue-open/yue-open-devops-deploy">
		<img src='https://gitee.com/yue-open/yue-open-devops-deploy/badge/star.svg?theme=dark' alt='gitee star'>
	</a>
	<a target="_blank" href='https://github.com/yue-open/yue-open-devops-deploy'>
		<img alt="GitHub Repo stars" src="https://img.shields.io/github/stars/yue-open/yue-open-devops-deploy?style=social">
	</a>
	<a target="_blank" href="https://github.com/yue-open/yue-open-devops-deploy/issues">
		<img alt="GitHub issues" src="https://img.shields.io/github/issues/yue-open/yue-open-devops-deploy?style=flat-square">
	</a>
</p>
<p align="center">
	-- 主页：<a href="https://ylyue.cn">https://ylyue.cn/</a> --
</p>
<p align="center">
	-- QQ群：<a href="https://jq.qq.com/?_wv=1027&k=5WI2Vbb">883630899</a> --
</p>

-------------------------------------------------------------------------------

# yue-open-devops-deploy
提供Rancher持续部署能力，通知Rancher进行工作负载重新部署（用于docker镜像版本更新后，在Pod配置不变的情况下，更新拉取最新（或指定版本）的镜像）。

yue-open-devops-deploy基于yue-library开发，所有配置项皆可通过docker `-e` 环境变量注入

## docker启动
### 配置项说明
|<div style="width:250px">配置项</div>					|必填	|<div style="width:350px">配置项说明</div>																												|示例配置																					|
|--														|--		|--																																						|--																							|
|`server.port`											|否		|容器内部端口号																																			|默认：9999																					|
|`yue.devops.deploy.bearer-tokens`						|是		|在Rancher UI中可创建API密钥（`bearerToken`） <br> ，用于调用Rancher API（格式：`key: value`）<br> ，在发起 `/devops/redeploy` 请求时需要用到，可填多个	|`dev: Bearer token-47f49:s8jvl9hcvrcg9r9jcrft7cgdzf2qrngfm4s9sf6xs75qtx7zhhb5xg`			|
|`yue.devops.deploy.yue-open-devops-deploy-workload-url`|否		|`yue-open-devops-deploy`在Rancher UI中的工作负载访问地址，用于在发起`/devops/redeploy`请求出现异常时，将在钉钉通知中添加此地址（方便排查异常）			|https://192.168.3.50/p/c-jrhf8:p-rg7dh/workload/deployment:default:yue-open-devops-deploy	|
|`yue.devops.deploy.dingtalk-devops-robot-webhook`		|否		|钉钉DevOps机器人Webhook																																|https://oapi.dingtalk.com/robot/send?access_token=**										|
|`yue.devops.deploy.dingtalk-devops-robot-sign-secret`	|否		|钉钉DevOps机器人密钥																																	|SECcb73a2de6d5176a2226df1157413f1d207e380f024351c04a46f1850dde14b22						|
|`yue.devops.deploy.dingtalk-at-mobiles`				|否		|钉钉通知@群成员手机号																																	|18511111111（多个`,`分割）																	|

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

|<div style="width:110px">参数名</div>	|必填	|<div style="width:300px">参数作用说明</div>									|参数值示例																					|
|--										|--		|--																				|--																							|
|workloadApiUrl							|是		|工作负载的ApiUrl（Rancher2.5以前可在工作负载中直接查看，Rancher2.6+见下述详解）|https://192.168.3.50/v3/project/c-nc2j5:p-rfgdc/workloads/deployment:pretest-sc-mdp:mdp-log|
|bearerTokenName						|是		|配置在yue-open-devops-deploy中，所对应的bearerToken名							|dev																						|
|imageTag								|否		|镜像版本																		|2.0.1																						|

完整请求示例：
```shell
curl -X PUT http://192.168.0.11:9999/devops/redeploy -d 'workloadApiUrl=https://192.168.3.50/v3/project/c-nc2j5:p-rfgdc/workloads/deployment:pretest-sc-mdp:mdp-log&bearerTokenName=dev&imageTag=2.0.1
```

环境变量请求示例（如：云效等场景）：
```shell
curl -X PUT ${yueOpenRedeployUrl} -d `workloadApiUrl=`${workloadApiUrl}'&bearerTokenName='${bearerTokenName}'&imageTag='${DATETIME}
```

### workloadApiUrl请求参数详解
> Rancher2.x开始，默认的API端点为v3版本，<br>
> 2.5之前可在工作负载中直接查看每个工作负载的API端点，<br>
> 2.6之后虽然还是使用的v3 API，但Rancher UI中已经取消了每个工作负载API端点查看按钮（原因：普通用户很少用到），虽然按钮已取消，但功能任然可用，<br>
> 因此2.x版本都可以通过API入口端口，逐一查看各自工作负载的API端点，当然你也可以不用了解这些，通过API端点统一的路径规则，替换为自己的`workloadApiUrl`。

workloadApiUrl示例：`https://192.168.3.50/v3/project/c-nc2j5:p-rfgdc/workloads/deployment:pretest-sc-mdp:mdp-log`

替换规则：`https://${rancherUrl}/v3/project/${clusterId}:${projectId}/workloads/deployment:${namespaceName}:${deploymentName}`

替换规则变量详解：

|变量				|说明																								|示例				|
|--					|--																									|--					|
|`rancherUrl`		|Rancher UI地址（不是默认端口时需要带端口）															|192.168.3.52:8080	|
|`clusterId`		|集群ID（2.5在集群、项目等列表界面点击查看API获得，<br>2.6在集群、项目等列表界面点击查看YAML获得）	|c-nc2j5			|
|`projectId`		|项目ID（2.5在项目列表界面点击查看API获得，<br>2.6在项目列表界面点击查看YAML获得）					|p-rfgdc			|
|`namespaceName`	|命名空间名称																						|pretest-sc-mdp		|
|`deploymentName`	|工作负载名称																						|mdp-log			|

project API端点地址示例：`https://192.168.3.50/v3/project`
- 你也可以访问project API端点，查看`clusterId`、`projectId`
- 当然你也可以在里面查找到workload API端点
- project API端点中的`data[0].id`属性（data是个数组），就是`clusterId:projectId`

project API端点浏览器访问响应示例（已做精简）：
```json
{
    "type": "collection",
    "links": {},
    "createTypes": {},
    "actions": {},
    "pagination": {},
    "sort": {},
    "filters": {},
    "resourceType": "project",
    "data": [
        {
            "actions": {},
            "annotations": {},
            "baseType": "project",
            "clusterId": "c-m-8w6456qc",
            "id": "c-m-8w6456qc:p-6t9fh",
            "links": {
                "replicaSets": "…/v3/projects/c-m-8w6456qc:p-6t9fh/replicasets",
                "replicationControllers": "…/v3/projects/c-m-8w6456qc:p-6t9fh/replicationcontrollers",
                "secrets": "…/v3/projects/c-m-8w6456qc:p-6t9fh/secrets",
                "self": "…/v3/projects/c-m-8w6456qc:p-6t9fh",
                "serviceAccountTokens": "…/v3/projects/c-m-8w6456qc:p-6t9fh/serviceaccounttokens",
                "serviceMonitors": "…/v3/projects/c-m-8w6456qc:p-6t9fh/servicemonitors",
                "workloads": "…/v3/projects/c-m-8w6456qc:p-6t9fh/workloads"
            },
            "name": "dev2",
            "namespaceId": null,
            "state": "active",
            "type": "project",
            "uuid": "0b067557-989b-4ee0-9f59-93d662ececdb"
        }
    ]
}
```

点击`data.links.workloads`的url，可用进入workloads端点，找到对应的工作负载，便可获得`workloadApiUrl`

## 版本历史
### 1.1.1
- 钉钉通知加入imageTag，方便docker镜像版本追溯

### 1.1.0
yodd加入yue-open项目集后，正式推出的第一个规范化版本：
- 优化API接口传参
- 优化环境变量参数命名
- 重构文档与示例

### 历史推荐版本
[1.0.2](https://gitee.com/yue-open/yue-open-devops-deploy/tree/master/docs)