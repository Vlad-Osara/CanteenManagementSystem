<template>
  <DefaultLayout>
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between mb-4">
      <div>
        <h3 class="fw-extrabold text-primary mb-1">
          <i class="bi bi-tags me-2"></i> Quản Lý Danh Mục Món Ăn
        </h3>
        <p class="text-muted small mb-0">Thêm, sửa, xóa các danh mục thực đơn của căn tin</p>
      </div>

      <button class="btn btn-canteen-primary rounded-pill px-4 py-2 fw-semibold" @click="openCreateModal">
        <i class="bi bi-plus-lg me-1"></i> Thêm Danh Mục Mới
      </button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status"></div>
      <p class="text-muted mt-2">Đang tải danh sách danh mục...</p>
    </div>

    <!-- Table -->
    <div v-else class="card card-canteen border-0 p-0 overflow-hidden shadow-sm">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="bg-light">
            <tr>
              <th class="ps-4">Tên danh mục</th>
              <th>Mô tả</th>
              <th>Ngày tạo</th>
              <th class="text-end pe-4">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="cat in categories" :key="cat.id">
              <td class="ps-4 fw-bold text-dark">
                <i class="bi bi-tag-fill me-2 text-primary"></i> {{ cat.name }}
              </td>
              <td class="text-muted small">{{ cat.description || 'Không có mô tả' }}</td>
              <td class="text-muted small">{{ formatDate(cat.createdAt) }}</td>
              <td class="text-end pe-4">
                <button class="btn btn-sm btn-light text-primary me-2 rounded-2" @click="openEditModal(cat)">
                  <i class="bi bi-pencil-square me-1"></i> Sửa
                </button>
                <button class="btn btn-sm btn-light text-danger rounded-2" @click="deleteCategory(cat.id)">
                  <i class="bi bi-trash me-1"></i> Xóa
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal Form -->
    <div class="modal fade" id="categoryModal" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header">
            <h5 class="modal-title fw-bold text-primary">
              <i class="bi bi-tag-fill me-2"></i> {{ isEdit ? 'Cập Nhật Danh Mục' : 'Thêm Danh Mục Mới' }}
            </h5>
            <button type="button" class="btn-close shadow-none" data-bs-dismiss="modal"></button>
          </div>

          <form @submit.prevent="saveCategory">
            <div class="modal-body p-4">
              <div class="mb-3">
                <label class="form-label fw-bold small text-muted">Tên danh mục (*)</label>
                <input 
                  type="text" 
                  class="form-control" 
                  placeholder="Ví dụ: Món Sáng, Cơm Trưa, Nước Uống..."
                  v-model="form.name"
                  required
                />
                <div v-if="fieldErrors.Name" class="invalid-feedback d-block small mt-1">
                  <i class="bi bi-exclamation-circle me-1"></i> {{ fieldErrors.Name }}
                </div>
              </div>

              <div class="mb-3">
                <label class="form-label fw-bold small text-muted">Mô tả danh mục</label>
                <textarea 
                  class="form-control" 
                  rows="3" 
                  placeholder="Nhập mô tả ngắn gọn..."
                  v-model="form.description"
                ></textarea>
              </div>

              <div v-if="errorMsg" class="alert alert-danger py-2 small mb-0">
                {{ errorMsg }}
              </div>
            </div>

            <div class="modal-footer bg-light">
              <button type="button" class="btn btn-light rounded-pill px-4" data-bs-dismiss="modal">Hủy</button>
              <button type="submit" class="btn btn-canteen-primary rounded-pill px-4" :disabled="saving">
                <span v-if="saving" class="spinner-border spinner-border-sm me-1"></span>
                Lưu Danh Mục
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </DefaultLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import DefaultLayout from '../../layouts/DefaultLayout.vue';
import api from '../../api/axios';

const categories = ref([]);
const loading = ref(true);
const saving = ref(false);
const isEdit = ref(false);
const editingId = ref(null);
const errorMsg = ref('');
const fieldErrors = ref({}); // Validation errors for each field

const form = reactive({
  name: '',
  description: ''
});

onMounted(() => {
  fetchCategories();
});

const fetchCategories = async () => {
  loading.value = true;
  try {
    const res = await api.get('/api/admin/category');
    const data = res.data.data;
    categories.value = Array.isArray(data) ? data : (data.content || []);
  } catch (e) {
    console.error('Error loading admin categories:', e);
  } finally {
    loading.value = false;
  }
};

const openCreateModal = () => {
  isEdit.value = false;
  editingId.value = null;
  form.name = '';
  form.description = '';
  errorMsg.value = '';
  showModal();
};

const openEditModal = (cat) => {
  isEdit.value = true;
  editingId.value = cat.id;
  form.name = cat.name;
  form.description = cat.description;
  errorMsg.value = '';
  showModal();
};

const saveCategory = async () => {
  saving.value = true;
  errorMsg.value = '';
  try {
    if (isEdit.value) {
      await api.put(`/api/admin/category/${editingId.value}`, form);
    } else {
      await api.post('/api/admin/category', form);
    }
    hideModal();
    await fetchCategories();
  } catch (e) {
    errorMsg.value = e.customMessage || 'Lỗi khi lưu danh mục!';
    if (e.fieldErrors) {
      fieldErrors.value = e.fieldErrors;
    }
  } finally {
    saving.value = false;
  }
};

const deleteCategory = async (id) => {
  if (!confirm('Bạn có chắc chắn muốn xóa danh mục này không?')) return;
  try {
    await api.delete(`/api/admin/category/${id}`);
    await fetchCategories();
  } catch (e) {
    alert(e.customMessage || 'Không thể xóa danh mục!');
  }
};

const showModal = () => {
  const el = document.getElementById('categoryModal');
  if (el && window.bootstrap) {
    const modal = window.bootstrap.Modal.getOrCreateInstance(el);
    modal.show();
  }
};

const hideModal = () => {
  const el = document.getElementById('categoryModal');
  if (el && window.bootstrap) {
    const modal = window.bootstrap.Modal.getInstance(el);
    if (modal) modal.hide();
  }
};

const formatDate = (dateStr) => {
  if (!dateStr) return 'N/A';
  return new Date(dateStr).toLocaleDateString('vi-VN');
};
</script>
