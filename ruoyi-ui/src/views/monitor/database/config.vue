<template>
  <div class="app-container">
    <el-card class="search-card" shadow="never" v-show="showSearch">
      <el-form :model="queryParams" ref="queryForm" size="small" label-width="88px">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="数据库类型" prop="dbType">
              <el-select v-model="queryParams.dbType" placeholder="请选择数据库类型" clearable class="search-control">
                <el-option v-for="dict in dict.type.db_monitor_db_type" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="实例名称" prop="instanceName">
              <el-input v-model="queryParams.instanceName" placeholder="请输入实例名称" clearable class="search-control" @keyup.enter.native="handleQuery" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="主机地址" prop="host">
              <el-input v-model="queryParams.host" placeholder="请输入主机地址" clearable class="search-control" @keyup.enter.native="handleQuery" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="状态" prop="status">
              <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="search-control">
                <el-option v-for="dict in dict.type.db_monitor_inst_status" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="分组" prop="instanceGroup">
              <el-input v-model="queryParams.instanceGroup" placeholder="请输入分组" clearable class="search-control" @keyup.enter.native="handleQuery" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
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
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['monitor:db:instance:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['monitor:db:instance:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['monitor:db:instance:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="instanceList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="实例名称" align="center" prop="instanceName" min-width="160" :show-overflow-tooltip="true" />
      <el-table-column label="数据库类型" align="center" prop="dbType" width="120">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.db_monitor_db_type" :value="scope.row.dbType" />
        </template>
      </el-table-column>
      <el-table-column label="主机地址" align="center" prop="host" width="140" />
      <el-table-column label="端口" align="center" prop="port" width="80" />
      <el-table-column label="数据库名" align="center" prop="dbName" width="120" :show-overflow-tooltip="true" />
      <el-table-column label="分组" align="center" prop="instanceGroup" width="120" />
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.db_monitor_inst_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="260">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleUpdate(scope.row)" v-hasPermi="['monitor:db:instance:query']">详情</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['monitor:db:instance:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-connection" @click="handleTestConn(scope.row)" v-hasPermi="['monitor:db:instance:test']">测试连接</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['monitor:db:instance:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改弹窗 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="实例名称" prop="instanceName">
              <el-input v-model="form.instanceName" placeholder="如：生产MySQL主库" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="数据库类型" prop="dbType">
              <el-select v-model="form.dbType" placeholder="请选择数据库类型" style="width: 100%" @change="onDbTypeChange">
                <el-option v-for="dict in dict.type.db_monitor_db_type" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
                <el-option v-for="dict in dict.type.db_monitor_inst_status" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="16">
            <el-form-item label="主机地址" prop="host">
              <el-input v-model="form.host" placeholder="如：10.0.1.100" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="端口" prop="port">
              <el-input-number v-model="form.port" controls-position="right" :min="1" :max="65535" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row v-if="form.dbType !== 'REDIS'">
          <el-col :span="12">
            <el-form-item label="数据库名" prop="dbName">
              <el-input v-model="form.dbName" placeholder="请输入数据库名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" placeholder="请输入用户名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="密码" prop="passwordRaw">
              <el-input v-model="form.passwordRaw" type="password" placeholder="请输入密码，修改时留空则不修改" show-password />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="分组" prop="instanceGroup">
              <el-input v-model="form.instanceGroup" placeholder="如：核心业务组" />
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
        <el-button type="warning" plain @click="handleTestFormConn" v-if="form.host && form.port">测试连接</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listDbInstance, getDbInstance, addDbInstance, updateDbInstance, delDbInstance, testDbConnection, testDbFormConnection } from '@/api/monitor/database'

export default {
  name: 'DbInstanceConfig',
  dicts: ['db_monitor_db_type', 'db_monitor_inst_status'],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      instanceList: [],
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        dbType: undefined,
        instanceName: undefined,
        host: undefined,
        status: undefined,
        instanceGroup: undefined
      },
      form: {},
      rules: {
        instanceName: [{ required: true, message: '实例名称不能为空', trigger: 'blur' }],
        dbType: [{ required: true, message: '数据库类型不能为空', trigger: 'change' }],
        host: [{ required: true, message: '主机地址不能为空', trigger: 'blur' }],
        port: [{ required: true, message: '端口不能为空', trigger: 'blur' }],
        username: [{ required: true, message: '用户名不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listDbInstance(this.queryParams).then(res => {
        this.instanceList = res.rows
        this.total = res.total
        this.loading = false
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = { dbType: 'MYSQL', port: 3306, status: 'ENABLED', instanceGroup: '默认分组' }
      this.resetForm('form')
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm('queryForm'); this.handleQuery() },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.instanceId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.title = '新增数据库实例'
      this.open = true
    },
    handleUpdate(row) {
      this.reset()
      const instanceId = row.instanceId || this.ids[0]
      getDbInstance(instanceId).then(res => {
        this.form = res.data
        this.title = '修改数据库实例'
        this.open = true
      })
    },
    handleDelete(row) {
      const instanceIds = row.instanceId || this.ids.join(',')
      this.$modal.confirm('确认删除该数据库实例？').then(() => {
        return delDbInstance(instanceIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        if (this.form.instanceId) {
          updateDbInstance(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
        } else {
          addDbInstance(this.form).then(() => {
            this.$modal.msgSuccess('新增成功')
            this.open = false
            this.getList()
          })
        }
      })
    },
    handleTestConn(row) {
      testDbConnection(row.instanceId).then(() => {
        this.$modal.msgSuccess(row.instanceName + ' 连接成功')
      }).catch(() => {
        this.$modal.msgError('连接失败，请检查网络、端口、用户名和密码')
      })
    },
    handleTestFormConn() {
      if (!this.form.host || !this.form.port) {
        this.$modal.msgWarning('请先填写主机地址和端口')
        return
      }
      testDbFormConnection(this.form).then(() => {
        this.$modal.msgSuccess('连接成功')
      }).catch(() => {
        this.$modal.msgError('连接失败')
      })
    },
    onDbTypeChange(val) {
      if (val === 'REDIS') {
        this.form.dbName = ''
      }
      if (val === 'MYSQL' && !this.form.port) this.form.port = 3306
      if (val === 'SQLSERVER' && !this.form.port) this.form.port = 1433
      if (val === 'REDIS' && !this.form.port) this.form.port = 6379
    }
  }
}
</script>

<style scoped lang="scss">
.search-card {
  margin-bottom: 16px;
}
.search-control {
  width: 100%;
}
.search-actions {
  float: right;
}
.mb8 {
  margin-bottom: 8px;
}
</style>
