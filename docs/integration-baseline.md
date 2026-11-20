# 集成基线与冲突清单

- 整理：stu3（晏思汗）
- 日期：2026-11-20
- 目的：写新功能之前先确定唯一技术基线，避免三人代码无法合并

## 1. 仓库现状

| 位置 | 内容 | 状态 |
|---|---|---|
| main | 物资模块（Material / StockLog / MaterialDao / MaterialService / MaterialServlet + 3 个 JSP + schema.sql / data.sql） | 可用，但登录是 DevLogin 占位 |
| stu1-ruanjingru | 申请模块（5 个类 + 3 个 JSP + temp 替身类） | 未合并 |
| stu3 | 无 | 本地登录已跑通，未推送 |

## 2. 两套基线不一致（不能直接合并）

| 项 | 仓库 | stu3 本地 |
|---|---|---|
| Servlet API | javax.servlet 4.0（build/lib/javax.servlet-api-4.0.1.jar） | jakarta.servlet 6.1（Tomcat 11） |
| 页面目录 | WebContent/ | src/main/webapp/ |
| 源码目录 | src/ | src/main/java/ |
| 根包 | com.club.* | com.clubloan.* |
| 数据库 | club_material | clubloan |
| JSTL | stu1 分支带 jstl-1.2（javax） | 未使用 |

后果：Tomcat 11 不识别 javax 注解，Tomcat 9 不识别 jakarta；两边混用一定 404 或 ClassNotFound。

## 3. 需要决定的一件事

- 方案 A（推荐）：统一 jakarta + Tomcat 11。改动：stu1/stu2 的 servlet import 换成 jakarta；stu1 的 JSTL 1.2 换成 JSTL 3.0，或改成 scriptlet；移除 build/lib 下的 javax jar。
- 方案 B：统一 javax + Tomcat 9。改动：stu3 的登录 / 过滤器 / 流程全部换 import，并安装 Tomcat 9；stu1 的 JSTL 保留。

截止：明天 12:00 前在群里回复 A 或 B。定之前不写新功能。

## 4. 事务问题（审批必须解决）

`MaterialService.reserve / release / borrowOut / giveBack` 内部自己 `getConnection()` 并 `commit()`。审批要做“改申请状态 + 扣库存”的同一事务，直接调用会变成两个事务。

需要：给这四个方法加接收 `Connection` 的重载（事务由 WorkflowService 控制），或 WorkflowService 直接调用 `MaterialDao` + `StockLogDao`。

## 5. 合并顺序与清理项

1. 以 main（stu2）为基线；
2. 合 stu1 分支：删除 `com.club.application.temp.*`；丢弃 `db/schema.sql`，只保留 `sql/`；统一使用 `com.club.common.*`；
3. 合 stu3：按选定基线改 import，统一库名；
4. 删除 `DevLoginServlet` 和临时 `index.jsp`；
5. 全流程联调。
