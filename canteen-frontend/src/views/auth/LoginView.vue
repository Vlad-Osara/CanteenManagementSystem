<template>
  <div class="min-vh-100 d-flex align-items-center justify-content-center bg-slate py-5 px-3">
    <div class="card card-canteen border-0 shadow-lg p-4 p-md-5" style="max-width: 440px; width: 100%;">
      <!-- Header -->
      <div class="text-center mb-4">
        <router-link to="/" class="d-inline-flex p-3 bg-light rounded-circle mb-3">
          <img src="/favicon.svg" alt="Logo" width="48" height="48" />
        </router-link>
        <h3 class="fw-extrabold text-primary mb-1">Căn Tin Smart</h3>
        <p class="text-muted small">Đăng nhập tài khoản để đặt món & nạp tiền số dư</p>
      </div>

      <!-- Error alert -->
      <div v-if="errorMsg" class="alert alert-danger py-2 small mb-4 rounded-3 d-flex align-items-center gap-2">
        <i class="bi bi-exclamation-octagon-fill text-danger fs-5"></i>
        <div>{{ errorMsg }}</div>
      </div>

      <!-- Form -->
      <form @submit.prevent="handleLogin">
        <div class="mb-3">
          <label class="form-label fw-bold small text-muted">Tên tài khoản</label>
          <div class="input-group">
            <span class="input-group-text bg-light border-end-0 text-muted"><i class="bi bi-person"></i></span>
            <input 
              type="text" 
              class="form-control form-control-lg border-start-0 fs-6" 
              placeholder="Nhập tên tài khoản"
              v-model="form.username"
              required
            />
          </div>
        </div>

        <div class="mb-4">
          <div class="d-flex justify-content-between align-items-center mb-1">
            <label class="form-label fw-bold small text-muted mb-0">Mật khẩu</label>
            <router-link to="/forgot-password" class="small text-primary text-decoration-none fw-semibold">
              Quên mật khẩu?
            </router-link>
          </div>
          <div class="input-group">
            <span class="input-group-text bg-light border-end-0 text-muted"><i class="bi bi-lock"></i></span>
            <input 
              type="password" 
              class="form-control form-control-lg border-start-0 fs-6" 
              placeholder="Nhập mật khẩu"
              v-model="form.password"
              required
            />
          </div>
        </div>

        <button 
          type="submit" 
          class="btn btn-canteen-primary w-100 py-3 rounded-3 fw-bold fs-6 shadow-md mb-3"
          :disabled="loading"
        >
          <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
          <i v-else class="bi bi-box-arrow-in-right me-1"></i> ĐẮNG NHẬP NGAY
        </button>
      </form>

      <!-- Footer register link -->
      <div class="text-center mt-3 pt-3 border-top">
        <span class="text-muted small me-1">Chưa có tài khoản?</span>
        <router-link to="/register" class="fw-bold text-primary text-decoration-none">
          Đăng ký tài khoản mới
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '../../stores/auth';

const authStore = useAuthStore();
const router = useRouter();
const route = useRoute();

const form = reactive({
  username: '',
  password: ''
});

const loading = ref(false);
const errorMsg = ref('');
const handleLogin = async () => {
  loading.value = true;
  errorMsg.value = '';
  try {
    const user = await authStore.login(form);
    
    // Redirect based on role or intended destination
    const redirectPath = route.query.redirect;
    if (redirectPath) {
      router.push(redirectPath);
    } else if (user.role === 'ROLE_ADMIN' || user.role === 'ADMIN') {
      router.push('/admin');
    } else if (user.role === 'ROLE_STAFF' || user.role === 'STAFF') {
      router.push('/staff/orders');
    } else {
      router.push('/');
    }
  } catch (err) {
    errorMsg.value = err.customMessage || 'Tên đăng nhập hoặc mật khẩu không chính xác!';
  } finally {
    loading.value = false;
  }
};
</script>
