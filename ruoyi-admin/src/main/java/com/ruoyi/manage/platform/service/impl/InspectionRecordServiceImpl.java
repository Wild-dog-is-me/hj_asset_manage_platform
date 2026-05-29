package com.ruoyi.manage.platform.service.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.Document;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.ImageUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.manage.platform.domain.InspectionItem;
import com.ruoyi.manage.platform.domain.InspectionItemField;
import com.ruoyi.manage.platform.domain.InspectionRecord;
import com.ruoyi.manage.platform.domain.InspectionRecordDetail;
import com.ruoyi.manage.platform.mapper.InspectionItemFieldMapper;
import com.ruoyi.manage.platform.mapper.InspectionItemMapper;
import com.ruoyi.manage.platform.mapper.InspectionRecordDetailMapper;
import com.ruoyi.manage.platform.mapper.InspectionRecordMapper;
import com.ruoyi.manage.platform.service.IInspectionRecordService;

/**
 * 点检记录服务实现。
 * 负责点检记录的增删改查、模板构建以及 Word 导出。
 * 当前实现额外支持富文本字段在导出时保留文字与图片内容。
 */
@Service
public class InspectionRecordServiceImpl extends ServiceImpl<InspectionRecordMapper, InspectionRecord> implements IInspectionRecordService
{
    private static final Pattern IMG_TAG_PATTERN = Pattern.compile("(?i)<img\\b[^>]*src\\s*=\\s*(['\"]?)([^'\"\\s>]+)\\1[^>]*>");
    private static final Pattern BR_TAG_PATTERN = Pattern.compile("(?i)<br\\s*/?>");
    private static final int EXPORT_IMAGE_MAX_WIDTH = 360;
    private static final int EXPORT_IMAGE_MAX_HEIGHT = 240;
    private static final int EXCEL_IMAGE_COLUMN_WIDTH = 28 * 256;
    private static final short EXCEL_TEXT_ROW_HEIGHT = 420;
    private static final short EXCEL_IMAGE_ROW_HEIGHT = 3000;
    private static final String EXCEL_SHEET_NAME = "点检记录";

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
            Map<Long, String> fieldRequiredMap = allFields.stream()
                .filter(f -> "1".equals(f.getRequired()))
                .collect(Collectors.toMap(InspectionItemField::getFieldId, f -> "1", (a, b) -> a));
            for (InspectionRecordDetail detail : record.getDetails())
            {
                detail.setFieldOptions(fieldOptionsMap.get(detail.getFieldId()));
                detail.setRequired(fieldRequiredMap.get(detail.getFieldId()));
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
            detail.setRequired(field.getRequired());
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
        validateRequiredFields(inspectionRecord);
        inspectionRecord.setCreateTime(DateUtils.getNowDate());
        boolean result = save(inspectionRecord);
        saveDetails(inspectionRecord);
        return result ? 1 : 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateInspectionRecord(InspectionRecord inspectionRecord)
    {
        validateRequiredFields(inspectionRecord);
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

    /**
     * 校验已完成状态下的必填项是否都已填写。
     * 该校验仅在记录状态为已完成时生效，避免草稿阶段无法暂存。
     *
     * @param inspectionRecord 点检记录
     */
    private void validateRequiredFields(InspectionRecord inspectionRecord)
    {
        if (!"COMPLETED".equals(inspectionRecord.getStatus()) || StringUtils.isEmpty(inspectionRecord.getDetails()))
        {
            return;
        }

        List<Long> fieldIds = inspectionRecord.getDetails().stream()
            .map(InspectionRecordDetail::getFieldId)
            .distinct()
            .collect(Collectors.toList());
        List<InspectionItemField> requiredFields = fieldMapper.selectList(new LambdaQueryWrapper<InspectionItemField>()
            .in(InspectionItemField::getFieldId, fieldIds)
            .eq(InspectionItemField::getRequired, "1"));
        if (StringUtils.isEmpty(requiredFields))
        {
            return;
        }

        Set<Long> requiredFieldIds = requiredFields.stream()
            .map(InspectionItemField::getFieldId)
            .collect(Collectors.toSet());

        List<String> missingFields = new ArrayList<>();
        for (InspectionRecordDetail detail : inspectionRecord.getDetails())
        {
            if (requiredFieldIds.contains(detail.getFieldId()))
            {
                String val = detail.getFieldValue();
                if (val == null || val.trim().isEmpty())
                {
                    missingFields.add(StringUtils.defaultString(detail.getItemPath()) + " / " + StringUtils.defaultString(detail.getFieldLabel()));
                }
            }
        }

        if (!missingFields.isEmpty())
        {
            throw new RuntimeException("以下必填字段尚未填写： " + String.join("； ", missingFields));
        }
    }

    /**
     * 批量保存点检记录明细。
     * 插入前统一回填 recordId，保证主子表关联一致。
     *
     * @param inspectionRecord 点检记录
     */
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

    /**
     * 递归拼装点检项路径。
     * 导出和页面展示统一使用该路径格式，避免不同位置的层级文案不一致。
     *
     * @param item 当前点检项
     * @param itemMap 点检项映射
     * @return 点检项完整路径
     */
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
        List<InspectionRecord> records = queryExportRecords(recordIds, beginDate, endDate);
        XWPFDocument doc = new XWPFDocument();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        XWPFParagraph mainTitle = doc.createParagraph();
        mainTitle.setAlignment(ParagraphAlignment.CENTER);
        mainTitle.setSpacingAfter(400);
        XWPFRun mainTitleRun = mainTitle.createRun();
        mainTitleRun.setText("点检记录导出");
        mainTitleRun.setFontSize(20);
        mainTitleRun.setBold(true);

        for (int i = 0; i < records.size(); i++)
        {
            if (i > 0)
            {
                XWPFParagraph pageBreak = doc.createParagraph();
                pageBreak.createRun().addBreak(BreakType.PAGE);
            }
            InspectionRecord record = records.get(i);
            String dateStr = record.getInspectionDate() != null ? sdf.format(record.getInspectionDate()) : "";
            appendRecordToDocument(doc, record, dateStr);
        }

        doc.write(outputStream);
        doc.close();
    }

    /**
     * 导出点检记录 Excel。
     * 该导出保留原 Word 接口的同时，针对富文本图片采用更适合表格浏览的 Excel 布局。
     *
     * @param recordIds 点检记录主键集合
     * @param beginDate 开始日期
     * @param endDate 结束日期
     * @param outputStream 输出流
     * @throws IOException io异常
     */
    @Override
    public void exportInspectionRecordsExcel(Long[] recordIds, Date beginDate, Date endDate, OutputStream outputStream) throws IOException
    {
        List<InspectionRecord> records = queryExportRecords(recordIds, beginDate, endDate);
        XSSFWorkbook workbook = new XSSFWorkbook();
        try
        {
            Sheet sheet = workbook.createSheet(EXCEL_SHEET_NAME);
            sheet.setColumnWidth(0, 40 * 256);
            sheet.setColumnWidth(1, 24 * 256);
            sheet.setColumnWidth(2, EXCEL_IMAGE_COLUMN_WIDTH);

            CellStyle titleStyle = createExcelTitleStyle(workbook);
            CellStyle headerStyle = createExcelHeaderStyle(workbook);
            CellStyle textStyle = createExcelTextStyle(workbook);
            CellStyle imageTextStyle = createExcelImageTextStyle(workbook);

            XSSFDrawing drawing = (XSSFDrawing) sheet.createDrawingPatriarch();
            int rowIndex = 0;
            for (InspectionRecord record : records)
            {
                rowIndex = appendRecordToSheet(sheet, drawing, record, rowIndex, titleStyle, headerStyle, textStyle, imageTextStyle);
            }
            workbook.write(outputStream);
        }
        finally
        {
            workbook.close();
        }
    }

    /**
     * 查询导出所需的点检记录及其明细。
     * Word 与 Excel 导出共用同一份数据装配逻辑，避免不同导出格式出现数据范围不一致。
     *
     * @param recordIds 点检记录主键集合
     * @param beginDate 开始日期
     * @param endDate 结束日期
     * @return 导出记录列表
     */
    private List<InspectionRecord> queryExportRecords(Long[] recordIds, Date beginDate, Date endDate)
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
        return records;
    }

