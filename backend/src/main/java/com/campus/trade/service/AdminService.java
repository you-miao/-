package com.campus.trade.service;

import com.campus.trade.dto.AuditDTO;
import java.util.Map;

public interface AdminService {
    void audit(Long auditorId, AuditDTO dto);
    Map<String, Object> getDashboardStats();
}
