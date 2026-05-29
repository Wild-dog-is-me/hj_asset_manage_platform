<template>
  <div class="app-container">
    <el-card class="search-card" shadow="never" v-show="showSearch">
      <el-form :model="queryParams" ref="queryForm" size="small" label-width="88px">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="账套" prop="accountSet">
              <el-select v-model="queryParams.accountSet" placeholder="请选择账套" clearable class="search-control">
                <el-option v-for="dict in dict.type.asset_account_set" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="资产类别" prop="assetCategory">
              <el-select v-model="queryParams.assetCategory" placeholder="请选择资产类别" clearable class="search-control">
                <el-option v-for="dict in dict.type.asset_category" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="资产状态" prop="assetStatus">
              <el-select v-model="queryParams.assetStatus" placeholder="请选择资产状态" clearable class="search-control">
                <el-option v-for="dict in dict.type.asset_status" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="设备类别" prop="deviceType">
              <el-select v-model="queryParams.deviceType" placeholder="请选择设备类别" clearable class="search-control">
                <el-option v-for="dict in dict.type.asset_device_type" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="资产编号" prop="assetNo">
              <el-input v-model="queryParams.assetNo" placeholder="请输入资产编号" clearable class="search-control" @keyup.enter.native="handleQuery" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="设备编号" prop="deviceNo">
              <el-input v-model="queryParams.deviceNo" placeholder="请输入设备编号" clearable class="search-control" @keyup.enter.native="handleQuery" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="财务帐编号" prop="financeAccountNo">
              <el-input v-model="queryParams.financeAccountNo" placeholder="请输入财务帐编号" clearable class="search-control" @keyup.enter.native="handleQuery" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="资产名称" prop="assetName">
              <el-input v-model="queryParams.assetName" placeholder="请输入资产名称" clearable class="search-control" @keyup.enter.native="handleQuery" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="使用部门" prop="deptId">
              <treeselect :append-to-body="true" v-model="queryParams.deptId" :options="deptOptions" :normalizer="normalizer" placeholder="请选择使用部门" clearable class="search-control" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="使用人" prop="userName">
              <el-input v-model="queryParams.userName" placeholder="请输入使用人" clearable class="search-control" @keyup.enter.native="handleQuery" />
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
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['manage:asset:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['manage:asset:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['manage:asset:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['manage:asset:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
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
      <el-table-column label="操作" align="center" class-name="opt-cell" width="330" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['manage:asset:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-s-order" @click="handleTransferHistory(scope.row)" v-hasPermi="['manage:asset:edit']">流转</el-button>
          <el-button v-if="scope.row.assetStatus === 'IN_STOCK'" size="mini" type="text" icon="el-icon-check" @click="handleTransferAction('receive', scope.row)" v-hasPermi="['manage:asset:edit']">领用</el-button>
          <el-button v-if="scope.row.assetStatus === 'IN_USE'" size="mini" type="text" icon="el-icon-refresh-left" @click="handleTransferAction('return', scope.row)" v-hasPermi="['manage:asset:edit']">归还</el-button>
          <el-button v-if="scope.row.assetStatus === 'IN_USE'" size="mini" type="text" icon="el-icon-sort" @click="handleTransferAction('transfer', scope.row)" v-hasPermi="['manage:asset:edit']">调拨</el-button>
          <el-button v-if="scope.row.assetStatus !== 'SCRAPPED'" size="mini" type="text" icon="el-icon-close" @click="handleTransferAction('scrap', scope.row)" v-hasPermi="['manage:asset:edit']">报废</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['manage:asset:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改弹窗 -->
    <el-dialog :title="title" :visible.sync="open" width="900px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="账套" prop="accountSet">
              <el-select v-model="form.accountSet" placeholder="请选择账套" clearable style="width: 100%">
                <el-option v-for="dict in dict.type.asset_account_set" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资产类别" prop="assetCategory">
              <el-select v-model="form.assetCategory" placeholder="请选择资产类别" clearable style="width: 100%">
                <el-option v-for="dict in dict.type.asset_category" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="资产状态">
              <el-input :value="form.assetStatus ? getDictLabel(dict.type.asset_status, form.assetStatus) : '-'" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备类别" prop="deviceType">
              <el-select v-model="form.deviceType" placeholder="请选择设备类别" clearable style="width: 100%">
                <el-option v-for="dict in dict.type.asset_device_type" :key="dict.value" :label="dict.label" :value="dict.value" />
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
            <el-form-item label="使用部门">
              <el-input :value="form.deptName || '-'" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="成本中心">
              <el-input :value="form.costCenter || '-'" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="使用人">
              <el-input :value="form.userName || '-'" disabled />
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

    <!-- 流转操作弹窗 -->
    <el-dialog :title="transferTitle" :visible.sync="transferOpen" width="520px" append-to-body>
      <el-card shadow="never" class="transfer-context-card">
        <el-row :gutter="12">
          <el-col :span="8"><span class="context-label">资产编号</span><span class="context-value">{{ currentAssetRow.assetNo || '-' }}</span></el-col>
          <el-col :span="8"><span class="context-label">资产名称</span><span class="context-value">{{ currentAssetRow.assetName || '-' }}</span></el-col>
          <el-col :span="8"><span class="context-label">当前状态</span><dict-tag :options="dict.type.asset_status" :value="currentAssetRow.assetStatus" /></el-col>
        </el-row>
        <el-row :gutter="12" class="context-second-row">
          <el-col :span="8"><span class="context-label">使用部门</span><span class="context-value">{{ currentAssetRow.deptName || '-' }}</span></el-col>
          <el-col :span="8"><span class="context-label">使用人</span><span class="context-value">{{ currentAssetRow.userName || '-' }}</span></el-col>
          <el-col :span="8"><span class="context-label">成本中心</span><span class="context-value">{{ currentAssetRow.costCenter || '-' }}</span></el-col>
        </el-row>
      </el-card>
      <el-form ref="transferForm" :model="transferForm" :rules="transferRules" label-width="90px" class="transfer-form-top">
        <el-form-item v-if="transferType === 'receive'" label="使用部门" prop="deptId">
          <treeselect v-model="transferForm.deptId" :options="deptOptions" :normalizer="normalizer" placeholder="请选择使用部门" class="form-control" />
        </el-form-item>
        <el-form-item v-if="transferType === 'receive'" label="使用人" prop="userName">
          <el-input v-model="transferForm.userName" placeholder="请输入使用人" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="transferType === 'receive'" label="成本中心" prop="costCenter">
          <el-input v-model="transferForm.costCenter" placeholder="请输入成本中心" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="transferType === 'transfer'" label="新部门" prop="deptId">
          <treeselect v-model="transferForm.deptId" :options="deptOptions" :normalizer="normalizer" placeholder="不填则不改变" class="form-control" />
        </el-form-item>
        <el-form-item v-if="transferType === 'transfer'" label="新使用人" prop="userName">
          <el-input v-model="transferForm.userName" placeholder="不填则不改变" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="transferType === 'transfer'" label="新成本中心" prop="costCenter">
          <el-input v-model="transferForm.costCenter" placeholder="不填则不改变" style="width: 100%" />
        </el-form-item>
        <el-form-item :label="transferRemarkLabel" prop="remark">
          <el-input v-model="transferForm.remark" type="textarea" :rows="3" :placeholder="transferRemarkPlaceholder" style="width: 100%" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitTransfer" :loading="transferLoading">确 定</el-button>
        <el-button @click="transferOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 流转记录弹窗 -->
    <el-dialog title="流转记录" :visible.sync="historyOpen" width="700px" append-to-body>
      <div v-if="transferRecords.length > 0" class="timeline-wrap">
        <el-timeline>
          <el-timeline-item v-for="item in transferRecords" :key="item.recordId" :timestamp="parseTime(item.operateTime)" :type="bizTimelineType(item.bizType)" placement="top">
            <el-card shadow="hover" class="timeline-card">
              <div class="timeline-card-header">
                <el-tag :type="bizTagType(item.bizType)" size="small">{{ getDictLabel(dict.type.asset_transfer_type, item.bizType) }}</el-tag>
              </div>
              <div class="timeline-card-body">
                <div class="timeline-status-line">
                  <span>{{ item.beforeStatus ? getDictLabel(dict.type.asset_status, item.beforeStatus) : '-' }}</span>
                  <i class="el-icon-right timeline-arrow" />
                  <span>{{ getDictLabel(dict.type.asset_status, item.afterStatus) }}</span>
                </div>
                <div v-if="hasDeptChange(item)" class="timeline-change-info">
                  部门: {{ item.beforeDeptName || '-' }} → {{ item.afterDeptName || '-' }}<template v-if="item.beforeUserName || item.afterUserName"> | 使用人: {{ item.beforeUserName || '-' }} → {{ item.afterUserName || '-' }}</template>
                </div>
                <div v-if="item.changeReason" class="timeline-reason">原因: {{ item.changeReason }}</div>
                <div class="timeline-operator">操作人: {{ item.operator || '-' }}</div>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
      <el-empty v-else description="暂无流转记录" />
      <div slot="footer" class="dialog-footer">
        <el-button @click="historyOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listAssetInfo, getAssetInfo, delAssetInfo, addAssetInfo, updateAssetInfo, receiveAsset, returnAsset, transferAsset, scrapAsset, listTransferRecord } from "@/api/asset/manage/info"
import { listDept } from "@/api/system/dept"
import Treeselect from "@riophae/vue-treeselect"
import "@riophae/vue-treeselect/dist/vue-treeselect.css"

export default {
  name: "AssetInfo",
  dicts: ['asset_account_set', 'asset_category', 'asset_status', 'asset_device_type', 'asset_transfer_type'],
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
        accountSet: [{ required: true, message: "账套不能为空", trigger: "change" }],
        assetCategory: [{ required: true, message: "资产类别不能为空", trigger: "change" }],
        deviceType: [{ required: true, message: "设备类别不能为空", trigger: "change" }],
        assetNo: [{ required: true, message: "资产编号不能为空", trigger: "blur" }],
        assetName: [{ required: true, message: "资产名称不能为空", trigger: "blur" }],
        quantity: [{ required: true, message: "数量不能为空", trigger: "blur" }]
      },
      transferOpen: false,
      transferTitle: "",
      transferType: "",
      transferLoading: false,
      currentAssetRow: {},
      transferForm: {},
      transferRules: {},
      historyOpen: false,
      transferRecords: []
    }
  },
  computed: {
    transferRemarkLabel() {
      const map = { receive: '领用原因', return: '归还原因', transfer: '调拨原因', scrap: '报废原因' }
      return map[this.transferType] || '备注'
    },
    transferRemarkPlaceholder() {
      const map = { receive: '请输入领用原因', return: '请输入归还原因', transfer: '请输入调拨原因', scrap: '请输入报废原因' }
      return map[this.transferType] || '请输入备注'
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
      const assetLabel = row.assetNo || row.assetId
      this.$modal.confirm('是否确认删除资产"' + assetLabel + '"？').then(function() {
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
    },
    handleTransferAction(cmd, row) {
      this.currentAssetRow = row
      this.transferType = cmd
      this.transferForm = { remark: undefined, deptId: undefined, userName: undefined, costCenter: undefined }
      if (cmd === 'receive') {
        this.transferTitle = '资产领用 - ' + (row.assetNo || row.assetId)
        this.transferRules = {
          deptId: [{ required: true, message: '请选择使用部门', trigger: 'change' }],
          userName: [{ required: true, message: '请输入使用人', trigger: 'blur' }],
          remark: [{ required: true, message: '请输入领用原因', trigger: 'blur' }]
        }
      } else if (cmd === 'return') {
        this.transferTitle = '资产归还 - ' + (row.assetNo || row.assetId)
        this.transferRules = {
          remark: [{ required: true, message: '请输入归还原因', trigger: 'blur' }]
        }
      } else if (cmd === 'transfer') {
        this.transferTitle = '资产调拨 - ' + (row.assetNo || row.assetId)
        this.transferRules = {
          remark: [{ required: true, message: '请输入调拨原因', trigger: 'blur' }]
        }
      } else if (cmd === 'scrap') {
        this.transferTitle = '资产报废 - ' + (row.assetNo || row.assetId)
        this.transferRules = {
          remark: [{ required: true, message: '请输入报废原因', trigger: 'blur' }]
        }
      }
      if (this.$refs.transferForm) {
        this.$refs.transferForm.clearValidate()
      }
      this.transferOpen = true
    },
    submitTransfer() {
      this.$refs["transferForm"].validate(valid => {
        if (!valid) return
        this.transferLoading = true
        const data = {
          assetId: this.currentAssetRow.assetId,
          remark: this.transferForm.remark,
          deptId: this.transferForm.deptId,
          userName: this.transferForm.userName,
          costCenter: this.transferForm.costCenter
        }
        let api
        if (this.transferType === 'receive') api = receiveAsset(data)
        else if (this.transferType === 'return') api = returnAsset(data)
        else if (this.transferType === 'transfer') api = transferAsset(data)
        else if (this.transferType === 'scrap') api = scrapAsset(data)
        api.then(() => {
          this.$modal.msgSuccess('操作成功')
          this.transferOpen = false
          this.transferLoading = false
          this.getList()
        }).catch(() => {
          this.transferLoading = false
        })
      })
    },
    handleTransferHistory(row) {
      this.historyOpen = true
      this.transferRecords = []
      listTransferRecord(row.assetId).then(response => {
        this.transferRecords = response.data || []
      })
    },
    hasDeptChange(item) {
      return item.beforeDeptName || item.afterDeptName || item.beforeUserName || item.afterUserName
    },
    getDictLabel(dictList, value) {
      if (!dictList || !value) return value
      const item = dictList.find(d => d.value === value)
      return item ? item.label : value
    },
    bizTagType(bizType) {
      const map = { 'RECEIVE': 'primary', 'RETURN': 'success', 'TRANSFER': 'warning', 'SCRAP': 'danger', 'INFO_CHANGE': 'info' }
      return map[bizType] || 'info'
    },
    bizTimelineType(bizType) {
      const map = { 'RECEIVE': 'primary', 'RETURN': 'success', 'TRANSFER': 'warning', 'SCRAP': 'danger', 'INFO_CHANGE': 'info' }
      return map[bizType] || 'primary'
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

.opt-cell {
  white-space: nowrap;
}
.opt-cell .el-button--text {
  padding-left: 4px;
  padding-right: 4px;
}

.transfer-context-card {
  margin-bottom: 18px;
  background: #f5f7fa;
}
.transfer-context-card ::v-deep .el-card__body {
  padding: 14px 18px;
}
.transfer-context-card .el-row {
  line-height: 28px;
}
.context-label {
  color: #909399;
  font-size: 13px;
  margin-right: 8px;
}
.context-value {
  color: #303133;
  font-size: 13px;
}
.context-second-row {
  margin-top: 4px;
}

.transfer-form-top {
  margin-top: 0;
}

.timeline-wrap {
  max-height: 460px;
  overflow-y: auto;
  padding-right: 8px;
}
.timeline-card {
  margin-bottom: 0;
}
.timeline-card ::v-deep .el-card__body {
  padding: 12px 16px;
}
.timeline-card-header {
  margin-bottom: 8px;
}
.timeline-card-body {
  font-size: 13px;
  color: #606266;
  line-height: 1.8;
}
.timeline-status-line {
  font-weight: 500;
}
.timeline-arrow {
  margin: 0 6px;
  color: #909399;
  font-size: 12px;
}
.timeline-change-info {
  color: #909399;
  font-size: 12px;
}
.timeline-reason {
  color: #909399;
  font-size: 12px;
}
.timeline-operator {
  color: #c0c4cc;
  font-size: 12px;
  margin-top: 2px;
}
</style>

<style>
.vue-treeselect__portal-target {
  z-index: 3000;
}
</style>
