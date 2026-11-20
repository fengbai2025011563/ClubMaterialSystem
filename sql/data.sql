-- ============================================================
-- 社团活动物资借还系统 · 种子数据
-- 负责人：刘艳（物资维护与库存）
-- 前置：先执行 schema.sql 建表
-- 说明：所有账号初始密码都是 123456（数据库里存的是它的 SHA-256 摘要）
-- ============================================================

USE club_material;
SET NAMES utf8mb4;

DELETE FROM stock_logs;
DELETE FROM application_items;
DELETE FROM loan_applications;
DELETE FROM materials;
DELETE FROM users;

-- ------------------------------------------------------------
-- 1. 账号：一个管理员 + 三名小组成员
--    123456 的 SHA-256 = 8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92
-- ------------------------------------------------------------
INSERT INTO users (id, username, password, real_name, role, status) VALUES
(1, 'admin',      '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '系统管理员', 'ADMIN',  1),
(2, '2024011275', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '晏思汗',     'MEMBER', 1),
(3, '2025011563', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '刘艳',       'MEMBER', 1),
(4, '2025011558', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '阮靖茹',     'MEMBER', 1);

-- ------------------------------------------------------------
-- 2. 物资：八件常用社团物资
--    每行后面的三个数字依次是 可用 / 预留 / 借出，相加必须等于总量
--      (15 + 3 + 2 = 20)  (8 + 2 + 2 = 12)  (4 + 1 + 1 = 6)  (6 + 0 + 2 = 8)
--      (25 + 3 + 2 = 30)  (100)             (8 + 2 + 0 = 10) (2)
-- ------------------------------------------------------------
INSERT INTO materials
(id, name, category, unit, total_qty, available_qty, reserved_qty, borrowed_qty, status, remark) VALUES
(1, '折叠帐篷',   '户外',   '顶', 20,  15,  3, 2, 'ACTIVE',   '3 号储物间 A 架'),
(2, '折叠长桌',   '家具',   '张', 12,   8,  2, 2, 'ACTIVE',   '3 号储物间 B 架'),
(3, '便携投影仪', '设备',   '台',  6,   4,  1, 1, 'ACTIVE',   '社团办公室带锁柜'),
(4, '蓝牙音箱',   '设备',   '台',  8,   6,  0, 2, 'ACTIVE',   '社团办公室带锁柜'),
(5, '露营灯',     '户外',   '个', 30,  25,  3, 2, 'ACTIVE',   '3 号储物间 A 架'),
(6, '一次性雨衣', '消耗品', '件', 100, 100, 0, 0, 'ACTIVE',   '前台纸箱'),
(7, '活动横幅',   '消耗品', '条', 10,   8,  2, 0, 'ACTIVE',   '物料柜第二层'),
(8, '老式白板',   '家具',   '块',  2,   2,  0, 0, 'INACTIVE', '轮子损坏，暂停外借');

-- ------------------------------------------------------------
-- 3. 期初库存流水
--    这里的每一条都是为了让"流水加总 = 当前库存"成立，方便演示对账。
--    顺序：先入库（ADJUST），再占用（RESERVE），最后借出（BORROW）
-- ------------------------------------------------------------
INSERT INTO stock_logs
(material_id, application_id, change_type, delta_available, delta_reserved, delta_borrowed, delta_total, operator_id, note, created_at) VALUES
-- 1 折叠帐篷：20 → 占用 5 → 借出 2
(1, NULL, 'ADJUST',  20,  0, 0, 20, 1, '期初入库',       DATE_SUB(NOW(), INTERVAL 30 DAY)),
(1, NULL, 'RESERVE', -5,  5, 0,  0, 1, '期初已审批占用', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(1, NULL, 'BORROW',   0, -2, 2,  0, 1, '期初已借出',     DATE_SUB(NOW(), INTERVAL 8 DAY)),
-- 2 折叠长桌：12 → 占用 4 → 借出 2
(2, NULL, 'ADJUST',  12,  0, 0, 12, 1, '期初入库',       DATE_SUB(NOW(), INTERVAL 30 DAY)),
(2, NULL, 'RESERVE', -4,  4, 0,  0, 1, '期初已审批占用', DATE_SUB(NOW(), INTERVAL 9 DAY)),
(2, NULL, 'BORROW',   0, -2, 2,  0, 1, '期初已借出',     DATE_SUB(NOW(), INTERVAL 7 DAY)),
-- 3 便携投影仪：6 → 占用 2 → 借出 1
(3, NULL, 'ADJUST',   6,  0, 0,  6, 1, '期初入库',       DATE_SUB(NOW(), INTERVAL 30 DAY)),
(3, NULL, 'RESERVE', -2,  2, 0,  0, 1, '期初已审批占用', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(3, NULL, 'BORROW',   0, -1, 1,  0, 1, '期初已借出',     DATE_SUB(NOW(), INTERVAL 5 DAY)),
-- 4 蓝牙音箱：8 → 占用 2 → 借出 2
(4, NULL, 'ADJUST',   8,  0, 0,  8, 1, '期初入库',       DATE_SUB(NOW(), INTERVAL 30 DAY)),
(4, NULL, 'RESERVE', -2,  2, 0,  0, 1, '期初已审批占用', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(4, NULL, 'BORROW',   0, -2, 2,  0, 1, '期初已借出',     DATE_SUB(NOW(), INTERVAL 3 DAY)),
-- 5 露营灯：30 → 占用 5 → 借出 2
(5, NULL, 'ADJUST',  30,  0, 0, 30, 1, '期初入库',       DATE_SUB(NOW(), INTERVAL 30 DAY)),
(5, NULL, 'RESERVE', -5,  5, 0,  0, 1, '期初已审批占用', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(5, NULL, 'BORROW',   0, -2, 2,  0, 1, '期初已借出',     DATE_SUB(NOW(), INTERVAL 2 DAY)),
-- 6 一次性雨衣：100，未占用未借出
(6, NULL, 'ADJUST', 100,  0, 0,100, 1, '期初入库',       DATE_SUB(NOW(), INTERVAL 30 DAY)),
-- 7 活动横幅：10 → 占用 2
(7, NULL, 'ADJUST',  10,  0, 0, 10, 1, '期初入库',       DATE_SUB(NOW(), INTERVAL 30 DAY)),
(7, NULL, 'RESERVE', -2,  2, 0,  0, 1, '期初已审批占用', DATE_SUB(NOW(), INTERVAL 3 DAY)),
-- 8 老式白板：2，停用
(8, NULL, 'ADJUST',   2,  0, 0,  2, 1, '期初入库',       DATE_SUB(NOW(), INTERVAL 30 DAY));

-- ------------------------------------------------------------
-- 4. 对账自检（可选执行，答辩时可以现场跑）
--    每个物资的四列都必须与流水加总一致，任何一行对不上就说明库存被绕过流水改过
-- ------------------------------------------------------------
-- SELECT m.id, m.name,
--        m.available_qty, SUM(l.delta_available) AS log_available,
--        m.reserved_qty,  SUM(l.delta_reserved)  AS log_reserved,
--        m.borrowed_qty,  SUM(l.delta_borrowed)  AS log_borrowed,
--        m.total_qty,     SUM(l.delta_total)     AS log_total
-- FROM materials m
-- LEFT JOIN stock_logs l ON l.material_id = m.id
-- GROUP BY m.id, m.name, m.available_qty, m.reserved_qty, m.borrowed_qty, m.total_qty
-- ORDER BY m.id;
