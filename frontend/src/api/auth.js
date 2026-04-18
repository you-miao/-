import request from '@/utils/request'

export function login(data) {
  return request.post('/auth/login', data)
}

export function register(data) {
  return request.post('/auth/register', data)
}

export function checkUsernameAvailable(username) {
  return request.get('/auth/check-username', { params: { username } })
}

export function checkStudentNoAvailable(studentNo) {
  return request.get('/auth/check-student-no', { params: { studentNo } })
}
