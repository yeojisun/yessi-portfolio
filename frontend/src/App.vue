<script setup>
import { ref, onMounted, computed } from 'vue'

// Username state
const username = 'yeojisun'

// UI Interactive states
const activeTab = ref('home') // 'home' | 'profile' | 'projects' | 'guestbook'
const theme = ref('pink') // 'pink' | 'blue'
const todayMood = ref('열공') // '행복' | '열공' | '바쁨' | '신남' | '피곤'

// Visitor counters (mocked with realistic Cyworld numbers)
const todayCount = ref(47)
const totalCount = ref(1328)

// BGM title
const bgmName = '프리스타일 - Y (Please Tell Me Why) ♬'

// API states
const profile = ref({
  avatar_url: '',
  name: '여지선 (Yeossi)',
  bio: '일촌 신청 환영! 🍀\n자바 스프링 부트 백엔드와 뷰3 프론트엔드를 개발하는 개발자 여지선입니다. 제 미니홈피에 오신 걸 환영해요!',
  public_repos: 0,
  html_url: 'https://github.com/yeojisun'
})
const repos = ref([])
const loading = ref(true)
const error = ref(false)

// Form states
const submitting = ref(false)
const formSubmitted = ref(false)
const formError = ref(false)
const contactForm = ref({
  name: '',
  email: '',
  message: ''
})

// Guestbook list (initial mock entries + prepended custom entries)
const guestbookEntries = ref([
  {
    id: 1,
    name: '신지민 (일촌)',
    message: '퍼가요~♡ 미니룸 너무 귀엽네요! 깃허브 프로젝트들이 깔끔하게 정리되어 있어서 구경 잘 하고 갑니다.',
    createdAt: '2026-06-22 10:15:30'
  },
  {
    id: 2,
    name: '김도훈 (일촌)',
    message: '야 홈피 꾸미느라 고생했다 ㅋㅋㅋ 자바 백엔드 프로젝트들 코드 대박이네! 나중에 소스공유좀~',
    createdAt: '2026-06-21 16:45:12'
  }
])

// Search and filter states
const searchQuery = ref('')
const activeFilter = ref('All')
const sortBy = ref('updated') // 'updated' or 'stars'

// Fallback projects if GitHub rate-limited
const fallbackRepos = [
  {
    id: 1,
    name: 'toy-store-backend',
    description: 'Spring Boot, Spring Security, JPA, and PostgreSQL-based API service for managing and processing orders in a toy store application.',
    stargazers_count: 8,
    language: 'Java',
    html_url: 'https://github.com/yeojisun/toy-store-backend',
    topics: ['spring-boot', 'jpa', 'postgresql', 'java'],
    updated_at: '2026-06-15T08:30:00Z'
  },
  {
    id: 2,
    name: 'realtime-chat-app',
    description: 'A responsive real-time messaging application with Vue 3, CSS Grid/Flexbox, WebSocket, and Redis caching layers.',
    stargazers_count: 14,
    language: 'Vue',
    html_url: 'https://github.com/yeojisun/realtime-chat-app',
    topics: ['vue', 'javascript', 'websocket', 'redis'],
    updated_at: '2026-06-20T12:00:00Z'
  },
  {
    id: 3,
    name: 'yeossi-portfolio',
    description: 'My personal developer portfolio containing active GitHub repositories, dynamic filters, and clean responsive CSS.',
    stargazers_count: 5,
    language: 'Vue',
    html_url: 'https://github.com/yeojisun/yeossi-portfolio',
    topics: ['vue', 'java', 'vite', 'netlify-forms', 'css-aurora'],
    updated_at: '2026-06-22T10:00:00Z'
  }
]

// Timeline information for Profile
const timeline = [
  { date: '2026.06 - 현재', text: '포트폴리오 미니홈피 리뉴얼 및 신규 프로젝트 진행 중' },
  { date: '2025.12 - 2026.05', text: 'Spring Boot 기반 장난감 가게 주문 관리 백엔드 API 서비스 개발' },
  { date: '2025.07 - 2025.11', text: 'Vue 3 + WebSocket 기반 실시간 다자간 채팅 웹 애플리케이션 프로젝트' },
  { date: '2024.03 - 2025.02', text: 'Spring Batch 프레임워크 기반 일 단위 매출 정산 통계 스케줄러 구현' }
]

