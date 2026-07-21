package com.admin.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Flyway 数据库迁移配置
 *
 * <p>职责：在 Spring 容器初始化<b>之前</b>（Flyway 自动建表之前）确保目标数据库存在。</p>
 *
 * <p>通过实现 {@link BeanFactoryPostProcessor}，在 {@link FlywayAutoConfiguration} 之前执行建库逻辑，
 * 后续表结构和数据由 Spring Boot 的 Flyway 自动配置完成迁移。</p>
 */
@Slf4j
@Configuration
public class FlywayConfig implements BeanFactoryPostProcessor {

    private static final String DATABASE_NAME = "java_admin";

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // 从 Environment 中读取数据源配置
        String datasourceUrl = resolveUrl(beanFactory);
        String username = resolveUsername(beanFactory);
        String password = resolvePassword(beanFactory);

        if (datasourceUrl == null) {
            log.warn("⚠️ 未找到 spring.datasource.url，跳过自动建库");
            return;
        }

        ensureDatabaseExists(datasourceUrl, username, password);
    }

    /**
     * 确保数据库存在：
     * 使用不带库名的 JDBC URL 连接，执行 CREATE DATABASE IF NOT EXISTS
     *
     * <p><b>注意：</b>建库时必须绕过 P6Spy，直接用 MySQL 原生驱动。
     * P6Spy 是 JDBC 代理驱动，建库时数据库尚不存在，P6Spy 无法找到下游驱动。</p>
     */
    private void ensureDatabaseExists(String datasourceUrl, String username, String password) {
        // 将 jdbc:p6spy:jdbc:mysql:// 还原为 jdbc:mysql://
        String mysqlUrl = datasourceUrl.replace("jdbc:p6spy:", "");

        // 从 jdbc:mysql://127.0.0.1:3306/java_admin?xxx 中提取 jdbc:mysql://127.0.0.1:3306
        String protocolPrefix = "jdbc:mysql://";
        int slashAfterHost = mysqlUrl.indexOf("/", protocolPrefix.length());
        String baseUrl = (slashAfterHost > 0) ? mysqlUrl.substring(0, slashAfterHost) : mysqlUrl;

        String initUrl = baseUrl + "/?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";

        // 显式指定 MySQL 驱动，绕过 P6Spy
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            log.error("❌ 未找到 MySQL 驱动", e);
            throw new RuntimeException("未找到 MySQL 驱动", e);
        }

        try (Connection conn = DriverManager.getConnection(initUrl, username, password);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                    "CREATE DATABASE IF NOT EXISTS `" + DATABASE_NAME + "` "
                            + "DEFAULT CHARACTER SET utf8mb4 "
                            + "DEFAULT COLLATE utf8mb4_unicode_ci");
            log.info("✅ 数据库 '{}' 已就绪（新建或已存在）", DATABASE_NAME);
        } catch (SQLException e) {
            log.error("❌ 自动创建数据库失败: {}", e.getMessage(), e);
            throw new RuntimeException("无法初始化数据库: " + DATABASE_NAME, e);
        }
    }

    // ---- 辅助方法：从 Spring Environment 读取配置 ----

    private String resolveUrl(ConfigurableListableBeanFactory beanFactory) {
        try {
            return beanFactory.resolveEmbeddedValue("${spring.datasource.url}");
        } catch (Exception e) {
            return null;
        }
    }

    private String resolveUsername(ConfigurableListableBeanFactory beanFactory) {
        try {
            return beanFactory.resolveEmbeddedValue("${spring.datasource.username}");
        } catch (Exception e) {
            return "root";
        }
    }

    private String resolvePassword(ConfigurableListableBeanFactory beanFactory) {
        try {
            return beanFactory.resolveEmbeddedValue("${spring.datasource.password}");
        } catch (Exception e) {
            return "";
        }
    }
}
