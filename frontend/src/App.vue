<script setup>
import { ref, onMounted, computed } from 'vue'

// Username state
const username = 'yeojisun'

// UI Interactive states
const activeTab = ref('home') // 'home' | 'profile' | 'projects' | 'guestbook'
const todayMood = ref('열공') // '행복' | '열공' | '바쁨' | '신남' | '피곤'

// Visitor counters (starts at 0 and increments with real visits)
const todayCount = ref(0)
const totalCount = ref(0)

// BGM title
const bgmName = '프리스타일 - Y ♬'

// BGM audio states
const isPlaying = ref(false)
const showWaveModal = ref(false)

const toggleBgm = () => {
  const player = document.getElementById('yt-player')
  if (!player) return

  if (isPlaying.value) {
    player.contentWindow.postMessage(JSON.stringify({
      event: 'command',
      func: 'pauseVideo'
    }), '*')
  } else {
    player.contentWindow.postMessage(JSON.stringify({
      event: 'command',
      func: 'playVideo'
    }), '*')
  }
  isPlaying.value = !isPlaying.value
}

const stopBgm = () => {
  const player = document.getElementById('yt-player')
  if (!player) return
  player.contentWindow.postMessage(JSON.stringify({
    event: 'command',
    func: 'stopVideo'
  }), '*')
  isPlaying.value = false
}

const prevTrack = () => {
  const player = document.getElementById('yt-player')
  if (!player) return
  player.contentWindow.postMessage(JSON.stringify({
    event: 'command',
    func: 'seekTo',
    args: [0, true]
  }), '*')
}

const nextTrack = () => {
  alert('등록된 다른 BGM이 없습니다.')
}

const handleWaveMenuClick = () => {
  showWaveModal.value = true
}

