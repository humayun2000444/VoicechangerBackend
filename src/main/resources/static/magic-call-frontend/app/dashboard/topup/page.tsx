'use client'

import { useState, useEffect } from 'react'
import { DashboardLayout } from '@/components/DashboardLayout'
import { api } from '@/lib/api'
import { TopUpResponse } from '@/types'
import { format } from 'date-fns'

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
        <div className="flex justify-center items-center h-64">
          <div className="text-lg text-gray-600">Loading top-up requests...</div>
        </div>
      </DashboardLayout>
    )
  }

  return (
    <DashboardLayout>
      <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Top-Up Management</h1>
          <p className="mt-2 text-sm text-gray-600">
            Manage user top-up requests and approve/reject transactions
          </p>
        </div>
        <button
          onClick={fetchTopUps}
          className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          Refresh
        </button>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-lg">
          {error}
        </div>
      )}

      {/* Statistics */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-white rounded-lg shadow p-6 border-l-4 border-blue-500">
          <div className="text-sm font-medium text-gray-600">Total Requests</div>
          <div className="mt-2 text-3xl font-bold text-gray-900">{topups.length}</div>
        </div>
        <div className="bg-white rounded-lg shadow p-6 border-l-4 border-yellow-500">
          <div className="text-sm font-medium text-gray-600">Pending</div>
          <div className="mt-2 text-3xl font-bold text-yellow-600">{pendingCount}</div>
        </div>
        <div className="bg-white rounded-lg shadow p-6 border-l-4 border-green-500">
          <div className="text-sm font-medium text-gray-600">Approved</div>
          <div className="mt-2 text-3xl font-bold text-green-600">{successCount}</div>
        </div>
        <div className="bg-white rounded-lg shadow p-6 border-l-4 border-red-500">
          <div className="text-sm font-medium text-gray-600">Rejected</div>
          <div className="mt-2 text-3xl font-bold text-red-600">{failedCount}</div>
        </div>
      </div>

      {/* Filter Tabs */}
      <div className="bg-white rounded-lg shadow">
        <div className="border-b border-gray-200">
          <nav className="flex -mb-px">
            {['ALL', 'PENDING', 'SUCCESS', 'FAILED'].map((status) => (
              <button
                key={status}
                onClick={() => setStatusFilter(status)}
                className={`px-6 py-3 text-sm font-medium border-b-2 transition-colors ${
                  statusFilter === status
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                {status}
                {status !== 'ALL' && (
                  <span className="ml-2 px-2 py-0.5 text-xs rounded-full bg-gray-200">
                    {status === 'PENDING' && pendingCount}
                    {status === 'SUCCESS' && successCount}
                    {status === 'FAILED' && failedCount}
                  </span>
                )}
              </button>
            ))}
          </nav>
        </div>

        {/* Top-Up Requests Table */}
        <div className="overflow-x-auto">
          {filteredTopups.length === 0 ? (
            <div className="text-center py-12 text-gray-500">
              No top-up requests found
            </div>
          ) : (
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    ID
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    User
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Amount (BDT)
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Duration
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Payment Method
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Transaction ID
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Date
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Status
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredTopups.map((topup) => (
                  <tr key={topup.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      #{topup.id}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">
                        {topup.user?.username || `User #${topup.idUser}`}
                      </div>
                      {topup.user && (
                        <div className="text-sm text-gray-500">
                          {topup.user.firstName} {topup.user.lastName}
                        </div>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-bold text-gray-900">
                        ৳{topup.amount.toFixed(2)}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-blue-600">
                        {formatDuration(topup.durationInSeconds)}
                      </div>
                      <div className="text-xs text-gray-500">
                        ({topup.durationInSeconds} seconds)
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {topup.transactionMethod}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-mono text-gray-700">
                        {topup.tnxId}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        {format(new Date(topup.date), 'MMM dd, yyyy')}
                      </div>
                      <div className="text-xs text-gray-500">
                        {format(new Date(topup.date), 'hh:mm a')}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span
                        className={`px-3 py-1 inline-flex text-xs leading-5 font-semibold rounded-full border ${getStatusColor(
                          topup.status
                        )}`}
                      >
                        {topup.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                      {topup.status === 'PENDING' && (
                        <>
                          <button
                            onClick={() => handleApprove(topup.id)}
                            disabled={processingId === topup.id}
                            className="text-green-600 hover:text-green-900 disabled:opacity-50 disabled:cursor-not-allowed"
                          >
                            {processingId === topup.id ? 'Processing...' : 'Approve'}
                          </button>
                          <span className="text-gray-300">|</span>
                          <button
                            onClick={() => handleReject(topup.id)}
                            disabled={processingId === topup.id}
                            className="text-red-600 hover:text-red-900 disabled:opacity-50 disabled:cursor-not-allowed"
                          >
                            Reject
                          </button>
                          <span className="text-gray-300">|</span>
                        </>
                      )}
                      <button
                        onClick={() => handleDelete(topup.id)}
                        disabled={processingId === topup.id}
                        className="text-red-600 hover:text-red-900 disabled:opacity-50 disabled:cursor-not-allowed"
                      >
                        Delete
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
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
        <div className="flex">
          <div className="flex-shrink-0">
            <svg className="h-5 w-5 text-blue-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
            </svg>
          </div>
          <div className="ml-3 flex-1">
            <h3 className="text-sm font-medium text-blue-800">Top-Up Information</h3>
            <div className="mt-2 text-sm text-blue-700">
              <ul className="list-disc list-inside space-y-1">
                <li><strong>Rate:</strong> 60 seconds per 3 BDT</li>
                <li><strong>Minimum:</strong> 20 BDT</li>
                <li><strong>Workflow:</strong> User submits request → Admin reviews → Approve/Reject → Balance updated automatically</li>
                <li><strong>Note:</strong> Approving a request will automatically add the calculated duration to the user's balance</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
      </div>
    </DashboardLayout>
  )
}
