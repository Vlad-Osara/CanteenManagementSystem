<template>
  <DefaultLayout>
    <!-- Header -->
    <div class="d-flex flex-wrap align-items-center justify-content-between mb-4 gap-2">
      <div>
        <h3 class="fw-extrabold text-primary mb-1">
          <i class="bi bi-headset me-2"></i> Màn Hình Quản Lý Bếp & Đơn Xử Lý
        </h3>
        <p class="text-muted small mb-0">Tiếp nhận, chế biến và cập nhật trạng thái đơn hàng thời gian thực</p>
      </div>

      <div class="d-flex gap-2">
        <router-link to="/staff/dishes" class="btn btn-outline-primary rounded-pill px-3 py-2 fw-semibold">
          <i class="bi bi-toggle-on me-1"></i> Bật/Tắt Hết Món
        </router-link>
        <button class="btn btn-primary rounded-pill px-3 py-2 fw-semibold shadow-sm" @click="fetchActiveOrders">
          <i class="bi bi-arrow-clockwise me-1"></i> Cập nhật danh sách
        </button>
      </div>
    </div>

    <!-- Stats Bar -->
    <div class="row g-3 mb-4">
      <div class="col-md-4">
        <div class="card card-canteen border-0 p-3 bg-warning bg-opacity-10 border-warning">
          <div class="d-flex align-items-center gap-3">
            <div class="p-3 bg-warning text-white rounded-3 fs-3">
              <i class="bi bi-hourglass-split"></i>
            </div>
            <div>
              <div class="fs-4 fw-extrabold text-dark">{{ pendingCount }}</div>
              <div class="text-muted small fw-semibold">Đơn chờ tiếp nhận</div>
            </div>
          </div>
        </div>
      </div>

      <div class="col-md-4">
        <div class="card card-canteen border-0 p-3 bg-info bg-opacity-10 border-info">
          <div class="d-flex align-items-center gap-3">
            <div class="p-3 bg-info text-white rounded-3 fs-3">
              <i class="bi bi-fire"></i>
            </div>
            <div>
              <div class="fs-4 fw-extrabold text-dark">{{ preparingCount }}</div>
              <div class="text-muted small fw-semibold">Đơn đang chế biến</div>
            </div>
          </div>
        </div>
      </div>

      <div class="col-md-4">
        <div class="card card-canteen border-0 p-3 bg-primary bg-opacity-10 border-primary">
          <div class="d-flex align-items-center gap-3">
            <div class="p-3 bg-primary text-white rounded-3 fs-3">
              <i class="bi bi-bell-fill"></i>
            </div>
            <div>
              <div class="fs-4 fw-extrabold text-dark">{{ readyCount }}</div>
              <div class="text-muted small fw-semibold">Đơn sẵn sàng phục vụ</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status"></div>
      <p class="text-muted mt-2">Đang tải danh sách đơn bếp...</p>
    </div>

    <!-- Empty -->
    <div v-else-if="orders.length === 0" class="card card-canteen p-5 text-center my-4">
      <div class="bg-light d-inline-flex p-4 rounded-circle mx-auto mb-3">
        <i class="bi bi-check2-all text-success display-4"></i>
      </div>
      <h5 class="fw-bold text-dark">Hiện tại không có đơn nào cần xử lý</h5>
      <p class="text-muted small">Tuyệt vời! Bếp đã xử lý xong toàn bộ đơn hàng hiện tại.</p>
    </div>

    <!-- Orders Grid -->
    <div v-else class="row g-4">
      <div v-for="order in orders" :key="order.id" class="col-md-6 col-lg-4">
        <div class="card card-canteen h-100 border-0 p-4 d-flex flex-column shadow-sm">
          <!-- Header -->
          <div class="d-flex justify-content-between align-items-start mb-3 pb-2 border-bottom">
            <div>
              <h6 class="fw-bold text-dark mb-0">Mã đơn: #{{ order.id ? order.id.substring(0, 8) : 'N/A' }}</h6>
              <span class="text-muted small"><i class="bi bi-clock me-1"></i> {{ formatDate(order.createdAt) }}</span>
            </div>
            <span :class="['badge rounded-pill px-3 py-2 fw-bold', getStatusBadge(order.status)]">
              {{ getStatusLabel(order.status) }}
            </span>
          </div>

          <!-- Type & Note -->
          <div class="d-flex justify-content-between align-items-center mb-3">
            <span class="badge bg-light text-primary border rounded-pill px-3 py-1 fw-semibold">
              <i :class="[order.type === 'DINE_IN' ? 'bi bi-cup-hot' : 'bi bi-bag-check', 'me-1']"></i>
              {{ order.type === 'DINE_IN' ? 'Ăn tại chỗ' : 'Mang về' }}
            </span>
            <span class="fw-extrabold text-primary fs-5">{{ formatVND(order.totalPrice) }}</span>
          </div>

          <div v-if="order.note" class="alert alert-warning py-2 small mb-3">
            <i class="bi bi-exclamation-triangle-fill me-1"></i> Ghi chú: {{ order.note }}
          </div>

          <!-- Items list -->
          <div class="bg-light p-3 rounded-3 mb-4 flex-grow-1">
            <div class="fw-bold small text-muted mb-2">Chi tiết món:</div>
            <ul class="list-unstyled mb-0">
              <li v-for="item in (order.orderItemDTO || [])" :key="item.dishId" class="d-flex justify-content-between py-1 border-bottom border-white">
                <span class="fw-bold text-dark"><span class="text-primary me-2">{{ item.quantity }}x</span> {{ item.dishName }}</span>
                <span class="text-muted small">{{ formatVND(item.price * item.quantity) }}</span>
              </li>
            </ul>
          </div>

          <!-- Action buttons for staff -->
          <div class="mt-auto d-grid gap-2">
            <button 
              v-if="order.status === 'PENDING'" 
              class="btn btn-warning text-dark fw-bold rounded-3 py-2"
              @click="updateStatus(order.id, 'PREPARING')"
            >
              <i class="bi bi-fire me-1"></i> Nhận Đơn & Chế Biến
            </button>

            <button 
              v-if="order.status === 'PREPARING'" 
              class="btn btn-info text-white fw-bold rounded-3 py-2"
              @click="updateStatus(order.id, 'READY')"
            >
              <i class="bi bi-bell-fill me-1"></i> Đã Xong - Mời Khách Nhận Món
            </button>

            <button 
              v-if="order.status === 'READY'" 
              class="btn btn-success text-white fw-bold rounded-3 py-2"
              @click="updateStatus(order.id, 'COMPLETED')"
            >
              <i class="bi bi-check-lg me-1"></i> Hoàn Tất Đơn Hàng
            </button>

            <button 
              v-if="order.status !== 'COMPLETED' && order.status !== 'CANCELLED'" 
              class="btn btn-link text-danger btn-sm text-decoration-none mt-1"
              @click="updateStatus(order.id, 'CANCELLED')"
            >
              <i class="bi bi-x-circle me-1"></i> Hủy đơn này
            </button>
          </div>
        </div>
      </div>
    </div>
  </DefaultLayout>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import DefaultLayout from '../../layouts/DefaultLayout.vue';
