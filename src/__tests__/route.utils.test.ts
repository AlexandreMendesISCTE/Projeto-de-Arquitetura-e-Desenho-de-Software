import { describe, it, expect } from 'vitest'
import {
  calculateDuration,
  adjustDuration,
  formatDuration,
  formatDistance,
} from '../utils/route.utils'
import { TransportMode } from '../types/route.types'

describe('route.utils', () => {
  describe('calculateDuration', () => {
    it('returns buffered walking time', () => {
      const seconds = calculateDuration(1000, TransportMode.WALKING) // ~1km
      // 1000 / 1.4 = 714s, with 10% buffer ≈ 785s
      expect(seconds).toBeGreaterThan(750)
      expect(seconds).toBeLessThan(820)
    })

    it('returns buffered driving time', () => {
      const seconds = calculateDuration(20000, TransportMode.DRIVING) // 20km
      // 20000 / 13.9 ≈ 1439s, with 15% buffer ≈ 1655s
      expect(seconds).toBeGreaterThan(1550)
      expect(seconds).toBeLessThan(1750)
    })
  })

  describe('adjustDuration', () => {
    it('falls back to calculated duration when API duration is unrealistic', () => {
      const adjusted = adjustDuration(
        100, // unrealistically low for 10km driving
        10000,
        TransportMode.DRIVING
      )
      const expected = calculateDuration(10000, TransportMode.DRIVING)
      expect(adjusted).toBeCloseTo(expected, 0)
    })

    it('clamps API duration inside reasonable bounds', () => {
      const calculated = calculateDuration(5000, TransportMode.WALKING)
      const tooHigh = calculated * 10
      const adjusted = adjustDuration(tooHigh, 5000, TransportMode.WALKING)
      expect(adjusted).toBeLessThan(calculated * 2.1)
      expect(adjusted).toBeGreaterThan(calculated * 0.69)
    })
  })

  describe('formatDuration', () => {
    it('formats minutes-only durations', () => {
      expect(formatDuration(90)).toBe('1min')
      expect(formatDuration(540)).toBe('9min')
    })

    it('formats hour+minute durations', () => {
      expect(formatDuration(3660)).toBe('1h 1min')
      expect(formatDuration(7200)).toBe('2h 0min')
    })
  })

  describe('formatDistance', () => {
    it('formats meters under 1km', () => {
      expect(formatDistance(750)).toBe('750 m')
    })

    it('formats kilometers with two decimals', () => {
      expect(formatDistance(1500)).toBe('1.50 km')
      expect(formatDistance(12345)).toBe('12.35 km')
    })
  })
})

