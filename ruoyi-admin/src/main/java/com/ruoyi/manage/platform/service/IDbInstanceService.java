package com.ruoyi.manage.platform.service;

import java.util.List;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.manage.platform.domain.DbInstance;

/**
 * 数据库实例配置 Service 接口。
 */
public interface IDbInstanceService extends IService<DbInstance>
{
    /**
     * 查询实例列表。
     */
    public List<DbInstance> selectDbInstanceList(DbInstance dbInstance);

    /**
     * 根据实例ID查询，密码字段解密后填充到 passwordRaw。
     */
    public DbInstance selectDbInstanceById(Long instanceId);

    /**
     * 新增实例。将 passwordRaw 加密后存入 password 字段。
     */
    public int insertDbInstance(DbInstance dbInstance);

    /**
     * 修改实例。若 passwordRaw 非空则重新加密，否则保留原密码。
     */
    public int updateDbInstance(DbInstance dbInstance);

    /**
     * 删除实例（支持批量）。
     */
    public int deleteDbInstanceByIds(Long[] instanceIds);

    /**
     * 测试数据库连接。
     * @return 是否连接成功
     */
    public boolean testConnection(DbInstance dbInstance);

    /**
     * 获取所有启用的实例。
     */
    public List<DbInstance> selectEnabledInstances();
}