// Fetch Github Info
onMounted(async () => {
  try {
    const profileRes = await fetch(`https://api.github.com/users/${username}`)
    if (profileRes.ok) {
      const data = await profileRes.json()
      profile.value = {
        avatar_url: data.avatar_url,
        name: data.name || '여지선 (Yeossi)',
        bio: data.bio || profile.value.bio,
        public_repos: data.public_repos,
        html_url: data.html_url
      }
    }

    const reposRes = await fetch(`https://api.github.com/users/${username}/repos?per_page=100`)
    if (reposRes.ok) {
      const data = await reposRes.json()
      repos.value = data.filter(repo => !repo.fork)
    } else {
      repos.value = fallbackRepos
    }
  } catch (err) {
    console.error(err)
    error.value = true
    repos.value = fallbackRepos
  } finally {
    loading.value = false
  }
})

// Search & Filter
const filteredRepos = computed(() => {
  let result = [...repos.value]

  if (searchQuery.value.trim() !== '') {
    const q = searchQuery.value.toLowerCase()
    result = result.filter(repo => 
      (repo.name && repo.name.toLowerCase().includes(q)) || 
      (repo.description && repo.description.toLowerCase().includes(q))
    )
  }

  if (activeFilter.value !== 'All') {
    if (activeFilter.value === 'Java') {
      result = result.filter(repo => repo.language === 'Java')
    } else if (activeFilter.value === 'Frontend') {
      result = result.filter(repo => repo.language === 'Vue' || repo.language === 'JavaScript' || repo.language === 'HTML' || repo.language === 'CSS')
    } else {
      result = result.filter(repo => repo.language !== 'Java' && repo.language !== 'Vue' && repo.language !== 'JavaScript' && repo.language !== 'HTML' && repo.language !== 'CSS')
    }
  }

  if (sortBy.value === 'updated') {
    result.sort((a, b) => new Date(b.updated_at) - new Date(a.updated_at))
  } else if (sortBy.value === 'stars') {
    result.sort((a, b) => (b.stargazers_count || 0) - (a.stargazers_count || 0))
  }

  return result
})

// BGM Toggle Play logic (mock audio alert)
const playBgm = () => {
  alert(`BGM 재생 중: ${bgmName}`)
}

// Wave navigation dropdown select
const handleWaveChange = (e) => {
  const url = e.target.value
  if (url) {
    window.open(url, '_blank')
    e.target.value = '' // Reset
  }
}

