import api from './index'

export const notificationApi = {
  list() {
    return api.get('/api/notifications')
  },
  markRead(id) {
    return api.put(`/api/notifications/${id}/read`)
  },
  unreadCount() {
    return api.get('/api/notifications/unread-count')
  },
}
