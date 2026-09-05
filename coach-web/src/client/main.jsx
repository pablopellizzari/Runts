import React, { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Navigate, NavLink, Route, Routes, useLocation, useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, ArrowRight, CalendarDays, CheckCircle, ChevronLeft, ChevronRight, ClipboardCopy, GripVertical, Heart, Home, Link2, LogOut, Plus, Route as RouteIcon, Save, Trash2, Trophy, UsersRound, X } from 'lucide-react'
import './styles.css'

const AuthContext = createContext(null)
const dayNames = ['DOM', 'SEG', 'TER', 'QUA', 'QUI', 'SEX', 'SÁB']
const routeLabels = { ROAD: 'Asfalto', TRAIL: 'Trilha', TRACK: 'Pista', TREADMILL: 'Esteira', MIXED: 'Misto' }
const workoutLabels = { RODAGEM: 'Rodagem', TIROS: 'Tiros', TEMPO_RUN: 'Ritmo', LONGAO: 'Longão', FORTALECIMENTO: 'Fortalecimento', PROVA: 'Prova' }
const segmentLabels = { WARMUP: 'Aquecimento', RUN: 'Corrida', INTERVAL: 'Tiro', RECOVERY: 'Recuperação', COOLDOWN: 'Desaquecimento', REST: 'Pausa' }

async function api(path, options = {}) {
  const token = sessionStorage.getItem('runts.coach.token')
  const response = await fetch(`/api${path}`, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}), ...options.headers },
  })
  if (response.status === 204) return null
  const body = await response.json().catch(() => ({}))
  if (response.status === 401 && token) {
    sessionStorage.removeItem('runts.coach.token')
    sessionStorage.removeItem('runts.coach.user')
    window.location.assign('/entrar')
  }
  if (!response.ok) throw new Error(body.error || 'Não foi possível concluir a operação.')
  return body
}

function AuthProvider({ children }) {
  const [user, setUser] = useState(() => JSON.parse(sessionStorage.getItem('runts.coach.user') || 'null'))
  const login = data => {
    sessionStorage.setItem('runts.coach.token', data.token)
    sessionStorage.setItem('runts.coach.user', JSON.stringify(data.user))
    setUser(data.user)
  }
  const logout = () => {
    sessionStorage.removeItem('runts.coach.token')
    sessionStorage.removeItem('runts.coach.user')
    setUser(null)
  }
  return <AuthContext.Provider value={{ user, login, logout }}>{children}</AuthContext.Provider>
}

function Brand() {
  return <div className="brand"><span className="brand-mark" />RUNTS <small>COACH</small></div>
}

function AuthPage() {
  const [mode, setMode] = useState('login')
  const [form, setForm] = useState({ name: '', email: '', password: '', timezone: Intl.DateTimeFormat().resolvedOptions().timeZone })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const auth = useContext(AuthContext)
  const submit = async event => {
    event.preventDefault(); setError(''); setLoading(true)
    try { auth.login(await api(`/auth/${mode}`, { method: 'POST', body: JSON.stringify(form) })) }
    catch (e) { setError(e.message) } finally { setLoading(false) }
  }
  return <main className="auth-page">
    <section className="auth-visual">
      <Brand />
      <div className="track-lines" aria-hidden="true"><i /><i /><i /></div>
      <div><span className="eyebrow">PLANEJAMENTO DE CORRIDA</span><h1>Treinos claros.<br /><em>Alunos em movimento.</em></h1><p>Organize a semana de cada atleta e mantenha o plano sincronizado com o aplicativo.</p></div>
      <div className="auth-metric"><strong>DOM → SÁB</strong><span>Planejamento semanal em um só lugar</span></div>
    </section>
    <section className="auth-panel">
      <form className="auth-card" onSubmit={submit}>
        <div className="mobile-brand"><Brand /></div>
        <span className="eyebrow">ÁREA DO TREINADOR</span>
        <h2>{mode === 'login' ? 'Bem-vindo de volta' : 'Crie sua conta'}</h2>
        <p>{mode === 'login' ? 'Entre para acessar seus alunos e treinos.' : 'O painel é exclusivo para treinadores.'}</p>
        {mode === 'register' && <label>Nome completo<input required minLength="2" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} placeholder="Seu nome" /></label>}
        <label>E-mail<input required type="email" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} placeholder="voce@email.com" /></label>
        <label>Senha<input required type="password" minLength="8" value={form.password} onChange={e => setForm({ ...form, password: e.target.value })} placeholder="Mínimo de 8 caracteres" /></label>
        {error && <div className="error-banner">{error}</div>}
        <button className="primary full" disabled={loading}>{loading ? 'AGUARDE…' : mode === 'login' ? 'ENTRAR' : 'CRIAR CONTA'}</button>
        <button type="button" className="text-button" onClick={() => { setMode(mode === 'login' ? 'register' : 'login'); setError('') }}>
          {mode === 'login' ? 'Ainda não tem conta? Cadastre-se' : 'Já tem uma conta? Entrar'}
        </button>
      </form>
    </section>
  </main>
}

