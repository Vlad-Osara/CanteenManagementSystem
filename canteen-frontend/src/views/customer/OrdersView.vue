<template>
  <DefaultLayout>
    <div class="d-flex align-items-center justify-content-between mb-4">
      <div>
        <h3 class="fw-extrabold text-primary mb-1">
          <i class="bi bi-receipt-cutoff me-2"></i> Đơn Hàng Của Tôi
        </h3>
        <p class="text-muted small mb-0">Theo dõi tiến độ chế biến và lịch sử đặt món của bạn</p>
      </div>

      <button class="btn btn-outline-primary rounded-pill px-3 py-2 fw-semibold" @click="fetchOrders">
        <i class="bi bi-arrow-clockwise me-1"></i> Làm mới
      </button>
    </div>

        <!-- Status & Sort Controls -->
    <div class="row g-3 align-items-center mb-4">
      <!-- Status Filters (Pills) -->
      <div class="col-md-8 col-lg-9">
        <div class="d-flex gap-2 overflow-auto pb-1" style="scrollbar-width: none;">
          <button 
            v-for="statusFilter in statusFilters" 
            :key="statusFilter.value"
            class="btn btn-sm rounded-pill px-3 py-2 fw-semibold text-nowrap transition-all"
            :class="activeFilter === statusFilter.value ? 'btn-primary shadow-sm' : 'btn-outline-primary bg-white'"
            @click="activeFilter = statusFilter.value"
          >
            {{ statusFilter.label }}
          </button>
        </div>
      </div>

      <!-- Dropdown Sắp Xếp Đơn Hàng -->
      <div class="col-md-4 col-lg-3">
        <select class="form-select form-select-sm shadow-sm rounded-pill bg-white fw-semibold text-muted" v-model="sortByOption">
          <option value="NEWEST">Mới nhất trước</option>
          <option value="ACTIVE_FIRST">Đang xử lý lên đầu</option>
          <option value="OLDEST">Cũ nhất trước</option>
        </select>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status"></div>
      <p class="text-muted mt-2">Đang tải lịch sử đơn hàng...</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="filteredOrders.length === 0" class="card card-canteen p-5 text-center my-4">
      <div class="bg-light d-inline-flex p-4 rounded-circle mx-auto mb-3">
        <i class="bi bi-journal-x text-muted display-4"></i>
      </div>
      <h5 class="fw-bold text-dark">Chưa có đơn hàng nào</h5>
      <p class="text-muted small">Hãy truy cập thực đơn và đặt những món ăn đầu tiên nhé!</p>
      <router-link to="/" class="btn btn-canteen-primary rounded-pill px-4 mx-auto">
        Khám phá thực đơn
      </router-link>
    </div>

    <!-- Orders List -->
    <div v-else class="row g-3">
      <div v-for="order in filteredOrders" :key="order.id" class="col-12">
        <div class="card card-canteen border-0 p-4">
          <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3 pb-3 border-bottom">
            <div>
              <span class="fw-bold text-dark me-2">Mã đơn: #{{ order.id ? order.id.substring(0, 8) : 'N/A' }}</span>
              <span class="text-muted small me-3">
                <i class="bi bi-clock me-1"></i> {{ formatDate(order.createdAt) }}
              </span>
              <span :class="['badge rounded-pill px-3 py-2', getOrderTypeBadge(order.type)]">
                <i :class="[order.type === 'DINE_IN' ? 'bi bi-cup-hot' : 'bi bi-bag-check', 'me-1']"></i>
                {{ order.type === 'DINE_IN' ? 'Ăn tại chỗ' : 'Mang về' }}
              </span>
            </div>

            <div class="d-flex align-items-center gap-2">
              <span :class="['badge rounded-pill px-3 py-2 fs-6 fw-bold', getStatusBadge(order.status)]">
                <i :class="[getStatusIcon(order.status), 'me-1']"></i>
                {{ getStatusLabel(order.status) }}
              </span>
            </div>
          </div>

          <!-- Items list -->
          <div class="row g-2 mb-3">
            <div 
              v-for="item in (order.orderItemDTO || [])" 
              :key="item.dishId" 
              class="col-md-6 col-lg-4"
            >
              <div class="d-flex justify-content-between align-items-center p-2 bg-light rounded-3">
                <div class="d-flex align-items-center gap-2">
                  <span class="badge bg-primary rounded-circle p-2">{{ item.quantity }}x</span>
                  <span class="fw-semibold text-dark small text-truncate" style="max-width: 180px;">{{ item.dishName }}</span>
                </div>
                <span class="fw-bold small text-primary">{{ formatVND(item.price * item.quantity) }}</span>
              </div>
            </div>
          </div>

          <!-- Note & Total -->
          <div class="d-flex flex-wrap justify-content-between align-items-center pt-2 gap-2">
            <div class="text-muted small">
              <span v-if="order.note" class="badge bg-light text-dark border">
                <i class="bi bi-chat-left-text me-1"></i> Ghi chú: {{ order.note }}
              </span>
            </div>

            <div class="d-flex align-items-center gap-3">
              <div class="text-end">
                <span class="text-muted small me-2">Tổng thanh toán:</span>
                <span class="fs-5 fw-extrabold text-primary">{{ formatVND(order.totalPrice) }}</span>
              </div>

              <!-- Cancel order button if PENDING
              <button 
                v-if="order.status === 'PENDING'" 
                class="btn btn-sm btn-outline-danger rounded-pill px-3"
                @click="cancelOrder(order.id)"
              >
                Hủy đơn
              </button> -->
            </div>
          </div>
        </div>
      </div>
    </div>
  </DefaultLayout>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import DefaultLayout from '../../layouts/DefaultLayout.vue';
import api from '../../api/axios';
import { useAuthStore } from '../../stores/auth';

