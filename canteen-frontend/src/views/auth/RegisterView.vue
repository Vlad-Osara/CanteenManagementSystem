<template>
  <div class="min-vh-100 d-flex align-items-center justify-content-center bg-slate py-5 px-3">
    <div class="card card-canteen border-0 shadow-lg p-4 p-md-5" style="max-width: 480px; width: 100%;">
      <!-- Header -->
      <div class="text-center mb-4">
        <router-link to="/" class="d-inline-flex p-3 bg-light rounded-circle mb-3">
          <img src="/favicon.svg" alt="Logo" width="48" height="48" />
        </router-link>
        <h3 class="fw-extrabold text-primary mb-1">Tạo Tài Khoản Căn Tin</h3>
        <p class="text-muted small">Đăng ký tài khoản để trải nghiệm đặt món thông minh</p>
      </div>

      <!-- Alerts -->
      <div v-if="errorMsg" class="alert alert-danger py-2 small mb-4 rounded-3 d-flex align-items-center gap-2">
        <i class="bi bi-exclamation-octagon-fill text-danger fs-5"></i>
        <div>{{ errorMsg }}</div>
      </div>
      <div v-if="successMsg" class="alert alert-success py-2 small mb-4 rounded-3">
        <i class="bi bi-check-circle-fill text-success me-1"></i> {{ successMsg }}
      </div>

      <!-- Form -->
      <form @submit.prevent="handleRegister">
        <div class="mb-3">
          <label class="form-label fw-bold small text-muted">Tên tài khoản (*)</label>
          <div class="input-group">
            <span class="input-group-text bg-light border-end-0 text-muted"><i class="bi bi-person"></i></span>
            <input 
              type="text" 
              class="form-control border-start-0" 
              placeholder="Nhập username"
              v-model="form.username"
              required
            />
            <div v-if="fieldErrors.username" class="invalid-feedback d-block small mt-1">
              <i class="bi bi-exclamation-circle me-1"></i> {{ fieldErrors.username }}
            </div>
          </div>
        </div>

        <div class="mb-3">
          <label class="form-label fw-bold small text-muted">Họ và tên (*)</label>
          <div class="input-group">
            <span class="input-group-text bg-light border-end-0 text-muted"><i class="bi bi-card-heading"></i></span>
            <input 
              type="text" 
              class="form-control border-start-0" 
              placeholder="Ví dụ: Nguyễn Văn A"
              v-model="form.fullName"
              required
            />
            <div v-if="fieldErrors.fullName" class="invalid-feedback d-block small mt-1">
              <i class="bi bi-exclamation-circle me-1"></i> {{ fieldErrors.fullName }}
            </div>
          </div>
        </div>

        <div class="row g-2 mb-3">
          <div class="col-md-6">
            <label class="form-label fw-bold small text-muted">Email</label>
            <input 
              type="email" 
              class="form-control" 
              placeholder="email@example.com"
              v-model="form.email"
            />
            <div v-if="fieldErrors.email" class="invalid-feedback d-block small mt-1">
              <i class="bi bi-exclamation-circle me-1"></i> {{ fieldErrors.email }}
            </div>
          </div>
          <div class="col-md-6">
            <label class="form-label fw-bold small text-muted">Số điện thoại</label>
            <input 
              type="tel" 
              class="form-control" 
              placeholder="0912345678"
              v-model="form.phoneNumber"
            />
            <div v-if="fieldErrors.phoneNumber" class="invalid-feedback d-block small mt-1">
              <i class="bi bi-exclamation-circle me-1"></i> {{ fieldErrors.phoneNumber }}
            </div>
          </div>
        </div>

        <div class="mb-4">
          <label class="form-label fw-bold small text-muted">Mật khẩu (*)</label>
          <div class="input-group">
            <span class="input-group-text bg-light border-end-0 text-muted"><i class="bi bi-lock"></i></span>
            <input 
              type="password" 
              class="form-control border-start-0" 
              placeholder="Tối thiểu 8 ký tự"
              v-model="form.password"
              minlength="8"
              required
            />
            <div v-if="fieldErrors.password" class="invalid-feedback d-block small mt-1">
              <i class="bi bi-exclamation-circle me-1"></i> {{ fieldErrors.password }}
            </div>
          </div>
        </div>

        <button 
          type="submit" 
          class="btn btn-canteen-primary w-100 py-3 rounded-3 fw-bold fs-6 shadow-md mb-3"
          :disabled="loading"
        >
          <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
          <i v-else class="bi bi-person-plus me-1"></i> HOÀN TẤT ĐĂNG KÝ
        </button>
      </form>

      <!-- Footer login link -->
      <div class="text-center mt-3 pt-3 border-top">
        <span class="text-muted small me-1">Đã có tài khoản?</span>
        <router-link to="/login" class="fw-bold text-primary text-decoration-none">
          Đăng nhập ngay
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../../stores/auth';

const authStore = useAuthStore();
const router = useRouter();

const form = reactive({
  username: '',
  password: '',
  fullName: '',
  email: '',
  phoneNumber: ''
});

const loading = ref(false);
const errorMsg = ref('');
const successMsg = ref('');
// Validation errors for each field
const fieldErrors = ref({});

const handleRegister = async () => {
  loading.value = true;
  errorMsg.value = '';
  successMsg.value = '';
  fieldErrors.value = {}; // Reset field errors before validation
  try {
    await authStore.register(form);
    successMsg.value = 'Đăng ký tài khoản thành công! Đang chuyển đến trang đăng nhập...';
    setTimeout(() => {
      router.push('/login');
    }, 1500);
  } catch (err) {
    errorMsg.value = err.customMessage || 'Đăng ký thất bại. Vui lòng kiểm tra lại thông tin!';
    if (err.fieldErrors) {
      fieldErrors.value = err.fieldErrors;
    }
  } finally {
    loading.value = false;
  }
};
</script>
