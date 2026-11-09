# TaskBoard — Spring Boot 3 + MyBatis-Plus 容器化示例项目

一个**能真跑**的最小业务系统,用来练容器化 + CI/CD,顺手把国内主流的分层写法讲清楚。

技术栈:Java 17 · Spring Boot 3.3.5 · **MyBatis-Plus 3.5.7** · MySQL 8 · Redis 7 · Maven · Docker

---

## 一、目录结构

```
taskboard/
├── src/main/java/com/demo/taskboard/
│   ├── TaskboardApplication.java
│   ├── common/                     # 通用能力
│   │   ├── ApiResponse.java        # 统一响应体
│   │   ├── PageResult.java         # 分页结果(与持久层解耦)
│   │   ├── ErrorCode.java
│   │   ├── BusinessException.java
│   │   └── GlobalExceptionHandler.java
│   ├── config/
│   │   ├── MybatisPlusConfig.java  # 分页插件
│   │   └── MyMetaObjectHandler.java# 时间字段自动填充
│   ├── controller/
│   │   ├── HomeController.java
│   │   ├── TaskController.java
│   │   └── CategoryController.java
│   ├── service/
│   │   ├── TaskService.java
│   │   ├── CategoryService.java
│   │   └── impl/{TaskServiceImpl, CategoryServiceImpl}.java
│   ├── mapper/                     # 数据访问
│   │   ├── TaskMapper.java
│   │   └── CategoryMapper.java
│   ├── domain/                     # 实体
│   │   ├── Task.java
│   │   ├── Category.java
│   │   └── TaskStatus.java
│   ├── dto/
│   │   ├── TaskCreateRequest.java
│   │   ├── TaskUpdateRequest.java
│   │   ├── TaskResponse.java
│   │   ├── TaskDetailVO.java       # 多表关联的返回对象
│   │   ├── CategoryStatVO.java     # 聚合统计的返回对象
│   │   └── TaskStats.java
│   └── cache/
│       ├── TaskCacheStore.java
│       └── RedisTaskCacheStore.java
├── src/main/resources/
│   ├── application.yml
│   ├── schema.sql                  # 建表 + 初始化数据(幂等)
│   └── mapper/
│       ├── TaskMapper.xml          # 多表关联 + resultMap
│       └── CategoryMapper.xml      # GROUP BY 聚合
├── src/test/java/...
├── Dockerfile
├── docker-compose.yml
├── settings.xml
└── .github/workflows/
    ├── ci.yml                      # 持续集成
    └── cd.yml                      # 持续部署(自托管 Runner)
```

**依赖方向严格单向**:`controller → service → mapper`。

---

## 二、为什么是 MyBatis-Plus

| 维度 | MyBatis-Plus | JPA / Hibernate |
|------|--------------|-----------------|
| SQL 控制 | 自己写,完全可控 | 自动生成,复杂查询难调 |
| 性能排查 | 看自己写的 SQL 和执行计划 | 先看它生成了什么,N+1 坑多 |
| 国内岗位 | 绝大多数公司主力 | 外企、部分互联网 |
| 面试高频 | `#{}` vs `${}`、动态 SQL、分页插件、resultMap | N+1、懒加载、脏检查 |

---

## 三、快速开始

### 先确认 Maven 可用

系统 `PATH` 里还排着旧的 3.5.0,直接敲 `mvn` 会用到旧版(Spring Boot 3 要求 3.6.3+)。

**要么**删掉「系统变量 Path」里的 `D:\mustAPP\maven\apache-maven-3.5.0\bin`,重开终端;

**要么**用完整路径:`"D:/mustAPP/maven/apache-maven-3.9.16/bin/mvn.cmd" -v`

用 IDEA 的话不用管,IDEA 自带的 Maven 能构建。

### 方式 A:连已有的 dev-env

```bash
cd taskboard
mvn clean spring-boot:run
```

### 方式 B:项目自带数据库(容器版,端口 8081)

```bash
cd taskboard
docker compose up -d --build
docker compose ps
```

容器版映射到 **8081**,本地 jar 的 8080 可以同时跑,互不冲突。

### 验证

```bash
# 本地 jar
curl http://localhost:8080/actuator/health

# 容器版
curl http://localhost:8081/actuator/health
```

---

## 四、接口清单