    /**
     * 将单条点检记录写入 Word 文档。
     * 该方法负责当前记录的标题、基础信息表格以及明细表格渲染。
     *
     * @param doc Word 文档对象
     * @param record 点检记录
     * @param dateStr 格式化后的点检日期
     */
    private void appendRecordToDocument(XWPFDocument doc, InspectionRecord record, String dateStr)
    {
        XWPFParagraph titlePara = doc.createParagraph();
        titlePara.setAlignment(ParagraphAlignment.CENTER);
        titlePara.setSpacingAfter(200);
        XWPFRun titleRun = titlePara.createRun();
        titleRun.setText("点检记录 - " + record.getRecordTitle());
        titleRun.setFontSize(16);
        titleRun.setBold(true);

        XWPFTable infoTable = doc.createTable(1, 6);
        infoTable.setWidth("4500");
        setTableBorder(infoTable);

        String[][] infoData = {
            {"点检日期", dateStr, "点检人", StringUtils.defaultString(record.getInspector()), "状态", "COMPLETED".equals(record.getStatus()) ? "已完成" : "草稿"}
        };
        fillInfoTable(infoTable, infoData);

        doc.createParagraph();

        XWPFParagraph detailTitle = doc.createParagraph();
        XWPFRun detailTitleRun = detailTitle.createRun();
        detailTitleRun.setText("点检明细");
        detailTitleRun.setBold(true);
        detailTitleRun.setFontSize(12);

        List<InspectionRecordDetail> details = record.getDetails();
        XWPFTable detailTable = doc.createTable();
        detailTable.setWidth("7500");
        setTableBorder(detailTable);

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

        if (StringUtils.isNotEmpty(details))
        {
            for (InspectionRecordDetail detail : details)
            {
                XWPFTableRow dataRow = detailTable.createRow();
                String[] values = {
                    StringUtils.defaultString(detail.getItemPath()),
                    StringUtils.defaultString(detail.getFieldLabel())
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
                XWPFTableCell valueCell = dataRow.getCell(2);
                if (valueCell == null)
                {
                    valueCell = dataRow.createCell();
                }
                valueCell.setWidth(colWidths[2] + "");
                writeFieldValue(valueCell, detail);
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
    }

    /**
     * 将单条点检记录追加到 Excel 工作表。
     * 每条记录独立占用一段区域，既保留记录头信息，也便于后续人工筛选与查看图片。
     *
     * @param sheet 工作表
     * @param drawing 图片画布
     * @param record 点检记录
     * @param startRowIndex 起始行号
     * @param titleStyle 标题样式
     * @param headerStyle 表头样式
     * @param textStyle 文本样式
     * @param imageTextStyle 图文样式
     * @return 下一条记录的起始行号
     */
    private int appendRecordToSheet(Sheet sheet, XSSFDrawing drawing, InspectionRecord record, int startRowIndex, CellStyle titleStyle,
        CellStyle headerStyle, CellStyle textStyle, CellStyle imageTextStyle)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Row titleRow = sheet.createRow(startRowIndex++);
        titleRow.setHeight((short) 520);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("点检记录 - " + StringUtils.defaultString(record.getRecordTitle()));
        titleCell.setCellStyle(titleStyle);
        titleRow.createCell(1).setCellStyle(titleStyle);
        titleRow.createCell(2).setCellStyle(titleStyle);

        Row infoRow = sheet.createRow(startRowIndex++);
        infoRow.setHeight((short) 420);
        createStyledCell(infoRow, 0, buildRecordMeta(record, sdf), textStyle);
        createStyledCell(infoRow, 1, "", textStyle);
        createStyledCell(infoRow, 2, "", textStyle);

        Row headerRow = sheet.createRow(startRowIndex++);
        headerRow.setHeight((short) 420);
        createStyledCell(headerRow, 0, "点检路径", headerStyle);
        createStyledCell(headerRow, 1, "填写点", headerStyle);
        createStyledCell(headerRow, 2, "填写值", headerStyle);

        if (StringUtils.isEmpty(record.getDetails()))
        {
            Row emptyRow = sheet.createRow(startRowIndex++);
            emptyRow.setHeight(EXCEL_TEXT_ROW_HEIGHT);
            createStyledCell(emptyRow, 0, "暂无明细数据", textStyle);
            createStyledCell(emptyRow, 1, "", textStyle);
            createStyledCell(emptyRow, 2, "", textStyle);
            return startRowIndex + 1;
        }

        for (InspectionRecordDetail detail : record.getDetails())
        {
            Row dataRow = sheet.createRow(startRowIndex++);
            boolean hasImage = isRichTextWithImage(detail);
            dataRow.setHeight(hasImage ? EXCEL_IMAGE_ROW_HEIGHT : EXCEL_TEXT_ROW_HEIGHT);
            createStyledCell(dataRow, 0, StringUtils.defaultString(detail.getItemPath()), textStyle);
            createStyledCell(dataRow, 1, StringUtils.defaultString(detail.getFieldLabel()), textStyle);
            Cell valueCell = createStyledCell(dataRow, 2, buildExcelCellText(detail), hasImage ? imageTextStyle : textStyle);
            if (hasImage)
            {
                appendImageToExcelCell(sheet, drawing, valueCell, extractFirstImage(detail.getFieldValue()));
            }
        }
        return startRowIndex + 1;
    }

    /**
     * 组装记录头部摘要信息。
     *
     * @param record 点检记录
     * @param sdf 日期格式化器
     * @return 摘要文本
     */
    private String buildRecordMeta(InspectionRecord record, SimpleDateFormat sdf)
    {
        String inspectionDate = record.getInspectionDate() != null ? sdf.format(record.getInspectionDate()) : "";
        String status = "COMPLETED".equals(record.getStatus()) ? "已完成" : "草稿";
        return "点检日期：" + inspectionDate + "    点检人：" + StringUtils.defaultString(record.getInspector()) + "    状态：" + status;
    }

    /**
     * 创建并填充 Excel 单元格。
     *
     * @param row 行对象
     * @param columnIndex 列索引
     * @param value 单元格值
     * @param style 单元格样式
     * @return 单元格对象
     */
    private Cell createStyledCell(Row row, int columnIndex, String value, CellStyle style)
    {
        Cell cell = row.createCell(columnIndex);
        cell.setCellStyle(style);
        cell.setCellValue(StringUtils.defaultString(value));
        return cell;
    }

    /**
     * 创建 Excel 标题样式。
     *
     * @param workbook 工作簿
     * @return 标题样式
     */
    private CellStyle createExcelTitleStyle(Workbook workbook)
    {
        CellStyle style = createBaseExcelStyle(workbook);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * 创建 Excel 表头样式。
     *
     * @param workbook 工作簿
     * @return 表头样式
     */
    private CellStyle createExcelHeaderStyle(Workbook workbook)
    {
        CellStyle style = createBaseExcelStyle(workbook);
        style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * 创建 Excel 普通文本样式。
     *
     * @param workbook 工作簿
     * @return 普通文本样式
     */
    private CellStyle createExcelTextStyle(Workbook workbook)
    {
        CellStyle style = createBaseExcelStyle(workbook);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    /**
     * 创建 Excel 图文单元格样式。
     * 为图片预留顶部空间，减少文字与图片重叠的视觉问题。
     *
     * @param workbook 工作簿
     * @return 图文样式
     */
    private CellStyle createExcelImageTextStyle(Workbook workbook)
    {
        CellStyle style = createExcelTextStyle(workbook);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        return style;
    }

    /**
     * 创建 Excel 基础边框样式。
     *
     * @param workbook 工作簿
     * @return 基础样式
     */
    private CellStyle createBaseExcelStyle(Workbook workbook)
    {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * 判断当前明细是否为带图片的富文本。
     *
     * @param detail 点检明细
     * @return 是否包含图片
     */
    private boolean isRichTextWithImage(InspectionRecordDetail detail)
    {
        return "RICHTEXT".equals(detail.getFieldType()) && StringUtils.isNotEmpty(extractFirstImage(detail.getFieldValue()));
    }

    /**
     * 构建 Excel 中显示的单元格文本。
     * 对富文本仅保留纯文本摘要，并在有图片时追加提示，避免在 Excel 中模拟复杂图文混排。
     *
     * @param detail 点检明细
     * @return 单元格文本
     */
    private String buildExcelCellText(InspectionRecordDetail detail)
    {
        if (!"RICHTEXT".equals(detail.getFieldType()))
        {
            return fieldValueDisplay(detail);
        }
        String text = stripHtml(StringUtils.defaultString(detail.getFieldValue()));
        String firstImage = extractFirstImage(detail.getFieldValue());
        if (StringUtils.isEmpty(firstImage))
        {
            return text;
        }
        if (StringUtils.isEmpty(text))
        {
            return "图片见单元格内";
        }
        return text + "\n\n图片见单元格内";
    }

    /**
     * 提取富文本中的第一张图片地址。
     * 当前 Excel 版本优先验证单图展示效果，避免多图叠加导致单元格进一步失真。
     *
     * @param html 富文本 HTML
     * @return 第一张图片地址
     */
    private String extractFirstImage(String html)
    {
        if (StringUtils.isEmpty(html))
        {
            return null;
        }
        Matcher matcher = IMG_TAG_PATTERN.matcher(html);
        return matcher.find() ? matcher.group(2) : null;
    }

    /**
     * 向 Excel 指定单元格插入图片。
     * 图片加载或插入失败时保留原单元格文本，不中断整份导出。
     *
     * @param sheet 工作表
     * @param drawing 图片画布
     * @param cell 目标单元格
     * @param imageSrc 图片地址
     */
    private void appendImageToExcelCell(Sheet sheet, XSSFDrawing drawing, Cell cell, String imageSrc)
    {
        if (StringUtils.isEmpty(imageSrc))
        {
            return;
        }
        byte[] imageBytes = ImageUtils.getImage(imageSrc);
        if (imageBytes == null || imageBytes.length == 0)
        {
            return;
        }
        int pictureIndex = sheet.getWorkbook().addPicture(imageBytes, new ExcelUtil<InspectionRecord>(InspectionRecord.class).getImageType(imageBytes));
        ClientAnchor anchor = new XSSFClientAnchor(Units.pixelToEMU(4), Units.pixelToEMU(36), Units.pixelToEMU(4), Units.pixelToEMU(4),
            cell.getColumnIndex(), cell.getRowIndex(), cell.getColumnIndex() + 1, cell.getRowIndex() + 1);
        drawing.createPicture(anchor, pictureIndex);
    }

    private void setTableBorder(XWPFTable table)
    {
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

    /**
     * 根据字段类型向单元格写入导出内容。
     * 富文本字段走图文混排渲染，其他类型仍保持原有纯文本导出逻辑，避免破坏旧字段兼容性。
     *
     * @param cell Word 单元格
     * @param detail 点检明细
     */
    private void writeFieldValue(XWPFTableCell cell, InspectionRecordDetail detail)
    {
        clearCellParagraphs(cell);
        if ("RICHTEXT".equals(detail.getFieldType()))
        {
            writeRichText(cell, detail.getFieldValue());
            return;
        }
        XWPFParagraph paragraph = cell.addParagraph();
        paragraph.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun run = paragraph.createRun();
        run.setFontSize(10);
        run.setText(fieldValueDisplay(detail));
    }

    /**
     * 清空单元格中已有段落。
     * 先移除默认段落后再写入内容，避免追加渲染导致空行或重复内容。
     *
     * @param cell Word 单元格
     */
    private void clearCellParagraphs(XWPFTableCell cell)
    {
        int paragraphCount = cell.getParagraphs().size();
        for (int i = paragraphCount - 1; i >= 0; i--)
        {
            cell.removeParagraph(i);
        }
    }

    /**
     * 将富文本内容写入 Word 单元格。
     * 当前实现会按顺序保留文本、换行和图片，解析失败时退化为纯文本导出，避免导出整体失败。
     *
     * @param cell Word 单元格
     * @param html 富文本 HTML
     */
    private void writeRichText(XWPFTableCell cell, String html)
    {
        if (StringUtils.isEmpty(html))
        {
            XWPFParagraph paragraph = cell.addParagraph();
            paragraph.setAlignment(ParagraphAlignment.LEFT);
            paragraph.createRun().setText("");
            return;
        }
        List<RichTextSegment> segments = parseRichTextSegments(html);
        if (segments.isEmpty())
        {
            XWPFParagraph paragraph = cell.addParagraph();
            paragraph.setAlignment(ParagraphAlignment.LEFT);
            XWPFRun run = paragraph.createRun();
            run.setFontSize(10);
            run.setText(stripHtml(html));
            return;
        }
        XWPFParagraph paragraph = cell.addParagraph();
        paragraph.setAlignment(ParagraphAlignment.LEFT);
        boolean paragraphHasContent = false;
        for (RichTextSegment segment : segments)
        {
            if (segment.isLineBreak())
            {
                paragraph = cell.addParagraph();
                paragraph.setAlignment(ParagraphAlignment.LEFT);
                paragraphHasContent = false;
                continue;
            }
            if (segment.isImage())
            {
                if (paragraphHasContent)
                {
                    paragraph = cell.addParagraph();
                    paragraph.setAlignment(ParagraphAlignment.LEFT);
                }
                appendImage(paragraph, segment.getValue());
                paragraph = cell.addParagraph();
                paragraph.setAlignment(ParagraphAlignment.LEFT);
                paragraphHasContent = false;
                continue;
            }
            String text = normalizeText(segment.getValue());
            if (StringUtils.isEmpty(text))
            {
                continue;
            }
            XWPFRun run = paragraph.createRun();
            run.setFontSize(10);
            run.setText(text);
            paragraphHasContent = true;
        }
        if (!paragraphHasContent && cell.getParagraphs().isEmpty())
        {
            cell.addParagraph();
        }
    }

    /**
     * 将富文本中的图片插入到 Word 段落中。
     * 图片读取失败或插入失败时不抛出异常中断导出，而是输出失败提示，方便定位具体图片问题。
     *
     * @param paragraph Word 段落
     * @param imageSrc 图片地址
     */
    private void appendImage(XWPFParagraph paragraph, String imageSrc)
    {
        byte[] imageBytes = ImageUtils.getImage(imageSrc);
        if (imageBytes == null || imageBytes.length == 0)
        {
            XWPFRun run = paragraph.createRun();
            run.setFontSize(10);
            run.setText("[图片加载失败] " + imageSrc);
            return;
        }
        try
        {
            int pictureType = getPictureType(imageBytes);
            XWPFRun run = paragraph.createRun();
            run.addPicture(ImageUtils.getFile(imageSrc), pictureType, extractImageName(imageSrc), Units.toEMU(EXPORT_IMAGE_MAX_WIDTH), Units.toEMU(EXPORT_IMAGE_MAX_HEIGHT));
            run.addBreak();
        }
        catch (IOException | InvalidFormatException e)
        {
            XWPFRun run = paragraph.createRun();
            run.setFontSize(10);
            run.setText("[图片插入失败] " + imageSrc);
        }
    }

    /**
     * 根据图片字节内容推断 Word 所需的图片类型。
     * 这里复用现有 ExcelUtil 的图片类型识别能力，避免重复维护文件头判断逻辑。
     *
     * @param imageBytes 图片字节数组
     * @return POI Word 图片类型常量
     */
    private int getPictureType(byte[] imageBytes)
    {
        int workbookPictureType = new ExcelUtil<InspectionRecord>(InspectionRecord.class).getImageType(imageBytes);
        switch (workbookPictureType)
        {
            case org.apache.poi.ss.usermodel.Workbook.PICTURE_TYPE_PNG:
                return Document.PICTURE_TYPE_PNG;
            case org.apache.poi.ss.usermodel.Workbook.PICTURE_TYPE_JPEG:
                return Document.PICTURE_TYPE_JPEG;
            default:
                return Document.PICTURE_TYPE_JPEG;
        }
    }

    /**
     * 从图片地址中提取文件名。
     * 提取时会去掉查询参数，避免 Word 中的图片名称包含无意义的临时签名或查询串。
     *
     * @param imageSrc 图片地址
     * @return 图片文件名
     */
    private String extractImageName(String imageSrc)
    {
        if (StringUtils.isEmpty(imageSrc))
        {
            return "image";
        }
        int queryIndex = imageSrc.indexOf('?');
        String path = queryIndex >= 0 ? imageSrc.substring(0, queryIndex) : imageSrc;
        int slashIndex = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return slashIndex >= 0 ? path.substring(slashIndex + 1) : path;
    }

    /**
     * 将富文本 HTML 解析为顺序片段列表。
     * 片段分为文本、图片和换行三类，若解析失败则自动退化为纯文本，保证旧数据或异常 HTML 也能导出。
     *
     * @param html 富文本 HTML
     * @return 富文本片段列表
     */
    private List<RichTextSegment> parseRichTextSegments(String html)
    {
        String normalizedHtml = normalizeRichHtml(html);
        List<RichTextSegment> segments = new ArrayList<>();
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document document = builder.parse(new InputSource(new StringReader("<root>" + normalizedHtml + "</root>")));
            collectSegments(document.getDocumentElement().getChildNodes(), segments);
        }
        catch (Exception e)
        {
            segments.clear();
            segments.add(RichTextSegment.text(stripHtml(html)));
        }
        return mergeAdjacentTextSegments(segments);
    }

    /**
     * 深度遍历 DOM 节点并收集导出片段。
     * 仅抽取导出需要的核心信息：文本、图片和块级换行，避免把复杂样式一并引入 Word 导出逻辑。
     *
     * @param nodes 当前层节点列表
     * @param segments 收集到的片段列表
     */
    private void collectSegments(NodeList nodes, List<RichTextSegment> segments)
    {
        for (int i = 0; i < nodes.getLength(); i++)
        {
            Node node = nodes.item(i);
            short nodeType = node.getNodeType();
            if (nodeType == Node.TEXT_NODE || nodeType == Node.CDATA_SECTION_NODE)
            {
                segments.add(RichTextSegment.text(node.getNodeValue()));
                continue;
            }
            if (nodeType != Node.ELEMENT_NODE)
            {
                continue;
            }
            String nodeName = node.getNodeName().toLowerCase();
            if ("img".equals(nodeName))
            {
                Node srcNode = node.getAttributes() != null ? node.getAttributes().getNamedItem("src") : null;
                if (srcNode != null && StringUtils.isNotEmpty(srcNode.getNodeValue()))
                {
                    segments.add(RichTextSegment.image(srcNode.getNodeValue()));
                }
                continue;
            }
            boolean blockNode = isBlockNode(nodeName);
            if (blockNode && !segments.isEmpty() && !segments.get(segments.size() - 1).isLineBreak())
            {
                segments.add(RichTextSegment.lineBreak());
            }
            collectSegments(node.getChildNodes(), segments);
            if (blockNode && !segments.isEmpty() && !segments.get(segments.size() - 1).isLineBreak())
            {
                segments.add(RichTextSegment.lineBreak());
            }
        }
    }

    /**
     * 判断当前 HTML 节点是否应视为块级结构。
     * 块级节点在导出到 Word 时需要尽量保留换行语义，避免文本全部粘连在一起。
     *
     * @param nodeName 节点名称
     * @return 是否为块级节点
     */
    private boolean isBlockNode(String nodeName)
    {
        return "p".equals(nodeName) || "div".equals(nodeName) || "section".equals(nodeName) || "ul".equals(nodeName)
            || "ol".equals(nodeName) || "li".equals(nodeName) || "table".equals(nodeName) || "tr".equals(nodeName);
    }

    /**
     * 合并相邻文本片段并清理多余换行片段。
     * 这样可以减少 Word 中不必要的 run 数量，避免导出后出现碎片化文本。
     *
     * @param segments 原始片段列表
     * @return 合并后的片段列表
     */
    private List<RichTextSegment> mergeAdjacentTextSegments(List<RichTextSegment> segments)
    {
        List<RichTextSegment> merged = new ArrayList<>();
        StringBuilder currentText = new StringBuilder();
        for (RichTextSegment segment : segments)
        {
            if (segment.getType() == RichTextSegmentType.TEXT)
            {
                currentText.append(segment.getValue());
                continue;
            }
            appendMergedText(merged, currentText);
            if (segment.isLineBreak())
            {
                if (merged.isEmpty() || !merged.get(merged.size() - 1).isLineBreak())
                {
                    merged.add(segment);
                }
            }
            else
            {
                merged.add(segment);
            }
        }
        appendMergedText(merged, currentText);
        while (!merged.isEmpty() && merged.get(merged.size() - 1).isLineBreak())
        {
            merged.remove(merged.size() - 1);
        }
        return merged;
    }

    /**
     * 将暂存的连续文本追加到结果列表中。
     *
     * @param merged 合并后的片段列表
     * @param currentText 当前累积文本
     */
    private void appendMergedText(List<RichTextSegment> merged, StringBuilder currentText)
    {
        if (currentText.length() == 0)
        {
            return;
        }
        merged.add(RichTextSegment.text(currentText.toString()));
        currentText.setLength(0);
    }

    /**
     * 规范化富文本 HTML，降低 XML 解析失败概率。
     * 主要处理换行标签、自闭合图片标签以及部分对导出无意义的标签。
     *
     * @param html 原始 HTML
     * @return 规范化后的 HTML
     */
    private String normalizeRichHtml(String html)
    {
        String normalized = StringUtils.defaultString(html);
        normalized = BR_TAG_PATTERN.matcher(normalized).replaceAll("<br/>");
        Matcher imgMatcher = IMG_TAG_PATTERN.matcher(normalized);
        StringBuffer buffer = new StringBuffer();
        while (imgMatcher.find())
        {
            String src = imgMatcher.group(2);
            String replacement = "<img src=\"" + escapeXmlAttribute(src) + "\"/>";
            imgMatcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }
        imgMatcher.appendTail(buffer);
        normalized = buffer.toString();
        normalized = normalized.replaceAll("(?i)<(/?)(hr|input|meta|link)([^>]*)>", "");
        return normalized;
    }

    /**
     * 转义 XML 属性中的特殊字符。
     * 避免图片地址中包含特殊符号时导致 DOM 解析失败。
     *
     * @param value 原始属性值
     * @return 转义后的属性值
     */
    private String escapeXmlAttribute(String value)
    {
        return StringUtils.defaultString(value).replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /**
     * 规范化文本内容并还原常见 HTML 实体。
     *
     * @param text 原始文本
     * @return 规范化后的文本
     */
    private String normalizeText(String text)
    {
        if (StringUtils.isEmpty(text))
        {
            return "";
        }
        String normalized = text.replace("\u00A0", " ");
        normalized = normalized.replace("&nbsp;", " ").replace("&lt;", "<").replace("&gt;", ">")
            .replace("&amp;", "&").replace("&quot;", "\"");
        return normalized;
    }

    /**
     * 将字段值转换为导出用文本。
     * 富文本字段在纯文本场景下仍保留原有去标签行为，作为解析失败时的兼容兜底。
     *
     * @param detail 点检明细
     * @return 导出文本
     */
    private String fieldValueDisplay(InspectionRecordDetail detail)
    {
        String val = detail.getFieldValue();
        if ("CHECKBOX".equals(detail.getFieldType()))
        {
            return "Y".equals(val) ? "是" : ("N".equals(val) ? "否" : "");
        }
        if ("RICHTEXT".equals(detail.getFieldType()))
        {
            return stripHtml(StringUtils.defaultString(val));
        }
        return StringUtils.defaultString(val);
    }

    /**
     * 去除 HTML 标签并还原常见实体字符。
     * 该方法用于富文本解析失败时的纯文本降级输出。
     *
     * @param html 原始 HTML
     * @return 去标签后的文本
     */
    private String stripHtml(String html)
    {
        if (StringUtils.isEmpty(html))
        {
            return "";
        }
        return html.replaceAll("<[^>]+>", "").replaceAll("&nbsp;", " ").replaceAll("&lt;", "<")
            .replaceAll("&gt;", ">").replaceAll("&amp;", "&").replaceAll("&quot;", "\"").trim();
    }

    /**
     * 富文本导出片段类型定义。
     */
    private enum RichTextSegmentType
    {
        TEXT,
        IMAGE,
        LINE_BREAK
    }

    /**
     * 富文本导出片段。
     * 用于在 HTML 解析完成后，以统一结构在 Word 中按顺序输出文本、图片和换行。
     */
    private static class RichTextSegment
    {
        private final RichTextSegmentType type;
        private final String value;

        /**
         * 创建富文本导出片段。
         *
         * @param type 片段类型
         * @param value 片段值
         */
        private RichTextSegment(RichTextSegmentType type, String value)
        {
            this.type = type;
            this.value = value;
        }

        /**
         * 创建文本片段。
         *
         * @param value 文本值
         * @return 文本片段
         */
        private static RichTextSegment text(String value)
        {
            return new RichTextSegment(RichTextSegmentType.TEXT, value);
        }

        /**
         * 创建图片片段。
         *
         * @param value 图片地址
         * @return 图片片段
         */
        private static RichTextSegment image(String value)
        {
            return new RichTextSegment(RichTextSegmentType.IMAGE, value);
        }

        /**
         * 创建换行片段。
         *
         * @return 换行片段
         */
        private static RichTextSegment lineBreak()
        {
            return new RichTextSegment(RichTextSegmentType.LINE_BREAK, StringUtils.EMPTY);
        }

        /**
         * 获取片段类型。
         *
         * @return 片段类型
         */
        private RichTextSegmentType getType()
        {
            return type;
        }

        /**
         * 获取片段值。
         *
         * @return 片段值
         */
        private String getValue()
        {
            return value;
        }

        /**
         * 是否为图片片段。
         *
         * @return 是否为图片片段
         */
        private boolean isImage()
        {
            return type == RichTextSegmentType.IMAGE;
        }

        /**
         * 是否为换行片段。
         *
         * @return 是否为换行片段
         */
        private boolean isLineBreak()
        {
            return type == RichTextSegmentType.LINE_BREAK;
        }
    }
}
