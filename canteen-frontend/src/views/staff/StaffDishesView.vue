<template>
  <DefaultLayout>
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between mb-4">
      <div>
        <h3 class="fw-extrabold text-primary mb-1">
          <i class="bi bi-toggle-on me-2"></i> Quản Lý Trạng Thái Món Ăn (Còn / Hết Hàng)
        </h3>
        <p class="text-muted small mb-0">Bật / tắt nhanh món ăn đang bán hoặc tạm ngưng tại căn tin</p>
      </div>

      <router-link to="/staff/orders" class="btn btn-outline-primary rounded-pill px-3 py-2 fw-semibold">
        <i class="bi bi-arrow-left me-1"></i> Quay lại Màn hình Bếp
      </router-link>
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
          placeholder="Tìm tên món ăn..."
          v-model="searchQuery"
        />
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status"></div>
      <p class="text-muted mt-2">Đang tải danh sách thực đơn...</p>
    </div>

    <!-- Table List -->
    <div v-else class="card card-canteen border-0 p-0 overflow-hidden shadow-sm">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="bg-light">
            <tr>
              <th class="ps-4">Hình ảnh</th>
              <th>Tên món ăn</th>
              <th>Danh mục</th>
              <th>Đơn giá</th>
              <th>Trạng thái hiện tại</th>
              <th class="text-end pe-4">Hành động Bật/Tắt</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="dish in filteredDishes" :key="dish.id">
              <td class="ps-4">
                <img 
                  :src="dish.imageUrl || '/favicon.svg'" 
                  :alt="dish.name" 
                  class="rounded-3 object-fit-cover bg-light"
                  style="width: 50px; height: 50px;"
                  @error="(e) => e.target.src = '/favicon.svg'"
                />
              </td>
              <td class="fw-bold text-dark">{{ dish.name }}</td>
              <td>
                <span class="badge bg-light text-primary border rounded-pill px-3 py-1">
                  {{ dish.categoryName || 'Món ăn' }}
                </span>
              </td>
              <td class="fw-extrabold text-primary">{{ formatVND(dish.price) }}</td>
              <td>
                <span :class="['badge rounded-pill px-3 py-2', dish.isAvailable ? 'badge-soft-success' : 'badge-soft-danger']">
                  <i :class="[dish.isAvailable ? 'bi bi-check-circle-fill' : 'bi bi-x-circle-fill', 'me-1']"></i>
                  {{ dish.isAvailable ? 'Đang Phục Vụ' : 'Tạm Hết Món' }}
                </span>
              </td>
              <td class="text-end pe-4">
                <div class="form-check form-switch d-inline-block">
                  <input 
                    class="form-check-input fs-4 cursor-pointer" 
                    type="checkbox" 
                    role="switch" 
                    :checked="dish.isAvailable"
                    @change="toggleAvailability(dish)"
                  />
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    
    <Pagination 
    :current-page="currentPage" 
    :total-pages="totalPages" 
    :total-elements="totalElements" 
    :current-count="dishes.length"
    item-label="món ăn"
    @change-page="handlePageChange"
  />
  </DefaultLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import DefaultLayout from '../../layouts/DefaultLayout.vue';
import Pagination from '../../components/Pagination.vue';
import api from '../../api/axios';

const dishes = ref([]);
const loading = ref(true);
const searchQuery = ref('');

// Pagination state
const currentPage = ref(1);
const limit = ref(10);
const totalPages = ref(1);
const totalElements = ref(0);

onMounted(() => {
  fetchDishes();
});

const fetchDishes = async () => {
  loading.value = true;
  try {
    const res = await api.get('/api/customer/dish', {
      params: {
        page: currentPage.value,
        limit: limit.value
      }
    });
    const data = res.data.data;
    if (data && data.content) {
      dishes.value = data.content;
      totalPages.value = data.totalPages || 1;
      totalElements.value = data.totalElements || 0;
    } else {
      dishes.value = Array.isArray(data) ? data : [];
    }
  } catch (e) {
    console.error('Error fetching dishes for staff:', e);
  } finally {
    loading.value = false;
  }
};

const handlePageChange = (page) => {
  currentPage.value = page;
  fetchDishes();
};

const filteredDishes = computed(() => {
  if (!searchQuery.value) return dishes.value;
  return dishes.value.filter(d => d.name.toLowerCase().includes(searchQuery.value.toLowerCase()));
});

const toggleAvailability = async (dish) => {
  try {
    await api.put(`/api/staff/dishes/${dish.id}/availability`);
    dish.isAvailable = !dish.isAvailable;
  } catch (e) {
    alert(e.customMessage || 'Không thể cập nhật trạng thái món!');
    dish.isAvailable = !dish.isAvailable; // revert UI
  }
};

const formatVND = (amount) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount || 0);
};
</script>
