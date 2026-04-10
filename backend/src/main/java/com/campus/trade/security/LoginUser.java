package com.campus.trade.security;

import com.campus.trade.entity.SysUser;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data
public class LoginUser implements UserDetails {
    private Long userId;
    private String username;
    private String password;
    private String role;
    private Integer status;

    public LoginUser() {}

    public LoginUser(SysUser user, String role) {
        if (user != null) {
            this.userId = user.getId();
            this.username = user.getUsername();
            this.password = user.getPassword();
            this.status = user.getStatus();
        }
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != null && status == 1;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status != null && status == 1;
    }
}
