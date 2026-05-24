package com.ruoyi.manage.platform.service;

import java.util.List;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.manage.platform.domain.InspectionRecord;

public interface IInspectionRecordService extends IService<InspectionRecord>
{
    public List<InspectionRecord> selectInspectionRecordList(InspectionRecord inspectionRecord);

    public InspectionRecord selectInspectionRecordByRecordId(Long recordId);

    public InspectionRecord buildInspectionRecordTemplate(InspectionRecord inspectionRecord);

    public int insertInspectionRecord(InspectionRecord inspectionRecord);

    public int updateInspectionRecord(InspectionRecord inspectionRecord);

    public int deleteInspectionRecordByRecordIds(Long[] recordIds);
}
