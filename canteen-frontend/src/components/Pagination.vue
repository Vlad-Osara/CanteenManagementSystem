<template>
  <div 
    v-if="totalPages > 1" 
    class="d-flex flex-column flex-sm-row justify-content-between align-items-center gap-3 my-4 pt-3 border-top"
  >
    <!-- Thống kê số lượng bản ghi -->
    <div class="text-muted small">
      Hiển thị <b>{{ currentCount }}</b> / <b>{{ totalElements }}</b> {{ itemLabel }} 
      (Trang <span class="text-primary fw-bold">{{ currentPage }}</span> / {{ totalPages }})
    </div>

    <!-- Thanh điều hướng phân trang -->
    <nav aria-label="Page navigation">
      <ul class="pagination mb-0 shadow-sm rounded-pill overflow-hidden">
        <!-- Nút Về trang trước -->
        <li class="page-item" :class="{ disabled: currentPage <= 1 }">
          <button 
            class="page-link shadow-none" 
            @click="goToPage(currentPage - 1)" 
            :disabled="currentPage <= 1"
          >
            <i class="bi bi-chevron-left me-1"></i> Trước
          </button>
        </li>

        <!-- Các nút số trang -->
        <li 
          v-for="p in visiblePages" 
          :key="p" 
          class="page-item" 
          :class="{ active: currentPage === p, disabled: p === '...' }"
        >
          <button 
            v-if="p !== '...'" 
            class="page-link shadow-none fw-semibold" 
            @click="goToPage(p)"
          >
            {{ p }}
          </button>
          <span v-else class="page-link shadow-none border-0 text-muted">...</span>
        </li>

        <!-- Nút Sang trang sau -->
        <li class="page-item" :class="{ disabled: currentPage >= totalPages }">
          <button 
            class="page-link shadow-none" 
            @click="goToPage(currentPage + 1)" 
            :disabled="currentPage >= totalPages"
          >
            Sau <i class="bi bi-chevron-right ms-1"></i>
          </button>
        </li>
      </ul>
    </nav>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  currentPage: {
    type: Number,
    required: true,
    default: 1
  },
  totalPages: {
    type: Number,
    required: true,
    default: 1
  },
  totalElements: {
    type: Number,
    default: 0
  },
  currentCount: {
    type: Number,
    default: 0
  },
  itemLabel: {
    type: String,
    default: 'mục'
  }
});

const emit = defineEmits(['change-page']);

const goToPage = (page) => {
  if (page >= 1 && page <= props.totalPages && page !== props.currentPage) {
    emit('change-page', page);
  }
};

// Thuật toán hiển thị danh sách trang thông minh (ví dụ: 1 2 3 ... 10)
const visiblePages = computed(() => {
  const total = props.totalPages;
  const current = props.currentPage;
  if (total <= 7) {
    return Array.from({ length: total }, (_, i) => i + 1);
  }
  const pages = [];
  if (current <= 4) {
    pages.push(1, 2, 3, 4, 5, '...', total);
  } else if (current >= total - 3) {
    pages.push(1, '...', total - 4, total - 3, total - 2, total - 1, total);
  } else {
    pages.push(1, '...', current - 1, current, current + 1, '...', total);
  }
  return pages;
});
</script>