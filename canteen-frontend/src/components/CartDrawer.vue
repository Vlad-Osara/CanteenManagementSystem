<template>
  <div 
    class="offcanvas offcanvas-end border-0 shadow-lg" 
    tabindex="-1" 
    id="cartDrawer" 
    aria-labelledby="cartDrawerLabel"
    style="width: 420px; max-width: 90vw;"
  >
    <div class="offcanvas-header bg-light">
      <h5 class="offcanvas-title fw-bold text-primary d-flex align-items-center gap-2" id="cartDrawerLabel">
        <i class="bi bi-cart-check-fill fs-4"></i> Giỏ Hàng Của Bạn
      </h5>
      <button type="button" class="btn-close shadow-none" data-bs-dismiss="offcanvas" aria-label="Close"></button>
    </div>

    <div class="offcanvas-body d-flex flex-column p-4">
      <!-- Empty State -->
      <div v-if="cartStore.items.length === 0" class="my-auto text-center py-5">
        <div class="bg-light d-inline-flex p-4 rounded-circle mb-3">
          <i class="bi bi-basket3 text-muted display-4"></i>
        </div>
        <h6 class="fw-bold text-dark">Giỏ hàng của bạn đang trống</h6>
        <p class="text-muted small">Hãy chọn những món ăn ngon miệng từ thực đơn căn tin nhé!</p>
      </div>

      <!-- Items List -->
      <div v-else class="flex-grow-1 overflow-auto pe-1">
        <div 
          v-for="item in cartStore.items" 
          :key="item.dish.id" 
          class="card border-0 bg-white mb-3 shadow-sm rounded-3 p-3 position-relative"
        >
          <div class="d-flex align-items-center gap-3">
            <img 
              :src="item.dish.imageUrl || '/favicon.svg'" 
              :alt="item.dish.name" 
              class="rounded-3 object-fit-cover bg-light"
              style="width: 65px; height: 65px;"
              @error="(e) => e.target.src = '/favicon.svg'"
            />

            <div class="flex-grow-1">
              <h6 class="fw-bold text-dark mb-1">{{ item.dish.name }}</h6>
              <div class="text-primary fw-bold small">{{ formatVND(item.dish.price) }}</div>
            </div>

            <!-- Quantity controls -->
            <div class="d-flex align-items-center bg-light rounded-pill p-1 gap-2">
              <button 
                class="btn btn-sm btn-white rounded-circle shadow-xs p-0 d-flex align-items-center justify-content-center"
                style="width: 26px; height: 26px;"
                @click="cartStore.updateQuantity(item.dish.id, item.quantity - 1)"
              >
                <i class="bi bi-dash"></i>
              </button>
              <span class="fw-bold small px-1">{{ item.quantity }}</span>
              <button 
                class="btn btn-sm btn-white rounded-circle shadow-xs p-0 d-flex align-items-center justify-content-center"
                style="width: 26px; height: 26px;"
                @click="cartStore.updateQuantity(item.dish.id, item.quantity + 1)"
              >
                <i class="bi bi-plus"></i>
              </button>
            </div>

            <!-- Remove btn -->
            <button 
              class="btn btn-link text-muted p-0 ms-1"
              @click="cartStore.removeFromCart(item.dish.id)"
            >
              <i class="bi bi-trash3 text-danger"></i>
            </button>
          </div>
        </div>
      </div>

      <!-- Checkout Footer -->
      <div v-if="cartStore.items.length > 0" class="pt-3 border-top mt-auto">
        <!-- Order Type Selection -->
        <div class="mb-3">
          <label class="form-label fw-bold small text-muted">Hình thức sử dụng</label>
          <div class="btn-group w-100" role="group">
            <input 
              type="radio" 
              class="btn-check" 
              name="orderType" 
              id="typeDineIn" 
              value="DINE_IN" 
              v-model="cartStore.orderType"
            >
            <label class="btn btn-outline-primary py-2 rounded-start-3" for="typeDineIn">
              <i class="bi bi-cup-hot me-1"></i> Ăn tại chỗ
            </label>

            <input 
              type="radio" 
              class="btn-check" 
              name="orderType" 
              id="typeTakeAway" 
              value="TAKE_AWAY" 
              v-model="cartStore.orderType"
            >
            <label class="btn btn-outline-primary py-2 rounded-end-3" for="typeTakeAway">
              <i class="bi bi-bag-check me-1"></i> Mang về
            </label>
          </div>
        </div>

        <!-- Order Note -->
        <div class="mb-3">
          <input 
            type="text" 
            class="form-control form-control-sm rounded-3" 
            placeholder="Ghi chú cho bếp (vd: Ít cay, không hành...)"
            v-model="cartStore.note"
          />
        </div>

        <!-- Balance Info -->
        <div class="bg-light p-3 rounded-3 mb-3">
          <div class="d-flex justify-content-between align-items-center mb-1">
            <span class="text-muted small">Tổng tiền đơn món:</span>
            <span class="fw-bold fs-5 text-primary">{{ formatVND(cartStore.totalPrice) }}</span>
          </div>

          <div v-if="authStore.user" class="d-flex justify-content-between align-items-center pt-2 border-top">
            <span class="text-muted small">Số dư ví của bạn:</span>
            <span :class="['fw-bold small', isInsufficientBalance ? 'text-danger' : 'text-success']">
              {{ formatVND(authStore.userBalance) }}
            </span>
          </div>
        </div>

        <!-- Warning if balance low -->
        <div v-if="authStore.user && isInsufficientBalance" class="alert alert-warning py-2 px-3 small rounded-3 d-flex align-items-center justify-content-between mb-3">
          <div>
            <i class="bi bi-exclamation-triangle-fill me-1"></i> Số dư ví không đủ!
          </div>
          <button class="btn btn-sm btn-warning text-dark fw-bold rounded-pill" @click="triggerDeposit">
            Nạp tiền ngay
          </button>
        </div>

        <div v-if="fieldErrors.confirmPassword" class="alert alert-danger py-2 small mb-3">
          {{ fieldErrors.confirmPassword }}
        </div>
        <div v-else-if="errorMessage" class="alert alert-danger py-2 small mb-3">
          {{ errorMessage }}
        </div>

        <!-- Confirm Password -->
        <div class="mb-3" v-if="authStore.user">
          <label class="form-label fw-bold small text-muted">
            <i class="bi bi-shield-lock me-1"></i> Mật khẩu xác nhận đặt món (*)
          </label>
          <input 
            type="password" 
            class="form-control form-control-sm rounded-3" 
            placeholder="Nhập mật khẩu của bạn để xác nhận trừ ví"
            v-model="confirmPassword"
            required
          />
        </div>

        <!-- Action Button -->
        <button 
          v-if="!authStore.user" 
          class="btn btn-canteen-primary w-100 py-3 rounded-3 fw-bold"
          @click="goToLogin"
        >
          <i class="bi bi-box-arrow-in-right me-1"></i> Đăng nhập để Đặt Món
        </button>

        <button 
          v-else 
          class="btn btn-canteen-primary w-100 py-3 rounded-3 fw-bold"
          :disabled="cartStore.loading || isInsufficientBalance"
          @click="handleCheckout"
        >
          <span v-if="cartStore.loading" class="spinner-border spinner-border-sm me-2"></span>
          <i v-else class="bi bi-check2-circle me-1"></i> XÁC NHẬN ĐẶT MÓN
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import { useCartStore } from '../stores/cart';

