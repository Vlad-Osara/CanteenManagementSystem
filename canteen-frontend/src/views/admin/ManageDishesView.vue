<template>
  <DefaultLayout>
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between mb-4">
      <div>
        <h3 class="fw-extrabold text-primary mb-1">
          <i class="bi bi-egg-fried me-2"></i> Quản Lý Món Ăn & Thực Đơn
        </h3>
        <p class="text-muted small mb-0">Thêm mới món ăn, cập nhật giá tiền, hình ảnh và trạng thái kinh doanh</p>
      </div>

      <button class="btn btn-canteen-primary rounded-pill px-4 py-2 fw-semibold" @click="openCreateModal">
        <i class="bi bi-plus-lg me-1"></i> Thêm Món Ăn Mới
      </button>
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

    <!-- Loading -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status"></div>
      <p class="text-muted mt-2">Đang tải danh sách món ăn...</p>
    </div>

    <!-- Table -->
    <div v-else class="card card-canteen border-0 p-0 overflow-hidden shadow-sm">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="bg-light">
            <tr>
              <th class="ps-4">Hình ảnh</th>
              <th>Tên món ăn</th>
              <th>Danh mục</th>
              <th>Đơn giá</th>
              <th>Trạng thái</th>
              <th class="text-end pe-4">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="dish in filteredDishes" :key="dish.id">
              <td class="ps-4">
                <img 
                  :src="dish.imageUrl || '/favicon.svg'" 
                  :alt="dish.name" 
                  class="rounded-3 object-fit-cover bg-light"
                  style="width: 54px; height: 54px;"
                  @error="(e) => e.target.src = '/favicon.svg'"
                />
              </td>
              <td>
                <div class="fw-bold text-dark">{{ dish.name }}</div>
                <div class="text-muted small text-truncate" style="max-width: 250px;">{{ dish.description }}</div>
              </td>
              <td>
                <span class="badge bg-light text-primary border rounded-pill px-3 py-1">
                  {{ dish.categoryName || 'Chưa phân loại' }}
                </span>
              </td>
              <td class="fw-extrabold text-primary">{{ formatVND(dish.price) }}</td>
              <td>
                <span :class="['badge rounded-pill px-3 py-2', dish.isAvailable ? 'badge-soft-success' : 'badge-soft-danger']">
                  {{ dish.isAvailable ? 'Đang Phục Vụ' : 'Hết Hàng' }}
                </span>
              </td>
              <td class="text-end pe-4">
                <button class="btn btn-sm btn-light text-primary me-2 rounded-2" @click="openEditModal(dish)">
                  <i class="bi bi-pencil-square me-1"></i> Sửa
                </button>
                <button class="btn btn-sm btn-light text-danger rounded-2" @click="deleteDish(dish.id)">
                  <i class="bi bi-trash me-1"></i> Xóa
                </button>
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
      :current-count="dishes.length"
      item-label="món ăn"
      @change-page="handlePageChange"
    />

    <!-- Modal Form -->
    <div class="modal fade" id="dishModal" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header">
            <h5 class="modal-title fw-bold text-primary">
              <i class="bi bi-egg-fried me-2"></i> {{ isEdit ? 'Cập Nhật Món Ăn' : 'Thêm Món Ăn Mới' }}
            </h5>
            <button type="button" class="btn-close shadow-none" data-bs-dismiss="modal"></button>
          </div>

          <form @submit.prevent="saveDish">
            <div class="modal-body p-4">
              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label fw-bold small text-muted">Tên món ăn (*)</label>
                  <input 
                    type="text" 
                    class="form-control" 
                    placeholder="Ví dụ: Cơm sườn nướng, Phở bò..."
                    v-model="form.name"
                    required
                  />
                  <div v-if="fieldErrors.Name" class="invalid-feedback d-block small mt-1">
                    <i class="bi bi-exclamation-circle me-1"></i> {{ fieldErrors.Name }}
                  </div>
                </div>

                <div class="col-md-6">
                  <label class="form-label fw-bold small text-muted">Danh mục (*)</label>
                  <select class="form-select" v-model="form.categoryId" required>
                    <option :value="null" disabled>-- Chọn danh mục --</option>
                    <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                      {{ cat.name }}
                    </option>
                  </select>
                </div>

                <div class="col-md-6">
                  <label class="form-label fw-bold small text-muted">Đơn giá (VNĐ) (*)</label>
                  <input 
                    type="number" 
                    class="form-control" 
                    placeholder="Ví dụ: 35000"
                    min="0"
                    step="1000"
                    v-model.number="form.price"
                    required
                  />
                </div>

                <!-- KHUNG UPLOAD HÌNH ẢNH TRONG MODAL (Bổ sung sau)-->
                <div class="col-md-6">
                  <label class="form-label fw-bold small text-muted">Hình ảnh món ăn (*)</label>
                  
                  <div class="d-flex align-items-center gap-3">
                    <!-- Khung xem trước ảnh preview -->
                    <div class="position-relative border rounded-3 bg-light d-flex align-items-center justify-content-center overflow-hidden" style="width: 80px; height: 80px; min-width: 80px;">
                      <img 
                        v-if="imagePreview || form.imageUrl" 
                        :src="imagePreview || form.imageUrl" 
                        class="w-100 h-100 object-fit-cover" 
                        alt="Preview" 
                      />
                      <i v-else class="bi bi-image text-muted fs-3"></i>
                      
                      <!-- Spinner khi đang upload ảnh lên Cloudinary -->
                      <div v-if="uploadingImage" class="position-absolute bg-dark bg-opacity-50 w-100 h-100 d-flex align-items-center justify-content-center">
                        <div class="spinner-border spinner-border-sm text-white"></div>
                      </div>
                    </div>

                    <!-- Nút chọn tệp từ máy tính -->
                    <div class="flex-grow-1">
                      <input 
                        type="file" 
                        class="form-control" 
                        accept="image/png, image/jpeg, image/webp" 
                        @change="handleFileChange" 
                      />
                      <div class="text-muted" style="font-size: 11px; margin-top: 4px;">
                        Hỗ trợ JPG, PNG, WEBP (Tối đa 5MB)
                      </div>
                    </div>
                  </div>

                  <div v-if="fieldErrors.imageUrl" class="invalid-feedback d-block small mt-1">
                    <i class="bi bi-exclamation-circle me-1"></i> {{ fieldErrors.imageUrl }}
                  </div>
                </div>

                <div class="col-12">
                  <label class="form-label fw-bold small text-muted">Mô tả món ăn</label>
                  <textarea 
                    class="form-control" 
                    rows="3" 
                    placeholder="Ghi chú thành phần, hương vị..."
                    v-model="form.description"
                  ></textarea>
                </div>

                <div class="col-12">
                  <div class="form-check form-switch">
                    <input 
                      class="form-check-input" 
                      type="checkbox" 
                      role="switch" 
                      id="availableCheck"
                      v-model="form.isAvailable"
                    />
                    <label class="form-check-label fw-bold text-dark" for="availableCheck">
                      Món ăn đang mở bán (Còn hàng)
                    </label>
                  </div>
                </div>
              </div>

              <div v-if="errorMsg" class="alert alert-danger py-2 small mt-3 mb-0">
                {{ errorMsg }}
              </div>
            </div>

            <div class="modal-footer bg-light">
              <button type="button" class="btn btn-light rounded-pill px-4" data-bs-dismiss="modal">Hủy</button>
              <button type="submit" class="btn btn-canteen-primary rounded-pill px-4" :disabled="saving">
                <span v-if="saving" class="spinner-border spinner-border-sm me-1"></span>
                Lưu Món Ăn
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </DefaultLayout>
</template>

