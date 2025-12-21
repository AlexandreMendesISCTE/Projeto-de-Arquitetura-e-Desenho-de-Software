/**
 * Performance tests for critical operations
 * These tests validate that operations complete within acceptable time limits
 */
import { describe, it, expect } from 'vitest'
import {
  calculateDuration,
  adjustDuration,
  formatDuration,
  formatDistance,
} from '../utils/route.utils'
import { TransportMode } from '../types/route.types'

describe('Performance Tests', () => {
  describe('Route Utilities Performance', () => {
    it('calculates duration quickly (< 1ms)', () => {
      const start = performance.now()
      calculateDuration(10000, TransportMode.DRIVING)
      const end = performance.now()
      const duration = end - start

      expect(duration).toBeLessThan(1) // Should complete in less than 1ms
    })

    it('adjusts duration quickly (< 1ms)', () => {
      const start = performance.now()
      adjustDuration(10000, 50000, TransportMode.DRIVING)
      const end = performance.now()
      const duration = end - start

      expect(duration).toBeLessThan(1)
    })

    it('formats duration quickly (< 1ms)', () => {
      const start = performance.now()
      formatDuration(3661) // 1 hour, 1 minute, 1 second
      const end = performance.now()
      const duration = end - start

      expect(duration).toBeLessThan(1)
    })

    it('formats distance quickly (< 1ms)', () => {
      const start = performance.now()
      formatDistance(1234) // 1.234 km
      const end = performance.now()
      const duration = end - start

      expect(duration).toBeLessThan(1)
    })

    it('handles large distance values efficiently', () => {
      const start = performance.now()
      formatDistance(1000000) // 1000 km
      const end = performance.now()
      const duration = end - start

      expect(duration).toBeLessThan(1)
    })

    it('handles large duration values efficiently', () => {
      const start = performance.now()
      formatDuration(86400) // 24 hours
      const end = performance.now()
      const duration = end - start

      expect(duration).toBeLessThan(1)
    })
  })

  describe('Batch Operations Performance', () => {
    it('processes multiple duration calculations efficiently', () => {
      const distances = Array.from({ length: 100 }, (_, i) => (i + 1) * 1000)
      const modes = [
        TransportMode.DRIVING,
        TransportMode.BICYCLING,
        TransportMode.WALKING,
        TransportMode.TRANSIT,
      ]

      const start = performance.now()
      distances.forEach((distance) => {
        modes.forEach((mode) => {
          calculateDuration(distance, mode)
        })
      })
      const end = performance.now()
      const duration = end - start

      // 100 distances * 4 modes = 400 calculations should complete in < 10ms
      expect(duration).toBeLessThan(10)
    })

    it('processes multiple format operations efficiently', () => {
      const durations = Array.from({ length: 1000 }, (_, i) => i * 60) // 0 to 999 minutes
      const distances = Array.from({ length: 1000 }, (_, i) => i * 100) // 0 to 99.9 km

      const start = performance.now()
      durations.forEach((duration) => formatDuration(duration))
      distances.forEach((distance) => formatDistance(distance))
      const end = performance.now()
      const duration = end - start

      // 2000 format operations should complete in < 20ms
      expect(duration).toBeLessThan(20)
    })
  })
})
