<script setup>
import { ref, onMounted, computed } from 'vue'

// Username state
const username = 'yeojisun'

// API states
const profile = ref({
  avatar_url: '',
  name: 'Yeo Ji Sun (Yessi)',
  bio: 'Java & Frontend Developer passionate about building robust backend APIs and gorgeous, interactive web interfaces.',
  public_repos: 0,
  html_url: 'https://github.com/yeojisun'
})
const repos = ref([])
const loading = ref(true)
const error = ref(false)

// Form states
const formSubmitted = ref(false)
const formError = ref(false)
const submitting = ref(false)
const contactForm = ref({
  name: '',
  email: '',
  message: ''
})

// Search and filter states
const searchQuery = ref('')
const activeFilter = ref('All')
const sortBy = ref('updated') // 'updated' or 'stars'

// Fallback mock repositories if rate-limited or offline
const fallbackRepos = [
  {
    id: 1,
    name: 'toy-store-backend',
    description: 'Spring Boot, Spring Security, JPA, and PostgreSQL-based API service for managing and processing orders in a toy store application.',
    stargazers_count: 8,
    forks_count: 2,
    language: 'Java',
    html_url: 'https://github.com/yeojisun/toy-store-backend',
    topics: ['spring-boot', 'jpa', 'postgresql', 'java', 'security'],
    updated_at: '2026-06-15T08:30:00Z'
  },
  {
    id: 2,
    name: 'realtime-chat-app',
    description: 'A responsive real-time messaging application with Vue 3, CSS Grid/Flexbox, WebSocket, and Redis caching layers.',
    stargazers_count: 14,
    forks_count: 4,
    language: 'Vue',
    html_url: 'https://github.com/yeojisun/realtime-chat-app',
    topics: ['vue', 'javascript', 'websocket', 'redis', 'glassmorphism'],
    updated_at: '2026-06-20T12:00:00Z'
  },
  {
    id: 3,
    name: 'yessi-portfolio',
    description: 'My personal developer portfolio containing active GitHub repositories, dynamic filters, and clean responsive CSS.',
    stargazers_count: 5,
    forks_count: 1,
    language: 'Vue',
    html_url: 'https://github.com/yeojisun/yessi-portfolio',
    topics: ['vue', 'java', 'vite', 'netlify-forms', 'css-aurora'],
    updated_at: '2026-06-22T10:00:00Z'
  },
  {
    id: 4,
    name: 'batch-processing-service',
    description: 'Spring Batch scheduler project compiling raw sales records into metrics tables, leveraging MySQL and cron triggers.',
    stargazers_count: 6,
    forks_count: 1,
    language: 'Java',
    html_url: 'https://github.com/yeojisun/batch-processing-service',
    topics: ['spring-batch', 'java', 'mysql', 'scheduler'],
    updated_at: '2026-05-18T14:40:00Z'
  }
]

// Tech Stack Categories
const skills = [
  { name: 'Java', icon: '☕', desc: 'Robust enterprise application and API development using Spring ecosystem.', category: 'Backend' },
  { name: 'Spring Boot', icon: '🍃', desc: 'Microservices, REST controllers, Spring Security, and Spring Data JPA.', category: 'Backend' },
  { name: 'Vue.js 3', icon: '💚', desc: 'Interactive SPA development with Vite, Vue Router, Composition API.', category: 'Frontend' },
  { name: 'JavaScript (ES6+)', icon: '💛', desc: 'Dynamic logic, DOM operations, asynchronous request handling.', category: 'Frontend' },
  { name: 'RDB / SQL', icon: '💾', desc: 'Database schema design and query optimization using H2, MySQL, PostgreSQL.', category: 'Database' },
  { name: 'Git & GitHub', icon: '🐙', desc: 'Collaborative development, version tracking, CI/CD pipeline structures.', category: 'Tools' }
]