function Layout() {
  const { user, logout } = useContext(AuthContext)
  return <div className="app-shell">
    <aside className="sidebar">
      <Brand />
      <nav>
        <NavLink to="/" end><Home />Visão geral</NavLink>
        <NavLink to="/vinculacao"><Link2 />Vinculação</NavLink>
      </nav>
      <div className="sidebar-user"><div className="avatar">{initials(user?.name)}</div><div><strong>{user?.name}</strong><span>Treinador</span></div><button aria-label="Sair" onClick={logout}><LogOut /></button></div>
    </aside>
    <div className="content"><Routes>
      <Route index element={<StudentsPage />} />
      <Route path="alunos" element={<StudentsPage />} />
      <Route path="alunos/:studentId" element={<StudentWeekPage />} />
      <Route path="alunos/:studentId/treino/:date" element={<WorkoutEditorPage />} />
      <Route path="alunos/:studentId/conclusao/:workoutId" element={<CompletionDetailPage />} />
      <Route path="vinculacao" element={<LinkCodePage />} />
    </Routes></div>
  </div>
}

function PageHeader({ eyebrow, title, description, children }) {
  return <header className="page-header"><div><span className="eyebrow">{eyebrow}</span><h1>{title}</h1>{description && <p>{description}</p>}</div>{children}</header>
}

function StudentsPage() {
  const [students, setStudents] = useState([])
  const [state, setState] = useState('loading')
  const [error, setError] = useState('')
  useEffect(() => { api('/students').then(data => { setStudents(data); setState('ready') }).catch(e => { setError(e.message); setState('error') }) }, [])
  return <>
    <PageHeader eyebrow="PAINEL" title="Seus alunos" description="Escolha um aluno para planejar ou revisar a semana de treinos."><div className="date-chip"><CalendarDays />{new Intl.DateTimeFormat('pt-BR', { dateStyle: 'long' }).format(new Date())}</div></PageHeader>
    {state === 'loading' && <div className="empty-state">Carregando alunos…</div>}
    {state === 'error' && <div className="error-banner">{error}</div>}
    {state === 'ready' && students.length === 0 && <div className="empty-state"><UsersRound /><h3>Nenhum aluno vinculado</h3><p>Compartilhe seu código de vinculação para o aluno inserir no aplicativo.</p><NavLink className="primary" to="/vinculacao">VER CÓDIGO</NavLink></div>}
    <div className="student-grid">{students.map(student => <NavLink className="student-card" key={student.id} to={`/alunos/${student.id}`} state={{ student }}>
      <div className="avatar large">{initials(student.name)}</div><div className="student-main"><span>ATLETA</span><h3>{student.name}</h3><p>{student.email}</p></div>
      <div className="student-stat"><strong>{student.upcoming_workouts || 0}</strong><span>treinos futuros</span></div><ArrowRight className="card-arrow" />
    </NavLink>)}</div>
  </>
}

