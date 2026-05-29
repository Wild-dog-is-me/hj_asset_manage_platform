package com.ruoyi.manage.platform.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.manage.platform.domain.InspectionRecord;

/**
 * 点检记录服务接口。
 */
public interface IInspectionRecordService extends IService<InspectionRecord>
{
    /**
     * 查询点检记录列表。
     *
     * @param inspectionRecord 查询条件
     * @return 点检记录列表
     */
    public List<InspectionRecord> selectInspectionRecordList(InspectionRecord inspectionRecord);

    /**
     * 根据主键查询点检记录详情。
     *
     * @param recordId 点检记录主键
     * @return 点检记录详情
     */
    public InspectionRecord selectInspectionRecordByRecordId(Long recordId);

    /**
     * 构建点检记录模板。
     *
     * @param inspectionRecord 点检记录
     * @return 带模板明细的点检记录
     */
    public InspectionRecord buildInspectionRecordTemplate(InspectionRecord inspectionRecord);

    /**
     * 新增点检记录。
     *
     * @param inspectionRecord 点检记录
     * @return 影响行数
     */
    public int insertInspectionRecord(InspectionRecord inspectionRecord);

    /**
     * 修改点检记录。
     *
     * @param inspectionRecord 点检记录
     * @return 影响行数
     */
    public int updateInspectionRecord(InspectionRecord inspectionRecord);

    /**
     * 批量删除点检记录。
     *
     * @param recordIds 点检记录主键集合
     * @return 影响行数
     */
    public int deleteInspectionRecordByRecordIds(Long[] recordIds);

    /**
     * 导出点检记录 Word。
     *
     * @param recordIds 点检记录主键集合
     * @param beginDate 开始日期
     * @param endDate 结束日期
     * @param outputStream 输出流
     * @throws IOException io异常
     */
    public void exportInspectionRecords(Long[] recordIds, Date beginDate, Date endDate, OutputStream outputStream) throws IOException;

    /**
     * 导出点检记录 Excel。
     *
     * @param recordIds 点检记录主键集合
     * @param beginDate 开始日期
     * @param endDate 结束日期
     * @param outputStream 输出流
     * @throws IOException io异常
     */
    public void exportInspectionRecordsExcel(Long[] recordIds, Date beginDate, Date endDate, OutputStream outputStream) throws IOException;
}
