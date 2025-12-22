'use client'

import { useState, useEffect } from 'react'
import { DashboardLayout } from '@/components/DashboardLayout'
import { api } from '@/lib/api'
import { TopUpResponse } from '@/types'
import { format } from 'date-fns'
import { Wallet, TrendingUp, CheckCircle, XCircle, Clock, RefreshCw, Info, Trash2 } from 'lucide-react'

export default function TopUpPage() {
  const [topups, setTopups] = useState<TopUpResponse[]>([])
  const [filteredTopups, setFilteredTopups] = useState<TopUpResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [statusFilter, setStatusFilter] = useState<string>('ALL')
  const [processingId, setProcessingId] = useState<number | null>(null)

  useEffect(() => {
    fetchTopUps()
  }, [])

  useEffect(() => {
    if (statusFilter === 'ALL') {
      setFilteredTopups(topups)
    } else {
      setFilteredTopups(topups.filter(t => t.status === statusFilter))
    }
  }, [statusFilter, topups])

  const fetchTopUps = async () => {
    try {
      setLoading(true)
      setError('')
      const response = await api.get<TopUpResponse[]>('/topup')
      setTopups(response)
      setFilteredTopups(response)
    } catch (err: any) {
      setError(err.message || 'Failed to fetch top-up requests')
    } finally {
      setLoading(false)
    }
  }

  const handleApprove = async (id: number) => {
    if (!confirm('Are you sure you want to approve this top-up request? This will add balance to the user account.')) {
      return
    }

    try {
      setProcessingId(id)
      setError('')
      await api.patch(`/topup/${id}/approve`)
      await fetchTopUps()
      alert('Top-up request approved successfully! Balance has been added to user account.')
    } catch (err: any) {
      setError(err.message || 'Failed to approve top-up request')
    } finally {
      setProcessingId(null)
    }
  }

  const handleReject = async (id: number) => {
    if (!confirm('Are you sure you want to reject this top-up request?')) {
      return
    }

    try {
      setProcessingId(id)
      setError('')
      await api.patch(`/topup/${id}/reject`)
      await fetchTopUps()
      alert('Top-up request rejected')
    } catch (err: any) {
      setError(err.message || 'Failed to reject top-up request')
    } finally {
      setProcessingId(null)
    }
  }

  const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this top-up request? This action cannot be undone.')) {
      return
    }

    try {
      setProcessingId(id)
      setError('')
      await api.delete(`/topup/${id}`)
      await fetchTopUps()
    } catch (err: any) {
      setError(err.message || 'Failed to delete top-up request')
    } finally {
      setProcessingId(null)
    }
  }

  const formatDuration = (seconds: number) => {
    const hours = Math.floor(seconds / 3600)
    const minutes = Math.floor((seconds % 3600) / 60)
    const secs = seconds % 60

    const parts = []
    if (hours > 0) parts.push(`${hours}h`)
    if (minutes > 0) parts.push(`${minutes}m`)
    if (secs > 0 || parts.length === 0) parts.push(`${secs}s`)

    return parts.join(' ')
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800 border-yellow-300'
      case 'SUCCESS':
        return 'bg-green-100 text-green-800 border-green-300'
      case 'FAILED':
        return 'bg-red-100 text-red-800 border-red-300'
      default:
        return 'bg-gray-100 text-gray-800 border-gray-300'
    }
  }

  const pendingCount = topups.filter(t => t.status === 'PENDING').length
  const successCount = topups.filter(t => t.status === 'SUCCESS').length
  const failedCount = topups.filter(t => t.status === 'FAILED').length

  if (loading) {
    return (
      <DashboardLayout>
        <div className="flex justify-center items-center h-96">
          <div className="text-center">
            <div className="inline-block h-12 w-12 animate-spin rounded-full border-4 border-solid border-purple-600 border-r-transparent mb-4"></div>
            <p className="text-lg text-gray-600 font-medium">Loading top-up requests...</p>
          </div>
        </div>
      </DashboardLayout>
    )
  }

  return (
    <DashboardLayout>
      <div className="space-y-8">
        {/* Header */}
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-4xl font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent">
              Top-Up Management
            </h1>
            <p className="mt-2 text-gray-600 flex items-center gap-2">
              <Wallet className="h-4 w-4" />
              Manage user top-up requests and approve/reject transactions
            </p>
          </div>
          <button
            onClick={fetchTopUps}
            className="px-6 py-3 bg-gradient-to-r from-purple-600 to-pink-600 hover:from-purple-700 hover:to-pink-700 text-white rounded-xl shadow-lg hover:shadow-xl transition-all flex items-center gap-2 font-semibold"
          >
            <RefreshCw className="h-5 w-5" />
            Refresh
          </button>
        </div>

        {/* Error Message */}
        {error && (
          <div className="bg-gradient-to-r from-red-50 to-pink-50 border-l-4 border-red-500 text-red-800 px-6 py-4 rounded-xl shadow-md">
            <p className="font-medium">{error}</p>
          </div>
        )}

        {/* Statistics Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          <div className="bg-gradient-to-br from-purple-50 to-purple-100 rounded-2xl shadow-lg p-6 border-0 hover:shadow-xl transition-all transform hover:-translate-y-1">
            <div className="flex items-center justify-between mb-3">
              <div className="bg-purple-500 p-3 rounded-xl shadow-lg">
                <TrendingUp className="h-6 w-6 text-white" />
              </div>
            </div>
            <p className="text-sm font-medium text-gray-600 mb-1">Total Requests</p>
            <p className="text-3xl font-bold text-purple-600">{topups.length}</p>
          </div>

          <div className="bg-gradient-to-br from-yellow-50 to-yellow-100 rounded-2xl shadow-lg p-6 border-0 hover:shadow-xl transition-all transform hover:-translate-y-1">
            <div className="flex items-center justify-between mb-3">
              <div className="bg-yellow-500 p-3 rounded-xl shadow-lg">
                <Clock className="h-6 w-6 text-white" />
              </div>
            </div>
            <p className="text-sm font-medium text-gray-600 mb-1">Pending</p>
            <p className="text-3xl font-bold text-yellow-600">{pendingCount}</p>
          </div>

          <div className="bg-gradient-to-br from-green-50 to-green-100 rounded-2xl shadow-lg p-6 border-0 hover:shadow-xl transition-all transform hover:-translate-y-1">
            <div className="flex items-center justify-between mb-3">
              <div className="bg-green-500 p-3 rounded-xl shadow-lg">
                <CheckCircle className="h-6 w-6 text-white" />
              </div>
            </div>
            <p className="text-sm font-medium text-gray-600 mb-1">Approved</p>
            <p className="text-3xl font-bold text-green-600">{successCount}</p>
          </div>

          <div className="bg-gradient-to-br from-red-50 to-red-100 rounded-2xl shadow-lg p-6 border-0 hover:shadow-xl transition-all transform hover:-translate-y-1">
            <div className="flex items-center justify-between mb-3">
              <div className="bg-red-500 p-3 rounded-xl shadow-lg">
                <XCircle className="h-6 w-6 text-white" />
              </div>
            </div>
            <p className="text-sm font-medium text-gray-600 mb-1">Rejected</p>
            <p className="text-3xl font-bold text-red-600">{failedCount}</p>
          </div>
        </div>

        {/* Filter Tabs & Table */}
        <div className="bg-white rounded-2xl shadow-xl border-0 overflow-hidden">
          <div className="bg-gradient-to-r from-purple-50 to-pink-50 border-b border-purple-100 p-1">
            <nav className="flex gap-2 p-2">
              {['ALL', 'PENDING', 'SUCCESS', 'FAILED'].map((status) => (
                <button
                  key={status}
                  onClick={() => setStatusFilter(status)}
                  className={`px-6 py-3 rounded-xl text-sm font-semibold transition-all ${
                    statusFilter === status
                      ? 'bg-gradient-to-r from-purple-600 to-pink-600 text-white shadow-lg'
                      : 'bg-white text-gray-600 hover:bg-gray-50 hover:shadow-md'
                  }`}
                >
                  {status}
                  {status !== 'ALL' && (
                    <span className={`ml-2 px-2 py-0.5 text-xs rounded-full ${
                      statusFilter === status ? 'bg-white/20' : 'bg-purple-100 text-purple-700'
                    }`}>
                      {status === 'PENDING' && pendingCount}
                      {status === 'SUCCESS' && successCount}
                      {status === 'FAILED' && failedCount}
                    </span>
                  )}
                </button>
              ))}
            </nav>
          </div>

          {/* Table */}
          <div className="overflow-x-auto">
            {filteredTopups.length === 0 ? (
              <div className="text-center py-16">
                <Wallet className="h-16 w-16 text-gray-300 mx-auto mb-4" />
                <p className="text-gray-500 text-lg font-medium">No top-up requests found</p>
              </div>
            ) : (
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">ID</th>
                    <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">User</th>
                    <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">Amount</th>
                    <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">Duration</th>
                    <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">Payment</th>
                    <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">Transaction ID</th>
                    <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">Date</th>
                    <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">Status</th>
                    <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">Actions</th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-100">
                  {filteredTopups.map((topup) => (
                    <tr key={topup.id} className="hover:bg-purple-50/30 transition-colors">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className="text-sm font-bold text-purple-600">#{topup.id}</span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm font-semibold text-gray-900">
                          {topup.user?.username || `User #${topup.idUser}`}
                        </div>
                        {topup.user && (
                          <div className="text-xs text-gray-500">
                            {topup.user.firstName} {topup.user.lastName}
                          </div>
                        )}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-base font-bold text-gray-900">৳{topup.amount.toFixed(2)}</div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm font-semibold text-purple-600">{formatDuration(topup.durationInSeconds)}</div>
                        <div className="text-xs text-gray-500">({topup.durationInSeconds}s)</div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className="text-sm font-medium text-gray-700 uppercase">{topup.transactionMethod}</span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className="text-sm font-mono text-gray-600">{topup.tnxId}</span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm text-gray-900">{format(new Date(topup.date), 'MMM dd, yyyy')}</div>
                        <div className="text-xs text-gray-500">{format(new Date(topup.date), 'hh:mm a')}</div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`px-3 py-1.5 inline-flex text-xs leading-5 font-bold rounded-full border ${getStatusColor(topup.status)}`}>
                          {topup.status}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                        {topup.status === 'PENDING' && (
                          <>
                            <button
                              onClick={() => handleApprove(topup.id)}
                              disabled={processingId === topup.id}
                              className="text-green-600 hover:text-green-800 font-semibold disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                            >
                              {processingId === topup.id ? 'Processing...' : 'Approve'}
                            </button>
                            <span className="text-gray-300">|</span>
                            <button
                              onClick={() => handleReject(topup.id)}
                              disabled={processingId === topup.id}
                              className="text-red-600 hover:text-red-800 font-semibold disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                            >
                              Reject
                            </button>
                            <span className="text-gray-300">|</span>
                          </>
                        )}
                        <button
                          onClick={() => handleDelete(topup.id)}
                          disabled={processingId === topup.id}
                          className="text-red-600 hover:text-red-800 font-semibold disabled:opacity-50 disabled:cursor-not-allowed transition-colors inline-flex items-center gap-1"
                        >
                          <Trash2 className="h-4 w-4" />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>

        {/* Info Panel */}
        <div className="bg-gradient-to-r from-purple-50 to-pink-50 border-l-4 border-purple-500 rounded-xl p-6 shadow-md">
          <div className="flex">
            <div className="flex-shrink-0">
              <Info className="h-6 w-6 text-purple-600" />
            </div>
            <div className="ml-4 flex-1">
              <h3 className="text-lg font-bold text-purple-900 mb-3">Top-Up Information</h3>
              <div className="text-sm text-purple-800 space-y-2">
                <div className="flex items-start gap-2">
                  <span className="font-bold min-w-[120px]">Rate:</span>
                  <span>60 seconds per 3 BDT</span>
                </div>
                <div className="flex items-start gap-2">
                  <span className="font-bold min-w-[120px]">Minimum:</span>
                  <span>20 BDT</span>
                </div>
                <div className="flex items-start gap-2">
                  <span className="font-bold min-w-[120px]">Workflow:</span>
                  <span>User submits request → Admin reviews → Approve/Reject → Balance updated automatically</span>
                </div>
                <div className="flex items-start gap-2">
                  <span className="font-bold min-w-[120px]">Note:</span>
                  <span>Approving a request will automatically add the calculated duration to the user's balance</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </DashboardLayout>
  )
}
