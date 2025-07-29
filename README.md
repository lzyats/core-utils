###### 1、maven修改settings.xml，增加如下配置
```
<mirror>
    <id>q3z3-boot-tools-maven</id>
    <name>maven</name>
    <mirrorOf>q3z3-boot-tools-maven</mirrorOf>
    <url>https://q3z3-maven.pkg.coding.net/repository/boot-tools/maven/</url>
</mirror>
```

###### 2、pom.xml增加依赖
```
<!-- 工具包 start -->
<dependency>
    <groupId>com.platform</groupId>
    <artifactId>core-utils</artifactId>
    <version>2.0.0</version>
</dependency>
<!-- 工具包 end -->
```

###### 3、缓存配置ehcache
```
platform:
  # 缓存地址 示例（ Windows配置E:/platform/cache，Linux配置 /data/cache）
  ehcachePath: /data/cache

# Spring配置
spring:
  # 缓存
  cache:
    type: ehcache
    ehcache:
      config: classpath:/ehcache.xml
```

###### 4、缓存配置redis
```
spring:
  # redis 配置
  redis:
    # 开关
    enabled: Y
    # 地址
    host: 127.0.0.1
    # 端口，默认为6379
    port: 6379
    # 数据库
    database: 0
    # 密码
    password:
    # 超时
    timeout: 5000
    # 连接池
    lettuce:
      pool:
        # 连接池中的最大空闲连接
        max-idle: 128
        # 连接池的最大数据库连接数
        max-active: 128
        # #连接池最大阻塞等待时间（使用负值表示没有限制）
        max-wait: 5000
```

###### 5、过滤器
```
# 过滤器
filter:
  # 版本配置
  version:
    # 版本开关
    enabled: Y
    # 最低版本
    lowest: 1.0.0
  # 设备过滤
  device:
    # 设备开关
    enabled: Y
  # 过滤拦截
  exclude:
    # 过滤请求
    dataList:
      - /
      - /favicon.ico
      - /test/**
  # 升级列表
  upgrade:
    dataList:
      - label: 1.0.0
        value: "{}安卓端、苹果端同步上线发布\n\
                  全新操作界面让眼前焕然一新\n\
                  全新高性能体验\n\
                  聊天消息采用全程点对点加密\n\
                  妈妈再也不用担心我的聊天安全了"
  # 签名过滤
  sign:
    # 签名开关
    enabled: Y
    # 移除前缀，比如/webApi
    prefix: "/webApi"
    # appId
    appId: 2024
    # 私钥
    privateKey: MIICdwIBADANBgkqhkiG9w0BA
    # 公钥
    publicKey: MIICdwIBADANBgkqhkiG9w0BA
  # 禁用拦截
  banned:
    # 过滤请求
    dataList:
      - /mine/setPass
  # xss过滤
  xss:
    # 过滤开关
    enabled: Y
    # 过滤请求
    dataList:
      - /mine/setPass
  # 地址转换
  path:
    # 前缀，默认/webApi
    prefix: "/webApi"
    # 映射map
    dataList:
      "ofyndizx9od5hgo0": "/test/test1"
```
WebMvcConfig.java
```
@Resource
private VersionInterceptor versionInterceptor;

@Resource
private DeviceInterceptor deviceInterceptor;

@Resource
private SignInterceptor signInterceptor;
```
```
/**
 * 自定义拦截规则
 */
@Override
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(versionInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns(AppConstants.PREVIEW);
    registry.addInterceptor(deviceInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns(AppConstants.PREVIEW);
    registry.addInterceptor(signInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns(AppConstants.PREVIEW);
}
```