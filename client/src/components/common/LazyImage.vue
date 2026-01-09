<template>
  <div ref="root" class="lazy-image-container" :style="containerStyle">
    <img
      v-if="isLoaded"
      :src="src"
      :srcset="srcset"
      :sizes="sizes"
      :alt="alt"
      :class="['lazy-image', { 'fade-in': isLoaded }]"
      @load="onLoad"
      @error="onError"
    />
    <div v-else class="skeleton"></div>
    <div v-if="hasError" class="error-placeholder">
      <span>이미지 로드 실패</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'

interface Props {
  src: string
  srcset?: string
  sizes?: string
  alt: string
  aspectRatio?: string  // 예: "16:9", "4:3", "1:1"
}

const props = withDefaults(defineProps<Props>(), {
  aspectRatio: '16:9'
})

const root = ref<HTMLDivElement>()
const isLoaded = ref(false)
const isVisible = ref(false)
const hasError = ref(false)

let observer: IntersectionObserver | null = null

// aspect ratio를 padding-bottom %로 변환
const containerStyle = computed(() => {
  if (!props.aspectRatio) return {}
  
  const [width, height] = props.aspectRatio.split(':').map(Number)
  const paddingBottom = (height / width) * 100
  
  return {
    paddingBottom: `${paddingBottom}%`
  }
})

onMounted(() => {
  if (!root.value) return
  
  // Intersection Observer로 뷰포트 진입 감지
  observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          isVisible.value = true
          observer?.disconnect()
        }
      })
    },
    {
      rootMargin: '50px' // 뷰포트 50px 전에 미리 로드
    }
  )
  
  observer.observe(root.value)
})

onUnmounted(() => {
  observer?.disconnect()
})

// 가시성이 true가 되면 이미지 로드
watch(isVisible, (visible) => {
  if (visible && !isLoaded.value) {
    isLoaded.value = true
  }
})

function onLoad() {
  isLoaded.value = true
  hasError.value = false
}

function onError() {
  console.error(`Failed to load image: ${props.src}`)
  hasError.value = true
  isLoaded.value = false
}
</script>

<style scoped>
.lazy-image-container {
  position: relative;
  overflow: hidden;
  background-color: var(--skeleton-base, #e0e0e0);
}

.lazy-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.skeleton {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    var(--skeleton-base, #e0e0e0) 25%,
    var(--skeleton-shine, #f0f0f0) 50%,
    var(--skeleton-base, #e0e0e0) 75%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

.fade-in {
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.error-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-center;
  background-color: var(--card-bg, #f5f5f5);
  color: var(--text-muted, #999);
  font-size: 0.875rem;
}
</style>
