"use client"

import { useEffect, useState } from "react"
import { useRouter, usePathname } from "next/navigation"
import Link from "next/link"
import {
  LayoutDashboard,
  Mic,
  Users,
  LogOut,
  Phone,
  Menu,
  PhoneCall,
  Wallet,
  ShoppingCart
} from "lucide-react"
import { Button } from "@/components/ui/button"
import { authService } from "@/lib/auth"

const navigation = [
  { name: "Dashboard", href: "/dashboard", icon: LayoutDashboard },
  { name: "Top-Up Requests", href: "/dashboard/topup", icon: Wallet },
  { name: "Voice Purchases", href: "/dashboard/voice-purchases", icon: ShoppingCart },
  { name: "Voice Types", href: "/dashboard/voice-types", icon: Mic },
  { name: "Users", href: "/dashboard/users", icon: Users },
  { name: "Call History", href: "/dashboard/call-history", icon: PhoneCall },
]

export function DashboardLayout({ children }: { children: React.ReactNode }) {
  const router = useRouter()
  const pathname = usePathname()
  const [username, setUsername] = useState("")
  const [sidebarOpen, setSidebarOpen] = useState(false)

  useEffect(() => {
    const user = authService.getUsername()
    if (user) {
      setUsername(user)
    } else {
      router.push("/login")
    }
  }, [router])

  const handleLogout = () => {
    authService.logout()
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Top Navigation */}
      <nav className="bg-gradient-to-r from-purple-600 via-purple-700 to-pink-600 text-white shadow-lg">
        <div className="mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex items-center">
              <button
                onClick={() => setSidebarOpen(!sidebarOpen)}
                className="md:hidden mr-2 p-2 rounded-md hover:bg-white/10 transition"
              >
                <Menu className="h-6 w-6" />
              </button>
              <div className="bg-white/20 p-2 rounded-lg backdrop-blur-sm">
                <Phone className="h-6 w-6" />
              </div>
              <span className="ml-3 text-xl font-bold">Magic Call Admin</span>
            </div>
            <div className="flex items-center gap-4">
              <div className="hidden sm:flex items-center gap-2 bg-white/10 px-3 py-1.5 rounded-lg backdrop-blur-sm">
                <div className="w-8 h-8 rounded-full bg-white/20 flex items-center justify-center">
                  <span className="text-sm font-semibold">{username.charAt(0).toUpperCase()}</span>
                </div>
                <span className="text-sm font-medium">{username}</span>
              </div>
              <Button
                variant="ghost"
                size="sm"
                onClick={handleLogout}
                className="text-white hover:bg-white/10 transition"
              >
                <LogOut className="h-4 w-4 mr-2" />
                Logout
              </Button>
            </div>
          </div>
        </div>
      </nav>

      <div className="flex">
        {/* Sidebar */}
        <aside className={`${sidebarOpen ? "block" : "hidden"} md:block w-64 bg-white shadow-md min-h-[calc(100vh-4rem)] border-r border-gray-200`}>
          <nav className="mt-5 px-2">
            {navigation.map((item) => {
              const isActive = pathname === item.href
              return (
                <Link
                  key={item.name}
                  href={item.href}
                  className={`group flex items-center px-3 py-3 text-sm font-medium rounded-lg mb-1 transition-all ${
                    isActive
                      ? "bg-gradient-to-r from-purple-50 to-pink-50 text-purple-700 border-l-4 border-purple-600"
                      : "text-gray-600 hover:bg-gray-50 hover:text-gray-900"
                  }`}
                >
                  <item.icon
                    className={`mr-3 h-5 w-5 ${
                      isActive ? "text-purple-600" : "text-gray-400 group-hover:text-gray-500"
                    }`}
                  />
                  {item.name}
                </Link>
              )
            })}
          </nav>
        </aside>

        {/* Main Content */}
        <main className="flex-1 p-6">
          {children}
        </main>
      </div>
    </div>
  )
}
