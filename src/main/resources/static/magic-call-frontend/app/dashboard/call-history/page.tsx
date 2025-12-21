"use client"

import { useEffect, useState } from "react"
import { DashboardLayout } from "@/components/DashboardLayout"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Button } from "@/components/ui/button"
import { api } from "@/lib/api"
import { formatDate } from "@/lib/utils"
import type { CallHistory } from "@/types"
import { Phone, RefreshCw, Trash2, Clock } from "lucide-react"

export default function CallHistoryPage() {
  const [callHistory, setCallHistory] = useState<CallHistory[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadCallHistory()
  }, [])

  const loadCallHistory = async () => {
    try {
      setLoading(true)
      const data = await api.get<CallHistory[]>("/call-history")
      setCallHistory(Array.isArray(data) ? data : [])
    } catch (error) {
      console.error("Error loading call history:", error)
    } finally {
      setLoading(false)
    }
  }

  const handleDelete = async (id: number, uuid: string) => {
    if (confirm(`Delete call record with UUID "${uuid}"?`)) {
      try {
        await api.delete(`/call-history/${id}`)
        await loadCallHistory()
      } catch (error) {
        console.error("Error deleting call history:", error)
        alert("Failed to delete call record")
      }
    }
  }

  const formatDuration = (seconds: number) => {
    if (!seconds || seconds === 0) return "0s"
    
    const hours = Math.floor(seconds / 3600)
    const minutes = Math.floor((seconds % 3600) / 60)
    const secs = seconds % 60
    
    const parts = []
    if (hours > 0) parts.push(`${hours}h`)
    if (minutes > 0) parts.push(`${minutes}m`)
    if (secs > 0 || parts.length === 0) parts.push(`${secs}s`)
    
    return parts.join(" ")
  }

  const getStatusColor = (status: string | null) => {
    switch (status?.toUpperCase()) {
      case "ANSWERED":
        return "bg-green-100 text-green-700"
      case "COMPLETED":
        return "bg-blue-100 text-blue-700"
      case "RESERVED":
        return "bg-yellow-100 text-yellow-700"
      case "REJECTED":
        return "bg-red-100 text-red-700"
      case "FAILED":
        return "bg-red-100 text-red-700"
      default:
        return "bg-gray-100 text-gray-700"
    }
  }

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Call History Management</h1>
            <p className="text-gray-600 mt-2">View all call records and CDR data</p>
          </div>
          <Button onClick={loadCallHistory} variant="outline">
            <RefreshCw className="h-4 w-4 mr-2" />
            Refresh
          </Button>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>All Call Records ({callHistory.length})</CardTitle>
          </CardHeader>
          <CardContent>
            {loading ? (
              <div className="text-center py-12">
                <div className="animate-spin h-8 w-8 border-4 border-blue-500 border-t-transparent rounded-full mx-auto"></div>
                <p className="text-gray-500 mt-4">Loading call history...</p>
              </div>
            ) : callHistory.length === 0 ? (
              <div className="text-center py-12 text-gray-500">
                <Phone className="h-12 w-12 mx-auto mb-4 text-gray-400" />
                <p>No call records found</p>
              </div>
            ) : (
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>ID</TableHead>
                      <TableHead>A-Party</TableHead>
                      <TableHead>B-Party</TableHead>
                      <TableHead>User</TableHead>
                      <TableHead>Call UUID</TableHead>
                      <TableHead>Duration</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead>Codec</TableHead>
                      <TableHead>Create Time</TableHead>
                      <TableHead>Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {callHistory.map((call) => (
                      <TableRow key={call.id}>
                        <TableCell className="font-mono">#{call.id}</TableCell>
                        <TableCell className="font-semibold">{call.aparty}</TableCell>
                        <TableCell>{call.bparty || "-"}</TableCell>
                        <TableCell>
                          {call.user?.username || (call.idUser ? `User #${call.idUser}` : "-")}
                        </TableCell>
                        <TableCell className="font-mono text-xs">
                          <span className="px-2 py-1 bg-gray-100 text-gray-700 rounded">
                            {call.uuid.substring(0, 8)}...
                          </span>
                        </TableCell>
                        <TableCell>
                          <div className="flex items-center gap-1 text-sm">
                            <Clock className="h-3 w-3 text-gray-500" />
                            {formatDuration(call.duration)}
                          </div>
                        </TableCell>
                        <TableCell>
                          <span className={`px-3 py-1 text-xs rounded-full font-medium ${getStatusColor(call.status)}`}>
                            {call.status || "UNKNOWN"}
                          </span>
                        </TableCell>
                        <TableCell className="text-sm">{call.codec || "-"}</TableCell>
                        <TableCell className="text-sm text-gray-600">
                          {formatDate(call.createTime)}
                        </TableCell>
                        <TableCell>
                          <Button
                            variant="destructive"
                            size="sm"
                            onClick={() => handleDelete(call.id, call.uuid)}
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  )
}

