# xsy-spring-sdk
销售易中间件SDK

在项目resources路径下application.yml文件里添加以下配置

oauthconfig:
  domain: https://api-xxx.xiaoshouyi.com
  userName: username
  password: password
  securityCode: securityCode
  clientId: clientId
  clientSecret: clientSecret
  socketTimeout: 10000
  connectionTimeout: 12000
  readTimedOutRetry: 10
