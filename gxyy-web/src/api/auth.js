import api from './index'

export const authApi = {
  login(data) {
    return api.post('/api/auth/login', data)
  },
  register(data) {
    return api.post('/api/auth/register', data)
  },
}
