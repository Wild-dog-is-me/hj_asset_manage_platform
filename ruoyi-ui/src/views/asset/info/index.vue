<template>
  <div class="app-container">
    <el-card class="search-card" shadow="never" v-show="showSearch">
      <el-form :model="queryParams" ref="queryForm" size="small" label-width="88px">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="账套" prop="accountSet">
              <el-select v-model="queryParams.accountSet" placeholder="请选择账套" clearable class="search-control">
                <el-option
                  v-for="dict in dict.type.asset_account_set"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="资产类别" prop="assetCategory">
              <el-select v-model="queryParams.assetCategory" placeholder="请选择资产类别" clearable class="search-control">
                <el-option
                  v-for="dict in dict.type.asset_category"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="资产状态" prop="assetStatus">
              <el-select v-model="queryParams.assetStatus" placeholder="请选择资产状态" clearable class="search-control">
                <el-option
                  v-for="dict in dict.type.asset_status"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="设备类别" prop="deviceType">
              <el-select v-model="queryParams.deviceType" placeholder="请选择设备类别" clearable class="search-control">
                <el-option
                  v-for="dict in dict.type.asset_device_type"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="资产编号" prop="assetNo">
              <el-input
                v-model="queryParams.assetNo"
                placeholder="请输入资产编号"
                clearable
                class="search-control"
                @keyup.enter.native="handleQuery"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="设备编号" prop="deviceNo">
              <el-input
                v-model="queryParams.deviceNo"
                placeholder="请输入设备编号"
                clearable
                class="search-control"
                @keyup.enter.native="handleQuery"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="财务帐编号" prop="financeAccountNo">
              <el-input
                v-model="queryParams.financeAccountNo"
                placeholder="请输入财务帐编号"
                clearable
                class="search-control"
                @keyup.enter.native="handleQuery"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="资产名称" prop="assetName">
              <el-input
                v-model="queryParams.assetName"
                placeholder="请输入资产名称"
                clearable
                class="search-control"
                @keyup.enter.native="handleQuery"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="使用部门" prop="deptId">
              <treeselect :append-to-body="true" v-model="queryParams.deptId" :options="deptOptions" :normalizer="normalizer" placeholder="请选择使用部门" clearable class="search-control" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="使用人" prop="userName">
              <el-input
                v-model="queryParams.userName"
                placeholder="请输入使用人"
                clearable
                class="search-control"
                @keyup.enter.native="handleQuery"
              />
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
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['manage:asset:add']"
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
          v-hasPermi="['manage:asset:edit']"
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
          v-hasPermi="['manage:asset:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['manage:asset:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="assetInfoList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="账套" align="center" prop="accountSet" width="110" fixed="left">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.asset_account_set" :value="scope.row.accountSet" />
        </template>
      </el-table-column>
      <el-table-column label="资产类别" align="center" prop="assetCategory" width="110" fixed="left">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.asset_category" :value="scope.row.assetCategory" />
        </template>
      </el-table-column>
      <el-table-column label="资产状态" align="center" prop="assetStatus" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.asset_status" :value="scope.row.assetStatus" />
        </template>
      </el-table-column>
      <el-table-column label="资产编号" align="center" prop="assetNo" width="150" :show-overflow-tooltip="true" />
      <el-table-column label="设备编号" align="center" prop="deviceNo" width="150" :show-overflow-tooltip="true" />
      <el-table-column label="财务帐编号" align="center" prop="financeAccountNo" width="150" :show-overflow-tooltip="true" />
      <el-table-column label="设备类别" align="center" prop="deviceType" width="110">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.asset_device_type" :value="scope.row.deviceType" />
        </template>
      </el-table-column>
      <el-table-column label="资产名称" align="center" prop="assetName" min-width="140" :show-overflow-tooltip="true" />
      <el-table-column label="型号" align="center" prop="model" width="130" :show-overflow-tooltip="true" />
      <el-table-column label="单位" align="center" prop="unit" width="80" />
      <el-table-column label="数量" align="center" prop="quantity" width="90" />
      <el-table-column label="使用部门" align="center" prop="deptName" width="140" :show-overflow-tooltip="true" />
      <el-table-column label="成本中心" align="center" prop="costCenter" width="130" :show-overflow-tooltip="true" />
      <el-table-column label="使用人" align="center" prop="userName" width="100" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150" fixed="right">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['manage:asset:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['manage:asset:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog :title="title" :visible.sync="open" width="900px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="账套" prop="accountSet">
              <el-select v-model="form.accountSet" placeholder="请选择账套" clearable style="width: 100%">
                <el-option
                  v-for="dict in dict.type.asset_account_set"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资产类别" prop="assetCategory">
              <el-select v-model="form.assetCategory" placeholder="请选择资产类别" clearable style="width: 100%">
                <el-option
                  v-for="dict in dict.type.asset_category"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="资产状态" prop="assetStatus">
              <el-select v-model="form.assetStatus" placeholder="请选择资产状态" clearable style="width: 100%">
                <el-option
                  v-for="dict in dict.type.asset_status"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备类别" prop="deviceType">
              <el-select v-model="form.deviceType" placeholder="请选择设备类别" clearable style="width: 100%">
                <el-option
                  v-for="dict in dict.type.asset_device_type"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="资产编号" prop="assetNo">
              <el-input v-model="form.assetNo" placeholder="请输入资产编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备编号" prop="deviceNo">
              <el-input v-model="form.deviceNo" placeholder="请输入设备编号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="财务帐编号" prop="financeAccountNo">
              <el-input v-model="form.financeAccountNo" placeholder="请输入财务帐编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资产名称" prop="assetName">
              <el-input v-model="form.assetName" placeholder="请输入资产名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="型号" prop="model">
              <el-input v-model="form.model" placeholder="请输入型号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位" prop="unit">
              <el-input v-model="form.unit" placeholder="请输入单位" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="数量" prop="quantity">
              <el-input-number v-model="form.quantity" controls-position="right" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="使用部门" prop="deptId">
              <treeselect v-model="form.deptId" :options="deptOptions" :normalizer="normalizer" placeholder="请选择使用部门" class="form-control" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="成本中心" prop="costCenter">
              <el-input v-model="form.costCenter" placeholder="请输入成本中心" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="使用人" prop="userName">
              <el-input v-model="form.userName" placeholder="请输入使用人" />
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
import { listAssetInfo, getAssetInfo, delAssetInfo, addAssetInfo, updateAssetInfo } from "@/api/asset/manage/info"
import { listDept } from "@/api/system/dept"
import Treeselect from "@riophae/vue-treeselect"
import "@riophae/vue-treeselect/dist/vue-treeselect.css"

