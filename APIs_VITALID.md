# 📋 APIs VITALID - ESPECIFICACIÓN COMPLETA

---

## 🔐 AUTENTICACIÓN

### Iniciar Sesión
```
POST /api/auth/login
Request: { email: string, password: string }
Response: { id: int, name: string, email: string, type: string, token: string }
```

### Registrarse
```
POST /api/auth/register
Request: { email: string, password: string, name: string, phone: string, type: string }
Response: { id: int, message: string, token: string }
```

### Cerrar Sesión
```
POST /api/auth/logout
Request: {}
Response: { message: string }
```

---

## 👨‍⚕️ MÉDICOS

### Obtener todos los médicos
```
GET /api/doctors
Request: {}
Response: [
  { id: int, name: string, specialty: string, avatar: string, status: string, unreadMessages: int }
]
```

### Obtener médico por ID
```
GET /api/doctors/{doctorId}
Request: {}
Response: { id: int, name: string, specialty: string, avatar: string, status: string, unreadMessages: int }
```

### Obtener médicos por especialidad
```
GET /api/doctors/specialty/{specialty}
Request: {}
Response: [{ id: int, name: string, specialty: string, avatar: string, status: string, unreadMessages: int }]
```

### Actualizar estado del médico
```
PUT /api/doctors/{doctorId}/status
Request: { status: string }
Response: { message: string }
```

---

## 💬 CHAT

### Obtener mensajes con médico
```
GET /api/chat/doctor/{doctorId}
Request: {}
Response: [
  { id: int, sender: string, content: string, timestamp: string }
]
```

### Enviar mensaje
```
POST /api/chat/send
Request: { doctorId: int, content: string, senderId: string }
Response: { id: int, sender: string, content: string, timestamp: string }
```

### Obtener mensajes sin leer
```
GET /api/chat/unread
Request: {}
Response: [
  { doctorId: int, unreadCount: int }
]
```

### Marcar mensajes como leídos
```
PUT /api/chat/read/{doctorId}
Request: {}
Response: { message: string }
```

---

## 🏥 TRATAMIENTOS

### Obtener todos los tratamientos
```
GET /api/treatments
Request: {}
Response: [
  { id: int, title: string, doctor: string, status: string, progress: int, nextAppointment: string, medications: [string] }
]
```

### Obtener tratamientos activos
```
GET /api/treatments/active
Request: {}
Response: [
  { id: int, title: string, doctor: string, status: string, progress: int, nextAppointment: string, medications: [string] }
]
```

### Obtener tratamientos completados
```
GET /api/treatments/completed
Request: {}
Response: [
  { id: int, title: string, doctor: string, status: string, progress: int }
]
```

### Crear tratamiento
```
POST /api/treatments
Request: { patientId: string, title: string, status: string, medications: [string], nextAppointment: string }
Response: { id: int, title: string, status: string }
```

### Actualizar progreso del tratamiento
```
PUT /api/treatments/{treatmentId}/progress
Request: { progress: int }
Response: { message: string }
```

---

## 💊 MEDICAMENTOS

### Obtener todos los medicamentos
```
GET /api/medications
Request: {}
Response: [
  { id: int, name: string, dosage: string, frequency: string, prescribedBy: string, startDate: string, endDate: string }
]
```

### Crear medicamento
```
POST /api/medications
Request: { patientId: string, name: string, dosage: string, frequency: string, startDate: string, endDate: string }
Response: { id: int, name: string, dosage: string }
```

### Registrar dosis tomada
```
POST /api/medications/{medicationId}/take-dose
Request: { timestamp: string }
Response: { message: string }
```

---

## ⏰ CHECKLIST DE MEDICAMENTOS

### Crear checklist con múltiples medicamentos
```
POST /api/checklist
Request: {
  medications: [
    { medicationId: string, scheduledTimes: ["07:00", "14:00", "21:00"] },
    { medicationId: string, scheduledTimes: ["08:00", "20:00"] },
    { medicationId: string, scheduledTimes: ["09:00"] }
  ]
}
Response: { id: int, medicationsAdded: int, message: string }
```

