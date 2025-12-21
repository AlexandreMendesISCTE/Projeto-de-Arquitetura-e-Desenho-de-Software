/**
 * Utility functions for geographic bounds calculations
 */

/**
 * Normalizes longitude to -180 to 180 range
 * @param lng Longitude in degrees
 * @returns Normalized longitude
 */
export const normalizeLongitude = (lng: number): number => {
  let normalized = lng
  while (normalized > 180) normalized -= 360
  while (normalized < -180) normalized += 360
  return normalized
}

/**
 * Validates and clamps latitude to valid range (-85 to 85)
 * @param lat Latitude in degrees
 * @returns Clamped latitude
 */
export const clampLatitude = (lat: number): number => {
  if (lat < -85) return -85
  if (lat > 85) return 85
  return lat
}

/**
 * Validates and clamps longitude to valid range (-180 to 180)
 * @param lng Longitude in degrees
 * @returns Clamped longitude
 */
export const clampLongitude = (lng: number): number => {
  if (lng < -180) return -180
  if (lng > 180) return 180
  return lng
}

/**
 * Calculates bounding box from center and zoom level
 * @param center Center coordinates [lat, lng]
 * @param zoom Zoom level
 * @param viewportWidth Viewport width in pixels (default: window.innerWidth or 800)
 * @param viewportHeight Viewport height in pixels (default: window.innerHeight or 600)
 * @returns Bounding box with minLat, maxLat, minLng, maxLng
 */
export const calculateBoundsFromCenter = (
  center: [number, number],
  zoom: number,
  viewportWidth: number = typeof window !== 'undefined' ? window.innerWidth : 800,
  viewportHeight: number = typeof window !== 'undefined' ? window.innerHeight : 600
): { minLat: number; maxLat: number; minLng: number; maxLng: number } => {
  const [lat, lng] = center
  const worldWidth = 360
  const pixelsPerDegree = (256 * Math.pow(2, zoom)) / worldWidth
  const latDelta = viewportHeight / pixelsPerDegree / 2
  const lngDelta = viewportWidth / pixelsPerDegree / 2

  return {
    minLat: lat - latDelta,
    maxLat: lat + latDelta,
    minLng: lng - lngDelta,
    maxLng: lng + lngDelta,
  }
}

/**
 * Checks if two bounding boxes overlap
 * @param bounds1 First bounding box
 * @param bounds2 Second bounding box
 * @returns True if boxes overlap
 */
export const boundsOverlap = (
  bounds1: { minLat: number; maxLat: number; minLng: number; maxLng: number },
  bounds2: { minLat: number; maxLat: number; minLng: number; maxLng: number }
): boolean => {
  return !(
    bounds1.maxLat < bounds2.minLat ||
    bounds1.minLat > bounds2.maxLat ||
    bounds1.maxLng < bounds2.minLng ||
    bounds1.minLng > bounds2.maxLng
  )
}

/**
 * Checks if a point is within bounds
 * @param point Point coordinates {lat, lng}
 * @param bounds Bounding box
 * @returns True if point is within bounds
 */
export const pointInBounds = (
  point: { lat: number; lng: number },
  bounds: { minLat: number; maxLat: number; minLng: number; maxLng: number }
): boolean => {
  return (
    point.lat >= bounds.minLat &&
    point.lat <= bounds.maxLat &&
    point.lng >= bounds.minLng &&
    point.lng <= bounds.maxLng
  )
}
