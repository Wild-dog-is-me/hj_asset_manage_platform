package com.ruoyi.manage.platform.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 数据库实例配置实体，对应 db_instance 表。
 * 用于存储待监控的目标数据库连接信息，支持 MySQL、SQL Server、Redis。
 */
@TableName(value = "db_instance", excludeProperty = {"searchValue", "params"})
public class DbInstance extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 实例主键 */
    @TableId(value = "instance_id", type = IdType.AUTO)
    private Long instanceId;

    /** 实例名称 */
    private String instanceName;

    /** 数据库类型：MYSQL / SQLSERVER / REDIS */
    private String dbType;

    /** 主机地址 */
    private String host;

    /** 端口号 */
    private Integer port;

    /** 默认数据库名（MySQL/SQLServer用，Redis不需要） */
    private String dbName;

    /** 用户名 */
    private String username;

    /** 加密后的密码 */
    private String password;

    /** 分组标签 */
    private String instanceGroup;

    /** 状态：ENABLED启用 / DISABLED禁用 */
    private String status;

    // ========== 非数据库字段 ==========

    /** 密码明文（仅用于新增/编辑表单传输，不入库），需在后端加密切换为password字段 */
    @TableField(exist = false)
    private String passwordRaw;

    // ========== Getters & Setters ==========

    public Long getInstanceId() { return instanceId; }
    public void setInstanceId(Long instanceId) { this.instanceId = instanceId; }

    public String getInstanceName() { return instanceName; }
    public void setInstanceName(String instanceName) { this.instanceName = instanceName; }

    public String getDbType() { return dbType; }
    public void setDbType(String dbType) { this.dbType = dbType; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }

    public String getDbName() { return dbName; }
    public void setDbName(String dbName) { this.dbName = dbName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getInstanceGroup() { return instanceGroup; }
    public void setInstanceGroup(String instanceGroup) { this.instanceGroup = instanceGroup; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPasswordRaw() { return passwordRaw; }
    public void setPasswordRaw(String passwordRaw) { this.passwordRaw = passwordRaw; }
}
