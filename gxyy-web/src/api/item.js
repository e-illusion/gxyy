import api from './index'

export const itemApi = {
  list(params) {
    return api.get('/api/items', { params })
  },
  detail(id) {
    return api.get(`/api/items/${id}`)
  },
  create(formData) {
    return api.post('/api/items', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
  update(id, data) {
    return api.put(`/api/items/${id}`, data)
  },
  delete(id) {
    return api.delete(`/api/items/${id}`)
  },
  myItems() {
    return api.get('/api/items/my')
  },
  offShelf(id) {
    return api.put(`/api/items/${id}/off-shelf`)
  },
}
