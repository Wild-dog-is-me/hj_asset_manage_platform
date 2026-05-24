package com.ruoyi.manage.platform.service;

import java.util.List;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.manage.platform.domain.InspectionItem;

public interface IInspectionItemService extends IService<InspectionItem>
{
    public List<InspectionItem> selectInspectionItemList(InspectionItem inspectionItem);

    public List<InspectionItem> selectInspectionItemTree();

    public InspectionItem selectInspectionItemByItemId(Long itemId);

    public int insertInspectionItem(InspectionItem inspectionItem);

    public int updateInspectionItem(InspectionItem inspectionItem);

    public int deleteInspectionItemByItemId(Long itemId);
}
