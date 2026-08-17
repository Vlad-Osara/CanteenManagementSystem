<template>
  <nav class="navbar navbar-expand-lg navbar-canteen sticky-top">
    <div class="container">
      <!-- Brand / Logo -->
      <router-link class="navbar-brand d-flex align-items-center gap-2" to="/">
        <img src="/favicon.svg" alt="Canteen Logo" width="36" height="36" class="rounded-3 bg-white p-1" />
        <span class="fs-4">Căn Tin Smart</span>
      </router-link>

      <!-- Mobile Toggle -->
      <button 
        class="navbar-toggler border-0 text-white shadow-none" 
        type="button" 
        data-bs-toggle="collapse" 
        data-bs-target="#navbarMain"
      >
        <i class="bi bi-list fs-2"></i>
      </button>

      <!-- Nav items -->
      <div class="collapse navbar-collapse" id="navbarMain">
        <ul class="navbar-nav me-auto mb-2 mb-lg-0 ms-lg-3 gap-1">
          <li class="nav-item">
            <router-link class="nav-link" to="/" active-class="active">
              <i class="bi bi-shop me-1"></i> Thực đơn
            </router-link>
          </li>
          
          <!-- Customer Orders -->
          <li class="nav-item" v-if="authStore.isCustomer">
            <router-link class="nav-link" to="/orders" active-class="active">
              <i class="bi bi-receipt me-1"></i> Đơn hàng của tôi
            </router-link>
          </li>

          <!-- Staff Links -->
          <li class="nav-item dropdown" v-if="authStore.isStaff">
            <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
              <i class="bi bi-headset me-1"></i> Bếp & Thu Ngân
            </a>
            <ul class="dropdown-menu border-0 shadow-lg rounded-3">
              <li>
                <router-link class="dropdown-item py-2" to="/staff/orders">
                  <i class="bi bi-hourglass-split me-2 text-primary"></i> Quản lý Đơn Xử Lý
                </router-link>
              </li>
              <li>
                <router-link class="dropdown-item py-2" to="/staff/dishes">
                  <i class="bi bi-toggle-on me-2 text-primary"></i> Trạng thái Món Ăn
                </router-link>
              </li>
            </ul>
          </li>

          <!-- Admin Links -->
          <li class="nav-item dropdown" v-if="authStore.isAdmin">
            <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
              <i class="bi bi-shield-lock me-1"></i> Quản Trị Hệ Thống
            </a>
            <ul class="dropdown-menu border-0 shadow-lg rounded-3">
              <li>
                <router-link class="dropdown-item py-2" to="/admin">
                  <i class="bi bi-speedometer2 me-2 text-primary"></i> Tổng Quan (Dashboard)
                </router-link>
              </li>
              <li><hr class="dropdown-divider"></li>
              <li>
                <router-link class="dropdown-item py-2" to="/admin/dishes">
                  <i class="bi bi-egg-fried me-2 text-primary"></i> Quản lý Món Ăn
                </router-link>
              </li>
              <li>
                <router-link class="dropdown-item py-2" to="/admin/categories">
                  <i class="bi bi-tags me-2 text-primary"></i> Quản lý Danh Mục
                </router-link>
              </li>
              <li>
                <router-link class="dropdown-item py-2" to="/admin/accounts">
                  <i class="bi bi-people me-2 text-primary"></i> Quản lý Tài Khoản
                </router-link>
              </li>
            </ul>
          </li>
        </ul>

        <!-- Right Side User controls -->
        <div class="d-flex align-items-center gap-3">
          <!-- Cart Button for Customers -->
          <button 
            v-if="!authStore.user || authStore.isCustomer" 
            class="btn btn-light position-relative rounded-pill px-3 py-2 fw-semibold text-primary d-flex align-items-center gap-2 shadow-sm"
            @click="$emit('openCart')"
          >
            <i class="bi bi-cart3 fs-5"></i>
            <span class="d-none d-sm-inline">Giỏ hàng</span>
            <span 
              v-if="cartStore.totalItems > 0" 
              class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger shadow"
            >
              {{ cartStore.totalItems }}
            </span>
          </button>

          <!-- Guest login/register -->
          <div v-if="!authStore.isAuthenticated" class="d-flex gap-2">
            <router-link to="/login" class="btn btn-outline-light rounded-pill px-3 py-2 fw-semibold">
              <i class="bi bi-box-arrow-in-right me-1"></i> Đăng Nhập
            </router-link>
            <router-link to="/register" class="btn btn-light text-primary rounded-pill px-3 py-2 fw-semibold shadow-sm">
              Đăng Ký
            </router-link>
          </div>

          <!-- Logged in User Menu -->
          <div v-else class="dropdown">
            <button 
              class="btn btn-light rounded-pill px-3 py-2 fw-semibold d-flex align-items-center gap-2 text-primary shadow-sm dropdown-toggle" 
              type="button" 
              data-bs-toggle="dropdown"
            >
              <i class="bi bi-person-circle fs-5"></i>
              <span class="text-truncate" style="max-width: 160px;">{{ authStore.user.fullName || authStore.user.username }}</span>
            </button>
            <ul class="dropdown-menu dropdown-menu-start dropdown-menu-lg-end border-0 shadow-lg rounded-3 p-2" style="min-width: 220px;">
              <li class="px-3 py-2 bg-light rounded-2 mb-2">
                <div class="fw-bold text-dark fs-6">{{ authStore.user.fullName }}</div>
                <div class="small text-muted mb-1">{{ authStore.user.email || authStore.user.username }}</div>
                <div class="badge bg-primary rounded-pill">{{ roleLabel }}</div>
              </li>

              <li v-if="authStore.isCustomer" class="px-3 py-2 mb-2 border-bottom">
                <div class="small text-muted">Số dư hiện tại:</div>
                <div class="fw-bold fs-5 text-primary">{{ formatVND(authStore.userBalance) }}</div>
                <button class="btn btn-sm btn-outline-primary w-100 mt-2 rounded-pill" @click="$emit('openDeposit')">
                  <i class="bi bi-wallet2 me-1"></i> Nạp Tiền Ví
                </button>
              </li>

              <li>
                <router-link class="dropdown-item rounded-2 py-2" to="/profile">
                  <i class="bi bi-person-gear me-2 text-muted"></i> Trang Cá Nhân
                </router-link>
              </li>

              <li><hr class="dropdown-divider"></li>

              <li>
                <button class="dropdown-item rounded-2 py-2 text-danger fw-semibold" @click="handleLogout">
                  <i class="bi bi-box-arrow-right me-2"></i> Đăng Xuất
                </button>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import { useCartStore } from '../stores/cart';

const authStore = useAuthStore();
const cartStore = useCartStore();
const router = useRouter();

defineEmits(['openCart', 'openDeposit']);

const roleLabel = computed(() => {
  const role = authStore.userRole;
  if (role === 'ROLE_ADMIN' || role === 'ADMIN') return 'Quản Trị Viên';
  if (role === 'ROLE_STAFF' || role === 'STAFF') return 'Nhân Viên Bếp';
  return 'Khách Hàng';
});

const formatVND = (amount) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount || 0);
};

const handleLogout = async () => {
  await authStore.logout();
  router.push('/login');
};
</script>
