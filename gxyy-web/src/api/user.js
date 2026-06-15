import api from './index'

export const userApi = {
  getProfile() {
    return api.get('/api/users/me')
  },
  updateProfile(data) {
    return api.put('/api/users/me', data)
  },
  uploadAvatar(file) {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/api/users/me/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 120000,
    })
  },
  getUserById(id) {
    return api.get(`/api/users/${id}`)
  },
}

export const aiApi = {
  polish(description) {
    return api.post('/api/ai/polish', { description })
  },
}
