import request from '@/utils/request'

export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function uploadFiles(files) {
  const formData = new FormData()
  files.forEach(f => formData.append('files', f))
  return request.post('/file/upload/batch', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
