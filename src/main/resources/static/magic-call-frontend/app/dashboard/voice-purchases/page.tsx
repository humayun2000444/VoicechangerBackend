'use client'

import { useState, useEffect } from 'react'
import { DashboardLayout } from '@/components/DashboardLayout'
import { api } from '@/lib/api'
import { VoicePurchaseResponse } from '@/types'
import { format } from 'date-fns'
import { ShoppingCart, CheckCircle, XCircle, Clock, RefreshCw, Info, Eye, TrendingUp } from 'lucide-react'

export default function VoicePurchasesPage() {
  const [purchases, setPurchases] = useState<VoicePurchaseResponse[]>([])
  const [filteredPurchases, setFilteredPurchases] = useState<VoicePurchaseResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [statusFilter, setStatusFilter] = useState<string>('ALL')
  const [processingId, setProcessingId] = useState<number | null>(null)
  const [selectedPurchase, setSelectedPurchase] = useState<VoicePurchaseResponse | null>(null)
  const [showModal, setShowModal] = useState(false)

  useEffect(() => {
    fetchPurchases()
  }, [])

  useEffect(() => {
    if (statusFilter === 'ALL') {
      setFilteredPurchases(purchases)
    } else {
      setFilteredPurchases(purchases.filter(p => p.status.toLowerCase() === statusFilter.toLowerCase()))
    }
  }, [statusFilter, purchases])

  const fetchPurchases = async () => {
    try {
      setLoading(true)
      setError('')
      const response = await api.get<VoicePurchaseResponse[]>('/voice-purchase')
      setPurchases(response)
      setFilteredPurchases(response)
    } catch (err: any) {
      setError(err.message || 'Failed to fetch voice purchases')
    } finally {
      setLoading(false)
    }
  }

  const handleApprove = async (id: number) => {
    if (!confirm('Are you sure you want to approve this voice purchase? The user will get access to this voice type.')) {
      return
    }

    try {
      setProcessingId(id)
      setError('')
      await api.put(`/voice-purchase/${id}/approve`)
      await fetchPurchases()
      alert('Voice purchase approved successfully! User has been granted access.')
      setShowModal(false)
    } catch (err: any) {
      setError(err.message || 'Failed to approve purchase')
    } finally {
      setProcessingId(null)
    }
  }

  const handleReject = async (id: number) => {
    if (!confirm('Are you sure you want to reject this voice purchase?')) {
      return
    }

    try {
      setProcessingId(id)
      setError('')
      await api.put(`/voice-purchase/${id}/reject`)
      await fetchPurchases()
      alert('Voice purchase rejected')
      setShowModal(false)
    } catch (err: any) {
      setError(err.message || 'Failed to reject purchase')
    } finally {
      setProcessingId(null)
    }
  }

  const openDetailsModal = (purchase: VoicePurchaseResponse) => {
    setSelectedPurchase(purchase)
    setShowModal(true)
  }

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'pending':
        return 'bg-yellow-100 text-yellow-800 border-yellow-300'
      case 'approved':
        return 'bg-green-100 text-green-800 border-green-300'
      case 'rejected':
        return 'bg-red-100 text-red-800 border-red-300'
      default:
        return 'bg-gray-100 text-gray-800 border-gray-300'
    }
  }

  const pendingCount = purchases.filter(p => p.status.toLowerCase() === 'pending').length
  const approvedCount = purchases.filter(p => p.status.toLowerCase() === 'approved').length
  const rejectedCount = purchases.filter(p => p.status.toLowerCase() === 'rejected').length

  if (loading) {
    return (
      <DashboardLayout>
        <div className="flex justify-center items-center h-96">
          <div className="text-center">
            <div className="inline-block h-12 w-12 animate-spin rounded-full border-4 border-solid border-pink-600 border-r-transparent mb-4"></div>
            <p className="text-lg text-gray-600 font-medium">Loading voice purchases...</p>
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
            <h1 className="text-4xl font-bold bg-gradient-to-r from-pink-600 to-purple-600 bg-clip-text text-transparent">
              Voice Purchase Management
            </h1>
            <p className="mt-2 text-gray-600 flex items-center gap-2">
              <ShoppingCart className="h-4 w-4" />
              Manage voice type purchase requests and approve/reject transactions
            </p>
          </div>
          <button
            onClick={fetchPurchases}
            className="px-6 py-3 bg-gradient-to-r from-pink-600 to-purple-600 hover:from-pink-700 hover:to-purple-700 text-white rounded-xl shadow-lg hover:shadow-xl transition-all flex items-center gap-2 font-semibold"
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
          <div className="bg-gradient-to-br from-pink-50 to-pink-100 rounded-2xl shadow-lg p-6 border-0 hover:shadow-xl transition-all transform hover:-translate-y-1">
            <div className="flex items-center justify-between mb-3">
              <div className="bg-pink-500 p-3 rounded-xl shadow-lg">
                <TrendingUp className="h-6 w-6 text-white" />
              </div>
            </div>
            <p className="text-sm font-medium text-gray-600 mb-1">Total Requests</p>
            <p className="text-3xl font-bold text-pink-600">{purchases.length}</p>
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
            <p className="text-3xl font-bold text-green-600">{approvedCount}</p>
          </div>

          <div className="bg-gradient-to-br from-red-50 to-red-100 rounded-2xl shadow-lg p-6 border-0 hover:shadow-xl transition-all transform hover:-translate-y-1">
            <div className="flex items-center justify-between mb-3">
              <div className="bg-red-500 p-3 rounded-xl shadow-lg">
                <XCircle className="h-6 w-6 text-white" />
              </div>
            </div>
            <p className="text-sm font-medium text-gray-600 mb-1">Rejected</p>
            <p className="text-3xl font-bold text-red-600">{rejectedCount}</p>
          </div>
        </div>

        {/* Filter Tabs & Table */}
        <div className="bg-white rounded-2xl shadow-xl border-0 overflow-hidden">
          <div className="bg-gradient-to-r from-pink-50 to-purple-50 border-b border-pink-100 p-1">
            <nav className="flex gap-2 p-2">
              {['ALL', 'PENDING', 'APPROVED', 'REJECTED'].map((status) => (
                <button
                  key={status}
                  onClick={() => setStatusFilter(status)}
                  className={`px-6 py-3 rounded-xl text-sm font-semibold transition-all ${
                    statusFilter === status
                      ? 'bg-gradient-to-r from-pink-600 to-purple-600 text-white shadow-lg'
                      : 'bg-white text-gray-600 hover:bg-gray-50 hover:shadow-md'
                  }`}
                >
                  {status}
                  {status !== 'ALL' && (
                    <span className={`ml-2 px-2 py-0.5 text-xs rounded-full ${
                      statusFilter === status ? 'bg-white/20' : 'bg-pink-100 text-pink-700'
                    }`}>
                      {status === 'PENDING' && pendingCount}
                      {status === 'APPROVED' && approvedCount}
                      {status === 'REJECTED' && rejectedCount}
                    </span>
                  )}
                </button>
              ))}
            </nav>
          </div>

          {/* Table */}
          <div className="overflow-x-auto">
            {filteredPurchases.length === 0 ? (
              <div className="text-center py-16">
                <ShoppingCart className="h-16 w-16 text-gray-300 mx-auto mb-4" />
                <p className="text-gray-500 text-lg font-medium">No voice purchase requests found</p>
              </div>
            ) : (
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">ID</th>
                    <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">User</th>
                    <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">Voice Type</th>
                    <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">Subscription</th>
                    <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">Payment</th>
                    <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">Amount</th>
                    <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">Expiry Date</th>
                    <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">Status</th>
                    <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">Actions</th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-100">
                  {filteredPurchases.map((purchase) => (
                    <tr key={purchase.id} className="hover:bg-pink-50/30 transition-colors">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className="text-sm font-bold text-pink-600">#{purchase.id}</span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm font-semibold text-gray-900">
                          {purchase.user?.username || `User #${purchase.idUser}`}
                        </div>
                        {purchase.user && (
                          <div className="text-xs text-gray-500">
                            {purchase.user.firstName} {purchase.user.lastName}
                          </div>
                        )}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm font-semibold text-gray-900">
                          {purchase.voiceType?.voiceName || 'Unknown'}
                        </div>
                        {purchase.voiceType && (
                          <div className="text-xs text-gray-500">
                            Code: {purchase.voiceType.code}
                          </div>
                        )}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className="px-3 py-1.5 text-xs font-bold rounded-full bg-purple-100 text-purple-700 border border-purple-200 capitalize">
                          {purchase.subscriptionType || 'N/A'}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className="text-sm font-medium text-gray-700 uppercase">
                          {purchase.transactionMethod || 'N/A'}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-base font-bold text-gray-900">৳{purchase.amount.toFixed(2)}</div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {purchase.expiryDate ? (
                          <>
                            <div className="text-sm text-gray-900">
                              {format(new Date(purchase.expiryDate), 'MMM dd, yyyy')}
                            </div>
                            <div className="text-xs text-gray-500">
                              {format(new Date(purchase.expiryDate), 'hh:mm a')}
                            </div>
                          </>
                        ) : (
                          <span className="text-gray-400">N/A</span>
                        )}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`px-3 py-1.5 inline-flex text-xs leading-5 font-bold rounded-full border capitalize ${getStatusColor(purchase.status)}`}>
                          {purchase.status}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                        <button
                          onClick={() => openDetailsModal(purchase)}
                          className="text-purple-600 hover:text-purple-800 font-semibold inline-flex items-center gap-1"
                        >
                          <Eye className="h-4 w-4" />
                          View
                        </button>
                        {purchase.status.toLowerCase() === 'pending' && (
                          <>
                            <span className="text-gray-300">|</span>
                            <button
                              onClick={() => handleApprove(purchase.id)}
                              disabled={processingId === purchase.id}
                              className="text-green-600 hover:text-green-800 font-semibold disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                              {processingId === purchase.id ? 'Processing...' : 'Approve'}
                            </button>
                            <span className="text-gray-300">|</span>
                            <button
                              onClick={() => handleReject(purchase.id)}
                              disabled={processingId === purchase.id}
                              className="text-red-600 hover:text-red-800 font-semibold disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                              Reject
                            </button>
                          </>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>

        {/* Info Panel */}
        <div className="bg-gradient-to-r from-pink-50 to-purple-50 border-l-4 border-pink-500 rounded-xl p-6 shadow-md">
          <div className="flex">
            <div className="flex-shrink-0">
              <Info className="h-6 w-6 text-pink-600" />
            </div>
            <div className="ml-4 flex-1">
              <h3 className="text-lg font-bold text-pink-900 mb-3">Voice Purchase Information</h3>
              <div className="text-sm text-pink-800 space-y-2">
                <div className="flex items-start gap-2">
                  <span className="font-bold min-w-[120px]">Monthly:</span>
                  <span>200 BDT per voice type (1 month access)</span>
                </div>
                <div className="flex items-start gap-2">
                  <span className="font-bold min-w-[120px]">Yearly:</span>
                  <span>1,000 BDT per voice type (1 year access, 17% off)</span>
                </div>
                <div className="flex items-start gap-2">
                  <span className="font-bold min-w-[120px]">Free Trial:</span>
                  <span>30 sec free calls, 1 premium voice for 3 days</span>
                </div>
                <div className="flex items-start gap-2">
                  <span className="font-bold min-w-[120px]">Workflow:</span>
                  <span>User selects subscription → Submits request with payment → Admin reviews → Approve/Reject → Access granted until expiry</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Modern Details Modal */}
      {showModal && selectedPurchase && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm overflow-y-auto h-full w-full z-50 flex items-center justify-center p-4" onClick={() => setShowModal(false)}>
          <div className="relative bg-white rounded-2xl shadow-2xl w-full max-w-3xl" onClick={(e) => e.stopPropagation()}>
            {/* Modal Header */}
            <div className="bg-gradient-to-r from-pink-500 to-purple-600 px-8 py-6 rounded-t-2xl">
              <div className="flex justify-between items-center">
                <h3 className="text-2xl font-bold text-white">Voice Purchase Details</h3>
                <button onClick={() => setShowModal(false)} className="text-white hover:bg-white/20 rounded-lg p-2 transition">
                  <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
            </div>

            {/* Modal Content */}
            <div className="p-8 space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Purchase Information */}
                <div className="bg-purple-50 rounded-xl p-5 border border-purple-100">
                  <h4 className="font-bold text-purple-900 mb-4 text-lg flex items-center gap-2">
                    <ShoppingCart className="h-5 w-5" />
                    Purchase Information
                  </h4>
                  <div className="space-y-3 text-sm">
                    <div className="flex justify-between">
                      <span className="text-gray-600">Purchase ID:</span>
                      <span className="font-bold text-purple-700">#{selectedPurchase.id}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600">Voice Type:</span>
                      <span className="font-semibold">{selectedPurchase.voiceType?.voiceName || 'Unknown'}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600">Code:</span>
                      <span className="font-mono font-semibold">{selectedPurchase.voiceType?.code || 'N/A'}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600">Subscription:</span>
                      <span className="font-semibold capitalize">{selectedPurchase.subscriptionType || 'N/A'}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600">Amount:</span>
                      <span className="font-bold text-lg">৳{selectedPurchase.amount.toFixed(2)}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600">Status:</span>
                      <span className={`px-3 py-1 text-xs rounded-full font-bold capitalize ${getStatusColor(selectedPurchase.status)}`}>
                        {selectedPurchase.status}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600">Purchase Date:</span>
                      <span className="font-semibold text-xs">{format(new Date(selectedPurchase.purchaseDate), 'MMM dd, yyyy hh:mm a')}</span>
                    </div>
                    {selectedPurchase.expiryDate && (
                      <div className="flex justify-between">
                        <span className="text-gray-600">Expiry Date:</span>
                        <span className="font-semibold text-xs">{format(new Date(selectedPurchase.expiryDate), 'MMM dd, yyyy hh:mm a')}</span>
                      </div>
                    )}
                  </div>
                </div>

                {/* User & Payment Information */}
                <div className="bg-pink-50 rounded-xl p-5 border border-pink-100">
                  <h4 className="font-bold text-pink-900 mb-4 text-lg">User & Payment</h4>
                  <div className="space-y-3 text-sm">
                    <div className="flex justify-between">
                      <span className="text-gray-600">User ID:</span>
                      <span className="font-semibold">{selectedPurchase.user?.id || selectedPurchase.idUser}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600">Username:</span>
                      <span className="font-semibold">{selectedPurchase.user?.username || 'N/A'}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600">Name:</span>
                      <span className="font-semibold">{selectedPurchase.user ? `${selectedPurchase.user.firstName} ${selectedPurchase.user.lastName}` : 'N/A'}</span>
                    </div>
                    <div className="h-px bg-pink-200 my-3"></div>
                    <div className="flex justify-between">
                      <span className="text-gray-600">Payment Method:</span>
                      <span className="font-semibold uppercase">{selectedPurchase.transactionMethod || 'N/A'}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600">Transaction ID:</span>
                      <span className="font-mono text-xs font-semibold">{selectedPurchase.tnxId || 'N/A'}</span>
                    </div>
                  </div>
                </div>
              </div>

              {/* Action Buttons */}
              {selectedPurchase.status.toLowerCase() === 'pending' && (
                <div className="flex justify-end space-x-3 pt-4 border-t border-gray-200">
                  <button
                    onClick={() => setShowModal(false)}
                    className="px-6 py-3 bg-gray-100 text-gray-700 rounded-xl hover:bg-gray-200 transition-colors font-semibold"
                  >
                    Close
                  </button>
                  <button
                    onClick={() => handleReject(selectedPurchase.id)}
                    disabled={processingId === selectedPurchase.id}
                    className="px-6 py-3 bg-red-600 text-white rounded-xl hover:bg-red-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed font-semibold"
                  >
                    Reject
                  </button>
                  <button
                    onClick={() => handleApprove(selectedPurchase.id)}
                    disabled={processingId === selectedPurchase.id}
                    className="px-6 py-3 bg-gradient-to-r from-green-600 to-emerald-600 text-white rounded-xl hover:from-green-700 hover:to-emerald-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed font-semibold shadow-lg"
                  >
                    {processingId === selectedPurchase.id ? 'Processing...' : 'Approve'}
                  </button>
                </div>
              )}
              {selectedPurchase.status.toLowerCase() !== 'pending' && (
                <div className="flex justify-end pt-4 border-t border-gray-200">
                  <button
                    onClick={() => setShowModal(false)}
                    className="px-6 py-3 bg-gray-100 text-gray-700 rounded-xl hover:bg-gray-200 transition-colors font-semibold"
                  >
                    Close
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </DashboardLayout>
  )
}
