import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from '@/views/DashboardView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'dashboard',
      component: DashboardView,
    },
    // 첫 화면에 필요 없는 코드는 나눠서 나중에 받는다 (lazy loading)
    {
      path: '/courses',
      name: 'course-list',
      component: () => import('@/views/CourseListView.vue'),
    },
    {
      path: '/courses/new',
      name: 'course-create',
      component: () => import('@/views/CourseFormView.vue'),
    },
    {
      path: '/courses/:courseId',
      name: 'course-detail',
      component: () => import('@/views/CourseDetailView.vue'),
    },
    {
      // 등록과 같은 폼을 쓴다 — courseId 유무로 모드가 갈린다
      path: '/courses/:courseId/edit',
      name: 'course-edit',
      component: () => import('@/views/CourseFormView.vue'),
    },
    {
      path: '/assignments',
      name: 'assignment-list',
      component: () => import('@/views/AssignmentListView.vue'),
    },
    {
      path: '/dev',
      name: 'dev-overview',
      component: () => import('@/views/DevOverviewView.vue'),
    },
    {
      path: '/dev/logs',
      name: 'dev-logs',
      component: () => import('@/views/LogView.vue'),
    },
  ],
  scrollBehavior: () => ({ top: 0 }),
})

export default router
