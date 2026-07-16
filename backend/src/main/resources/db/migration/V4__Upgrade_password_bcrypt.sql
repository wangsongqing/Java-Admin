-- ============================================
-- 密码加密升级：MD5 → BCrypt
-- 使用 BCryptPasswordEncoder 生成（强度 10）
-- ============================================

-- admin 密码: admin123
UPDATE `sys_user` SET `password` = '$2a$10$j9adGlh2b0mDde7CNs9/mePNVrPBhjPBIkcVyrVDnbvdtFXw4Anqi' WHERE `username` = 'admin';

-- test 密码: 123456
UPDATE `sys_user` SET `password` = '$2a$10$q9Wlm7AciR6Cxp7qdxk2HuNV.HZQVri9j9zhP9mJ.EApOngbM8Lku' WHERE `username` = 'test';
