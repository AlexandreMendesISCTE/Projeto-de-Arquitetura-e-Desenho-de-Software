import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { TransportMode } from '../types/route.types'

// Use vi.hoisted() for mocks
const { mockDirectionsService, mockTextarea } = vi.hoisted(() => {
  const directionsService = {
    route: vi.fn(),
  }

  const latLng = vi.fn((lat: number, lng: number) => ({
    lat: () => lat,
    lng: () => lng,
  }))

  // Create mock Google Maps object
  const mockGoogleMaps = {
    maps: {
      DirectionsService: vi.fn(() => directionsService),
      DirectionsStatus: {
        OK: 'OK',
      },
      TravelMode: {
        DRIVING: 'DRIVING',
        BICYCLING: 'BICYCLING',
        WALKING: 'WALKING',
        TRANSIT: 'TRANSIT',
      },
      UnitSystem: {
        METRIC: 'METRIC',
      },
      TrafficModel: {
        BEST_GUESS: 'BEST_GUESS',
      },
      LatLng: latLng,
    },
  }

  // Setup window.google before importing service
  ;(globalThis as any).window = {
    google: mockGoogleMaps,
    googleMapsLoaded: true,
  }
  ;(globalThis as any).google = mockGoogleMaps

  // Setup document for createElement (used in HTML sanitization)
  const textarea = {
    textContent: '',
    innerHTML: '',
  }
  ;(globalThis as any).document = {
    createElement: vi.fn((tag: string) => {
      if (tag === 'textarea') {
        return textarea
      }
      return {}
    }),
  }

  return {
    mockDirectionsService: directionsService,
    mockLatLng: latLng,
    mockTextarea: textarea,
  }
})

// Mock loadGoogleMapsAPI
vi.mock('../services/api/google-maps.service', async () => {
  const actual = await vi.importActual('../services/api/google-maps.service')
  return {
    ...actual,
    loadGoogleMapsAPI: vi.fn().mockResolvedValue(undefined),
  }
})

// Import service after mocks are set up
import googleMapsService from '../services/api/google-maps.service'

