-- ============================================================
-- 社团活动物资借还系统 · 建库脚本
-- 负责人：刘艳（物资维护与库存）
-- 适用：MySQL 8.0.16 及以上（CHECK 约束从该版本开始真正生效，推荐 8.4）
-- 用法：在 Navicat / MySQL 命令行里整段执行一次即可
-- 注意：脚本会先删表再建表，方便反复重跑；已有数据会被清空
-- ============================================================

CREATE DATABASE IF NOT EXISTS club_material
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE club_material;
SET NAMES utf8mb4;

-- 删除顺序与外键相反，否则会因"被子表引用"而删不掉
DROP TABLE IF EXISTS stock_logs;
DROP TABLE IF EXISTS application_items;
DROP TABLE IF EXISTS loan_applications;
DROP TABLE IF EXISTS materials;
DROP TABLE IF EXISTS users;

-- ------------------------------------------------------------
-- 1. 用户表
-- ------------------------------------------------------------
CREATE TABLE users (
    id          INT          NOT NULL AUTO_INCREMENT COMMENT '用户编号',
    username    VARCHAR(30)  NOT NULL COMMENT '登录名',
    password    VARCHAR(64)  NOT NULL COMMENT 'SHA-256 十六进制摘要',
    real_name   VARCHAR(30)  NOT NULL COMMENT '真实姓名，页面展示用',
    role        VARCHAR(10)  NOT NULL COMMENT 'MEMBER 社团成员，ADMIN 管理员',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1 正常，0 停用',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username),
    CONSTRAINT chk_users_role CHECK (role IN ('MEMBER', 'ADMIN')),
    CONSTRAINT chk_users_status CHECK (status IN (0, 1))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- ------------------------------------------------------------
-- 2. 物资表
--    四个数量的含义：
--      total_qty     库存总量
--      available_qty 当前可借
--      reserved_qty  已审批占用、还没被拿走
--      borrowed_qty  已拿走、还没归还
--    恒等式：total_qty = available_qty + reserved_qty + borrowed_qty
-- ------------------------------------------------------------
CREATE TABLE materials (
    id              INT          NOT NULL AUTO_INCREMENT COMMENT '物资编号',
    name            VARCHAR(50)  NOT NULL COMMENT '物资名称',
    category        VARCHAR(30)  NOT NULL COMMENT '分类，如户外、家具、设备',
    unit            VARCHAR(10)  NOT NULL DEFAULT '件' COMMENT '计量单位',
    total_qty       INT          NOT NULL DEFAULT 0 COMMENT '库存总量',
    available_qty   INT          NOT NULL DEFAULT 0 COMMENT '可用数量',
    reserved_qty    INT          NOT NULL DEFAULT 0 COMMENT '预留数量',
    borrowed_qty    INT          NOT NULL DEFAULT 0 COMMENT '借出数量',
    status          VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE 可借，INACTIVE 停用',
    remark          VARCHAR(200) NULL COMMENT '备注',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_materials_name (name),
    KEY idx_materials_category (category),
    CONSTRAINT chk_materials_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT chk_materials_non_negative CHECK (
        total_qty >= 0 AND available_qty >= 0 AND reserved_qty >= 0 AND borrowed_qty >= 0
    ),
    CONSTRAINT chk_materials_balance CHECK (
        total_qty = available_qty + reserved_qty + borrowed_qty
    )
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '物资表';

-- ------------------------------------------------------------
-- 3. 借用申请表
-- ------------------------------------------------------------
CREATE TABLE loan_applications (
    id              INT          NOT NULL AUTO_INCREMENT COMMENT '申请编号',
    applicant_id    INT          NOT NULL COMMENT '申请人，引用 users.id',
    activity_name   VARCHAR(80)  NOT NULL COMMENT '活动名称',
    purpose         VARCHAR(300) NOT NULL COMMENT '借用用途',
    expected_start  DATE         NOT NULL COMMENT '预计开始日期',
    expected_return DATE         NOT NULL COMMENT '预计归还日期',
    status          VARCHAR(12)  NOT NULL DEFAULT 'PENDING' COMMENT '申请状态',
    client_token    VARCHAR(36)  NOT NULL COMMENT '防重复提交令牌',
    apply_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请提交时间',
    reviewer_id     INT          NULL COMMENT '审批人，引用 users.id',
    review_time     DATETIME     NULL COMMENT '审批时间',
    review_remark   VARCHAR(200) NULL COMMENT '审批意见',
    borrow_time     DATETIME     NULL COMMENT '借出时间',
    return_time     DATETIME     NULL COMMENT '归还时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_loan_client_token (client_token),
    KEY idx_loan_applicant_status (applicant_id, status),
    KEY idx_loan_status (status),
    CONSTRAINT fk_loan_applicant FOREIGN KEY (applicant_id) REFERENCES users (id),
    CONSTRAINT fk_loan_reviewer FOREIGN KEY (reviewer_id) REFERENCES users (id),
    CONSTRAINT chk_loan_status CHECK (
        status IN ('PENDING', 'APPROVED', 'REJECTED', 'BORROWED', 'RETURNED', 'CANCELLED')
    ),
    CONSTRAINT chk_loan_dates CHECK (expected_return >= expected_start)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '借用申请表';

-- ------------------------------------------------------------
-- 4. 申请明细表（一张申请包含多种物资）
-- ------------------------------------------------------------
CREATE TABLE application_items (
    id             INT          NOT NULL AUTO_INCREMENT COMMENT '明细编号',
    application_id INT          NOT NULL COMMENT '所属申请',
    material_id    INT          NOT NULL COMMENT '物资',
    quantity       INT          NOT NULL COMMENT '申请数量',
    damaged_qty    INT          NOT NULL DEFAULT 0 COMMENT '归还时损坏数量',
    lost_qty       INT          NOT NULL DEFAULT 0 COMMENT '归还时遗失数量',
    remark         VARCHAR(200) NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_item_application_material (application_id, material_id),
    KEY idx_item_material (material_id),
    CONSTRAINT fk_item_application FOREIGN KEY (application_id) REFERENCES loan_applications (id) ON DELETE CASCADE,
    CONSTRAINT fk_item_material FOREIGN KEY (material_id) REFERENCES materials (id),
    CONSTRAINT chk_item_quantity CHECK (quantity > 0),
    CONSTRAINT chk_item_damage CHECK (damaged_qty >= 0 AND lost_qty >= 0 AND damaged_qty + lost_qty <= quantity)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '申请明细表';

-- ------------------------------------------------------------
-- 5. 库存流水表（只追加，不修改，不删除）
--    change_type 含义：
--      RESERVE 审批通过占用   RELEASE 驳回/作废释放
--      BORROW  确认借出       RETURN  归还核销
--      ADJUST  管理员手工入库或初始化
-- ------------------------------------------------------------
CREATE TABLE stock_logs (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '流水编号',
    material_id     INT          NOT NULL COMMENT '物资',
    application_id  INT          NULL COMMENT '关联申请，手工调整时为空',
    change_type     VARCHAR(16)  NOT NULL COMMENT '变动类型',
    delta_available INT          NOT NULL DEFAULT 0 COMMENT '可用数量增量',
    delta_reserved  INT          NOT NULL DEFAULT 0 COMMENT '预留数量增量',
    delta_borrowed  INT          NOT NULL DEFAULT 0 COMMENT '借出数量增量',
    delta_total     INT          NOT NULL DEFAULT 0 COMMENT '总量增量',
    operator_id     INT          NULL COMMENT '操作人',
    note            VARCHAR(200) NULL COMMENT '备注',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发生时间',
    PRIMARY KEY (id),
    KEY idx_stock_material (material_id, id),
    CONSTRAINT fk_stock_material FOREIGN KEY (material_id) REFERENCES materials (id),
    CONSTRAINT fk_stock_application FOREIGN KEY (application_id) REFERENCES loan_applications (id),
    CONSTRAINT chk_stock_type CHECK (
        change_type IN ('RESERVE', 'RELEASE', 'BORROW', 'RETURN', 'ADJUST')
    )
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '库存流水表';
