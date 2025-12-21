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
    default: "Magic Call - Transform Your Voice in Real-Time During Calls | AI Voice Changer",
    template: "%s | Magic Call"
  },
  description: "Change your voice during calls with Magic Call - the ultimate AI-powered voice changer app. Try premium voice filters like Alien, Child, Robot & more. 3 days free trial. Download for Android & iOS!",
  keywords: [
    "voice changer",
    "magic call",
    "voice changer app",
    "real-time voice changer",
    "AI voice transformer",
    "voice effects",
    "call voice modifier",
    "prank call app",
    "voice filter app",
    "alien voice",
    "child voice",
    "robot voice",
    "voice changer for calls",
    "live voice changer",
    "free voice changer",
    "voice changer Bangladesh",
    "voice modulator",
    "call effects app"
  ],
  authors: [{ name: "Magic Call Team" }],
  creator: "Magic Call",
  publisher: "Magic Call",
  robots: {
    index: true, // Public landing page should be indexed
    follow: true,
    googleBot: {
      index: true,
      follow: true,
      "max-video-preview": -1,
      "max-image-preview": "large",
      "max-snippet": -1,
    },
  },
  openGraph: {
    type: "website",
    locale: "en_US",
    url: "http://localhost:3000",
    title: "Magic Call - Transform Your Voice in Real-Time | AI Voice Changer App",
    description: "Download Magic Call and change your voice during calls with amazing AI effects. Try Alien, Child, Robot voices & more. 3 days FREE trial! Available for Android & iOS.",
    siteName: "Magic Call",
    images: [
      {
        url: "/og-image.png", // TODO: Add actual OG image
        width: 1200,
        height: 630,
        alt: "Magic Call - AI Voice Changer App",
      },
    ],
  },
  twitter: {
    card: "summary_large_image",
    title: "Magic Call - Transform Your Voice in Real-Time | AI Voice Changer",
    description: "Download Magic Call and change your voice during calls with amazing AI effects. 3 days FREE trial! Available for Android & iOS.",
    images: ["/og-image.png"], // TODO: Add actual Twitter card image
    creator: "@magiccall",
  },
  viewport: {
    width: "device-width",
    initialScale: 1,
    maximumScale: 5,
  },
  icons: {
    icon: "/favicon.ico",
    apple: "/apple-touch-icon.png",
  },
  manifest: "/manifest.json",
  alternates: {
    canonical: "http://localhost:3000",
  },
  category: "Entertainment",
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
        suppressHydrationWarning
      >
        {children}
      </body>
    </html>
  );
}
