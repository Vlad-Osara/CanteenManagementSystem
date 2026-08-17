import { createApp } from 'vue';
import { createPinia, setActivePinia } from 'pinia';
import App from './App.vue';
import router from './router';
import './assets/main.css';
import * as bootstrap from 'bootstrap';

window.bootstrap = bootstrap;

const pinia = createPinia();
setActivePinia(pinia); 
const app = createApp(App);
app.use(pinia);
app.use(router);
app.mount('#app');
