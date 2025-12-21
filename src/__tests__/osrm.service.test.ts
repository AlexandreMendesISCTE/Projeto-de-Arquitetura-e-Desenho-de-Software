import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { TransportMode } from '../types/route.types'

// Use vi.hoisted() to create mocks that can be accessed in both mock factory and tests
const { mockGet, mockIsAxiosError } = vi.hoisted(() => {
  return {
    mockGet: vi.fn(),
    mockIsAxiosError: vi.fn(),
  }
})

// Mock axios before importing the service
vi.mock('axios', () => {
  const mockAxiosInstance = {
    get: mockGet,
  }
  return {
    default: {
      create: vi.fn(() => mockAxiosInstance),
      isAxiosError: mockIsAxiosError,
    },
  }
})

// Import service after mocking axios
import osrmService from '../services/api/osrm.service'

describe('osrm.service', () => {
  const origin = { lat: 38.7223, lng: -9.1393, name: 'Lisboa' }
  const destination = { lat: 41.1579, lng: -8.6291, name: 'Porto' }

  beforeEach(() => {
    vi.clearAllMocks()
    vi.spyOn(console, 'error').mockImplementation(() => {})
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('calculateRoute', () => {
    it('calculates route successfully for driving mode', async () => {
      const mockResponse = {
        data: {
          code: 'Ok',
          routes: [
            {
              distance: 313000, // meters
              duration: 12000, // seconds
              geometry: {
                coordinates: [
                  [-9.1393, 38.7223],
                  [-8.6291, 41.1579],
                ],
              },
              legs: [
                {
                  steps: [
                    {
                      maneuver: {
                        instruction: 'Head north',
                      },
                    },
                  ],
                },
              ],
            },
          ],
          waypoints: [{ location: [-9.1393, 38.7223] }, { location: [-8.6291, 41.1579] }],
        },
      }

      mockGet.mockResolvedValueOnce(mockResponse)

      const result = await osrmService.calculateRoute(origin, destination, TransportMode.DRIVING)

      expect(mockGet).toHaveBeenCalledWith(
        expect.stringContaining('/driving/'),
        expect.objectContaining({
          params: {
            overview: 'full',
            geometries: 'geojson',
            steps: true,
          },
        })
      )

      expect(result.totalDistance).toBe(313000)
      expect(result.transportMode).toBe(TransportMode.DRIVING)
      expect(result.geometry.type).toBe('LineString')
      expect(result.geometry.coordinates).toHaveLength(2)
      expect(result.waypoints).toHaveLength(2)
      expect(result.instructions).toContain('Head north')
    })

    it('calculates route successfully for cycling mode', async () => {
      const mockResponse = {
        data: {
          code: 'Ok',
          routes: [
            {
              distance: 320000,
              duration: 80000,
              geometry: {
                coordinates: [
                  [-9.1393, 38.7223],
                  [-8.6291, 41.1579],
                ],
              },
              legs: [{ steps: [] }],
            },
          ],
          waypoints: [{ location: [-9.1393, 38.7223] }, { location: [-8.6291, 41.1579] }],
        },
      }

      mockGet.mockResolvedValueOnce(mockResponse)

      const result = await osrmService.calculateRoute(origin, destination, TransportMode.BICYCLING)

      expect(mockGet).toHaveBeenCalledWith(expect.stringContaining('/cycling/'), expect.any(Object))
      expect(result.transportMode).toBe(TransportMode.BICYCLING)
    })

    it('calculates route successfully for walking mode', async () => {
      const mockResponse = {
        data: {
          code: 'Ok',
          routes: [
            {
              distance: 350000,
              duration: 250000,
              geometry: {
                coordinates: [
                  [-9.1393, 38.7223],
                  [-8.6291, 41.1579],
                ],
              },
              legs: [{ steps: [] }],
            },
          ],
          waypoints: [{ location: [-9.1393, 38.7223] }, { location: [-8.6291, 41.1579] }],
        },
      }

      mockGet.mockResolvedValueOnce(mockResponse)

      const result = await osrmService.calculateRoute(origin, destination, TransportMode.WALKING)

      expect(mockGet).toHaveBeenCalledWith(expect.stringContaining('/walking/'), expect.any(Object))
      expect(result.transportMode).toBe(TransportMode.WALKING)
    })

    it('uses driving profile as fallback for transit mode', async () => {
      const mockResponse = {
        data: {
          code: 'Ok',
          routes: [
            {
              distance: 313000,
              duration: 12000,
              geometry: {
                coordinates: [
                  [-9.1393, 38.7223],
                  [-8.6291, 41.1579],
                ],
              },
              legs: [{ steps: [] }],
            },
          ],
          waypoints: [{ location: [-9.1393, 38.7223] }, { location: [-8.6291, 41.1579] }],
        },
      }

      mockGet.mockResolvedValueOnce(mockResponse)

      const result = await osrmService.calculateRoute(origin, destination, TransportMode.TRANSIT)

      expect(mockGet).toHaveBeenCalledWith(expect.stringContaining('/driving/'), expect.any(Object))
      expect(result.transportMode).toBe(TransportMode.TRANSIT)
    })

    it('constructs correct URL with coordinates', async () => {
      const mockResponse = {
        data: {
          code: 'Ok',
          routes: [
            {
              distance: 1000,
              duration: 100,
              geometry: {
                coordinates: [
                  [-9.1393, 38.7223],
                  [-8.6291, 41.1579],
                ],
              },
              legs: [{ steps: [] }],
            },
          ],
          waypoints: [{ location: [-9.1393, 38.7223] }, { location: [-8.6291, 41.1579] }],
        },
      }

      mockGet.mockResolvedValueOnce(mockResponse)

      await osrmService.calculateRoute(origin, destination)

      const url = mockGet.mock.calls[0][0]
      expect(url).toContain(`${origin.lng},${origin.lat}`)
      expect(url).toContain(`${destination.lng},${destination.lat}`)
    })

    it('throws error when no route found', async () => {
      const mockResponse = {
        data: {
          code: 'NoRoute',
          routes: [],
          waypoints: [],
        },
      }

      mockGet.mockResolvedValueOnce(mockResponse)

      await expect(osrmService.calculateRoute(origin, destination)).rejects.toThrow(
        'No route found'
      )
    })

    it('throws error when API returns error code', async () => {
      const mockResponse = {
        data: {
          code: 'InvalidQuery',
          routes: [],
          waypoints: [],
        },
      }

      mockGet.mockResolvedValueOnce(mockResponse)

      await expect(osrmService.calculateRoute(origin, destination)).rejects.toThrow(
        'No route found'
      )
    })

    it('handles axios errors correctly', async () => {
      const axiosError = new Error('Network error')
      mockGet.mockRejectedValueOnce(axiosError)
      mockIsAxiosError.mockReturnValue(true)

      await expect(osrmService.calculateRoute(origin, destination)).rejects.toThrow(
        'OSRM API error: Network error'
      )
    })

    it('handles non-axios errors correctly', async () => {
      const genericError = new Error('Generic error')
      mockGet.mockRejectedValueOnce(genericError)
      mockIsAxiosError.mockReturnValue(false)

      await expect(osrmService.calculateRoute(origin, destination)).rejects.toThrow('Generic error')
    })

    it('adjusts duration when API returns unrealistic values', async () => {
      // Very unrealistic duration: 1 second for 100km
      const mockResponse = {
        data: {
          code: 'Ok',
          routes: [
            {
              distance: 100000, // 100km
              duration: 1, // 1 second (unrealistic)
              geometry: {
                coordinates: [
                  [-9.1393, 38.7223],
                  [-8.6291, 41.1579],
                ],
              },
              legs: [{ steps: [] }],
            },
          ],
          waypoints: [{ location: [-9.1393, 38.7223] }, { location: [-8.6291, 41.1579] }],
        },
      }

      mockGet.mockResolvedValueOnce(mockResponse)

      const result = await osrmService.calculateRoute(origin, destination, TransportMode.DRIVING)

      // Duration should be adjusted (much higher than 1 second)
      // For 100km driving at ~50km/h average, should be at least 5000 seconds
      expect(result.totalDuration).toBeGreaterThan(5000)
      // adjustDuration can return up to 2.1x calculated duration, so allow higher range
      expect(result.totalDuration).toBeLessThan(30000)
    })

    it('parses multiple instructions from legs', async () => {
      const mockResponse = {
        data: {
          code: 'Ok',
          routes: [
            {
              distance: 1000,
              duration: 100,
              geometry: {
                coordinates: [
                  [-9.1393, 38.7223],
                  [-8.6291, 41.1579],
                ],
              },
              legs: [
                {
                  steps: [
                    { maneuver: { instruction: 'Turn left' } },
                    { maneuver: { instruction: 'Turn right' } },
                    { maneuver: { instruction: 'Go straight' } },
                  ],
                },
              ],
            },
          ],
          waypoints: [{ location: [-9.1393, 38.7223] }, { location: [-8.6291, 41.1579] }],
        },
      }

      mockGet.mockResolvedValueOnce(mockResponse)

      const result = await osrmService.calculateRoute(origin, destination)

      // Instructions should be extracted from all steps in all legs
      // The service iterates over legs and then steps, extracting maneuver.instruction
      expect(result.instructions).toBeDefined()
      expect(result.instructions).toHaveLength(3)
      expect(result.instructions![0]).toBe('Turn left')
      expect(result.instructions![1]).toBe('Turn right')
      expect(result.instructions![2]).toBe('Go straight')
    })
  })
})
