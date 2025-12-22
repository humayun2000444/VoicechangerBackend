'use client'

import { useState, useEffect } from 'react'
import { DashboardLayout } from '@/components/DashboardLayout'
import { api } from '@/lib/api'
import { format } from 'date-fns'
import { Archive, Clock, TrendingDown, Filter, RefreshCw, BarChart3, AlertCircle } from 'lucide-react'

interface VoiceMappingHistory {
  id: number
  originalMappingId: number
  idUser: number
  idVoiceType: number
  isPurchased: boolean
  assignedAt: string
  trialExpiryDate: string | null
  expiryDate: string | null
  isDefault: boolean
  expiredAt: string
  expiryReason: string
  createdAt: string
  user?: {
    id: number
    username: string
    firstName: string
    lastName: string
  }
  voiceType?: {
    id: number
    voiceName: string
    code: string
  }
}

interface CleanupStats {
  totalHistoryRecords: number
  countByExpiryReason: {
    [key: string]: number
  }
  recentExpiredCount: number
  currentActiveMappings: number
}

export default function ExpiredVoicesPage() {
  const [history, setHistory] = useState<VoiceMappingHistory[]>([])
  const [filteredHistory, setFilteredHistory] = useState<VoiceMappingHistory[]>([])
  const [stats, setStats] = useState<CleanupStats | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [reasonFilter, setReasonFilter] = useState<string>('ALL')
  const [searchQuery, setSearchQuery] = useState('')

  useEffect(() => {
    fetchHistory()
    fetchStats()
  }, [])

  useEffect(() => {
    let filtered = history

    // Filter by reason
    if (reasonFilter !== 'ALL') {
      filtered = filtered.filter(h => h.expiryReason === reasonFilter)
    }

    // Filter by search query (username or voice name)
    if (searchQuery) {
      filtered = filtered.filter(h =>
        h.user?.username?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        h.user?.firstName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        h.user?.lastName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        h.voiceType?.voiceName?.toLowerCase().includes(searchQuery.toLowerCase())
      )
    }

    setFilteredHistory(filtered)
  }, [reasonFilter, searchQuery, history])

  const fetchHistory = async () => {
    try {
      setLoading(true)
      setError('')
      const response = await api.get<VoiceMappingHistory[]>('/voice-cleanup/history')
      setHistory(response)
      setFilteredHistory(response)
    } catch (err: any) {
      setError(err.message || 'Failed to fetch expired voice history')
    } finally {
      setLoading(false)
    }
  }

  const fetchStats = async () => {
    try {
      const response = await api.get<CleanupStats>('/voice-cleanup/statistics')
      setStats(response)
    } catch (err: any) {
      console.error('Failed to fetch stats:', err)
    }
  }

  const runCleanup = async () => {
    if (!confirm('Run cleanup now? This will move all expired voice mappings to history.')) {
      return
    }

    try {
      setError('')
      await api.post('/voice-cleanup/run')
      alert('Cleanup completed successfully!')
      await fetchHistory()
      await fetchStats()
    } catch (err: any) {
      setError(err.message || 'Failed to run cleanup')
    }
  }

  const getExpiryReasonBadge = (reason: string) => {
    const badges = {
      'TRIAL_EXPIRED': 'bg-yellow-100 text-yellow-800 border border-yellow-200',
      'SUBSCRIPTION_EXPIRED': 'bg-red-100 text-red-800 border border-red-200',
      'BOTH_EXPIRED': 'bg-purple-100 text-purple-800 border border-purple-200'
    }
    return badges[reason as keyof typeof badges] || 'bg-gray-100 text-gray-800'
  }

  const getExpiryReasonText = (reason: string) => {
    const texts = {
      'TRIAL_EXPIRED': 'Trial Expired',
      'SUBSCRIPTION_EXPIRED': 'Subscription Expired',
      'BOTH_EXPIRED': 'Both Expired'
    }
    return texts[reason as keyof typeof texts] || reason
  }

  return (
    <DashboardLayout>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent">
              Expired Voice History
            </h1>
            <p className="text-gray-600 mt-2">View all expired voice mappings (trials and subscriptions)</p>
          </div>
          <button
            onClick={runCleanup}
            className="flex items-center gap-2 px-4 py-2 bg-gradient-to-r from-purple-600 to-pink-600 text-white rounded-lg hover:shadow-lg transition-all"
          >
            <RefreshCw className="h-4 w-4" />
            Run Cleanup Now
          </button>
        </div>

        {/* Statistics Cards */}
        {stats && (
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div className="bg-gradient-to-br from-purple-50 to-purple-100 rounded-xl p-6 border border-purple-200 shadow-sm hover:shadow-md transition-all">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-purple-600 font-semibold">Total Expired</p>
                  <p className="text-3xl font-bold text-purple-700 mt-2">{stats.totalHistoryRecords}</p>
                </div>
                <div className="bg-purple-500 p-3 rounded-xl">
                  <Archive className="h-6 w-6 text-white" />
                </div>
              </div>
            </div>

            <div className="bg-gradient-to-br from-yellow-50 to-yellow-100 rounded-xl p-6 border border-yellow-200 shadow-sm hover:shadow-md transition-all">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-yellow-600 font-semibold">Trial Expired</p>
                  <p className="text-3xl font-bold text-yellow-700 mt-2">{stats.countByExpiryReason?.TRIAL_EXPIRED || 0}</p>
                </div>
                <div className="bg-yellow-500 p-3 rounded-xl">
                  <Clock className="h-6 w-6 text-white" />
                </div>
              </div>
            </div>

            <div className="bg-gradient-to-br from-red-50 to-red-100 rounded-xl p-6 border border-red-200 shadow-sm hover:shadow-md transition-all">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-red-600 font-semibold">Subscription Expired</p>
                  <p className="text-3xl font-bold text-red-700 mt-2">{stats.countByExpiryReason?.SUBSCRIPTION_EXPIRED || 0}</p>
                </div>
                <div className="bg-red-500 p-3 rounded-xl">
                  <TrendingDown className="h-6 w-6 text-white" />
                </div>
              </div>
            </div>

            <div className="bg-gradient-to-br from-blue-50 to-blue-100 rounded-xl p-6 border border-blue-200 shadow-sm hover:shadow-md transition-all">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-blue-600 font-semibold">Recent (7 days)</p>
                  <p className="text-3xl font-bold text-blue-700 mt-2">{stats.recentExpiredCount}</p>
                </div>
                <div className="bg-blue-500 p-3 rounded-xl">
                  <BarChart3 className="h-6 w-6 text-white" />
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Filters */}
        <div className="bg-white rounded-xl shadow-md p-6 border border-gray-100">
          <div className="flex items-center gap-4 flex-wrap">
            <div className="flex items-center gap-2">
              <Filter className="h-5 w-5 text-gray-500" />
              <span className="text-sm font-semibold text-gray-700">Filter by Reason:</span>
            </div>
            {['ALL', 'TRIAL_EXPIRED', 'SUBSCRIPTION_EXPIRED', 'BOTH_EXPIRED'].map((reason) => (
              <button
                key={reason}
                onClick={() => setReasonFilter(reason)}
                className={`px-4 py-2 rounded-xl text-sm font-semibold transition-all ${
                  reasonFilter === reason
                    ? 'bg-gradient-to-r from-purple-600 to-pink-600 text-white shadow-lg'
                    : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                }`}
              >
                {reason === 'ALL' ? 'All' : getExpiryReasonText(reason)}
              </button>
            ))}
          </div>

          <div className="mt-4">
            <input
              type="text"
              placeholder="Search by username or voice name..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full px-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-transparent"
            />
          </div>
        </div>

        {/* Error Message */}
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-xl flex items-center gap-2">
            <AlertCircle className="h-5 w-5" />
            {error}
          </div>
        )}

        {/* History Table */}
        <div className="bg-white rounded-xl shadow-lg border border-gray-100 overflow-hidden">
          {loading ? (
            <div className="flex items-center justify-center h-64">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600"></div>
            </div>
          ) : filteredHistory.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-64 text-gray-500">
              <Archive className="h-16 w-16 mb-4 text-gray-300" />
              <p className="text-lg font-semibold">No expired voice mappings found</p>
              <p className="text-sm">All voice mappings are currently active</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gradient-to-r from-purple-50 to-pink-50">
                  <tr>
                    <th className="px-6 py-4 text-left text-xs font-bold text-purple-700 uppercase tracking-wider">
                      User
                    </th>
                    <th className="px-6 py-4 text-left text-xs font-bold text-purple-700 uppercase tracking-wider">
                      Voice Type
                    </th>
                    <th className="px-6 py-4 text-left text-xs font-bold text-purple-700 uppercase tracking-wider">
                      Type
                    </th>
                    <th className="px-6 py-4 text-left text-xs font-bold text-purple-700 uppercase tracking-wider">
                      Expiry Reason
                    </th>
                    <th className="px-6 py-4 text-left text-xs font-bold text-purple-700 uppercase tracking-wider">
                      Assigned
                    </th>
                    <th className="px-6 py-4 text-left text-xs font-bold text-purple-700 uppercase tracking-wider">
                      Expired
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {filteredHistory.map((item) => (
                    <tr key={item.id} className="hover:bg-purple-50 transition-colors">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div>
                          <div className="text-sm font-semibold text-gray-900">
                            {item.user?.firstName} {item.user?.lastName}
                          </div>
                          <div className="text-sm text-gray-500">{item.user?.username}</div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center gap-2">
                          <span className="px-3 py-1 rounded-lg bg-gradient-to-r from-purple-500 to-pink-500 text-white text-sm font-semibold">
                            {item.voiceType?.code}
                          </span>
                          <span className="text-sm text-gray-700">{item.voiceType?.voiceName}</span>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`px-3 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${
                          item.isPurchased
                            ? 'bg-green-100 text-green-800'
                            : 'bg-blue-100 text-blue-800'
                        }`}>
                          {item.isPurchased ? 'Purchased' : 'Trial'}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`px-3 py-1 inline-flex text-xs leading-5 font-semibold rounded-lg ${getExpiryReasonBadge(item.expiryReason)}`}>
                          {getExpiryReasonText(item.expiryReason)}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-700">
                        {format(new Date(item.assignedAt), 'MMM dd, yyyy')}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-700">
                        {format(new Date(item.expiredAt), 'MMM dd, yyyy HH:mm')}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Summary */}
        {filteredHistory.length > 0 && (
          <div className="bg-gradient-to-r from-purple-50 to-pink-50 rounded-xl p-6 border border-purple-100">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Total Records</p>
                <p className="text-2xl font-bold text-purple-700">{filteredHistory.length}</p>
              </div>
              <Archive className="h-12 w-12 text-purple-400" />
            </div>
          </div>
        )}
      </div>
    </DashboardLayout>
  )
}
