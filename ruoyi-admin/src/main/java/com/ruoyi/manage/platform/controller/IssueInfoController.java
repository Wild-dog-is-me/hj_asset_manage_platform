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
import com.ruoyi.manage.platform.domain.IssueInfo;
import com.ruoyi.manage.platform.domain.IssueInfoExport;
import com.ruoyi.manage.platform.service.IIssueInfoService;

@RestController
@RequestMapping("/manage/platform/issue")
public class IssueInfoController extends BaseController
{
    @Autowired
    private IIssueInfoService issueInfoService;

    @PreAuthorize("@ss.hasPermi('manage:issue:list')")
    @GetMapping("/list")
    public TableDataInfo list(IssueInfo issueInfo)
    {
        startPage();
        List<IssueInfo> list = issueInfoService.selectIssueInfoList(issueInfo);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('manage:issue:export')")
    @Log(title = "问题信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, IssueInfo issueInfo)
    {
        List<IssueInfo> list = issueInfoService.selectIssueInfoList(issueInfo);
        List<IssueInfoExport> exportList = list.stream().map(IssueInfoExport::new).collect(Collectors.toList());
        ExcelUtil<IssueInfoExport> util = new ExcelUtil<IssueInfoExport>(IssueInfoExport.class);
        util.exportExcel(response, exportList, "问题信息数据");
    }

    @PreAuthorize("@ss.hasPermi('manage:issue:query')")
    @GetMapping(value = "/{issueId}")
    public AjaxResult getInfo(@PathVariable Long issueId)
    {
        return success(issueInfoService.selectIssueInfoByIssueId(issueId));
    }

    @PreAuthorize("@ss.hasPermi('manage:issue:add')")
    @Log(title = "问题信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody IssueInfo issueInfo)
    {
        issueInfo.setCreateBy(getUsername());
        return toAjax(issueInfoService.insertIssueInfo(issueInfo));
    }

    @PreAuthorize("@ss.hasPermi('manage:issue:edit')")
    @Log(title = "问题信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody IssueInfo issueInfo)
    {
        issueInfo.setUpdateBy(getUsername());
        return toAjax(issueInfoService.updateIssueInfo(issueInfo));
    }

    @PreAuthorize("@ss.hasPermi('manage:issue:remove')")
    @Log(title = "问题信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{issueIds}")
    public AjaxResult remove(@PathVariable Long[] issueIds)
    {
        return toAjax(issueInfoService.deleteIssueInfoByIssueIds(issueIds));
    }
}
