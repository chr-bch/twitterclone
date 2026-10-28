import './assets/main.css'
import { createApp } from 'vue'
import App from './App.vue'
import { initAuth } from './auth'

await initAuth()
createApp(App).mount('#app')
