# 单点登录SSO
## 单点登录（SSO）概述
　　单点登录（SSO），英文全称为 Single Sign On。 SSO 是指在多个应用系统中，用户只需要登录一次，就可以访问所有相互信任的应用系统。IDaaS SSO 服务用于解决同一公司不同业务应用之间的身份认证问题，只需要登录一次，即可访问所有添加的应用。此服务可以涵盖用户在公有云和私有云中的双重需求。<br>
　　本文档默认你已经拥有了开发者权限，并已经阅读了 准备开发 文档 和 应用管理 文档。如果需要分配新的开发者权限的话，请联系 IT 管理员进行授权操作。
![SP发起 -> 单点登录示例](SP发起_files/1.png)
<center>SP发起 -> 单点登录示例</center>

## 单点登录场景-SP发起
在单点登录实现过程中，现已满足以下登录场景，包括：
1. [SP发起](#SP发起)
2. [接口后置](#接口后置)
3. [登录跳转](#登录跳转)

### SP发起
SP 发起主要应用于 SSO 后可以跳回发起 SSO 的应用页面。下面以 SAML 和 JWT 的实现为例来阐述 SP 发起的单点登录流程。

#### 1、访问应用页面
用户到 SP 应用页面，会在 SP 通过 Redirect 或 POST 提交一个某种认证协议如 SAML / CAS 的挑战请求， 在 IDaaS 登录后， 再跳转回 SP 作为应答， 实现统一认证，如下图所示：
![SP 发起单点登录流程](SP发起_files/2.png)
<center>SP 发起单点登录流程</center><br>

以 SAML 协议 SP 发起的单点登录为例：

（1）用户访问 SP 资源页面

（2）浏览器向 SP 请求资源

（3）SP 生成 SAML AuthnRequest 请求，其中包含当前 URL 到 RelayState，并返回给浏览器

（4）浏览器携带 SAML AuthnRequest 请求，访问定义好的 IDaaS 中 SP 发起的 SSO URL

（5）IDaaS 验证 SAML AuthnRequest，若该用户已登录直接跳至步骤（9），否则继续步骤（6）

（6）重定向到 IDaaS 登录页面

（7）用户输入 IDaaS 的账户和密码

（8）浏览器携带账号密码请求登录 IDaaS

（9）登录 IDaaS 后， IDaaS 分析 SAML AuthnRequest 中的 SP 应用信息，并获取更多用户信息，然后组合生成响应包含 RelayState 的 SAML Response Token。

（10）返回 SAML Response Token 数据给浏览器

（11）浏览器进行跳转，携带 SAML Response Token，访问 SP ACS URL

（12）SP 利用公钥验证 SAML Response Token

（13）校验成功后，创建 session 会话，从 RelayState 中取出开始时发起的 URL，返回给浏览器

（14）浏览器访问资源页面

（15）SP 返回资源页面

（16）用户登录进 SP 资源页面

#### 2、访问 SP 资源
用户访问 SP 资源，SP 会重定向到 IDaaS 的 SSO 地址，在 IDaaS 认证通过后，会向 SP 返回 SP 发起登录的页面并携带请求事件redirect_url参数，用户登录后 SP会向浏览器返回并显示用户访问的资源页面，如下图所示：
![SP 发起单点登录流程-deeplinking](SP发起_files/3.png)
<center>SP 发起单点登录流程-deeplinking</center>

以 JWT 协议 SP 发起的单点登录为例：

（1）用户访问 SP 资源页面

（2）浏览器向 SP 请求资源

（3）SP 检测登录，若该用户已登录直接跳至步骤（9），未登录继续到（4）

（4）SP 把当前页面地址置入 redirect_url 参数，并重定向到 SP SSO URL

（5）重定向到 IDaaS 登录页面

（6）用户输入 IDaaS 的账户和密码进行登录

（7）浏览器携带账号密码请求登录 IDaaS

（8）IDaaS 对账户进行认证并通过

（9）返回 JWT SSO URL，在 URL 中包含了 id_token 和 target_url

（10）浏览器携带上述参数跳转到 redirect_url

（11）SP 通过 PublicKey 对 id_token 进行校验

（12）校验成功后，创建 session 会话，向浏览器返回用户访问的 redirect_url 地址并进行跳转

（13）用户查看到之前访问的页面资源

### 接口后置
SP 登录时使用 IDaaS 进行接口后置认证登录场景，如下图所示：
![接口后置单点登录流程](SP发起_files/4.png)
<center>接口后置单点登录流程</center><br>

（1）用户访问 SP1 登录页面

（2）浏览器请求 SP1 登录页面

（3）SP 1 返回其登录页面

（4）用户看到 SP1 登录页面

（5）用户输入用户名密码进行登录

（6）浏览器携带用户名、密码向 SP1 发起登录请求

（7）SP 1 使用 IDaaS 接口进行认证，将用户名、密码传递给 IDaaS 进行认证登录

（8）IDaaS 进行认证后，生成主 token ，并返回主 token 和应用列表以及用户信息给 SP1

（9）登录 SP1 成功，浏览器获取到 SP1 登录后页面

（10）用户可看到 SP1 登录后页面，可看到显示的应用列表

（11）用户在 SP1 显示的应用列表中点击 SP2 应用图标进行单点登录

（12）浏览器携带主 token 和应用id，向 IDaaS 请求生成子 token

（13）IDaaS 返回 SP2 的子 token 以及 SP2 重定向地址

（14）浏览器携带子 token 访问 SP2 重定向地址

（15）SP2 解析子 token ，验证成功，并返回 SP2 登录后页面

（16）用户看到 SP2 登录后页面， SP2 应用系统登录成功

### 登录跳转
用户到 SP 应用页面，会跳转到 IDaaS 登录页面进行统一认证，登录后，返回一个包含更多用户信息 JWT token 到 SP 页面，包括应用列表和访问一个 SP 应用的子token，从而实现单点登录。如下图所示：
![登录跳转单点登录流程](SP发起_files/5.png)<br>

（1）用户访问 SP1 系统登录页面

（2）浏览器向 SP1 请求登录页面

（3）SP 1 返回重定向地址

（4）浏览器访问重定向地址

（5）IDaaS 返回登录页面

（6）浏览器携带用户提交的用户名密码以及应用id，向 IDaaS 请求登录

（7）IDaaS 本地认证成功后，创建主 session ，并返回应用列表的 JWT token 内含了子 token

（8）浏览器接收信息后，校验并解析出子 token ，向 SP1 请求认证登录

（9）SP 1 获取子 token 并解析后，创建子 session ，并返回登录后页面

（10）用户看到 SP1 登录后页面， SP1 登录成功

（11）用户在 SP1 已成功登录后，访问 SP2 登录页面

（12）浏览器向 SP2 请求登录页面

（13）SP2 返回重定向地址

（14）浏览器访问重定向地址

（15）IDaaS 由于主 session 已创建，可直接根据应用id生成 SP2 系统的子 token ，并返回子 token

（16）浏览器携带子 token ，向 SP2 发起请求

（17）SP2 解析子 token 后，创建子 session 并返回 SP2 登录后页面

（18）用户看到 SP2 登录后页面， SP2 登录成功

## 开发须知
### 开发者须知
`文档中的 “IDaaS-Base-URL” 需要替换为当前访问地址的主域，文中接口地址前也都需要替换主域地址；接口地址中的版本号以当前使用系统版本为准，也可以查看开发者文档中右侧菜单顶部的接口版本。`

用户通过登录 IDaaS 系统，能够在用户的主界面应用列表中看到自身有权限访问的应用列表。用户可以选择一个应用进行单点登录。

单点登录到第三方应用的过程，对于用户来说是透明过程。此过程无需用户填写第三方应用系统的账号信息，而是通过安全的协议交换令牌，直接验证身份进入应用系统。

公有云中的应用普遍支持 <font color=red>`OIDC`</font> ， <font color=red>`OAuth`</font> ， <font color=red>`SAML`</font> 等标准协议。在私有云中，我们也提供了无插件式 SSO （如 CAS 标准和 CAS 改良）和插件式 SSO （如 JWT 标准协议）两种方式以方便企业内部应用向 IDaaS 的迁移。

业务应用集成到 IDaaS 后， IDaaS 会提供针对应用的一些接口，包括 账号关联，修改账户信息，解锁账户，删除账户等操作。针对用户或组织机构的操作接口，可以参考 [<font color=red>用户目录（UD）</font>](开发指南/用户目录（UD）/用户目录（UD）同步概述.md) 文档。

已经支持语言：<font color=red>`PHP`</font>，<font color=red>`JAVA`</font>，<font color=red>`.NET`</font>，<font color=red>`Python`</font>。

## 无插件式 SSO （ CAS 标准）
### 原理和协议
从结构上看，CAS 包含两个部分： <font color=red>`CAS Server`</font> 和 <font color=red>`CAS Client`</font> 。 CAS Server 需要独立部署，主要负责对用户的认证工作； CAS Client 负责处理对客户端受保护资源的访问请求，需要登录时，重定向到 CAS Server。

下图是标准 CAS 最基本的协议过程：
![无插件式 SSO ( CAS 标准)原理](SP发起_files/6.png)
<center>无插件式 SSO ( CAS 标准)原理</center><br>

CAS Client 与受保护的客户端应用部署在一起，以 <font color=red>`Filter`</font> 方式保护受保护的资源。对于访问受保护资源的每个 Web 请求， CAS Client 会分析该请求的 Http 请求中是否包含 <font color=red>`Service Ticket`</font> ，如果没有，则说明当前用户尚未登录，于是将请求重定向到指定好的 <font color=red>`CAS Server`</font> 登录地址，并传递 Service （也就是要访问的目的资源地址），以便登录成功过后转回该地址。

用户在 上图流程中的 <font color=red>`第 3 步`</font> 输入认证信息，如果登录成功， <font color=red>`CAS Server`</font> 随机产生一个相当长度、唯一、不可伪造的 <font color=red>`Service Ticket`</font> ，并缓存以待将来验证，之后系统自动重定向到 Service 所在地址，并为客户端浏览器设置一个 Ticket Granted Cookie（TGC）， <font color=red>`CAS Client`</font> 在拿到 Service 和新产生的 <font color=red>`Ticket`</font> 过后，在 <font color=red>`第 5，6 步`</font> 中与 CAS Server 进行身份核实，以确保 Service Ticket 的合法性。

而在 IDaaS 中， 一个 CAS （标准）应用实现了标准的 CAS 流程。它充当一个 CAS Server的角色。当 CAS client 决定使用 CAS （标准）应用作为 CAS Server 时。在登录认证时需要使用 IDaaS 系统中公司的主账号，密码进行认证。

### IDaaS 中添加 CAS （标准）应用
`CAS 标准应用目前只能由 IT 管理员 在应用添加菜单中添加。下面是 IT 管理员的应用添加流程。如果希望使用 CAS（标准）单点登录，可以请管理员进行协助添加和配置。`<br>
1、以IT管理员身份登录 IDaaS ，点击添加应用,找到 CAS （标准），点击 添加应用
![添加 CAS（标准）应用第 1 步](SP发起_files/7.png)
<center>添加 CAS（标准）应用第 1 步</center><br>

2、填写 <font color=red>`serverNames`</font> ,即每一个 CAS Client 的名称，一行一个，如有多个换行添加即可，点击保存。
![添加 CAS（标准）应用第 2 步](SP发起_files/8.png)
<center>添加 CAS（标准）应用第 2 步</center><br>

3、启动该应用，并查看该应用详情，这里可以对应用进行一系列操作。 client里面配置使用。
![添加 CAS （标准）应用第 3 步](SP发起_files/9.png)
<center>添加 CAS （标准）应用第 3 步</center><br>

4、点开应用详情，主要注意 <font color=red>`CAS Login URL`</font> 和 <font color=red>`CAS Server URL Prefix`</font> 两个参数以便接下来在 CAS client里面配置使用。
![添加 CAS （标准）应用第 4 步](SP发起_files/10.png)
<center>添加 CAS （标准）应用第 4 步</center>

### 在 CAS Client 中使用 CAS（标准）应用作为 CAS Server
在 CAS client 中配置刚刚添加的 CAS （标准）应用，找到 CAS Client 的 web.xml 文件，将以下参数配置为 CAS （标准）应用中的参数值。

1、修改 <font color=red>`CAS ServerLoginUrl`</font> 为 CAS （标准）应用中的 <font color=red>`CAS Login Url`</font><br>
修改 CAS ServerLoginUrl

2、修改 <font color=red>`CAS ServerUrlPrefix`</font> 为 CAS （标准）应用中的 <font color=red>`CAS Server URL Prefix`</font><br>
修改 CAS ServerUrlPrefix

3、其他的 CAS Client 应用请参照以上配置