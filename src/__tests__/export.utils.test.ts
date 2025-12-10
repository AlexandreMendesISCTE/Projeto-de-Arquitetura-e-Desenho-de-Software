import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { exportRouteToGPX, exportRouteToJSON } from '../utils/export.utils'
import { TransportMode } from '../types/route.types'

const route = {
  waypoints: [
    { lat: 38.7223, lng: -9.1393 },
    { lat: 39, lng: -9 },
    { lat: 41.1579, lng: -8.6291 },
  ],
  totalDistance: 313000,
  totalDuration: 12000,
  transportMode: TransportMode.DRIVING,
  geometry: {
    type: 'LineString' as const,
    coordinates: [
      [-9.1393, 38.7223],
      [-9, 39],
      [-8.6291, 41.1579],
    ],
  },
}

const origin = { lat: 38.7223, lng: -9.1393 }
const destination = { lat: 41.1579, lng: -8.6291 }

describe('export.utils', () => {
  const appendChild = vi.fn()
  const removeChild = vi.fn()
  const click = vi.fn()
  const link = { href: '', download: '', click }
  const createObjectURL = vi.fn(() => 'blob://test')
  const revokeObjectURL = vi.fn()
  const originalDocument = globalThis.document
  const originalURL = globalThis.URL

  type MockLink = { href: string; download: string; click: ReturnType<typeof vi.fn> }
  type MockDocument = {
    createElement: (tag: string) => MockLink
    body: { appendChild: (link: MockLink) => void; removeChild: (link: MockLink) => void }
  }
  type MockURL = { createObjectURL: (blob: Blob) => string; revokeObjectURL: (url: string) => void }

  beforeEach(() => {
    vi.spyOn(Date, 'now').mockReturnValue(1700000000000)
    const mockDoc: MockDocument = {
      createElement: vi.fn(() => link),
      body: { appendChild, removeChild },
    }
    const mockURL: MockURL = {
      createObjectURL,
      revokeObjectURL,
    }
    ;(globalThis as unknown as { document: Document }).document = mockDoc as unknown as Document
    ;(globalThis as unknown as { URL: MockURL }).URL = mockURL
  })

  afterEach(() => {
    vi.restoreAllMocks()
    ;(globalThis as unknown as { document: Document }).document = originalDocument
    ;(globalThis as unknown as { URL: MockURL }).URL = originalURL as unknown as MockURL
  })

  it('exports route to JSON with deterministic filename and content', async () => {
    await exportRouteToJSON(route, origin, destination)

    expect(appendChild).toHaveBeenCalledWith(link)
    expect(click).toHaveBeenCalledTimes(1)
    expect(removeChild).toHaveBeenCalledWith(link)
    expect(createObjectURL).toHaveBeenCalledTimes(1)

    expect(link.download).toBe('route-driving-1700000000000.json')

    const calls = createObjectURL.mock.calls as unknown[][]
    expect(calls.length).toBeGreaterThan(0)
    const blob = calls[0][0] as unknown
    expect(blob).toBeInstanceOf(Blob)
    const text = await (blob as Blob).text()
    expect(text).toContain('"origin"')
    expect(text).toContain('"destination"')
    expect(text).toContain('"transportMode"')
  })

  it('exports route to GPX with deterministic filename and GPX structure', async () => {
    await exportRouteToGPX(route, origin, destination)

    expect(link.download).toBe('route-driving-1700000000000.gpx')
    expect(createObjectURL).toHaveBeenCalledTimes(1)

    const calls = createObjectURL.mock.calls as unknown[][]
    expect(calls.length).toBeGreaterThan(0)
    const blob = calls[0][0] as unknown
    expect(blob).toBeInstanceOf(Blob)
    const text = await (blob as Blob).text()
    expect(text).toContain('<?xml version="1.0" encoding="UTF-8"?>')
    expect(text).toContain('<gpx')
    expect(text).toContain('<rtept lat="38.7223" lon="-9.1393">')
    expect(text).toContain('<rtept lat="41.1579" lon="-8.6291">')
  })
})
