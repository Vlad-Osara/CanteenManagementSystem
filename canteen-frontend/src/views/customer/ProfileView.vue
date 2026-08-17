<template>
  <DefaultLayout>
    <div class="row justify-content-center">
      <div class="col-md-8 col-lg-6">
        <div class="card card-canteen border-0 p-4 p-md-5">
          <!-- Profile Header -->
          <div class="text-center mb-4">
            <div class="bg-primary text-white d-inline-flex align-items-center justify-content-center rounded-circle mb-3 shadow" style="width: 80px; height: 80px;">
              <i class="bi bi-person-fill display-5"></i>
            </div>
            <h4 class="fw-extrabold text-primary mb-1">{{ authStore.user?.fullName || authStore.user?.username }}</h4>
            <div class="badge bg-light text-primary border rounded-pill px-3 py-1">{{ roleLabel }}</div>
          </div>

          <!-- Alert thông báo thành công -->
          <div v-if="successMsg" class="alert alert-success py-2 px-3 rounded-3 small mb-4 d-flex align-items-center justify-content-between">
            <div><i class="bi bi-check-circle-fill me-1"></i> {{ successMsg }}</div>
            <button type="button" class="btn-close shadow-none" @click="successMsg = ''"></button>
          </div>

          <!-- VNPay Deposit Return Alert Banner -->
          <div v-if="depositAlert && authStore.isCustomer" :class="['alert py-3 px-4 rounded-3 mb-4 d-flex align-items-center justify-content-between', depositAlert.isSuccess ? 'alert-success border-success' : 'alert-danger border-danger']">
            <div class="d-flex align-items-center gap-2">
              <i :class="[depositAlert.isSuccess ? 'bi bi-check-circle-fill text-success fs-4' : 'bi bi-exclamation-octagon-fill text-danger fs-4']"></i>
              <div>
                <div class="fw-bold">{{ depositAlert.title }}</div>
                <div class="small">{{ depositAlert.message }}</div>
              </div>
            </div>
            <button type="button" class="btn-close shadow-none" @click="depositAlert = null"></button>
          </div>

          <!-- Balance Wallet Card -->
          <div v-if="!authStore.isStaff" class="canteen-banner p-4 rounded-4 mb-4 text-center border">
            <div class="text-muted small mb-1">Ví điện tử Căn tin</div>
            <div class="display-6 fw-extrabold text-primary mb-3">{{ formatVND(authStore.userBalance) }}</div>
            <button class="btn btn-canteen-primary rounded-pill px-4" @click="openDepositModal">
              <i class="bi bi-plus-circle me-1"></i> Nạp tiền qua VNPay
            </button>
          </div>

          <!-- Account Details -->
          <div class="bg-light p-3 rounded-3 mb-4">
            <div class="d-flex justify-content-between align-items-center mb-3 pb-2 border-bottom">
              <span class="fw-bold text-dark"><i class="bi bi-info-circle me-1 text-primary"></i> Thông Tin Tài Khoản</span>
              <!-- Nút mở Modal Chỉnh sửa -->
              <button class="btn btn-sm btn-outline-primary rounded-pill px-3" @click="openEditProfileModal">
                <i class="bi bi-pencil-square me-1"></i> Sửa thông tin
              </button>
            </div>

            <div class="row g-3">
              <div class="col-6">
                <div class="text-muted small">Họ tên:</div>
                <div class="fw-bold text-dark">{{ authStore.user?.fullName }}</div>
              </div>
              <div class="col-6">
                <div class="text-muted small">Số điện thoại:</div>
                <div class="fw-bold text-dark">{{ authStore.user?.phoneNumber || 'Chưa cập nhật' }}</div>
              </div>
              <div class="col-12">
                <div class="text-muted small">Email:</div>
                <div class="fw-bold text-dark">{{ authStore.user?.email || 'Chưa cập nhật' }}</div>
              </div>
            </div>
          </div>

          <!-- Action buttons -->
                    <!-- Action buttons -->
          <div class="d-flex gap-2">
            <!-- Khách hàng: Xem đơn hàng của tôi -->
            <router-link v-if="authStore.isCustomer" to="/orders" class="btn btn-outline-primary rounded-3 flex-grow-1 py-2 fw-semibold">
              <i class="bi bi-receipt me-1"></i> Đơn hàng của tôi
            </router-link>

            <!-- Quản trị viên: Chuyển nhanh đến trang Dashboard -->
            <router-link v-else-if="authStore.isAdmin" to="/admin" class="btn btn-outline-primary rounded-3 flex-grow-1 py-2 fw-semibold">
              <i class="bi bi-speedometer2 me-1"></i> Trang Quản Trị
            </router-link>

            <!-- Nhân viên Bếp: Chuyển nhanh đến trang xử lý đơn -->
            <router-link v-else-if="authStore.isStaff" to="/staff/orders" class="btn btn-outline-primary rounded-3 flex-grow-1 py-2 fw-semibold">
              <i class="bi bi-hourglass-split me-1"></i> Bếp & Đơn Đang Xử Lý
            </router-link>

            <button class="btn btn-outline-danger rounded-3 py-2 fw-semibold" @click="handleLogout">
              <i class="bi bi-box-arrow-right me-1"></i> Đăng xuất
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- MODAL SỬA THÔNG TIN CÁ NHÂN -->
    <div class="modal fade" id="editProfileModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header">
            <h5 class="modal-title fw-bold text-primary">
              <i class="bi bi-person-gear me-2"></i> Cập Nhật Thông Tin Cá Nhân
            </h5>
            <button type="button" class="btn-close shadow-none" data-bs-dismiss="modal"></button>
          </div>

          <form @submit.prevent="handleUpdateProfile">
            <div class="modal-body p-4">
              <!-- Họ và tên (Tùy chọn) -->
              <div class="mb-3">
                <label class="form-label fw-bold small text-muted">Họ và tên</label>
                <input 
                  type="text" 
                  class="form-control rounded-3" 
                  placeholder="Nhập họ và tên mới"
                  v-model="profileForm.fullName"
                />
                <div v-if="fieldErrors?.fullName" class="invalid-feedback d-block small mt-1">
                  <i class="bi bi-exclamation-circle me-1"></i> {{ fieldErrors.fullName }}
                </div>
              </div>

              <!-- Email & SĐT (Tùy chọn) -->
              <div class="row g-2 mb-3">
                <div class="col-6">
                  <label class="form-label fw-bold small text-muted">Email</label>
                  <input 
                    type="email" 
                    class="form-control rounded-3" 
                    placeholder="email@example.com"
                    v-model="profileForm.email"
                  />
                  <div v-if="fieldErrors?.email" class="invalid-feedback d-block small mt-1">
                    <i class="bi bi-exclamation-circle me-1"></i> {{ fieldErrors.email }}
                  </div>
                </div>
                <div class="col-6">
                  <label class="form-label fw-bold small text-muted">Số điện thoại</label>
                  <input 
                    type="tel" 
                    class="form-control rounded-3" 
                    placeholder="0912345678"
                    v-model="profileForm.phoneNumber"
                  />
                  <div v-if="fieldErrors?.phoneNumber" class="invalid-feedback d-block small mt-1">
                    <i class="bi bi-exclamation-circle me-1"></i> {{ fieldErrors.phoneNumber }}
                  </div>
                </div>
              </div>

              <!-- Mật khẩu mới (Tùy chọn) -->
              <div class="mb-3">
                <label class="form-label fw-bold small text-muted">
                  Mật khẩu mới <span class="text-muted fw-normal">(Bỏ trống nếu không muốn đổi)</span>
                </label>
                <input 
                  type="password" 
                  class="form-control rounded-3" 
                  placeholder="Tối thiểu 8 ký tự gồm chữ và số"
                  v-model="profileForm.newPassword"
                />
                <div v-if="fieldErrors?.newPassword" class="invalid-feedback d-block small mt-1">
                  <i class="bi bi-exclamation-circle me-1"></i> {{ fieldErrors.newPassword }}
                </div>
              </div>

              <!-- MẬT KHẨU HIỆN TẠI (BẮT BUỘC ĐỂ XÁC THỰC & CHỐNG CSRF) -->
              <div class="mb-3 pt-3 border-top">
                <label class="form-label fw-bold small text-danger">
                  <i class="bi bi-shield-lock-fill me-1"></i> Mật khẩu hiện tại (*) <span class="fw-normal text-muted">(Bắt buộc)</span>
                </label>
                <input 
                  type="password" 
                  class="form-control border-danger rounded-3" 
                  placeholder="Nhập mật khẩu hiện tại của bạn"
                  v-model="profileForm.confirmPassword"
                  required
                />
                <div v-if="fieldErrors?.confirmPassword" class="invalid-feedback d-block small mt-1">
                  <i class="bi bi-exclamation-circle me-1"></i> {{ fieldErrors.confirmPassword }}
                </div>
              </div>

              <div v-if="errorMsg" class="alert alert-danger py-2 small mb-0">
                {{ errorMsg }}
              </div>
            </div>

            <div class="modal-footer bg-light">
              <button type="button" class="btn btn-light rounded-pill px-4" data-bs-dismiss="modal">Hủy</button>
              <button type="submit" class="btn btn-canteen-primary rounded-pill px-4" :disabled="saving">
                <span v-if="saving" class="spinner-border spinner-border-sm me-1"></span>
                Lưu Thay Đổi
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </DefaultLayout>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import DefaultLayout from '../../layouts/DefaultLayout.vue';
import { useAuthStore } from '../../stores/auth';
import api from '../../api/axios';

