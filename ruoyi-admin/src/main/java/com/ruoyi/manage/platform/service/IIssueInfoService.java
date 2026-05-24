package com.ruoyi.manage.platform.service;

import java.util.List;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.manage.platform.domain.IssueInfo;

public interface IIssueInfoService extends IService<IssueInfo>
{
    public List<IssueInfo> selectIssueInfoList(IssueInfo issueInfo);

    public IssueInfo selectIssueInfoByIssueId(Long issueId);

    public int insertIssueInfo(IssueInfo issueInfo);

    public int updateIssueInfo(IssueInfo issueInfo);

    public int deleteIssueInfoByIssueIds(Long[] issueIds);
}
