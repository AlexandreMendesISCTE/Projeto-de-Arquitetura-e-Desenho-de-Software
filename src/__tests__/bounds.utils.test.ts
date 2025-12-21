import { describe, it, expect } from 'vitest'
import {
  normalizeLongitude,
  clampLatitude,
  clampLongitude,
  calculateBoundsFromCenter,
  boundsOverlap,
  pointInBounds,
} from '../utils/bounds.utils'

describe('bounds.utils', () => {
  describe('normalizeLongitude', () => {
    it('normalizes longitude within valid range', () => {
      expect(normalizeLongitude(0)).toBe(0)
      expect(normalizeLongitude(180)).toBe(180)
      expect(normalizeLongitude(-180)).toBe(-180)
    })

    it('normalizes longitude greater than 180', () => {
      expect(normalizeLongitude(181)).toBe(-179)
      expect(normalizeLongitude(360)).toBe(0)
      expect(normalizeLongitude(540)).toBe(180)
    })

    it('normalizes longitude less than -180', () => {
      expect(normalizeLongitude(-181)).toBe(179)
      expect(normalizeLongitude(-360)).toBe(0)
      expect(normalizeLongitude(-540)).toBe(-180)
    })
  })

  describe('clampLatitude', () => {
    it('returns latitude within valid range', () => {
      expect(clampLatitude(0)).toBe(0)
      expect(clampLatitude(38.7223)).toBe(38.7223)
      expect(clampLatitude(-45)).toBe(-45)
    })

    it('clamps latitude greater than 85', () => {
      expect(clampLatitude(86)).toBe(85)
      expect(clampLatitude(100)).toBe(85)
    })

    it('clamps latitude less than -85', () => {
      expect(clampLatitude(-86)).toBe(-85)
      expect(clampLatitude(-100)).toBe(-85)
    })
  })

  describe('clampLongitude', () => {
    it('returns longitude within valid range', () => {
      expect(clampLongitude(0)).toBe(0)
      expect(clampLongitude(-9.1393)).toBe(-9.1393)
      expect(clampLongitude(180)).toBe(180)
      expect(clampLongitude(-180)).toBe(-180)
    })

    it('clamps longitude greater than 180', () => {
      expect(clampLongitude(181)).toBe(180)
      expect(clampLongitude(200)).toBe(180)
    })

    it('clamps longitude less than -180', () => {
      expect(clampLongitude(-181)).toBe(-180)
      expect(clampLongitude(-200)).toBe(-180)
    })
  })

  describe('calculateBoundsFromCenter', () => {
    it('calculates bounds from center and zoom', () => {
      const center: [number, number] = [38.7223, -9.1393]
      const zoom = 13
      const bounds = calculateBoundsFromCenter(center, zoom, 800, 600)

      expect(bounds.minLat).toBeLessThan(center[0])
      expect(bounds.maxLat).toBeGreaterThan(center[0])
      expect(bounds.minLng).toBeLessThan(center[1])
      expect(bounds.maxLng).toBeGreaterThan(center[1])
    })

    it('calculates smaller bounds for higher zoom levels', () => {
      const center: [number, number] = [38.7223, -9.1393]
      const boundsLowZoom = calculateBoundsFromCenter(center, 10, 800, 600)
      const boundsHighZoom = calculateBoundsFromCenter(center, 15, 800, 600)

      const lowZoomArea =
        (boundsLowZoom.maxLat - boundsLowZoom.minLat) *
        (boundsLowZoom.maxLng - boundsLowZoom.minLng)
      const highZoomArea =
        (boundsHighZoom.maxLat - boundsHighZoom.minLat) *
        (boundsHighZoom.maxLng - boundsHighZoom.minLng)

      expect(highZoomArea).toBeLessThan(lowZoomArea)
    })

    it('uses viewport dimensions correctly', () => {
      const center: [number, number] = [38.7223, -9.1393]
      const zoom = 13
      const boundsSmall = calculateBoundsFromCenter(center, zoom, 400, 300)
      const boundsLarge = calculateBoundsFromCenter(center, zoom, 1600, 1200)

      const smallArea =
        (boundsSmall.maxLat - boundsSmall.minLat) * (boundsSmall.maxLng - boundsSmall.minLng)
      const largeArea =
        (boundsLarge.maxLat - boundsLarge.minLat) * (boundsLarge.maxLng - boundsLarge.minLng)

      expect(largeArea).toBeGreaterThan(smallArea)
    })
  })

  describe('boundsOverlap', () => {
    it('returns true when bounds overlap', () => {
      const bounds1 = { minLat: 38.0, maxLat: 39.0, minLng: -9.5, maxLng: -8.5 }
      const bounds2 = { minLat: 38.5, maxLat: 39.5, minLng: -9.0, maxLng: -8.0 }

      expect(boundsOverlap(bounds1, bounds2)).toBe(true)
    })

    it('returns false when bounds do not overlap', () => {
      const bounds1 = { minLat: 38.0, maxLat: 39.0, minLng: -9.5, maxLng: -8.5 }
      const bounds2 = { minLat: 40.0, maxLat: 41.0, minLng: -8.0, maxLng: -7.0 }

      expect(boundsOverlap(bounds1, bounds2)).toBe(false)
    })

    it('returns true when bounds are adjacent', () => {
      const bounds1 = { minLat: 38.0, maxLat: 39.0, minLng: -9.5, maxLng: -8.5 }
      const bounds2 = { minLat: 38.0, maxLat: 39.0, minLng: -8.5, maxLng: -7.5 }

      expect(boundsOverlap(bounds1, bounds2)).toBe(true) // Touching at edge counts as overlap
    })

    it('returns true when one bounds contains the other', () => {
      const bounds1 = { minLat: 38.0, maxLat: 40.0, minLng: -10.0, maxLng: -8.0 }
      const bounds2 = { minLat: 38.5, maxLat: 39.5, minLng: -9.5, maxLng: -8.5 }

      expect(boundsOverlap(bounds1, bounds2)).toBe(true)
    })
  })

  describe('pointInBounds', () => {
    it('returns true when point is within bounds', () => {
      const point = { lat: 38.7223, lng: -9.1393 }
      const bounds = { minLat: 38.0, maxLat: 39.0, minLng: -9.5, maxLng: -8.5 }

      expect(pointInBounds(point, bounds)).toBe(true)
    })

    it('returns false when point is outside bounds', () => {
      const point = { lat: 40.0, lng: -8.0 }
      const bounds = { minLat: 38.0, maxLat: 39.0, minLng: -9.5, maxLng: -8.5 }

      expect(pointInBounds(point, bounds)).toBe(false)
    })

    it('returns true when point is on boundary', () => {
      const point = { lat: 38.0, lng: -9.5 }
      const bounds = { minLat: 38.0, maxLat: 39.0, minLng: -9.5, maxLng: -8.5 }

      expect(pointInBounds(point, bounds)).toBe(true)
    })

    it('returns false when point is outside latitude range', () => {
      const point = { lat: 40.0, lng: -9.0 }
      const bounds = { minLat: 38.0, maxLat: 39.0, minLng: -9.5, maxLng: -8.5 }

      expect(pointInBounds(point, bounds)).toBe(false)
    })

    it('returns false when point is outside longitude range', () => {
      const point = { lat: 38.5, lng: -10.0 }
      const bounds = { minLat: 38.0, maxLat: 39.0, minLng: -9.5, maxLng: -8.5 }

      expect(pointInBounds(point, bounds)).toBe(false)
    })
  })
})