<script setup>
import { ref, reactive, computed, onMounted , watch} from 'vue';
import DefaultLayout from '../../layouts/DefaultLayout.vue';
import Pagination from '../../components/Pagination.vue';
import api from '../../api/axios';

const dishes = ref([]);
const categories = ref([]);
const loading = ref(true);
const saving = ref(false);
const isEdit = ref(false);
const editingId = ref(null);
const searchQuery = ref('');
const selectedCategoryId = ref(null);
const errorMsg = ref('');
const fieldErrors = ref({}); // Validation errors for each field
// Image upload state
const selectedFile = ref(null);
const imagePreview = ref('');
const uploadingImage = ref(false);

// Pagination state
const currentPage = ref(1);
const limit = ref(10);
const totalPages = ref(1);
const totalElements = ref(0);

const form = reactive({
  name: '',
  description: '',
  price: 30000,
  categoryId: null,
  imageUrl: '',
  isAvailable: true
});

onMounted(async () => {
  await Promise.all([fetchCategories(), fetchDishes()]);
});

const fetchCategories = async () => {
  try {
    const res = await api.get('/api/admin/category');
    const data = res.data.data;
    categories.value = Array.isArray(data) ? data : (data.content || []);
  } catch (e) {
    console.error('Error fetching categories:', e);
  }
};

