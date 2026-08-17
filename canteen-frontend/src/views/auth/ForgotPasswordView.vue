<template>
  <div class="min-vh-100 d-flex align-items-center justify-content-center bg-slate py-5 px-3">
    <div class="card card-canteen border-0 shadow-lg p-4 p-md-5" style="max-width: 460px; width: 100%;">
      <div class="text-center mb-4">
        <router-link to="/" class="d-inline-flex p-3 bg-light rounded-circle mb-3">
          <img src="/favicon.svg" alt="Logo" width="48" height="48" />
        </router-link>
        <h3 class="fw-extrabold text-primary mb-1">Quên Mật Khẩu</h3>
        <p class="text-muted small">Nhập email để nhận mã xác thực OTP lấy lại mật khẩu</p>
      </div>

      <div v-if="errorMsg" class="alert alert-danger py-2 small mb-3">
        <i class="bi bi-exclamation-octagon-fill me-1"></i> {{ errorMsg }}
      </div>
      <div v-if="successMsg" class="alert alert-success py-2 small mb-3">
        <i class="bi bi-check-circle-fill me-1"></i> {{ successMsg }}
      </div>

      <!-- BƯỚC 1: Nhập Email xin mã OTP -->
      <form v-if="step === 1" @submit.prevent="handleSendOtp">
        <div class="mb-4">
          <label class="form-label fw-bold small text-muted">Email tài khoản (*)</label>
          <div class="input-group">
            <span class="input-group-text bg-light border-end-0 text-muted"><i class="bi bi-envelope"></i></span>
            <input type="email" class="form-control" placeholder="nhapemail@example.com" v-model="email" required />
          </div>
        </div>

        <button type="submit" class="btn btn-canteen-primary w-100 py-3 rounded-3 fw-bold mb-3" :disabled="loading">
          <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
          <i v-else class="bi bi-send me-1"></i> GỬI MÃ XÁC THỰC OTP
        </button>
      </form>

      <!-- BƯỚC 2: Nhập OTP & Đặt Mật Khẩu Mới -->
      <form v-else @submit.prevent="handleResetPassword">
        <div class="mb-3">
          <label class="form-label fw-bold small text-muted">Mã OTP (6 chữ số) (*)</label>
          <input type="text" class="form-control text-center fw-bold fs-4 tracking-widest" placeholder="123456" maxlength="6" v-model="otpCode" required />
        </div>

        <div class="mb-4">
          <label class="form-label fw-bold small text-muted">Mật khẩu mới (*)</label>
          <input type="password" class="form-control" placeholder="Tối thiểu 8 ký tự" minlength="8" v-model="newPassword" required />
        </div>

        <button type="submit" class="btn btn-canteen-primary w-100 py-3 rounded-3 fw-bold mb-3" :disabled="loading">
          <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
          <i v-else class="bi bi-check-circle me-1"></i> ĐẶT LẠI MẬT KHẨU
        </button>

        <button type="button" class="btn btn-link text-muted btn-sm w-100 text-decoration-none" @click="step = 1">
          <i class="bi bi-arrow-left me-1"></i> Nhập lại Email khác
        </button>
      </form>

      <div class="text-center mt-3 pt-3 border-top">
        <router-link to="/login" class="fw-bold text-primary text-decoration-none">
          <i class="bi bi-box-arrow-in-right me-1"></i> Quay lại Đăng Nhập
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import api from '../../api/axios';

const router = useRouter();
const step = ref(1);
const email = ref('');
const otpCode = ref('');
const newPassword = ref('');
const loading = ref(false);
const errorMsg = ref('');
const successMsg = ref('');

// MẪU HTML EMAIL
const getEmailTemplate = () => {
  return `
    <div style="font-family: Arial, sans-serif; padding: 25px; background-color: #f8fafc;">
      <div style="max-width: 520px; margin: 0 auto; background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 25px rgba(2, 132, 199, 0.1); border: 1px solid #e0f2fe;">
        <div style="background: linear-gradient(135deg, #0284c7 0%, #0369a1 100%); padding: 30px; text-align: center;">
          <h1 style="color: #ffffff; margin: 0; font-size: 24px; font-weight: 800;">Căn Tin Smart</h1>
          <p style="color: #e0f2fe; margin: 5px 0 0 0; font-size: 14px;">Hệ Thống Quản Lý & Đặt Món Thông Minh</p>
        </div>
        <div style="padding: 30px; color: #1e293b;">
          <h3 style="color: #0284c7; margin-top: 0;">Mã Xác Thực Quên Mật Khẩu</h3>
          <p style="font-size: 15px; line-height: 1.6; color: #475569;">
            Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu cho tài khoản liên kết với email này. Vui lòng sử dụng mã OTP bên dưới để hoàn tất:
          </p>
          <div style="background-color: #f0f9ff; border: 2px dashed #38bdf8; border-radius: 12px; padding: 20px; text-align: center; margin: 25px 0;">
            <span style="font-size: 36px; font-weight: 800; color: #0284c7; letter-spacing: 8px;">{{OTP}}</span>
          </div>
          <p style="font-size: 13px; color: #64748b; line-height: 1.5;">
            * Mã OTP có hiệu lực trong <b>10 phút</b>. Vui lòng không chia sẻ mã này cho bất kỳ ai.
          </p>
        </div>
        <div style="background-color: #f1f5f9; padding: 15px; text-align: center; font-size: 12px; color: #94a3b8;">
          &copy; 2026 Canteen Management System. All rights reserved.
        </div>
      </div>
    </div>
  `;
};

const handleSendOtp = async () => {
  loading.value = true;
  errorMsg.value = '';
  successMsg.value = '';
  try {
    const res = await api.post('/api/auth/forgot-password', { 
      email: email.value,
      htmlTemplate: getEmailTemplate() // Gửi kèm mẫu HTML Email sang Backend
    });
    successMsg.value = res.data.message || 'Mã OTP đã được gửi đến email của bạn!';
    step.value = 2;
  } catch (err) {
    errorMsg.value = err.customMessage || 'Không thể gửi mã OTP!';
  } finally {
    loading.value = false;
  }
};

const handleResetPassword = async () => {
  loading.value = true;
  errorMsg.value = '';
  successMsg.value = '';
  try {
    const res = await api.post('/api/auth/reset-password', {
      email: email.value,
      otpCode: otpCode.value,
      newPassword: newPassword.value
    });
    successMsg.value = res.data.message || 'Đặt lại mật khẩu thành công!';
    setTimeout(() => router.push('/login'), 2000);
  } catch (err) {
    errorMsg.value = err.customMessage || 'Đặt lại mật khẩu thất bại!';
  } finally {
    loading.value = false;
  }
};
</script>