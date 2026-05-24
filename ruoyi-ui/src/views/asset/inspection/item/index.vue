<template>
  <div class="app-container">
    <el-card class="search-card" shadow="never" v-show="showSearch">
      <el-form :model="queryParams" ref="queryForm" size="small" label-width="88px">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="项目名称" prop="itemName">
              <el-input v-model="queryParams.itemName" placeholder="请输入项目名称" clearable class="search-control" @keyup.enter.native="handleQuery" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="节点类型" prop="itemType">
              <el-select v-model="queryParams.itemType" placeholder="请选择节点类型" clearable class="search-control">
                <el-option label="分类" value="CATEGORY" />
                <el-option label="对象" value="OBJECT" />
                <el-option label="检查项" value="CHECK" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="状态" prop="status">
              <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="search-control">
                <el-option label="正常" value="0" />
                <el-option label="停用" value="1" />
              </el-select>
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
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['manage:inspection:item:add']">新增</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="itemList" row-key="itemId" :tree-props="{children: 'children', hasChildren: 'hasChildren'}" default-expand-all>
      <el-table-column prop="itemName" label="项目名称" min-width="220" />
      <el-table-column prop="itemType" label="节点类型" width="100" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.itemType === 'CATEGORY'" type="info">分类</el-tag>
          <el-tag v-else-if="scope.row.itemType === 'OBJECT'" type="warning">对象</el-tag>
          <el-tag v-else type="success">检查项</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="orderNum" label="排序" width="80" align="center" />
      <el-table-column prop="status" label="状态" width="80" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === '0'" type="success">正常</el-tag>
          <el-tag v-else type="danger">停用</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="填写点" min-width="240">
        <template slot-scope="scope">
          <el-tag v-for="field in scope.row.fields || []" :key="field.fieldId" size="mini" class="field-tag">{{ field.fieldLabel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-plus" @click="handleAdd(scope.row)" v-hasPermi="['manage:inspection:item:add']">新增</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['manage:inspection:item:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['manage:inspection:item:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="title" :visible.sync="open" width="900px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="上级项目" prop="parentId">
              <treeselect v-model="form.parentId" :options="itemOptions" :normalizer="normalizer" placeholder="请选择上级项目" class="form-control" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目名称" prop="itemName">
              <el-input v-model="form.itemName" placeholder="请输入项目名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="节点类型" prop="itemType">
              <el-select v-model="form.itemType" placeholder="请选择节点类型" class="form-control">
                <el-option label="分类" value="CATEGORY" />
                <el-option label="对象" value="OBJECT" />
                <el-option label="检查项" value="CHECK" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="显示排序" prop="orderNum">
              <el-input-number v-model="form.orderNum" controls-position="right" :min="0" class="form-control" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio label="0">正常</el-radio>
                <el-radio label="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">填写点配置</el-divider>
        <el-alert title="只有检查项节点需要配置填写点；同一个检查项可以配置多个填写点，例如是否误报和误报数量。" type="info" show-icon :closable="false" class="field-alert" />
        <el-table :data="form.fields" border size="mini">
          <el-table-column label="名称" min-width="150">
            <template slot-scope="scope">
              <el-input v-model="scope.row.fieldLabel" placeholder="如是否检查报警" />
            </template>
          </el-table-column>
          <el-table-column label="标识" min-width="150">
            <template slot-scope="scope">
              <el-input v-model="scope.row.fieldKey" placeholder="如alarm_checked" />
            </template>
          </el-table-column>
          <el-table-column label="类型" width="120">
            <template slot-scope="scope">
              <el-select v-model="scope.row.fieldType" class="form-control">
                <el-option label="勾选" value="CHECKBOX" />
                <el-option label="文本" value="TEXT" />
                <el-option label="数字" value="NUMBER" />
                <el-option label="下拉" value="SELECT" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="选项" min-width="150">
            <template slot-scope="scope">
              <el-input v-model="scope.row.fieldOptions" placeholder="下拉选项逗号分隔" />
            </template>
          </el-table-column>
          <el-table-column label="必填" width="80" align="center">
            <template slot-scope="scope">
              <el-switch v-model="scope.row.required" active-value="1" inactive-value="0" />
            </template>
          </el-table-column>
          <el-table-column label="排序" width="90">
            <template slot-scope="scope">
              <el-input-number v-model="scope.row.orderNum" controls-position="right" :min="0" size="mini" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="70" align="center">
            <template slot-scope="scope">
              <el-button type="text" icon="el-icon-delete" @click="removeField(scope.$index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button type="primary" plain icon="el-icon-plus" size="mini" class="add-field-btn" @click="addField">新增填写点</el-button>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listInspectionItem, treeInspectionItem, getInspectionItem, addInspectionItem, updateInspectionItem, delInspectionItem } from "@/api/asset/inspection"
import Treeselect from "@riophae/vue-treeselect"
import "@riophae/vue-treeselect/dist/vue-treeselect.css"

export default {
  name: "InspectionItem",
  components: { Treeselect },
  data() {
    return {
      loading: true,
      showSearch: true,
      itemList: [],
      itemOptions: [],
      open: false,
      title: "",
      queryParams: {
        itemName: undefined,
        itemType: undefined,
        status: undefined
      },
      form: {},
      rules: {
        itemName: [{ required: true, message: "项目名称不能为空", trigger: "blur" }],
        itemType: [{ required: true, message: "节点类型不能为空", trigger: "change" }],
        orderNum: [{ required: true, message: "显示排序不能为空", trigger: "blur" }]
      }
    }
  },
  created() {
    this.getList()
    this.getTree()
  },
  methods: {
    getList() {
      this.loading = true
      listInspectionItem(this.queryParams).then(response => {
        this.itemList = this.handleTree(response.data, "itemId", "parentId")
        this.loading = false
      })
    },
    getTree() {
      treeInspectionItem().then(response => {
        this.itemOptions = [{ itemId: 0, itemName: "主目录", children: response.data }]
      })
    },
    normalizer(node) {
      if (node.children && !node.children.length) {
        delete node.children
      }
      return {
        id: node.itemId,
        label: node.itemName,
        children: node.children
      }
    },
    reset() {
      this.form = {
        itemId: undefined,
        parentId: 0,
        itemName: undefined,
        itemType: "CATEGORY",
        orderNum: 0,
        status: "0",
        remark: undefined,
        fields: []
      }
      this.resetForm("form")
    },
    handleQuery() {
      this.getList()
    },
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    handleAdd(row) {
      this.reset()
      if (row && row.itemId) {
        this.form.parentId = row.itemId
      }
      this.open = true
      this.title = "新增点检项目"
    },
    handleUpdate(row) {
      this.reset()
      getInspectionItem(row.itemId).then(response => {
        this.form = response.data
        if (!this.form.fields) {
          this.form.fields = []
        }
        this.open = true
        this.title = "修改点检项目"
      })
    },
    addField() {
      this.form.fields.push({
        fieldLabel: undefined,
        fieldKey: undefined,
        fieldType: "CHECKBOX",
        fieldOptions: undefined,
        required: "0",
        orderNum: this.form.fields.length + 1
      })
    },
    removeField(index) {
      this.form.fields.splice(index, 1)
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.itemId != undefined) {
            updateInspectionItem(this.form).then(() => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
              this.getTree()
            })
          } else {
            addInspectionItem(this.form).then(() => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
              this.getTree()
            })
          }
        }
      })
    },
    handleDelete(row) {
      this.$modal.confirm('是否确认删除点检项目"' + row.itemName + '"？子级也会一起删除。').then(function() {
        return delInspectionItem(row.itemId)
      }).then(() => {
        this.getList()
        this.getTree()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    cancel() {
      this.open = false
      this.reset()
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

.field-tag {
  margin-right: 6px;
  margin-bottom: 4px;
}

.field-alert {
  margin-bottom: 12px;
}

.add-field-btn {
  margin-top: 10px;
}
</style>
