# IDaaS推数据到SP(第三方应用)
此同步方式，需要SP提供接口API。IDaaS通过调用这些API将数据同步到SP。其中需要注意的就是这个接口需要业务系统根据我们提供的字段名称和错误返回码来进行开发，此接口的开发需要提供Basic协议或者Oauth协议来保护接口。现在，我们将常用的按照组织机构，账户，组来进行分类.

IDaaS请求SP所有的地址都基于在IT管理员应用处的SCIM配置的地址基础上,以不同的请求方式(POST,PUT,DELETE)请求该地址。

## 接口列表
以下是关于组织机构操作的API，包括：
- IDaaS给SP添加组织机构
- IDaaS给SP修改或移动组织机构
- IDaaS给SP删除组织机构

以下是关于账户操作的API，包括：

- IDaaS给SP添加账户
- IDaaS给SP修改或移动账户
- IDaaS给SP删除账户

以下是关于组操作的API，包括：

- IDaaS给SP添加账户组
- IDaaS给SP删除账户组

## 前言
下面的是账户同步的第二种方式，即IDaaS推送到业务系统(SP)中，目前IDaaS仅提供一个给SP添加账户功能，其中需要注意的就是这个接口需要业务系统根据我们提供的字段名称和错误返回码来进行开发。

`IDaaS请求SP所有的地址都基于在IT管理员应用处的SCIM配置的地址基础上,以不同的请求方式请求该地址。`

### IDaaS给SP添加组织机构
IDaaS通过API给业务系统添加一个组织机构。此接口可以与“SP推至IDaaS的推送组织机构接口”共用一个实体类。

Request URI: /api/bff/v1.2/developer/scim/organization `POST`  <font color=green>`REST`</font>

Content-Type: application/json

业务系统需要根据我们提供的字段标准来开发接口，如下所示:

参数说明：

|参数名				|参数值				|备注																											|
|--	|--	|--	|
|organization		|{organization}		|组织机构的名称																									|
|parentUuid			|{parentUuid}		|所属父级组织机构的uuid或外部ID																					|
|rootNode			|{rootNode}			|是否是根节点																									|
|organizationUuid	|{organizationUuid}	|本组织机构的uuid或外部ID																						|
|manager			|{manager}			|组织机构的管理者,value是管理者账户的外部ID,displayName是用户名,管理者可为空									|
|regionId			|{regionId}			|组织机构所属的区域id,type为SELF_OU(自建组织机构)时有可能会有值,可为空,type为DEPARTMENT(“自建部门”)不会出现值	|
|type				|{manager}			|SELF_OU(自建组织机构)或DEPARTMENT(“自建部门”)																|
|levelNumber		|{levelNumber}		|部门排序号																										|
|extendField		|{extendField}		|扩展字典,attributes为系统定义扩展字段																			|