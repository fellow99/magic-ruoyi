<template>
  <div class="magic-editor-container">
    <magic-editor :config="editorConfig" />
  </div>
</template>

<script setup name="MagicWeb">
import { ref } from 'vue'
import MagicEditor from '@fellow99/magic-editor'
import '@fellow99/magic-editor/dist/magic-editor.css'
import { getToken } from '@/utils/auth'

const editorConfig = ref({
  baseURL: `${window.location.origin}${import.meta.env.VITE_APP_BASE_API}/magic/web`,
  serverURL: `${window.location.origin}${import.meta.env.VITE_APP_BASE_API}/`,
  request: {
    beforeSend(config) {
      const token = getToken()
      if (token) {
        config.headers = config.headers || {}
        config.headers['Authorization'] = `Bearer ${token}`
      }
      return config
    },
    onError(err) {
      return Promise.reject(err)
    }
  }
})
</script>

<style scoped>
.magic-editor-container {
  width: 100%;
  height: calc(100vh - 84px);
  overflow: hidden;
}
</style>
