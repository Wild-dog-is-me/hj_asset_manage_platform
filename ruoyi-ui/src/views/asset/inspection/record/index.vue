<template>
  <div class="app-container">
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="88px">
        <el-form-item label="记录标题" prop="recordTitle">
          <el-input
            v-model="queryParams.recordTitle"
            placeholder="请输入记录标题"
            clearable
            size="small"
            class="search-control"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item label="点检人" prop="inspector">
          <el-input
            v-model="queryParams.inspector"
            placeholder="请输入点检人"
            clearable
            size="small"
            class="search-control"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item label="点检日期" prop="inspectionDate">
          <el-date-picker
            v-model="queryParams.inspectionDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择点检日期"
            clearable
            size="small"
            class="search-control"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable size="small" class="search-control">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
        </el-form-item>
        <el-form-item class="search-actions">
          <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['manage:inspection:record:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['manage:inspection:record:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['manage:inspection:record:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          :loading="exportLoading"
          @click="handleExport"
          v-hasPermi="['manage:inspection:record:export']"
        >导出</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="recordList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="记录编号" align="center" prop="recordId" width="100" />
      <el-table-column label="记录标题" align="center" prop="recordTitle" min-width="180" show-overflow-tooltip />
      <el-table-column label="点检日期" align="center" prop="inspectionDate" width="120">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.inspectionDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="点检人" align="center" prop="inspector" width="120" />
      <el-table-column label="状态" align="center" prop="status" width="120">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 'COMPLETED' ? 'success' : 'info'">
            {{ scope.row.status === 'COMPLETED' ? '已完成' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作" align="center" width="220" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['manage:inspection:record:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['manage:inspection:record:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog title="导出点检记录" :visible.sync="exportOpen" width="460px" append-to-body>
      <el-form :model="exportParams" label-width="88px">
        <el-form-item label="开始日期">
          <el-date-picker
            v-model="exportParams.beginDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择开始日期"
            clearable
            class="form-control"
          />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker
            v-model="exportParams.endDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择结束日期"
            clearable
            class="form-control"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button :loading="exportLoading" type="primary" @click="confirmExport">确 定</el-button>
        <el-button @click="exportOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="title" :visible.sync="open" width="1200px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="记录标题" prop="recordTitle">
              <el-input v-model="form.recordTitle" placeholder="请输入记录标题" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="点检日期" prop="inspectionDate">
              <el-date-picker
                v-model="form.inspectionDate"
                type="date"
                value-format="yyyy-MM-dd"
                placeholder="请选择点检日期"
                class="form-control"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="点检人" prop="inspector">
              <el-input v-model="form.inspector" placeholder="请输入点检人" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>

        <el-alert type="info" :closable="false" show-icon class="detail-alert" title="请按点检项填写对应内容，必填项在保存为已完成时会校验。" />

        <el-table :data="form.details" border>
          <el-table-column label="点检路径" prop="itemPath" min-width="220" show-overflow-tooltip />
          <el-table-column label="填写点" prop="fieldLabel" min-width="160" show-overflow-tooltip>
            <template slot-scope="scope">
              <span>{{ scope.row.fieldLabel }}</span>
              <el-tag v-if="scope.row.required === '1'" size="mini" type="danger" effect="plain" style="margin-left: 8px;">必填</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="填写值" min-width="300">
            <template slot-scope="scope">
              <component
                :is="resolveFieldComponent(scope.row)"
                v-model="scope.row.fieldValue"
                :row="scope.row"
              />
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm('DRAFT')">保存草稿</el-button>
        <el-button type="success" @click="submitForm('COMPLETED')">保存并完成</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'
import { listInspectionRecord, getInspectionRecord, delInspectionRecord, addInspectionRecord, updateInspectionRecord, buildInspectionRecordTemplate } from '@/api/asset/inspection'
import Editor from '@/components/Editor'

const TextField = {
  functional: true,
  props: {
    value: {
      type: String,
      default: ''
    }
  },
  render(h, { props, listeners }) {
    return h('el-input', {
      props: {
        value: props.value,
        placeholder: '请输入内容'
      },
      on: {
        input: value => listeners.input && listeners.input(value)
      }
    })
  }
}

const NumberField = {
  functional: true,
  props: {
    value: {
      type: [String, Number],
      default: ''
    }
  },
  render(h, { props, listeners }) {
    return h('el-input-number', {
      props: {
        value: props.value === '' ? undefined : Number(props.value),
        controlsPosition: 'right',
        min: 0,
        precision: 0,
        style: 'width: 100%;'
      },
      on: {
        input: value => listeners.input && listeners.input(value === undefined || value === null ? '' : String(value))
      }
    })
  }
}

const CheckboxField = {
  functional: true,
  props: {
    value: {
      type: String,
      default: ''
    }
  },
  render(h, { props, listeners }) {
    return h('el-radio-group', {
      props: { value: props.value },
      on: {
        input: value => listeners.input && listeners.input(value)
      }
    }, [
      h('el-radio', { props: { label: 'Y' } }, '是'),
      h('el-radio', { props: { label: 'N' } }, '否')
    ])
  }
}

const SelectField = {
  functional: true,
  props: {
    value: {
      type: String,
      default: ''
    },
    row: {
      type: Object,
      default: () => ({})
    }
  },
  render(h, { props, listeners }) {
    const options = (props.row.fieldOptions || '').split(',').filter(item => item)
    return h('el-select', {
      props: {
        value: props.value,
        placeholder: '请选择',
        clearable: true,
        style: 'width: 100%;'
      },
      on: {
        input: value => listeners.input && listeners.input(value)
      }
    }, options.map(item => h('el-option', {
      props: {
        label: item,
        value: item
      }
    })))
  }
}

const RichTextField = {
  components: { Editor },
  props: {
    value: {
      type: String,
      default: ''
    }
  },
  template: '<Editor :value="value" height="200px" class="richtext-editor" @input="$emit(\'input\', $event)" />'
}

export default {
  name: 'InspectionRecord',
  components: {
    TextField,
    NumberField,
    CheckboxField,
    SelectField,
    RichTextField
  },
  data() {
    return {
      loading: false,
      exportLoading: false,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      recordList: [],
      title: '',
      open: false,
      exportOpen: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        recordTitle: undefined,
        inspector: undefined,
        inspectionDate: undefined,
        status: undefined
      },
      exportParams: {
        beginDate: undefined,
        endDate: undefined
      },
      form: {
        recordId: undefined,
        recordTitle: undefined,
        inspectionDate: undefined,
        inspector: undefined,
        remark: undefined,
        status: 'DRAFT',
        details: []
      },
      rules: {
        recordTitle: [
          { required: true, message: '记录标题不能为空', trigger: 'blur' }
        ],
        inspectionDate: [
          { required: true, message: '点检日期不能为空', trigger: 'change' }
        ],
        inspector: [
          { required: true, message: '点检人不能为空', trigger: 'blur' }
        ]
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
      }).catch(() => {
        this.loading = false
      })
    },
    reset() {
      this.form = {
        recordId: undefined,
        recordTitle: undefined,
        inspectionDate: undefined,
        inspector: undefined,
        remark: undefined,
        status: 'DRAFT',
        details: []
      }
      this.resetForm('form')
    },
    resolveFieldComponent(row) {
      switch (row.fieldType) {
        case 'NUMBER':
          return 'NumberField'
        case 'CHECKBOX':
          return 'CheckboxField'
        case 'SELECT':
          return 'SelectField'
        case 'RICHTEXT':
          return 'RichTextField'
        default:
          return 'TextField'
      }
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.recordId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      buildInspectionRecordTemplate({}).then(response => {
        this.form.details = response.data.details || []
        this.open = true
        this.title = '新增点检记录'
      })
    },
    handleUpdate(row) {
      this.reset()
      const recordId = row.recordId || this.ids[0]
      getInspectionRecord(recordId).then(response => {
        this.form = response.data
        this.form.details = response.data.details || []
        this.open = true
        this.title = '修改点检记录'
      })
    },
    submitForm(status) {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        const payload = JSON.parse(JSON.stringify(this.form))
        payload.status = status
        const requestApi = payload.recordId ? updateInspectionRecord : addInspectionRecord
        requestApi(payload).then(() => {
          this.$modal.msgSuccess(payload.recordId ? '修改成功' : '新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      const recordIds = row.recordId ? [row.recordId] : this.ids
      this.$modal.confirm('是否确认删除点检记录编号为"' + recordIds.join(',') + '"的数据项？').then(function() {
        return delInspectionRecord(recordIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
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
        url: '/manage/platform/inspection/record/exportExcel',
        method: 'get',
        params: params,
        responseType: 'blob'
      }).then(blob => {
        const url = window.URL.createObjectURL(new Blob([blob]))
        const link = document.createElement('a')
        link.href = url
        link.download = '点检记录导出_' + new Date().getTime() + '.xlsx'
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
