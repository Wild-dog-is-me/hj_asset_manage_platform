package com.ruoyi.manage.platform.domain;

import java.util.Date;
import java.util.List;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

@TableName(value = "inspection_record", excludeProperty = {"searchValue", "params"})
public class InspectionRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @TableId(value = "record_id", type = IdType.AUTO)
    private Long recordId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date inspectionDate;

    private String recordTitle;

    private String inspector;

    private String status;

    @TableField(exist = false)
    private List<InspectionRecordDetail> details;

    public Long getRecordId()
    {
        return recordId;
    }

    public void setRecordId(Long recordId)
    {
        this.recordId = recordId;
    }

    public Date getInspectionDate()
    {
        return inspectionDate;
    }

    public void setInspectionDate(Date inspectionDate)
    {
        this.inspectionDate = inspectionDate;
    }

    public String getRecordTitle()
    {
        return recordTitle;
    }

    public void setRecordTitle(String recordTitle)
    {
        this.recordTitle = recordTitle;
    }

    public String getInspector()
    {
        return inspector;
    }

    public void setInspector(String inspector)
    {
        this.inspector = inspector;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public List<InspectionRecordDetail> getDetails()
    {
        return details;
    }

    public void setDetails(List<InspectionRecordDetail> details)
    {
        this.details = details;
    }
}
