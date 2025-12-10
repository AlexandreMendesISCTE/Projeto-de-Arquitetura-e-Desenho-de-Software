// Fetch nearby POIs with debounce, rate limiting, and viewport-based caching
import { useQuery } from '@tanstack/react-query'
import { useEffect, useState, useRef } from 'react'
import poiService from '../services/api/poi.service'
import { usePOIStore } from '../store/poi.store'

// Minimum time between requests (5 seconds) to prevent rate limiting
const MIN_REQUEST_INTERVAL = 5000
// Debounce delay for map movement (1 second)
const DEBOUNCE_DELAY = 1000
// Minimum distance change to trigger new request (in degrees)
const MIN_DISTANCE_CHANGE = 0.01

export const usePOIs = (center: [number, number], zoom: number) => {
  const { enabled, getPOIsInBounds, isAreaLoaded, addPOIs, lastRequestTime } = usePOIStore()
  const [debouncedCenter, setDebouncedCenter] = useState(center)
  const [debouncedZoom, setDebouncedZoom] = useState(zoom)
  const debounceTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null)
  const lastRequestedBoundsRef = useRef<{
    minLat: number
    maxLat: number
    minLng: number
    maxLng: number
  } | null>(null)

  // Debounce center and zoom changes
  useEffect(() => {
    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current)
    }

    debounceTimerRef.current = setTimeout(() => {
      setDebouncedCenter(center)
      setDebouncedZoom(zoom)
    }, DEBOUNCE_DELAY)

    return () => {
      if (debounceTimerRef.current) {
        clearTimeout(debounceTimerRef.current)
      }
    }
  }, [center, zoom])

  // Calculate bounding box
  const calculateBounds = (center: [number, number], zoom: number) => {
    const [lat, lng] = center
    const worldWidth = 360
    const pixelsPerDegree = (256 * Math.pow(2, zoom)) / worldWidth
    const viewportWidthPx = typeof window !== 'undefined' ? window.innerWidth : 800
    const viewportHeightPx = typeof window !== 'undefined' ? window.innerHeight : 600

    const latDelta = viewportHeightPx / pixelsPerDegree / 2
    const lngDelta = viewportWidthPx / pixelsPerDegree / 2

    return {
      minLat: lat - latDelta,
      maxLat: lat + latDelta,
      minLng: lng - lngDelta,
      maxLng: lng + lngDelta,
    }
  }

  // Check if bounds have changed significantly
  const hasBoundsChanged = (
    bounds1: { minLat: number; maxLat: number; minLng: number; maxLng: number } | null,
    bounds2: { minLat: number; maxLat: number; minLng: number; maxLng: number }
  ) => {
    if (!bounds1) return true

    const latChange =
      Math.abs(bounds1.minLat - bounds2.minLat) + Math.abs(bounds1.maxLat - bounds2.maxLat)
    const lngChange =
      Math.abs(bounds1.minLng - bounds2.minLng) + Math.abs(bounds1.maxLng - bounds2.maxLng)

    return latChange > MIN_DISTANCE_CHANGE || lngChange > MIN_DISTANCE_CHANGE
  }

  return useQuery({
    queryKey: ['pois', debouncedCenter, debouncedZoom, enabled],
    queryFn: async () => {
      if (!enabled) {
        return []
      }

      const bounds = calculateBounds(debouncedCenter, debouncedZoom)

      // Check if we already have POIs for this area
      if (isAreaLoaded(bounds)) {
        // Return cached POIs
        const cachedPOIs = getPOIsInBounds(bounds)
        const [lat, lng] = debouncedCenter
        return cachedPOIs
          .map((poi) => {
            const latDiff = poi.lat - lat
            const lngDiff = poi.lng - lng
            const dist = Math.sqrt(latDiff * latDiff + lngDiff * lngDiff)
            return { poi, dist }
          })
          .sort((a, b) => a.dist - b.dist)
          .slice(0, 30)
          .map(({ poi }) => poi)
      }

      // Check rate limiting - don't request if too soon since last request
      const timeSinceLastRequest = Date.now() - lastRequestTime
      if (timeSinceLastRequest < MIN_REQUEST_INTERVAL) {
        // Return cached POIs if available, even if not for exact area
        const cachedPOIs = getPOIsInBounds(bounds)
        if (cachedPOIs.length > 0) {
          const [lat, lng] = debouncedCenter
          return cachedPOIs
            .map((poi) => {
              const latDiff = poi.lat - lat
              const lngDiff = poi.lng - lng
              const dist = Math.sqrt(latDiff * latDiff + lngDiff * lngDiff)
              return { poi, dist }
            })
            .sort((a, b) => a.dist - b.dist)
            .slice(0, 30)
            .map(({ poi }) => poi)
        }
        return []
      }

      // Check if bounds have changed significantly from last request
      if (!hasBoundsChanged(lastRequestedBoundsRef.current, bounds)) {
        // Return cached POIs
        const cachedPOIs = getPOIsInBounds(bounds)
        const [lat, lng] = debouncedCenter
        return cachedPOIs
          .map((poi) => {
            const latDiff = poi.lat - lat
            const lngDiff = poi.lng - lng
            const dist = Math.sqrt(latDiff * latDiff + lngDiff * lngDiff)
            return { poi, dist }
          })
          .sort((a, b) => a.dist - b.dist)
          .slice(0, 30)
          .map(({ poi }) => poi)
      }

      // Request new POIs
      try {
        const allPOIs = await poiService.getPOIsInBounds(
          bounds.minLat,
          bounds.minLng,
          bounds.maxLat,
          bounds.maxLng
        )

        // Only cache if we got results (empty array might indicate timeout/error)
        if (allPOIs.length > 0) {
          addPOIs(allPOIs, bounds)
          lastRequestedBoundsRef.current = bounds
        }

        // Sort by distance from center and limit to 30 closest
        const [lat, lng] = debouncedCenter
        const sortedPOIs = allPOIs
          .map((poi) => {
            const latDiff = poi.lat - lat
            const lngDiff = poi.lng - lng
            const dist = Math.sqrt(latDiff * latDiff + lngDiff * lngDiff)
            return { poi, dist }
          })
          .sort((a, b) => a.dist - b.dist)
          .slice(0, 30)
          .map(({ poi }) => poi)

        // If we got POIs, return them; otherwise try cached
        if (sortedPOIs.length > 0) {
          return sortedPOIs
        }

        // Fall back to cached POIs if request returned empty (likely timeout)
        const cachedPOIs = getPOIsInBounds(bounds)
        if (cachedPOIs.length > 0) {
          const sortedCachedPOIs = cachedPOIs
            .map((poi) => {
              const latDiff = poi.lat - lat
              const lngDiff = poi.lng - lng
              const dist = Math.sqrt(latDiff * latDiff + lngDiff * lngDiff)
              return { poi, dist }
            })
            .sort((a, b) => a.dist - b.dist)
            .slice(0, 30)
            .map(({ poi }) => poi)
          return sortedCachedPOIs
        }

        return []
      } catch (error) {
        // Silently handle errors - use cached POIs instead
        const cachedPOIs = getPOIsInBounds(bounds)
        if (cachedPOIs.length > 0) {
          const [lat, lng] = debouncedCenter
          return cachedPOIs
            .map((poi) => {
              const latDiff = poi.lat - lat
              const lngDiff = poi.lng - lng
              const dist = Math.sqrt(latDiff * latDiff + lngDiff * lngDiff)
              return { poi, dist }
            })
            .sort((a, b) => a.dist - b.dist)
            .slice(0, 30)
            .map(({ poi }) => poi)
        }
        return []
      }
    },
    enabled: enabled,
    staleTime: 5 * 60 * 1000, // 5 minutes - longer since we cache
    gcTime: 10 * 60 * 1000, // 10 minutes garbage collection
  })
}
