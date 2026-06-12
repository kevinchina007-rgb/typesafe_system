// 本文件定义前端应用入口，负责挂载根组件、路由和全局样式。

import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'

import { MvpApp } from '@/pages/AppPage'
import '@/index.css'

document.documentElement.dataset.theme = 'light'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <MvpApp />
  </StrictMode>,
)