describe('google-maps.service', () => {
  const origin = { lat: 38.7223, lng: -9.1393, name: 'Lisboa' }
  const destination = { lat: 41.1579, lng: -8.6291, name: 'Porto' }

  beforeEach(() => {
    vi.clearAllMocks()
    // Reset textarea for each test
    mockTextarea.textContent = ''
    mockTextarea.innerHTML = ''
    vi.spyOn(console, 'error').mockImplementation(() => {})
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('calculateRoute', () => {
    it('calculates route successfully for driving mode', async () => {
      const mockRouteResult = {
        routes: [
          {
            overview_polyline: {
              points: 'encoded_polyline_string',
            },
            legs: [
              {
                distance: { value: 313000 },
                duration: { value: 12000 },
                duration_in_traffic: { value: 13000 },
                steps: [
                  {
                    end_location: {
                      lat: () => 39.0,
                      lng: () => -9.0,
                    },
                    instructions: 'Head north on Rua X',
                  },
                ],
              },
            ],
          },
        ],
      }

      mockDirectionsService.route.mockImplementation(
        (_request: any, callback: (result: any, status: string) => void) => {
          callback(mockRouteResult, 'OK')
        }
      )

      const result = await googleMapsService.calculateRoute(
        origin,
        destination,
        TransportMode.DRIVING
      )

      expect(mockDirectionsService.route).toHaveBeenCalledWith(
        expect.objectContaining({
          origin: expect.any(Object),
          destination: expect.any(Object),
          travelMode: 'DRIVING',
          unitSystem: 'METRIC',
          language: 'pt',
          drivingOptions: expect.objectContaining({
            departureTime: expect.any(Date),
            trafficModel: 'BEST_GUESS',
          }),
        }),
        expect.any(Function)
      )

      expect(result.totalDistance).toBe(313000)
      expect(result.totalDuration).toBe(13000) // Uses duration_in_traffic for driving
      expect(result.transportMode).toBe(TransportMode.DRIVING)
      expect(result.geometry.type).toBe('LineString')
    })

    it('calculates route successfully for walking mode', async () => {
      const mockRouteResult = {
        routes: [
          {
            overview_polyline: {
              points: 'encoded_polyline_string',
            },
            legs: [
              {
                distance: { value: 350000 },
                duration: { value: 250000 },
                steps: [
                  {
                    end_location: {
                      lat: () => 39.0,
                      lng: () => -9.0,
                    },
                    instructions: 'Walk north',
                  },
                ],
              },
            ],
          },
        ],
      }

      mockDirectionsService.route.mockImplementation(
        (_request: any, callback: (result: any, status: string) => void) => {
          callback(mockRouteResult, 'OK')
        }
      )

      const result = await googleMapsService.calculateRoute(
        origin,
        destination,
        TransportMode.WALKING
      )

      expect(result.transportMode).toBe(TransportMode.WALKING)
      expect(result.totalDuration).toBe(250000) // Uses duration (no traffic for walking)
    })

    it('includes waypoints when provided', async () => {
      const waypoints = [
        { lat: 39.0, lng: -9.0, name: 'Coimbra' },
        { lat: 40.0, lng: -8.5, name: 'Aveiro' },
      ]

      const mockRouteResult = {
        routes: [
          {
            overview_polyline: {
              points: 'encoded_polyline_string',
            },
            legs: [
              {
                distance: { value: 100000 },
                duration: { value: 5000 },
                steps: [],
              },
              {
                distance: { value: 50000 },
                duration: { value: 2500 },
                steps: [],
              },
              {
                distance: { value: 50000 },
                duration: { value: 2500 },
                steps: [],
              },
            ],
          },
        ],
      }

      mockDirectionsService.route.mockImplementation(
        (_request: any, callback: (result: any, status: string) => void) => {
          callback(mockRouteResult, 'OK')
        }
      )

      const result = await googleMapsService.calculateRoute(
        origin,
        destination,
        TransportMode.DRIVING,
        waypoints
      )

      expect(mockDirectionsService.route).toHaveBeenCalledWith(
        expect.objectContaining({
          waypoints: expect.arrayContaining([
            expect.objectContaining({
              location: expect.any(Object),
              stopover: true,
            }),
          ]),
          optimizeWaypoints: false,
        }),
        expect.any(Function)
      )

      expect(result.totalDistance).toBe(200000) // Sum of all legs
      expect(result.totalDuration).toBe(10000) // Sum of all legs
    })

    it('sanitizes HTML from instructions', async () => {
      const mockRouteResult = {
        routes: [
          {
            overview_polyline: {
              points: 'encoded_polyline_string',
            },
            legs: [
              {
                distance: { value: 1000 },
                duration: { value: 100 },
                steps: [
                  {
                    end_location: {
                      lat: () => 39.0,
                      lng: () => -9.0,
                    },
                    instructions: '<b>Head</b> north on <a href="#">Rua X</a>',
                  },
                ],
              },
            ],
          },
        ],
      }

      mockDirectionsService.route.mockImplementation(
        (_request: any, callback: (result: any, status: string) => void) => {
          callback(mockRouteResult, 'OK')
        }
      )

      const result = await googleMapsService.calculateRoute(origin, destination)

      // Instructions should have HTML tags removed
      expect(result.instructions).toBeDefined()
      expect(result.instructions).toHaveLength(1)
      expect(result.instructions![0]).toBe('Head north on Rua X')
      expect(result.instructions![0]).not.toContain('<b>')
      expect(result.instructions![0]).not.toContain('<a')
    })

    it('validates API key is configured', async () => {
      // Verify that the API key is available in the environment
      // The API key should be set in vitest.config.ts
      const apiKey = import.meta.env.VITE_GOOGLE_MAPS_API_KEY
      expect(apiKey).toBeTruthy()
      expect(apiKey).toBeTypeOf('string')
      expect(apiKey.length).toBeGreaterThan(0)
    })

    it('throws error when DirectionsService returns error status', async () => {
      mockDirectionsService.route.mockImplementation(
        (_request: any, callback: (result: any, status: string) => void) => {
          callback(null, 'ZERO_RESULTS')
        }
      )

      await expect(googleMapsService.calculateRoute(origin, destination)).rejects.toThrow(
        'Google Maps API error: ZERO_RESULTS'
      )
    })

    it('throws error when no routes returned', async () => {
      mockDirectionsService.route.mockImplementation(
        (_request: any, callback: (result: any, status: string) => void) => {
          callback({ routes: [] }, 'OK')
        }
      )

      await expect(googleMapsService.calculateRoute(origin, destination)).rejects.toThrow(
        'Google Maps API error: OK'
      )
    })

    it('throws error when route has invalid data (zero distance/duration)', async () => {
      const mockRouteResult = {
        routes: [
          {
            overview_polyline: {
              points: 'encoded_polyline_string',
            },
            legs: [
              {
                distance: { value: 0 },
                duration: { value: 0 },
                steps: [],
              },
            ],
          },
        ],
      }

      mockDirectionsService.route.mockImplementation(
        (_request: any, callback: (result: any, status: string) => void) => {
          callback(mockRouteResult, 'OK')
        }
      )

      await expect(googleMapsService.calculateRoute(origin, destination)).rejects.toThrow(
        'Invalid route data'
      )
    })

    it('uses step polyline as fallback when overview_polyline is not available', async () => {
      const mockRouteResult = {
        routes: [
          {
            overview_polyline: null,
            legs: [
              {
                distance: { value: 1000 },
                duration: { value: 100 },
                steps: [
                  {
                    polyline: {
                      points: 'fallback_polyline',
                    },
                    end_location: {
                      lat: () => 39.0,
                      lng: () => -9.0,
                    },
                    instructions: 'Head north',
                  },
                ],
              },
            ],
          },
        ],
      }

      mockDirectionsService.route.mockImplementation(
        (_request: any, callback: (result: any, status: string) => void) => {
          callback(mockRouteResult, 'OK')
        }
      )

      const result = await googleMapsService.calculateRoute(origin, destination)

      expect(result.geometry.coordinates.length).toBeGreaterThan(0)
    })

    it('throws error when no polyline data is available', async () => {
      const mockRouteResult = {
        routes: [
          {
            overview_polyline: null,
            legs: [
              {
                distance: { value: 1000 },
                duration: { value: 100 },
                steps: [
                  {
                    polyline: null,
                    end_location: {
                      lat: () => 39.0,
                      lng: () => -9.0,
                    },
                    instructions: 'Head north',
                  },
                ],
              },
            ],
          },
        ],
      }

      mockDirectionsService.route.mockImplementation(
        (_request: any, callback: (result: any, status: string) => void) => {
          callback(mockRouteResult, 'OK')
        }
      )

      await expect(googleMapsService.calculateRoute(origin, destination)).rejects.toThrow(
        'No polyline data in route'
      )
    })

    it('aggregates distance and duration from multiple legs', async () => {
      const mockRouteResult = {
        routes: [
          {
            overview_polyline: {
              points: 'encoded_polyline_string',
            },
            legs: [
              {
                distance: { value: 100000 },
                duration: { value: 5000 },
                steps: [],
              },
              {
                distance: { value: 50000 },
                duration: { value: 2500 },
                steps: [],
              },
            ],
          },
        ],
      }

      mockDirectionsService.route.mockImplementation(
        (_request: any, callback: (result: any, status: string) => void) => {
          callback(mockRouteResult, 'OK')
        }
      )

      const result = await googleMapsService.calculateRoute(origin, destination)

      expect(result.totalDistance).toBe(150000) // Sum of both legs
      expect(result.totalDuration).toBe(7500) // Sum of both legs
    })
  })
})
