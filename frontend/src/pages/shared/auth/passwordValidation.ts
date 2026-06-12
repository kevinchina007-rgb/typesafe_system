// 本文件定义密码强度校验规则，供注册、登录和改密流程复用。

// 弱口令黑名单，避免用户设置过于常见的密码。
const weakPasswordValues = new Set(['password', 'password123', '1234567890', 'qwerty123', 'admin123456', 'hzhishengheng'])

// 校验密码强度并返回提示文案，满足要求时返回 null。
export function getPasswordValidationMessage(password: string, email?: string): string | null {
  const normalizedPassword = password.trim()

  // 空密码直接提示用户补填。
  if (!normalizedPassword) {
    return '密码还没填写。'
  }

  // 长度不足时提醒用户增加长度。
  if (normalizedPassword.length < 10) {
    return '密码至少需要 10 位。'
  }

  // 密码必须同时包含字母和数字。
  const hasLetter = /[a-zA-Z]/.test(normalizedPassword)
  const hasDigit = /\d/.test(normalizedPassword)
  if (!hasLetter || !hasDigit) {
    return '密码需要同时包含字母和数字。'
  }

  // 命中弱口令黑名单时直接拒绝。
  if (weakPasswordValues.has(normalizedPassword.toLowerCase())) {
    return '这个密码太常见了，请换一个更特别的。'
  }

  // 不允许密码直接包含邮箱本地部分。
  const emailLocalPart = email?.split('@')[0]?.trim().toLowerCase() ?? ''
  if (emailLocalPart && emailLocalPart.includes(normalizedPassword.toLowerCase())) {
    return '密码不要直接包含邮箱名。'
  }

  // 过于单一的重复字符也不算安全密码。
  if (new Set(normalizedPassword.toLowerCase()).size <= 3) {
    return '密码变化太少了，请再复杂一点。'
  }

  return null
}
