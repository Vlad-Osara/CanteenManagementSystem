<template>
  <div 
    class="modal fade" 
    id="depositModal" 
    tabindex="-1" 
    aria-labelledby="depositModalLabel" 
    aria-hidden="true"
  >
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content border-0 shadow-lg">
        <div class="modal-header">
          <h5 class="modal-title fw-bold text-primary d-flex align-items-center gap-2" id="depositModalLabel">
            <i class="bi bi-wallet-fill text-primary"></i> Nạp Tiền Vào Ví Căn Tin
          </h5>
          <button type="button" class="btn-close shadow-none" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>

        <div class="modal-body p-4">
          <!-- Balance Summary Card -->
          <div class="canteen-banner p-3 mb-4 rounded-3 text-center">
            <div class="text-muted small">Số dư ví hiện tại:</div>
            <div class="fs-3 fw-extrabold text-primary">{{ formatVND(authStore.userBalance) }}</div>
          </div>

          <!-- Deposit Amounts options -->
          <label class="form-label fw-bold small text-muted">Chọn số tiền nạp nhanh</label>
          <div class="row g-2 mb-3">
            <div v-for="amount in presetAmounts" :key="amount" class="col-6 col-md-4">
              <button 
                class="btn w-100 py-2 rounded-3 fw-semibold transition-all"
                :class="selectedAmount === amount ? 'btn-primary' : 'btn-outline-primary'"
                @click="selectedAmount = amount"
              >
                {{ formatVND(amount) }}
              </button>
            </div>
          </div>

          <!-- Custom Amount Input -->
          <div class="mb-4">
            <label class="form-label fw-bold small text-muted">Hoặc nhập số tiền khác (VNĐ)</label>
            <div class="input-group">
              <span class="input-group-text bg-light border-end-0"><i class="bi bi-cash-stack"></i></span>
              <input 
                type="number" 
                class="form-control form-control-lg border-start-0" 
                placeholder="Ví dụ: 100000"
                min="10000"
                step="10000"
                v-model.number="selectedAmount"
              />
            </div>
          </div>

          <!-- Error Alert -->
          <div v-if="errorMsg" class="alert alert-danger py-2 small mb-3">
            <i class="bi bi-exclamation-triangle-fill me-1"></i> {{ errorMsg }}
          </div>

          <!-- Payment Method info -->
          <div class="d-flex align-items-center gap-3 p-3 bg-light rounded-3 mb-4">
            <i class="bi bi-qr-code-scan fs-2 text-primary"></i>
            <div>
              <div class="fw-bold text-dark small">Thanh toán qua cổng VNPay</div>
              <div class="text-muted small">Hỗ trợ quét mã QR Banking, Ví MoMo, VNPay-QR...</div>
            </div>
          </div>

          <!-- Payment QR / Redirect result area -->
          <div v-if="paymentUrl" class="text-center p-3 bg-white border border-primary-subtle rounded-3 mb-3">
            <div class="text-success fw-bold mb-2">
              <i class="bi bi-check-circle-fill me-1"></i> Đã tạo liên kết thanh toán!
            </div>
            <a :href="paymentUrl" target="_self" class="btn btn-sm btn-success rounded-pill px-4 mb-2">
              <i class="bi bi-box-arrow-up-right me-1"></i> Mở trang thanh toán VNPay
            </a>
            <div class="text-muted small">Sau khi hoàn tất thanh toán trên VNPay, số dư sẽ tự động được cập nhật.</div>
          </div>

          <button 
            v-else 
            class="btn btn-canteen-primary w-100 py-3 rounded-3 fw-bold fs-6"
            :disabled="loading || selectedAmount < 10000"
            @click="handleDeposit"
          >
            <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
            <i v-else class="bi bi-credit-card me-1"></i> TẠO YÊU CẦU NẠP {{ formatVND(selectedAmount) }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useAuthStore } from '../stores/auth';
import api from '../api/axios';

const authStore = useAuthStore();
const presetAmounts = [20000, 50000, 100000, 200000, 500000, 1000000];
const selectedAmount = ref(50000);
const loading = ref(false);
const errorMsg = ref('');
const paymentUrl = ref('');

const formatVND = (amount) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount || 0);
};

const handleDeposit = async () => {
  if (selectedAmount.value < 10000) {
    errorMsg.value = 'Số tiền nạp tối thiểu là 10.000 VNĐ';
    return;
  }

  loading.value = true;
  errorMsg.value = '';
  paymentUrl.value = '';

  try {
    const response = await api.post(`/api/customer/deposit?amount=${selectedAmount.value}`);
    if (response.data && response.data.paymentUrl) {
      paymentUrl.value = response.data.paymentUrl;
    } else {
      errorMsg.value = 'Không lấy được liên kết VNPay. Vui lòng thử lại!';
    }
  } catch (err) {
    errorMsg.value = err.customMessage || 'Lỗi tạo giao dịch nạp tiền!';
  } finally {
    loading.value = false;
  }
};
</script>
