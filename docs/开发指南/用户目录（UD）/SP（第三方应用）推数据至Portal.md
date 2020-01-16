# SP（第三方应用）推数据至Portal
Portal提供一些UD同步的接口API（所有的API都是遵循SCIM协议的），SP通过调用这些API，可以将数据同步到Portal。SP在调用Portal接口时，必须传递access_token(具体使用方式请查看获取access_token)。以下，我们将常用的API按照 组织机构，账户，组进行分类。

## 前提条件：
### 1.获取Portal-Base-URL
文档中的“Portal-Base-URL”需要替换为当前访问地址的主域，文中接口地址前也都需要替换主域地址。主域为Portal控制台中的用户访问的Portal的sso地址。

### 2.使用access_token调用接口
调用 Portal API接口时，需要先获取access_token，调用接口时传入access_token有两种方式：
- URL值后：URL?access_token={access_token}
- Header里面：Authorization Bearer {access_token}（注意 Bearer与access_token之间的空格）

access_token分为两种类型：
- 以 /api/enduser 开头URL的API使用的access_token，代表的是一个用户登录后的access_token
- 以 /api/application 开头URL的API使用的access_token： 获取方式是向下面的URL进行POST请求，请求将返回json包含access_token：`{Portal-Base-URL}/oauth/token?client_id={client-id}&client_secret={client-secret}&scope=read&grant_type=client_credentials` client_id和client_secret即为创建应用中的API Key和APi Secret

开发者需要根据使用的API明确区分这2种Token：

!> 第一种 access_token 可获得用户信息，适用于需要记录用户操作等信息的场景<br>
第二种 access_token 以服务器作为通信集成，数据变更只能记录到具体应用信息，但大部分UD场景使用的都是这种方式

## 接口列表
以下是关于组织机构操作的API，包括：

- 推送组织机构
- 修改或移动组织机构
- 删除组织机构
- 查询组织机构
- 获取组织机构列表
- 获取根节点组织机构信息
- 获取组织机构的直属子级

以下是关于账户操作的API，包括：

- 推送账户
- 修改或移动账户
- 删除账户
- 获取账户信息
- 查询账户列表

以下是关于组操作的API，包括：

- 推送账户组
- 更新账户组
- 删除账户组

其他：
- 获取应用已经授权的组织机构及账户列表

## 具体接口
[点击查看API文档](https://apizza.net/pro/#/project/e8f5d608348d74235241bf1fba6fc69b/browse)

密码：hy-portal-api-doc