const emit = defineEmits(['openDeposit', 'orderSuccess']);
const authStore = useAuthStore();
const cartStore = useCartStore();
const router = useRouter();

const confirmPassword = ref('');
const errorMessage = ref('');
const fieldErrors = ref({});

const isInsufficientBalance = computed(() => {
  if (!authStore.user) return false;
  return authStore.userBalance < cartStore.totalPrice;
});

// Should use like DishCard.Vue to avoid creating a new formatter on each render, which can be inefficient.
const formatVND = (amount) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount || 0);
};

const goToLogin = () => {
  const offcanvasEl = document.getElementById('cartDrawer');
  const bsOffcanvas = window.bootstrap?.Offcanvas.getInstance(offcanvasEl);
  if (bsOffcanvas) bsOffcanvas.hide();
  router.push('/login');
};

// Emit an event to the parent component to open the deposit modal or page when the user clicks on "Nạp tiền ngay" button.
// Different from emit in HTML
const triggerDeposit = () => {
  emit('openDeposit');
};

const handleCheckout = async () => {
  if (!confirmPassword.value) {
    errorMessage.value = 'Vui lòng nhập mật khẩu để xác nhận đặt món!';
    return;
  }

  errorMessage.value = '';
  try {
    const createdOrder = await cartStore.checkout(confirmPassword.value);
    confirmPassword.value = '';
    
    // Close offcanvas
    const offcanvasEl = document.getElementById('cartDrawer');
    const bsOffcanvas = window.bootstrap?.Offcanvas.getInstance(offcanvasEl);
    if (bsOffcanvas) bsOffcanvas.hide();

    emit('orderSuccess', createdOrder);
    router.push('/orders');
  } catch (err) {
    errorMessage.value = err.message || err.customMessage || 'Không thể tạo đơn hàng!';
    if (err.fieldErrors) {
      fieldErrors.value = err.fieldErrors;
    }
  }
};
</script>
