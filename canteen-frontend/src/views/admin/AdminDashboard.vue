<template>
  <DefaultLayout>
    <!-- Header -->
    <div class="d-flex flex-wrap justify-content-between align-items-center mb-4 gap-3">
      <div>
        <h3 class="fw-extrabold text-primary mb-1">
          <i class="bi bi-speedometer2 me-2"></i> Báo Cáo & Thống Kê Hoạt Động
        </h3>
        <p class="text-muted small mb-0">Theo dõi doanh thu bán hàng, tiền nạp ví VNPay và sản lượng món ăn theo từng tháng</p>
      </div>
      
      <div class="d-flex align-items-center gap-2">
        <div class="d-flex align-items-center gap-2 bg-white px-3 py-1 rounded-pill shadow-sm border">
          <i class="bi bi-calendar3 text-primary"></i>
          <span class="small text-muted fw-bold">Năm:</span>
          <select class="form-select form-select-sm border-0 shadow-none fw-bold text-primary pe-4" style="width: auto; cursor: pointer;" v-model="selectedYear" @change="fetchDashboardData">
            <option v-for="y in availableYears" :key="y" :value="y">
              {{ y }}
            </option>
          </select>
        </div>
        <button class="btn btn-outline-primary rounded-pill px-3 py-2 fw-semibold" @click="fetchDashboardData">
          <i class="bi bi-arrow-clockwise me-1"></i> Làm mới
        </button>
      </div>
    </div>
    <!-- KPI Summary Cards -->
    <div class="row g-3 mb-4">
      <!-- Doanh thu đặt món năm nay -->
      <div class="col-md-6 col-lg-3">
        <div class="card card-canteen border-0 p-3 bg-white shadow-sm border-start border-4 border-primary">
          <div class="text-muted small fw-bold text-uppercase">Doanh Thu Món Ăn (Năm {{ statsData?.year }})</div>
          <div class="fs-4 fw-extrabold text-primary my-1">{{ formatVND(statsData?.totalYearRevenue) }}</div>
          <div class="small text-muted"><i class="bi bi-check2-circle text-success me-1"></i> Từ đơn hàng thành công</div>
        </div>
      </div>
      <!-- Tiền nạp ví VNPay năm nay -->
      <div class="col-md-6 col-lg-3">
        <div class="card card-canteen border-0 p-3 bg-white shadow-sm border-start border-4 border-success">
          <div class="text-muted small fw-bold text-uppercase">Tổng Tiền Nạp VNPay</div>
          <div class="fs-4 fw-extrabold text-success my-1">{{ formatVND(statsData?.totalYearDeposit) }}</div>
          <div class="small text-muted"><i class="bi bi-wallet2 text-success me-1"></i> Khách nạp vào ví căn tin</div>
        </div>
      </div>
      <!-- Tổng đơn hoàn tất -->
      <div class="col-md-6 col-lg-3">
        <div class="card card-canteen border-0 p-3 bg-white shadow-sm border-start border-4 border-info">
          <div class="text-muted small fw-bold text-uppercase">Đơn Hàng Hoàn Tất</div>
          <div class="fs-4 fw-extrabold text-info my-1">{{ statsData?.totalYearOrders || 0 }} đơn</div>
          <div class="small text-muted"><i class="bi bi-bag-check text-info me-1"></i> Đã phục vụ khách</div>
        </div>
      </div>
      <!-- Quy mô thực đơn & User -->
      <div class="col-md-6 col-lg-3">
        <div class="card card-canteen border-0 p-3 bg-white shadow-sm border-start border-4 border-warning">
          <div class="text-muted small fw-bold text-uppercase">Quy Mô Hệ Thống</div>
          <div class="fs-4 fw-extrabold text-dark my-1">{{ dishesCount }} món / {{ usersCount }} user</div>
          <div class="small text-muted"><i class="bi bi-tags text-warning me-1"></i> {{ categoriesCount }} danh mục</div>
        </div>
      </div>
    </div>
    <!-- Charts Section -->
    <div class="row g-4 mb-4">
      <!-- 1. Biểu đồ Doanh Thu & Nạp Tiền -->
      <div class="col-lg-7">
        <div class="card card-canteen border-0 p-4 shadow-sm h-100 bg-white">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h6 class="fw-bold text-dark mb-0">
              <i class="bi bi-bar-chart-line-fill text-primary me-2"></i> Doanh Thu & Nạp Tiền Qua Các Tháng (Năm {{ statsData?.year }})
            </h6>
          </div>
          <div class="position-relative" style="height: 320px;">
            <canvas id="revenueDepositChart"></canvas>
          </div>
        </div>
      </div>
      <!-- 2. Biểu đồ Phân Bổ Món Ăn Theo Danh Mục -->
      <div class="col-lg-5">
        <div class="card card-canteen border-0 p-4 shadow-sm h-100 bg-white">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h6 class="fw-bold text-dark mb-0">
              <i class="bi bi-pie-chart-fill text-primary me-2"></i> Món Bán Ra Theo Danh Mục
            </h6>
          </div>
          <div class="position-relative" style="height: 320px;">
            <canvas id="categoryChart"></canvas>
          </div>
        </div>
      </div>
    </div>
    <!-- Navigation Quick Actions -->
    <div class="row g-4">
      <div class="col-md-6">
        <div class="card card-canteen border-0 p-4 shadow-sm bg-white">
          <h6 class="fw-bold text-primary mb-3">
            <i class="bi bi-lightning-charge me-2"></i> Điều Hành Bếp & Thu Ngân
          </h6>
          <div class="d-grid gap-2">
            <router-link to="/staff/orders" class="btn btn-outline-primary py-2 text-start rounded-3 fw-semibold">
              <i class="bi bi-hourglass-split me-2"></i> Màn hình Bếp Quản Lý Đơn Đang Chế Biến
            </router-link>
            <router-link to="/staff/dishes" class="btn btn-outline-primary py-2 text-start rounded-3 fw-semibold">
              <i class="bi bi-toggle-on me-2"></i> Cập Nhật Trạng Thái Món Ăn (Còn / Hết Hàng)
            </router-link>
          </div>
        </div>
      </div>
      <div class="col-md-6">
        <div class="card card-canteen border-0 p-4 shadow-sm bg-white">
          <h6 class="fw-bold text-primary mb-3">
            <i class="bi bi-gear-wide-connected me-2"></i> Quản Lý Dữ Liệu Căn Tin
          </h6>
          <div class="d-grid gap-2">
            <router-link to="/admin/dishes" class="btn btn-canteen-light py-2 text-start rounded-3 fw-semibold">
              <i class="bi bi-egg-fried me-2"></i> Quản Lý Thực Đơn & Giá Tiền
            </router-link>
            <router-link to="/admin/accounts" class="btn btn-canteen-light py-2 text-start rounded-3 fw-semibold">
              <i class="bi bi-people me-2"></i> Quản Lý Tài Khoản & Phân Quyền
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </DefaultLayout>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue';
import { Chart, registerables } from 'chart.js';
import DefaultLayout from '../../layouts/DefaultLayout.vue';
import api from '../../api/axios';
Chart.register(...registerables);
const statsData = ref(null);
const dishesCount = ref(0);
const categoriesCount = ref(0);
const usersCount = ref(0);
const currentRealYear = new Date().getFullYear();
const selectedYear = ref(Math.max(currentRealYear, 2026));
let revenueChartInstance = null;
let categoryChartInstance = null;

