<template>
  <DefaultLayout>
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between mb-4">
      <div>
        <h3 class="fw-extrabold text-primary mb-1">
          <i class="bi bi-people me-2"></i> Quản Lý Tài Khoản Người Dùng
        </h3>
        <p class="text-muted small mb-0">Quản lý danh sách người dùng, vai trò (Admin, Staff, Customer) và số dư ví</p>
      </div>

      <button class="btn btn-canteen-primary rounded-pill px-4 py-2 fw-semibold" @click="openCreateModal">
        <i class="bi bi-person-plus me-1"></i> Thêm Tài Khoản Mới
      </button>
    </div>

    <!-- Search bar -->
    <div class="mb-4">
      <div class="input-group shadow-sm rounded-pill overflow-hidden bg-white" style="max-width: 400px;">
        <span class="input-group-text bg-white border-0 ps-3 text-muted">
          <i class="bi bi-search"></i>
        </span>
        <input 
          type="text" 
          class="form-control border-0 shadow-none ps-2" 
          placeholder="Tìm tên hoặc username..."
          v-model="searchQuery"
        />
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status"></div>
      <p class="text-muted mt-2">Đang tải danh sách người dùng hệ thống...</p>
    </div>

    <!-- Table -->
    <div v-else class="card card-canteen border-0 p-0 overflow-hidden shadow-sm">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="bg-light">
            <tr>
              <th class="ps-4">Họ và tên / Username</th>
              <th>Liên hệ</th>
              <th>Vai trò</th>
              <th>Số dư ví</th>
              <th class="text-center">Trạng thái</th>
              <th class="text-end pe-4">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in filteredUsers" :key="user.id">
              <td class="ps-4">
                <div class="fw-bold text-dark">{{ user.fullName || 'Chưa đặt tên' }}</div>
                <div class="text-muted small">@{{ user.username || 'user' }}</div>
              </td>
              <td>
                <div class="small text-dark">{{ user.email || '---' }}</div>
                <div class="small text-muted">{{ user.phoneNumber || '---' }}</div>
              </td>
              <td>
                <span :class="['badge rounded-pill px-3 py-2', getRoleBadge(user.role)]">
                  {{ getRoleLabel(user.role) }}
                </span>
              </td>
              <td class="fw-extrabold text-primary">{{ formatVND(user.balance) }}</td>
              <td>
                <span :class="['badge rounded-pill px-3 py-2', user.isActive !== false ? 'bg-success text-white' : 'bg-danger text-white']">
                  <i :class="[user.isActive !== false ? 'bi bi-check-circle-fill' : 'bi bi-lock-fill', 'me-1']"></i>
                  {{ user.isActive !== false ? 'Đang hoạt động' : 'Đã bị khóa' }}
                </span>
              </td>
              <td class="text-end pe-4">
                <button 
                  v-if="user.role !== 'ROLE_ADMIN' && user.role !== 'ADMIN' || user.id === authStore.user?.id" 
                  class="btn btn-sm btn-light text-primary me-2 rounded-2" 
                  @click="openEditModal(user)"
                  title="Chỉnh sửa tài khoản">
                  <i class="bi bi-pencil-square me-1"></i> Sửa
                </button>
                <span v-else class="badge bg-light text-secondary border py-2 px-2" title="Tài khoản Quản trị được bảo vệ">
                  <i class="bi bi-shield-shaded text-danger"></i> Bảo vệ
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Pagination -->
    <Pagination 
    :current-page="currentPage" 
    :total-pages="totalPages" 
    :total-elements="totalElements" 
    :current-count="users.length"
    item-label="tài khoản"
    @change-page="handlePageChange"
    />

    <!-- Modal Form -->
    <div class="modal fade" id="userModal" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header">
            <h5 class="modal-title fw-bold text-primary">
              <i class="bi bi-person-gear me-2"></i> {{ isEdit ? 'Cập Nhật Tài Khoản' : 'Thêm Tài Khoản Mới' }}
            </h5>
            <button type="button" class="btn-close shadow-none" data-bs-dismiss="modal"></button>
          </div>

          <form @submit.prevent="saveUser">
            <div class="modal-body p-4">
              <div class="mb-3" v-if="!isEdit">
                <label class="form-label fw-bold small text-muted">Tên tài khoản (Username) (*)</label>
                <input 
                  type="text" 
                  class="form-control" 
                  v-model="form.username"
                  required
                />
                <div v-if="fieldErrors?.username" class="invalid-feedback d-block small mt-1">
                  <i class="bi bi-exclamation-circle me-1"></i> {{ fieldErrors.username }}
                </div>
              </div>

              <div class="mb-3">
                <label class="form-label fw-bold small text-muted">Họ và tên (*)</label>
                <input 
                  type="text" 
                  class="form-control" 
                  v-model="form.fullName"
                  required
                />
                <div v-if="fieldErrors?.fullName" class="invalid-feedback d-block small mt-1">
                  <i class="bi bi-exclamation-circle me-1"></i> {{ fieldErrors.fullName }}
                </div>
              </div>

              <div class="row g-2 mb-3">
                <div class="col-6">
                  <label class="form-label fw-bold small text-muted">Email</label>
                  <input type="email" class="form-control" v-model="form.email" />
                  <div v-if="fieldErrors?.email" class="invalid-feedback d-block small mt-1">
                    <i class="bi bi-exclamation-circle me-1"></i> {{ fieldErrors.email }}
                  </div>
                </div>
                <div class="col-6">
                  <label class="form-label fw-bold small text-muted">Số điện thoại</label>
                  <input type="tel" class="form-control" v-model="form.phoneNumber" />
                  <div v-if="fieldErrors?.phoneNumber" class="invalid-feedback d-block small mt-1">
                    <i class="bi bi-exclamation-circle me-1"></i> {{ fieldErrors.phoneNumber }}
                  </div>
                </div>
              </div>

              <div class="row g-2 mb-3">
                <div class="col-6">
                  <label class="form-label fw-bold small text-muted">Vai trò (*)</label>
                  <select class="form-select" v-model="form.role" required>
                    <option value="CUSTOMER">Khách Hàng</option>
                    <option value="STAFF">Nhân Viên Bếp</option>
                    <option value="ADMIN">Quản Trị Viên</option>
                  </select>
                </div>
                <div class="col-6">
                  <label class="form-label fw-bold small text-muted">Số dư ví (VNĐ)</label>
                  <input type="number" class="form-control" min="0" step="1000" v-model.number="form.balance" />
                </div>
              </div>

              <div class="mb-3">
                <label class="form-label fw-bold small text-muted">
                  {{ isEdit ? 'Mật khẩu mới (Bỏ trống nếu không đổi)' : 'Mật khẩu (*)' }}
                </label>
                <input 
                  type="password" 
                  class="form-control" 
                  v-model="form.password"
                  :required="!isEdit"
                />
                <div v-if="fieldErrors?.password" class="invalid-feedback d-block small mt-1">
                  <i class="bi bi-exclamation-circle me-1"></i> {{ fieldErrors.password }}
                </div>
              </div>

              <div class="mb-3 p-3 rounded-3 border" :class="form.isActive ? 'bg-light' : 'bg-danger bg-opacity-10 border-danger'" v-if="isEdit">
                <div class="form-check form-switch d-flex justify-content-between align-items-center ps-0">
                  <label class="form-check-label fw-bold mb-0 cursor-pointer" for="activeSwitch">
                    <i :class="[form.isActive ? 'bi bi-check-circle-fill text-success' : 'bi bi-lock-fill text-danger', 'me-1 fs-5']"></i>
                    Trạng thái tài khoản: 
                    <span :class="form.isActive ? 'text-success' : 'text-danger'">
                      {{ form.isActive ? 'Đang hoạt động (Cho phép đăng nhập)' : 'Đang bị khóa (Chặn đăng nhập)' }}
                    </span>
                  </label>
                  <input 
                    class="form-check-input fs-4 cursor-pointer ms-auto" 
                    type="checkbox" 
                    role="switch" 
                    id="activeSwitch"
                    v-model="form.isActive"
                  />
                </div>
              </div>
              <!-- Admin's Confirmation Password -->
              <div class="mb-3 pt-3 border-top" v-if="isEdit">
                <label class="form-label fw-bold small text-danger">
                  <i class="bi bi-shield-lock-fill me-1"></i> Mật khẩu Admin của bạn (*) <span class="fw-normal text-muted">(Bắt buộc để xác nhận)</span>
                </label>
                <input 
                  type="password" 
                  class="form-control border-danger" 
                  placeholder="Nhập mật khẩu tài khoản Admin bạn đang đăng nhập"
                  v-model="form.confirmPassword"
                  required
                />
                <div v-if="fieldErrors?.confirmPassword" class="invalid-feedback d-block small mt-1">
                  <i class="bi bi-exclamation-circle me-1"></i> {{ fieldErrors.confirmPassword }}
                </div>
              </div>
            </div>

            <div class="modal-footer bg-light">
              <button type="button" class="btn btn-light rounded-pill px-4" data-bs-dismiss="modal">Hủy</button>
              <button type="submit" class="btn btn-canteen-primary rounded-pill px-4" :disabled="saving">
                <span v-if="saving" class="spinner-border spinner-border-sm me-1"></span>
                Lưu Tài Khoản
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
import DefaultLayout from '../../layouts/DefaultLayout.vue';
import Pagination from '../../components/Pagination.vue';
import { useAuthStore } from '../../stores/auth';
import api from '../../api/axios';