// Fetch data on mount
onMounted(async () => {
  try {
    // 1. Fetch GitHub User Profile
    const profileRes = await fetch(`https://api.github.com/users/${username}`)
    if (profileRes.ok) {
      const data = await profileRes.json()
      profile.value = {
        avatar_url: data.avatar_url,
        name: data.name || 'Yeo Ji Sun',
        bio: data.bio || profile.value.bio,
        public_repos: data.public_repos,
        html_url: data.html_url
      }
    }

    // 2. Fetch Repositories
    const reposRes = await fetch(`https://api.github.com/users/${username}/repos?per_page=100`)
    if (reposRes.ok) {
      const data = await reposRes.json()
      // Filter out forks if preferred
      repos.value = data.filter(repo => !repo.fork)
    } else {
      // Fallback if API fails (rate limits)
      repos.value = fallbackRepos
    }
  } catch (err) {
    console.error('Error fetching GitHub data:', err)
    error.value = true
    repos.value = fallbackRepos
  } finally {
    loading.value = false
  }
})

// Star count calculator
const totalStars = computed(() => {
  return repos.value.reduce((sum, repo) => sum + (repo.stargazers_count || 0), 0)
})

// Fork count calculator
const totalForks = computed(() => {
  return repos.value.reduce((sum, repo) => sum + (repo.forks_count || 0), 0)
})

// Language badges mapper colors
const getLanguageColor = (lang) => {
  const colors = {
    Java: '#b07219',
    Vue: '#41b883',
    JavaScript: '#f1e05a',
    HTML: '#e34c26',
    CSS: '#563d7c',
    TypeScript: '#3178c6',
    Python: '#3572A5'
  }
  return colors[lang] || '#8e8e8e'
}

// Search and Filter Repositories logic
const filteredRepos = computed(() => {
  let result = [...repos.value]

  // Filter by search query (name or description)
  if (searchQuery.value.trim() !== '') {
    const q = searchQuery.value.toLowerCase()
    result = result.filter(repo => 
      (repo.name && repo.name.toLowerCase().includes(q)) || 
      (repo.description && repo.description.toLowerCase().includes(q))
    )
  }

  // Filter by Category tabs
  if (activeFilter.value !== 'All') {
    if (activeFilter.value === 'Java') {
      result = result.filter(repo => repo.language === 'Java')
    } else if (activeFilter.value === 'Frontend') {
      result = result.filter(repo => repo.language === 'Vue' || repo.language === 'JavaScript' || repo.language === 'HTML' || repo.language === 'CSS' || repo.language === 'TypeScript')
    } else {
      // Other
      result = result.filter(repo => repo.language !== 'Java' && repo.language !== 'Vue' && repo.language !== 'JavaScript' && repo.language !== 'HTML' && repo.language !== 'CSS' && repo.language !== 'TypeScript')
    }
  }

  // Sort logic
  if (sortBy.value === 'updated') {
    result.sort((a, b) => new Date(b.updated_at) - new Date(a.updated_at))
  } else if (sortBy.value === 'stars') {
    result.sort((a, b) => (b.stargazers_count || 0) - (a.stargazers_count || 0))
  }

  return result
})

// Netlify form submission AJAX handler
const handleSubmit = async () => {
  submitting.value = true
  formError.value = false
  formSubmitted.value = false

  try {
    const body = new URLSearchParams()
    body.append('form-name', 'contact')
    body.append('name', contactForm.value.name)
    body.append('email', contactForm.value.email)
    body.append('message', contactForm.value.message)

    const response = await fetch('/', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: body.toString()
    })

    if (response.ok) {
      formSubmitted.value = true
      contactForm.value = { name: '', email: '', message: '' }
    } else {
      throw new Error('Netlify form submission failed')
    }
  } catch (err) {
    console.error(err)
    formError.value = true
  } finally {
    submitting.value = false
  }
}

// Date formatter helper
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' })
}
</script>

