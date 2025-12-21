export interface User {
  id: number
  username: string
  firstName: string
  lastName: string
  enabled: boolean
  roles: string[]
  createdAt: string
  updatedAt: string
}

export interface AuthResponse {
  token: string
  username: string
  firstName: string
  lastName: string
  roles: string[]
  createdAt: string
  message: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface VoiceType {
  id: number
  voiceName: string
  code: string
  createdAt: string
  updatedAt: string
}

export interface Transaction {
  id: number
  idUser: number
  transactionMethod: string
  amount: number
  tnxId: string
  date: string
  status: string
  updatedAt: string
  durationInSeconds?: number
  user?: User
}

export interface TopUpRequest {
  amount: number
  transactionMethod: string
  tnxId: string
}

export interface TopUpResponse {
  id: number
  idUser: number
  transactionMethod: string
  amount: number
  tnxId: string
  date: string
  status: string
  updatedAt: string
  durationInSeconds: number
  user?: User
}

export interface Balance {
  id: number
  purchaseAmount: number
  lastUsedAmount: number
  totalUsedAmount: number
  remainAmount: number
  idUser: number
}

export interface DashboardStats {
  totalUsers: number
  totalVoiceTypes: number
  totalTopUps: number
  totalCallHistory: number
}

export interface UserDetails {
  idUserDetails: number
  idUser: number
  user: User
  dateOfBirth: string | null
  gender: string | null
  address: string | null
  email: string | null
  profilePhoto: string | null
  selectedVoiceTypeId: number | null // User's selected default voice type for calls
  createdAt: string
  updatedAt: string
}

export interface CallHistory {
  id: number
  aparty: string
  bparty: string | null
  uuid: string
  sourceIp: string | null
  createTime: string
  startTime: string | null
  endTime: string | null
  duration: number
  status: string | null
  hangupCause: string | null
  codec: string | null
  idUser: number | null
  user?: User
}

export interface VoicePurchaseRequest {
  idVoiceType: number
  transactionMethod: string // "bkash", "nagad", "rocket"
  tnxId: string // Payment transaction ID
  subscriptionType: string // "monthly", "yearly"
}

export interface VoicePurchaseResponse {
  id: number
  idUser: number
  idVoiceType: number
  idTransaction: number | null
  transactionMethod: string | null // "bkash", "nagad", "rocket"
  tnxId: string | null // Payment transaction ID
  subscriptionType: string | null // "monthly", "yearly"
  amount: number
  purchaseDate: string
  expiryDate: string | null // When the subscription expires
  status: string // "pending", "approved", "rejected"
  updatedAt: string | null
  voiceType?: VoiceType
  user?: User
}

export interface VoiceUserMapping {
  id: number
  idUser: number
  idVoiceType: number
  isPurchased: boolean
  assignedAt: string
  trialExpiryDate: string | null // null = no trial (permanent), non-null = trial expires at this date
  expiryDate: string | null // null = permanent access, non-null = subscription expires at this date
  voiceType?: VoiceType
}
