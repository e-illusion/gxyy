// GXYY 共享常量 — 单一定义，全局引用

export const CATEGORIES = [
  { id: 1, name: '数码电子' },
  { id: 2, name: '书籍教材' },
  { id: 3, name: '生活用品' },
  { id: 4, name: '服饰鞋包' },
  { id: 5, name: '运动器材' },
  { id: 6, name: '美妆护肤' },
  { id: 7, name: '玩具乐器' },
  { id: 8, name: '其他' },
]

// 带"全部"选项的版本，用于首页过滤
export const CATEGORIES_WITH_ALL = [
  { id: null, name: '全部' },
  ...CATEGORIES,
]

export const CONDITION_MAP = {
  'NEW': '全新',
  'LIKE_NEW': '几乎全新',
  'SLIGHTLY_USED': '轻微使用',
  'NORMAL_USE': '正常使用',
}

export const CONDITION_OPTIONS = [
  { value: 'NEW', label: '全新' },
  { value: 'LIKE_NEW', label: '几乎全新' },
  { value: 'SLIGHTLY_USED', label: '轻微使用' },
  { value: 'NORMAL_USE', label: '正常使用' },
]

// 物品状态映射（统一为 object-of-objects 格式，携带 label 和 type）
export const STATUS_MAP = {
  'ACTIVE': { label: '在架', type: 'success' },
  'EXCHANGED': { label: '已换出', type: 'info' },
  'OFF_SHELF': { label: '已下架', type: 'info' },
}

// 交换请求状态映射
export const EXCHANGE_STATUS_MAP = {
  'PENDING': { label: '待确认', type: 'warning' },
  'ACCEPTED': { label: '已同意', type: 'success' },
  'REJECTED': { label: '已拒绝', type: 'danger' },
  'CANCELLED': { label: '已取消', type: 'info' },
}

// 通知类型映射
export const NOTIFICATION_TYPE_MAP = {
  'EXCHANGE_REQUEST': { icon: 'Bell', color: 'var(--color-primary)', label: '交换请求' },
  'EXCHANGE_ACCEPTED': { icon: 'CircleCheck', color: 'var(--color-success)', label: '请求通过' },
  'EXCHANGE_REJECTED': { icon: 'CircleClose', color: 'var(--color-danger)', label: '请求拒绝' },
}
