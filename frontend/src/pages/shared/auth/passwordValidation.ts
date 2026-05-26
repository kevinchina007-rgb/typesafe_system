const weakPasswordValues = new Set(['password', 'password123', '1234567890', 'qwerty123', 'admin123456', 'hzhishengheng'])

export function getPasswordValidationMessage(password: string, email?: string): string | null {
  const normalizedPassword = password.trim()

  if (!normalizedPassword) {
    return '密码还没填呢。'
  }

  if (normalizedPassword.length < 10) {
    return '密码至少需要 10 位，再给它加一点长度吧。'
  }

  const hasLetter = /[a-zA-Z]/.test(normalizedPassword)
  const hasDigit = /\d/.test(normalizedPassword)
  if (!hasLetter || !hasDigit) {
    return '这个密码太好猜啦，请同时包含字母和数字。'
  }

  if (weakPasswordValues.has(normalizedPassword.toLowerCase())) {
    return '这个密码已经在弱密码名单里了，换一个更特别的吧。'
  }

  const emailLocalPart = email?.split('@')[0]?.trim().toLowerCase() ?? ''
  if (emailLocalPart && emailLocalPart.includes(normalizedPassword.toLowerCase())) {
    return '密码不要直接藏在邮箱名里，太容易被猜到了。'
  }

  if (new Set(normalizedPassword.toLowerCase()).size <= 3) {
    return '这个密码重复字符太多啦，给它多一点变化。'
  }

  return null
}
