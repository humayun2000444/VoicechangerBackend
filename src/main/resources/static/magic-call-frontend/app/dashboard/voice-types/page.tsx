"use client"

import { useEffect, useState } from "react"
import { DashboardLayout } from "@/components/DashboardLayout"
import { Button } from "@/components/ui/button"
import { api } from "@/lib/api"
import { formatDate } from "@/lib/utils"
import type { VoiceType } from "@/types"
import { Mic, RefreshCw, Edit, Trash2, Plus, Activity } from "lucide-react"

export default function VoiceTypesPage() {
  const [voiceTypes, setVoiceTypes] = useState<VoiceType[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadVoiceTypes()
  }, [])

  const loadVoiceTypes = async () => {
    try {
      setLoading(true)
      const data = await api.get<VoiceType[]>("/voice-types")
      setVoiceTypes(Array.isArray(data) ? data : [])
    } catch (error) {
      console.error("Error loading voice types:", error)
    } finally {
      setLoading(false)
    }
  }

  const handleDelete = async (id: number, name: string) => {
    if (confirm(`Delete voice type "${name}"?`)) {
      try {
        await api.delete(`/voice-types/${id}`)
        await loadVoiceTypes()
      } catch (error) {
        console.error("Error deleting voice type:", error)
        alert("Failed to delete voice type")
      }
    }
  }

  if (loading) {
    return (
      <DashboardLayout>
        <div className="flex justify-center items-center h-96">
          <div className="text-center">
            <div className="inline-block h-12 w-12 animate-spin rounded-full border-4 border-solid border-purple-600 border-r-transparent mb-4"></div>
            <p className="text-lg text-gray-600 font-medium">Loading voice types...</p>
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
              Voice Types Management
            </h1>
            <p className="text-gray-600 mt-2 flex items-center gap-2">
              <Activity className="h-4 w-4" />
              Manage available voice transformation types
            </p>
          </div>
          <div className="flex gap-3">
            <button
              onClick={loadVoiceTypes}
              className="px-5 py-3 bg-white border-2 border-purple-200 text-purple-700 rounded-xl hover:bg-purple-50 transition-all shadow-sm hover:shadow-md flex items-center gap-2 font-semibold"
            >
              <RefreshCw className="h-5 w-5" />
              Refresh
            </button>
            <button className="px-6 py-3 bg-gradient-to-r from-purple-600 to-pink-600 hover:from-purple-700 hover:to-pink-700 text-white rounded-xl shadow-lg hover:shadow-xl transition-all flex items-center gap-2 font-semibold">
              <Plus className="h-5 w-5" />
              Add Voice Type
            </button>
          </div>
        </div>

        {/* Stats Card */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-gradient-to-br from-purple-50 to-purple-100 rounded-2xl shadow-lg p-6 border-0 hover:shadow-xl transition-all transform hover:-translate-y-1">
            <div className="flex items-center justify-between mb-3">
              <div className="bg-purple-500 p-3 rounded-xl shadow-lg">
                <Mic className="h-6 w-6 text-white" />
              </div>
            </div>
            <p className="text-sm font-medium text-gray-600 mb-1">Total Voice Types</p>
            <p className="text-3xl font-bold text-purple-600">{voiceTypes.length}</p>
          </div>

          <div className="bg-gradient-to-br from-pink-50 to-pink-100 rounded-2xl shadow-lg p-6 border-0 hover:shadow-xl transition-all transform hover:-translate-y-1">
            <div className="flex items-center justify-between mb-3">
              <div className="bg-pink-500 p-3 rounded-xl shadow-lg">
                <Activity className="h-6 w-6 text-white" />
              </div>
            </div>
            <p className="text-sm font-medium text-gray-600 mb-1">Active Types</p>
            <p className="text-3xl font-bold text-pink-600">{voiceTypes.length}</p>
          </div>

          <div className="bg-gradient-to-br from-blue-50 to-blue-100 rounded-2xl shadow-lg p-6 border-0 hover:shadow-xl transition-all transform hover:-translate-y-1">
            <div className="flex items-center justify-between mb-3">
              <div className="bg-blue-500 p-3 rounded-xl shadow-lg">
                <Edit className="h-6 w-6 text-white" />
              </div>
            </div>
            <p className="text-sm font-medium text-gray-600 mb-1">Recently Updated</p>
            <p className="text-3xl font-bold text-blue-600">
              {voiceTypes.length > 0 ? new Date(voiceTypes[0].updatedAt).toLocaleDateString('en', { month: 'short', day: 'numeric' }) : '-'}
            </p>
          </div>
        </div>

        {/* Voice Types Table */}
        <div className="bg-white rounded-2xl shadow-xl border-0 overflow-hidden">
          <div className="bg-gradient-to-r from-purple-50 to-pink-50 border-b border-purple-100 px-6 py-5">
            <h2 className="text-2xl font-bold text-gray-800 flex items-center gap-2">
              <Mic className="h-6 w-6 text-purple-600" />
              All Voice Types ({voiceTypes.length})
            </h2>
            <p className="text-sm text-gray-600 mt-1">Manage and configure voice transformation effects</p>
          </div>

          <div className="p-0">
            {voiceTypes.length === 0 ? (
              <div className="text-center py-16">
                <Mic className="h-16 w-16 text-gray-300 mx-auto mb-4" />
                <p className="text-gray-500 text-lg font-medium">No voice types found</p>
                <p className="text-gray-400 text-sm mt-2">Add a new voice type to get started</p>
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead className="bg-gray-50 border-b border-gray-200">
                    <tr>
                      <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">ID</th>
                      <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">Voice Name</th>
                      <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">Code</th>
                      <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">Created At</th>
                      <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">Updated At</th>
                      <th className="px-6 py-4 text-left text-xs font-bold text-gray-600 uppercase tracking-wider">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {voiceTypes.map((voiceType, index) => (
                      <tr key={voiceType.id} className="hover:bg-purple-50/30 transition-colors">
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className="text-sm font-bold text-purple-600">#{voiceType.id}</span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="flex items-center gap-3">
                            <div className={`w-10 h-10 rounded-xl flex items-center justify-center font-bold text-white shadow-md ${
                              index % 4 === 0 ? 'bg-purple-500' :
                              index % 4 === 1 ? 'bg-pink-500' :
                              index % 4 === 2 ? 'bg-blue-500' : 'bg-emerald-500'
                            }`}>
                              {voiceType.voiceName.charAt(0)}
                            </div>
                            <span className="text-sm font-bold text-gray-900">{voiceType.voiceName}</span>
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className="px-4 py-2 bg-gradient-to-r from-purple-100 to-pink-100 text-purple-700 text-sm rounded-xl font-mono font-bold border border-purple-200 shadow-sm">
                            {voiceType.code}
                          </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                          {formatDate(voiceType.createdAt)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                          {formatDate(voiceType.updatedAt)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="flex gap-2">
                            <button className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors border border-blue-200 hover:border-blue-300 shadow-sm">
                              <Edit className="h-4 w-4" />
                            </button>
                            <button
                              onClick={() => handleDelete(voiceType.id, voiceType.voiceName)}
                              className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors border border-red-200 hover:border-red-300 shadow-sm"
                            >
                              <Trash2 className="h-4 w-4" />
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      </div>
    </DashboardLayout>
  )
}
