// ────────────────────────────────────────────────
// 데이터 정의
// ────────────────────────────────────────────────
const STORAGE_KEY = 'reinnew_spaces';

// 최초 접속 시 사용할 샘플 데이터 (localStorage에 아무것도 없을 때만 씀)
const SEED_DATA = [
    { id: 1, name: '강의실 1', location: '1층 실습실 1004', group: '강의실',
        online: true, temp: 32, tempDesc: '맑음', humidity: 85, humidityDesc: '습도 양호',
        co2: 420, status: 'ok' },
    { id: 2, name: '강의실 2', location: '1층 실습실 1005', group: '강의실',
        online: true, temp: 28, tempDesc: '구름 많음', humidity: 62, humidityDesc: '습도 보통',
        co2: 850, status: 'warn' },
    { id: 3, name: '세미나실 A', location: '2층 세미나실 A', group: '세미나실',
        online: true, temp: 24, tempDesc: '흐림', humidity: 40, humidityDesc: '습도 낮음',
        co2: 1200, status: 'danger' },
    { id: 4, name: '스터디룸 1', location: '2층 스터디룸 1', group: '스터디룸',
        online: true, temp: 30, tempDesc: '맑음', humidity: 70, humidityDesc: '습도 보통',
        co2: 500, status: 'ok' },
    { id: 5, name: '강의실 3', location: '3층 실습실 3001', group: '강의실',
        online: true, temp: 31, tempDesc: '구름 많음', humidity: 78, humidityDesc: '습도 보통',
        co2: 600, status: 'ok' },
    { id: 6, name: '회의실 B', location: '3층 회의실 B', group: '회의실',
        online: false, temp: null, tempDesc: null, humidity: null, humidityDesc: null,
        co2: null, status: 'off' },
];

let spaces = [];
let nextId = 1;

// ────────────────────────────────────────────────
// localStorage 저장/로드
// ────────────────────────────────────────────────
function loadSpaces() {
    try {
        const raw = localStorage.getItem(STORAGE_KEY);
        if (raw) {
            spaces = JSON.parse(raw);
        } else {
            spaces = SEED_DATA;
            saveSpaces();
        }
    } catch (e) {
        console.error('저장된 데이터를 불러오지 못했어요:', e);
        spaces = SEED_DATA;
    }
    nextId = spaces.reduce((max, s) => Math.max(max, s.id), 0) + 1;
}

function saveSpaces() {
    try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(spaces));
    } catch (e) {
        console.error('데이터를 저장하지 못했어요:', e);
    }
}

// ────────────────────────────────────────────────
// 아이콘 (상태별 썸네일 배경/CO2 도넛 색상)
// ────────────────────────────────────────────────
const STATUS_META = {
    ok:     { badgeClass: 'ok',     badgeLabel: '정상', thumbBg: '#e7f6ec', thumbStroke: '#3f9d63' },
    warn:   { badgeClass: 'warn',   badgeLabel: '주의', thumbBg: '#fdf1dd', thumbStroke: '#d99b2b' },
    danger: { badgeClass: 'danger', badgeLabel: '위험', thumbBg: '#e7f6ec', thumbStroke: '#3f9d63' },
    off:    { badgeClass: 'off',    badgeLabel: '오프라인', thumbBg: '#f0f1f4', thumbStroke: '#9ca3af' },
};

function co2Ratio(co2) {
    if (co2 == null) return 0;
    return Math.max(0, Math.min(100, Math.round((co2 / 2000) * 100)));
}

function co2Color(status) {
    if (status === 'danger') return '#ef4444';
    if (status === 'warn') return '#f59e0b';
    return '#22c55e';
}

// ────────────────────────────────────────────────
// 렌더링
// ────────────────────────────────────────────────
function renderKpis() {
    const total = spaces.length;
    const okCount = spaces.filter(s => s.status === 'ok').length;
    const warnCount = spaces.filter(s => s.status === 'warn').length;
    const dangerCount = spaces.filter(s => s.status === 'danger').length;

    document.getElementById('kpi-total').textContent = total;
    document.getElementById('kpi-ok').textContent = okCount;
    document.getElementById('kpi-warn').textContent = warnCount;
    document.getElementById('kpi-danger').textContent = dangerCount;
}

