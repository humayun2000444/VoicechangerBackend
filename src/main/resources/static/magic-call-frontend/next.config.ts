import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Suppress hydration warnings caused by browser extensions in development
  // These warnings occur when extensions like Grammarly, password managers,
  // or security tools modify the DOM before React hydrates
  reactStrictMode: true,
};

export default nextConfig;
