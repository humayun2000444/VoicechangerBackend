"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { DashboardLayout } from "@/components/DashboardLayout"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { api } from "@/lib/api"
import { formatDate } from "@/lib/utils"
import { Users, CreditCard, Phone, Mic, ArrowRight, TrendingUp, Activity } from "lucide-react"
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

      const [statsRes, topUpsRes, voicePurchasesRes] = await Promise.all([
        api.get("/dashboard/stats"),
        api.get("/topup"),
        api.get("/voice-purchase"),
      ])

      setStats({
        totalUsers: statsRes.totalUsers || 0,
        totalVoiceTypes: statsRes.totalVoiceTypes || 0,
        totalTopUps: statsRes.totalTopUps || 0,
        totalCallHistory: statsRes.totalCallHistory || 0,
      })

      if (Array.isArray(topUpsRes)) {
        setRecentTopUps(topUpsRes.slice(0, 5))
      } else if (topUpsRes.data && Array.isArray(topUpsRes.data)) {
        setRecentTopUps(topUpsRes.data.slice(0, 5))
      }

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
      gradient: "from-purple-500 to-purple-600",
      bgGradient: "from-purple-50 to-purple-100",
      iconBg: "bg-purple-500",
    },
    {
      title: "Voice Types",
      value: stats?.totalVoiceTypes || 0,
      icon: Mic,
      gradient: "from-pink-500 to-pink-600",
      bgGradient: "from-pink-50 to-pink-100",
      iconBg: "bg-pink-500",
    },
    {
      title: "Total Top-Ups",
      value: stats?.totalTopUps || 0,
      icon: CreditCard,
      gradient: "from-blue-500 to-blue-600",
      bgGradient: "from-blue-50 to-blue-100",
      iconBg: "bg-blue-500",
    },
    {
      title: "Call History",
      value: stats?.totalCallHistory || 0,
      icon: Phone,
      gradient: "from-emerald-500 to-emerald-600",
      bgGradient: "from-emerald-50 to-emerald-100",
      iconBg: "bg-emerald-500",
    },
  ]

  return (
    <DashboardLayout>
      <div className="space-y-8">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-4xl font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent">
              Dashboard
            </h1>
            <p className="text-gray-500 mt-2 flex items-center gap-2">
              <Activity className="h-4 w-4" />
              Welcome to Magic Call Admin Panel
            </p>
          </div>
          <Button
            onClick={loadDashboardData}
            className="bg-gradient-to-r from-purple-600 to-pink-600 hover:from-purple-700 hover:to-pink-700 text-white shadow-lg hover:shadow-xl transition-all"
          >
            <TrendingUp className="h-4 w-4 mr-2" />
            Refresh Data
          </Button>
        </div>

        {/* Error Message */}
        {error && (
          <Card className="border-red-200 bg-gradient-to-r from-red-50 to-pink-50 shadow-md">
            <CardContent className="p-4">
              <p className="text-red-800 font-medium">{error}</p>
              <button
                onClick={loadDashboardData}
                className="mt-2 text-sm text-red-600 hover:text-red-800 underline font-semibold"
              >
                Try Again
              </button>
            </CardContent>
          </Card>
        )}

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {statsCards.map((stat, index) => (
            <Card
              key={stat.title}
              className="overflow-hidden border-0 shadow-lg hover:shadow-xl transition-all duration-300 transform hover:-translate-y-1"
              style={{ animationDelay: `${index * 100}ms` }}
            >
              <CardContent className="p-0">
                <div className={`bg-gradient-to-br ${stat.bgGradient} p-6`}>
                  <div className="flex items-center justify-between mb-4">
                    <div className={`${stat.iconBg} p-3 rounded-xl shadow-lg`}>
                      <stat.icon className="h-6 w-6 text-white" />
                    </div>
                    <div className="text-right">
                      <p className="text-sm font-medium text-gray-600 mb-1">{stat.title}</p>
                      <p className={`text-3xl font-bold bg-gradient-to-r ${stat.gradient} bg-clip-text text-transparent`}>
                        {loading ? "..." : stat.value}
                      </p>
                    </div>
                  </div>
                  <div className="h-1 bg-gradient-to-r ${stat.gradient} rounded-full opacity-30"></div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>

        {/* Recent Top-Ups */}
        <Card className="border-0 shadow-xl overflow-hidden">
          <CardHeader className="bg-gradient-to-r from-purple-50 to-pink-50 border-b border-purple-100">
            <div className="flex items-center justify-between">
              <div>
                <CardTitle className="text-2xl font-bold text-gray-800 flex items-center gap-2">
                  <CreditCard className="h-6 w-6 text-purple-600" />
                  Recent Top-Ups
                </CardTitle>
                <p className="text-sm text-gray-600 mt-1">Latest 5 top-up requests</p>
              </div>
              <Button
                variant="outline"
                size="sm"
                onClick={() => router.push("/dashboard/topup")}
                className="gap-2 border-purple-200 text-purple-700 hover:bg-purple-50 shadow-sm"
              >
                View All
                <ArrowRight className="h-4 w-4" />
              </Button>
            </div>
          </CardHeader>
          <CardContent className="p-0">
            {loading ? (
              <div className="text-center py-12">
                <div className="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-purple-600 border-r-transparent"></div>
                <p className="text-gray-500 mt-4">Loading top-ups...</p>
              </div>
            ) : recentTopUps.length === 0 ? (
              <div className="text-center py-12">
                <CreditCard className="h-12 w-12 text-gray-300 mx-auto mb-3" />
                <p className="text-gray-500">No top-ups yet</p>
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead className="bg-gray-50 border-b border-gray-200">
                    <tr>
                      <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">ID</th>
                      <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">User</th>
                      <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Method</th>
                      <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Amount</th>
                      <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Duration</th>
                      <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Status</th>
                      <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Date</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {recentTopUps.map((topup, index) => (
                      <tr key={topup.id} className="hover:bg-purple-50/50 transition-colors">
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className="text-sm font-bold text-purple-600">#{topup.id}</span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className="text-sm font-medium text-gray-900">
                            {topup.user?.username || `User #${topup.idUser}`}
                          </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className="text-sm text-gray-700 capitalize">{topup.transactionMethod}</span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className="text-sm font-bold text-gray-900">{topup.amount} BDT</span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className="text-sm font-medium text-purple-600">{topup.durationInSeconds}s</span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className={`px-3 py-1 rounded-full text-xs font-semibold ${
                            topup.status === "approved"
                              ? "bg-green-100 text-green-700 border border-green-200"
                              : topup.status === "pending"
                              ? "bg-yellow-100 text-yellow-700 border border-yellow-200"
                              : "bg-red-100 text-red-700 border border-red-200"
                          }`}>
                            {topup.status}
                          </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                          {formatDate(topup.date)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Recent Voice Purchases */}
        <Card className="border-0 shadow-xl overflow-hidden">
          <CardHeader className="bg-gradient-to-r from-pink-50 to-purple-50 border-b border-pink-100">
            <div className="flex items-center justify-between">
              <div>
                <CardTitle className="text-2xl font-bold text-gray-800 flex items-center gap-2">
                  <Mic className="h-6 w-6 text-pink-600" />
                  Recent Voice Purchases
                </CardTitle>
                <p className="text-sm text-gray-600 mt-1">Latest 5 voice purchase requests</p>
              </div>
              <Button
                variant="outline"
                size="sm"
                onClick={() => router.push("/dashboard/voice-purchases")}
                className="gap-2 border-pink-200 text-pink-700 hover:bg-pink-50 shadow-sm"
              >
                View All
                <ArrowRight className="h-4 w-4" />
              </Button>
            </div>
          </CardHeader>
          <CardContent className="p-0">
            {loading ? (
              <div className="text-center py-12">
                <div className="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-pink-600 border-r-transparent"></div>
                <p className="text-gray-500 mt-4">Loading voice purchases...</p>
              </div>
            ) : recentVoicePurchases.length === 0 ? (
              <div className="text-center py-12">
                <Mic className="h-12 w-12 text-gray-300 mx-auto mb-3" />
                <p className="text-gray-500">No voice purchases yet</p>
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead className="bg-gray-50 border-b border-gray-200">
                    <tr>
                      <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">ID</th>
                      <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">User</th>
                      <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Voice Type</th>
                      <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Subscription</th>
                      <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Amount</th>
                      <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Status</th>
                      <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Purchase Date</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {recentVoicePurchases.map((purchase, index) => (
                      <tr key={purchase.id} className="hover:bg-pink-50/50 transition-colors">
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className="text-sm font-bold text-pink-600">#{purchase.id}</span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className="text-sm font-medium text-gray-900">
                            {purchase.user?.username || `User #${purchase.idUser}`}
                          </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className="text-sm font-medium text-gray-900">
                            {purchase.voiceType?.voiceName || `Voice #${purchase.idVoiceType}`}
                          </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className="px-3 py-1 rounded-full text-xs font-semibold bg-purple-100 text-purple-700 border border-purple-200 capitalize">
                            {purchase.subscriptionType || 'N/A'}
                          </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className="text-sm font-bold text-gray-900">{purchase.amount} BDT</span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className={`px-3 py-1 rounded-full text-xs font-semibold ${
                            purchase.status === "approved"
                              ? "bg-green-100 text-green-700 border border-green-200"
                              : purchase.status === "pending"
                              ? "bg-yellow-100 text-yellow-700 border border-yellow-200"
                              : "bg-red-100 text-red-700 border border-red-200"
                          }`}>
                            {purchase.status}
                          </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                          {formatDate(purchase.purchaseDate)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  )
}
