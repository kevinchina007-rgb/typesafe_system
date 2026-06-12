// 本文件定义应用级设计令牌，包括颜色、间距、字号和圆角。

export const designTokens = {
  spacing: {
    xs: '0.375rem',
    sm: '0.625rem',
    md: '1rem',
    lg: '1.5rem',
    xl: '2rem',
    xxl: '3rem',
  },
  radius: {
    sm: '0.875rem',
    md: '1.25rem',
    lg: '1.75rem',
    xl: '2.25rem',
    pill: '999px',
  },
  typography: {
    eyebrow: '0.78rem',
    body: '0.98rem',
    sectionTitle: '1.4rem',
    pageTitle: '2.5rem',
    heroTitle: '3.4rem',
  },
  shadows: {
    soft: '0 20px 60px rgba(8, 15, 32, 0.22)',
    card: '0 22px 80px rgba(6, 15, 33, 0.2)',
    glow: '0 0 0 1px rgba(255,255,255,0.08), 0 24px 70px rgba(89, 92, 255, 0.16)',
  },
  colors: {
    canvas: '#050816',
    surface: 'rgba(11, 18, 36, 0.7)',
    surfaceStrong: 'rgba(13, 22, 43, 0.88)',
    border: 'rgba(255, 255, 255, 0.08)',
    textPrimary: '#f5f7ff',
    textSecondary: '#a4adca',
    accentPrimary: '#7c8cff',
    accentSecondary: '#39d0ff',
    accentTertiary: '#ff8bbd',
    success: '#71f4c1',
  },
} as const

export type DesignTokens = typeof designTokens
