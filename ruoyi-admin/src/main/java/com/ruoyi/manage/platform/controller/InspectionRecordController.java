package com.ruoyi.manage.platform.controller;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.manage.platform.domain.InspectionRecord;
import com.ruoyi.manage.platform.service.IInspectionRecordService;

@RestController
@RequestMapping("/manage/platform/inspection/record")
public class InspectionRecordController extends BaseController
{
    @Autowired
    private IInspectionRecordService inspectionRecordService;

    @PreAuthorize("@ss.hasPermi('manage:inspection:record:list')")
    @GetMapping("/list")
    public TableDataInfo list(InspectionRecord inspectionRecord)
    {
        startPage();
        List<InspectionRecord> list = inspectionRecordService.selectInspectionRecordList(inspectionRecord);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('manage:inspection:record:query')")
    @GetMapping(value = "/{recordId}")
    public AjaxResult getInfo(@PathVariable Long recordId)
    {
        return success(inspectionRecordService.selectInspectionRecordByRecordId(recordId));
    }

    @PreAuthorize("@ss.hasPermi('manage:inspection:record:add')")
    @PostMapping("/template")
    public AjaxResult template(@RequestBody InspectionRecord inspectionRecord)
    {
        return success(inspectionRecordService.buildInspectionRecordTemplate(inspectionRecord));
    }

    @PreAuthorize("@ss.hasPermi('manage:inspection:record:add')")
    @Log(title = "点检记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody InspectionRecord inspectionRecord)
    {
        inspectionRecord.setCreateBy(getUsername());
        return toAjax(inspectionRecordService.insertInspectionRecord(inspectionRecord));
    }

    @PreAuthorize("@ss.hasPermi('manage:inspection:record:edit')")
    @Log(title = "点检记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody InspectionRecord inspectionRecord)
    {
        inspectionRecord.setUpdateBy(getUsername());
        return toAjax(inspectionRecordService.updateInspectionRecord(inspectionRecord));
    }

    @PreAuthorize("@ss.hasPermi('manage:inspection:record:remove')")
    @Log(title = "点检记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds)
    {
        return toAjax(inspectionRecordService.deleteInspectionRecordByRecordIds(recordIds));
    }

    @PreAuthorize("@ss.hasPermi('manage:inspection:record:export')")
    @Log(title = "点检记录", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(HttpServletResponse response, Long[] recordIds, String beginDateStr, String endDateStr) throws ParseException, IOException
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date beginDate = StringUtils.isNotEmpty(beginDateStr) ? sdf.parse(beginDateStr) : null;
        Date endDate = StringUtils.isNotEmpty(endDateStr) ? sdf.parse(endDateStr) : null;
        response.setContentType("application/zip");
        response.setCharacterEncoding("utf-8");
        String fileName = "点检记录导出_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        response.setHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes("GBK"), "ISO-8859-1") + ".zip");
        inspectionRecordService.exportInspectionRecords(recordIds, beginDate, endDate, response.getOutputStream());
    }
}