### Obtener checklist de medicamentos de hoy
```
GET /api/checklist/today
Request: {}
Response: [
  {
    checklistId: int
    medicationId: int
    medicationName: string
    dosage: string
    scheduledTimes: [
      { time: "07:00", taken: boolean, actualTime: string },
      { time: "14:00", taken: boolean, actualTime: string },
      { time: "21:00", taken: boolean, actualTime: string }
    ]
  }
]
```

### Marcar dosis como tomada
```
POST /api/checklist/{medicationId}/mark-taken
Request: { time: string, timestamp: string }
Response: { message: string }
```

### Obtener resumen del checklist
```
GET /api/checklist/today/summary
Request: {}
Response: {
  totalMedications: int
  totalScheduledDoses: int
  takenDoses: int
  pendingDoses: int
  percentage: int
}
```

### Obtener medicamentos pendientes del checklist
```
GET /api/checklist/pending
Request: {}
Response: [
  {
    medicationId: string
    medicationName: string
    dosage: string
    scheduledTime: string
    isOverdue: boolean
  }
]
```

### Obtener historial de adherencia del checklist
```
GET /api/checklist/{medicationId}/adherence
Request: { from: string, to: string }
Response: [
  {
    date: string
    scheduledTimes: int
    takenTimes: int
    percentage: int
  }
]
```

### Actualizar horarios del checklist
```
PUT /api/checklist
Request: {
  medications: [
    { medicationId: string, scheduledTimes: ["07:00", "14:00"] },
    { medicationId: string, scheduledTimes: ["09:00", "18:00"] }
  ]
}
Response: { message: string }
```

### Obtener estadísticas del checklist
```
GET /api/checklist/statistics
Request: { month: string }
Response: {
  adherenceRate: int (%)
  medicationsTakenOnTime: int
  missedDoses: int
  skippedDoses: int
  trend: string
}
```

---

## 📅 CITAS

### Obtener todas las citas
```
GET /api/appointments
Request: { page: int, size: int }
Response: { content: [Appointment], totalElements: int, totalPages: int, currentPage: int }

Appointment: { id: int, date: string, time: string, doctor: string, reason: string, status: string }
```

### Agendar cita
```
POST /api/appointments
Request: { doctorId: int, date: string, time: string, reason: string }
Response: { id: int, date: string, time: string, doctor: string, status: string }
```

### Cancelar cita
```
DELETE /api/appointments/{appointmentId}
Request: {}
Response: { message: string }
```

### Obtener citas del médico
```
GET /api/doctors/appointments
Request: {}
Response: [
  { id: int, date: string, time: string, patientName: string, reason: string, status: string }
]
```

---

## 📊 DATOS DE SALUD

### Obtener presión arterial
```
GET /api/health/blood-pressure
Request: {}
Response: [
  { date: string, value: int }
]
```

### Obtener glucosa en sangre
```
GET /api/health/blood-sugar
Request: {}
Response: [
  { date: string, value: int }
]
```

### Registrar métrica de salud
```
POST /api/health/metrics
Request: { metric: string, value: int, timestamp: string }
Response: { message: string }
```

### Obtener historial de salud
```
GET /api/health/history
Request: { metric: string, from: string, to: string }
Response: [
  { date: string, value: int }
]
```

---

## 👤 PERFIL

### Obtener perfil
```
GET /api/profile
Request: {}
Response: { id: int, name: string, email: string, phone: string, type: string, avatar: string, bloodType: string, allergies: [string], dateOfBirth: string }
```

### Actualizar perfil
```
PUT /api/profile
Request: { name: string, phone: string, avatar: string, bloodType: string, allergies: [string], specialty: string }
Response: { id: int, name: string, message: string }
```

### Cambiar contraseña
```
PUT /api/profile/password
Request: { oldPassword: string, newPassword: string }
Response: { message: string }
```

---

## 📊 RESUMEN TOTAL

| Módulo | Endpoints |
|--------|-----------|
| Autenticación | 3 |
| Médicos | 4 |
| Chat | 4 |
| Tratamientos | 5 |
| Medicamentos | 3 |
| Checklist | 8 |
| Citas | 4 |
| Datos de Salud | 4 |
| Perfil | 3 |
| **TOTAL** | **38** |

---

**Documento Generado:** 03 de Mayo de 2026
**Proyecto:** Vitalid - Plataforma de Telemedicina
**Versión:** 1.0