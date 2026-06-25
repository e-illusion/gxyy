import api from './index'

export const followApi = {
  follow(userId) {
    return api.post(`/api/follow/${userId}`)
  },
  unfollow(userId) {
    return api.delete(`/api/follow/${userId}`)
  },
  check(userId) {
    return api.get(`/api/follow/${userId}/check`)
  },
  followers(userId) {
    return api.get(`/api/follow/${userId}/followers`)
  },
  following(userId) {
    return api.get(`/api/follow/${userId}/following`)
  },
  counts(userId) {
    return api.get(`/api/follow/${userId}/counts`)
  },
}