const authStore = useAuthStore();
const orders = ref([]);
const loading = ref(true);
const activeFilter = ref('ALL');
const sortByOption = ref('NEWEST');
let stompClient = null;

const statusFilters = [
  { value: 'ALL', label: 'Tất cả đơn' },
  { value: 'PENDING', label: 'Chờ tiếp nhận' },
  { value: 'PREPARING', label: 'Đang chế biến' },
  { value: 'READY', label: 'Sẵn sàng nhận' },
  { value: 'COMPLETED', label: 'Hoàn tất' },
  { value: 'CANCELLED', label: 'Đã hủy' }
];

onMounted(() => {
  fetchOrders();
  connectWebSocket();
});

const connectWebSocket = () => {
  const customerId = authStore.user?.id;
  if (!customerId) return;
  const wsBaseUrl = import.meta.env.VITE_WS_BASE_URL || 'https://api.huyloi.uk';
  const socketUrl = `${wsBaseUrl.replace('wss://', 'https://').replace('ws://', 'http://')}/ws`;
  const socket = new SockJS(socketUrl);
  stompClient = Stomp.over(socket);
  stompClient.debug = () => {};
  stompClient.connect({}, () => {
    stompClient.subscribe(`/topic/customer/${customerId}/orders`, (message) => {
      if (message.body) {
        const updatedOrder = JSON.parse(message.body);
        
        const index = orders.value.findIndex(o => o.id === updatedOrder.id);
        if (index !== -1) {
          orders.value[index] = updatedOrder;
        } else {
          orders.value.unshift(updatedOrder);
        }
      }
    });
  });
};

const fetchOrders = async () => {
  loading.value = true;
  try {
    const res = await api.get('/api/customer/order', {
      params: {
        isPaginate: false // Lấy toàn bộ lịch sử đơn của khách
      }
    });
    const data = res.data.data;
    orders.value = Array.isArray(data) ? data : (data.content || []);
  } catch (e) {
    console.error('Error fetching customer orders:', e);
  } finally {
    loading.value = false;
  }
};

const filteredOrders = computed(() => {
  let list = [...orders.value];
  // 1. Lọc theo trạng thái
  if (activeFilter.value !== 'ALL') {
    list = list.filter(o => o.status === activeFilter.value);
  }
  // 2. Sắp xếp theo tùy chọn
  if (sortByOption.value === 'ACTIVE_FIRST') {
    // Thứ tự ưu tiên: Sẵn sàng nhận (READY) > Đang nấu (PREPARING) > Chờ (PENDING) > Đã xong/Hủy
    const priority = { READY: 1, PREPARING: 2, PENDING: 3, COMPLETED: 4, CANCELLED: 5 };
    list.sort((a, b) => {
      const pA = priority[a.status] || 99;
      const pB = priority[b.status] || 99;
      if (pA !== pB) return pA - pB;
      return parseOrderDate(b.createdAt) - parseOrderDate(a.createdAt);
    });
  } else if (sortByOption.value === 'OLDEST') {
    list.sort((a, b) => parseOrderDate(a.createdAt) - parseOrderDate(b.createdAt));
  } else {
    // Mặc định: Mới nhất trước (NEWEST)
    list.sort((a, b) => parseOrderDate(b.createdAt) - parseOrderDate(a.createdAt));
  }
  return list;
});

const parseOrderDate = (dateStr) => {
  if (!dateStr) return 0;
  if (typeof dateStr === 'string' && dateStr.includes('/')) {
    // Định dạng "17/08/2026 14:30:00"
    const [d, m, yAndTime] = dateStr.split('/');
    const [y, time] = yAndTime.split(' ');
    return new Date(`${y}-${m}-${d}T${time}`).getTime() || 0;
  }
  return new Date(dateStr).getTime() || 0;
};

const formatDate = (dateStr) => {
  if (!dateStr) return 'Vừa xong';
  const timestamp = parseOrderDate(dateStr);
  if (!timestamp) return dateStr;
  return new Intl.DateTimeFormat('vi-VN', {
    hour: '2-digit', minute: '2-digit', second: '2-digit',
    day: '2-digit', month: '2-digit', year: 'numeric'
  }).format(new Date(timestamp));
};

// Cancel order function (commented out for now), in future, can implement it if needed. It will send a request to cancel the order and refresh the list afterward.
// const cancelOrder = async (orderId) => {
//   if (!confirm('Bạn có chắc chắn muốn hủy đơn hàng này không? Sẽ hoàn tiền lại số dư ví của bạn.')) return;
//   try {
//     await api.put(`/api/customer/order/${orderId}`, { status: 'CANCELLED' });
//     await fetchOrders();
//   } catch (e) {
//     alert(e.customMessage || 'Không thể hủy đơn hàng!');
//   }
// };

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
    case 'PENDING': return 'Chờ nhận đơn';
    case 'PREPARING': return 'Đang chế biến';
    case 'READY': return 'Sẵn sàng nhận món';
    case 'COMPLETED': return 'Đã hoàn tất';
    case 'CANCELLED': return 'Đã hủy đơn';
    default: return status;
  }
};

const getStatusIcon = (status) => {
  switch (status) {
    case 'PENDING': return 'bi-hourglass-split';
    case 'PREPARING': return 'bi-fire';
    case 'READY': return 'bi-bell-fill';
    case 'COMPLETED': return 'bi-check-circle-fill';
    case 'CANCELLED': return 'bi-x-circle-fill';
    default: return 'bi-info-circle';
  }
};

const getOrderTypeBadge = (type) => {
  return type === 'DINE_IN' ? 'bg-light text-primary border' : 'bg-light text-success border';
};

const formatVND = (amount) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount || 0);
};
</script>
