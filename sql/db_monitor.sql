-- ----------------------------
-- 数据库监控模块 - 建表与初始化数据
-- ----------------------------

-- ----------------------------
-- 1、数据库实例配置表
-- ----------------------------
drop table if exists db_instance;
create table db_instance (
  instance_id     bigint(20)      not null auto_increment    comment '主键ID',
  instance_name   varchar(100)    not null                   comment '实例名称',
  db_type         varchar(30)     not null                   comment '数据库类型（MYSQL / SQLSERVER / REDIS）',
  host            varchar(200)    not null                   comment '主机地址',
  port            int(5)          not null                   comment '端口号',
  db_name         varchar(100)    default ''                 comment '默认数据库名（MySQL/SQLServer用）',
  username        varchar(100)    default ''                 comment '用户名',
  password        varchar(500)    default ''                 comment '加密后的密码',
  instance_group  varchar(100)    default '默认分组'          comment '分组标签',
  status          varchar(20)     default 'ENABLED'          comment '状态（ENABLED启用 / DISABLED禁用）',
  create_by       varchar(64)     default ''                 comment '创建者',
  create_time     datetime                                   comment '创建时间',
  update_by       varchar(64)     default ''                 comment '更新者',
  update_time     datetime                                   comment '更新时间',
  remark          varchar(500)    default ''                 comment '备注',
  primary key (instance_id)
) engine=innodb auto_increment=1 comment = '数据库实例配置表';

-- ----------------------------
-- 2、数据库指标快照表
-- ----------------------------
drop table if exists db_metric_snapshot;
create table db_metric_snapshot (
  snapshot_id     bigint(20)      not null auto_increment    comment '主键ID',
  instance_id     bigint(20)      not null                   comment '实例ID',
  metric_time     datetime        not null                   comment '采集时间',
  connections     int             default 0                  comment '当前连接数',
  max_connections int             default 0                  comment '最大连接数',
  slow_queries    int             default 0                  comment '慢查询累计数',
  qps             decimal(12,2)   default 0                  comment '每秒查询数(近似)',
  cache_hit_ratio decimal(5,2)    default 0                  comment '缓存命中率(%)',
  memory_used_pct decimal(5,2)    default 0                  comment '内存使用率(%)',
  is_alive        char(1)         default '1'                comment '采集时是否连通（1正常 0不可达）',
  extra_json      mediumtext                                  comment '扩展指标JSON（长事务/表空间/死锁/大Key等）',
  primary key (snapshot_id),
  index idx_instance_time (instance_id, metric_time)
) engine=innodb auto_increment=1 comment = '数据库指标快照表';

-- ----------------------------
-- 3、字典类型
-- ----------------------------
insert into sys_dict_type(dict_name, dict_type, status, create_by, create_time, remark)
values('数据库类型', 'db_monitor_db_type', '0', 'admin', sysdate(), '数据库监控-数据库类型');

insert into sys_dict_type(dict_name, dict_type, status, create_by, create_time, remark)
values('数据库实例状态', 'db_monitor_inst_status', '0', 'admin', sysdate(), '数据库监控-实例状态');

-- 字典数据: 数据库类型
insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
values(1, 'MySQL',      'MYSQL',     'db_monitor_db_type',   'Y', '0', 'admin', sysdate(), 'MySQL 数据库');

insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
values(2, 'SQL Server', 'SQLSERVER', 'db_monitor_db_type',   'N', '0', 'admin', sysdate(), 'SQL Server 数据库');

insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
values(3, 'Redis',      'REDIS',     'db_monitor_db_type',   'N', '0', 'admin', sysdate(), 'Redis 缓存');

-- 字典数据: 实例状态
insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, css_class, is_default, status, create_by, create_time, remark)
values(1, '启用', 'ENABLED',  'db_monitor_inst_status', 'success', 'Y', '0', 'admin', sysdate(), '实例启用中');

insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, css_class, is_default, status, create_by, create_time, remark)
values(2, '禁用', 'DISABLED', 'db_monitor_inst_status', 'danger',  'N', '0', 'admin', sysdate(), '实例已禁用');

-- ----------------------------
-- 4、菜单权限（使用LAST_INSERT_ID解决自增ID引用问题）
-- ----------------------------
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
values('数据库监控', '2', '7', 'dbMonitor', 'monitor/database/index', '', '', 1, 0, 'C', '0', '0', 'monitor:db:list', 'monitor', 'admin', sysdate());
SET @db_parent_id = LAST_INSERT_ID();

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
values('实例配置', @db_parent_id, '1', 'dbConfig', 'monitor/database/config', '', '', 1, 0, 'C', '0', '0', 'monitor:db:config', '#', 'admin', sysdate());
SET @db_config_id = LAST_INSERT_ID();

-- 按钮: 监控大屏
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
values('数据库监控查询', @db_parent_id, '1', '', '', '', '', 1, 0, 'F', '0', '0', 'monitor:db:query', '#', 'admin', sysdate());
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
values('数据库指标导出', @db_parent_id, '2', '', '', '', '', 1, 0, 'F', '0', '0', 'monitor:db:export', '#', 'admin', sysdate());

-- 按钮: 实例配置
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
values('实例查询', @db_config_id, '1', '', '', '', '', 1, 0, 'F', '0', '0', 'monitor:db:instance:query', '#', 'admin', sysdate());
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
values('实例新增', @db_config_id, '2', '', '', '', '', 1, 0, 'F', '0', '0', 'monitor:db:instance:add', '#', 'admin', sysdate());
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
values('实例修改', @db_config_id, '3', '', '', '', '', 1, 0, 'F', '0', '0', 'monitor:db:instance:edit', '#', 'admin', sysdate());
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
values('实例删除', @db_config_id, '4', '', '', '', '', 1, 0, 'F', '0', '0', 'monitor:db:instance:remove', '#', 'admin', sysdate());
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
values('连接测试', @db_config_id, '5', '', '', '', '', 1, 0, 'F', '0', '0', 'monitor:db:instance:test', '#', 'admin', sysdate());
