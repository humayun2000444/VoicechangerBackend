"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { DashboardLayout } from "@/components/DashboardLayout"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Button } from "@/components/ui/button"
import { api } from "@/lib/api"
import { formatDate, formatCurrency } from "@/lib/utils"
import { Users, CreditCard, Phone, Mic, ArrowRight } from "lucide-react"
import type { DashboardStats, Transaction, VoicePurchaseResponse } from "@/types"

export default function DashboardPage() {
  const router = useRouter()
  const [stats, setStats] = useState<DashboardStats | null>(null)
  const [recentTopUps, setRecentTopUps] = useState<Transaction[]>([])
  const [recentVoicePurchases, setRecentVoicePurchases] = useState<VoicePurchaseResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    loadDashboardData()
  }, [])

  const loadDashboardData = async () => {
    try {
      setLoading(true)
      setError(null)

      // Load stats, recent top-ups, and voice purchases in parallel
      const [statsRes, topUpsRes, voicePurchasesRes] = await Promise.all([
        api.get("/dashboard/stats"),
        api.get("/topup"),
        api.get("/voice-purchase"),
      ])

      // Set stats from dedicated endpoint
      setStats({
        totalUsers: statsRes.totalUsers || 0,
        totalVoiceTypes: statsRes.totalVoiceTypes || 0,
        totalTopUps: statsRes.totalTopUps || 0,
        totalCallHistory: statsRes.totalCallHistory || 0,
      })

      // Get recent 5 top-ups
      if (Array.isArray(topUpsRes)) {
        setRecentTopUps(topUpsRes.slice(0, 5))
      } else if (topUpsRes.data && Array.isArray(topUpsRes.data)) {
        setRecentTopUps(topUpsRes.data.slice(0, 5))
      }

      // Get recent 5 voice purchases
      if (Array.isArray(voicePurchasesRes)) {
        setRecentVoicePurchases(voicePurchasesRes.slice(0, 5))
      } else if (voicePurchasesRes.data && Array.isArray(voicePurchasesRes.data)) {
        setRecentVoicePurchases(voicePurchasesRes.data.slice(0, 5))
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
          <CardHeader className="flex flex-row items-center justify-between">
            <CardTitle>Recent Top-Ups (Top 5)</CardTitle>
            <Button
              variant="outline"
              size="sm"
              onClick={() => router.push("/dashboard/topup")}
              className="gap-2"
            >
              See More
              <ArrowRight className="h-4 w-4" />
            </Button>
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

        {/* Recent Voice Purchases */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between">
            <CardTitle>Recent Voice Purchases (Top 5)</CardTitle>
            <Button
              variant="outline"
              size="sm"
              onClick={() => router.push("/dashboard/voice-purchases")}
              className="gap-2"
            >
              See More
              <ArrowRight className="h-4 w-4" />
            </Button>
          </CardHeader>
          <CardContent>
            {loading ? (
              <div className="text-center py-8 text-gray-500">Loading...</div>
            ) : recentVoicePurchases.length === 0 ? (
              <div className="text-center py-8 text-gray-500">No voice purchases yet</div>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>ID</TableHead>
                    <TableHead>User</TableHead>
                    <TableHead>Voice Type</TableHead>
                    <TableHead>Subscription</TableHead>
                    <TableHead>Amount</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Purchase Date</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {recentVoicePurchases.map((purchase) => (
                    <TableRow key={purchase.id}>
                      <TableCell>#{purchase.id}</TableCell>
                      <TableCell>{purchase.user?.username || `User #${purchase.idUser}`}</TableCell>
                      <TableCell>{purchase.voiceType?.voiceName || `Voice #${purchase.idVoiceType}`}</TableCell>
                      <TableCell className="capitalize">{purchase.subscriptionType || 'N/A'}</TableCell>
                      <TableCell>{purchase.amount} BDT</TableCell>
                      <TableCell>
                        <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                          purchase.status === "approved"
                            ? "bg-green-100 text-green-800"
                            : purchase.status === "pending"
                            ? "bg-yellow-100 text-yellow-800"
                            : "bg-red-100 text-red-800"
                        }`}>
                          {purchase.status}
                        </span>
                      </TableCell>
                      <TableCell>{formatDate(purchase.purchaseDate)}</TableCell>
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
