<template>
  <DefaultLayout>
    <!-- Header -->
    <div class="mb-4">
      <h3 class="fw-extrabold text-primary mb-1">
        <i class="bi bi-speedometer2 me-2"></i> Tổng Quan Quản Trị Căn Tin
      </h3>
      <p class="text-muted small mb-0">Theo dõi toàn bộ số liệu thực đơn, danh mục và tài khoản người dùng hệ thống</p>
    </div>

    <!-- Metrics Cards -->
    <div class="row g-4 mb-5">
      <!-- Dishes count -->
      <div class="col-md-4">
        <div class="card card-canteen border-0 p-4 bg-primary text-white shadow-sm">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <div>
              <div class="text-white-50 text-uppercase small fw-bold">Tổng Món Ăn</div>
              <div class="display-5 fw-extrabold">{{ dishesCount }}</div>
            </div>
            <div class="p-3 bg-white bg-opacity-25 rounded-circle fs-2 text-white">
              <i class="bi bi-egg-fried"></i>
            </div>
          </div>
          <router-link to="/admin/dishes" class="btn btn-light btn-sm text-primary fw-bold rounded-pill w-100">
            Quản lý Thực Đơn <i class="bi bi-arrow-right ms-1"></i>
          </router-link>
        </div>
      </div>

      <!-- Categories count -->
      <div class="col-md-4">
        <div class="card card-canteen border-0 p-4 bg-info text-white shadow-sm">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <div>
              <div class="text-white-50 text-uppercase small fw-bold">Danh Mục Món</div>
              <div class="display-5 fw-extrabold">{{ categoriesCount }}</div>
            </div>
            <div class="p-3 bg-white bg-opacity-25 rounded-circle fs-2 text-white">
              <i class="bi bi-tags"></i>
            </div>
          </div>
          <router-link to="/admin/categories" class="btn btn-light btn-sm text-info fw-bold rounded-pill w-100">
            Quản lý Danh Mục <i class="bi bi-arrow-right ms-1"></i>
          </router-link>
        </div>
      </div>

      <!-- Accounts count -->
      <div class="col-md-4">
        <div class="card card-canteen border-0 p-4 bg-success text-white shadow-sm">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <div>
              <div class="text-white-50 text-uppercase small fw-bold">Người Dùng Hệ Thống</div>
              <div class="display-5 fw-extrabold">{{ usersCount }}</div>
            </div>
            <div class="p-3 bg-white bg-opacity-25 rounded-circle fs-2 text-white">
              <i class="bi bi-people"></i>
            </div>
          </div>
          <router-link to="/admin/accounts" class="btn btn-light btn-sm text-success fw-bold rounded-pill w-100">
            Quản lý Tài Khoản <i class="bi bi-arrow-right ms-1"></i>
          </router-link>
        </div>
      </div>
    </div>

    <!-- Navigation Quick Actions -->
    <div class="row g-4">
      <div class="col-md-6">
        <div class="card card-canteen border-0 p-4">
          <h5 class="fw-bold text-primary mb-3">
            <i class="bi bi-lightning-charge me-2"></i> Thao Tác Nhanh Bếp & Phục Vụ
          </h5>
          <div class="d-grid gap-2">
            <router-link to="/staff/orders" class="btn btn-outline-primary py-3 text-start rounded-3 fw-semibold">
              <i class="bi bi-hourglass-split me-2 fs-5"></i> Màn hình Bếp Quản Lý Đơn Đang Xử Lý
            </router-link>
            <router-link to="/staff/dishes" class="btn btn-outline-primary py-3 text-start rounded-3 fw-semibold">
              <i class="bi bi-toggle-on me-2 fs-5"></i> Cập Nhật Trạng Thái Món Ăn (Còn/Hết Hàng)
            </router-link>
          </div>
        </div>
      </div>

      <div class="col-md-6">
        <div class="card card-canteen border-0 p-4">
          <h5 class="fw-bold text-primary mb-3">
            <i class="bi bi-gear-wide-connected me-2"></i> Lối Tắt Quản Trị Viên
          </h5>
          <div class="d-grid gap-2">
            <router-link to="/admin/dishes" class="btn btn-canteen-light py-3 text-start rounded-3 fw-semibold">
              <i class="bi bi-plus-circle me-2 fs-5"></i> Thêm Món Ăn Mới Vào Thực Đơn
            </router-link>
            <router-link to="/admin/categories" class="btn btn-canteen-light py-3 text-start rounded-3 fw-semibold">
              <i class="bi bi-folder-plus me-2 fs-5"></i> Tạo Danh Mục Thực Đơn Mới
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </DefaultLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import DefaultLayout from '../../layouts/DefaultLayout.vue';
import api from '../../api/axios';

const dishesCount = ref(0);
const categoriesCount = ref(0);
const usersCount = ref(0);

onMounted(async () => {
  try {
    const [dishRes, catRes, userRes] = await Promise.allSettled([
      api.get('/api/customer/dish'),
      api.get('/api/customer/category'),
      api.get('/api/admin/account')
    ]);

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
    console.error('Error fetching admin dashboard metrics:', e);
  }
});
</script>
