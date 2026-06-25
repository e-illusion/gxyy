import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useFavoriteStore = defineStore('favorite', () => {
  const favoriteIds = ref(new Set())
  const favoriteItems = ref([])

  function setFavorites(items) {
    favoriteItems.value = items
    favoriteIds.value = new Set(items.map(i => i.id))
  }

  function isFavorited(itemId) {
    return favoriteIds.value.has(itemId)
  }

  function addFavorite(item) {
    favoriteIds.value.add(item.id)
    favoriteItems.value.unshift(item)
  }

  function removeFavorite(itemId) {
    favoriteIds.value.delete(itemId)
    favoriteItems.value = favoriteItems.value.filter(i => i.id !== itemId)
  }

  return { favoriteIds, favoriteItems, setFavorites, isFavorited, addFavorite, removeFavorite }
})
