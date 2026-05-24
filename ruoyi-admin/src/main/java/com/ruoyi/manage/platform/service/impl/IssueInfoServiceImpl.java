package com.ruoyi.manage.platform.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.manage.platform.domain.IssueInfo;
import com.ruoyi.manage.platform.mapper.IssueInfoMapper;
import com.ruoyi.manage.platform.service.IIssueInfoService;
import com.ruoyi.system.service.ISysDeptService;

@Service
public class IssueInfoServiceImpl extends ServiceImpl<IssueInfoMapper, IssueInfo> implements IIssueInfoService
{
    @Autowired
    private ISysDeptService deptService;

    @Override
    public List<IssueInfo> selectIssueInfoList(IssueInfo issueInfo)
    {
        LambdaQueryWrapper<IssueInfo> queryWrapper = buildQueryWrapper(issueInfo);
        List<IssueInfo> list = list(queryWrapper);
        setApplicantDeptNames(list);
        return list;
    }

    @Override
    public IssueInfo selectIssueInfoByIssueId(Long issueId)
    {
        IssueInfo issueInfo = getById(issueId);
        setApplicantDeptName(issueInfo);
        return issueInfo;
    }

    @Override
    public int insertIssueInfo(IssueInfo issueInfo)
    {
        issueInfo.setCreateTime(DateUtils.getNowDate());
        return save(issueInfo) ? 1 : 0;
    }

    @Override
    public int updateIssueInfo(IssueInfo issueInfo)
    {
        issueInfo.setUpdateTime(DateUtils.getNowDate());
        return updateById(issueInfo) ? 1 : 0;
    }

    @Override
    public int deleteIssueInfoByIssueIds(Long[] issueIds)
    {
        return removeByIds(Arrays.asList(issueIds)) ? issueIds.length : 0;
    }

    private LambdaQueryWrapper<IssueInfo> buildQueryWrapper(IssueInfo issueInfo)
    {
        LambdaQueryWrapper<IssueInfo> queryWrapper = new LambdaQueryWrapper<IssueInfo>();
        queryWrapper.like(StringUtils.isNotEmpty(issueInfo.getIssueNo()), IssueInfo::getIssueNo, issueInfo.getIssueNo())
            .like(StringUtils.isNotEmpty(issueInfo.getAssetNo()), IssueInfo::getAssetNo, issueInfo.getAssetNo())
            .eq(StringUtils.isNotEmpty(issueInfo.getSystemType()), IssueInfo::getSystemType, issueInfo.getSystemType())
            .eq(StringUtils.isNotEmpty(issueInfo.getBusinessEntity()), IssueInfo::getBusinessEntity, issueInfo.getBusinessEntity())
            .like(StringUtils.isNotEmpty(issueInfo.getApplicant()), IssueInfo::getApplicant, issueInfo.getApplicant())
            .eq(StringUtils.isNotNull(issueInfo.getApplicantDeptId()), IssueInfo::getApplicantDeptId, issueInfo.getApplicantDeptId())
            .like(StringUtils.isNotEmpty(issueInfo.getIssueTitle()), IssueInfo::getIssueTitle, issueInfo.getIssueTitle())
            .eq(StringUtils.isNotEmpty(issueInfo.getUrgencyLevel()), IssueInfo::getUrgencyLevel, issueInfo.getUrgencyLevel())
            .like(StringUtils.isNotEmpty(issueInfo.getIssueCategory()), IssueInfo::getIssueCategory, issueInfo.getIssueCategory())
            .eq(StringUtils.isNotEmpty(issueInfo.getProcessStatus()), IssueInfo::getProcessStatus, issueInfo.getProcessStatus())
            .like(StringUtils.isNotEmpty(issueInfo.getKpiOwner()), IssueInfo::getKpiOwner, issueInfo.getKpiOwner())
            .like(StringUtils.isNotEmpty(issueInfo.getCurrentSupportEngineer()), IssueInfo::getCurrentSupportEngineer, issueInfo.getCurrentSupportEngineer())
            .eq(StringUtils.isNotEmpty(issueInfo.getSelfResolved()), IssueInfo::getSelfResolved, issueInfo.getSelfResolved())
            .ge(StringUtils.isNotNull(issueInfo.getParams().get("beginIssueSubmitTime")), IssueInfo::getIssueSubmitTime, issueInfo.getParams().get("beginIssueSubmitTime"))
            .le(StringUtils.isNotNull(issueInfo.getParams().get("endIssueSubmitTime")), IssueInfo::getIssueSubmitTime, issueInfo.getParams().get("endIssueSubmitTime"))
            .orderByDesc(IssueInfo::getIssueSubmitTime)
            .orderByDesc(IssueInfo::getIssueId);
        return queryWrapper;
    }

    private void setApplicantDeptNames(List<IssueInfo> list)
    {
        if (StringUtils.isEmpty(list))
        {
            return;
        }
        List<SysDept> depts = deptService.selectDeptList(new SysDept());
        Map<Long, SysDept> deptMap = depts.stream().collect(Collectors.toMap(SysDept::getDeptId, Function.identity(), (a, b) -> a));
        for (IssueInfo issueInfo : list)
        {
            if (StringUtils.isNotNull(issueInfo.getApplicantDeptId()) && deptMap.containsKey(issueInfo.getApplicantDeptId()))
            {
                issueInfo.setApplicantDeptName(deptMap.get(issueInfo.getApplicantDeptId()).getDeptName());
            }
        }
    }

    private void setApplicantDeptName(IssueInfo issueInfo)
    {
        if (StringUtils.isNull(issueInfo) || StringUtils.isNull(issueInfo.getApplicantDeptId()))
        {
            return;
        }
        SysDept dept = deptService.selectDeptById(issueInfo.getApplicantDeptId());
        if (StringUtils.isNotNull(dept))
        {
            issueInfo.setApplicantDeptName(dept.getDeptName());
        }
    }
}
