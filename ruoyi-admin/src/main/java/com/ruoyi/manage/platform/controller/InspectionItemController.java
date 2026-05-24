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
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.manage.platform.domain.InspectionItem;
import com.ruoyi.manage.platform.service.IInspectionItemService;

@RestController
@RequestMapping("/manage/platform/inspection/item")
public class InspectionItemController extends BaseController
{
    @Autowired
    private IInspectionItemService inspectionItemService;

    @PreAuthorize("@ss.hasPermi('manage:inspection:item:list')")
    @GetMapping("/list")
    public AjaxResult list(InspectionItem inspectionItem)
    {
        List<InspectionItem> list = inspectionItemService.selectInspectionItemList(inspectionItem);
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('manage:inspection:item:list')")
    @GetMapping("/tree")
    public AjaxResult tree()
    {
        return success(inspectionItemService.selectInspectionItemTree());
    }

    @PreAuthorize("@ss.hasPermi('manage:inspection:item:query')")
    @GetMapping(value = "/{itemId}")
    public AjaxResult getInfo(@PathVariable Long itemId)
    {
        return success(inspectionItemService.selectInspectionItemByItemId(itemId));
    }

    @PreAuthorize("@ss.hasPermi('manage:inspection:item:add')")
    @Log(title = "点检项目", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody InspectionItem inspectionItem)
    {
        inspectionItem.setCreateBy(getUsername());
        return toAjax(inspectionItemService.insertInspectionItem(inspectionItem));
    }

    @PreAuthorize("@ss.hasPermi('manage:inspection:item:edit')")
    @Log(title = "点检项目", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody InspectionItem inspectionItem)
    {
        inspectionItem.setUpdateBy(getUsername());
        return toAjax(inspectionItemService.updateInspectionItem(inspectionItem));
    }

    @PreAuthorize("@ss.hasPermi('manage:inspection:item:remove')")
    @Log(title = "点检项目", businessType = BusinessType.DELETE)
    @DeleteMapping("/{itemId}")
    public AjaxResult remove(@PathVariable Long itemId)
    {
        return toAjax(inspectionItemService.deleteInspectionItemByItemId(itemId));
    }
}
