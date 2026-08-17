import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '../stores/auth';

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/LoginView.vue'),
    meta: { guestOnly: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/auth/RegisterView.vue'),
    meta: { guestOnly: true }
  },
  {
  path: '/forgot-password',
  name: 'ForgotPassword',
  component: () => import('../views/auth/ForgotPasswordView.vue'),
  meta: { guestOnly: true }
},
  // Customer Routes
  {
    path: '/',
    name: 'Menu',
    component: () => import('../views/customer/MenuView.vue'),
    meta: { title: 'Thực Đơn Căn Tin' }
  },
  {
    path: '/orders',
    name: 'CustomerOrders',
    component: () => import('../views/customer/OrdersView.vue'),
    meta: { requiresAuth: true, roles: ['ROLE_CUSTOMER', 'CUSTOMER'] }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/customer/ProfileView.vue'),
    meta: { requiresAuth: true }
  },
  // Staff Routes
  {
    path: '/staff/orders',
    name: 'StaffOrders',
    component: () => import('../views/staff/StaffOrdersView.vue'),
    meta: { requiresAuth: true, roles: ['ROLE_STAFF', 'STAFF', 'ROLE_ADMIN', 'ADMIN'] }
  },
  {
    path: '/staff/dishes',
    name: 'StaffDishes',
    component: () => import('../views/staff/StaffDishesView.vue'),
    meta: { requiresAuth: true, roles: ['ROLE_STAFF', 'STAFF', 'ROLE_ADMIN', 'ADMIN'] }
  },
  // Admin Routes
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: () => import('../views/admin/AdminDashboard.vue'),
    meta: { requiresAuth: true, roles: ['ROLE_ADMIN', 'ADMIN'] }
  },
  {
    path: '/admin/categories',
    name: 'ManageCategories',
    component: () => import('../views/admin/ManageCategoriesView.vue'),
    meta: { requiresAuth: true, roles: ['ROLE_ADMIN', 'ADMIN'] }
  },
  {
    path: '/admin/dishes',
    name: 'ManageDishes',
    component: () => import('../views/admin/ManageDishesView.vue'),
    meta: { requiresAuth: true, roles: ['ROLE_ADMIN', 'ADMIN'] }
  },
  {
    path: '/admin/accounts',
    name: 'ManageAccounts',
    component: () => import('../views/admin/ManageAccountsView.vue'),
    meta: { requiresAuth: true, roles: ['ROLE_ADMIN', 'ADMIN'] }
  },
  // Fallback
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 };
  }
});

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();
  const user = authStore.user;

  if (to.meta.requiresAuth && !user) {
    return next({ name: 'Login', query: { redirect: to.fullPath } });
  }

  if (to.meta.guestOnly && user) {
    return next({ name: 'Menu' });
  }

  if (to.meta.roles && user) {
    const userRole = user.role;
    const hasRole = to.meta.roles.includes(userRole);
    if (!hasRole) {
      return next({ name: 'Menu' });
    }
  }

  next();
});

export default router;
