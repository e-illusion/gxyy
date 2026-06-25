import api from './index'

export const favoriteApi = {
  /** 获取我的收藏列表 */
  list() {
    return api.get('/api/favorites')
  },
  /** 添加收藏 */
  add(itemId) {
    return api.post(`/api/favorites/${itemId}`)
  },
  /** 取消收藏 */
  remove(itemId) {
    return api.delete(`/api/favorites/${itemId}`)
  },
  /** 检查是否已收藏 */
  check(itemId) {
    return api.get(`/api/favorites/${itemId}/check`)
  },
}
