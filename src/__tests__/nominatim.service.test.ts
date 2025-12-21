import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import nominatimService from '../services/api/nominatim.service'

// Use vi.hoisted() for mocks
const { mockGet, mockIsAxiosError, mockGooglePlacesSearch, mockGooglePlacesReverseGeocode } =
  vi.hoisted(() => {
    return {
      mockGet: vi.fn(),
      mockIsAxiosError: vi.fn(),
      mockGooglePlacesSearch: vi.fn(),
      mockGooglePlacesReverseGeocode: vi.fn(),
    }
  })

// Mock axios
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

// Mock googlePlacesService
vi.mock('../services/api/google-places.service', () => ({
  default: {
    search: mockGooglePlacesSearch,
    reverseGeocode: mockGooglePlacesReverseGeocode,
  },
}))

describe('nominatim.service', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.spyOn(console, 'warn').mockImplementation(() => {})
    vi.spyOn(console, 'log').mockImplementation(() => {})
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('search', () => {
    it('uses Google Places API first (primary)', async () => {
      const mockGoogleResults = [
        { lat: 38.7223, lng: -9.1393, name: 'Lisboa', address: 'Lisboa, Portugal' },
      ]

      mockGooglePlacesSearch.mockResolvedValueOnce(mockGoogleResults)

      const result = await nominatimService.search('Lisboa')

      expect(mockGooglePlacesSearch).toHaveBeenCalledWith('Lisboa', 10)
      expect(result).toEqual(mockGoogleResults)
      expect(mockGet).not.toHaveBeenCalled()
    })

    it('falls back to Nominatim when Google Places fails', async () => {
      const googleError = new Error('Google Places API error')
      const mockNominatimResponse = {
        data: [
          {
            lat: '38.7223',
            lon: '-9.1393',
            display_name: 'Lisboa, Portugal',
            address: {
              city: 'Lisboa',
              country: 'Portugal',
            },
          },
        ],
        status: 200,
      }

      mockGooglePlacesSearch.mockRejectedValueOnce(googleError)
      mockGet.mockResolvedValueOnce(mockNominatimResponse)

      const result = await nominatimService.search('Lisboa')

      expect(mockGooglePlacesSearch).toHaveBeenCalled()
      expect(mockGet).toHaveBeenCalledWith(
        '/search',
        expect.objectContaining({
          params: {
            q: 'Lisboa',
            format: 'json',
            limit: 10,
            addressdetails: 1,
          },
        })
      )
      expect(result).toHaveLength(1)
      expect(result[0].lat).toBe(38.7223)
      expect(result[0].lng).toBe(-9.1393)
      expect(result[0].name).toBe('Lisboa, Portugal')
    })

    it('parses Nominatim results correctly', async () => {
      const googleError = new Error('Google Places API error')
      const mockNominatimResponse = {
        data: [
          {
            lat: '38.7223',
            lon: '-9.1393',
            display_name: 'Rua da Prata, Lisboa, Portugal',
            address: {
              road: 'Rua da Prata',
              city: 'Lisboa',
              country: 'Portugal',
            },
          },
        ],
        status: 200,
      }

      mockGooglePlacesSearch.mockRejectedValueOnce(googleError)
      mockGet.mockResolvedValueOnce(mockNominatimResponse)

      const result = await nominatimService.search('Rua da Prata')

      expect(result[0].address).toBe('Rua da Prata, Lisboa, Portugal')
    })

    it('handles Nominatim 418 rate limit error', async () => {
      const googleError = new Error('Google Places API error')
      const rateLimitResponse = {
        data: null,
        status: 418,
      }

      mockGooglePlacesSearch.mockRejectedValueOnce(googleError)
      mockGet.mockResolvedValueOnce(rateLimitResponse)

      await expect(nominatimService.search('Lisboa')).rejects.toThrow('Nominatim returned 418')
    })

    it('handles Nominatim 400+ errors', async () => {
      const googleError = new Error('Google Places API error')
      const badRequestResponse = {
        data: null,
        status: 403,
      }

      mockGooglePlacesSearch.mockRejectedValueOnce(googleError)
      mockGet.mockResolvedValueOnce(badRequestResponse)

      await expect(nominatimService.search('Lisboa')).rejects.toThrow('Nominatim returned 403')
    })

    it('handles empty Nominatim results', async () => {
      const googleError = new Error('Google Places API error')
      const emptyResponse = {
        data: [],
        status: 200,
      }

      mockGooglePlacesSearch.mockRejectedValueOnce(googleError)
      mockGet.mockResolvedValueOnce(emptyResponse)

      await expect(nominatimService.search('NonexistentPlace')).rejects.toThrow(
        'Nominatim returned empty results'
      )
    })

    it('throws combined error when both Google and Nominatim fail', async () => {
      const googleError = new Error('Google Places API error')
      const nominatimError = {
        message: 'Network error',
        isAxiosError: true,
      }

      mockGooglePlacesSearch.mockRejectedValueOnce(googleError)
      mockGet.mockRejectedValueOnce(nominatimError)
      mockIsAxiosError.mockReturnValue(true)

      await expect(nominatimService.search('Lisboa')).rejects.toThrow(
        'Geocoding failed (Google Places: Error: Google Places API error, Nominatim: Network error)'
      )
    })

    it('respects limit parameter', async () => {
      const googleError = new Error('Google Places API error')
      const mockNominatimResponse = {
        data: Array.from({ length: 5 }, (_, i) => ({
          lat: `${38.7223 + i}`,
          lon: `${-9.1393 + i}`,
          display_name: `Location ${i}`,
        })),
        status: 200,
      }

      mockGooglePlacesSearch.mockRejectedValueOnce(googleError)
      mockGet.mockResolvedValueOnce(mockNominatimResponse)

      const result = await nominatimService.search('Lisboa', 5)

      expect(mockGet).toHaveBeenCalledWith(
        '/search',
        expect.objectContaining({
          params: expect.objectContaining({
            limit: 5,
          }),
        })
      )
      expect(result).toHaveLength(5)
    })
  })

  describe('reverseGeocode', () => {
    it('uses Google Places API first (primary)', async () => {
      const mockGoogleResult = {
        lat: 38.7223,
        lng: -9.1393,
        name: 'Lisboa, Portugal',
        address: 'Lisboa, Portugal',
      }

      mockGooglePlacesReverseGeocode.mockResolvedValueOnce(mockGoogleResult)

      const result = await nominatimService.reverseGeocode(38.7223, -9.1393)

      expect(mockGooglePlacesReverseGeocode).toHaveBeenCalledWith(38.7223, -9.1393)
      expect(result).toEqual(mockGoogleResult)
      expect(mockGet).not.toHaveBeenCalled()
    })

    it('falls back to Nominatim when Google Places fails', async () => {
      const googleError = new Error('Google Places API error')
      const mockNominatimResponse = {
        data: {
          lat: '38.7223',
          lon: '-9.1393',
          display_name: 'Lisboa, Portugal',
          address: {
            city: 'Lisboa',
            country: 'Portugal',
          },
        },
        status: 200,
      }

      mockGooglePlacesReverseGeocode.mockRejectedValueOnce(googleError)
      mockGet.mockResolvedValueOnce(mockNominatimResponse)

      const result = await nominatimService.reverseGeocode(38.7223, -9.1393)

      expect(mockGet).toHaveBeenCalledWith(
        '/reverse',
        expect.objectContaining({
          params: {
            lat: 38.7223,
            lon: -9.1393,
            format: 'json',
            addressdetails: 1,
          },
        })
      )
      expect(result.lat).toBe(38.7223)
      expect(result.lng).toBe(-9.1393)
      expect(result.name).toBe('Lisboa, Portugal')
    })

    it('handles Nominatim 418 rate limit error in reverse geocoding', async () => {
      const googleError = new Error('Google Places API error')
      const rateLimitResponse = {
        data: null,
        status: 418,
      }

      mockGooglePlacesReverseGeocode.mockRejectedValueOnce(googleError)
      mockGet.mockResolvedValueOnce(rateLimitResponse)

      await expect(nominatimService.reverseGeocode(38.7223, -9.1393)).rejects.toThrow(
        'Nominatim returned 418'
      )
    })

    it('handles empty Nominatim reverse geocoding result', async () => {
      const googleError = new Error('Google Places API error')
      const emptyResponse = {
        data: null,
        status: 200,
      }

      mockGooglePlacesReverseGeocode.mockRejectedValueOnce(googleError)
      mockGet.mockResolvedValueOnce(emptyResponse)

      await expect(nominatimService.reverseGeocode(38.7223, -9.1393)).rejects.toThrow(
        'Nominatim returned empty result'
      )
    })

    it('throws combined error when both Google and Nominatim fail in reverse geocoding', async () => {
      const googleError = new Error('Google Places API error')
      const nominatimError = {
        message: 'Network error',
        isAxiosError: true,
      }

      mockGooglePlacesReverseGeocode.mockRejectedValueOnce(googleError)
      mockGet.mockRejectedValueOnce(nominatimError)
      mockIsAxiosError.mockReturnValue(true)

      await expect(nominatimService.reverseGeocode(38.7223, -9.1393)).rejects.toThrow(
        'Reverse geocoding failed (Google Places: Error: Google Places API error, Nominatim: Network error)'
      )
    })
  })
})