const authStore = useAuthStore();

const users = ref([]);
const loading = ref(true);
const saving = ref(false);
const isEdit = ref(false);
const editingId = ref(null);
const searchQuery = ref('');
const errorMsg = ref('');
const fieldErrors = ref({}); // Validation errors for each field

// Pagination state
const currentPage = ref(1);
const limit = ref(10);
const totalPages = ref(1);
const totalElements = ref(0);

const form = reactive({
  username: '',
  fullName: '',
  email: '',
  phoneNumber: '',
  role: 'CUSTOMER',
  balance: 0,
  password: '',
  confirmPassword: '',
  isActive: true
});

onMounted(() => {
  fetchUsers();
});

const fetchUsers = async () => {
  loading.value = true;
  try {
    const res = await api.get('/api/admin/account', {
      params: {
        page: currentPage.value,
        limit: limit.value
      }
    });
    const data = res.data.data;
    if (data && data.content) {
      users.value = data.content;
      totalPages.value = data.totalPages || 1;
      totalElements.value = data.totalElements || 0;
    } else {
      users.value = Array.isArray(data) ? data : [];
    }
  } catch (e) {
    console.error('Error fetching admin accounts:', e);
  } finally {
    loading.value = false;
  }
};
const handlePageChange = (page) => {
  currentPage.value = page;
  fetchUsers();
};