统一前缀 `/api/v1`,统一响应体 `{ "code": 0, "message": "ok", "data": {...} }`。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 服务信息与接口导航 |
| POST | `/api/v1/tasks` | 创建任务(可带 `categoryId`) |
| GET | `/api/v1/tasks/{id}` | 详情(走 Redis 缓存) |
| GET | `/api/v1/tasks` | 简单分页(MP 通用 CRUD) |
| GET | `/api/v1/tasks/with-category` | **多表关联分页**(自定义 XML) |
| PUT | `/api/v1/tasks/{id}` | 更新(失效缓存) |
| DELETE | `/api/v1/tasks/{id}` | 删除 |
| GET | `/api/v1/tasks/stats` | 状态统计 |
| POST | `/api/v1/tasks/{id}/views` | 浏览量 +1(Redis 计数器) |
| GET | `/api/v1/categories` | 分类列表 |
| GET | `/api/v1/categories/stats` | **各分类任务数**(GROUP BY) |

### 多表查询支持的参数

```
GET /api/v1/tasks/with-category
    ?keyword=xxx          标题模糊匹配
    &status=TODO          状态过滤
    &categoryId=4         分类过滤
    &sort=priority        排序字段,仅 id / created_at / priority
    &direction=asc        排序方向
    &page=1&size=10       分页
```

### 跑一遍

```bash
# 关联查询:任务 + 分类名
curl "http://localhost:8080/api/v1/tasks/with-category?page=1&size=5"

# 只看"工作"分类
curl "http://localhost:8080/api/v1/tasks/with-category?categoryId=4"

# 按优先级升序
curl "http://localhost:8080/api/v1/tasks/with-category?sort=priority&direction=asc"

# 分类任务数统计
curl http://localhost:8080/api/v1/categories/stats
```

---

## 五、多表关联查询(XML 写法,重点)

### 5.1 resultMap 的 association + notNullColumn

```xml
<resultMap id="TaskDetailMap" type="com.demo.taskboard.dto.TaskDetailVO">
    <id property="id" column="id"/>
    <result property="title" column="title"/>
    ...
    <association property="category"
                 javaType="com.demo.taskboard.domain.Category"
                 notNullColumn="category_id">
        <id property="id" column="category_id"/>
        <result property="name" column="category_name"/>
    </association>
</resultMap>
```

**`notNullColumn` 是关键**:左连接没匹配到分类时,如果不写这一项,MyBatis 会 `new` 一个所有字段为 null 的 `Category` 塞进去,业务代码判 `category != null` 会失效,很容易 NPE。写了之后,`category_id` 为 null 就直接给 `null`。

### 5.2 `#{}` 与 `${}` 的区别

| 写法 | 编译结果 | 安全性 | 用途 |
|------|----------|--------|------|
| `#{keyword}` | `?` 占位符,PreparedStatement 绑定 | 安全,天然防注入 | 所有参数值 |
| `${orderBy}` | 字符串直接拼进 SQL | **有注入风险** | 只能拼表名/列名/排序关键字 |

本项目里唯一用 `${}` 的地方是排序:

```xml
ORDER BY ${orderBy}
```

值不是用户原样传入的,而是服务层白名单校验后重新拼出来的:

```java
private static final Set<String> SORTABLE_COLUMNS = Set.of("id", "created_at", "priority");

private String buildOrderBy(String sort, String direction) {
    String column = (sort == null ? "" : sort.trim().toLowerCase());
    if (!SORTABLE_COLUMNS.contains(column)) {
        column = "id";                       // 不在白名单,静默回退
    }
    String dir = "asc".equalsIgnoreCase(direction) ? "ASC" : "DESC";
    return column + " " + dir;
}
```

实测传 `sort=id;DROP TABLE tasks` 时,服务会回退到 `id DESC`,SQL 正常执行、表也没事。

**面试答法**:`#{}` 是预编译占位符,参数走 `PreparedStatement`,不会改变 SQL 结构;`${}` 是文本替换,会和 SQL 拼在一起。凡是用 `${}`,必须保证值来自白名单或代码常量,绝不能直接接用户输入。

### 5.3 自定义 SQL 也能用 MP 的分页

Mapper 方法第一个参数传 `Page`,返回 `IPage`,分页插件会自动:
1. 生成 `COUNT` 语句查总数
2. 给原 SQL 追加 `LIMIT`

```java
IPage<TaskDetailVO> selectTaskPage(Page<TaskDetailVO> page,
                                   @Param("keyword") String keyword,
                                   @Param("status") TaskStatus status,
                                   @Param("categoryId") Long categoryId,
                                   @Param("orderBy") String orderBy);
```

不用自己写两条 SQL。

### 5.4 动态 SQL

```xml
<where>
    <if test="keyword != null and keyword != ''">
        AND t.title LIKE CONCAT('%', #{keyword}, '%')
    </if>
    <if test="status != null">
        AND t.status = #{status}
    </if>
    <if test="categoryId != null">
        AND t.category_id = #{categoryId}
    </if>
</where>
```