<template>
  <div class="aurora-bg">
    <div class="blob blob-1"></div>
    <div class="blob blob-2"></div>
    <div class="blob blob-3"></div>
  </div>

  <div class="app-container">
    <!-- Navigation -->
    <nav class="navbar">
      <div class="logo-text">yessi.dev</div>
      <ul class="nav-links">
        <li><a href="#about">About</a></li>
        <li><a href="#skills">Skills</a></li>
        <li><a href="#projects">Projects</a></li>
        <li><a href="#contact">Contact</a></li>
      </ul>
    </nav>

    <!-- Hero / Profile -->
    <header id="about" class="hero glass-panel">
      <div class="hero-content">
        <span class="badge">AVAILABLE FOR WORK</span>
        <h1 class="hero-title">안녕하세요, <br><span>여지선(Yessi)</span>입니다.</h1>
        <p class="hero-description">{{ profile.bio }}</p>
        <div class="social-links">
          <a href="#projects" class="btn btn-primary">
            프로젝트 보기
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14M5 12h14"/></svg>
          </a>
          <a :href="profile.html_url" target="_blank" rel="noopener noreferrer" class="btn btn-secondary">
            GitHub 프로필
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6M15 3h6v6M10 14L21 3"/></svg>
          </a>
        </div>
      </div>

      <div class="profile-container">
        <div class="avatar-wrapper">
          <img v-if="profile.avatar_url" :src="profile.avatar_url" alt="Yeo Ji Sun Profile" class="avatar-img">
          <div v-else class="profile-initials">YS</div>
        </div>
      </div>
    </header>

    <!-- Stats Dashboard -->
    <div class="stats-bar glass-panel">
      <div class="stat-item">
        <span class="stat-num">{{ repos.length }}</span>
        <span class="stat-label">Repositories</span>
      </div>
      <div class="stat-item">
        <span class="stat-num">{{ totalStars }}</span>
        <span class="stat-label">Total Stars</span>
      </div>
      <div class="stat-item">
        <span class="stat-num">{{ totalForks }}</span>
        <span class="stat-label">Forks</span>
      </div>
      <div class="stat-item">
        <span class="stat-num">Java & Vue</span>
        <span class="stat-label">Core Stacks</span>
      </div>
    </div>

    <!-- Skills Section -->
    <section id="skills">
      <div class="section-header">
        <p class="section-subtitle">What I Use</p>
        <h2 class="section-title">Tech Stack</h2>
      </div>
      <div class="skills-grid">
        <div v-for="skill in skills" :key="skill.name" class="skill-card glass-panel">
          <div class="skill-icon">{{ skill.icon }}</div>
          <h3 class="skill-name">{{ skill.name }}</h3>
          <p class="skill-desc">{{ skill.desc }}</p>
        </div>
      </div>
    </section>

    <!-- Projects Section -->
    <section id="projects">
      <div class="section-header">
        <p class="section-subtitle">My Creations</p>
        <h2 class="section-title">GitHub Repositories</h2>
      </div>

      <!-- Controls -->
      <div class="project-controls">
        <div class="filter-tabs">
          <button 
            v-for="filter in ['All', 'Java', 'Frontend', 'Other']" 
            :key="filter"
            @click="activeFilter = filter"
            :class="['filter-tab', { active: activeFilter === filter }]"
          >
            {{ filter }}
          </button>
        </div>

        <div style="display: flex; gap: 1rem; flex-wrap: wrap;">
          <input 
            type="text" 
            v-model="searchQuery" 
            placeholder="프로젝트 검색..." 
            class="search-box"
          >
          <select v-model="sortBy" class="search-box" style="min-width: 140px; cursor: pointer;">
            <option value="updated">최근 업데이트순</option>
            <option value="stars">별점 많은순</option>
          </select>
        </div>
      </div>

      <!-- Projects Grid -->
      <div class="projects-grid">
        <!-- Loading State -->
        <div v-if="loading" class="loading-spinner glass-panel">
          <div class="spinner"></div>
          <p>GitHub 저장소를 불러오고 있습니다...</p>
        </div>

        <!-- Repo Cards -->
        <div 
          v-else-if="filteredRepos.length > 0" 
          v-for="repo in filteredRepos" 
          :key="repo.id" 
          class="project-card glass-panel"
        >
          <div class="project-card-top">
            <div class="repo-header">
              <div class="repo-icon-wrap">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>
              </div>
              <div v-if="repo.stargazers_count > 0" class="repo-stars">
                <svg width="12" height="12" viewBox="0 0 24 24"><path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/></svg>
                {{ repo.stargazers_count }}
              </div>
            </div>

            <h3 class="project-name">{{ repo.name }}</h3>
            <p class="project-desc">{{ repo.description || '등록된 프로젝트 설명이 없습니다.' }}</p>
            
            <div v-if="repo.topics && repo.topics.length" class="project-tags">
              <span v-for="topic in repo.topics.slice(0, 4)" :key="topic" class="project-tag">
                {{ topic }}
              </span>
            </div>
          </div>

          <div class="project-card-bottom">
            <div class="project-lang">
              <span 
                v-if="repo.language" 
                class="lang-dot" 
                :style="{ backgroundColor: getLanguageColor(repo.language) }"
              ></span>
              <span>{{ repo.language || 'Documentation' }}</span>
            </div>
            <a :href="repo.html_url" target="_blank" rel="noopener noreferrer" class="repo-link">
              GitHub로 이동
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
            </a>
          </div>
        </div>

        <!-- Empty State -->
        <div v-else class="empty-state glass-panel">
          <p>검색 조건에 맞는 저장소가 없습니다.</p>
        </div>
      </div>
    </section>

    <!-- Contact Section -->
    <section id="contact">
      <div class="section-header">
        <p class="section-subtitle">Get In Touch</p>
        <h2 class="section-title">Contact Me</h2>
      </div>

      <div class="contact-container glass-panel">
        <div class="contact-info">
          <div class="contact-card">
            <div class="contact-icon">📧</div>
            <div class="contact-details">
              <h4>Email</h4>
              <p>yeojisun@gmail.com</p>
            </div>
          </div>
          <div class="contact-card">
            <div class="contact-icon">🐙</div>
            <div class="contact-details">
              <h4>GitHub</h4>
              <p>github.com/yeojisun</p>
            </div>
          </div>
          <div class="contact-card">
            <div class="contact-icon">📍</div>
            <div class="contact-details">
              <h4>Location</h4>
              <p>Seoul, Republic of Korea</p>
            </div>
          </div>
        </div>

        <!-- Netlify-integrated form -->
        <form 
          name="contact" 
          method="POST" 
          data-netlify="true" 
          data-netlify-honeypot="bot-field"
          @submit.prevent="handleSubmit"
          class="contact-form"
        >
          <!-- Hidden inputs for Netlify -->
          <input type="hidden" name="form-name" value="contact" />
          <p style="display: none;">
            <label>Don't fill this out if you're human: <input name="bot-field" /></label>
          </p>

          <div class="form-group">
            <label for="name">Name</label>
            <input 
              type="text" 
              id="name" 
              name="name" 
              v-model="contactForm.name" 
              required 
              placeholder="이름을 입력해주세요"
              class="form-control"
            >
          </div>

          <div class="form-group">
            <label for="email">Email</label>
            <input 
              type="email" 
              id="email" 
              name="email" 
              v-model="contactForm.email" 
              required 
              placeholder="이메일을 입력해주세요"
              class="form-control"
            >
          </div>

          <div class="form-group">
            <label for="message">Message</label>
            <textarea 
              id="message" 
              name="message" 
              v-model="contactForm.message" 
              required 
              placeholder="메시지를 입력해주세요"
              class="form-control"
            ></textarea>
          </div>

          <button 
            type="submit" 
            :disabled="submitting" 
            class="btn btn-primary" 
            style="align-self: flex-start; min-width: 140px; justify-content: center;"
          >
            {{ submitting ? '보내는 중...' : '보내기' }}
          </button>

          <!-- Status Banners -->
          <div v-if="formSubmitted" class="badge" style="background: rgba(16, 185, 129, 0.1); border-color: rgba(16, 185, 129, 0.3); color: var(--success); width: 100%; text-align: center;">
            ✓ 메시지가 성공적으로 전송되었습니다!
          </div>
          <div v-if="formError" class="badge" style="background: rgba(239, 68, 68, 0.1); border-color: rgba(239, 68, 68, 0.3); color: #ef4444; width: 100%; text-align: center;">
            ✗ 전송 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.
          </div>
        </form>
      </div>
    </section>

    <!-- Footer -->
    <footer class="footer">
      <p>&copy; 2026 Yeo Ji Sun (Yessi). All rights reserved.</p>
      <p>Built with Vue.js 3, hosted on Netlify.</p>
    </footer>
  </div>
</template>
