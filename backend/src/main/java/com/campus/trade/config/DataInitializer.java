package com.campus.trade.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.trade.entity.SysUser;
import com.campus.trade.entity.SysUserRole;
import com.campus.trade.mapper.SysUserMapper;
import com.campus.trade.mapper.SysUserRoleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    @Resource
    private SysUserMapper userMapper;
    @Resource
    private SysUserRoleMapper userRoleMapper;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        migrateSchema();
        initAdmin();
        initTestUsers();
    }

    private void migrateSchema() {
        try {
            boolean hasColumn = jdbcTemplate.queryForList(
                    "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'exchange_request' AND COLUMN_NAME = 'offer_product_id'"
            ).size() > 0;
            if (!hasColumn) {
                jdbcTemplate.execute("ALTER TABLE `exchange_request` ADD COLUMN `offer_product_id` BIGINT DEFAULT NULL COMMENT '拟交换物品的商品ID' AFTER `owner_id`");
                jdbcTemplate.execute("ALTER TABLE `exchange_request` ADD INDEX `idx_offer_product_id` (`offer_product_id`)");
                log.info("数据库迁移：exchange_request 表已添加 offer_product_id 字段");
            }
        } catch (Exception e) {
            log.warn("数据库迁移检查失败: {}", e.getMessage());
        }
    }

    private void initAdmin() {
        SysUser admin = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, "admin"));
        if (admin == null) {
            admin = new SysUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNickname("系统管理员");
            admin.setRealName("管理员");
            admin.setGender(1);
            admin.setStatus(1);
            userMapper.insert(admin);

            SysUserRole role = new SysUserRole();
            role.setUserId(admin.getId());
            role.setRoleId(1L);
            userRoleMapper.insert(role);
            log.info("初始化管理员账号: admin / admin123");
        } else {
            admin.setPassword(passwordEncoder.encode("admin123"));
            userMapper.updateById(admin);
            log.info("管理员密码已重置: admin / admin123");
        }
    }

    private void initTestUsers() {
        String[][] users = {
                {"zhangsan", "123456", "张三", "张三", "1", "13800001001", "2022010101", "东校区"},
                {"lisi",     "123456", "李四", "李四", "2", "13800001002", "2022010102", "西校区"},
                {"wangwu",   "123456", "王五", "王五", "1", "13800001003", "2022010103", "东校区"},
                {"zhaoliu",  "123456", "赵六", "赵六", "2", "13800001004", "2022010104", "南校区"},
                {"sunqi",    "123456", "孙七", "孙七", "1", "13800001005", "2022010105", "西校区"},
        };

        for (String[] u : users) {
            SysUser existing = userMapper.selectOne(
                    new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, u[0]));
            if (existing == null) {
                SysUser user = new SysUser();
                user.setUsername(u[0]);
                user.setPassword(passwordEncoder.encode(u[1]));
                user.setNickname(u[2]);
                user.setRealName(u[3]);
                user.setGender(Integer.parseInt(u[4]));
                user.setPhone(u[5]);
                user.setStudentNo(u[6]);
                user.setCampus(u[7]);
                user.setStatus(1);
                userMapper.insert(user);

                SysUserRole role = new SysUserRole();
                role.setUserId(user.getId());
                role.setRoleId(2L);
                userRoleMapper.insert(role);
                log.info("初始化测试用户: {} / {}", u[0], u[1]);
            } else {
                existing.setPassword(passwordEncoder.encode(u[1]));
                userMapper.updateById(existing);
                log.info("测试用户密码已重置: {} / {}", u[0], u[1]);
            }
        }
    }
}
