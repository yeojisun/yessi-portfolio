<script setup>
import { ref, onMounted, computed } from 'vue'

// Username state
const username = 'yeojisun'

// UI Interactive states
const activeTab = ref('home') // 'home' | 'profile' | 'projects' | 'guestbook'
const todayMood = ref('열공') // '행복' | '열공' | '바쁨' | '신남' | '피곤'

// Visitor counters (mocked with realistic Cyworld numbers)
const todayCount = ref(38)
const totalCount = ref(1524)

// BGM title
const bgmName = '프리스타일 - Y (Please Tell Me Why) ♬'

// API states
const profile = ref({
  avatar_url: '',
  name: '여지선 (Yeossi)',
  bio: '',
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

  const isLocal = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1'

  try {
    let success = false

    if (isLocal) {
      // Simulate network request delay for natural UX
      await new Promise(resolve => setTimeout(resolve, 500))
      success = true
      console.log('Local development mode: Bypassed Netlify form POST. Message saved to screen mockup.')
    } else {
      const body = new URLSearchParams()
      body.append('form-name', 'contact')
      body.append('bot-field', '')
      body.append('name', contactForm.value.name)
      body.append('email', contactForm.value.email)
      body.append('message', contactForm.value.message)

      const response = await fetch('/', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: body.toString()
      })
      success = response.ok
    }

    if (success) {
      formSubmitted.value = true
      
      // Prepend entry dynamically
      guestbookEntries.value.unshift({
        id: Date.now(),
        name: `${contactForm.value.name} (손님)`,
        message: contactForm.value.message,
        createdAt: new Date().toISOString().slice(0, 19).replace('T', ' ')
      })

      // Reset form fields
      contactForm.value = { name: '', email: '', message: '' }
    } else {
      throw new Error('Form submission failed')
    }
  } catch (err) {
    console.error(err)
    formError.value = true
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="background">
    <div class="outerbox">
      <div class="wrapper">
        
        <!-- WRAPPER LEFT: Profile Panel -->
        <div class="wrapper__left">
          <div class="wrapper__left__header">
            <div class="today">
              <span>TODAY </span><span class="today-num">{{ todayCount }}</span><span> | TOTAL </span><span>{{ totalCount }}</span>
            </div>
          </div>
          <div class="wrapper__left__body">
            <div class="left__body__header">
              <!-- Avatar Profile Image -->
              <div class="left__body__header_gray">
                <img v-if="profile.avatar_url" :src="profile.avatar_url" alt="Yeossi Profile" />
                <div v-else style="font-weight:bold; color:gray;">YS</div>
              </div>
              
              <!-- Mood display -->
              <div style="font-size: 8px; text-align: center; margin-top: 6px; color: #ff5500;">
                오늘의 기분: 
                <select v-model="todayMood" style="font-family: inherit; font-size: 8px; border: none; font-weight: bold; cursor: pointer; color: #ff5500; background: transparent;">
                  <option value="행복">행복 ☀️</option>
                  <option value="열공">열공 💻</option>
                  <option value="바쁨">바쁨 ⚡</option>
                  <option value="신남">신남 🎶</option>
                  <option value="피곤">피곤 💤</option>
                </select>
              </div>

              <div class="left__body__header_line"></div>
            </div>

            <!-- Profile Info Details -->
            <div class="left__body__profile">
              <div class="profile-detail-item">👤 이름: 여지선</div>
              <div class="profile-detail-item">✉️ E-mail: duwltjs5921@gmail.com</div>
              <div class="profile-detail-item">🔔 Location: Incheon</div>
              
              <!-- Short description bio text -->
              <p style="font-size: 8px; color: gray; margin-top: 6px; line-height: 1.3; white-space: pre-wrap;">{{ profile.bio }}</p>
            </div>

            <!-- Footer Wave selection drop-down -->
            <div class="left__body__footer">
              <p style="font-size: 10px; color: gray; padding-bottom: 6px;">일촌 파도타기</p>
              <div>
                <select @change="handleWaveChange" class="selectBox">
                  <option value="" disabled selected>선택하세요</option>
                  <option :value="profile.html_url">GitHub 저장소</option>
                  <option value="mailto:duwltjs5921@gmail.com">이메일 쓰기</option>
                </select>
              </div>
            </div>
          </div>
        </div>

        <!-- WRAPPER RIGHT: Content Panel -->
        <div class="wrapper__right">
          <div class="wrapper__right__header">
            <div class="right__header__title"><b>{{ profile.name }}의 미니홈피</b></div>
            <div class="right__header__setup">{{ bgmName }}</div>
          </div>
          <div class="wrapper__right__body">
            
            <!-- 1. HOME TAB -->
            <div v-if="activeTab === 'home'" class="home-layout">
              <!-- Updated News Block -->
              <div class="home-section-block">
                <div class="contents_title">
                  <div class="title">Updated news</div>
                  <div class="subtitle">TODAY STORY</div>
                </div>
                <div class="divdideLine"></div>
                <div class="contents__body">
                  <div style="display:grid; grid-template-columns: 1fr 1fr; gap: 4px; font-size: 10px;">
                    <div v-for="repo in repos.slice(0, 4)" :key="repo.id" style="overflow:hidden; text-overflow:ellipsis; white-space:nowrap; color:#666;">
                      📂 {{ repo.name }} ({{ repo.language || 'Code' }})
                    </div>
                  </div>
                </div>
              </div>

              <!-- Miniroom Block -->
              <div class="home-section-block" style="margin-top: 15px; flex: 1; display:flex; flex-direction:column; justify-content:flex-end;">
                <div class="contents_title">
                  <div class="title">My Miniroom</div>
                  <div class="subtitle">INTRODUCE YOURSELF</div>
                </div>
                <div class="body__contents__body_gray">
                  <div class="miniroom-floor"></div>
                  <div class="miniroom-avatar">
                    <div class="miniroom-speech">어서오세요! {{ todayMood }} 모드 여지선 미니룸 ☘</div>
                    <img v-if="profile.avatar_url" :src="profile.avatar_url" alt="Yeossi avatar" />
                    <div v-else style="font-weight:bold; color:gray; font-size:1.5rem;">YS</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 2. PROFILE TAB -->
            <div v-else-if="activeTab === 'profile'" class="guestbook-wrapper">
              <div class="contents_title">
                <div class="title">My Profile</div>
                <div class="subtitle">ABOUT ME & TIMELINE</div>
              </div>
              <div class="divdideLine"></div>
              
              <p style="font-size:10px; color:#555; line-height:1.4; margin-bottom: 8px;">
                안녕하세요! 자바 백엔드와 뷰3 프론트엔드를 다루는 주니어 개발자 여지선입니다.
                구조적이고 확장 가능한 백엔드 아키텍처 설계와 아름답고 사용하기 편리한 웹 서비스 개발을 지향합니다.
              </p>

              <div class="profile-timeline">
                <div v-for="item in timeline" :key="item.date" class="profile-timeline-item">
                  <div class="profile-timeline-date">{{ item.date }}</div>
                  <div class="profile-timeline-desc">{{ item.text }}</div>
                </div>
              </div>
            </div>

            <!-- 3. PROJECTS TAB (사진첩) -->
            <div v-else-if="activeTab === 'projects'" class="guestbook-wrapper">
              <div class="contents_title">
                <div class="title">My Projects</div>
                <div class="subtitle">PHOTO GALLERY</div>
              </div>
              <div class="divdideLine"></div>

              <!-- Gallery filter list -->
              <div style="display:flex; justify-content:space-between; gap:4px; font-size:9px; margin-bottom: 8px;">
                <div style="display:flex; gap:2px;">
                  <button 
                    v-for="cat in ['All', 'Java', 'Frontend']" 
                    :key="cat"
                    @click="activeFilter = cat"
                    :style="{
                      border: '1px solid gray',
                      background: activeFilter === cat ? '#298eb5' : '#fff',
                      color: activeFilter === cat ? '#fff' : '#000',
                      padding: '2px 5px',
                      fontSize: '9px',
                      cursor: 'pointer'
                    }"
                  >
                    {{ cat }}
                  </button>
                </div>
                <input 
                  type="text" 
                  v-model="searchQuery" 
                  placeholder="검색..." 
                  style="font-family:inherit; font-size:9px; width:80px; padding:1px 4px; border:1px solid gray;"
                />
              </div>

              <!-- Photo Gallery Grid -->
              <div v-if="loading" style="font-size:10px; text-align:center; padding: 20px;">
                저장소를 불러오는 중...
              </div>
              <div v-else class="projects-gallery">
                <div v-for="repo in filteredRepos.slice(0, 4)" :key="repo.id" class="project-photo-card">
                  <div class="photo-placeholder">📂</div>
                  <div class="photo-title">{{ repo.name }}</div>
                  <p class="photo-desc">{{ repo.description || '프로젝트 설명이 없습니다.' }}</p>
                  <a :href="repo.html_url" target="_blank" rel="noopener noreferrer" class="photo-link">구경가기 →</a>
                </div>
              </div>
            </div>

            <!-- 4. GUESTBOOK TAB (방명록) -->
            <div v-else-if="activeTab === 'guestbook'" class="guestbook-wrapper">
              <div class="contents_title">
                <div class="title">Guest Book</div>
                <div class="subtitle">방명록을 남겨주세요</div>
              </div>
              <div class="divdideLine"></div>

              <!-- Submit Form -->
              <div class="guestbook-input-card">
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
                  
                  <div class="guestbook-form-row">
                    <input 
                      type="text" 
                      name="name" 
                      v-model="contactForm.name" 
                      required 
                      placeholder="이름" 
                      class="guestbook-form-input" 
                      style="width: 80px;"
                    />
                    <input 
                      type="email" 
                      name="email" 
                      v-model="contactForm.email" 
                      required 
                      placeholder="이메일" 
                      class="guestbook-form-input" 
                      style="flex: 1;"
                    />
                  </div>
                  <textarea 
                    name="message" 
                    v-model="contactForm.message" 
                    required 
                    placeholder="방명록을 입력해 주세요." 
                    class="guestbook-form-input guestbook-form-textarea"
                  ></textarea>
                  <button 
                    type="submit" 
                    :disabled="submitting" 
                    class="guestbook-form-submit"
                  >
                    {{ submitting ? '등록중...' : '등록' }}
                  </button>
                </form>
                
                <div v-if="formSubmitted" style="font-size:9px; color:#10b981; margin-top:4px; text-align:center; font-weight:bold;">
                  방명록이 등록되었습니다!
                </div>
              </div>

              <!-- Entries Timeline -->
              <div class="guestbook-entries">
                <div v-for="entry in guestbookEntries" :key="entry.id" class="guestbook-post">
                  <div class="guestbook-post-header">
                    <span class="guestbook-post-author">{{ entry.name }}</span>
                    <span>({{ entry.createdAt }})</span>
                  </div>
                  <p class="guestbook-post-body">{{ entry.message }}</p>
                </div>
              </div>
            </div>

          </div>
        </div>

        <!-- NAVIGATION: Right vertical tabs from original template -->
        <div class="navigation">
          <div 
            @click="activeTab = 'home'" 
            :class="['navigation_item', { inactive: activeTab !== 'home' }]"
          >
            홈
          </div>
          <div 
            @click="activeTab = 'profile'" 
            :class="['navigation_item', { inactive: activeTab !== 'profile' }]"
          >
            프로필
          </div>
          <div 
            @click="activeTab = 'projects'" 
            :class="['navigation_item', { inactive: activeTab !== 'projects' }]"
          >
            사진첩
          </div>
          <div 
            @click="activeTab = 'guestbook'" 
            :class="['navigation_item', { inactive: activeTab !== 'guestbook' }]"
          >
            방명록
          </div>
        </div>

      </div>
    </div>
  </div>
</template>