// Netlify form submission handler
const handleSubmit = async () => {
  submitting.value = true
  formSubmitted.value = false
  formError.value = false

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
      // Form submits to Netlify successfully
      formSubmitted.value = true
      
      // Also prepend the entry dynamically to our local Guestbook display
      guestbookEntries.value.unshift({
        id: Date.now(),
        name: `${contactForm.value.name} (손님)`,
        message: contactForm.value.message,
        createdAt: new Date().toISOString().slice(0, 19).replace('T', ' ')
      })

      // Reset form fields
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

// Formatter for date
const getRelativeDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getMonth() + 1}/${date.getDate()}`
}
</script>

<template>
  <div :class="['cyworld-page-wrapper', { 'theme-blue': theme === 'blue' }]">
    <!-- Top Control Bar -->
    <div class="top-control-bar">
      <div class="theme-toggles">
        <button @click="theme = 'pink'" class="theme-btn theme-btn-pink">Classic Pink</button>
        <button @click="theme = 'blue'" class="theme-btn theme-btn-blue">Ocean Blue</button>
      </div>
      <div>
        <span>yeossi.netlify.app</span>
      </div>
    </div>

    <!-- Outer Binder Grid -->
    <div class="cyworld-binder">
      <!-- Spiral rings connector -->
      <div class="binder-spirals">
        <div v-for="n in 9" :key="n" class="spiral-ring"></div>
      </div>

      <!-- Inner frame content -->
      <div class="cyworld-inner">
        
        <!-- LEFT PANEL: Profile -->
        <aside class="pane-left">
          <!-- Visitor counter -->
          <div class="visitor-counter">
            TODAY <span class="today">{{ todayCount }}</span> | TOTAL <span class="total">{{ totalCount }}</span>
          </div>

          <!-- Profile box structure -->
          <div class="profile-box">
            <!-- Profile avatar -->
            <div class="profile-avatar-wrap">
              <img v-if="profile.avatar_url" :src="profile.avatar_url" alt="Yeossi Profile" class="profile-avatar" />
              <div v-else class="profile-default-avatar">YS</div>
            </div>

            <!-- Mood box -->
            <div class="mood-box">
              TODAY IS.. 
              <select v-model="todayMood" style="font-family: inherit; font-size: 0.72rem; border: none; background: transparent; color: #ff0055; font-weight: bold; cursor: pointer;">
                <option value="행복">행복 ☀️</option>
                <option value="열공">열공 💻</option>
                <option value="바쁨">바쁨 ⚡</option>
                <option value="신남">신남 🎶</option>
                <option value="피곤">피곤 💤</option>
              </select>
            </div>

            <!-- Profile Description -->
            <p class="profile-desc">{{ profile.bio }}</p>

            <!-- User Detail info -->
            <div class="profile-user">
              <div class="profile-user-name">
                {{ profile.name }}
                <span style="font-size: 0.7rem; color: #ff0055; font-weight: normal;">(여씨)</span>
              </div>
              <div class="profile-user-detail">📧 duwltjs5921@gmail.com</div>
              <div class="profile-user-detail">📍 Incheon, Korea</div>
            </div>
          </div>

          <!-- Wave drop-down list -->
          <select @change="handleWaveChange" class="ilchon-dropdown">
            <option value="" disabled selected>파도타기 (Quick Links)</option>
            <option :value="profile.html_url">GitHub 주소</option>
            <option value="mailto:duwltjs5921@gmail.com">메일 보내기</option>
          </select>
        </aside>

        <!-- RIGHT PANEL: Main Content -->
        <main class="pane-right">
          <!-- Header (Title and BGM) -->
          <div class="right-header">
            <h2 class="right-title">{{ profile.name }}의 미니홈피에 오신것을 환영합니다☘</h2>
            <div @click="playBgm" class="bgm-player" style="cursor: pointer;">
              <span class="bgm-icon">▶</span>
              <span class="bgm-title">{{ bgmName }}</span>
            </div>
          </div>

          <!-- Content Switcher Area -->
          <div class="right-content-body">
            
            <!-- 1. HOME TAB -->
            <div v-if="activeTab === 'home'" class="home-section">
              <!-- Isometric Miniroom container -->
              <div class="miniroom-container">
                <span class="miniroom-header-text">Mini Room</span>
                <div class="isometric-floor"></div>
                
                <!-- Avatar character -->
                <div class="isometric-avatar-wrap">
                  <div class="miniroom-bubble">
                    어서오세요! {{ todayMood }} 모드인 여지선의 미니룸입니다 🍀
                  </div>
                  <img 
                    v-if="profile.avatar_url" 
                    :src="profile.avatar_url" 
                    alt="Mini avatar" 
                    class="mini-avatar-character"
                  />
                  <div v-else class="mini-avatar-character" style="display:flex;align-items:center;justify-content:center;font-weight:bold;">YS</div>
                </div>
              </div>

              <!-- Updated News Grid -->
              <div class="updated-news">
                <div class="news-title">Updated News (최근 프로젝트 소식)</div>
                <div class="news-grid">
                  <div v-for="repo in repos.slice(0, 4)" :key="repo.id" class="news-item">
                    <span class="news-item-text">💻 {{ repo.name }}</span>
                    <span class="news-item-tag">{{ repo.language || 'Code' }}</span>
                  </div>
                </div>
              </div>

              <!-- What Friends Say (Mini guestbook comments) -->
              <div class="friends-say">
                <div class="friends-say-title">What friends say (일촌 한마디)</div>
                <div class="friends-say-list">
                  <div v-for="entry in guestbookEntries.slice(0, 3)" :key="entry.id" class="friends-say-item">
                    <span class="friends-say-name">{{ entry.name }}</span>
                    <span class="friends-say-content">{{ entry.message }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 2. PROFILE TAB -->
            <div v-else-if="activeTab === 'profile'" class="profile-section">
              <div class="profile-card">
                <div class="profile-card-title">소개 (About Me)</div>
                <p style="line-height: 1.5; margin-bottom: 10px;">
                  안녕하세요! 견고한 자바 스프링 부트 백엔드와 상호작용하는 프론트엔드 환경을 구축하는 개발자 여지선입니다.
                  새로운 기술을 도입하고 성능을 최적화하는 과정에 흥미를 느끼며, 깃허브 프로젝트를 기반으로 포트폴리오를 만들어가고 있습니다.
                </p>
              </div>

              <div class="profile-card">
                <div class="profile-card-title">활동 내역 (Timeline)</div>
                <div class="timeline">
                  <div v-for="item in timeline" :key="item.date" class="timeline-item">
                    <span class="timeline-date">{{ item.date }}</span>
                    <p class="timeline-text">{{ item.text }}</p>
                  </div>
                </div>
              </div>

              <div class="profile-card">
                <div class="profile-card-title">기술 스택 (Core Skills)</div>
                <div style="display: flex; flex-wrap: wrap; gap: 6px; font-size: 0.72rem;">
                  <span style="background: #e0f2fe; color: #0369a1; padding: 3px 8px; border-radius: 4px; border: 1px solid #bae6fd;">Java</span>
                  <span style="background: #dcfce7; color: #15803d; padding: 3px 8px; border-radius: 4px; border: 1px solid #bbf7d0;">Spring Boot</span>
                  <span style="background: #fef9c3; color: #a16207; padding: 3px 8px; border-radius: 4px; border: 1px solid #fef08a;">Vue.js 3</span>
                  <span style="background: #fae8ff; color: #86198f; padding: 3px 8px; border-radius: 4px; border: 1px solid #f5d0fe;">JPA / Hibernate</span>
                  <span style="background: #ffe4e6; color: #be123c; padding: 3px 8px; border-radius: 4px; border: 1px solid #fecdd3;">H2 / PostgreSQL</span>
                  <span style="background: #f1f5f9; color: #334155; padding: 3px 8px; border-radius: 4px; border: 1px solid #cbd5e1;">Git & GitHub</span>
                </div>
              </div>
            </div>

            <!-- 3. PROJECTS TAB (사진첩) -->
            <div v-else-if="activeTab === 'projects'" style="display:flex; flex-direction:column; gap:12px;">
              <!-- Search, Sort and Filter controls -->
              <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:8px; border-bottom: 1px solid var(--cy-border-light); padding-bottom: 8px;">
                <div style="display:flex; gap:4px;">
                  <button 
                    v-for="f in ['All', 'Java', 'Frontend', 'Other']" 
                    :key="f"
                    @click="activeFilter = f"
                    :style="{
                      border: '1px solid var(--cy-border)',
                      background: activeFilter === f ? 'var(--cy-primary)' : '#ffffff',
                      color: activeFilter === f ? '#ffffff' : 'var(--cy-text-dark)',
                      padding: '3px 8px',
                      fontSize: '0.7rem',
                      fontWeight: 'bold',
                      borderRadius: '4px',
                      cursor: 'pointer'
                    }"
                  >
                    {{ f }}
                  </button>
                </div>
                <div style="display:flex; gap:4px;">
                  <input 
                    type="text" 
                    v-model="searchQuery" 
                    placeholder="프로젝트 검색..." 
                    style="font-family:inherit; font-size:0.7rem; border:1px solid var(--cy-border); border-radius:4px; padding:3px 8px; max-width: 120px;"
                  />
                  <select v-model="sortBy" style="font-family:inherit; font-size:0.7rem; border:1px solid var(--cy-border); border-radius:4px; cursor:pointer;">
                    <option value="updated">최근수정</option>
                    <option value="stars">별점순</option>
                  </select>
                </div>
              </div>

              <!-- Loading spinner -->
              <div v-if="loading" style="text-align:center; padding: 2rem;">
                <p>사진첩을 불러오는 중...</p>
              </div>

              <!-- Gallery projects grid -->
              <div v-else-if="filteredRepos.length > 0" class="gallery-grid">
                <div v-for="repo in filteredRepos" :key="repo.id" class="gallery-card">
                  <!-- Graphic placeholder -->
                  <div class="gallery-image-placeholder">
                    📂
                  </div>
                  <div class="gallery-body">
                    <h3 class="gallery-title">{{ repo.name }}</h3>
                    <p class="gallery-desc">{{ repo.description || '등록된 프로젝트 설명이 없습니다.' }}</p>
                    
                    <div class="gallery-footer">
                      <div class="gallery-lang">
                        <span style="display:inline-block; width:8px; height:8px; border-radius:50%; background:#f59e0b; margin-right:2px;"></span>
                        <span>{{ repo.language || 'Code' }}</span>
                      </div>
                      <a :href="repo.html_url" target="_blank" rel="noopener noreferrer" class="gallery-link">
                        구경가기 →
                      </a>
                    </div>
                  </div>
                </div>
              </div>

              <div v-else style="text-align:center; padding: 2rem; color:var(--cy-text-light);">
                등록된 프로젝트가 없습니다.
              </div>
            </div>

            <!-- 4. GUESTBOOK TAB (방명록) -->
            <div v-else-if="activeTab === 'guestbook'" class="guestbook-section">
              <!-- Guestbook submit card -->
              <div class="guestbook-form-card">
                <form 
                  name="contact" 
                  method="POST" 
                  data-netlify="true" 
                  data-netlify-honeypot="bot-field"
                  @submit.prevent="handleSubmit"
                  class="guestbook-form"
                >
                  <input type="hidden" name="form-name" value="contact" />
                  <p style="display: none;">
                    <label><input name="bot-field" /></label>
                  </p>

                  <div class="guestbook-input-row">
                    <input 
                      type="text" 
                      name="name" 
                      v-model="contactForm.name" 
                      required 
                      placeholder="이름" 
                      class="guestbook-control" 
                      style="width: 120px;"
                    />
                    <input 
                      type="email" 
                      name="email" 
                      v-model="contactForm.email" 
                      required 
                      placeholder="이메일" 
                      class="guestbook-control" 
                      style="flex: 1;"
                    />
                  </div>
                  <textarea 
                    name="message" 
                    v-model="contactForm.message" 
                    required 
                    placeholder="방명록을 남겨주세요 (Netlify Form 연동)" 
                    class="guestbook-control guestbook-textarea"
                  ></textarea>

                  <button 
                    type="submit" 
                    :disabled="submitting" 
                    class="guestbook-submit"
                  >
                    {{ submitting ? '작성 중...' : '등록' }}
                  </button>
                </form>

                <div v-if="formSubmitted" style="font-size:0.75rem; color:#10b981; margin-top:8px; font-weight:bold; text-align:center;">
                  ✓ 방명록이 정상적으로 등록되었습니다!
                </div>
                <div v-if="formError" style="font-size:0.75rem; color:#ef4444; margin-top:8px; font-weight:bold; text-align:center;">
                  ✗ 등록 중 오류가 발생했습니다. 다시 시도해 주세요.
                </div>
              </div>

              <!-- Guestbook posts list -->
              <div class="guestbook-timeline">
                <div v-for="entry in guestbookEntries" :key="entry.id" class="guestbook-card">
                  <div class="guestbook-card-header">
                    <span class="guestbook-card-author">{{ entry.name }}</span>
                    <span>({{ entry.createdAt }})</span>
                  </div>
                  <p class="guestbook-card-body">{{ entry.message }}</p>
                </div>
              </div>
            </div>

          </div>
        </main>

        <!-- Vertical folder tabs on the right edge -->
        <nav class="nav-tabs-vertical">
          <button 
            @click="activeTab = 'home'" 
            :class="['nav-tab-v', { inactive: activeTab !== 'home' }]"
          >
            홈
          </button>
          <button 
            @click="activeTab = 'profile'" 
            :class="['nav-tab-v', { inactive: activeTab !== 'profile' }]"
          >
            프로필
          </button>
          <button 
            @click="activeTab = 'projects'" 
            :class="['nav-tab-v', { inactive: activeTab !== 'projects' }]"
          >
            사진첩
          </button>
          <button 
            @click="activeTab = 'guestbook'" 
            :class="['nav-tab-v', { inactive: activeTab !== 'guestbook' }]"
          >
            방명록
          </button>
        </nav>

      </div>
    </div>

    <!-- Cyworld footer copyright -->
    <footer class="cyworld-footer">
      <p>&copy; 2026 Yeo Ji Sun (Yeossi). All rights reserved.</p>
      <p>Retro design inspired by classic Cyworld mini-homepage.</p>
    </footer>
  </div>
</template>