const authStore = useAuthStore();
const router = useRouter();
const route = useRoute();

const depositAlert = ref(null);
const successMsg = ref('');
const errorMsg = ref('');
const fieldErrors = ref({});
const saving = ref(false);

const profileForm = reactive({
  fullName: '',
  email: '',
  phoneNumber: '',
  newPassword: '',
  confirmPassword: ''
});

onMounted(async () => {
  if (route.query.deposit === 'success') {
    depositAlert.value = {
      isSuccess: true,
      title: 'Nạp Tiền Thành Công!',
      message: route.query.amount 
        ? `Đã cộng ${formatVND(Number(route.query.amount))} vào ví điện tử căn tin của bạn.`
        : 'Số dư mới đã được tự động cập nhật vào tài khoản.'
    };
    await authStore.fetchCurrentUser();
  } else if (route.query.deposit === 'failed') {
    depositAlert.value = {
      isSuccess: false,
      title: 'Giao Dịch Không Thành Công',
      message: 'Giao dịch nạp tiền qua VNPay đã bị hủy hoặc gặp sự cố.'
    };
  }
});

const roleLabel = computed(() => {
  const role = authStore.userRole;
  if (role === 'ROLE_ADMIN' || role === 'ADMIN') return 'Quản Trị Viên';
  if (role === 'ROLE_STAFF' || role === 'STAFF') return 'Nhân Viên Bếp';
  return 'Khách Hàng';
});

