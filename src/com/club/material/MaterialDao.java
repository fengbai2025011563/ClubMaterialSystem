package com.club.material;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.club.common.DBUtil;

/**
 * 物资表的数据访问对象（DAO）。
 *
 * <p>【它的使命】只做两件事：把 SQL 发出去、把结果集搬成 Java 对象。
 * 它不做"能不能借""名称重不重"这种判断——那些属于业务规则，归 MaterialService 管。</p>
 *
 * <p>【为什么有的方法自己拿连接、有的要传 Connection】读操作（查询）自成一体，
 * 自己拿连接、用完就关；写操作需要和"库存流水"在同一个事务里，
 * 所以连接由 Service 统一提供并控制提交/回滚，DAO 只负责执行。</p>
 *
 * <p>【最关键的一点】所有扣减库存的 UPDATE 语句，WHERE 里都带了"够不够扣"的条件。
 * 这样即使两个人同时点"审批通过"，数据库也只会让其中一次成功，不会把库存扣成负数。</p>
 */
public class MaterialDao {

    /**
     * 按关键字、分类、状态查询物资列表。参数为空表示不参与筛选。
     */
    public List<Material> search(String keyword, String category, String status) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT id, name, category, unit, total_qty, available_qty, reserved_qty, borrowed_qty, "
                        + "status, remark, created_at, updated_at FROM materials WHERE 1 = 1");
        List<Object> params = new ArrayList<Object>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND name LIKE ?");
            params.add("%" + keyword.trim() + "%");
        }
        if (category != null && !category.trim().isEmpty()) {
            sql.append(" AND category = ?");
            params.add(category.trim());
        }
        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status.trim());
        }
        sql.append(" ORDER BY category, id");

        List<Material> result = new ArrayList<Material>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }
        return result;
    }

    /**
     * 按主键查一件物资，查不到返回 null。
     */
    public Material findById(int id) throws SQLException {
        String sql = "SELECT id, name, category, unit, total_qty, available_qty, reserved_qty, borrowed_qty, "
                + "status, remark, created_at, updated_at FROM materials WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /**
     * 取出库里已有的全部分类，给下拉框用，省得在页面里写死。
     */
    public List<String> findAllCategories() throws SQLException {
        List<String> result = new ArrayList<String>();
        String sql = "SELECT DISTINCT category FROM materials ORDER BY category";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(rs.getString(1));
            }
        }
        return result;
    }

    /**
     * 名称是否已被别的物资占用。excludeId 用于编辑场景：排除自己。
     */
    public boolean existsName(String name, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM materials WHERE name = ? AND id <> ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * 新增物资，返回数据库生成的主键。
     * 新建时"总量"直接作为"可用"，预留与借出都是 0。
     */
    public int insert(Connection conn, Material material) throws SQLException {
        String sql = "INSERT INTO materials (name, category, unit, total_qty, available_qty, reserved_qty, "
                + "borrowed_qty, status, remark) VALUES (?, ?, ?, ?, ?, 0, 0, 'ACTIVE', ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, material.getName());
            ps.setString(2, material.getCategory());
            ps.setString(3, material.getUnit());
            ps.setInt(4, material.getTotalQty());
            ps.setInt(5, material.getTotalQty());
            ps.setString(6, material.getRemark());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("新增物资失败：没有拿到自增主键");
    }

    /**
     * 修改基础信息（名称、分类、单位、备注）。
     * 有意不在这里改数量：数量只能通过入库、预留、借出、归还这些"有流水"的动作变化。
     */
    public int updateBasic(Connection conn, Material material) throws SQLException {
        String sql = "UPDATE materials SET name = ?, category = ?, unit = ?, remark = ?, updated_at = NOW() WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, material.getName());
            ps.setString(2, material.getCategory());
            ps.setString(3, material.getUnit());
            ps.setString(4, material.getRemark());
            ps.setInt(5, material.getId());
            return ps.executeUpdate();
        }
    }

    /**
     * 启用 / 停用。停用不删除数据，历史流水仍然能查到。
     */
    public int updateStatus(Connection conn, int id, String status) throws SQLException {
        String sql = "UPDATE materials SET status = ?, updated_at = NOW() WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate();
        }
    }

    /**
     * 入库：总量与可用同时加。相当于"从仓库外面又搬进来一批"。
     */
    public int addStock(Connection conn, int id, int quantity) throws SQLException {
        String sql = "UPDATE materials SET total_qty = total_qty + ?, available_qty = available_qty + ?, "
                + "updated_at = NOW() WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, quantity);
            ps.setInt(3, id);
            return ps.executeUpdate();
        }
    }

    /**
     * 调减：总量与可用同时减。
     * 条件 available_qty >= ? 保证只动"可用"的部分，预留和借出的数量减不到，
     * 也就不会出现"总量小于预留+借出"的坏数据。返回 0 表示可用不够。
     */
    public int reduceTotal(Connection conn, int id, int quantity) throws SQLException {
        String sql = "UPDATE materials SET total_qty = total_qty - ?, available_qty = available_qty - ?, "
                + "updated_at = NOW() WHERE id = ? AND available_qty >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, quantity);
            ps.setInt(3, id);
            ps.setInt(4, quantity);
            return ps.executeUpdate();
        }
    }

    /**
     * 预留：审批通过时把"可用"挪到"预留"。
     * WHERE 里两个条件缺一不可：物资必须是可借状态，且可用数量足够。
     * 更新 0 行就说明没抢到，由 Service 决定报什么错。
     */
    public int reserve(Connection conn, int id, int quantity) throws SQLException {
        String sql = "UPDATE materials SET available_qty = available_qty - ?, reserved_qty = reserved_qty + ?, "
                + "updated_at = NOW() WHERE id = ? AND status = 'ACTIVE' AND available_qty >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, quantity);
            ps.setInt(3, id);
            ps.setInt(4, quantity);
            return ps.executeUpdate();
        }
    }

    /**
     * 释放预留：驳回、作废时把"预留"还回"可用"。
     */
    public int release(Connection conn, int id, int quantity) throws SQLException {
        String sql = "UPDATE materials SET reserved_qty = reserved_qty - ?, available_qty = available_qty + ?, "
                + "updated_at = NOW() WHERE id = ? AND reserved_qty >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, quantity);
            ps.setInt(3, id);
            ps.setInt(4, quantity);
            return ps.executeUpdate();
        }
    }

    /**
     * 借出：管理员确认"东西被拿走了"，把"预留"挪到"借出"。
     */
    public int borrowOut(Connection conn, int id, int quantity) throws SQLException {
        String sql = "UPDATE materials SET reserved_qty = reserved_qty - ?, borrowed_qty = borrowed_qty + ?, "
                + "updated_at = NOW() WHERE id = ? AND reserved_qty >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, quantity);
            ps.setInt(3, id);
            ps.setInt(4, quantity);
            return ps.executeUpdate();
        }
    }

    /**
     * 归还：借出数量减少，完好的部分回到可用，损坏和遗失的部分从总量里核销掉。
     *
     * <p>举例：申请 10 顶帐篷，归还时 1 顶损坏，则 借出-10、可用+9、总量-1，
     * 恒等式 总量 = 可用 + 预留 + 借出 依然成立。</p>
     */
    public int giveBack(Connection conn, int id, int quantity, int damagedQty, int lostQty) throws SQLException {
        String sql = "UPDATE materials SET borrowed_qty = borrowed_qty - ?, available_qty = available_qty + ?, "
                + "total_qty = total_qty - ?, updated_at = NOW() WHERE id = ? AND borrowed_qty >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int goodQty = quantity - damagedQty - lostQty;
            ps.setInt(1, quantity);
            ps.setInt(2, goodQty);
            ps.setInt(3, damagedQty + lostQty);
            ps.setInt(4, id);
            ps.setInt(5, quantity);
            return ps.executeUpdate();
        }
    }

    /**
     * 结果集一行 → 一个 Material 对象。字段顺序与上面几条 SELECT 保持一致。
     */
    private Material mapRow(ResultSet rs) throws SQLException {
        Material material = new Material();
        material.setId(rs.getInt("id"));
        material.setName(rs.getString("name"));
        material.setCategory(rs.getString("category"));
        material.setUnit(rs.getString("unit"));
        material.setTotalQty(rs.getInt("total_qty"));
        material.setAvailableQty(rs.getInt("available_qty"));
        material.setReservedQty(rs.getInt("reserved_qty"));
        material.setBorrowedQty(rs.getInt("borrowed_qty"));
        material.setStatus(rs.getString("status"));
        material.setRemark(rs.getString("remark"));
        material.setCreatedAt(rs.getTimestamp("created_at"));
        material.setUpdatedAt(rs.getTimestamp("updated_at"));
        return material;
    }
}
