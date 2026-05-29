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
import com.ruoyi.manage.platform.domain.AssetReceiveDTO;
import com.ruoyi.manage.platform.domain.AssetReturnDTO;
import com.ruoyi.manage.platform.domain.AssetScrapDTO;
import com.ruoyi.manage.platform.domain.AssetStatisticsDTO;
import com.ruoyi.manage.platform.domain.AssetTransferDTO;
import com.ruoyi.manage.platform.domain.AssetTransferRecord;
import com.ruoyi.manage.platform.service.IAssetInfoService;
import com.ruoyi.manage.platform.service.IAssetTransferRecordService;

/**
 * 资产信息 Controller。
 */
@RestController
@RequestMapping("/manage/platform/asset")
public class AssetInfoController extends BaseController
{
    @Autowired
    private IAssetInfoService assetInfoService;

    @Autowired
    private IAssetTransferRecordService transferRecordService;

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

    /**
     * 领用资产。
     * 将资产从库存中状态变更为使用中，绑定使用部门和领用人。
     */
    @PreAuthorize("@ss.hasPermi('manage:asset:edit')")
    @Log(title = "资产领用", businessType = BusinessType.UPDATE)
    @PostMapping("/receive")
    public AjaxResult receive(@RequestBody AssetReceiveDTO dto)
    {
        dto.setOperator(getUsername());
        assetInfoService.receiveAsset(dto);
        return success("领用成功");
    }

    /**
     * 归还资产。
     * 将使用中的资产归还至库存，清空部门和使用人。
     */
    @PreAuthorize("@ss.hasPermi('manage:asset:edit')")
    @Log(title = "资产归还", businessType = BusinessType.UPDATE)
    @PostMapping("/return")
    public AjaxResult doReturn(@RequestBody AssetReturnDTO dto)
    {
        dto.setOperator(getUsername());
        assetInfoService.returnAsset(dto);
        return success("归还成功");
    }

    /**
     * 调拨资产。
     * 将资产从当前归属调拨到新归属，状态保持使用中不变。
     */
    @PreAuthorize("@ss.hasPermi('manage:asset:edit')")
    @Log(title = "资产调拨", businessType = BusinessType.UPDATE)
    @PostMapping("/transfer")
    public AjaxResult doTransfer(@RequestBody AssetTransferDTO dto)
    {
        dto.setOperator(getUsername());
        assetInfoService.transferAsset(dto);
        return success("调拨成功");
    }

    /**
     * 报废资产。
     * 将资产状态变更为已报废，报废后不可再做任何流转操作。
     */
    @PreAuthorize("@ss.hasPermi('manage:asset:edit')")
    @Log(title = "资产报废", businessType = BusinessType.UPDATE)
    @PostMapping("/scrap")
    public AjaxResult scrap(@RequestBody AssetScrapDTO dto)
    {
        dto.setOperator(getUsername());
        assetInfoService.scrapAsset(dto);
        return success("报废成功");
    }

    /**
     * 根据资产ID查询流转记录。
     */
    @PreAuthorize("@ss.hasPermi('manage:asset:list')")
    @GetMapping("/transfer/list")
    public AjaxResult transferList(Long assetId)
    {
        List<AssetTransferRecord> list = transferRecordService.selectByAssetId(assetId);
        return success(list);
    }

    /**
     * 分页查询流转记录（管理端审计用）。
     */
    @PreAuthorize("@ss.hasPermi('manage:asset:list')")
    @GetMapping("/transfer/pageList")
    public TableDataInfo transferPageList(AssetTransferRecord record)
    {
        startPage();
        List<AssetTransferRecord> list = transferRecordService.selectTransferRecordList(record);
        return getDataTable(list);
    }

    /**
     * 获取资产统计数据（首页仪表盘用）。
     */
    @PreAuthorize("@ss.hasPermi('manage:asset:list')")
    @GetMapping("/statistics")
    public AjaxResult statistics()
    {
        AssetStatisticsDTO dto = assetInfoService.getAssetStatistics();
        return success(dto);
    }
}
