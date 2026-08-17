<template>
  <div class="d-flex flex-column min-vh-100 bg-slate">
    <!-- Top Navbar -->
    <Navbar @openCart="openCartDrawer" @openDeposit="openDepositModal" />

    <!-- Main Content -->
    <main class="flex-grow-1 py-4">
      <div class="container">
        <!-- Optional Toast Alert -->
        <div v-if="toastMessage" class="toast-container position-fixed bottom-0 end-0 p-3" style="z-index: 1080;">
          <div class="toast show align-items-center text-white bg-primary border-0 rounded-3 shadow-lg" role="alert">
            <div class="d-flex">
              <div class="toast-body d-flex align-items-center gap-2">
                <i class="bi bi-check-circle-fill fs-5"></i>
                <span>{{ toastMessage }}</span>
              </div>
              <button type="button" class="btn-close btn-close-white me-2 m-auto" @click="toastMessage = ''"></button>
            </div>
          </div>
        </div>

        <slot />
      </div>
    </main>

    <!-- Cart Drawer -->
    <CartDrawer @openDeposit="openDepositModal" @orderSuccess="handleOrderSuccess" />

    <!-- Deposit Modal -->
    <DepositModal />

    <!-- Footer -->
    <footer class="mt-auto py-4 bg-white border-top">
      <div class="container text-center text-md-start">
        <div class="row align-items-center">
          <div class="col-md-6 mb-3 mb-md-0">
            <div class="d-flex align-items-center gap-2 justify-content-center justify-content-md-start">
              <img src="/favicon.svg" width="28" height="28" alt="Logo" />
              <span class="fw-bold text-primary">Căn Tin Smart - Canteen Management System</span>
            </div>
            <p class="text-muted small mb-0 mt-1">Phục vụ món ăn nhanh chóng, thơm ngon & đảm bảo vệ sinh an toàn thực phẩm.</p>
          </div>
          <div class="col-md-6 text-center text-md-end text-muted small">
            <div><i class="bi bi-clock me-1"></i> Giờ mở cửa: 06:30 - 18:30 (Thứ 2 - Thứ 7)</div>
            <div>&copy; 2026 Canteen Management System. All rights reserved.</div>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import Navbar from '../components/Navbar.vue';
import CartDrawer from '../components/CartDrawer.vue';
import DepositModal from '../components/DepositModal.vue';
import { useAuthStore } from '../stores/auth';

const authStore = useAuthStore();
const toastMessage = ref('');

onMounted(async () => {
  // Silent check current user session on mount
  if (authStore.user) {
    await authStore.fetchCurrentUser();
  }
});

const openCartDrawer = () => {
  const offcanvasEl = document.getElementById('cartDrawer');
  if (offcanvasEl && window.bootstrap) {
    const bsOffcanvas = window.bootstrap.Offcanvas.getOrCreateInstance(offcanvasEl);
    bsOffcanvas.show();
  }
};

const openDepositModal = () => {
  const modalEl = document.getElementById('depositModal');
  if (modalEl && window.bootstrap) {
    const bsModal = window.bootstrap.Modal.getOrCreateInstance(modalEl);
    bsModal.show();
  }
};

const handleOrderSuccess = (order) => {
  showToast('Đặt món thành công! Đơn hàng của bạn đang chờ chế biến.');
};

const showToast = (msg) => {
  toastMessage.value = msg;
  setTimeout(() => {
    toastMessage.value = '';
  }, 4000);
};
</script>
