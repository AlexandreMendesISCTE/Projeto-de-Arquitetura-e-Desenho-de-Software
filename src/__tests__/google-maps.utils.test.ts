import { describe, it, expect, beforeEach, vi } from 'vitest'
import { openRouteInGoogleMaps } from '../utils/google-maps.utils'
import { TransportMode } from '../types/route.types'

describe('google-maps.utils', () => {
  const origin = { lat: 38.7223, lng: -9.1393 }
  const destination = { lat: 41.1579, lng: -8.6291 }

  beforeEach(() => {
    vi.restoreAllMocks()
    // Minimal window mock for the util
    const mockOpen = vi.fn()
    const holder = globalThis as unknown as { window?: { open: ReturnType<typeof vi.fn> } }
    holder.window = { open: mockOpen }
  })

  it('opens Google Maps with correct params (driving, no waypoints)', () => {
    openRouteInGoogleMaps(origin, destination, TransportMode.DRIVING, [])

    const openMock = (globalThis as unknown as { window: { open: ReturnType<typeof vi.fn> } })
      .window.open
    expect(openMock).toHaveBeenCalledTimes(1)
    const url = openMock.mock.calls[0][0] as string
    expect(url).toContain('https://www.google.com/maps/dir/?api=1')
    expect(url).toContain(`origin=${origin.lat},${origin.lng}`)
    expect(url).toContain(`destination=${destination.lat},${destination.lng}`)
    expect(url).toContain('travelmode=driving')
    expect(url).not.toContain('waypoints=')
  })

  it('includes waypoints when provided', () => {
    const waypoints = [
      { lat: 39, lng: -9 },
      { lat: 40, lng: -8.8 },
    ]

    openRouteInGoogleMaps(origin, destination, TransportMode.WALKING, waypoints)

    const openMock = (globalThis as unknown as { window: { open: ReturnType<typeof vi.fn> } })
      .window.open
    const url = openMock.mock.calls[0][0] as string
    expect(url).toContain('travelmode=walking')
    expect(url).toContain('waypoints=')
    expect(url).toContain(encodeURIComponent(`${waypoints[0].lat},${waypoints[0].lng}`))
    expect(url).toContain(encodeURIComponent(`${waypoints[1].lat},${waypoints[1].lng}`))
  })
})