const filteredUsers = computed(() => {
  if (!searchQuery.value) return users.value;
  const q = searchQuery.value.toLowerCase();
  return users.value.filter(u => 
    (u.fullName && u.fullName.toLowerCase().includes(q)) || 
    (u.username && u.username.toLowerCase().includes(q))
  );
});

const openCreateModal = () => {
  isEdit.value = false;
  editingId.value = null;
  form.username = '';
  form.fullName = '';
  form.email = '';
  form.phoneNumber = '';
  form.role = 'CUSTOMER';
  form.balance = 0;
  form.password = '';
  form.confirmPassword = '';
  errorMsg.value = '';
  showModal();
};

const openEditModal = (u) => {
  isEdit.value = true;
  editingId.value = u.id;
  form.username = u.username;
  form.fullName = u.fullName || '';
  form.email = u.email || '';
  form.phoneNumber = u.phoneNumber || '';
  form.role = u.role ? u.role.replace('ROLE_', '') : 'CUSTOMER';
  form.balance = u.balance || 0;
  form.password = '';
  form.confirmPassword = '';
  form.isActive = u.isActive !== undefined ? u.isActive : true;
  errorMsg.value = '';
  showModal();
};

const saveUser = async () => {
  saving.value = true;
  errorMsg.value = '';
  fieldErrors.value = {};
  try {
    if (isEdit.value) {
      const payload = {
        fullName: form.fullName?.trim() || undefined,
        email: form.email?.trim() || undefined,
        phoneNumber: form.phoneNumber?.trim() || undefined,
        role: form.role,
        balance: form.balance,
        isActive: form.isActive,
        confirmPassword: form.confirmPassword
      };
      if (form.password && form.password.trim() !== '') {
        payload.newPassword = form.password.trim();
      }
      await api.put(`/api/admin/account/${editingId.value}`, payload);
    } else {
      await api.post('/api/admin/account', form);
    }
    hideModal();
    await fetchUsers();
  } catch (e) {
    errorMsg.value = e.customMessage || 'Lỗi khi lưu thông tin người dùng!';
    if (e.fieldErrors) {
      fieldErrors.value = e.fieldErrors;
    }
  } finally {
    saving.value = false;
  }
};

// const deleteUser = async (id) => {
//   const adminPassword = prompt('XÁC THỰC BẢO MẬT:\nVui lòng nhập mật khẩu tài khoản Quản trị viên của bạn để xác nhận xóa vĩnh viễn:');
//   if (!adminPassword) return;

//   try {
//     // Gửi mật khẩu qua Header an toàn
//     await api.delete(`/api/admin/account/${id}`, {
//       headers: {
//         'X-Confirm-Password': adminPassword  // Gửi mật khẩu xác thực qua header để xác nhận quyền xóa
//       }
//     });
//     alert('Xóa tài khoản thành công!');
//     await fetchUsers();
//   } catch (e) {
//     alert(e.customMessage || 'Mật khẩu xác thực không đúng hoặc không thể xóa tài khoản!');
//   }
// };

const showModal = () => {
  const el = document.getElementById('userModal');
  if (el && window.bootstrap) {
    const modal = window.bootstrap.Modal.getOrCreateInstance(el);
    modal.show();
  }
};

const hideModal = () => {
  const el = document.getElementById('userModal');
  if (el && window.bootstrap) {
    const modal = window.bootstrap.Modal.getInstance(el);
    if (modal) modal.hide();
  }
};

const getRoleBadge = (role) => {
  if (role === 'ROLE_ADMIN' || role === 'ADMIN') return 'bg-danger text-white';
  if (role === 'ROLE_STAFF' || role === 'STAFF') return 'bg-warning text-dark';
  return 'bg-primary text-white';
};

const getRoleLabel = (role) => {
  if (role === 'ROLE_ADMIN' || role === 'ADMIN') return 'Quản Trị Viên';
  if (role === 'ROLE_STAFF' || role === 'STAFF') return 'Nhân Viên Bếp';
  return 'Khách Hàng';
};

const formatVND = (amount) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount || 0);
};
</script>
