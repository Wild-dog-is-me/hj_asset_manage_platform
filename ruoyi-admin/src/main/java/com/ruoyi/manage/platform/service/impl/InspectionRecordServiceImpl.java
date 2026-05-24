package com.ruoyi.manage.platform.service.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.manage.platform.domain.InspectionItem;
import com.ruoyi.manage.platform.domain.InspectionItemField;
import com.ruoyi.manage.platform.domain.InspectionRecord;
import com.ruoyi.manage.platform.domain.InspectionRecordDetail;
import com.ruoyi.manage.platform.mapper.InspectionItemFieldMapper;
import com.ruoyi.manage.platform.mapper.InspectionItemMapper;
import com.ruoyi.manage.platform.mapper.InspectionRecordDetailMapper;
import com.ruoyi.manage.platform.mapper.InspectionRecordMapper;
import com.ruoyi.manage.platform.service.IInspectionRecordService;

@Service
public class InspectionRecordServiceImpl extends ServiceImpl<InspectionRecordMapper, InspectionRecord> implements IInspectionRecordService
{
    @Autowired
    private InspectionRecordDetailMapper detailMapper;

    @Autowired
    private InspectionItemMapper itemMapper;

    @Autowired
    private InspectionItemFieldMapper fieldMapper;

    @Override
    public List<InspectionRecord> selectInspectionRecordList(InspectionRecord inspectionRecord)
    {
        LambdaQueryWrapper<InspectionRecord> queryWrapper = new LambdaQueryWrapper<InspectionRecord>()
            .like(StringUtils.isNotEmpty(inspectionRecord.getRecordTitle()), InspectionRecord::getRecordTitle, inspectionRecord.getRecordTitle())
            .like(StringUtils.isNotEmpty(inspectionRecord.getInspector()), InspectionRecord::getInspector, inspectionRecord.getInspector())
            .eq(StringUtils.isNotEmpty(inspectionRecord.getStatus()), InspectionRecord::getStatus, inspectionRecord.getStatus())
            .eq(StringUtils.isNotNull(inspectionRecord.getInspectionDate()), InspectionRecord::getInspectionDate, inspectionRecord.getInspectionDate())
            .orderByDesc(InspectionRecord::getInspectionDate)
            .orderByDesc(InspectionRecord::getRecordId);
        return list(queryWrapper);
    }

    @Override
    public InspectionRecord selectInspectionRecordByRecordId(Long recordId)
    {
        InspectionRecord record = getById(recordId);
        if (StringUtils.isNotNull(record))
        {
            record.setDetails(detailMapper.selectList(new LambdaQueryWrapper<InspectionRecordDetail>()
                .eq(InspectionRecordDetail::getRecordId, recordId)
                .orderByAsc(InspectionRecordDetail::getOrderNum)
                .orderByAsc(InspectionRecordDetail::getDetailId)));

            List<InspectionItemField> allFields = fieldMapper.selectList(new LambdaQueryWrapper<InspectionItemField>());
            Map<Long, String> fieldOptionsMap = allFields.stream()
                .filter(f -> StringUtils.isNotEmpty(f.getFieldOptions()))
                .collect(Collectors.toMap(InspectionItemField::getFieldId, InspectionItemField::getFieldOptions, (a, b) -> a));
            for (InspectionRecordDetail detail : record.getDetails())
            {
                detail.setFieldOptions(fieldOptionsMap.get(detail.getFieldId()));
            }
        }
        return record;
    }

