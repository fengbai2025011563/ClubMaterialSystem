package com.club.material;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.club.common.DBUtil;

/**
 * 库存流水表的数据访问对象。
 *
 * <p>【它的使命】把每一次库存变化写成一条流水，以及按物资把流水查出来给页面看。</p>
 *
 * <p>【为什么插入要传 Connection】因为"改库存"和"记流水"必须同生共死：
 * 库存改了但流水没写，对账就断了；流水写了但库存没改，账就是假的。
 * 只有放在同一个事务里，才能保证要么都成功、要么都不发生。</p>
 */
public class StockLogDao {

    /**
     * 插入一条流水。applicationId 允许为空（管理员直接入库时没有申请单）。
     */
    public void insert(Connection conn, StockLog log) throws SQLException {
        String sql = "INSERT INTO stock_logs (material_id, application_id, change_type, delta_available, "
                + "delta_reserved, delta_borrowed, delta_total, operator_id, note) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, log.getMaterialId());
            // setObject 可以直接接收 null，适合 application_id / operator_id 这种可空外键
            ps.setObject(2, log.getApplicationId());
            ps.setString(3, log.getChangeType());
            ps.setInt(4, log.getDeltaAvailable());
            ps.setInt(5, log.getDeltaReserved());
            ps.setInt(6, log.getDeltaBorrowed());
            ps.setInt(7, log.getDeltaTotal());
            ps.setObject(8, log.getOperatorId());
            ps.setString(9, log.getNote());
            ps.executeUpdate();
        }
    }

    /**
     * 查询某个物资最近 100 条流水，新的在前。
     * 这一步是读操作，不需要参与事务，自己拿连接即可。
     */
    public List<StockLog> findByMaterial(int materialId) throws SQLException {
        String sql = "SELECT id, material_id, application_id, change_type, delta_available, delta_reserved, "
                + "delta_borrowed, delta_total, operator_id, note, created_at "
                + "FROM stock_logs WHERE material_id = ? ORDER BY id DESC LIMIT 100";
        List<StockLog> result = new ArrayList<StockLog>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, materialId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StockLog log = new StockLog();
                    log.setId(rs.getLong("id"));
                    log.setMaterialId(rs.getInt("material_id"));
                    // getObject 取可空整数，避免 0 与 null 混淆
                    log.setApplicationId((Integer) rs.getObject("application_id"));
                    log.setChangeType(rs.getString("change_type"));
                    log.setDeltaAvailable(rs.getInt("delta_available"));
                    log.setDeltaReserved(rs.getInt("delta_reserved"));
                    log.setDeltaBorrowed(rs.getInt("delta_borrowed"));
                    log.setDeltaTotal(rs.getInt("delta_total"));
                    log.setOperatorId((Integer) rs.getObject("operator_id"));
                    log.setNote(rs.getString("note"));
                    log.setCreatedAt(rs.getTimestamp("created_at"));
                    result.add(log);
                }
            }
        }
        return result;
    }
}