`<where>` 会自动去掉开头多余的 `AND`,不用手写 `WHERE 1=1`。

---

## 六、几个值得讲的设计点

**1. 分页插件必须显式注册**

不注册 `PaginationInnerInterceptor`,`selectPage` 会**静默返回全量数据**,不报错。最阴的一个坑。

**2. 时间字段自动填充**

`@TableField(fill = FieldFill.INSERT / INSERT_UPDATE)` + `MyMetaObjectHandler`。

注意 `strictUpdateFill` **只在字段为 null 时填充**。更新时实体是从库里查出来的,`updatedAt` 已有值,所以必须用 `setFieldValByName` 强制覆盖。

**3. 统一响应 + 全局异常**

业务只抛 `BusinessException`,Controller 零 try-catch。

**4. 缓存降级**

`RedisTaskCacheStore` 所有 Redis 操作都包了 try-catch,缓存挂了只打 warn,不影响主流程。

**5. 缓存一致性**

读 Cache-Aside(未命中回填),写"先更库、再删缓存"。

**6. 表结构不依赖 ORM**

`schema.sql` + `CREATE TABLE IF NOT EXISTS`,启动时幂等执行。老库补列用 `information_schema` 判断后动态 `ALTER`。

**7. 分页边界防护**

`page` 最小 1,`size` 限制 1~100,插件侧 `maxLimit(500)`。

---

## 七、测试

```bash
mvn test
mvn verify
```

- `TaskServiceImplTest`:纯 Mockito 单测,不依赖 Spring 容器和数据库
- `TaskControllerTest`:`@WebMvcTest` 切片,验证参数校验与异常映射

---

## 八、容器化与 CI

```bash
docker build -t taskboard:v1 .
docker history taskboard:v1
```

- 多阶段构建:`maven:3.9-eclipse-temurin-17` 构建 → `eclipse-temurin:17-jre` 运行
- `<finalName>app</finalName>` 固定产物名

CI:先 `mvn verify`,通过后再构建镜像推 GHCR(`latest` + `commit sha` 双标签)。

CD(`.github/workflows/cd.yml`):在**自托管 Runner**(你自己的电脑)上执行
`docker pull` 指定 sha 的镜像 → `docker compose up -d --no-build` → 健康检查
`http://localhost:8081/actuator/health`。

详细操作步骤见仓库外的 `CI-CD-实操指引.md`。

---

## 九、搭建过程中真实踩到的坑

**1. 中文乱码,一共三个来源**

| 来源 | 现象 | 解法 |
|------|------|------|
| MySQL 初始化脚本(SQL 文件) | 存进去就是乱码 | 文件开头 `SET NAMES utf8mb4;` |
| Spring 读取 `schema.sql` | 种子数据乱码 | `spring.sql.init.encoding: UTF-8` |
| JDBC 连接串 | 查出来是乱码 | `characterEncoding=utf8` |

判断方法:
```sql
SELECT HEX(LEFT(title,2)) FROM tasks;
-- 正确: E5ADA6(学)    错误: C3A5C2AD(双重编码)
```

**2. 访问不存在的路径返回 500 而不是 404**

Spring Boot 3.2+ 抛 `NoResourceFoundException`,被兜底的 `@ExceptionHandler(Exception.class)` 吞成 500。必须单独接住框架异常:

| 异常 | 应返回 |
|------|--------|
| `NoResourceFoundException` | 404 |
| `MethodArgumentTypeMismatchException` | 400 |
| `HttpMessageNotReadableException` | 400 |
| `HttpRequestMethodNotSupportedException` | 405 |
| `Exception` | 500(放最后) |

**兜底 `Exception` 处理器是安全网,不是垃圾桶。**

**3. 给已有数据的表加 `NOT NULL` 列失败**

`Data truncation: Incorrect datetime value '0000-00-00'`。建表时给默认值 `DEFAULT CURRENT_TIMESTAMP(6)`。

**4. MyBatis-Plus 自动填充对更新不生效**

`strictUpdateFill` 只在字段为 null 时填充。改 `setFieldValByName` 强制覆盖。

**5. Mockito 报 `deleteById 的引用不明确`**

`BaseMapper` 有 `deleteById(Serializable)` 和 `deleteById(T)` 两个重载,`any()` 无法推断。写 `any(Serializable.class)`。

---

## 十、接下来可以加什么

- Flyway / Liquibase 管理表结构版本
- 一对多集合映射(`<collection>`,比如分类下挂任务列表)
- 多条件批量更新、`<foreach>` 动态 IN 查询
- Spring Security + JWT 鉴权
- 分布式锁(Redis `SETNX`)防重复提交
- Testcontainers 做真实数据库集成测试
