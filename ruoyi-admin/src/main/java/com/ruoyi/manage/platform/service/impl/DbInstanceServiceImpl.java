package com.ruoyi.manage.platform.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.manage.platform.domain.DbInstance;
import com.ruoyi.manage.platform.mapper.DbInstanceMapper;
import com.ruoyi.manage.platform.service.IDbInstanceService;
import com.ruoyi.manage.platform.service.IDbMetricSnapshotService;

/**
 * 数据库实例配置 Service 实现。
 * 密码使用AES对称加密存储，密钥通过配置项 db.monitor.secret-key 设置。
 */
@Service
public class DbInstanceServiceImpl extends ServiceImpl<DbInstanceMapper, DbInstance> implements IDbInstanceService
{
    /** AES加密算法 */
    private static final String AES_ALGORITHM = "AES";

    @Value("${db.monitor.secret-key:RuoyiDbMonitor@2024!}")
    private String secretKey;

    @org.springframework.beans.factory.annotation.Autowired
    private IDbMetricSnapshotService metricSnapshotService;

    @Override
    public List<DbInstance> selectDbInstanceList(DbInstance dbInstance)
    {
        LambdaQueryWrapper<DbInstance> wrapper = buildQueryWrapper(dbInstance);
        List<DbInstance> list = list(wrapper);
        // 列表不返回密码
        for (DbInstance item : list)
        {
            item.setPassword(null);
        }
        return list;
    }

    @Override
    public DbInstance selectDbInstanceById(Long instanceId)
    {
        DbInstance dbInstance = getById(instanceId);
        if (dbInstance != null && StringUtils.isNotEmpty(dbInstance.getPassword()))
        {
            try
            {
                dbInstance.setPasswordRaw(decrypt(dbInstance.getPassword()));
            }
            catch (Exception e)
            {
                dbInstance.setPasswordRaw("");
            }
            // 不返回密文给前端
            dbInstance.setPassword(null);
        }
        return dbInstance;
    }

    @Override
    public int insertDbInstance(DbInstance dbInstance)
    {
        validateInstance(dbInstance);
        // 加密密码
        if (StringUtils.isNotEmpty(dbInstance.getPasswordRaw()))
        {
            dbInstance.setPassword(encrypt(dbInstance.getPasswordRaw()));
        }
        else
        {
            throw new ServiceException("密码不能为空");
        }
        dbInstance.setCreateTime(DateUtils.getNowDate());
        return save(dbInstance) ? 1 : 0;
    }

    @Override
    public int updateDbInstance(DbInstance dbInstance)
    {
        if (dbInstance.getInstanceId() == null)
        {
            throw new ServiceException("实例ID不能为空");
        }
        validateInstance(dbInstance);
        // 如果传了新密码则重新加密，否则保留原密码
        if (StringUtils.isNotEmpty(dbInstance.getPasswordRaw()))
        {
            dbInstance.setPassword(encrypt(dbInstance.getPasswordRaw()));
        }
        else
        {
            // 不修改密码
            dbInstance.setPassword(null);
        }
        dbInstance.setUpdateTime(DateUtils.getNowDate());
        return updateById(dbInstance) ? 1 : 0;
    }

    @Override
    public int deleteDbInstanceByIds(Long[] instanceIds)
    {
        if (instanceIds == null || instanceIds.length == 0) return 0;
        return removeByIds(Arrays.asList(instanceIds)) ? instanceIds.length : 0;
    }

    @Override
    public boolean testConnection(DbInstance dbInstance)
    {
        // 如果是测试已有实例（传了instanceId），需要从数据库加载完整信息
        if (dbInstance.getInstanceId() != null)
        {
            DbInstance exist = getById(dbInstance.getInstanceId());
            if (exist != null)
            {
                dbInstance = exist;
            }
        }
        // 如果是测试新建表单，passwordRaw 已有明文
        if (StringUtils.isEmpty(dbInstance.getPassword()) && StringUtils.isNotEmpty(dbInstance.getPasswordRaw()))
        {
            // 直接用明文
        }
        else if (StringUtils.isNotEmpty(dbInstance.getPassword()))
        {
            dbInstance.setPasswordRaw(decrypt(dbInstance.getPassword()));
        }
        try
        {
            metricSnapshotService.collectMetrics(dbInstance);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    @Override
    public List<DbInstance> selectEnabledInstances()
    {
        LambdaQueryWrapper<DbInstance> wrapper = new LambdaQueryWrapper<DbInstance>()
            .eq(DbInstance::getStatus, "ENABLED")
            .orderByAsc(DbInstance::getInstanceGroup)
            .orderByAsc(DbInstance::getDbType);
        return list(wrapper);
    }

    // ==================== 私有方法 ====================

    private void validateInstance(DbInstance instance)
    {
        if (StringUtils.isEmpty(instance.getInstanceName()))
            throw new ServiceException("实例名称不能为空");
        if (StringUtils.isEmpty(instance.getDbType()))
            throw new ServiceException("数据库类型不能为空");
        if (StringUtils.isEmpty(instance.getHost()))
            throw new ServiceException("主机地址不能为空");
        if (instance.getPort() == null || instance.getPort() <= 0)
            throw new ServiceException("端口号不能为空");
    }

    private LambdaQueryWrapper<DbInstance> buildQueryWrapper(DbInstance dbInstance)
    {
        LambdaQueryWrapper<DbInstance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotEmpty(dbInstance.getDbType()), DbInstance::getDbType, dbInstance.getDbType())
            .eq(StringUtils.isNotEmpty(dbInstance.getStatus()), DbInstance::getStatus, dbInstance.getStatus())
            .like(StringUtils.isNotEmpty(dbInstance.getInstanceName()), DbInstance::getInstanceName, dbInstance.getInstanceName())
            .like(StringUtils.isNotEmpty(dbInstance.getHost()), DbInstance::getHost, dbInstance.getHost())
            .eq(StringUtils.isNotEmpty(dbInstance.getInstanceGroup()), DbInstance::getInstanceGroup, dbInstance.getInstanceGroup())
            .orderByAsc(DbInstance::getInstanceGroup)
            .orderByAsc(DbInstance::getDbType)
            .orderByDesc(DbInstance::getCreateTime);
        return wrapper;
    }

    /**
     * AES加密，返回Base64编码的密文。
     */
    private String encrypt(String plainText)
    {
        try
        {
            SecretKeySpec keySpec = new SecretKeySpec(fixKeyLength(secretKey), AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        }
        catch (Exception e)
        {
            throw new ServiceException("密码加密失败", e);
        }
    }

    /**
     * AES解密，输入Base64密文，返回明文。
     */
    private String decrypt(String encryptedText)
    {
        try
        {
            SecretKeySpec keySpec = new SecretKeySpec(fixKeyLength(secretKey), AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decrypted, StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            throw new ServiceException("密码解密失败", e);
        }
    }

    /**
     * 将任意长度的密钥截断/补齐到16字节（AES-128）。
     */
    private byte[] fixKeyLength(String key)
    {
        byte[] keyBytes = new byte[16];
        byte[] src = key.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(src, 0, keyBytes, 0, Math.min(src.length, 16));
        return keyBytes;
    }
}
