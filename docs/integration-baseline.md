# 集成基线与冲突清单

- 整理：stu3（晏思汗）
- 日期：2026-11-20
- 状态：**已决定 —— 方案 B（javax + Tomcat 9）**
- 目的：记录基线决定与合并顺序，避免三人代码无法合并

## 1. 仓库现状

| 位置 | 内容 | 状态 |
|---|---|---|
| main | 物资模块（Material / StockLog / MaterialDao / MaterialService / MaterialServlet + 3 个 JSP + schema.sql / data.sql） | 可用，但登录是 DevLogin 占位 |
| stu1-ruanjingru | 申请模块（5 个类 + 3 个 JSP + temp 替身类） | 未合并 |
| stu3-yansihan | 本文件 | 已推送，待合并 |

## 2. 基线决定（2026-11-20）

团队确认：**统一使用仓库现有的 javax + Tomcat 9 基线**，stu1 / stu2 的代码不改 import。

| 项 | 统一后的取值 |
|---|---|
| Servlet API | javax.servlet 4.0 |
| 容器 | Tomcat 9（9.0.x） |
| 页面目录 | WebContent/ |
| 源码目录 | src/ |
| 根包 | com.club.* |
| 数据库 | club_material |
| JSP 写法 | scriptlet；stu1 的 JSTL 1.2 保留 |
| web.xml | 3.1 / 4.0，javaee 命名空间 |

## 3. 对三个人的影响

- **stu1**：合并前删除 `com.club.application.temp.*`，丢弃 `db/` 目录，只保留 `sql/`；JSTL 1.2 继续使用。
- **stu2**：main 不动；需要给库存四个方法加接收 `Connection` 的重载（见第 4 节）。
- **stu3**：本地 jakarta 代码不合入；按第 2 节重写为 `com.club.*` + `javax.servlet`，库名改 `club_material`；本地 Tomcat 11 工程只作参考。

## 4. 事务问题（审批必须解决）

`MaterialService.reserve / release / borrowOut / giveBack` 内部自己 `getConnection()` 并 `commit()`。审批要做“改申请状态 + 扣库存”的同一事务，直接调用会变成两个事务。

需要：给这四个方法加接收 `Connection` 的重载（事务由 WorkflowService 控制），或 WorkflowService 直接调用 `MaterialDao` + `StockLogDao`。

## 5. 合并顺序与清理项

1. 以 main（stu2）为基线；
2. 合 stu1 分支：删除 temp 类；丢弃 `db/schema.sql`，保留 `sql/`；统一使用 `com.club.common.*`；
3. 合 stu3：按方案 B 的包名、目录与库名；
4. 删除 `DevLoginServlet` 和临时 `index.jsp`；
5. 全流程联调。

## 6. 待办

- [ ] stu3 安装 Tomcat 9 并导入工程
- [ ] stu3 搬登录 / 过滤器（javax）
- [ ] stu1 清理并合并分支
- [ ] stu2 给库存方法加 Connection 重载
