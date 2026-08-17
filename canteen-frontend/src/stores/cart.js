import { defineStore } from 'pinia';
import api from '../api/axios';
import { useAuthStore } from './auth';

// Cart store manages the shopping cart state, including adding, updating, and removing items, as well as handling checkout.
// Be used by components like CartView, Navbar, CheckoutView, etc.
const getStoredCart = () => {
  try {
    const raw = localStorage.getItem('canteen_cart');
    return raw ? JSON.parse(raw) : [];
  } catch (e) {
    localStorage.removeItem('canteen_cart');
    return [];
  }
};

export const useCartStore = defineStore('cart', { // Use Options API for better readability and maintainability
  state: () => ({
    items: getStoredCart(),
    orderType: 'DINE_IN', // DINE_IN | TAKE_AWAY
    note: '',
    loading: false,
    error: null,
  }),
  getters: {
    totalItems: (state) => state.items.reduce((sum, item) => sum + item.quantity, 0),
    totalPrice: (state) => state.items.reduce((sum, item) => sum + (item.dish.price * item.quantity), 0),
  },
  actions: {
    // Save the current cart state to localStorage to persist it across page reloads and sessions.
    saveToStorage() {
      localStorage.setItem('canteen_cart', JSON.stringify(this.items));
    },

    addToCart(dish, quantity = 1) {
      const existing = this.items.find(i => i.dish.id === dish.id);
      if (existing) {
        existing.quantity += quantity;
      } else {
        this.items.push({
          dish,
          quantity
        });
      }
      this.saveToStorage();
    },

    updateQuantity(dishId, quantity) {
      if (quantity <= 0) {
        this.removeFromCart(dishId);
        return;
      }
      const item = this.items.find(i => i.dish.id === dishId);
      if (item) {
        item.quantity = quantity;
        this.saveToStorage();
      }
    },

    removeFromCart(dishId) {
      this.items = this.items.filter(i => i.dish.id !== dishId);
      this.saveToStorage();
    },

    clearCart() {
      this.items = [];
      this.note = '';
      this.saveToStorage();
    },

    // Checkout action sends the current cart items to the backend to create an order.
    // It also fetches the updated user info to reflect any changes in balance after the order is placed.
    // Use asynchronously to handle the checkout process and catch any errors that may occur during the API request.
    async checkout(confirmPassword) {
      if (this.items.length === 0) {
        throw new Error('Giỏ hàng của bạn đang trống!');
      }

      this.loading = true;
      this.error = null;

      try {
        const payload = {
          type: this.orderType,
          note: this.note,
          confirmPassword: confirmPassword,
          items: this.items.map(item => ({
            dishId: item.dish.id,
            quantity: item.quantity
          }))
        };

        // Use await to call the API and wait for the response before proceeding.
        // Async + await ensures that the checkout process is completed before moving on to the next steps, such as clearing the cart and updating the user info.
        const response = await api.post('/api/customer/order', payload);
        const createdOrder = response.data.data || response.data;

        // Fetch updated user info to get updated balance
        const authStore = useAuthStore();
        await authStore.fetchCurrentUser();

        this.clearCart();
        return createdOrder;
      } catch (err) {
        this.error = err.customMessage || 'Đặt đơn hàng thất bại!';
        throw err;
      } finally {
        this.loading = false;
      }
    }
  }
});
