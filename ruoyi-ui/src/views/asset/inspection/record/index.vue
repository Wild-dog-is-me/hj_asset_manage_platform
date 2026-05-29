<template>
  <div class="app-container">
    <el-card class="search-card" shadow="never" v-show="showSearch">
      <el-form :model="queryParams" ref="queryForm" size="small" label-width="88px">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="点检日期" prop="inspectionDate">
              <el-date-picker v-model="queryParams.inspectionDate" value-format="yyyy-MM-dd" type="date" placeholder="请选择点检日期" clearable class="search-control" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="记录标题" prop="recordTitle">
              <el-input v-model="queryParams.recordTitle" placeholder="请输入记录标题" clearable class="search-control" @keyup.enter.native="handleQuery" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="点检人" prop="inspector">
              <el-input v-model="queryParams.inspector" placeholder="请输入点检人" clearable class="search-control" @keyup.enter.native="handleQuery" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="状态" prop="status">
              <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="search-control">
                <el-option label="草稿" value="DRAFT" />
                <el-option label="已完成" value="COMPLETED" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="24">
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
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['manage:inspection:record:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['manage:inspection:record:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['manage:inspection:record:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['manage:inspection:record:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="recordList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="记录标题" align="center" prop="recordTitle" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column label="点检日期" align="center" prop="inspectionDate" width="120" />
      <el-table-column label="点检人" align="center" prop="inspector" width="110" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === 'COMPLETED'" type="success">已完成</el-tag>
          <el-tag v-else type="info">草稿</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" min-width="160" :show-overflow-tooltip="true" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['manage:inspection:record:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['manage:inspection:record:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="1280px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-row>
          <el-col :span="8">
            <el-form-item label="点检日期" prop="inspectionDate">
              <el-date-picker v-model="form.inspectionDate" value-format="yyyy-MM-dd" type="date" placeholder="请选择点检日期" class="form-control" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="记录标题" prop="recordTitle">
              <el-input v-model="form.recordTitle" placeholder="请输入记录标题" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="点检人" prop="inspector">
              <el-input v-model="form.inspector" placeholder="请输入点检人" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="8">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择状态" class="form-control">
                <el-option label="草稿" value="DRAFT" />
                <el-option label="已完成" value="COMPLETED" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">点检明细</el-divider>
        <el-alert title="新增记录时会按当前点检项目配置生成明细；后续配置变化不会影响已保存记录。" type="info" show-icon :closable="false" class="detail-alert" />
        <el-table :data="form.details" border size="mini" max-height="560">
          <el-table-column label="点检路径" prop="itemPath" min-width="260" :show-overflow-tooltip="true" />
          <el-table-column label="填写点" width="160">
            <template slot-scope="scope">
              <span>{{ scope.row.fieldLabel }}</span>
              <span v-if="scope.row.required === '1'" style="color: #F56C6C; margin-left: 2px;">*</span>
            </template>
          </el-table-column>
          <el-table-column label="填写值" min-width="260">
            <template slot-scope="scope">
              <el-checkbox v-if="scope.row.fieldType === 'CHECKBOX'" v-model="scope.row.fieldValue" true-label="Y" false-label="N">是</el-checkbox>
              <el-input-number v-else-if="scope.row.fieldType === 'NUMBER'" v-model="scope.row.fieldValue" controls-position="right" class="form-control" />
              <el-select v-else-if="scope.row.fieldType === 'SELECT'" v-model="scope.row.fieldValue" clearable class="form-control">
                <el-option v-for="option in splitOptions(scope.row.fieldOptions)" :key="option" :label="option" :value="option" />
              </el-select>
              <editor v-else-if="scope.row.fieldType === 'RICHTEXT'" v-model="scope.row.fieldValue" :min-height="180" class="richtext-editor" />
              <el-input v-else v-model="scope.row.fieldValue" placeholder="请输入" />
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="导出点检记录" :visible.sync="exportOpen" width="480px" append-to-body>
      <el-form ref="exportForm" :model="exportParams" label-width="100px">
        <el-form-item label="开始日期" prop="beginDate">
          <el-date-picker v-model="exportParams.beginDate" value-format="yyyy-MM-dd" type="date" placeholder="不选则不限制开始时间" clearable class="form-control" />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker v-model="exportParams.endDate" value-format="yyyy-MM-dd" type="date" placeholder="不选则不限制结束时间" clearable class="form-control" />
        </el-form-item>
        <el-alert title="导出会生成一个 Word 文件，多条记录使用分页符分隔" type="info" show-icon :closable="false" />
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" :loading="exportLoading" @click="confirmExport">确 定 导 出</el-button>
        <el-button @click="exportOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listInspectionRecord, getInspectionRecord, buildInspectionRecordTemplate, addInspectionRecord, updateInspectionRecord, delInspectionRecord } from "@/api/asset/inspection"
import request from '@/utils/request'
import { MessageBox } from 'element-ui'

export default {
  name: "InspectionRecord",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      recordList: [],
      open: false,
      title: "",
      exportOpen: false,
      exportLoading: false,
      exportParams: {
        beginDate: undefined,
        endDate: undefined
      },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        inspectionDate: undefined,
        recordTitle: undefined,
        inspector: undefined,
        status: undefined
      },
      form: {},
      rules: {
        inspectionDate: [{ required: true, message: "点检日期不能为空", trigger: "change" }],
        recordTitle: [{ required: true, message: "记录标题不能为空", trigger: "blur" }],
        status: [{ required: true, message: "状态不能为空", trigger: "change" }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listInspectionRecord(this.queryParams).then(response => {
        this.recordList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    reset() {
      const today = this.parseTime(new Date(), '{y}-{m}-{d}')
      this.form = {
        recordId: undefined,
        inspectionDate: today,
        recordTitle: today + ' 点检记录',
        inspector: undefined,
        status: 'DRAFT',
        remark: undefined,
        details: []
      }
      this.resetForm("form")
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.recordId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      buildInspectionRecordTemplate(this.form).then(response => {
        this.form = response.data
        this.open = true
        this.title = "新增点检记录"
      })
    },
    handleUpdate(row) {
      this.reset()
      const recordId = row.recordId || this.ids
      getInspectionRecord(recordId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改点检记录"
      })
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.status === 'COMPLETED') {
            const missingFields = []
            if (this.form.details) {
              this.form.details.forEach(d => {
                if (d.required === '1') {
                  const val = d.fieldValue
                  if (val === undefined || val === null || (typeof val === 'string' && val.trim() === '')) {
                    missingFields.push((d.itemPath || '') + ' / ' + (d.fieldLabel || ''))
                  }
                }
              })
            }
            if (missingFields.length > 0) {
              const msg = '以下必填字段尚未填写：<br/>' + missingFields.map((f, i) => (i + 1) + '. ' + f.replace(/\//g, ' <b style="color:#909399">&gt;</b> ')).join('<br/>')
              MessageBox.alert(msg, '系统提示', { dangerouslyUseHTMLString: true, type: 'error' })
              return
            }
          }
          if (this.form.recordId != undefined) {
            updateInspectionRecord(this.form).then(() => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addInspectionRecord(this.form).then(() => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    handleDelete(row) {
      const recordIds = row.recordId || this.ids
      this.$modal.confirm('是否确认删除点检记录编号为"' + recordIds + '"的数据项？').then(function() {
        return delInspectionRecord(recordIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    cancel() {
      this.open = false
      this.reset()
    },
    splitOptions(options) {
      if (!options) {
        return []
      }
      return options.split(',').filter(item => item)
    },
    handleExport() {
      if (this.ids.length > 0) {
        this.exportLoading = true
        const params = { recordIds: this.ids.join(',') }
        this.doExport(params)
      } else {
        this.exportParams.beginDate = undefined
        this.exportParams.endDate = undefined
        this.exportOpen = true
      }
    },
    confirmExport() {
      this.exportLoading = true
      const params = {}
      if (this.exportParams.beginDate) {
        params.beginDateStr = this.exportParams.beginDate
      }
      if (this.exportParams.endDate) {
        params.endDateStr = this.exportParams.endDate
      }
      this.doExport(params)
    },
    doExport(params) {
      request({
        url: '/manage/platform/inspection/record/export',
        method: 'get',
        params: params,
        responseType: 'blob'
      }).then(blob => {
        const url = window.URL.createObjectURL(new Blob([blob]))
        const link = document.createElement('a')
        link.href = url
        link.download = '点检记录导出_' + new Date().getTime() + '.docx'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        this.exportLoading = false
        this.exportOpen = false
      }).catch(() => {
        this.exportLoading = false
        this.exportOpen = false
      })
    }
  }
}
</script>

<style scoped>
.search-card {
  margin-bottom: 12px;
}

.search-card ::v-deep .el-card__body {
  padding: 18px 18px 2px;
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

.detail-alert {
  margin-bottom: 12px;
}

.richtext-editor {
  min-width: 380px;
}
</style>
