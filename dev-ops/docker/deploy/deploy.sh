#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info()  { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

BACKUP_TIME=$(date +"%Y%m%d_%H%M%S")
BACKUP_DIR="$SCRIPT_DIR/backups"
BACKEND_BAK_DIR="$BACKUP_DIR/backend"
FRONTEND_BAK_DIR="$BACKUP_DIR/frontend"
RELEASE_PKG_DIR="$SCRIPT_DIR/release_pkg"
RELEASE_FRONTEND_DIR="$RELEASE_PKG_DIR/dist"
BASE_IMAGE_PREFIX="${BASE_IMAGE_PREFIX:-}"

get_server_ip() {
  local ip
  ip=$(hostname -I 2>/dev/null | awk '{print $1}')
  if [ -z "$ip" ]; then
    ip=$(ip route get 1 2>/dev/null | awk '{print $7;exit}')
  fi
  if [ -z "$ip" ]; then
    ip=$(ifconfig 2>/dev/null | awk '/inet / && $2 != "127.0.0.1" {print $2; exit}')
  fi
  if [ -z "$ip" ]; then
    ip="localhost"
  fi
  echo "$ip"
}

SERVER_IP=$(get_server_ip)

prepare_directories() {
    log_info "初始化目录结构..."

    mkdir -p data/mysql
    mkdir -p data/redis
    mkdir -p data/upload
    mkdir -p data/logs
    mkdir -p conf/mysql
    mkdir -p conf/redis
    mkdir -p nginx
    mkdir -p "$BACKEND_BAK_DIR"
    mkdir -p "$FRONTEND_BAK_DIR"
    mkdir -p "$RELEASE_PKG_DIR"

    if [ ! -f "conf/mysql/mysql.cnf" ]; then
        cat > conf/mysql/mysql.cnf << 'CNF'
[mysqld]
default-character-set=utf8mb4
collation_server=utf8mb4_unicode_ci
[client]
default-character-set=utf8mb4
CNF
    fi

    if [ ! -f "conf/redis/redis.conf" ]; then
        cat > conf/redis/redis.conf << 'CNF'
bind 0.0.0.0
port 6379
requirepass fsWds#$%
appendonly yes
CNF
    fi

    if [ ! -f "nginx/default.conf" ]; then
        log_error "缺少 nginx/default.conf，请检查部署目录文件是否完整"
        exit 1
    fi

    log_info "目录初始化完成"
}

