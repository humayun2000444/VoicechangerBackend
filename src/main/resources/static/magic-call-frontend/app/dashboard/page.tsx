"use client"

import { useEffect, useState } from "react"
import { DashboardLayout } from "@/components/DashboardLayout"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { api } from "@/lib/api"
import { formatDate, formatCurrency } from "@/lib/utils"
import { Users, CreditCard, Phone, Mic } from "lucide-react"
import type { DashboardStats, Transaction } from "@/types"

export default function DashboardPage() {
  const [stats, setStats] = useState<DashboardStats | null>(null)
  const [recentTopUps, setRecentTopUps] = useState<Transaction[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    loadDashboardData()
  }, [])

  const loadDashboardData = async () => {
    try {
      setLoading(true)
      setError(null)

      // Load all data in parallel
      const [usersRes, voiceTypesRes, topUpsRes, callHistoryRes] = await Promise.all([
        api.get("/api/users"),
        api.get("/api/voice-types"),
        api.get("/api/topup"),
        api.get("/api/call-history"),
      ])

      // Extract data from wrapped responses
      const users = usersRes.data?.data || []
      const voiceTypes = voiceTypesRes.data?.data || []
      const topUps = topUpsRes.data?.data || []
      const callHistory = callHistoryRes.data?.data || []

      setStats({
        totalUsers: users.length || 0,
        totalVoiceTypes: voiceTypes.length || 0,
        totalTopUps: topUps.length || 0,
        totalCallHistory: callHistory.length || 0,
      })

      // Get recent 5 top-ups
      if (Array.isArray(topUps)) {
        setRecentTopUps(topUps.slice(0, 5))
      }
    } catch (error: any) {
      console.error("Error loading dashboard data:", error)
      setError(error.response?.data?.message || "Failed to load dashboard data. Please try again.")
    } finally {
      setLoading(false)
    }
  }

  const statsCards = [
    {
      title: "Total Users",
      value: stats?.totalUsers || 0,
      icon: Users,
      color: "bg-blue-500",
    },
    {
      title: "Voice Types",
      value: stats?.totalVoiceTypes || 0,
      icon: Mic,
      color: "bg-green-500",
    },
    {
      title: "Total Top-Ups",
      value: stats?.totalTopUps || 0,
      icon: CreditCard,
      color: "bg-cyan-500",
    },
    {
      title: "Call History",
      value: stats?.totalCallHistory || 0,
      icon: Phone,
      color: "bg-orange-500",
    },
  ]

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
          <p className="text-gray-600 mt-2">Welcome to Magic Call Admin Panel</p>
        </div>

        {/* Error Message */}
        {error && (
          <Card className="border-red-200 bg-red-50">
            <CardContent className="p-4">
              <p className="text-red-800">{error}</p>
              <button
                onClick={loadDashboardData}
                className="mt-2 text-sm text-red-600 hover:text-red-800 underline"
              >
                Try Again
              </button>
            </CardContent>
          </Card>
        )}

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {statsCards.map((stat) => (
            <Card key={stat.title} className="overflow-hidden">
              <CardContent className="p-6">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-gray-600">{stat.title}</p>
                    <p className="text-3xl font-bold text-gray-900 mt-2">
                      {loading ? "-" : stat.value}
                    </p>
                  </div>
                  <div className={`${stat.color} p-3 rounded-lg`}>
                    <stat.icon className="h-8 w-8 text-white" />
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>

        {/* Recent Top-Ups */}
        <Card>
          <CardHeader>
            <CardTitle>Recent Top-Ups</CardTitle>
          </CardHeader>
          <CardContent>
            {loading ? (
              <div className="text-center py-8 text-gray-500">Loading...</div>
            ) : recentTopUps.length === 0 ? (
              <div className="text-center py-8 text-gray-500">No top-ups yet</div>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>ID</TableHead>
                    <TableHead>User</TableHead>
                    <TableHead>Method</TableHead>
                    <TableHead>Amount</TableHead>
                    <TableHead>Duration</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Date</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {recentTopUps.map((topup) => (
                    <TableRow key={topup.id}>
                      <TableCell>#{topup.id}</TableCell>
                      <TableCell>{topup.user?.username || `User #${topup.idUser}`}</TableCell>
                      <TableCell className="capitalize">{topup.transactionMethod}</TableCell>
                      <TableCell>{topup.amount} BDT</TableCell>
                      <TableCell>{topup.durationInSeconds}s</TableCell>
                      <TableCell>
                        <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                          topup.status === "approved"
                            ? "bg-green-100 text-green-800"
                            : topup.status === "pending"
                            ? "bg-yellow-100 text-yellow-800"
                            : "bg-red-100 text-red-800"
                        }`}>
                          {topup.status}
                        </span>
                      </TableCell>
                      <TableCell>{formatDate(topup.date)}</TableCell>
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
