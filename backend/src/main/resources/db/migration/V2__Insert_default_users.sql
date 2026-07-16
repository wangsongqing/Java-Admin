-- =============================================
--  V2: 插入初始用户数据
--  默认密码 admin → admin123 (MD5: 0192023a7bbd73250516f069df18b500)
--  默认密码 test  → 123456  (MD5: e10adc3949ba59abbe56e057f20f883e)
-- =============================================

INSERT INTO `sys_user`
    (`username`, `password`, `nickname`, `email`, `gender`, `status`, `role`, `remark`)
VALUES
    ('admin', '0192023a7bbd73250516f069df18b500', '超级管理员', 'admin@java-admin.com', 1, 1, 'admin', '系统初始化管理员账号'),
    ('test',  'e10adc3949ba59abbe56e057f20f883e', '测试用户',   'test@java-admin.com',   1, 1, 'user',  '测试账号');
