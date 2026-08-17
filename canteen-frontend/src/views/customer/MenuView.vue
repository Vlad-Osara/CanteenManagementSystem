<template>
  <DefaultLayout>
    <!-- Welcome Banner -->
    <div class="canteen-banner p-4 p-md-5 mb-4 rounded-4 position-relative overflow-hidden shadow-sm">
      <div class="row align-items-center">
        <div class="col-lg-7">
          <span class="badge bg-primary text-white px-3 py-2 rounded-pill fw-semibold mb-2">
            <i class="bi bi-stars me-1"></i> Thực Đơn Hàng Ngày
          </span>
          <h2 class="display-6 fw-extrabold text-primary mb-2">
            Thưởng Thức Món Ăn Thơm Ngon Tại Căn Tin!
          </h2>
          <p class="text-secondary fs-6 mb-4">
            Đặt món trực tuyến nhanh chóng, thanh toán trừ ví tiện lợi, không lo chờ đợi xếp hàng.
          </p>
          <div class="d-flex flex-wrap gap-3 align-items-center">
            <div class="d-flex align-items-center gap-2 text-dark font-semibold">
              <i class="bi bi-shield-check text-success fs-4"></i>
              <span>Vệ sinh & An toàn</span>
            </div>
            <div class="d-flex align-items-center gap-2 text-dark font-semibold ms-md-3">
              <i class="bi bi-lightning-charge-fill text-warning fs-4"></i>
              <span>Phục vụ siêu tốc</span>
            </div>
          </div>
        </div>
        <div class="col-lg-5 text-center d-none d-lg-block">
          <img src="/favicon.svg" alt="Canteen Graphic" width="160" height="160" class="drop-shadow filter-primary" />
        </div>
      </div>
    </div>

    <!-- Filter & Search Controls -->
    <div class="row g-3 align-items-center mb-4">
      <!-- Search Box -->
      <div class="col-md-5 col-lg-4">
        <div class="input-group shadow-sm rounded-pill overflow-hidden bg-white">
          <span class="input-group-text bg-white border-0 ps-3 text-muted">
            <i class="bi bi-search"></i>
          </span>
          <input 
            type="text" 
            class="form-control border-0 shadow-none ps-2" 
            placeholder="Tìm kiếm món ăn yêu thích..."
            v-model="searchQuery"
          />
          <button v-if="searchQuery" class="btn bg-white border-0 text-muted pe-3" @click="searchQuery = ''">
            <i class="bi bi-x-circle-fill"></i>
          </button>
        </div>
      </div>

      <!-- Category Filter Pills -->
      <div class="col-md-7 col-lg-8">
        <div class="d-flex gap-2 overflow-auto pb-1 align-items-center" style="scrollbar-width: none;">
          <button 
            class="btn btn-sm rounded-pill px-3 py-2 fw-semibold text-nowrap transition-all"
            :class="selectedCategoryId === null ? 'btn-primary shadow-sm' : 'btn-outline-primary bg-white'"
            @click="selectedCategoryId = null"
          >
            <i class="bi bi-grid-fill me-1"></i> Tất cả
          </button>

          <button 
            v-for="cat in categories" 
            :key="cat.id"
            class="btn btn-sm rounded-pill px-3 py-2 fw-semibold text-nowrap transition-all"
            :class="selectedCategoryId === cat.id ? 'btn-primary shadow-sm' : 'btn-outline-primary bg-white'"
            @click="selectedCategoryId = cat.id"
          >
            <i class="bi bi-tag-fill me-1"></i> {{ cat.name }}
          </button>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" style="width: 3rem; height: 3rem;" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
      <p class="text-muted mt-2 fw-semibold">Đang tải danh sách món ăn...</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="filteredDishes.length === 0" class="card card-canteen p-5 text-center my-4">
      <div class="bg-light d-inline-flex p-4 rounded-circle mx-auto mb-3">
        <i class="bi bi-search-heart text-muted display-4"></i>
      </div>
      <h5 class="fw-bold text-dark">Không tìm thấy món ăn phù hợp</h5>
      <p class="text-muted small">Bạn hãy thử thay đổi từ khóa tìm kiếm hoặc chọn danh mục khác nhé.</p>
      <button class="btn btn-outline-primary rounded-pill px-4 mx-auto" @click="resetFilters">
        Xóa bộ lọc
      </button>
    </div>

    <!-- Dishes Grid -->
    <div v-else class="row g-4 mb-5">
      <div v-for="dish in filteredDishes" :key="dish.id" class="col-sm-6 col-md-4 col-lg-3">
        <!-- Child Component will send an emit (@click="$emit('addToCart', dish)) when "Add to Cart" is clicked -->
        <!-- Then we handle the event (@addToCart="handleAddToCart") in the parent component (in this case, the MenuView) -->
         <!-- Parent send dish data to the child component by :dish="dish" -->
        <DishCard :dish="dish" @addToCart="handleAddToCart" />
      </div>
    </div>

    <!-- Pagination -->
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
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import DefaultLayout from '../../layouts/DefaultLayout.vue';
import DishCard from '../../components/DishCard.vue';
import Pagination from '../../components/Pagination.vue';
import { useCartStore } from '../../stores/cart';
import api from '../../api/axios';

const cartStore = useCartStore();
const dishes = ref([]);
const categories = ref([]);
const loading = ref(true);
const searchQuery = ref('');
const selectedCategoryId = ref(null);
let stompClient = null;

const currentPage = ref(1);
const limit = ref(12);
const totalPages = ref(1);
const totalElements = ref(0);

onMounted(async () => {
  await Promise.all([fetchCategories(), fetchDishes()]);
  connectMenuWebSocket();
});

onUnmounted(() => {
  if (stompClient && stompClient.connected) {
    stompClient.disconnect();
  }
});

const connectMenuWebSocket = () => {
  const wsBaseUrl = import.meta.env.VITE_WS_BASE_URL || 'https://api.huyloi.uk';
  const socketUrl = `${wsBaseUrl.replace('wss://', 'https://').replace('ws://', 'http://')}/ws`;
  const socket = new SockJS(socketUrl);
  stompClient = Stomp.over(socket);
  stompClient.debug = () => {};
  stompClient.connect({}, () => {
    // Lắng nghe kênh thay đổi trạng thái món ăn: /topic/dishes/availability
    stompClient.subscribe('/topic/dishes/availability', (message) => {
      if (message.body) {
        const updatedDish = JSON.parse(message.body);
        
        // Tìm món trong danh sách thực đơn và cập nhật trạng thái tức thì
        const targetDish = dishes.value.find(d => d.id === updatedDish.id);
        if (targetDish) {
          targetDish.isAvailable = updatedDish.isAvailable;
          targetDish.price = updatedDish.price;
          targetDish.name = updatedDish.name;
        }
      }
    });
  });
};

const fetchCategories = async () => {
  try {
    const res = await api.get('/api/customer/category');
    const data = res.data.data;
    categories.value = Array.isArray(data) ? data : (data.content || []);
  } catch (e) {
    console.error('Failed to load categories:', e);
  }
};

const fetchDishes = async () => {
  loading.value = true;
  try {
    const res = await api.get('/api/customer/dish', {
      params: {
        page: currentPage.value,
        limit: limit.value,
        search: searchQuery.value?.trim() || undefined,
        categoryId: selectedCategoryId.value || undefined
      }
    });
    const data = res.data.data;
    if (data && data.content) {
      dishes.value = data.content;
      totalPages.value = data.totalPages || 1;
      totalElements.value = data.totalElements || 0;
    } else {
      dishes.value = Array.isArray(data) ? data : [];
      totalPages.value = 1;
      totalElements.value = dishes.value.length;
    }
  } catch (e) {
    console.error('Failed to load dishes:', e);
  } finally {
    loading.value = false;
  }
};

let searchTimeout = null;
watch([selectedCategoryId, searchQuery], () => {
  currentPage.value = 1;
  clearTimeout(searchTimeout);
  // Debounce 300ms để tránh gửi request liên tục khi đang gõ phím
  searchTimeout = setTimeout(() => {
    fetchDishes();
  }, 300);
});

const filteredDishes = computed(() => dishes.value);

const handlePageChange = (page) => {
  currentPage.value = page;
  fetchDishes();
  window.scrollTo({ top: 200, behavior: 'smooth' }); // Cuộn nhẹ lên đầu thực đơn
};

const resetFilters = () => {
  searchQuery.value = '';
  selectedCategoryId.value = null;
  currentPage.value = 1;
  fetchDishes();
};

const handleAddToCart = (dish) => {
  cartStore.addToCart(dish, 1);
  const offcanvasEl = document.getElementById('cartDrawer');
  if (offcanvasEl && window.bootstrap) {
    const bsOffcanvas = window.bootstrap.Offcanvas.getOrCreateInstance(offcanvasEl);
    bsOffcanvas.show();
  }
};
</script>