// API states
const profile = ref({
  avatar_url: '/images/profile.png',
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

// Guestbook list (initial// Backend API URL mapping
const backendUrl = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1'
  ? 'http://localhost:8085'
  : 'https://yeossi-backend.onrender.com' // Replace with your actual Render URL once deployed

// Guestbook list (loaded dynamically from database)
const guestbookEntries = ref([])

// Fetch guestbook list from Java Backend
const fetchGuestbook = async () => {
  try {
    const res = await fetch(`${backendUrl}/api/messages`)
    if (res.ok) {
      const data = await res.json()
      if (data && data.length > 0) {
        guestbookEntries.value = data.map(item => ({
          id: item.id,
          name: `${item.name} (손님)`,
          message: item.message,
          createdAt: item.createdAt ? item.createdAt.replace('T', ' ').slice(0, 19) : ''
        })).reverse() // Show latest first
      }
    }
  } catch (err) {
    console.error('Error fetching guestbook:', err)
  }
}

// Fetch and increment visitor count from database
const recordVisit = async () => {
  try {
    const hasVisited = sessionStorage.getItem('visited_today')
    let res
    if (!hasVisited) {
      res = await fetch(`${backendUrl}/api/visits`, { method: 'POST' })
      sessionStorage.setItem('visited_today', 'true')
    } else {
      res = await fetch(`${backendUrl}/api/visits`, { method: 'GET' })
    }

    if (res.ok) {
      const data = await res.json()
      // Adding a base offset to the total counts to make it look like a historical Cyworld homepage
      const BASE_OFFSET = 1520
      todayCount.value = data.today || 0
      totalCount.value = (data.total || 0) + BASE_OFFSET
    }
  } catch (err) {
    console.error('Error logging visit:', err)
  }
}

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
  {
    date: '2024.11 ~ 현재',
    text: '뱅크웨어글로벌 ㈜ | 과장/팀원 (웹개발)\n• 하나은행 DMD 통합금고 재구축 (2024.11 ~ 2025.04 | Java, JSP, Websquare, PostgreSQL)\n• SBI 저축은행 여신 채널 통합 및 미성년자 비대면서비스구축 (2025.05 ~ 2026.04 | Java, JSP, Jex Framework)'
  },
  {
    date: '2022.05 ~ 2024.07',
    text: '케이스마텍 | 개발팀 과장 (웹개발/서비스개발팀)\n• 하나은행 트래블로그 (2024.02 ~ 2024.06 | Java, JSP)\n• 하나캐피탈 DT리빌드 구축 (2022.11 ~ 2023.12 | Vue.js, Java)\n• 현대자동차 블루링크 플릿 2.0 (2022.06 ~ 2022.10 | Java, JSP, Thymeleaf)'
  },
  {
    date: '2020.08 ~ 2022.05',
    text: '인프라닉스 | DX SW 개발팀 대리 (사내 솔루션 프론트엔드 개발 및 유지보수)\n• Sysmaster suite K8s 신규 사내 솔루션 개발 (2022.01 ~ 2022.02 | Vue, Java)\n• Sysmaster suite 8.0 사내 솔루션 개발 및 운영 (2021.06 ~ 2021.12 | Java, JSP, jqxgrid, fusionchart)\n• 행정안전부 전자정부 클라우드 플랫폼 구축 사업 2차 (2020.09 ~ 2021.05 | Java, JSP, React, Grafana, Kibana)'
  },
  {
    date: '2017.09 ~ 2020.03',
    text: '씨알랩 | 개발팀 대리 (웹사이트, 응용sw 개발 및 유지보수)\n• 현대카드 모니터링 시스템 유지보수 및 추가개발 (2017.09 ~ 2020.03 | Java, C#, C++)\n• 인천종합에너지 홈페이지 에너지 바우처 기능 추가 개발 (2017.09 ~ 2020.03 | Java)\n• 인천 미래엔 홈페이지 에너지 바우처 기능 추가 개발 (2019.01 ~ 2020.03 | HTML, jQuery, PHP)\n• 회사 내부 솔루션 외국어 메뉴판 번역 앱 개발 (2019.01 ~ 2019.12 | Android, Java, Google OCR API)\n• 안양시 시설관리공단 주차관리 시스템 유지보수 (2018.10 ~ 2019.05 | C#)'
  },
  {
    date: '2014.09 ~ 2017.03',
    text: '㈜수리이엔씨 | 개발팀 사원 (Java 기반 웹 사이트 개발 및 C# 기반 수자원관리 시스템 개발)\n• 농어촌 공간정보 통합운영체계 구축용역 (2015.06 ~ 2017.03 | Java)\n• 농촌용수 합리화계획 상시관리체계 구축 방안 연구용역 (2014.09 ~ 2015.11 | C#, devexpress, postgreSQL)\n• 가뭄 대응능력 분석 시범모형 개발 (2014.09 ~ 2017.03 | C#, devexpress, postgreSQL, SWMM)\n• 농촌용수 합리화계획 홈페이지 유지보수 및 Google GIS 맵 서비스 개발 (2014.09 ~ 2017.03 | HTML, jQuery, Java)'
  }
]

// Fetch Github Info, Guestbook & Visits
onMounted(async () => {
  // Load guestbook and visits from DB
  fetchGuestbook()
  recordVisit()

  try {
    const profileRes = await fetch(`https://api.github.com/users/${username}`)
    if (profileRes.ok) {
      const data = await profileRes.json()
      profile.value = {
        avatar_url: '/images/profile.png',
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

// Java Spring Boot REST API guestbook submission handler
const handleSubmit = async () => {
  submitting.value = true
  formSubmitted.value = false
  formError.value = false

  try {
    const response = await fetch(`${backendUrl}/api/messages`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        name: contactForm.value.name,
        email: contactForm.value.email,
        message: contactForm.value.message
      })
    })

    if (response.ok) {
      formSubmitted.value = true
      
      // Refresh guestbook list from DB
      await fetchGuestbook()

      // Reset form fields
      contactForm.value = { name: '', email: '', message: '' }
    } else {
      throw new Error('Database message post failed')
    }
  } catch (err) {
    console.error('Error posting guestbook:', err)
    formError.value = true
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="background">
    <!-- Hidden YouTube Embed for BGM Playback (Freestyle - Y) -->
    <iframe 
      id="yt-player"
      src="https://www.youtube.com/embed/dYIT_jeUBKg?enablejsapi=1&controls=0&rel=0&autoplay=0&loop=1&playlist=dYIT_jeUBKg"
      style="position: absolute; width: 1px; height: 1px; left: -9999px; top: -9999px; border: none; pointer-events: none;"
      allow="autoplay"
    ></iframe>
    <!-- DESKTOP ONLY LAYOUT -->
    <div class="desktop-only-layout">
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
            <div class="right__header__title"><b>Hello Yeo’s World!</b></div>
            <div class="right__header__setup">
              <span style="font-size: 9px; font-weight: bold; color: #ff5500; cursor: pointer;">★ 일촌맺기 ★ 팬되기</span>
            </div>
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
              
              <p style="font-size:10px; color:#555; line-height:1.4; margin-bottom: 12px; white-space: pre-wrap;">
                새로운 것에 대한 두려움보다는 설렘을 갖는 개발자 입니다. 동료들과 협업하고 의사소통하며 성취하는 것에 희열(?)을 느끼는 개발자입니다.
              </p>

              <div class="profile-timeline">
                <div v-for="item in timeline" :key="item.date" class="profile-timeline-item">
                  <div class="profile-timeline-date" style="font-weight: bold; color: #298eb5;">{{ item.date }}</div>
                  <div class="profile-timeline-desc" style="white-space: pre-wrap; line-height: 1.5; font-size: 10px; margin-top: 4px; color: #333;">{{ item.text }}</div>
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
    
    <!-- Desktop Right-Side Music Player Sidebar -->
    <div class="desktop-sidebar-player">
      <div class="player-card">
        <!-- Digital Display Screen -->
        <div class="player-display">
          <div class="marquee-container">
            <span class="marquee-text" :class="{ 'marquee-playing': isPlaying }">{{ bgmName }}</span>
          </div>
          <span class="digital-status" :class="{ 'live-blinking': isPlaying }">LIVE</span>
        </div>
        <!-- Progress Bar -->
        <div class="player-progress-bar">
          <div class="progress-fill" :style="{ width: isPlaying ? '60%' : '0%' }"></div>
        </div>
        <!-- Control Buttons -->
        <div class="player-buttons">
          <button @click="prevTrack" class="ctrl-btn">⏮</button>
          <button @click="toggleBgm" class="ctrl-btn play-btn">
            {{ isPlaying ? '⏸' : '▶' }}
          </button>
          <button @click="stopBgm" class="ctrl-btn">⏹</button>
          <button @click="nextTrack" class="ctrl-btn">⏭</button>
          <span class="volume-icon">🔊</span>
        </div>
      </div>
    </div>
    </div>

    <!-- MOBILE ONLY LAYOUT -->
    <div class="mobile-only-layout">
      <!-- 1. Header (Top Blue Bar) -->
      <div class="mobile-header">
        <div class="mobile-header-title">Hello Yeo’s World!</div>
        <div class="mobile-header-icons">
          <span class="m-icon" style="font-size: 14px;">⚙️</span>
          <span class="m-icon" style="font-size: 14px;">🔗</span>
          <span class="m-icon" style="font-size: 14px;" @click="handleWaveMenuClick">💨</span>
        </div>
      </div>

      <!-- 2. Active Tab Content Pane -->
      <div class="mobile-body">
        
        <!-- Home Sub-page -->
        <div v-if="activeTab === 'home'" class="mobile-subpage">
          <!-- Card 1: Stats Grid -->
          <div class="mobile-card">
            <div class="mobile-stats-grid">
              <div class="grid-item">
                <span class="grid-label">다이어리</span>
                <span class="grid-value">0/0</span>
              </div>
              <div class="grid-item" @click="activeTab = 'projects'">
                <span class="grid-label">사진첩</span>
                <span class="grid-value">0/{{ filteredRepos.length }}</span>
              </div>
              <div class="grid-item" @click="toggleBgm" style="cursor: pointer;">
                <span class="grid-label">쥬크박스</span>
                <span class="grid-value">
                  1/1 <span class="badge-n">N</span>
                  <span style="font-size: 8px; margin-left: 2px;">{{ isPlaying ? '⏸' : '▶' }}</span>
                </span>
              </div>
              <div class="grid-item" @click="activeTab = 'guestbook'">
                <span class="grid-label">방명록</span>
                <span class="grid-value">0/{{ guestbookEntries.length }}</span>
              </div>
            </div>
          </div>

          <!-- Card 2: Mini Room -->
          <div class="mobile-card">
            <div class="card-header">
              <span class="card-title-blue">Mini Room</span>
              <span class="card-subtitle-gray">기본 미니룸</span>
            </div>
            <div class="body__contents__body_gray" style="height: 160px; margin-top: 8px;">
              <div class="miniroom-floor"></div>
              <div class="miniroom-avatar">
                <div class="miniroom-speech" style="font-size: 8px; padding: 2px 4px;">어서오세요! {{ todayMood }} 모드 여지선 미니룸 ☘</div>
                <img :src="profile.avatar_url" alt="Yeossi avatar" style="width: 44px; height: 44px;" />
              </div>
            </div>
          </div>

          <!-- Card 3: What Friends Say -->
          <div class="mobile-card" style="margin-bottom: 20px;">
            <div class="card-header">
              <span class="card-title-blue">What Friends Say</span>
            </div>
            <div class="friends-say-container">
              <div v-if="guestbookEntries.length === 0" class="empty-friends-say">
                <div class="speech-bubble-icon">💬</div>
                <div style="font-size: 10px; color: gray; margin-top: 4px;">등록된 일촌평이 없어요.</div>
              </div>
              <div v-else class="friends-say-list">
                <div v-for="entry in guestbookEntries.slice(0, 3)" :key="entry.id" class="friend-say-item">
                  <span class="friend-say-name">{{ entry.name.replace(' (손님)', '').replace(' (일촌)', '') }}</span>
                  <span class="friend-say-msg">{{ entry.message }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Profile Sub-page (싸이생활) -->
        <div v-else-if="activeTab === 'profile'" class="mobile-card" style="padding: 15px;">
          <div class="card-header" style="margin-bottom: 10px;">
            <span class="card-title-blue">싸이생활 (Profile)</span>
          </div>
          <p style="font-size:11px; color:#333; line-height:1.5; margin-bottom: 12px; white-space: pre-wrap;">
            새로운 것에 대한 두려움보다는 설렘을 갖는 개발자 입니다. 동료들과 협업하고 의사소통하며 성취하는 것에 희열(?)을 느끼는 개발자입니다.
          </p>
          <div class="profile-timeline" style="margin-top: 15px;">
            <div v-for="item in timeline" :key="item.date" class="profile-timeline-item">
              <div class="profile-timeline-date" style="font-size: 9px; font-weight: bold; color: #298eb5;">{{ item.date }}</div>
              <div class="profile-timeline-desc" style="white-space: pre-wrap; line-height: 1.5; font-size: 10px; margin-top: 4px; color: #333;">{{ item.text }}</div>
            </div>
          </div>
        </div>

        <!-- Projects Sub-page (사진첩) -->
        <div v-else-if="activeTab === 'projects'" class="mobile-card" style="padding: 12px;">
          <div class="card-header" style="margin-bottom: 10px;">
            <span class="card-title-blue">사진첩 (Projects)</span>
          </div>
          <div class="projects-gallery" style="grid-template-columns: 1fr; gap: 10px;">
            <div v-for="repo in filteredRepos.slice(0, 4)" :key="repo.id" class="project-photo-card" style="padding: 8px;">
              <div class="photo-placeholder" style="height: 80px;">📂</div>
              <div class="photo-title" style="font-size: 11px; font-weight: bold; margin-top: 4px;">{{ repo.name }}</div>
              <p class="photo-desc" style="font-size: 9px; color: gray; height: auto; overflow: visible; display: block; -webkit-line-clamp: none;">{{ repo.description || '프로젝트 설명이 없습니다.' }}</p>
              <a :href="repo.html_url" target="_blank" rel="noopener noreferrer" class="photo-link" style="font-size: 9px; margin-top: 4px;">구경가기 →</a>
            </div>
          </div>
        </div>

        <!-- Guestbook Sub-page (방명록) -->
        <div v-else-if="activeTab === 'guestbook'" class="mobile-card" style="padding: 12px; margin-bottom: 20px;">
          <div class="card-header" style="margin-bottom: 8px;">
            <span class="card-title-blue">방명록 (Guestbook)</span>
          </div>
          <!-- Guestbook Form -->
          <div class="guestbook-input-card" style="margin-bottom: 12px; padding: 8px;">
            <form @submit.prevent="handleSubmit" class="guestbook-form">
              <div class="guestbook-form-row">
                <input type="text" required v-model="contactForm.name" placeholder="이름" class="guestbook-form-input" style="width: 70px; font-size: 9px;" />
                <input type="email" required v-model="contactForm.email" placeholder="이메일" class="guestbook-form-input" style="flex: 1; font-size: 9px;" />
              </div>
              <textarea required v-model="contactForm.message" placeholder="방명록을 남겨주세요." class="guestbook-form-input guestbook-form-textarea" style="font-size: 9px; min-height: 35px;"></textarea>
              <button type="submit" :disabled="submitting" class="guestbook-form-submit" style="font-size: 9px; padding: 2px 8px;">
                {{ submitting ? '등록중...' : '등록' }}
              </button>
            </form>
          </div>
          <!-- Guestbook History -->
          <div class="guestbook-entries" style="gap: 6px;">
            <div v-for="entry in guestbookEntries" :key="entry.id" class="guestbook-post" style="padding: 6px;">
              <div class="guestbook-post-header" style="font-size: 8px; margin-bottom: 4px;">
                <span class="guestbook-post-author" style="color: #298eb5; font-weight: bold;">{{ entry.name }}</span>
                <span>({{ entry.createdAt }})</span>
              </div>
              <p class="guestbook-post-body" style="font-size: 9px;">{{ entry.message }}</p>
            </div>
          </div>
        </div>

      </div>

      <!-- 3. Fixed Footer Navigation Bar -->
      <div class="mobile-footer-nav">
        <div class="footer-nav-item" :class="{ 'footer-active': activeTab === 'profile' }" @click="activeTab = 'profile'">
          <span class="footer-icon">👤</span>
          <span class="footer-label">싸이생활</span>
        </div>
        <div class="footer-nav-item" :class="{ 'footer-active': activeTab === 'home' }" @click="activeTab = 'home'">
          <span class="footer-icon">🏠</span>
          <span class="footer-label">미니홈피</span>
        </div>
        <div class="footer-nav-item" @click="handleWaveMenuClick">
          <span class="footer-icon">💨</span>
          <span class="footer-label">파도타기</span>
        </div>
        <div class="footer-nav-item" :class="{ 'footer-active': activeTab === 'projects' || activeTab === 'guestbook' }" @click="activeTab = 'projects'">
          <span class="footer-icon">⚙️</span>
          <span class="footer-label">더보기</span>
        </div>
      </div>
    </div>

    <!-- Retro Wave Modal for Mobile -->
    <div v-if="showWaveModal" class="wave-modal-overlay" @click="showWaveModal = false">
      <div class="wave-modal-content" @click.stop>
        <div class="wave-modal-header">일촌 파도타기</div>
        <div class="wave-modal-body">
          <a :href="profile.html_url" target="_blank" class="wave-modal-link">📁 GitHub 저장소 바로가기</a>
          <a href="mailto:duwltjs5921@gmail.com" class="wave-modal-link">✉️ 이메일 보내기</a>
        </div>
        <button class="wave-modal-close" @click="showWaveModal = false">닫기</button>
      </div>
    </div>
  </div>
</template>
