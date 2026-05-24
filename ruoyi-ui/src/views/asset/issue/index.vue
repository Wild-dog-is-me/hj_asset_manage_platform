<template>
  <div class="app-container">
    <el-card class="search-card" shadow="never" v-show="showSearch">
      <el-form :model="queryParams" ref="queryForm" size="small" label-width="96px">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="问题编号" prop="issueNo">
              <el-input v-model="queryParams.issueNo" placeholder="请输入问题编号" clearable class="search-control" @keyup.enter.native="handleQuery" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="资产编号" prop="assetNo">
              <el-input v-model="queryParams.assetNo" placeholder="请输入资产编号" clearable class="search-control" @keyup.enter.native="handleQuery" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="系统类别" prop="systemType">
              <el-select v-model="queryParams.systemType" placeholder="请选择系统类别" clearable class="search-control">
                <el-option v-for="dict in dict.type.issue_system_type" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="业务主体" prop="businessEntity">
              <el-select v-model="queryParams.businessEntity" placeholder="请选择业务主体" clearable class="search-control">
                <el-option v-for="dict in dict.type.issue_business_entity" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="处理状态" prop="processStatus">
              <el-select v-model="queryParams.processStatus" placeholder="请选择处理状态" clearable class="search-control">
                <el-option v-for="dict in dict.type.issue_process_status" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="紧急程度" prop="urgencyLevel">
              <el-select v-model="queryParams.urgencyLevel" placeholder="请选择紧急程度" clearable class="search-control">
                <el-option v-for="dict in dict.type.issue_urgency_level" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="申请人" prop="applicant">
              <el-input v-model="queryParams.applicant" placeholder="请输入申请人" clearable class="search-control" @keyup.enter.native="handleQuery" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="申请部门" prop="applicantDeptId">
              <treeselect :append-to-body="true" v-model="queryParams.applicantDeptId" :options="deptOptions" :normalizer="normalizer" placeholder="请选择申请部门" clearable class="search-control" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="问题标题" prop="issueTitle">
              <el-input v-model="queryParams.issueTitle" placeholder="请输入问题标题" clearable class="search-control" @keyup.enter.native="handleQuery" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="提交时间" prop="issueSubmitTime">
              <el-date-picker
                v-model="dateRange"
                class="search-control"
                value-format="yyyy-MM-dd"
                type="daterange"
                range-separator="-"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="当前工程师" prop="currentSupportEngineer">
              <el-input v-model="queryParams.currentSupportEngineer" placeholder="请输入当前支持工程师" clearable class="search-control" @keyup.enter.native="handleQuery" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="12">
            <el-form-item class="search-actions">
              <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
              <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['manage:issue:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['manage:issue:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['manage:issue:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['manage:issue:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="issueInfoList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="问题编号" align="center" prop="issueNo" width="150" fixed="left" :show-overflow-tooltip="true" />
      <el-table-column label="资产编号" align="center" prop="assetNo" width="140" :show-overflow-tooltip="true" />
      <el-table-column label="系统类别" align="center" prop="systemType" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.issue_system_type" :value="scope.row.systemType" />
        </template>
      </el-table-column>
      <el-table-column label="业务主体" align="center" prop="businessEntity" width="110">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.issue_business_entity" :value="scope.row.businessEntity" />
        </template>
      </el-table-column>
      <el-table-column label="问题标题" align="center" prop="issueTitle" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column label="紧急程度" align="center" prop="urgencyLevel" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.issue_urgency_level" :value="scope.row.urgencyLevel" />
        </template>
      </el-table-column>
      <el-table-column label="处理状态" align="center" prop="processStatus" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.issue_process_status" :value="scope.row.processStatus" />
        </template>
      </el-table-column>
      <el-table-column label="提交时间" align="center" prop="issueSubmitTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.issueSubmitTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="响应分钟" align="center" prop="responseMinutes" width="100" />
      <el-table-column label="申请人" align="center" prop="applicant" width="100" />
      <el-table-column label="申请部门" align="center" prop="applicantDeptName" width="140" :show-overflow-tooltip="true" />
      <el-table-column label="问题类别" align="center" prop="issueCategory" width="120" :show-overflow-tooltip="true" />
      <el-table-column label="KPI担责人" align="center" prop="kpiOwner" width="110" />
      <el-table-column label="当前工程师" align="center" prop="currentSupportEngineer" width="130" />
      <el-table-column label="是否自己解决" align="center" prop="selfResolved" width="120">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.issue_self_resolved" :value="scope.row.selfResolved" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['manage:issue:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['manage:issue:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="1100px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="问题编号" prop="issueNo">
              <el-input v-model="form.issueNo" placeholder="请输入问题编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资产编号" prop="assetNo">
              <el-input v-model="form.assetNo" placeholder="请输入资产编号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="系统类别" prop="systemType">
              <el-select v-model="form.systemType" placeholder="请选择系统类别" clearable class="form-control">
                <el-option v-for="dict in dict.type.issue_system_type" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="业务主体" prop="businessEntity">
              <el-select v-model="form.businessEntity" placeholder="请选择业务主体" clearable class="form-control">
                <el-option v-for="dict in dict.type.issue_business_entity" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="提交时间" prop="issueSubmitTime">
              <el-date-picker v-model="form.issueSubmitTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" placeholder="请选择问题提交时间" class="form-control" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="响应分钟" prop="responseMinutes">
              <el-input-number v-model="form.responseMinutes" controls-position="right" :min="0" :precision="0" class="form-control" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="申请人" prop="applicant">
              <el-input v-model="form.applicant" placeholder="请输入申请人" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="申请部门" prop="applicantDeptId">
              <treeselect v-model="form.applicantDeptId" :options="deptOptions" :normalizer="normalizer" placeholder="请选择申请部门" class="form-control" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="紧急程度" prop="urgencyLevel">
              <el-select v-model="form.urgencyLevel" placeholder="请选择紧急程度" clearable class="form-control">
                <el-option v-for="dict in dict.type.issue_urgency_level" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="问题类别" prop="issueCategory">
              <el-input v-model="form.issueCategory" placeholder="请输入问题类别" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="处理状态" prop="processStatus">
              <el-select v-model="form.processStatus" placeholder="请选择处理状态" clearable class="form-control">
                <el-option v-for="dict in dict.type.issue_process_status" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="KPI担责人" prop="kpiOwner">
              <el-input v-model="form.kpiOwner" placeholder="请输入KPI担责人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="当前工程师" prop="currentSupportEngineer">
              <el-input v-model="form.currentSupportEngineer" placeholder="请输入当前支持工程师" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="是否自己解决" prop="selfResolved">
              <el-select v-model="form.selfResolved" placeholder="请选择是否自己解决" clearable class="form-control">
                <el-option v-for="dict in dict.type.issue_self_resolved" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="问题标题" prop="issueTitle">
              <el-input v-model="form.issueTitle" placeholder="请输入问题标题" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="问题内容" prop="issueContent">
              <editor v-model="form.issueContent" :min-height="180" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="具体处理" prop="processDetail">
              <editor v-model="form.processDetail" :min-height="180" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listIssueInfo, getIssueInfo, delIssueInfo, addIssueInfo, updateIssueInfo } from "@/api/asset/issue"
import { listDept } from "@/api/system/dept"
import Treeselect from "@riophae/vue-treeselect"
import "@riophae/vue-treeselect/dist/vue-treeselect.css"

export default {
  name: "IssueInfo",
  dicts: ['issue_system_type', 'issue_business_entity', 'issue_urgency_level', 'issue_process_status', 'issue_self_resolved'],
  components: { Treeselect },
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      issueInfoList: [],
      deptOptions: [],
      dateRange: [],
      title: "",
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        issueNo: undefined,
        assetNo: undefined,
        systemType: undefined,
        businessEntity: undefined,
        applicant: undefined,
        applicantDeptId: undefined,
        issueTitle: undefined,
        urgencyLevel: undefined,
        processStatus: undefined,
        currentSupportEngineer: undefined
      },
      form: {},
      rules: {
        issueNo: [
          { required: true, message: "问题编号不能为空", trigger: "blur" }
        ],
        systemType: [
          { required: true, message: "系统类别不能为空", trigger: "change" }
        ],
        issueSubmitTime: [
          { required: true, message: "问题提交时间不能为空", trigger: "change" }
        ],
        businessEntity: [
          { required: true, message: "业务主体不能为空", trigger: "change" }
        ],
        issueTitle: [
          { required: true, message: "问题标题不能为空", trigger: "blur" }
        ],
        urgencyLevel: [
          { required: true, message: "紧急程度不能为空", trigger: "change" }
        ],
        processStatus: [
          { required: true, message: "处理状态不能为空", trigger: "change" }
        ]
      }
    }
  },
  created() {
    this.getList()
    this.getDeptTree()
  },
  methods: {
    getList() {
      this.loading = true
      listIssueInfo(this.addDateRange(this.queryParams, this.dateRange, 'IssueSubmitTime')).then(response => {
        this.issueInfoList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    getDeptTree() {
      listDept().then(response => {
        this.deptOptions = this.handleTree(response.data, "deptId")
      })
    },
    normalizer(node) {
      if (node.children && !node.children.length) {
        delete node.children
      }
      return {
        id: node.deptId,
        label: node.deptName,
        children: node.children
      }
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        issueId: undefined,
        issueNo: undefined,
        assetNo: undefined,
        systemType: undefined,
        issueSubmitTime: undefined,
        responseMinutes: undefined,
        businessEntity: undefined,
        applicant: undefined,
        applicantDeptId: undefined,
        issueTitle: undefined,
        issueContent: undefined,
        urgencyLevel: "NORMAL",
        issueCategory: undefined,
        processStatus: "UNPROCESSED",
        kpiOwner: undefined,
        currentSupportEngineer: undefined,
        processDetail: undefined,
        selfResolved: "N",
        remark: undefined
      }
      this.resetForm("form")
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.dateRange = []
      this.resetForm("queryForm")
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.issueId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加问题信息"
    },
    handleUpdate(row) {
      this.reset()
      const issueId = row.issueId || this.ids
      getIssueInfo(issueId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改问题信息"
      })
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.issueId != undefined) {
            updateIssueInfo(this.form).then(() => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addIssueInfo(this.form).then(() => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    handleDelete(row) {
      const issueIds = row.issueId || this.ids
      this.$modal.confirm('是否确认删除问题信息编号为"' + issueIds + '"的数据项？').then(function() {
        return delIssueInfo(issueIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    handleExport() {
      this.download('manage/platform/issue/export', {
        ...this.addDateRange(this.queryParams, this.dateRange, 'IssueSubmitTime')
      }, `issue_info_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>

<style scoped>
.search-card {
  margin-bottom: 12px;
  border-color: #ebeef5;
}

.search-card ::v-deep .el-card__body {
  padding: 18px 18px 2px;
}

.search-card ::v-deep .el-form-item {
  margin-bottom: 16px;
}

.search-control,
.form-control {
  width: 100%;
}

.search-actions {
  text-align: right;
}

.search-actions ::v-deep .el-form-item__content {
  margin-left: 0 !important;
}

.search-actions .el-button + .el-button {
  margin-left: 8px;
}
</style>

<style>
.vue-treeselect__portal-target {
  z-index: 3000;
}
</style>