function renderRow(space) {
    const meta = STATUS_META[space.status] || STATUS_META.off;
    const offlineClass = space.online ? '' : ' offline';

    if (!space.online) {
        return `
      <div class="row${offlineClass}" data-id="${space.id}">
        <div class="room">
          <span class="thumb" style="background:${meta.thumbBg};">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="${meta.thumbStroke}" stroke-width="1.7"><rect x="4" y="8" width="16" height="12" rx="1.5"/><path d="M8 8V5h8v3M8 12h3M13 12h3M8 16h3M13 16h3"/></svg>
          </span>
          <div><div class="rname">${escapeHtml(space.name)}</div><div class="rloc">${escapeHtml(space.location)}</div></div>
        </div>
        <div class="metric first" style="display:block;"><div class="dash">-</div><div class="nodata">데이터 없음</div></div>
        <div><div class="dash">-</div><div class="nodata">데이터 없음</div></div>
        <div><div class="dash">-</div><div class="nodata">데이터 없음</div></div>
        <div><span class="badge off">오프라인</span></div>
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#c3c8d0" stroke-width="2"><path d="M9 6l6 6-6 6"/></svg>
      </div>`;
    }

    return `
    <div class="row" data-id="${space.id}">
      <div class="room">
        <span class="thumb" style="background:${meta.thumbBg};">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="${meta.thumbStroke}" stroke-width="1.7"><rect x="4" y="8" width="16" height="12" rx="1.5"/><path d="M8 8V5h8v3M8 12h3M13 12h3M8 16h3M13 16h3"/></svg>
        </span>
        <div><div class="rname">${escapeHtml(space.name)}</div><div class="rloc">${escapeHtml(space.location)}</div></div>
      </div>
      <div class="metric first">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="1.6"><path d="M7 18h10a4 4 0 000-8 6 6 0 00-11.6 1.8A3.2 3.2 0 007 18z"/></svg>
        <div><div><span class="val">${space.temp}</span><span class="unit">°C</span></div><div class="cap">${escapeHtml(space.tempDesc || '')}</div></div>
      </div>
      <div class="metric">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="#60a5fa"><path d="M12 3s6 6.5 6 10.5A6 6 0 016 13.5C6 9.5 12 3 12 3z"/></svg>
        <div><div><span class="val">${space.humidity}</span><span class="unit">%</span></div><div class="cap">${escapeHtml(space.humidityDesc || '')}</div></div>
      </div>
      <div class="metric">
        <span class="donut" style="background:conic-gradient(${co2Color(space.status)} 0 ${co2Ratio(space.co2)}%, #eef1f4 0);"><span>CO2</span></span>
        <div><div class="val">${space.co2}</div><div class="cap">ppm</div></div>
      </div>
      <div><span class="badge ${meta.badgeClass}">${meta.badgeLabel}</span></div>
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#c3c8d0" stroke-width="2"><path d="M9 6l6 6-6 6"/></svg>
    </div>`;
}

function escapeHtml(str) {
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}

function renderRows() {
    const container = document.getElementById('rows-container');
    const pager = document.getElementById('pager');
    const emptyState = document.getElementById('empty-state');

    if (spaces.length === 0) {
        container.innerHTML = '';
        pager.style.display = 'none';
        emptyState.style.display = 'flex';
        return;
    }

    emptyState.style.display = 'none';
    pager.style.display = 'flex';
    container.innerHTML = spaces.map(renderRow).join('');
    document.getElementById('pager-count').textContent = `1–${spaces.length} of ${spaces.length}`;
}

function renderAll() {
    renderKpis();
    renderRows();
}

// ────────────────────────────────────────────────
// 모달 (공간 추가)
// ────────────────────────────────────────────────
function openModal() {
    document.getElementById('modal-overlay').classList.add('open');
    document.getElementById('space-name').focus();
}

function closeModal() {
    document.getElementById('modal-overlay').classList.remove('open');
    document.getElementById('add-space-form').reset();
}

function handleAddSpace(e) {
    e.preventDefault();
    const form = e.target;
    const name = form.name.value.trim();
    const location = form.location.value.trim();
    const group = form.group.value;

    if (!name || !location) return;

    const newSpace = {
        id: nextId++,
        name,
        location,
        group,
        online: false,
        temp: null, tempDesc: null,
        humidity: null, humidityDesc: null,
        co2: null,
        status: 'off',
    };

    spaces.push(newSpace);
    saveSpaces();
    renderAll();
    closeModal();
}

// ────────────────────────────────────────────────
// 초기화
// ────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    loadSpaces();
    renderAll();

    document.getElementById('btn-add-space').addEventListener('click', openModal);
    const emptyAddBtn = document.getElementById('btn-add-space-empty');
    if (emptyAddBtn) emptyAddBtn.addEventListener('click', openModal);

    document.getElementById('modal-close').addEventListener('click', closeModal);
    document.getElementById('modal-cancel').addEventListener('click', closeModal);
    document.getElementById('modal-overlay').addEventListener('click', (e) => {
        if (e.target.id === 'modal-overlay') closeModal();
    });
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') closeModal();
    });

    document.getElementById('add-space-form').addEventListener('submit', handleAddSpace);
});