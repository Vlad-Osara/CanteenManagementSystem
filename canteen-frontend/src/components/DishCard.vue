<template>
  <div class="card card-canteen h-100 border-0 overflow-hidden d-flex flex-column shadow-sm">
    <!-- Image Header with Category Badge & Availability -->
    <div class="position-relative bg-light" style="height: 180px;">
      <!-- Fallback to a default image if the dish image fails to load -->
      <img 
        :src="dish.imageUrl || fallbackImage" 
        :alt="dish.name" 
        class="w-100 h-100 object-fit-cover"
        @error="handleImageError"
      />

      <!-- Category Badge -->
      <span class="position-absolute top-0 start-0 m-3 badge bg-white text-primary shadow-sm rounded-pill px-3 py-2 fw-semibold">
        <i class="bi bi-tag-fill me-1 text-primary"></i> {{ dish.categoryName || 'Món ăn' }}
      </span>

      <!-- Availability Overlay -->
      <div 
        v-if="!dish.isAvailable" 
        class="position-absolute top-0 bottom-0 start-0 end-0 bg-dark bg-opacity-60 d-flex align-items-center justify-content-center text-white fw-bold fs-5"
      >
        <span class="badge bg-danger rounded-pill px-3 py-2">
          <i class="bi bi-slash-circle me-1"></i> Tạm hết món
        </span>
      </div>
    </div>

    <!-- Card Body -->
    <div class="card-body d-flex flex-column p-3">
      <div class="d-flex justify-content-between align-items-start gap-2 mb-2">
        <h5 class="card-title fw-bold text-dark mb-0 text-truncate" :title="dish.name">
          {{ dish.name }}
        </h5>
        <span class="fs-5 fw-extrabold text-primary text-nowrap">
          {{ formatVND(dish.price) }}
        </span>
      </div>

      <p class="card-text text-muted small mb-3 flex-grow-1 text-multiline-truncate" style="min-height: 40px;">
        {{ dish.description || 'Món ăn thơm ngon, đảm bảo vệ sinh an toàn thực phẩm tại Căn tin.' }}
      </p>

      <!-- Action Button -->
       <!-- Event: addToCart and Data: dish will be passed as payload -->
      <button 
        type="button"
        class="btn btn-canteen-primary w-100 rounded-3 d-flex align-items-center justify-content-center gap-2"
        :disabled="!dish.isAvailable"
        @click="$emit('addToCart', dish)"
      >
        <i class="bi bi-cart-plus fs-5"></i>
        <span>{{ dish.isAvailable ? 'Thêm Vào Giỏ' : 'Hết Hàng' }}</span>
      </button>
    </div>
  </div>
</template>

<script setup>
// Receive the dish object as a prop and emit an event when the "Add to Cart" button is clicked. 
// The component also handles image loading errors by falling back to a default image and formats the price in Vietnamese Dong (VND) currency format.
defineProps({
  dish: {
    type: Object,
    required: true
  }
});

// Emit an event to the parent component when the "Add to Cart" button is clicked, passing the dish object as payload.
defineEmits(['addToCart']);

// Fallback image in case the dish image fails to load or is not provided.
const fallbackImage = '/favicon.svg';

// Intl.NumberFormat for Vietnamese Dong (VND) currency formatting once, to avoid creating a new formatter on each render.
const vndFormatter = new Intl.NumberFormat('vi-VN', { 
  style: 'currency', 
  currency: 'VND' 
});

const formatVND = (amount) => {
  return vndFormatter.format(amount || 0);
};

// function to handle image loading errors and fallback to a default image
const handleImageError = (event) => {
  if (event.target.src !== fallbackImage) {
    event.target.src = fallbackImage;
  }
};
</script>

<style scoped>
.text-multiline-truncate {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>