const availableYears = computed(() => {
  const years = [];
  const endYear = Math.max(currentRealYear, 2026);
  for (let y = endYear; y >= 2026; y--) {
    years.push(y);
  }
  return years;
});

onMounted(async () => {
  await fetchDashboardData();
});

onUnmounted(() => {
  if (revenueChartInstance) revenueChartInstance.destroy();
  if (categoryChartInstance) categoryChartInstance.destroy();
});

const fetchDashboardData = async () => {
  try {
    const [statsRes, dishRes, catRes, userRes] = await Promise.allSettled([
      api.get('/api/admin/statistics/dashboard', {
        params: { year: selectedYear.value }
      }),
      api.get('/api/customer/dish'),
      api.get('/api/customer/category'),
      api.get('/api/admin/account')
    ]);
    if (statsRes.status === 'fulfilled') {
      statsData.value = statsRes.value.data.data;
      await nextTick();
      renderRevenueChart();
      renderCategoryChart();
    }
    if (dishRes.status === 'fulfilled') {
      const data = dishRes.value.data.data;
      dishesCount.value = Array.isArray(data) ? data.length : (data.totalElements || data.content?.length || 0);
    }
    if (catRes.status === 'fulfilled') {
      const data = catRes.value.data.data;
      categoriesCount.value = Array.isArray(data) ? data.length : (data.totalElements || data.content?.length || 0);
    }
    if (userRes.status === 'fulfilled') {
      const data = userRes.value.data.data;
      usersCount.value = Array.isArray(data) ? data.length : (data.totalElements || data.content?.length || 0);
    }
  } catch (e) {
    console.error('Error fetching dashboard statistics:', e);
  }
};

