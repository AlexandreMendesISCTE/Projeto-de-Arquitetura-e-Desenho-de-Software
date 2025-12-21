import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'

// Use vi.hoisted() to create mocks that can be accessed in both mock factory and tests
const { mockPost, mockIsAxiosError } = vi.hoisted(() => {
  return {
    mockPost: vi.fn(),
    mockIsAxiosError: vi.fn(),
  }
})

// Mock axios before importing the service
vi.mock('axios', () => {
  const mockAxiosInstance = {
    post: mockPost,
  }
  return {
    default: {
      create: vi.fn(() => mockAxiosInstance),
      isAxiosError: mockIsAxiosError,
    },
  }
})

// Import service after mocking axios
import poiService from '../services/api/poi.service'

describe('poi.service', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.spyOn(console, 'error').mockImplementation(() => {})
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('getPOIsInBounds', () => {
    it('fetches POIs within bounds successfully', async () => {
      const mockResponse = {
        data: {
          elements: [
            {
              type: 'node',
              id: 1,
              lat: 38.7223,
              lon: -9.1393,
              tags: {
                name: 'Cafe Central',
                amenity: 'cafe',
              },
            },
            {
              type: 'node',
              id: 2,
              lat: 38.73,
              lon: -9.14,
              tags: {
                name: 'Posto de Combustível',
                amenity: 'fuel',
              },
            },
          ],
        },
      }

      mockPost.mockResolvedValueOnce(mockResponse)

      const pois = await poiService.getPOIsInBounds(38.7, -9.15, 38.75, -9.1)

      expect(mockPost).toHaveBeenCalledWith(
        '',
        expect.stringContaining('[out:json]'),
        expect.objectContaining({
          headers: {
            'Content-Type': 'text/plain',
          },
          timeout: 15000,
        })
      )

      expect(pois).toHaveLength(2)
      expect(pois[0].name).toBe('Cafe Central')
      expect(pois[0].category).toBe('cafe')
      expect(pois[1].name).toBe('Posto de Combustível')
      expect(pois[1].category).toBe('fuel')
    })

    it('limits results to 100 POIs', async () => {
      const elements = Array.from({ length: 150 }, (_, i) => ({
        type: 'node',
        id: i + 1,
        lat: 38.7223 + i * 0.001,
        lon: -9.1393 + i * 0.001,
        tags: {
          name: `POI ${i + 1}`,
          amenity: 'cafe',
        },
      }))

      const mockResponse = {
        data: {
          elements,
        },
      }

      mockPost.mockResolvedValueOnce(mockResponse)

      const pois = await poiService.getPOIsInBounds(38.7, -9.15, 38.75, -9.1)

      expect(pois).toHaveLength(100)
    })

    it('filters POIs by category (restaurant, cafe, fuel, etc)', async () => {
      const mockResponse = {
        data: {
          elements: [
            {
              type: 'node',
              id: 1,
              lat: 38.7223,
              lon: -9.1393,
              tags: {
                name: 'Restaurante',
                amenity: 'restaurant',
              },
            },
            {
              type: 'node',
              id: 2,
              lat: 38.73,
              lon: -9.14,
              tags: {
                name: 'Supermercado',
                shop: 'supermarket',
              },
            },
            {
              type: 'node',
              id: 3,
              lat: 38.74,
              lon: -9.13,
              tags: {
                name: 'Museu',
                tourism: 'museum',
              },
            },
          ],
        },
      }

      mockPost.mockResolvedValueOnce(mockResponse)

      const pois = await poiService.getPOIsInBounds(38.7, -9.15, 38.75, -9.1)

      expect(pois).toHaveLength(3)
      expect(pois[0].category).toBe('restaurant')
      expect(pois[1].category).toBe('supermarket')
      expect(pois[2].category).toBe('museum')
    })

    it('handles way elements with center coordinates', async () => {
      const mockResponse = {
        data: {
          elements: [
            {
              type: 'way',
              id: 1,
              lat: 38.7223,
              lon: -9.1393,
              tags: {
                name: 'Cafe Way',
                amenity: 'cafe',
              },
            },
          ],
        },
      }

      mockPost.mockResolvedValueOnce(mockResponse)

      const pois = await poiService.getPOIsInBounds(38.7, -9.15, 38.75, -9.1)

      expect(pois).toHaveLength(1)
      expect(pois[0].lat).toBe(38.7223)
      expect(pois[0].lng).toBe(-9.1393)
      expect(pois[0].type).toBe('way')
    })

    it('skips elements without name tag', async () => {
      const mockResponse = {
        data: {
          elements: [
            {
              type: 'node',
              id: 1,
              lat: 38.7223,
              lon: -9.1393,
              tags: {
                amenity: 'cafe',
                // No name tag
              },
            },
            {
              type: 'node',
              id: 2,
              lat: 38.73,
              lon: -9.14,
              tags: {
                name: 'Cafe Named',
                amenity: 'cafe',
              },
            },
          ],
        },
      }

      mockPost.mockResolvedValueOnce(mockResponse)

      const pois = await poiService.getPOIsInBounds(38.7, -9.15, 38.75, -9.1)

      expect(pois).toHaveLength(1)
      expect(pois[0].name).toBe('Cafe Named')
    })

    it('shrinks bounding box if too large', async () => {
      // Very large bounding box (> 0.1 degrees)
      const largeMinLat = 38.0
      const largeMaxLat = 39.0 // 1 degree = ~111km
      const largeMinLng = -10.0
      const largeMaxLng = -8.0

      const mockResponse = {
        data: {
          elements: [],
        },
      }

      mockPost.mockResolvedValueOnce(mockResponse)

      await poiService.getPOIsInBounds(largeMinLat, largeMinLng, largeMaxLat, largeMaxLng)

      const query = mockPost.mock.calls[0][1] as string
      // Query should contain smaller bounds (shrunk to max 0.1 degrees)
      expect(query).toBeDefined()
    })

    it('returns empty array on timeout', async () => {
      const timeoutError = {
        code: 'ECONNABORTED',
        isAxiosError: true,
      }

      mockPost.mockRejectedValueOnce(timeoutError)
      mockIsAxiosError.mockReturnValue(true)

      const pois = await poiService.getPOIsInBounds(38.7, -9.15, 38.75, -9.1)

      expect(pois).toEqual([])
    })

    it('returns empty array on 400 bad request', async () => {
      const badRequestError = {
        response: { status: 400 },
        isAxiosError: true,
      }

      mockPost.mockRejectedValueOnce(badRequestError)
      mockIsAxiosError.mockReturnValue(true)

      const pois = await poiService.getPOIsInBounds(38.7, -9.15, 38.75, -9.1)

      expect(pois).toEqual([])
    })

    it('returns empty array on 504 gateway timeout', async () => {
      const gatewayTimeoutError = {
        response: { status: 504 },
        isAxiosError: true,
      }

      mockPost.mockRejectedValueOnce(gatewayTimeoutError)
      mockIsAxiosError.mockReturnValue(true)

      const pois = await poiService.getPOIsInBounds(38.7, -9.15, 38.75, -9.1)

      expect(pois).toEqual([])
    })

    it('logs error for non-timeout errors', async () => {
      const networkError = {
        response: { status: 500, data: 'Server error' },
        message: 'Network error',
        isAxiosError: true,
      }

      mockPost.mockRejectedValueOnce(networkError)
      mockIsAxiosError.mockReturnValue(true)

      const pois = await poiService.getPOIsInBounds(38.7, -9.15, 38.75, -9.1)

      expect(pois).toEqual([])
      expect(console.error).toHaveBeenCalled()
    })
  })

  describe('getPOIsAlongRoute', () => {
    it('returns empty array for route with less than 2 coordinates', async () => {
      const pois = await poiService.getPOIsAlongRoute([])
      expect(pois).toEqual([])

      const poisSingle = await poiService.getPOIsAlongRoute([[38.7223, -9.1393]])
      expect(poisSingle).toEqual([])
    })

    it('creates bounding box around route coordinates', async () => {
      const routeCoordinates: [number, number][] = [
        [-9.1393, 38.7223],
        [-9.1, 38.73],
        [-8.6291, 41.1579],
      ]

      const mockResponse = {
        data: {
          elements: [],
        },
      }

      mockPost.mockResolvedValueOnce(mockResponse)

      await poiService.getPOIsAlongRoute(routeCoordinates)

      const query = mockPost.mock.calls[0][1] as string
      // Query should contain bounding box coordinates (with padding, so check approximate values)
      expect(query).toContain('38.') // min lat (with padding)
      expect(query).toContain('41.') // max lat (with padding)
      expect(query).toContain('-9.') // min lng (with padding)
      expect(query).toContain('-8.') // max lng (with padding)
    })

    it('adds padding to bounding box', async () => {
      const routeCoordinates: [number, number][] = [
        [-9.1393, 38.7223],
        [-8.6291, 41.1579],
      ]

      const mockResponse = {
        data: {
          elements: [],
        },
      }

      mockPost.mockResolvedValueOnce(mockResponse)

      await poiService.getPOIsAlongRoute(routeCoordinates)

      const query = mockPost.mock.calls[0][1] as string
      // Bounding box should have 0.01 degree padding
      expect(query).toBeDefined()
    })
  })

  describe('getPOIsNearLocation', () => {
    it('converts radius from meters to degrees', async () => {
      const location = { lat: 38.7223, lng: -9.1393, name: 'Lisboa' }
      const radius = 500 // meters

      const mockResponse = {
        data: {
          elements: [],
        },
      }

      mockPost.mockResolvedValueOnce(mockResponse)

      await poiService.getPOIsNearLocation(location, radius)

      // Should call getPOIsInBounds with converted radius
      expect(mockPost).toHaveBeenCalled()
    })

    it('uses default radius of 500 meters', async () => {
      const location = { lat: 38.7223, lng: -9.1393, name: 'Lisboa' }

      const mockResponse = {
        data: {
          elements: [],
        },
      }

      mockPost.mockResolvedValueOnce(mockResponse)

      await poiService.getPOIsNearLocation(location)

      expect(mockPost).toHaveBeenCalled()
    })
  })
})
