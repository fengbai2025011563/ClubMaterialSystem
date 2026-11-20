package com.club.application.temp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 【临时类 · 仅用于 stu1 本地自测】
 * 正式版本由 stu2 提供（com.club.application.service.MaterialService），合并时请删除本类。
 *
 * stu1 只用到两个能力：
 *   1. listActive(keyword) —— 表单下拉，只列 ACTIVE 且 available > 0
 *   2. 按 id 批量取可用量 —— 提交时的“提示性校验”（数量不得超可用）
 */
public class TempMaterialService {

    /** 表单下拉：只列 ACTIVE 且 available > 0 的物资 */
    public List<TempMaterial> listActive(String keyword) {
        StringBuilder sql = new StringBuilder(
                "SELECT id, name, unit, available FROM materials WHERE status = 'ACTIVE' AND available > 0");
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        if (hasKeyword) {
            sql.append(" AND name LIKE ?");
        }
        sql.append(" ORDER BY id");

        List<TempMaterial> list = new ArrayList<>();
        try (Connection conn = TempDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (hasKeyword) {
                ps.setString(1, "%" + keyword.trim() + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new TempMaterial(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("unit"),
                            rs.getInt("available")));
                }
            }
        } catch (Exception e) {
            throw new BusinessException("读取物资列表失败", e);
        }
        return list;
    }

    /** materialId -> available，供 Service 做提示性库存校验 */
    public Map<Integer, Integer> loadAvailableByIds(List<Integer> ids) {
        Map<Integer, Integer> map = new LinkedHashMap<>();
        if (ids == null || ids.isEmpty()) {
            return map;
        }
        StringBuilder sql = new StringBuilder("SELECT id, available FROM materials WHERE id IN (");
        for (int i = 0; i < ids.size(); i++) {
            sql.append(i == 0 ? "?" : ",?");
        }
        sql.append(")");

        try (Connection conn = TempDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < ids.size(); i++) {
                ps.setInt(i + 1, ids.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("id"), rs.getInt("available"));
                }
            }
        } catch (Exception e) {
            throw new BusinessException("读取物资库存失败", e);
        }
        return map;
    }

    /** 物资名称，用于错误提示 */
    public Map<Integer, String> loadNameByIds(List<Integer> ids) {
        Map<Integer, String> map = new LinkedHashMap<>();
        if (ids == null || ids.isEmpty()) {
            return map;
        }
        StringBuilder sql = new StringBuilder("SELECT id, name FROM materials WHERE id IN (");
        for (int i = 0; i < ids.size(); i++) {
            sql.append(i == 0 ? "?" : ",?");
        }
        sql.append(")");

        try (Connection conn = TempDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < ids.size(); i++) {
                ps.setInt(i + 1, ids.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("id"), rs.getString("name"));
                }
            }
        } catch (Exception e) {
            throw new BusinessException("读取物资信息失败", e);
        }
        return map;
    }
}
