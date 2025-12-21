"use client"

import { useEffect, useState } from "react"
import { DashboardLayout } from "@/components/DashboardLayout"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Button } from "@/components/ui/button"
import { api } from "@/lib/api"
import { formatDate } from "@/lib/utils"
import type { VoiceType } from "@/types"
import { Mic, RefreshCw, Edit, Trash2, Plus } from "lucide-react"

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

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Voice Types Management</h1>
            <p className="text-gray-600 mt-2">Manage available voice transformation types</p>
          </div>
          <div className="flex gap-2">
            <Button onClick={loadVoiceTypes} variant="outline">
              <RefreshCw className="h-4 w-4 mr-2" />
              Refresh
            </Button>
            <Button>
              <Plus className="h-4 w-4 mr-2" />
              Add Voice Type
            </Button>
          </div>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>All Voice Types ({voiceTypes.length})</CardTitle>
          </CardHeader>
          <CardContent>
            {loading ? (
              <div className="text-center py-12">
                <div className="animate-spin h-8 w-8 border-4 border-blue-500 border-t-transparent rounded-full mx-auto"></div>
                <p className="text-gray-500 mt-4">Loading voice types...</p>
              </div>
            ) : voiceTypes.length === 0 ? (
              <div className="text-center py-12 text-gray-500">
                <Mic className="h-12 w-12 mx-auto mb-4 text-gray-400" />
                <p>No voice types found</p>
              </div>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>ID</TableHead>
                    <TableHead>Voice Name</TableHead>
                    <TableHead>Code</TableHead>
                    <TableHead>Created At</TableHead>
                    <TableHead>Updated At</TableHead>
                    <TableHead>Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {voiceTypes.map((voiceType) => (
                    <TableRow key={voiceType.id}>
                      <TableCell className="font-mono">#{voiceType.id}</TableCell>
                      <TableCell className="font-semibold">{voiceType.voiceName}</TableCell>
                      <TableCell>
                        <span className="px-3 py-1 bg-purple-100 text-purple-700 text-sm rounded-full font-mono font-semibold">
                          {voiceType.code}
                        </span>
                      </TableCell>
                      <TableCell className="text-sm text-gray-600">
                        {formatDate(voiceType.createdAt)}
                      </TableCell>
                      <TableCell className="text-sm text-gray-600">
                        {formatDate(voiceType.updatedAt)}
                      </TableCell>
                      <TableCell>
                        <div className="flex gap-2">
                          <Button variant="outline" size="sm">
                            <Edit className="h-4 w-4" />
                          </Button>
                          <Button
                            variant="destructive"
                            size="sm"
                            onClick={() => handleDelete(voiceType.id, voiceType.voiceName)}
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            )}
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  )
}
