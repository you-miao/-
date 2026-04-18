export function resolveImageUrl(url) {
  if (!url) return ''
  let value = String(url).trim()
  if (!value) return ''

  value = value.replace(/\\/g, '/')

  if (/^https?:\/\//i.test(value)) return value
  if (value.startsWith('/api/')) return value

  const uploadIndex = value.indexOf('/uploads/')
  if (uploadIndex >= 0) {
    return `/api${value.substring(uploadIndex)}`
  }

  if (value.startsWith('/uploads/')) return `/api${value}`
  if (value.startsWith('uploads/')) return `/api/${value}`
  if (value.startsWith('/')) return `/api${value}`
  return `/api/${value}`
}
