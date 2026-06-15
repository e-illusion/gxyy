import api from './index'

export const exchangeApi = {
  list() {
    return api.get('/api/exchange-requests')
  },
  create(data) {
    return api.post('/api/exchange-requests', data)
  },
  accept(id) {
    return api.put(`/api/exchange-requests/${id}/accept`)
  },
  reject(id) {
    return api.put(`/api/exchange-requests/${id}/reject`)
  },
  cancel(id) {
    return api.put(`/api/exchange-requests/${id}/cancel`)
  },
}
