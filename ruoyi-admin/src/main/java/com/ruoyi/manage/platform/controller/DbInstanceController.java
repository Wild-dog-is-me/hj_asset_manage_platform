package com.ruoyi.manage.platform.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.manage.platform.domain.DbInstance;
import com.ruoyi.manage.platform.service.IDbInstanceService;

/**
 * 数据库实例配置 Controller。
 */
@RestController
@RequestMapping("/manage/platform/db/instance")
public class DbInstanceController extends BaseController
{
    @Autowired
    private IDbInstanceService dbInstanceService;

    /**
     * 查询实例列表。
     */
    @PreAuthorize("@ss.hasPermi('monitor:db:instance:query')")
    @GetMapping("/list")
    public TableDataInfo list(DbInstance dbInstance)
    {
        startPage();
        List<DbInstance> list = dbInstanceService.selectDbInstanceList(dbInstance);
        return getDataTable(list);
    }

    /**
     * 查询实例详情。
     */
    @PreAuthorize("@ss.hasPermi('monitor:db:instance:query')")
    @GetMapping("/{instanceId}")
    public AjaxResult getInfo(@PathVariable Long instanceId)
    {
        return success(dbInstanceService.selectDbInstanceById(instanceId));
    }

    /**
     * 新增实例。
     */
    @PreAuthorize("@ss.hasPermi('monitor:db:instance:add')")
    @Log(title = "数据库实例", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody DbInstance dbInstance)
    {
        dbInstance.setCreateBy(getUsername());
        return toAjax(dbInstanceService.insertDbInstance(dbInstance));
    }

    /**
     * 修改实例。
     */
    @PreAuthorize("@ss.hasPermi('monitor:db:instance:edit')")
    @Log(title = "数据库实例", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody DbInstance dbInstance)
    {
        dbInstance.setUpdateBy(getUsername());
        return toAjax(dbInstanceService.updateDbInstance(dbInstance));
    }

    /**
     * 删除实例。
     */
    @PreAuthorize("@ss.hasPermi('monitor:db:instance:remove')")
    @Log(title = "数据库实例", businessType = BusinessType.DELETE)
    @DeleteMapping("/{instanceIds}")
    public AjaxResult remove(@PathVariable Long[] instanceIds)
    {
        return toAjax(dbInstanceService.deleteDbInstanceByIds(instanceIds));
    }

    /**
     * 测试数据库连接。
     */
    @PreAuthorize("@ss.hasPermi('monitor:db:instance:test')")
    @Log(title = "数据库实例", businessType = BusinessType.OTHER)
    @PostMapping("/test/{instanceId}")
    public AjaxResult testConnection(@PathVariable Long instanceId)
    {
        DbInstance dbInstance = new DbInstance();
        dbInstance.setInstanceId(instanceId);
        boolean ok = dbInstanceService.testConnection(dbInstance);
        return ok ? success("连接成功") : error("连接失败，请检查网络、端口、用户名和密码");
    }

    /**
     * 测试表单中的数据库连接（不需先保存）。
     */
    @PreAuthorize("@ss.hasPermi('monitor:db:instance:test')")
    @PostMapping("/testForm")
    public AjaxResult testFormConnection(@RequestBody DbInstance dbInstance)
    {
        boolean ok = dbInstanceService.testConnection(dbInstance);
        return ok ? success("连接成功") : error("连接失败，请检查网络、端口、用户名和密码");
    }
}
