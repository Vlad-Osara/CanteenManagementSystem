<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue';
import * as bootstrap from 'bootstrap';

onMounted(() => {
  window.bootstrap = bootstrap;
});
</script>
