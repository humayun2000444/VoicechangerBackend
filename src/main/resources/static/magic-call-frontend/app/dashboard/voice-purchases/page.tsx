'use client'

import { useState, useEffect } from 'react'
import { DashboardLayout } from '@/components/DashboardLayout'
import { api } from '@/lib/api'
import { VoicePurchaseResponse } from '@/types'
import { format } from 'date-fns'

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
    if (!confirm('Are you sure you want to approve this voice purchase? The user will get permanent access to this voice type.')) {
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
        <div className="flex justify-center items-center h-64">
          <div className="text-lg text-gray-600">Loading voice purchases...</div>
        </div>
      </DashboardLayout>
    )
  }

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Voice Purchase Management</h1>
            <p className="mt-2 text-sm text-gray-600">
              Manage voice type purchase requests and approve/reject transactions
            </p>
          </div>
          <button
            onClick={fetchPurchases}
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
            <div className="mt-2 text-3xl font-bold text-gray-900">{purchases.length}</div>
          </div>
          <div className="bg-white rounded-lg shadow p-6 border-l-4 border-yellow-500">
            <div className="text-sm font-medium text-gray-600">Pending</div>
            <div className="mt-2 text-3xl font-bold text-yellow-600">{pendingCount}</div>
          </div>
          <div className="bg-white rounded-lg shadow p-6 border-l-4 border-green-500">
            <div className="text-sm font-medium text-gray-600">Approved</div>
            <div className="mt-2 text-3xl font-bold text-green-600">{approvedCount}</div>
          </div>
          <div className="bg-white rounded-lg shadow p-6 border-l-4 border-red-500">
            <div className="text-sm font-medium text-gray-600">Rejected</div>
            <div className="mt-2 text-3xl font-bold text-red-600">{rejectedCount}</div>
          </div>
        </div>

        {/* Filter Tabs */}
        <div className="bg-white rounded-lg shadow">
          <div className="border-b border-gray-200">
            <nav className="flex -mb-px">
              {['ALL', 'PENDING', 'APPROVED', 'REJECTED'].map((status) => (
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
                      {status === 'APPROVED' && approvedCount}
                      {status === 'REJECTED' && rejectedCount}
                    </span>
                  )}
                </button>
              ))}
            </nav>
          </div>

          {/* Purchases Table */}
          <div className="overflow-x-auto">
            {filteredPurchases.length === 0 ? (
              <div className="text-center py-12 text-gray-500">
                No voice purchase requests found
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
                      Voice Type
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Subscription
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Payment Method
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Transaction ID
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Amount (BDT)
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Expiry Date
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
                  {filteredPurchases.map((purchase) => (
                    <tr key={purchase.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                        #{purchase.id}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm font-medium text-gray-900">
                          {purchase.user?.username || `User #${purchase.idUser}`}
                        </div>
                        {purchase.user && (
                          <div className="text-sm text-gray-500">
                            {purchase.user.firstName} {purchase.user.lastName}
                          </div>
                        )}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm font-medium text-gray-900">
                          {purchase.voiceType?.voiceName || 'Unknown'}
                        </div>
                        {purchase.voiceType && (
                          <div className="text-xs text-gray-500">
                            Code: {purchase.voiceType.code}
                          </div>
                        )}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className="px-2 py-1 text-xs font-semibold rounded-full bg-blue-100 text-blue-800 capitalize">
                          {purchase.subscriptionType || 'N/A'}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        <span className="uppercase">
                          {purchase.transactionMethod || 'N/A'}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm font-mono text-gray-700">
                          {purchase.tnxId || 'N/A'}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm font-bold text-gray-900">
                          ৳{purchase.amount.toFixed(2)}
                        </div>
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
                        <span
                          className={`px-3 py-1 inline-flex text-xs leading-5 font-semibold rounded-full border capitalize ${getStatusColor(
                            purchase.status
                          )}`}
                        >
                          {purchase.status}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                        <button
                          onClick={() => openDetailsModal(purchase)}
                          className="text-blue-600 hover:text-blue-900"
                        >
                          View
                        </button>
                        {purchase.status.toLowerCase() === 'pending' && (
                          <>
                            <span className="text-gray-300">|</span>
                            <button
                              onClick={() => handleApprove(purchase.id)}
                              disabled={processingId === purchase.id}
                              className="text-green-600 hover:text-green-900 disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                              {processingId === purchase.id ? 'Processing...' : 'Approve'}
                            </button>
                            <span className="text-gray-300">|</span>
                            <button
                              onClick={() => handleReject(purchase.id)}
                              disabled={processingId === purchase.id}
                              className="text-red-600 hover:text-red-900 disabled:opacity-50 disabled:cursor-not-allowed"
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
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <div className="flex">
            <div className="flex-shrink-0">
              <svg className="h-5 w-5 text-blue-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
              </svg>
            </div>
            <div className="ml-3 flex-1">
              <h3 className="text-sm font-medium text-blue-800">Voice Purchase Information</h3>
              <div className="mt-2 text-sm text-blue-700">
                <ul className="list-disc list-inside space-y-1">
                  <li><strong>Price:</strong> 50 BDT per voice type</li>
                  <li><strong>Subscription Types:</strong> Monthly (1 month access) or Yearly (1 year access)</li>
                  <li><strong>Trial System:</strong> Voice Type 3 (Child Voice) has 3-day trial, Voice Type 4 (Robot Voice) is free forever</li>
                  <li><strong>Workflow:</strong> User selects subscription period → Submits request with payment → Admin reviews → Approve/Reject → Access granted until expiry</li>
                  <li><strong>Payment Methods:</strong> bKash, Nagad, Rocket</li>
                  <li><strong>Note:</strong> Approving a request will grant the user access to the voice type until the expiry date (calculated from purchase date)</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Details Modal */}
      {showModal && selectedPurchase && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50" onClick={() => setShowModal(false)}>
          <div className="relative top-20 mx-auto p-5 border w-11/12 md:w-3/4 lg:w-1/2 shadow-lg rounded-md bg-white" onClick={(e) => e.stopPropagation()}>
            <div className="flex justify-between items-center border-b pb-3">
              <h3 className="text-lg font-semibold text-gray-900">Voice Purchase Details</h3>
              <button onClick={() => setShowModal(false)} className="text-gray-400 hover:text-gray-600">
                <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <div className="mt-4 grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <h4 className="font-semibold text-gray-700 mb-2">Purchase Information</h4>
                <div className="space-y-2 text-sm">
                  <div><span className="font-medium">Purchase ID:</span> #{selectedPurchase.id}</div>
                  <div><span className="font-medium">Voice Type:</span> {selectedPurchase.voiceType?.voiceName || 'Unknown'}</div>
                  <div><span className="font-medium">Code:</span> {selectedPurchase.voiceType?.code || 'N/A'}</div>
                  <div><span className="font-medium">Subscription:</span> <span className="capitalize">{selectedPurchase.subscriptionType || 'N/A'}</span></div>
                  <div><span className="font-medium">Amount:</span> ৳{selectedPurchase.amount.toFixed(2)}</div>
                  <div>
                    <span className="font-medium">Status:</span>{' '}
                    <span className={`px-2 py-1 text-xs rounded-full capitalize ${getStatusColor(selectedPurchase.status)}`}>
                      {selectedPurchase.status}
                    </span>
                  </div>
                  <div><span className="font-medium">Purchase Date:</span> {format(new Date(selectedPurchase.purchaseDate), 'MMM dd, yyyy hh:mm a')}</div>
                  {selectedPurchase.expiryDate && (
                    <div><span className="font-medium">Expiry Date:</span> {format(new Date(selectedPurchase.expiryDate), 'MMM dd, yyyy hh:mm a')}</div>
                  )}
                  {selectedPurchase.updatedAt && (
                    <div><span className="font-medium">Updated:</span> {format(new Date(selectedPurchase.updatedAt), 'MMM dd, yyyy hh:mm a')}</div>
                  )}
                </div>
              </div>

              <div>
                <h4 className="font-semibold text-gray-700 mb-2">User & Payment Information</h4>
                <div className="space-y-2 text-sm">
                  <div><span className="font-medium">User ID:</span> {selectedPurchase.user?.id || selectedPurchase.idUser}</div>
                  <div><span className="font-medium">Username:</span> {selectedPurchase.user?.username || 'N/A'}</div>
                  <div><span className="font-medium">Name:</span> {selectedPurchase.user ? `${selectedPurchase.user.firstName} ${selectedPurchase.user.lastName}` : 'N/A'}</div>
                  <div className="pt-2 border-t">
                    <span className="font-medium">Payment Method:</span> <span className="uppercase">{selectedPurchase.transactionMethod || 'N/A'}</span>
                  </div>
                  <div><span className="font-medium">Transaction ID:</span> <span className="font-mono text-xs">{selectedPurchase.tnxId || 'N/A'}</span></div>
                </div>
              </div>
            </div>

            {selectedPurchase.status.toLowerCase() === 'pending' && (
              <div className="mt-6 flex justify-end space-x-3 border-t pt-4">
                <button
                  onClick={() => setShowModal(false)}
                  className="px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors"
                >
                  Close
                </button>
                <button
                  onClick={() => handleReject(selectedPurchase.id)}
                  disabled={processingId === selectedPurchase.id}
                  className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Reject
                </button>
                <button
                  onClick={() => handleApprove(selectedPurchase.id)}
                  disabled={processingId === selectedPurchase.id}
                  className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {processingId === selectedPurchase.id ? 'Processing...' : 'Approve'}
                </button>
              </div>
            )}
            {selectedPurchase.status.toLowerCase() !== 'pending' && (
              <div className="mt-6 flex justify-end border-t pt-4">
                <button
                  onClick={() => setShowModal(false)}
                  className="px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors"
                >
                  Close
                </button>
              </div>
            )}
          </div>
        </div>
      )}
    </DashboardLayout>
  )
}