function startOfWeek(date = new Date()) {
  const local = new Date(date.getFullYear(), date.getMonth(), date.getDate(), 12)
  local.setDate(local.getDate() - local.getDay())
  return local
}
function todayInTimezone(timezone) {
  const parts = new Intl.DateTimeFormat('en-CA', { timeZone: timezone || 'America/Sao_Paulo', year: 'numeric', month: '2-digit', day: '2-digit' }).formatToParts(new Date())
  const values = Object.fromEntries(parts.map(part => [part.type, part.value]))
  return new Date(Number(values.year), Number(values.month) - 1, Number(values.day), 12)
}
function iso(date) { return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}` }
function addDays(date, count) { const next = new Date(date); next.setDate(next.getDate() + count); return next }
function initials(name = '') { return name.split(/\s+/).filter(Boolean).slice(0, 2).map(value => value[0]).join('').toUpperCase() || 'RT' }

function StudentWeekPage() {
  const { studentId } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const { user } = useContext(AuthContext)
  const coachToday = useMemo(() => todayInTimezone(user?.timezone), [user?.timezone])
  const [week, setWeek] = useState(() => startOfWeek(coachToday))
  const [workouts, setWorkouts] = useState([])
  const [races, setRaces] = useState([])
  const [favorites, setFavorites] = useState([])
  const [student, setStudent] = useState(() => location.state?.student || null)
  const [selectedDay, setSelectedDay] = useState(null)
  const [error, setError] = useState('')
  const days = useMemo(() => Array.from({ length: 7 }, (_, i) => addDays(week, i)), [week])
  const load = async () => {
    try {
      const [studentList, weekWorkouts, weekRaces, favoriteList] = await Promise.all([api('/students'), api(`/students/${studentId}/workouts?start=${iso(week)}`), api(`/students/${studentId}/races?start=${iso(week)}`), api('/favorites')])
      setStudent(studentList.find(item => item.id === studentId)); setWorkouts(weekWorkouts); setRaces(weekRaces); setFavorites(favoriteList)
    } catch (e) { setError(e.message) }
  }
  useEffect(() => {
    load()
    const timer = window.setInterval(load, 15000)
    return () => window.clearInterval(timer)
  }, [studentId, week])
  useEffect(() => {
    const context = document.modelContext
    if (!context?.registerTool) return
    const lifecycle = new AbortController()
    Promise.resolve(context.registerTool({
      name: 'start_workout_creation',
      title: 'Iniciar criação de treino',
      description: 'Abre o editor de um novo treino para este aluno em uma data da semana.',
      inputSchema: { type: 'object', properties: { date: { type: 'string', pattern: '^\\d{4}-\\d{2}-\\d{2}$' } }, required: ['date'], additionalProperties: false },
      annotations: { readOnlyHint: false, untrustedContentHint: false },
      execute(input) {
        if (!/^\d{4}-\d{2}-\d{2}$/.test(input?.date || '')) throw new Error('Data inválida.')
        navigate(`/alunos/${studentId}/treino/${input.date}`, { state: { student } })
        return { status: 'editor_opened', studentId, date: input.date }
      },
    }, { signal: lifecycle.signal })).catch(() => {})
    return () => lifecycle.abort()
  }, [navigate, student, studentId])
  const chooseDay = date => {
    const workout = workouts.find(item => item.target_date === iso(date))
    if (workout?.execution) navigate(`/alunos/${studentId}/conclusao/${workout.id}`, { state: { student, workout } })
    else if (workout) navigate(`/alunos/${studentId}/treino/${iso(date)}`, { state: { student, workout } })
    else setSelectedDay(date)
  }
  const useFavorite = async favorite => {
    try {
      await api(`/students/${studentId}/workouts/from-favorite`, { method: 'POST', body: JSON.stringify({ favoriteId: favorite.id, targetDate: iso(selectedDay) }) })
      setSelectedDay(null); load()
    } catch (e) { setError(e.message) }
  }
  return <>
    <button className="back-link" onClick={() => navigate('/alunos')}><ArrowLeft />Todos os alunos</button>
    <PageHeader eyebrow="PLANEJAMENTO SEMANAL" title={student?.name || 'Aluno'} description="Clique em um dia para criar, vincular ou editar o treino." />
    <div className="week-toolbar"><button aria-label="Semana anterior" onClick={() => setWeek(addDays(week, -7))}><ChevronLeft /></button><div><strong>{new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: 'short' }).format(days[0])} — {new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: 'short', year: 'numeric' }).format(days[6])}</strong><span>Horário do treinador</span></div><button aria-label="Próxima semana" onClick={() => setWeek(addDays(week, 7))}><ChevronRight /></button></div>
    {error && <div className="error-banner">{error}</div>}
    <div className="week-grid">{days.map((day, index) => {
      const workout = workouts.find(item => item.target_date === iso(day))
      const race = races.find(item => item.date.slice(0, 10) === iso(day))
      const today = iso(day) === iso(coachToday)
      return <article key={iso(day)} className={`day-card ${workout ? 'has-workout' : ''} ${today ? 'today' : ''}`}>
        <div className="day-head"><span>{dayNames[index]}</span><strong>{day.getDate()}</strong>{today && <i>HOJE</i>}</div>
        {race && <div className="race-badge"><Trophy /><div><strong>{race.name}</strong><span>{race.modality}</span></div></div>}
        {workout ? <button className={`workout-summary ${workout.execution ? 'completed' : ''}`} onClick={() => chooseDay(day)}>{workout.execution && <span className="completed-label"><CheckCircle />CONCLUÍDO</span>}<span className="workout-type">{workoutLabels[workout.workout_type] || workout.workout_type}</span><h3>{workout.name || workout.description.split('\n')[0]}</h3><p><RouteIcon />{routeLabels[workout.route_type] || 'Percurso livre'}</p><div><strong>{workout.target_distance_km} km</strong><span>{workout.target_duration_minutes} min</span></div><small>{workout.execution ? `PSE ${workout.execution.pse}/10` : `ESFORÇO ${workout.effort || '—'}/10`}</small></button> : <button className="day-empty" onClick={() => chooseDay(day)}><Plus /><strong>Adicionar treino</strong><span>{race ? 'Sem treino' : 'Dia livre'}</span></button>}
      </article>
    })}</div>
    {selectedDay && <div className="modal-backdrop" onMouseDown={() => setSelectedDay(null)}><section className="modal" onMouseDown={e => e.stopPropagation()}><button className="close" onClick={() => setSelectedDay(null)}><X /></button><span className="eyebrow">{new Intl.DateTimeFormat('pt-BR', { weekday: 'long', day: '2-digit', month: 'long' }).format(selectedDay)}</span><h2>Adicionar treino</h2><button className="choice-card" onClick={() => navigate(`/alunos/${studentId}/treino/${iso(selectedDay)}`, { state: { student } })}><Plus /><div><strong>Criar novo treino</strong><span>Monte os segmentos do zero</span></div><ChevronRight /></button><h4>OU USE UM FAVORITO</h4>{favorites.length ? favorites.map(favorite => <button className="favorite-row" key={favorite.id} onClick={() => useFavorite(favorite)}><Heart /><div><strong>{favorite.name}</strong><span>{favorite.target_distance_km} km · esforço {favorite.effort}/10</span></div><Plus /></button>) : <p className="muted">Você ainda não favoritou nenhum treino.</p>}</section></div>}
  </>
}

const blankSegment = () => ({ id: crypto.randomUUID(), type: 'RUN', durationType: 'TIME', durationValue: 10, intensity: 'Z2', notes: '' })
function normalizeWorkout(workout, date) {
  if (!workout) return { name: '', routeType: 'ROAD', effort: 5, workoutType: 'RODAGEM', targetDate: date, targetDistanceKm: 5, targetDurationMinutes: 30, targetPace: '6:00', targetHeartRateZone: 'Z2', notes: '', favorite: false, segments: [blankSegment()] }
  return { name: workout.name || workout.description.split('\n')[0], routeType: workout.route_type || 'ROAD', effort: workout.effort || 5, workoutType: workout.workout_type, targetDate: workout.target_date, targetDistanceKm: Number(workout.target_distance_km), targetDurationMinutes: Number(workout.target_duration_minutes), targetPace: workout.target_pace, targetHeartRateZone: workout.target_hr_zone, notes: '', favorite: false, segments: workout.segments?.length ? workout.segments.map(segment => ({ ...segment, durationType: 'TIME', durationValue: Number(segment.durationValue) })) : [blankSegment()] }
}

function WorkoutEditorPage() {
  const { studentId, date } = useParams(); const navigate = useNavigate()
  const location = useLocation(); const passed = location.state || {}
  const [form, setForm] = useState(() => normalizeWorkout(passed.workout, date))
  const [dragIndex, setDragIndex] = useState(null); const [saving, setSaving] = useState(false); const [error, setError] = useState('')
  const update = (key, value) => setForm(current => ({ ...current, [key]: value }))
  const updateSegment = (index, key, value) => setForm(current => ({ ...current, segments: current.segments.map((segment, position) => position === index ? { ...segment, [key]: value } : segment) }))
  const moveSegment = target => {
    if (dragIndex === null || dragIndex === target) return
    setForm(current => { const next = [...current.segments]; const [item] = next.splice(dragIndex, 1); next.splice(target, 0, item); return { ...current, segments: next } }); setDragIndex(target)
  }
  const submit = async event => {
    event.preventDefault(); setSaving(true); setError('')
    try { await api(`/students/${studentId}/workouts`, { method: 'POST', body: JSON.stringify(form) }); navigate(`/alunos/${studentId}`) }
    catch (e) { setError(e.message) } finally { setSaving(false) }
  }
  const removeWorkout = async () => {
    if (!passed.workout?.id || !confirm('Remover este treino do dia?')) return
    try { await api(`/students/${studentId}/workouts/${passed.workout.id}`, { method: 'DELETE' }); navigate(`/alunos/${studentId}`) } catch (e) { setError(e.message) }
  }
  return <form className="editor" onSubmit={submit}>
    <button type="button" className="back-link" onClick={() => navigate(`/alunos/${studentId}`)}><ArrowLeft />Voltar à semana</button>
    <PageHeader eyebrow={passed.workout ? 'EDITAR TREINO' : 'NOVO TREINO'} title={new Intl.DateTimeFormat('pt-BR', { weekday: 'long', day: '2-digit', month: 'long', timeZone: 'UTC' }).format(new Date(`${date}T12:00:00Z`))} description={passed.student?.name ? `Para ${passed.student.name}` : ''}><button type="submit" className="primary" disabled={saving}><Save />{saving ? 'SALVANDO…' : 'SALVAR TREINO'}</button></PageHeader>
    {error && <div className="error-banner">{error}</div>}
    <div className="editor-grid"><section className="form-card"><h2>Informações gerais</h2><div className="form-grid">
      <label className="span-2">Nome do treino<input required minLength="2" value={form.name} onChange={e => update('name', e.target.value)} placeholder="Ex.: Intervalado de velocidade" /></label>
      <label>Tipo de percurso<select value={form.routeType} onChange={e => update('routeType', e.target.value)}>{Object.entries(routeLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></label>
      <label>Tipo de treino<select value={form.workoutType} onChange={e => update('workoutType', e.target.value)}>{Object.entries(workoutLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></label>
      <label>Distância total (km)<input type="number" min="0" step="0.1" value={form.targetDistanceKm} onChange={e => update('targetDistanceKm', Number(e.target.value))} /></label>
      <label>Duração total (min)<input type="number" min="0" value={form.targetDurationMinutes} onChange={e => update('targetDurationMinutes', Number(e.target.value))} /></label>
      <label>Ritmo alvo<input required value={form.targetPace} onChange={e => update('targetPace', e.target.value)} placeholder="5:30" /></label>
      <label>Zona alvo<input required value={form.targetHeartRateZone} onChange={e => update('targetHeartRateZone', e.target.value)} placeholder="Z2" /></label>
      <label className="span-2 effort-label"><span>Esforço percebido <strong>{form.effort}/10</strong></span><input type="range" min="1" max="10" value={form.effort} onChange={e => update('effort', Number(e.target.value))} /></label>
      <label className="span-2">Observações gerais<textarea value={form.notes} onChange={e => update('notes', e.target.value)} placeholder="Orientações para o aluno" /></label>
    </div></section>
    <aside className="editor-aside"><div className="effort-gauge"><span>ESFORÇO</span><strong>{form.effort}</strong><i>/10</i><div style={{ '--effort': `${form.effort * 10}%` }} /></div><label className="favorite-toggle"><input type="checkbox" checked={form.favorite} onChange={e => update('favorite', e.target.checked)} /><Heart />Salvar também nos favoritos</label>{passed.workout && <button type="button" className="danger" onClick={removeWorkout}><Trash2 />Remover do dia</button>}</aside></div>
    <section className="segments-section"><div className="section-title"><div><span className="eyebrow">ESTRUTURA</span><h2>Segmentos do treino</h2><p>Arraste os cartões para alterar a ordem.</p></div><button type="button" className="secondary" onClick={() => update('segments', [...form.segments, blankSegment()])}><Plus />ADICIONAR SEGMENTO</button></div>
      <div className="segments">{form.segments.map((segment, index) => <article className="segment-card" key={segment.id} draggable onDragStart={() => setDragIndex(index)} onDragOver={e => { e.preventDefault(); moveSegment(index) }} onDragEnd={() => setDragIndex(null)}>
        <div className="drag-handle"><GripVertical /><span>{String(index + 1).padStart(2, '0')}</span></div><div className="segment-fields">
          <label>Tipo<select value={segment.type} onChange={e => updateSegment(index, 'type', e.target.value)}>{Object.entries(segmentLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></label>
          <label>Duração (minutos)<div className="unit-input"><input type="number" min="1" step="1" value={segment.durationValue} onChange={e => updateSegment(index, 'durationValue', Number(e.target.value))} /><span>min</span></div></label>
          <label>Intensidade<input required value={segment.intensity} onChange={e => updateSegment(index, 'intensity', e.target.value)} placeholder="Z2 ou 5:00/km" /></label>
          <label className="segment-notes">Observação<input value={segment.notes} onChange={e => updateSegment(index, 'notes', e.target.value)} placeholder="Instruções deste segmento" /></label>
        </div><button type="button" className="icon-danger" aria-label="Remover segmento" disabled={form.segments.length === 1} onClick={() => update('segments', form.segments.filter((_, position) => position !== index))}><Trash2 /></button>
      </article>)}</div>
    </section>
  </form>
}

function durationText(seconds) {
  const value = Number(seconds || 0)
  return `${String(Math.floor(value / 3600)).padStart(2, '0')}:${String(Math.floor(value / 60) % 60).padStart(2, '0')}:${String(value % 60).padStart(2, '0')}`
}

function CompletionDetailPage() {
  const { studentId } = useParams(); const navigate = useNavigate(); const location = useLocation()
  const { workout, student } = location.state || {}; const execution = workout?.execution
  if (!workout || !execution) return <><button className="back-link" onClick={() => navigate(`/alunos/${studentId}`)}><ArrowLeft />Voltar</button><div className="empty-state">Registro concluído não encontrado.</div></>
  return <>
    <button className="back-link" onClick={() => navigate(`/alunos/${studentId}`)}><ArrowLeft />Voltar à semana</button>
    <PageHeader eyebrow="TREINO CONCLUÍDO" title={workout.name || 'Treino'} description={`Feedback de ${student?.name || 'aluno'}`} />
    <div className="comparison-grid"><section className="form-card"><h2>Previsto × realizado</h2>{execution.sourceProvider === 'STRAVA' && <div className="imported-source">Dados da atividade importados do Strava</div>}<div className="comparison-row"><span>Distância</span><strong>{workout.target_distance_km} km</strong><b>{execution.distanceKm} km</b></div><div className="comparison-row"><span>Duração</span><strong>{workout.target_duration_minutes} min</strong><b>{durationText(execution.durationSeconds)}</b></div><div className="comparison-row"><span>Ritmo</span><strong>{workout.target_pace} /km</strong><b>{execution.pace} /km</b></div><div className="comparison-row"><span>FC média</span><strong>{workout.target_hr_zone}</strong><b>{execution.avgHeartRate ? `${execution.avgHeartRate} bpm` : 'Não informada'}</b></div></section><aside className="feedback-card"><CheckCircle /><span>PERCEPÇÃO DE ESFORÇO</span><strong>{execution.pse}/10</strong><h3>Feedback do atleta</h3><p>{execution.comments || 'Nenhuma observação informada.'}</p></aside></div>
  </>
}

function LinkCodePage() {
  const { user, login } = useContext(AuthContext); const [copied, setCopied] = useState(false); const [freshUser, setFreshUser] = useState(user)
  useEffect(() => { api('/me').then(setFreshUser).catch(() => {}) }, [])
  const code = freshUser?.invite_code || '—'
  const copy = async () => { await navigator.clipboard.writeText(code); setCopied(true); setTimeout(() => setCopied(false), 1800) }
  return <><PageHeader eyebrow="VINCULAÇÃO" title="Código do treinador" description="O aluno insere este código no aplicativo Runts para aparecer na sua lista." />
    <section className="link-card"><div className="link-icon"><Link2 /></div><span>SEU CÓDIGO</span><strong>{code}</strong><button className="primary" onClick={copy}><ClipboardCopy />{copied ? 'COPIADO!' : 'COPIAR CÓDIGO'}</button><div className="steps"><div><i>1</i><p>O aluno abre <b>Perfil → Vínculo com Treinador</b>.</p></div><div><i>2</i><p>Ele informa o código exibido acima.</p></div><div><i>3</i><p>O aluno aparece automaticamente no seu painel.</p></div></div></section>
  </>
}

function ProtectedApp() { const { user } = useContext(AuthContext); return user ? <Layout /> : <Navigate to="/entrar" replace /> }
function Root() { const { user } = useContext(AuthContext); return <Routes><Route path="/entrar" element={user ? <Navigate to="/" replace /> : <AuthPage />} /><Route path="/*" element={<ProtectedApp />} /></Routes> }

createRoot(document.getElementById('root')).render(<React.StrictMode><BrowserRouter><AuthProvider><Root /></AuthProvider></BrowserRouter></React.StrictMode>)