let searchTimeout = null;
watch([selectedCategoryId, searchQuery], () => {
  currentPage.value = 1;
  clearTimeout(searchTimeout);
  searchTimeout = setTimeout(() => {
    fetchDishes();
  }, 300);
});

const fetchDishes = async () => {
  loading.value = true;
  try {
    const res = await api.get('/api/admin/dish', {
      params: {
        page: currentPage.value,
        limit: limit.value,
        categoryId: selectedCategoryId.value || undefined,
        search: searchQuery.value?.trim() || undefined
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
    console.error('Error fetching dishes:', e);
  } finally {
    loading.value = false;
  }
};

const handlePageChange = (page) => {
  currentPage.value = page;
  fetchDishes();
  window.scrollTo({ top: 0, behavior: 'smooth' });
};


const filteredDishes = computed(() => dishes.value);

const openCreateModal = () => {
  isEdit.value = false;
  editingId.value = null;
  form.name = '';
  form.description = '';
  form.price = 30000;
  form.categoryId = categories.value.length > 0 ? categories.value[0].id : null;
  form.imageUrl = '';
  form.isAvailable = true;
  selectedFile.value = null;
  imagePreview.value = '';
  errorMsg.value = '';
  showModal();
};

const openEditModal = (dish) => {
  isEdit.value = true;
  editingId.value = dish.id;
  form.name = dish.name;
  form.description = dish.description;
  form.price = dish.price;
  form.categoryId = dish.categoryId;
  form.imageUrl = dish.imageUrl || '';
  form.isAvailable = dish.isAvailable !== false;
  selectedFile.value = null;
  imagePreview.value = dish.imageUrl || '';
  errorMsg.value = '';
  showModal();
};

// Cập nhật hàm saveDish: Tải ảnh lên Cloudinary trước, sau đó lưu món ăn
const saveDish = async () => {
  saving.value = true;
  errorMsg.value = '';
  try {
    // 1. Nếu người dùng có chọn tệp ảnh mới, tải lên Cloudinary trước:
    if (selectedFile.value) {
      uploadingImage.value = true;
      const formData = new FormData();
      formData.append('file', selectedFile.value);
      const uploadRes = await api.post('/api/admin/upload/img', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      // Gán URL trả về từ Cloudinary vào form
      form.imageUrl = uploadRes.data.url;
      uploadingImage.value = false;
    }
    if (!form.imageUrl) {
      errorMsg.value = 'Vui lòng chọn hình ảnh minh họa cho món ăn!';
      saving.value = false;
      return;
    }
    // 2. Gửi request lưu món ăn như bình thường (chỉ lưu chuỗi URL)
    if (isEdit.value) {
      await api.put(`/api/admin/dish/${editingId.value}`, form);
    } else {
      await api.post('/api/admin/dish', form);
    }
    hideModal();
    await fetchDishes();
  } catch (e) {
    errorMsg.value = e.customMessage || 'Lỗi khi lưu thông tin món ăn!';
    if (e.fieldErrors) {
      fieldErrors.value = e.fieldErrors;
    }
  } finally {
    saving.value = false;
    uploadingImage.value = false;
  }
};

const deleteDish = async (id) => {
  if (!confirm('Bạn có chắc chắn muốn xóa món ăn này không?')) return;
  try {
    await api.delete(`/api/admin/dish/${id}`);
    await fetchDishes();
  } catch (e) {
    alert(e.customMessage || 'Không thể xóa món ăn!');
  }
};

const showModal = () => {
  const el = document.getElementById('dishModal');
  if (el && window.bootstrap) {
    const modal = window.bootstrap.Modal.getOrCreateInstance(el);
    modal.show();
  }
};

const hideModal = () => {
  const el = document.getElementById('dishModal');
  if (el && window.bootstrap) {
    const modal = window.bootstrap.Modal.getInstance(el);
    if (modal) modal.hide();
  }
};

const handleFileChange = (event) => {
  const file = event.target.files[0];
  if (!file) return;

  // Validate file type and size
  const validTypes = ['image/jpeg', 'image/png', 'image/webp'];
  if (!validTypes.includes(file.type)) {
    fieldErrors.value.imageUrl = 'Chỉ chấp nhận định dạng JPG, PNG, WEBP.';
    return;
  }
  if (file.size > 5 * 1024 * 1024) { // 5MB
    fieldErrors.value.imageUrl = 'Kích thước ảnh không được vượt quá 5MB.';
    return;
  }

  selectedFile.value = file;
  imagePreview.value = URL.createObjectURL(file);
  fieldErrors.value.imageUrl = '';
};

const formatVND = (amount) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount || 0);
};
</script>