const renderRevenueChart = () => {
  if (!statsData.value) return;
  const ctx = document.getElementById('revenueDepositChart');
  if (!ctx) return;
  if (revenueChartInstance) revenueChartInstance.destroy();
  const labels = statsData.value.monthlyStats.map(m => `Tháng ${m.month}`);
  const depositData = statsData.value.monthlyStats.map(m => m.totalDeposit);
  const revenueData = statsData.value.monthlyStats.map(m => m.totalOrderRevenue);
  revenueChartInstance = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: labels,
      datasets: [
        {
          label: 'Tiền Nạp Ví VNPay',
          data: depositData,
          backgroundColor: 'rgba(40, 167, 69, 0.7)',
          borderColor: '#28a745',
          borderWidth: 1,
          borderRadius: 6,
          order: 2
        },
        {
          label: 'Doanh Thu Đặt Món',
          data: revenueData,
          type: 'line',
          borderColor: '#0d6efd',
          backgroundColor: 'rgba(13, 110, 253, 0.1)',
          fill: true,
          tension: 0.35,
          pointBackgroundColor: '#0d6efd',
          pointRadius: 5,
          order: 1
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { position: 'top' },
        tooltip: {
          callbacks: {
            label: (ctx) => `${ctx.dataset.label}: ${formatVND(ctx.raw)}`
          }
        }
      },
      scales: {
        y: {
          beginAtZero: true,
          ticks: {
            callback: (value) => value >= 1000000 ? `${value / 1000000}Tr` : `${value / 1000}k`
          }
        }
      }
    }
  });
};

const renderCategoryChart = () => {
  if (!statsData.value) return;
  const ctx = document.getElementById('categoryChart');
  if (!ctx) return;
  if (categoryChartInstance) categoryChartInstance.destroy();
  // Gom tổng số lượng món bán ra theo danh mục trong cả năm
  const categoryTotals = {};
  statsData.value.monthlyStats.forEach(m => {
    if (m.categorySales) {
      Object.entries(m.categorySales).forEach(([catName, qty]) => {
        categoryTotals[catName] = (categoryTotals[catName] || 0) + qty;
      });
    }
  });
  const catLabels = Object.keys(categoryTotals);
  const catValues = Object.values(categoryTotals);
  categoryChartInstance = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: catLabels.length > 0 ? catLabels : ['Chưa có đơn'],
      datasets: [{
        data: catValues.length > 0 ? catValues : [1],
        backgroundColor: [
          '#0d6efd', '#20c997', '#ffc107', '#fd7e14', '#6f42c1', '#e83e8c'
        ]
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { position: 'bottom' },
        tooltip: {
          callbacks: {
            label: (ctx) => `${ctx.label}: ${ctx.raw} phần món`
          }
        }
      }
    }
  });
};

const formatVND = (amount) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount || 0);
};
</script>
