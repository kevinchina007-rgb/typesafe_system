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
