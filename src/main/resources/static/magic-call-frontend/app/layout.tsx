import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: {
    default: "Magic Call - Voice Changer Admin Panel",
    template: "%s | Magic Call Admin"
  },
  description: "Complete management system for Magic Call Voice Changer platform. Manage packages, users, voice types, and purchase transactions with our comprehensive admin panel.",
  keywords: ["voice changer", "admin panel", "magic call", "voice types", "package management", "FreeSWITCH", "VoIP", "call management"],
  authors: [{ name: "Magic Call Team" }],
  creator: "Magic Call",
  publisher: "Magic Call",
  robots: {
    index: false, // Admin panel should not be indexed
    follow: false,
    googleBot: {
      index: false,
      follow: false,
    },
  },
  openGraph: {
    type: "website",
    locale: "en_US",
    url: "http://localhost:3000",
    title: "Magic Call - Voice Changer Admin Panel",
    description: "Complete management system for Magic Call Voice Changer platform",
    siteName: "Magic Call Admin",
  },
  twitter: {
    card: "summary_large_image",
    title: "Magic Call - Voice Changer Admin Panel",
    description: "Complete management system for Magic Call Voice Changer platform",
  },
  viewport: {
    width: "device-width",
    initialScale: 1,
    maximumScale: 5,
  },
  icons: {
    icon: "/favicon.ico",
  },
  manifest: "/manifest.json",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <head>
        <meta name="theme-color" content="#2563eb" />
        <meta name="apple-mobile-web-app-capable" content="yes" />
        <meta name="apple-mobile-web-app-status-bar-style" content="black-translucent" />
      </head>
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased`}
      >
        {children}
      </body>
    </html>
  );
}
