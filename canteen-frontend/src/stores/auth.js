import { defineStore } from 'pinia';
import api from '../api/axios';

// Auth store manages user authentication state, including login, logout, registration, and fetching the current user.
// Be used by components to check if the user is authenticated, their role, and balance.
// ie. LoginView, RegisterView, Navbar, ProfileView, etc.
const getStoredUser = () => {
  try {
    const raw = localStorage.getItem('canteen_user');
    return raw ? JSON.parse(raw) : null;
  } catch (e) {
    localStorage.removeItem('canteen_user');
    return null;
  }
};

export const useAuthStore = defineStore('auth', { // Use Options API for better readability and maintainability
  state: () => ({
    user: getStoredUser(),
    loading: false,
    error: null,
  }),
  getters: {
    isAuthenticated: (state) => !!state.user,
    userRole: (state) => state.user?.role || null,
    isCustomer: (state) => state.user?.role === 'ROLE_CUSTOMER' || state.user?.role === 'CUSTOMER',
    isStaff: (state) => state.user?.role === 'ROLE_STAFF' || state.user?.role === 'STAFF' || state.user?.role === 'ROLE_ADMIN' || state.user?.role === 'ADMIN',
    isAdmin: (state) => state.user?.role === 'ROLE_ADMIN' || state.user?.role === 'ADMIN',
    userBalance: (state) => state.user?.balance || 0,
  },
  actions: {
    async login(credentials) {
      this.loading = true;
      this.error = null;
      try {
        const response = await api.post('/api/auth/login', credentials);
        // Login endpoint returns UserDTO directly or message
        // Because of the backend's response structure, we check if the data is nested under 'data' or directly in the response.
        // Backend use ResponseEntity.ok(ResponseObject) which wraps the actual data in a 'data' field, but some endpoints might return the data directly.
        const userData = response.data.data || response.data;
        this.user = userData;
        localStorage.setItem('canteen_user', JSON.stringify(userData));
        return userData;
      } catch (err) {
        this.error = err.customMessage || 'Đăng nhập thất bại!';
        throw err;
      } finally {
        this.loading = false;
      }
    },

    async register(payload) {
      this.loading = true;
      this.error = null;
      try {
        const response = await api.post('/api/auth/register', payload);
        return response.data;
      } catch (err) {
        this.error = err.customMessage || 'Đăng ký thất bại!';
        throw err;
      } finally {
        this.loading = false;
      }
    },

    async fetchCurrentUser() {
      try {
        const response = await api.get('/api/auth/me');
        const userData = response.data.data || response.data;
        this.user = userData;
        localStorage.setItem('canteen_user', JSON.stringify(userData));
        return userData;
      } catch (err) {
        // Session expired or not logged in
        this.user = null;
        localStorage.removeItem('canteen_user');
        return null;
      }
    },

    async logout() {
      try {
        await api.post('/api/auth/logout');
      } catch (e) {
        console.warn('Logout endpoint warning:', e);
      } finally {
        this.user = null;
        localStorage.removeItem('canteen_user');
      }
    },

    setBalance(newBalance) {
      if (this.user) {
        this.user.balance = newBalance;
        localStorage.setItem('canteen_user', JSON.stringify(this.user));
      }
    }
  },
});