find_backend_jar() {
    local jars=( "$RELEASE_PKG_DIR"/*.jar )
    if [ "${jars[0]}" = "$RELEASE_PKG_DIR/*.jar" ] || [ ${#jars[@]} -eq 0 ]; then
        log_error "release_pkg 目录未找到后端 JAR 包！"
        echo ""
        echo "  请将后端 JAR 包放到目录："
        echo "  $RELEASE_PKG_DIR"
        exit 1
    fi

    if [ ${#jars[@]} -gt 1 ]; then
        log_warn "release_pkg 目录发现多个 JAR 文件："
        printf '  %s\n' "${jars[@]}"
        echo ""
        read -r -p "请输入要部署的后端 JAR 文件名: " chosen
        BACKEND_JAR="$RELEASE_PKG_DIR/$chosen"
        if [ ! -f "$BACKEND_JAR" ]; then
            log_error "文件不存在: $chosen"
            exit 1
        fi
    else
        BACKEND_JAR="${jars[0]}"
    fi

    log_info "后端 JAR: $(basename "$BACKEND_JAR")"
}

find_frontend_dist() {
    if [ ! -d "$RELEASE_FRONTEND_DIR" ]; then
        log_error "release_pkg 目录缺少 dist 前端构建产物"
        exit 1
    fi
    log_info "前端 dist 目录已找到: $RELEASE_FRONTEND_DIR"
}

prepare_build_context() {
    log_info "准备构建上下文..."
    cp -f "$BACKEND_JAR" "$SCRIPT_DIR/$(basename "$BACKEND_JAR")"
    rm -rf "$SCRIPT_DIR/dist"
    cp -R "$RELEASE_FRONTEND_DIR" "$SCRIPT_DIR/dist"
}

backup_packages() {
    log_info "备份历史发包文件..."

    if ls "$RELEASE_PKG_DIR"/*.jar >/dev/null 2>&1; then
      for f in "$RELEASE_PKG_DIR"/*.jar; do
        [ "$f" = "$BACKEND_JAR" ] && continue
        cp -f "$f" "$BACKEND_BAK_DIR/$(basename "$f" .jar)_${BACKUP_TIME}_bak.jar"
      done
    fi

    if [ -d "$RELEASE_FRONTEND_DIR" ]; then
      tar -czf "$FRONTEND_BAK_DIR/dist_${BACKUP_TIME}_bak.tar.gz" -C "$RELEASE_PKG_DIR" dist
    fi

    if [ -f "$BACKEND_JAR" ]; then
      cp -f "$BACKEND_JAR" "$BACKEND_BAK_DIR/$(basename "$BACKEND_JAR" .jar)_${BACKUP_TIME}_bak.jar"
    fi

    log_info "备份完成：$BACKUP_DIR"
}

deploy() {
    log_info "停止旧容器..."
    docker compose down 2>/dev/null || true
    log_info "构建镜像（后端+前端）"
    BASE_IMAGE_PREFIX="$BASE_IMAGE_PREFIX" docker compose build --no-cache app web
    log_info "启动所有服务..."
    BASE_IMAGE_PREFIX="$BASE_IMAGE_PREFIX" docker compose up -d
    echo ""
    log_info "等待服务就绪..."
    local max_wait=180
    local waited=0
    while [ $waited -lt $max_wait ]; do
        if docker compose ps app | grep -q "healthy" && docker compose ps web | grep -q "healthy"; then
            echo ""
            log_info "应用启动成功！"
            echo ""
            echo "============================================"
            echo "  部署完成！"
            echo "  前端访问地址: http://$SERVER_IP:8088"
            echo "  后端访问地址: http://$SERVER_IP:8080"
            echo "============================================"
            echo ""
            echo "  数据目录："
            echo "    上传文件: $SCRIPT_DIR/data/upload"
            echo "    应用日志: $SCRIPT_DIR/data/logs"
            echo "    MySQL数据: $SCRIPT_DIR/data/mysql"
            echo "    Redis数据: $SCRIPT_DIR/data/redis"
            echo "    备份目录: $BACKUP_DIR"
            echo ""
            echo "  常用命令："
            echo "    查看后端日志: cd $SCRIPT_DIR && docker compose logs -f app"
            echo "    查看前端日志: cd $SCRIPT_DIR && docker compose logs -f web"
            echo "    停止服务: cd $SCRIPT_DIR && docker compose down"
            echo "    重启服务: cd $SCRIPT_DIR && docker compose restart app web"
            return 0
        fi
        sleep 3
        waited=$((waited + 3))
        echo -n "."
    done

    echo ""
    log_warn "服务可能还未完全就绪，请检查日志："
    log_warn "  docker compose logs app"
    log_warn "  docker compose logs web"
}

show_usage() {
    echo "用法:"
    echo "  $0                 一键部署（后端+前端，默认使用官方镜像）"
    echo "  BASE_IMAGE_PREFIX=docker.xuanyuan.me/ $0  使用指定镜像前缀部署"
    echo "  $0 up              仅启动（不重建镜像）"
    echo "  $0 down            停止所有服务"
    echo "  $0 restart         重启前后端服务"
    echo "  $0 logs            查看所有日志"
    echo "  $0 logs-app        查看后端日志"
    echo "  $0 logs-web        查看前端日志"
    echo "  $0 status          查看服务状态"
    echo "  $0 build           仅构建镜像"
    echo ""
    echo "发包目录约定："
    echo "  后端 JAR 放到: $RELEASE_PKG_DIR"
    echo "  前端 dist 放到: $RELEASE_FRONTEND_DIR"
}

case "${1:-deploy}" in
    deploy)
        prepare_directories
        find_backend_jar
        find_frontend_dist
        backup_packages
        prepare_build_context
        deploy
        ;;
    up)
        log_info "启动所有服务..."
        BASE_IMAGE_PREFIX="$BASE_IMAGE_PREFIX" docker compose up -d
        ;;
    down)
        log_info "停止所有服务..."
        docker compose down
        ;;
    restart)
        log_info "重启前后端服务..."
        docker compose restart app web
        ;;
    logs)
        docker compose logs -f
        ;;
    logs-app)
        docker compose logs -f app
        ;;
    logs-web)
        docker compose logs -f web
        ;;
    status)
        docker compose ps
        ;;
    build)
        prepare_directories
        find_backend_jar
        find_frontend_dist
        prepare_build_context
        log_info "构建镜像..."
        BASE_IMAGE_PREFIX="$BASE_IMAGE_PREFIX" docker compose build --no-cache app web
        log_info "构建完成"
        ;;
    -h|--help|help)
        show_usage
        ;;
    *)
        show_usage
        exit 1
        ;;
esac
