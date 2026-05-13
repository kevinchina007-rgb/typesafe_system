import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'

import { MvpApp } from '@/pages/AppPage'
import '@/index.css'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <MvpApp />
  </StrictMode>,
)
