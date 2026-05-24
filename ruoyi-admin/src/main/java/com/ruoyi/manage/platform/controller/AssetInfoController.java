package com.ruoyi.manage.platform.controller;

import java.util.List;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletResponse;
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
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.manage.platform.domain.AssetInfo;
import com.ruoyi.manage.platform.domain.AssetInfoExport;
import com.ruoyi.manage.platform.service.IAssetInfoService;

@RestController
@RequestMapping("/manage/platform/asset")
public class AssetInfoController extends BaseController
{
    @Autowired
    private IAssetInfoService assetInfoService;

    @PreAuthorize("@ss.hasPermi('manage:asset:list')")
    @GetMapping("/list")
    public TableDataInfo list(AssetInfo assetInfo)
    {
        startPage();
        List<AssetInfo> list = assetInfoService.selectAssetInfoList(assetInfo);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('manage:asset:export')")
    @Log(title = "资产信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AssetInfo assetInfo)
    {
        List<AssetInfo> list = assetInfoService.selectAssetInfoList(assetInfo);
        List<AssetInfoExport> exportList = list.stream().map(AssetInfoExport::new).collect(Collectors.toList());
        ExcelUtil<AssetInfoExport> util = new ExcelUtil<AssetInfoExport>(AssetInfoExport.class);
        util.exportExcel(response, exportList, "资产信息数据");
    }

    @PreAuthorize("@ss.hasPermi('manage:asset:query')")
    @GetMapping(value = "/{assetId}")
    public AjaxResult getInfo(@PathVariable Long assetId)
    {
        return success(assetInfoService.selectAssetInfoByAssetId(assetId));
    }

    @PreAuthorize("@ss.hasPermi('manage:asset:add')")
    @Log(title = "资产信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AssetInfo assetInfo)
    {
        assetInfo.setCreateBy(getUsername());
        return toAjax(assetInfoService.insertAssetInfo(assetInfo));
    }

    @PreAuthorize("@ss.hasPermi('manage:asset:edit')")
    @Log(title = "资产信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AssetInfo assetInfo)
    {
        assetInfo.setUpdateBy(getUsername());
        return toAjax(assetInfoService.updateAssetInfo(assetInfo));
    }

    @PreAuthorize("@ss.hasPermi('manage:asset:remove')")
    @Log(title = "资产信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{assetIds}")
    public AjaxResult remove(@PathVariable Long[] assetIds)
    {
        return toAjax(assetInfoService.deleteAssetInfoByAssetIds(assetIds));
    }
}