    @Override
    public InspectionRecord buildInspectionRecordTemplate(InspectionRecord inspectionRecord)
    {
        List<InspectionItem> items = itemMapper.selectList(new LambdaQueryWrapper<InspectionItem>()
            .eq(InspectionItem::getStatus, "0")
            .orderByAsc(InspectionItem::getParentId)
            .orderByAsc(InspectionItem::getOrderNum)
            .orderByAsc(InspectionItem::getItemId));
        Map<Long, InspectionItem> itemMap = items.stream().collect(Collectors.toMap(InspectionItem::getItemId, Function.identity(), (a, b) -> a));
        List<InspectionItemField> fields = fieldMapper.selectList(new LambdaQueryWrapper<InspectionItemField>()
            .orderByAsc(InspectionItemField::getOrderNum)
            .orderByAsc(InspectionItemField::getFieldId));
        List<InspectionRecordDetail> details = new ArrayList<InspectionRecordDetail>();
        int orderNum = 1;
        for (InspectionItemField field : fields)
        {
            InspectionItem item = itemMap.get(field.getItemId());
            if (StringUtils.isNull(item) || !"CHECK".equals(item.getItemType()))
            {
                continue;
            }
            InspectionRecordDetail detail = new InspectionRecordDetail();
            detail.setItemId(item.getItemId());
            detail.setItemPath(buildItemPath(item, itemMap));
            detail.setItemName(item.getItemName());
            detail.setFieldId(field.getFieldId());
            detail.setFieldLabel(field.getFieldLabel());
            detail.setFieldKey(field.getFieldKey());
            detail.setFieldType(field.getFieldType());
            detail.setFieldOptions(field.getFieldOptions());
            detail.setOrderNum(orderNum++);
            details.add(detail);
        }
        inspectionRecord.setDetails(details);
        return inspectionRecord;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertInspectionRecord(InspectionRecord inspectionRecord)
    {
        inspectionRecord.setCreateTime(DateUtils.getNowDate());
        boolean result = save(inspectionRecord);
        saveDetails(inspectionRecord);
        return result ? 1 : 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateInspectionRecord(InspectionRecord inspectionRecord)
    {
        inspectionRecord.setUpdateTime(DateUtils.getNowDate());
        boolean result = updateById(inspectionRecord);
        detailMapper.delete(new LambdaQueryWrapper<InspectionRecordDetail>().eq(InspectionRecordDetail::getRecordId, inspectionRecord.getRecordId()));
        saveDetails(inspectionRecord);
        return result ? 1 : 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteInspectionRecordByRecordIds(Long[] recordIds)
    {
        detailMapper.delete(new LambdaQueryWrapper<InspectionRecordDetail>().in(InspectionRecordDetail::getRecordId, Arrays.asList(recordIds)));
        return removeByIds(Arrays.asList(recordIds)) ? recordIds.length : 0;
    }

    private void saveDetails(InspectionRecord inspectionRecord)
    {
        if (StringUtils.isEmpty(inspectionRecord.getDetails()))
        {
            return;
        }
        for (InspectionRecordDetail detail : inspectionRecord.getDetails())
        {
            detail.setRecordId(inspectionRecord.getRecordId());
            detailMapper.insert(detail);
        }
    }

    private String buildItemPath(InspectionItem item, Map<Long, InspectionItem> itemMap)
    {
        List<String> names = new ArrayList<String>();
        InspectionItem current = item;
        while (StringUtils.isNotNull(current))
        {
            names.add(0, current.getItemName());
            current = itemMap.get(current.getParentId());
        }
        return String.join(" / ", names);
    }

    @Override
    public void exportInspectionRecords(Long[] recordIds, Date beginDate, Date endDate, OutputStream outputStream) throws IOException
    {
        LambdaQueryWrapper<InspectionRecord> queryWrapper = new LambdaQueryWrapper<InspectionRecord>();
        if (StringUtils.isNotEmpty(recordIds))
        {
            queryWrapper.in(InspectionRecord::getRecordId, Arrays.asList(recordIds));
        }
        else
        {
            queryWrapper.ge(StringUtils.isNotNull(beginDate), InspectionRecord::getInspectionDate, beginDate)
                .le(StringUtils.isNotNull(endDate), InspectionRecord::getInspectionDate, endDate);
        }
        queryWrapper.orderByAsc(InspectionRecord::getInspectionDate)
            .orderByAsc(InspectionRecord::getRecordId);
        List<InspectionRecord> records = list(queryWrapper);

        for (InspectionRecord record : records)
        {
            record.setDetails(detailMapper.selectList(new LambdaQueryWrapper<InspectionRecordDetail>()
                .eq(InspectionRecordDetail::getRecordId, record.getRecordId())
                .orderByAsc(InspectionRecordDetail::getOrderNum)
                .orderByAsc(InspectionRecordDetail::getDetailId)));
        }

        ZipOutputStream zipOut = new ZipOutputStream(outputStream);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        for (InspectionRecord record : records)
        {
            String safeTitle = record.getRecordTitle().replaceAll("[\\\\/:*?\"<>|]", "_");
            String dateStr = record.getInspectionDate() != null ? sdf.format(record.getInspectionDate()) : "";

            byte[] wordBytes = generateWordBytes(record, dateStr);
            zipOut.putNextEntry(new ZipEntry(safeTitle + "_" + dateStr + ".docx"));
            zipOut.write(wordBytes);
            zipOut.closeEntry();
        }

        zipOut.close();
    }

    private byte[] generateWordBytes(InspectionRecord record, String dateStr)
    {
        XWPFDocument doc = new XWPFDocument();

        // 标题
        XWPFParagraph titlePara = doc.createParagraph();
        titlePara.setAlignment(ParagraphAlignment.CENTER);
        titlePara.setSpacingAfter(200);
        XWPFRun titleRun = titlePara.createRun();
        titleRun.setText("点检记录 - " + record.getRecordTitle());
        titleRun.setFontSize(16);
        titleRun.setBold(true);

        // 基本信息表格
        XWPFTable infoTable = doc.createTable(1, 6);
        infoTable.setWidth("4500");
        setTableBorder(infoTable);

        String[][] infoData = {
            {"点检日期", dateStr, "点检人", StringUtils.defaultString(record.getInspector()), "状态", "COMPLETED".equals(record.getStatus()) ? "已完成" : "草稿"}
        };
        fillInfoTable(infoTable, infoData);

        doc.createParagraph();

        // 明细标题
        XWPFParagraph detailTitle = doc.createParagraph();
        XWPFRun detailTitleRun = detailTitle.createRun();
        detailTitleRun.setText("点检明细");
        detailTitleRun.setBold(true);
        detailTitleRun.setFontSize(12);

        // 明细表格
        List<InspectionRecordDetail> details = record.getDetails();
        XWPFTable detailTable = doc.createTable();
        detailTable.setWidth("7500");
        setTableBorder(detailTable);

        // 表头行
        XWPFTableRow headerRow = detailTable.getRow(0);
        String[] headers = {"点检路径", "填写点", "填写值"};
        int[] colWidths = {3000, 1800, 2700};
        for (int i = 0; i < headers.length; i++)
        {
            XWPFTableCell cell = headerRow.getCell(i);
            if (cell == null)
            {
                cell = headerRow.createCell();
            }
            cell.setWidth(colWidths[i] + "");
            XWPFParagraph cp = cell.getParagraphs().get(0);
            cp.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun cr = cp.createRun();
            cr.setText(headers[i]);
            cr.setBold(true);
            cr.setFontSize(10);
        }

        // 数据行
        if (StringUtils.isNotEmpty(details))
        {
            for (InspectionRecordDetail detail : details)
            {
                XWPFTableRow dataRow = detailTable.createRow();
                String[] values = {
                    StringUtils.defaultString(detail.getItemPath()),
                    StringUtils.defaultString(detail.getFieldLabel()),
                    fieldValueDisplay(detail)
                };
                for (int i = 0; i < values.length; i++)
                {
                    XWPFTableCell cell = dataRow.getCell(i);
                    if (cell == null)
                    {
                        cell = dataRow.createCell();
                    }
                    cell.setWidth(colWidths[i] + "");
                    XWPFParagraph cp = cell.getParagraphs().get(0);
                    cp.setAlignment(ParagraphAlignment.LEFT);
                    XWPFRun cr = cp.createRun();
                    cr.setText(values[i]);
                    cr.setFontSize(10);
                }
            }
        }
        else
        {
            XWPFTableRow emptyRow = detailTable.createRow();
            XWPFTableCell cell = emptyRow.getCell(0);
            cell.setWidth("7500");
            XWPFParagraph cp = cell.getParagraphs().get(0);
            XWPFRun cr = cp.createRun();
            cr.setText("暂无明细数据");
            cr.setFontSize(10);
        }

        try
        {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            doc.write(baos);
            doc.close();
            return baos.toByteArray();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void setTableBorder(XWPFTable table)
    {
        // no-op
    }

    private void fillInfoTable(XWPFTable table, String[][] data)
    {
        for (int i = 0; i < data.length; i++)
        {
            XWPFTableRow row = table.getRow(i);
            if (row == null)
            {
                row = table.createRow();
            }
            for (int j = 0; j < data[i].length; j++)
            {
                XWPFTableCell cell = row.getCell(j);
                if (cell == null)
                {
                    cell = row.createCell();
                }
                XWPFParagraph cp = cell.getParagraphs().get(0);
                cp.setAlignment(j % 2 == 0 ? ParagraphAlignment.CENTER : ParagraphAlignment.LEFT);
                XWPFRun cr = cp.createRun();
                cr.setText(data[i][j]);
                cr.setFontSize(10);
                cr.setBold(j % 2 == 0);
            }
        }
    }

    private String fieldValueDisplay(InspectionRecordDetail detail)
    {
        String val = detail.getFieldValue();
        if ("CHECKBOX".equals(detail.getFieldType()))
        {
            return "Y".equals(val) ? "是" : ("N".equals(val) ? "否" : "");
        }
        return StringUtils.defaultString(val);
    }
}