export default {
  name: "AssetInfo",
  dicts: ['asset_account_set', 'asset_category', 'asset_status', 'asset_device_type'],
  components: { Treeselect },
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      assetInfoList: [],
      deptOptions: [],
      title: "",
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        accountSet: undefined,
        assetCategory: undefined,
        assetStatus: undefined,
        assetNo: undefined,
        deviceNo: undefined,
        financeAccountNo: undefined,
        deviceType: undefined,
        assetName: undefined,
        deptId: undefined,
        userName: undefined
      },
      form: {},
      rules: {
        accountSet: [
          { required: true, message: "账套不能为空", trigger: "change" }
        ],
        assetCategory: [
          { required: true, message: "资产类别不能为空", trigger: "change" }
        ],
        assetStatus: [
          { required: true, message: "资产状态不能为空", trigger: "change" }
        ],
        assetNo: [
          { required: true, message: "资产编号不能为空", trigger: "blur" }
        ],
        assetName: [
          { required: true, message: "资产名称不能为空", trigger: "blur" }
        ],
        quantity: [
          { required: true, message: "数量不能为空", trigger: "blur" }
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
      listAssetInfo(this.queryParams).then(response => {
        this.assetInfoList = response.rows
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
        assetId: undefined,
        accountSet: undefined,
        assetCategory: undefined,
        assetStatus: "IN_STOCK",
        assetNo: undefined,
        deviceNo: undefined,
        financeAccountNo: undefined,
        deviceType: undefined,
        assetName: undefined,
        model: undefined,
        unit: "",
        quantity: 1,
        deptId: undefined,
        costCenter: undefined,
        userName: undefined,
        remark: undefined
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
      this.ids = selection.map(item => item.assetId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加资产信息"
    },
    handleUpdate(row) {
      this.reset()
      const assetId = row.assetId || this.ids
      getAssetInfo(assetId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改资产信息"
      })
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.assetId != undefined) {
            updateAssetInfo(this.form).then(() => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addAssetInfo(this.form).then(() => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    handleDelete(row) {
      const assetIds = row.assetId || this.ids
      this.$modal.confirm('是否确认删除资产信息编号为"' + assetIds + '"的数据项？').then(function() {
        return delAssetInfo(assetIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    handleExport() {
      this.download('manage/platform/asset/export', {
        ...this.queryParams
      }, `asset_info_${new Date().getTime()}.xlsx`)
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
