import axios from 'axios';

// Get API base URL from Vite environment variables or fallback to Cloudflare domain
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'https://api.huyloi.uk';


const api = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  },
});

// Response Interceptor bóc tách lỗi chi tiết từ Backend
api.interceptors.response.use(
  (response) => response,
  (error) => {
    let message = 'Đã có lỗi xảy ra. Vui lòng thử lại!';
    let fieldErrors = null; // Chứa object các lỗi từng trường
    if (error.response && error.response.data) {
      const resData = error.response.data; // Dữ liệu lỗi từ Backend
      // Nếu Backend trả về lỗi dạng chuỗi hoặc có thuộc tính "message", sử dụng nó làm thông báo lỗi
      if (typeof resData === 'string') {
        message = resData;
      } else if (resData.message) {
        message = resData.message;
      }
      // Bóc tách object lỗi từng trường từ thuộc tính "data" nếu có và khác mảng
      if (resData.data && typeof resData.data === 'object' && !Array.isArray(resData.data)) {
        fieldErrors = resData.data;
      }
    }
    error.customMessage = message;
    error.fieldErrors = fieldErrors; // Gán object lỗi để Component đọc được
    return Promise.reject(error);
  }
);

export default api;
