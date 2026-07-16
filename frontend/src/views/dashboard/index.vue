<template>
  <div class="dashboard">
    <div class="welcome-card">
      <h2>欢迎回来，{{ userStore.userInfo.nickname || userStore.userInfo.username }} 👋</h2>
      <p>这是 Java-Admin 后台管理系统</p>
    </div>

    <el-row :gutter="20">
      <el-col :span="6">
        <div class="stat-card">
          <el-icon class="stat-icon" style="background: #e6f7ff; color: #1890ff"><User /></el-icon>
          <div class="stat-info">
            <div class="stat-label">用户总数</div>
            <div class="stat-value">{{ stats.userCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <el-icon class="stat-icon" style="background: #f6ffed; color: #52c41a"><UserFilled /></el-icon>
          <div class="stat-info">
            <div class="stat-label">今日活跃</div>
            <div class="stat-value">{{ stats.activeToday }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <el-icon class="stat-icon" style="background: #fff7e6; color: #fa8c16"><Key /></el-icon>
          <div class="stat-info">
            <div class="stat-label">角色数量</div>
            <div class="stat-value">{{ stats.roleCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <el-icon class="stat-icon" style="background: #f9f0ff; color: #722ed1"><Setting /></el-icon>
          <div class="stat-info">
            <div class="stat-label">系统状态</div>
            <div class="stat-value" style="color: #52c41a">正常</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 20px">
      <template #header>
        <span>系统信息</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="系统名称">Java-Admin 后台管理系统</el-descriptions-item>
        <el-descriptions-item label="后端技术">Spring Boot 3 + MyBatis-Plus + JWT</el-descriptions-item>
        <el-descriptions-item label="前端技术">Vue 3 + Vite + Element Plus + Pinia</el-descriptions-item>
        <el-descriptions-item label="数据库">MySQL 8.x</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { User, UserFilled, Key, Setting } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { getUserPage } from '@/api/user'

const userStore = useUserStore()
const stats = ref({
  userCount: 0,
  activeToday: 0,
  roleCount: 2
})

onMounted(async () => {
  try {
    const res = await getUserPage({ pageNum: 1, pageSize: 1 })
    stats.value.userCount = res.data.total
    stats.value.activeToday = res.data.total
  } catch (e) {
    // ignore
  }
})
</script>

<style lang="scss" scoped>
.dashboard {
  .welcome-card {
    background: linear-gradient(135deg, #1e3c72, #2a5298);
    color: #fff;
    padding: 30px;
    border-radius: 8px;
    margin-bottom: 20px;

    h2 {
      font-size: 22px;
      margin-bottom: 8px;
    }

    p {
      opacity: 0.85;
      font-size: 14px;
    }
  }

  .stat-card {
    background: #fff;
    padding: 20px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    gap: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

    .stat-icon {
      width: 48px;
      height: 48px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
    }

    .stat-info {
      .stat-label {
        font-size: 14px;
        color: #909399;
        margin-bottom: 4px;
      }

      .stat-value {
        font-size: 24px;
        font-weight: bold;
        color: #303133;
      }
    }
  }
}
</style>
