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
      <nav className="bg-gradient-to-r from-blue-600 to-cyan-700 text-white shadow-lg">
        <div className="mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex items-center">
              <button
                onClick={() => setSidebarOpen(!sidebarOpen)}
                className="md:hidden mr-2 p-2 rounded-md hover:bg-white/10"
              >
                <Menu className="h-6 w-6" />
              </button>
              <Phone className="h-8 w-8" />
              <span className="ml-2 text-xl font-bold">Magic Call Admin</span>
            </div>
            <div className="flex items-center gap-4">
              <span className="text-sm">👤 {username}</span>
              <Button
                variant="ghost"
                size="sm"
                onClick={handleLogout}
                className="text-white hover:bg-white/10"
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
        <aside className={`${sidebarOpen ? "block" : "hidden"} md:block w-64 bg-white shadow-md min-h-[calc(100vh-4rem)]`}>
          <nav className="mt-5 px-2">
            {navigation.map((item) => {
              const isActive = pathname === item.href
              return (
                <Link
                  key={item.name}
                  href={item.href}
                  className={`group flex items-center px-2 py-3 text-sm font-medium rounded-md mb-1 ${
                    isActive
                      ? "bg-blue-50 text-blue-700"
                      : "text-gray-600 hover:bg-gray-50 hover:text-gray-900"
                  }`}
                >
                  <item.icon
                    className={`mr-3 h-5 w-5 ${
                      isActive ? "text-blue-700" : "text-gray-400 group-hover:text-gray-500"
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
