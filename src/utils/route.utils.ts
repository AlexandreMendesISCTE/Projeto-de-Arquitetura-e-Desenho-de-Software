import { TransportMode } from '../types/route.types'

/**
 * Average speeds for different transport modes (in meters per second)
 * These are realistic averages for route planning:
 * - Walking: ~1.4 m/s (5 km/h)
 * - Cycling: ~4.2 m/s (15 km/h)
 * - Driving: ~13.9 m/s (50 km/h) - varies by road type, but average for urban routes
 */
const AVERAGE_SPEEDS: Record<TransportMode, number> = {
  [TransportMode.WALKING]: 1.4, // 5 km/h
  [TransportMode.BICYCLING]: 4.2, // 15 km/h
  [TransportMode.DRIVING]: 13.9, // 50 km/h (urban average)
}

/**
 * Calculates estimated duration based on distance and transport mode
 * @param distance Distance in meters
 * @param mode Transport mode
 * @returns Duration in seconds
 */
export const calculateDuration = (distance: number, mode: TransportMode): number => {
  const speed = AVERAGE_SPEEDS[mode]
  if (!speed || distance <= 0) {
    return 0
  }

  // Add a small buffer for stops, traffic lights, etc.
  const baseTime = distance / speed
  const bufferMultiplier = mode === TransportMode.DRIVING ? 1.15 : 1.1 // 15% buffer for driving, 10% for others

  return Math.round(baseTime * bufferMultiplier)
}

/**
 * Adjusts API duration if it seems unrealistic for the transport mode
 * Uses frontend calculation if API duration is suspiciously similar across modes
 * @param apiDuration Duration from API in seconds
 * @param distance Distance in meters
 * @param mode Transport mode
 * @returns Adjusted duration in seconds
 */
export const adjustDuration = (
  apiDuration: number,
  distance: number,
  mode: TransportMode
): number => {
  const calculatedDuration = calculateDuration(distance, mode)

  // If API duration is very close to calculated duration for a different mode,
  // it might be incorrect. Use calculated duration as fallback.
  const durationDifference = Math.abs(apiDuration - calculatedDuration)
  const percentageDifference = (durationDifference / calculatedDuration) * 100

  // If API duration differs by more than 50% from expected, use calculated
  // This handles cases where API returns same duration for all modes
  if (percentageDifference > 50) {
    return calculatedDuration
  }

  // Otherwise, use API duration but ensure it's reasonable
  // Minimum duration based on distance and mode
  const minDuration = calculatedDuration * 0.7
  const maxDuration = calculatedDuration * 2.0

  return Math.max(minDuration, Math.min(maxDuration, apiDuration))
}

/**
 * Formats duration in seconds to human-readable string
 * @param seconds Duration in seconds
 * @returns Formatted string (e.g., "1h 30min" or "45min")
 */
export const formatDuration = (seconds: number): string => {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)

  if (hours > 0) {
    return `${hours}h ${minutes}min`
  }
  return `${minutes}min`
}

/**
 * Formats distance in meters to human-readable string
 * @param meters Distance in meters
 * @returns Formatted string (e.g., "1.5 km" or "500 m")
 */
export const formatDistance = (meters: number): string => {
  if (meters < 1000) {
    return `${Math.round(meters)} m`
  }
  return `${(meters / 1000).toFixed(2)} km`
}