const openEditProfileModal = () => {
  profileForm.fullName = authStore.user?.fullName || '';
  profileForm.email = authStore.user?.email || '';
  profileForm.phoneNumber = authStore.user?.phoneNumber || '';
  profileForm.newPassword = '';
  profileForm.confirmPassword = '';
  errorMsg.value = '';
  fieldErrors.value = {};

  const modalEl = document.getElementById('editProfileModal');
  if (modalEl && window.bootstrap) {
    const modal = window.bootstrap.Modal.getOrCreateInstance(modalEl);
    modal.show();
  }
};

const handleUpdateProfile = async () => {
  saving.value = true;
  errorMsg.value = '';
  fieldErrors.value = {};
  try {
    const payload = {
      fullName: profileForm.fullName?.trim() || undefined,
      email: profileForm.email?.trim() || undefined,
      phoneNumber: profileForm.phoneNumber?.trim() || undefined,
      newPassword: profileForm.newPassword?.trim() || undefined,
      confirmPassword: profileForm.confirmPassword
    };

    const res = await api.put('/api/customer/profile', payload);
    const updatedUser = res.data.data;
    
    authStore.user = updatedUser;
    localStorage.setItem('canteen_user', JSON.stringify(updatedUser));

    const modalEl = document.getElementById('editProfileModal');
    if (modalEl && window.bootstrap) {
      const modal = window.bootstrap.Modal.getInstance(modalEl);
      if (modal) modal.hide();
    }

    successMsg.value = 'Thông tin cá nhân của bạn đã được cập nhật thành công!';
  } catch (e) {
    errorMsg.value = e.customMessage || 'Lỗi khi cập nhật thông tin cá nhân!';
    if (e.fieldErrors) {
      fieldErrors.value = e.fieldErrors;
    }
  } finally {
    saving.value = false;
  }
};

const openDepositModal = () => {
  const modalEl = document.getElementById('depositModal');
  if (modalEl && window.bootstrap) {
    const bsModal = window.bootstrap.Modal.getOrCreateInstance(modalEl);
    bsModal.show();
  }
};

const formatVND = (amount) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount || 0);
};

const handleLogout = async () => {
  await authStore.logout();
  router.push('/login');
};
</script>