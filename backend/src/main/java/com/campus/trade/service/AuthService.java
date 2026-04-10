package com.campus.trade.service;

import com.campus.trade.dto.LoginDTO;
import com.campus.trade.dto.RegisterDTO;
import java.util.Map;

public interface AuthService {
    Map<String, Object> login(LoginDTO dto, String ip, String device);
    void register(RegisterDTO dto);
}
