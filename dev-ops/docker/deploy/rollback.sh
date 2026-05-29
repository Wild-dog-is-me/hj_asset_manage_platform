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

BACKUP_DIR="$SCRIPT_DIR/backups"
BACKEND_BAK_DIR="$BACKUP_DIR/backend"
FRONTEND_BAK_DIR="$BACKUP_DIR/frontend"
RELEASE_PKG_DIR="$SCRIPT_DIR/release_pkg"
RELEASE_FRONTEND_DIR="$RELEASE_PKG_DIR/dist"
BASE_IMAGE_PREFIX="${BASE_IMAGE_PREFIX:-}"

pick_latest_file() {
  local dir="$1"
  local pattern="$2"
  ls -1t "$dir"/$pattern 2>/dev/null | head -n 1
}

rollback_backend() {
  local bak_file
  bak_file=$(pick_latest_file "$BACKEND_BAK_DIR" "*_bak.jar")
  if [ -z "$bak_file" ]; then
    log_error "未找到后端备份包：$BACKEND_BAK_DIR"
    return 1
  fi

  mkdir -p "$RELEASE_PKG_DIR"
  find "$RELEASE_PKG_DIR" -maxdepth 1 -name "*.jar" -type f -delete
  log_info "回滚后端包：$(basename "$bak_file")"
  cp -f "$bak_file" "$RELEASE_PKG_DIR/ruoyi-admin.jar"
}

rollback_frontend() {
  local bak_file
  bak_file=$(pick_latest_file "$FRONTEND_BAK_DIR" "dist_*_bak.tar.gz")
  if [ -z "$bak_file" ]; then
    log_error "未找到前端备份包：$FRONTEND_BAK_DIR"
    return 1
  fi

  mkdir -p "$RELEASE_PKG_DIR"
  log_info "回滚前端包：$(basename "$bak_file")"
  rm -rf "$RELEASE_FRONTEND_DIR"
  tar -xzf "$bak_file" -C "$RELEASE_PKG_DIR"
}

restart_services() {
  log_info "重建并重启前后端容器..."
  BASE_IMAGE_PREFIX="$BASE_IMAGE_PREFIX" "$SCRIPT_DIR/deploy.sh" build
  BASE_IMAGE_PREFIX="$BASE_IMAGE_PREFIX" docker compose down || true
  BASE_IMAGE_PREFIX="$BASE_IMAGE_PREFIX" docker compose up -d
  log_info "回滚完成，可使用 docker compose ps 查看状态"
}

show_usage() {
  echo "用法:"
  echo "  $0              回滚前后端到最近一次备份"
  echo "  $0 backend      仅回滚后端"
  echo "  $0 frontend     仅回滚前端"
  echo "  $0 list         查看备份列表"
}

list_backups() {
  echo "后端备份："
  ls -1t "$BACKEND_BAK_DIR"/*_bak.jar 2>/dev/null || echo "  (无)"
  echo ""
  echo "前端备份："
  ls -1t "$FRONTEND_BAK_DIR"/dist_*_bak.tar.gz 2>/dev/null || echo "  (无)"
}

case "${1:-all}" in
  all)
    rollback_backend
    rollback_frontend
    restart_services
    ;;
  backend)
    rollback_backend
    restart_services
    ;;
  frontend)
    rollback_frontend
    restart_services
    ;;
  list)
    list_backups
    ;;
  -h|--help|help)
    show_usage
    ;;
  *)
    show_usage
    exit 1
    ;;
esac
