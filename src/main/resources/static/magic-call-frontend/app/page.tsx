"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import {
  Phone,
  Mic,
  Shield,
  Zap,
  Download,
  Check,
  Star,
  Users,
  Clock,
  Smartphone
} from "lucide-react"

export default function LandingPage() {
  const [email, setEmail] = useState("")
  const [subscribed, setSubscribed] = useState(false)

  const handleSubscribe = (e: React.FormEvent) => {
    e.preventDefault()
    // TODO: Implement email subscription API call
    setSubscribed(true)
    setEmail("")
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-white to-gray-50">
      {/* Header/Navigation */}
      <header className="border-b bg-white/80 backdrop-blur-md sticky top-0 z-50">
        <div className="container mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="bg-gradient-to-r from-purple-600 to-pink-600 p-2 rounded-lg">
              <Phone className="h-6 w-6 text-white" />
            </div>
            <span className="text-2xl font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent">
              Magic Call
            </span>
          </div>
          <nav className="hidden md:flex items-center gap-6">
            <a href="#features" className="text-gray-600 hover:text-purple-600 transition">Features</a>
            <a href="#pricing" className="text-gray-600 hover:text-purple-600 transition">Pricing</a>
            <a href="#download" className="text-gray-600 hover:text-purple-600 transition">Download</a>
          </nav>
        </div>
      </header>

      {/* Hero Section */}
      <section className="container mx-auto px-4 py-20 md:py-32">
        <div className="grid md:grid-cols-2 gap-12 items-center">
          <div className="space-y-6">
            <div className="inline-block">
              <span className="bg-purple-100 text-purple-700 px-4 py-2 rounded-full text-sm font-semibold">
                🎉 Transform Your Voice in Real-Time
              </span>
            </div>
            <h1 className="text-5xl md:text-6xl font-bold leading-tight">
              Make Every Call
              <span className="block bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent">
                Magical
              </span>
            </h1>
            <p className="text-xl text-gray-600 leading-relaxed">
              Change your voice during calls with amazing effects. Surprise your friends, prank your family, or just have fun with premium voice filters powered by AI.
            </p>
            <div className="flex flex-col sm:flex-row gap-4 pt-4">
              <a href="#download">
                <Button size="lg" className="bg-gradient-to-r from-purple-600 to-pink-600 hover:from-purple-700 hover:to-pink-700 text-white px-8 py-6 text-lg">
                  <Download className="mr-2 h-5 w-5" />
                  Download Now - FREE
                </Button>
              </a>
              <a href="#features">
                <Button size="lg" variant="outline" className="px-8 py-6 text-lg border-2 border-purple-600 text-purple-600 hover:bg-purple-50">
                  Explore Features
                </Button>
              </a>
            </div>
            <div className="flex items-center gap-6 pt-4">
              <div className="flex items-center gap-2">
                <div className="flex -space-x-2">
                  <div className="w-8 h-8 rounded-full bg-purple-400 border-2 border-white"></div>
                  <div className="w-8 h-8 rounded-full bg-pink-400 border-2 border-white"></div>
                  <div className="w-8 h-8 rounded-full bg-blue-400 border-2 border-white"></div>
                </div>
                <span className="text-sm text-gray-600">10,000+ Happy Users</span>
              </div>
              <div className="flex items-center gap-1">
                {[1, 2, 3, 4, 5].map((i) => (
                  <Star key={i} className="h-4 w-4 fill-yellow-400 text-yellow-400" />
                ))}
                <span className="text-sm text-gray-600 ml-1">4.9/5 Rating</span>
              </div>
            </div>
          </div>
          <div className="relative">
            <div className="bg-gradient-to-r from-purple-600 to-pink-600 rounded-2xl p-8 shadow-2xl transform hover:scale-105 transition">
              <div className="bg-white rounded-xl p-6 space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-sm font-semibold text-gray-500">Voice Filter Active</span>
                  <span className="bg-green-500 text-white px-3 py-1 rounded-full text-xs font-semibold">LIVE</span>
                </div>
                <div className="space-y-3">
                  <div className="flex items-center gap-3 p-3 bg-purple-50 rounded-lg">
                    <Mic className="h-8 w-8 text-purple-600" />
                    <div>
                      <div className="font-semibold text-gray-800">Alien Voice</div>
                      <div className="text-sm text-gray-500">Transform to extraterrestrial</div>
                    </div>
                  </div>
                  <div className="flex items-center gap-3 p-3 bg-pink-50 rounded-lg">
                    <Mic className="h-8 w-8 text-pink-600" />
                    <div>
                      <div className="font-semibold text-gray-800">Child Voice</div>
                      <div className="text-sm text-gray-500">Sound like a kid</div>
                    </div>
                  </div>
                  <div className="flex items-center gap-3 p-3 bg-blue-50 rounded-lg">
                    <Mic className="h-8 w-8 text-blue-600" />
                    <div>
                      <div className="font-semibold text-gray-800">Robot Voice</div>
                      <div className="text-sm text-gray-500">Robotic transformation</div>
                    </div>
                  </div>
                </div>
                <div className="pt-2 flex items-center justify-center gap-2 text-purple-600">
                  <div className="w-2 h-2 bg-purple-600 rounded-full animate-pulse"></div>
                  <span className="text-sm font-semibold">Call in Progress...</span>
                </div>
              </div>
            </div>
            <div className="absolute -bottom-6 -right-6 bg-yellow-400 text-yellow-900 px-6 py-3 rounded-full font-bold shadow-lg transform rotate-12">
              3 Days FREE Trial!
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section id="features" className="bg-white py-20">
        <div className="container mx-auto px-4">
          <div className="text-center mb-16">
            <h2 className="text-4xl md:text-5xl font-bold mb-4">
              Powerful Features for
              <span className="block bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent">
                Amazing Voice Transformation
              </span>
            </h2>
            <p className="text-xl text-gray-600 max-w-2xl mx-auto">
              Experience the most advanced voice changing technology with real-time processing and crystal clear quality
            </p>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-8">
            <Card className="border-2 hover:border-purple-600 transition hover:shadow-lg">
              <CardContent className="p-6 space-y-4">
                <div className="bg-purple-100 w-14 h-14 rounded-lg flex items-center justify-center">
                  <Mic className="h-7 w-7 text-purple-600" />
                </div>
                <h3 className="text-xl font-bold">Multiple Voice Effects</h3>
                <p className="text-gray-600">
                  Choose from premium voice filters including Alien, Child, Robot and more. New voices added regularly!
                </p>
              </CardContent>
            </Card>

            <Card className="border-2 hover:border-pink-600 transition hover:shadow-lg">
              <CardContent className="p-6 space-y-4">
                <div className="bg-pink-100 w-14 h-14 rounded-lg flex items-center justify-center">
                  <Zap className="h-7 w-7 text-pink-600" />
                </div>
                <h3 className="text-xl font-bold">Real-Time Processing</h3>
                <p className="text-gray-600">
                  Transform your voice instantly during live calls with zero delay. AI-powered for natural sound quality.
                </p>
              </CardContent>
            </Card>

            <Card className="border-2 hover:border-blue-600 transition hover:shadow-lg">
              <CardContent className="p-6 space-y-4">
                <div className="bg-blue-100 w-14 h-14 rounded-lg flex items-center justify-center">
                  <Shield className="h-7 w-7 text-blue-600" />
                </div>
                <h3 className="text-xl font-bold">100% Secure & Private</h3>
                <p className="text-gray-600">
                  Your calls are encrypted and private. We never record or store your conversations. Your privacy is guaranteed.
                </p>
              </CardContent>
            </Card>

            <Card className="border-2 hover:border-green-600 transition hover:shadow-lg">
              <CardContent className="p-6 space-y-4">
                <div className="bg-green-100 w-14 h-14 rounded-lg flex items-center justify-center">
                  <Smartphone className="h-7 w-7 text-green-600" />
                </div>
                <h3 className="text-xl font-bold">Easy to Use</h3>
                <p className="text-gray-600">
                  Simple interface designed for everyone. Just one tap to activate voice filters during your calls.
                </p>
              </CardContent>
            </Card>
          </div>
        </div>
      </section>

      {/* Pricing Section */}
      <section id="pricing" className="py-20 bg-gradient-to-b from-gray-50 to-white">
        <div className="container mx-auto px-4">
          <div className="text-center mb-16">
            <h2 className="text-4xl md:text-5xl font-bold mb-4">
              Simple, Transparent Pricing
            </h2>
            <p className="text-xl text-gray-600">
              Start free, upgrade anytime. No hidden fees.
            </p>
          </div>

          <div className="grid md:grid-cols-3 gap-8 max-w-5xl mx-auto">
            {/* Free Trial */}
            <Card className="border-2 hover:border-purple-600 transition hover:shadow-xl">
              <CardContent className="p-8 space-y-6">
                <div>
                  <h3 className="text-2xl font-bold mb-2">Free Trial</h3>
                  <div className="flex items-baseline gap-2">
                    <span className="text-4xl font-bold">৳0</span>
                    <span className="text-gray-600">/ 3 days</span>
                  </div>
                </div>
                <ul className="space-y-3">
                  <li className="flex items-center gap-2">
                    <Check className="h-5 w-5 text-green-600" />
                    <span>30 sec free calls</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <Check className="h-5 w-5 text-green-600" />
                    <span>1 premium voice for 3 days</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <Check className="h-5 w-5 text-green-600" />
                    <span>No credit card required</span>
                  </li>
                </ul>
                <Button className="w-full" variant="outline" size="lg">
                  Start Free Trial
                </Button>
              </CardContent>
            </Card>

            {/* Monthly Plan - Popular */}
            <Card className="border-4 border-purple-600 relative hover:shadow-2xl transition scale-105">
              <div className="absolute -top-4 left-1/2 -translate-x-1/2 bg-gradient-to-r from-purple-600 to-pink-600 text-white px-6 py-2 rounded-full font-bold text-sm">
                MOST POPULAR
              </div>
              <CardContent className="p-8 space-y-6">
                <div>
                  <h3 className="text-2xl font-bold mb-2">Monthly</h3>
                  <div className="flex items-baseline gap-2">
                    <span className="text-4xl font-bold">৳200</span>
                    <span className="text-gray-600">/ month</span>
                  </div>
                </div>
                <ul className="space-y-3">
                  <li className="flex items-center gap-2">
                    <Check className="h-5 w-5 text-green-600" />
                    <span>1 Premium voice filter for 1 month</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <Check className="h-5 w-5 text-green-600" />
                    <span>Priority support</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <Check className="h-5 w-5 text-green-600" />
                    <span>Cancel anytime</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <Check className="h-5 w-5 text-green-600" />
                    <span>New filters first</span>
                  </li>
                </ul>
                <Button className="w-full bg-gradient-to-r from-purple-600 to-pink-600 hover:from-purple-700 hover:to-pink-700 text-white" size="lg">
                  Get Started
                </Button>
              </CardContent>
            </Card>

            {/* Yearly Plan - Best Value */}
            <Card className="border-2 border-green-600 relative hover:shadow-xl transition">
              <div className="absolute -top-4 left-1/2 -translate-x-1/2 bg-green-600 text-white px-6 py-2 rounded-full font-bold text-sm">
                BEST VALUE
              </div>
              <CardContent className="p-8 space-y-6">
                <div>
                  <h3 className="text-2xl font-bold mb-2">Yearly</h3>
                  <div className="flex items-baseline gap-2">
                    <span className="text-4xl font-bold">৳1,000</span>
                    <span className="text-gray-600">/ year</span>
                  </div>
                  <div className="mt-2 text-sm text-green-600 font-semibold">
                    Save ৳200 (17% off)
                  </div>
                </div>
                <ul className="space-y-3">
                  <li className="flex items-center gap-2">
                    <Check className="h-5 w-5 text-green-600" />
                    <span className="font-semibold">Everything in Monthly</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <Check className="h-5 w-5 text-green-600" />
                    <span>17% discount</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <Check className="h-5 w-5 text-green-600" />
                    <span>Exclusive voice filters</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <Check className="h-5 w-5 text-green-600" />
                    <span>VIP support</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <Check className="h-5 w-5 text-green-600" />
                    <span>Beta features access</span>
                  </li>
                </ul>
                <Button className="w-full bg-green-600 hover:bg-green-700 text-white" size="lg">
                  Get Best Deal
                </Button>
              </CardContent>
            </Card>
          </div>
        </div>
      </section>

      {/* Download Section */}
      <section id="download" className="py-20 bg-gradient-to-r from-purple-600 to-pink-600 text-white">
        <div className="container mx-auto px-4">
          <div className="max-w-4xl mx-auto text-center space-y-8">
            <h2 className="text-4xl md:text-5xl font-bold">
              Ready to Transform Your Voice?
            </h2>
            <p className="text-xl opacity-90">
              Download Magic Call now and start your 3-day free trial. No credit card required!
            </p>

            <div className="flex flex-col sm:flex-row gap-4 justify-center pt-4">
              <Button
                size="lg"
                className="bg-white text-purple-600 hover:bg-gray-100 px-8 py-6 text-lg font-semibold"
              >
                <Download className="mr-2 h-6 w-6" />
                Download for Android
              </Button>
              <Button
                size="lg"
                variant="outline"
                className="border-2 border-white text-white hover:bg-white hover:text-purple-600 px-8 py-6 text-lg font-semibold"
              >
                <Download className="mr-2 h-6 w-6" />
                Download for iOS
              </Button>
            </div>

            <div className="grid md:grid-cols-3 gap-8 pt-12">
              <div className="space-y-2">
                <Users className="h-12 w-12 mx-auto" />
                <div className="text-3xl font-bold">10,000+</div>
                <div className="opacity-90">Active Users</div>
              </div>
              <div className="space-y-2">
                <Phone className="h-12 w-12 mx-auto" />
                <div className="text-3xl font-bold">100,000+</div>
                <div className="opacity-90">Calls Transformed</div>
              </div>
              <div className="space-y-2">
                <Star className="h-12 w-12 mx-auto" />
                <div className="text-3xl font-bold">4.9/5</div>
                <div className="opacity-90">User Rating</div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Newsletter Section */}
      <section className="py-20 bg-gray-50">
        <div className="container mx-auto px-4">
          <div className="max-w-2xl mx-auto text-center space-y-6">
            <h2 className="text-3xl md:text-4xl font-bold">
              Stay Updated with Magic Call
            </h2>
            <p className="text-lg text-gray-600">
              Get notified about new voice filters, features, and exclusive offers!
            </p>

            {subscribed ? (
              <div className="bg-green-50 border-2 border-green-600 rounded-lg p-6">
                <Check className="h-12 w-12 text-green-600 mx-auto mb-2" />
                <p className="text-green-800 font-semibold">
                  Thanks for subscribing! Check your email for updates.
                </p>
              </div>
            ) : (
              <form onSubmit={handleSubscribe} className="flex flex-col sm:flex-row gap-3 max-w-md mx-auto">
                <Input
                  type="email"
                  placeholder="Enter your email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  className="flex-1 px-4 py-6 text-lg"
                />
                <Button
                  type="submit"
                  size="lg"
                  className="bg-gradient-to-r from-purple-600 to-pink-600 hover:from-purple-700 hover:to-pink-700 text-white px-8 py-6 text-lg"
                >
                  Subscribe
                </Button>
              </form>
            )}
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-gray-900 text-gray-300 py-12">
        <div className="container mx-auto px-4">
          <div className="grid md:grid-cols-4 gap-8 mb-8">
            <div className="space-y-4">
              <div className="flex items-center gap-2">
                <div className="bg-gradient-to-r from-purple-600 to-pink-600 p-2 rounded-lg">
                  <Phone className="h-5 w-5 text-white" />
                </div>
                <span className="text-xl font-bold text-white">Magic Call</span>
              </div>
              <p className="text-sm">
                Transform your voice during calls with amazing AI-powered effects.
              </p>
            </div>

            <div>
              <h3 className="text-white font-semibold mb-4">Product</h3>
              <ul className="space-y-2 text-sm">
                <li><a href="#features" className="hover:text-purple-400 transition">Features</a></li>
                <li><a href="#pricing" className="hover:text-purple-400 transition">Pricing</a></li>
                <li><a href="#download" className="hover:text-purple-400 transition">Download</a></li>
              </ul>
            </div>

            <div>
              <h3 className="text-white font-semibold mb-4">Company</h3>
              <ul className="space-y-2 text-sm">
                <li><a href="#" className="hover:text-purple-400 transition">About Us</a></li>
                <li><a href="#" className="hover:text-purple-400 transition">Contact</a></li>
                <li><a href="#" className="hover:text-purple-400 transition">Support</a></li>
              </ul>
            </div>

            <div>
              <h3 className="text-white font-semibold mb-4">Legal</h3>
              <ul className="space-y-2 text-sm">
                <li><a href="#" className="hover:text-purple-400 transition">Privacy Policy</a></li>
                <li><a href="#" className="hover:text-purple-400 transition">Terms of Service</a></li>
                <li><a href="#" className="hover:text-purple-400 transition">Refund Policy</a></li>
              </ul>
            </div>
          </div>

          <div className="border-t border-gray-800 pt-8 text-center text-sm">
            <p>&copy; 2025 Magic Call. All rights reserved. Made with ❤️ for voice transformation enthusiasts.</p>
          </div>
        </div>
      </footer>
    </div>
  )
}