import api from '../../api/axios';

const orders = ref([]);
const loading = ref(true);
let pollInterval = null;

onMounted(() => {
  fetchActiveOrders();
  // Auto refresh active orders every 10 seconds for real-time kitchen updates
  pollInterval = setInterval(fetchActiveOrders, 10000);
});

onUnmounted(() => {
  if (pollInterval) clearInterval(pollInterval);
});

const fetchActiveOrders = async () => {
  try {
    const res = await api.get('/api/staff/order/active');
    const data = res.data.data;
    orders.value = Array.isArray(data) ? data : (data.content || []);
  } catch (e) {
    console.error('Error loading active staff orders:', e);
  } finally {
    loading.value = false;
  }
};

const updateStatus = async (orderId, newStatus) => {
  if (newStatus === 'CANCELLED' && !confirm('Bạn có chắc chắn muốn hủy đơn hàng này?')) return;

  try {
    await api.put(`/api/staff/order/${orderId}/status`, { status: newStatus });
    await fetchActiveOrders();
  } catch (e) {
    alert(e.customMessage || 'Không thể cập nhật trạng thái đơn!');
  }
};

const pendingCount = computed(() => orders.value.filter(o => o.status === 'PENDING').length);
const preparingCount = computed(() => orders.value.filter(o => o.status === 'PREPARING').length);
const readyCount = computed(() => orders.value.filter(o => o.status === 'READY').length);

const getStatusBadge = (status) => {
  switch (status) {
    case 'PENDING': return 'badge-soft-warning';
    case 'PREPARING': return 'badge-soft-info';
    case 'READY': return 'badge-soft-primary';
    case 'COMPLETED': return 'badge-soft-success';
    case 'CANCELLED': return 'badge-soft-danger';
    default: return 'bg-secondary';
  }
};

const getStatusLabel = (status) => {
  switch (status) {
    case 'PENDING': return 'Chờ nhận';
    case 'PREPARING': return 'Đang chế biến';
    case 'READY': return 'Sẵn sàng';
    case 'COMPLETED': return 'Hoàn tất';
    case 'CANCELLED': return 'Đã hủy';
    default: return status;
  }
};

const formatVND = (amount) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount || 0);
};

const formatDate = (dateStr) => {
  if (!dateStr) return 'Vừa xong';
  return new Date(dateStr).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
};
</script>
