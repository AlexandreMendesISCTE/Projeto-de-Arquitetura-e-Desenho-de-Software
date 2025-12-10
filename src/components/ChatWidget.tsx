/**
 * Componente ChatWidget
 *
 * Widget de chat flutuante com ícone circular no canto inferior direito.
 * Integra com n8n workflow para processar mensagens e definir localizações.
 *
 * Funcionalidades:
 * - Chat interativo com n8n
 * - Definição de localização inicial, final e paragens
 * - Interface responsiva
 */

import { useState, useRef, useEffect, useCallback } from 'react'
import { Send, X, Minimize2 } from 'lucide-react'
import { useRouteStore } from '../store/route.store'
import { useMapStore } from '../store/map.store'
import { sendMessageToN8N, N8NResponse } from '../services/api/n8n.service'
import chatBotIcon from '/1eb05f325ec50a15c8b045f3428d6d5e-removebg-preview.png'

interface Message {
  id: string
  text: string
  sender: 'user' | 'bot'
  timestamp: Date
}

const ChatWidget = () => {
  const [isOpen, setIsOpen] = useState(false)
  const [isMinimized, setIsMinimized] = useState(false)
  const [messages, setMessages] = useState<Message[]>([
    {
      id: '1',
      text: 'Olá! Posso ajudá-lo a definir a sua rota. Pode dizer-me o ponto de partida, destino e paragens desejadas.',
      sender: 'bot',
      timestamp: new Date(),
    },
  ])
  const [inputMessage, setInputMessage] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [useCurrentLocation, setUseCurrentLocation] = useState(false)
  const [currentLocation, setCurrentLocation] = useState<{ name: string; lat: number; lng: number } | null>(null)
  const [locationError, setLocationError] = useState<string | null>(null)
  const messagesEndRef = useRef<HTMLDivElement>(null)
  const inputRef = useRef<HTMLInputElement>(null)

  // Draggable state - always reset to default position on mount
  const [position, setPosition] = useState<{ x: number | null; y: number | null }>({
    x: null,
    y: null,
  })
  const [isDragging, setIsDragging] = useState(false)
  const [dragStart, setDragStart] = useState<{ x: number; y: number }>({ x: 0, y: 0 })
  const [hasDragged, setHasDragged] = useState(false)
  const hasUserDraggedRef = useRef(false) // Track if user manually dragged (use ref for closure access)
  const [isMobile, setIsMobile] = useState(false)
  const buttonRef = useRef<HTMLButtonElement>(null)
  const chatWindowRef = useRef<HTMLDivElement>(null)

  // Check if mobile on mount and resize
  useEffect(() => {
    const checkMobile = () => {
      setIsMobile(window.innerWidth < 768)
    }
    checkMobile()
    window.addEventListener('resize', checkMobile)
    return () => window.removeEventListener('resize', checkMobile)
  }, [])

  const { setOrigin, setDestination, addWaypoint, setWaypoints, origin, destination, waypoints } =
    useRouteStore()
  const { setCenter, setWaitingForInput, waitingForInput } = useMapStore()

  // Track waypoints count to detect changes
  const waypointsCount = waypoints.filter((wp) => wp.lat !== 0 || wp.lng !== 0).length

  /**
   * Scroll automático para a última mensagem
   */
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  /**
   * Foco no input quando o chat abre
   */
  useEffect(() => {
    if (isOpen && !isMinimized) {
      inputRef.current?.focus()
    }
  }, [isOpen, isMinimized])

  /**
   * Calculate default position below the green arrow button in destination row (mobile)
   * Or bottom-right for desktop
   * Recalculates on resize, waypoints changes, and DOM mutations to maintain responsive positioning
   */
  useEffect(() => {
    const calculateDefaultPosition = () => {
      // On desktop, use bottom-right position (don't set position, use CSS)
      if (!isMobile) {
        // Reset to null to use CSS positioning (bottom-right)
        setPosition({ x: null, y: null })
        return
      }

      // On mobile, position below the green arrow button in destination row
      const destinationField = document.querySelector('[data-destination-field]')
      if (destinationField && buttonRef.current) {
        const greenButton = destinationField.querySelector('button[class*="bg-green-500"]')
        if (greenButton) {
          const rect = greenButton.getBoundingClientRect()
          const chatButton = buttonRef.current
          const chatButtonWidth = chatButton.offsetWidth || 56

          // Calculate responsive gap based on viewport height
          // Use a small fixed gap plus a small percentage of viewport for better scaling
          const gap = Math.max(2, window.innerHeight * 0.02) // At least 2px, or 2% of viewport height

          // Position below the green button, aligned to its right edge
          // Use right edge of green button minus chat button width
          const x = rect.right - chatButtonWidth
          const y = rect.bottom + gap

          // Only update if user hasn't manually dragged
          // This allows responsive updates on resize while maintaining user's manual position
          if (!hasUserDraggedRef.current) {
            setPosition({ x, y })
          } else {
            // If user has dragged, constrain to viewport but keep their position
            setPosition((prev) => {
              if (prev.x === null || prev.y === null) return prev
              const windowWidth = window.innerWidth
              const windowHeight = window.innerHeight
              const constrainedX = Math.max(0, Math.min(prev.x, windowWidth - chatButtonWidth))
              const constrainedY = Math.max(
                0,
                Math.min(prev.y, windowHeight - chatButton.offsetHeight)
              )
              return { x: constrainedX, y: constrainedY }
            })
          }
        }
      }
    }

    // Calculate position after a short delay to ensure DOM is ready
    const timeout = setTimeout(calculateDefaultPosition, 150)

    // Recalculate on window resize for responsive behavior
    window.addEventListener('resize', calculateDefaultPosition)

    // Watch for DOM changes in LocationSearch component (when waypoints are added/removed)
    const locationSearchContainer = document.querySelector('[data-location-search-container]')

    let mutationObserver: MutationObserver | null = null
    if (locationSearchContainer && typeof MutationObserver !== 'undefined') {
      let mutationTimeout: ReturnType<typeof setTimeout> | null = null
      mutationObserver = new MutationObserver(() => {
        // Debounce mutation observer calls
        if (mutationTimeout) clearTimeout(mutationTimeout)
        mutationTimeout = setTimeout(calculateDefaultPosition, 150)
      })

      mutationObserver.observe(locationSearchContainer, {
        childList: true,
        subtree: true,
        attributes: true,
        attributeFilter: ['style', 'class'],
      })
    }

    return () => {
      clearTimeout(timeout)
      window.removeEventListener('resize', calculateDefaultPosition)
      if (mutationObserver) {
        mutationObserver.disconnect()
      }
    }
  }, [isMobile, waypointsCount, origin, destination]) // Re-run if mobile state, waypoints, origin, or destination changes

  /**
   * Handle drag start
   */
  const handleDragStart = (
    e: React.MouseEvent<HTMLButtonElement> | React.TouchEvent<HTMLButtonElement>
  ) => {
    if (isOpen) return // Don't drag when chat is open

    // Only preventDefault for mouse events, not touch (touch will be handled in non-passive listener)
    if (!('touches' in e)) {
      e.preventDefault()
    }

    setIsDragging(true)
    setHasDragged(false)

    const clientX = 'touches' in e ? e.touches[0].clientX : e.clientX
    const clientY = 'touches' in e ? e.touches[0].clientY : e.clientY

    const button = buttonRef.current
    if (!button) return

    // Get current button position
    const rect = button.getBoundingClientRect()
    const currentX = position.x !== null ? position.x : window.innerWidth - rect.width - 16
    const currentY =
      position.y !== null ? position.y : window.innerHeight - rect.height - (isMobile ? 80 : 24)

    setDragStart({
      x: clientX - currentX,
      y: clientY - currentY,
    })
  }

  /**
   * Handle drag move
   */
  const handleDragMove = useCallback(
    (e: MouseEvent | TouchEvent) => {
      if (!isDragging) return

      // preventDefault is called in the event listener wrapper
      setHasDragged(true)
      hasUserDraggedRef.current = true // Mark that user has manually dragged

      const clientX = 'touches' in e ? e.touches[0].clientX : e.clientX
      const clientY = 'touches' in e ? e.touches[0].clientY : e.clientY

      const newX = clientX - dragStart.x
      const newY = clientY - dragStart.y

      // Get button dimensions
      const button = buttonRef.current
      if (!button) return

      const buttonWidth = button.offsetWidth
      const buttonHeight = button.offsetHeight
      const windowWidth = window.innerWidth
      const windowHeight = window.innerHeight

      // Constrain to viewport bounds
      const constrainedX = Math.max(0, Math.min(newX, windowWidth - buttonWidth))
      const constrainedY = Math.max(0, Math.min(newY, windowHeight - buttonHeight))

      setPosition({ x: constrainedX, y: constrainedY })
    },
    [dragStart.x, dragStart.y, isDragging]
  )

  /**
   * Handle drag end
   */
  const handleDragEnd = useCallback(() => {
    setIsDragging(false)
    // Position is kept in state but will reset on page reload
  }, [])

  /**
   * Set up drag event listeners
   */
  useEffect(() => {
    if (isDragging) {
      const handleMouseMove = (e: MouseEvent) => {
        e.preventDefault()
        handleDragMove(e)
      }
      const handleTouchMove = (e: TouchEvent) => {
        e.preventDefault()
        handleDragMove(e)
      }
      const handleMouseUp = () => handleDragEnd()
      const handleTouchEnd = () => handleDragEnd()

      document.addEventListener('mousemove', handleMouseMove, { passive: false })
      document.addEventListener('touchmove', handleTouchMove, { passive: false })
      document.addEventListener('mouseup', handleMouseUp)
      document.addEventListener('touchend', handleTouchEnd)

      return () => {
        document.removeEventListener('mousemove', handleMouseMove)
        document.removeEventListener('touchmove', handleTouchMove)
        document.removeEventListener('mouseup', handleMouseUp)
        document.removeEventListener('touchend', handleTouchEnd)
      }
    }
  }, [handleDragEnd, handleDragMove, isDragging])

  /**
   * Processa resposta do n8n e atualiza o mapa conforme necessário
   */
  const processN8NResponse = (response: N8NResponse) => {
    // Processar ação se existir
    if (response.action) {
      switch (response.action) {
        case 'set_route':
          // Definir origem e destino quando ambos estão presentes
          if (response.origin && response.destination) {
            setOrigin({
              lat: response.origin.lat,
              lng: response.origin.lng,
              name: response.origin.name,
            })
            setDestination({
              lat: response.destination.lat,
              lng: response.destination.lng,
              name: response.destination.name,
            })
            
            // Centrar mapa entre origem e destino
            const centerLat = (response.origin.lat + response.destination.lat) / 2
            const centerLng = (response.origin.lng + response.destination.lng) / 2
            setCenter([centerLat, centerLng])

            // Mensagem de confirmação formatada
            let confirmMessage = `✅ Processado. 🗺️ Rota definida:\n📍 Origem: ${response.origin.name}\n🎯 Destino: ${response.destination.name}`
            if (response.waypoints && response.waypoints.length > 0) {
              confirmMessage += `\n🛑 Paragens: ${response.waypoints.map((w: any) => w.name.split(',')[0]).join(', ')}`
            }
            addBotMessage(confirmMessage)
          } else {
            addBotMessage(response.message)
          }
          break

        case 'set_origin':
          if (response.location) {
            setOrigin({
              lat: response.location.lat,
              lng: response.location.lng,
              name: response.location.name,
            })
            setCenter([response.location.lat, response.location.lng])
            addBotMessage(`✅ Origem definida: ${response.location.name}`)
          } else {
            addBotMessage(response.message)
          }
          break

        case 'set_destination':
          if (response.location) {
            setDestination({
              lat: response.location.lat,
              lng: response.location.lng,
              name: response.location.name,
            })
            setCenter([response.location.lat, response.location.lng])
            addBotMessage(`✅ Destino definido: ${response.location.name}`)
          } else {
            addBotMessage(response.message)
          }
          break

        case 'add_waypoint':
          if (response.location) {
            addWaypoint({
              lat: response.location.lat,
              lng: response.location.lng,
              name: response.location.name,
            })
            setCenter([response.location.lat, response.location.lng])
            addBotMessage(`✅ Paragem adicionada: ${response.location.name}`)
          } else {
            addBotMessage(response.message)
          }
          break

        case 'add_waypoints':
          // Add multiple waypoints (from chat command)
          if (response.waypoints && response.waypoints.length > 0) {
            // Check if we would exceed the 5 waypoint limit
            const currentCount = waypoints.filter(wp => wp.lat !== 0 || wp.lng !== 0).length
            const availableSlots = 5 - currentCount
            
            if (availableSlots <= 0) {
              addBotMessage('⚠️ Limite máximo de 5 paragens atingido.')
            } else {
              const waypointsToAdd = response.waypoints.slice(0, availableSlots)
              waypointsToAdd.forEach((wp: any) => {
                addWaypoint({
                  lat: wp.lat,
                  lng: wp.lng,
                  name: wp.name,
                })
              })
              
              if (response.waypoints.length > availableSlots) {
                addBotMessage(`✅ ${availableSlots} paragem(ns) adicionada(s). Limite máximo atingido.`)
              } else {
                addBotMessage(response.message)
              }
            }
          } else {
            addBotMessage(response.message)
          }
          break

        case 'clear_route':
          // Reset route store
          setOrigin(null)
          setDestination(null)
          setWaypoints([])
          addBotMessage(response.message || 'Rota limpa.')
          break

        default:
          // Apenas mostrar mensagem se não houver ação específica
          addBotMessage(response.message)
      }
    } else {
      // Sem ação específica, apenas mostrar mensagem
      addBotMessage(response.message)
    }

    // Limpar waitingForInput após processar resposta
    setWaitingForInput(null)
  }

  /**
   * Adiciona mensagem do bot ao chat
   */
  const addBotMessage = (text: string) => {
    const newMessage: Message = {
      id: Date.now().toString(),
      text,
      sender: 'bot',
      timestamp: new Date(),
    }
    setMessages((prev) => [...prev, newMessage])
  }

  /**
   * Obtém a localização atual do utilizador
   */
  const getCurrentLocation = () => {
    if (!navigator.geolocation) {
      setLocationError('Geolocalização não suportada pelo seu navegador')
      return
    }

    setLocationError(null)
    navigator.geolocation.getCurrentPosition(
      async (position) => {
        const lat = position.coords.latitude
        const lng = position.coords.longitude

        // Reverse geocode to get location name
        try {
          const response = await fetch(
            `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json`
          )
          const data = await response.json()
          const locationName = data.display_name || `${lat.toFixed(4)}, ${lng.toFixed(4)}`
          
          setCurrentLocation({ name: locationName, lat, lng })
          addBotMessage(`📍 Localização atual: ${locationName}`)
        } catch (error) {
          console.error('Erro ao obter nome da localização:', error)
          setCurrentLocation({ 
            name: `${lat.toFixed(4)}, ${lng.toFixed(4)}`, 
            lat, 
            lng 
          })
        }
      },
      (error) => {
        console.error('Erro ao obter localização:', error)
        setLocationError('Não foi possível obter a sua localização')
        setUseCurrentLocation(false)
      }
    )
  }

  /**
   * Toggle use current location
   */
  const handleToggleCurrentLocation = () => {
    const newValue = !useCurrentLocation
    setUseCurrentLocation(newValue)
    
    if (newValue && !currentLocation) {
      getCurrentLocation()
    }
  }

  /**
   * Envia mensagem para o n8n workflow
   */
  const handleSendMessage = async () => {
    if (!inputMessage.trim() || isLoading) return

    const userMessage: Message = {
      id: Date.now().toString(),
      text: inputMessage,
      sender: 'user',
      timestamp: new Date(),
    }

    setMessages((prev) => [...prev, userMessage])
    setInputMessage('')
    setIsLoading(true)

    try {
      // Enviar contexto atual da rota para o n8n
      const context = {
        message: inputMessage,
        currentRoute: {
          origin: origin ? { name: origin.name || '', lat: origin.lat, lng: origin.lng } : null,
          destination: destination
            ? { name: destination.name || '', lat: destination.lat, lng: destination.lng }
            : null,
          waypoints: waypoints.map((wp) => ({ name: wp.name || '', lat: wp.lat, lng: wp.lng })),
        },
        waitingForInput: waitingForInput,
        userLocation: currentLocation,
        useCurrentLocationAsOrigin: useCurrentLocation,
      }

      const response = await sendMessageToN8N(context)
      processN8NResponse(response)
    } catch (error) {
      console.error('Erro ao enviar mensagem para n8n:', error)
      addBotMessage(
        'Desculpe, ocorreu um erro ao processar a sua mensagem. Por favor, tente novamente.'
      )
    } finally {
      setIsLoading(false)
    }
  }

  /**
   * Manipula tecla Enter para enviar mensagem
   */
  const handleKeyPress = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSendMessage()
    }
  }

  /**
   * Formata timestamp da mensagem
   */
  const formatTime = (date: Date) => {
    return date.toLocaleTimeString('pt-PT', { hour: '2-digit', minute: '2-digit' })
  }

  return (
    <>
      {/* Botão flutuante do chat */}
      {!isOpen && (
        <button
          ref={buttonRef}
          onMouseDown={handleDragStart}
          onTouchStart={handleDragStart}
          onClick={() => {
            // Only open if not dragging and didn't drag
            if (!isDragging && !hasDragged) {
              setIsOpen(true)
            }
            setHasDragged(false)
          }}
          className={`fixed w-14 h-14 md:w-16 md:h-16 rounded-full bg-blue-600 hover:bg-blue-700 shadow-lg hover:shadow-xl transition-all duration-300 flex items-center justify-center group ${
            isDragging ? 'cursor-grabbing' : 'cursor-grab'
          }`}
          style={{
            pointerEvents: 'auto',
            zIndex: 100001,
            position: 'fixed',
            ...(isMobile && position.x !== null && position.y !== null
              ? {
                  left: `${position.x}px`,
                  top: `${position.y}px`,
                  bottom: 'auto',
                  right: 'auto',
                }
              : {
                  // Desktop: use CSS positioning (bottom-right) - scales automatically
                  bottom: '24px',
                  right: '16px',
                  left: 'auto',
                  top: 'auto',
                }),
            transform: isDragging ? 'scale(1.1)' : undefined,
          }}
          aria-label="Abrir chat"
        >
          <img
            src={chatBotIcon}
            alt="Chat Bot"
            className="w-10 h-10 md:w-12 md:h-12 object-contain filter brightness-0 invert"
          />
          {/* Indicador de notificação */}
          <span className="absolute -top-1 -right-1 w-3 h-3 md:w-4 md:h-4 bg-red-500 rounded-full border-2 border-white"></span>
        </button>
      )}

      {/* Janela do chat */}
      {isOpen && (
        <div
          ref={chatWindowRef}
          className={`fixed bottom-20 right-4 md:bottom-6 md:right-6 bg-white rounded-lg shadow-2xl flex flex-col transition-all duration-300 ${
            isMinimized
              ? 'w-[calc(100vw-2rem)] md:w-80 h-12'
              : 'w-[calc(100vw-2rem)] md:w-96 h-[calc(100vh-8rem)] md:h-[600px] max-w-md'
          }`}
          style={{ pointerEvents: 'auto', zIndex: 100001, position: 'fixed' }}
        >
          {/* Cabeçalho do chat */}
          <div className="bg-blue-600 text-white p-4 rounded-t-lg flex items-center justify-between">
            <div className="flex items-center gap-3">
              <img
                src={chatBotIcon}
                alt="Chat Bot"
                className="w-8 h-8 object-contain filter brightness-0 invert"
              />
              <div>
                <h3 className="font-semibold">Assistente de Rotas</h3>
                <p className="text-xs text-blue-100">Online</p>
              </div>
            </div>
            <div className="flex gap-2">
              <button
                onClick={() => setIsMinimized(!isMinimized)}
                className="p-1 hover:bg-blue-700 rounded transition-colors"
                aria-label={isMinimized ? 'Expandir' : 'Minimizar'}
              >
                <Minimize2 size={18} />
              </button>
              <button
                onClick={() => {
                  setIsOpen(false)
                  setIsMinimized(false)
                }}
                className="p-1 hover:bg-blue-700 rounded transition-colors"
                aria-label="Fechar chat"
              >
                <X size={18} />
              </button>
            </div>
          </div>

          {/* Área de mensagens */}
          {!isMinimized && (
            <>
              <div className="flex-1 overflow-y-auto p-4 space-y-4 bg-gray-50">
                {messages.map((message) => (
                  <div
                    key={message.id}
                    className={`flex ${message.sender === 'user' ? 'justify-end' : 'justify-start'}`}
                  >
                    <div
                      className={`max-w-[80%] rounded-lg px-4 py-2 ${
                        message.sender === 'user'
                          ? 'bg-blue-600 text-white'
                          : 'bg-white text-gray-800 border border-gray-200'
                      }`}
                    >
                      <p className="text-sm whitespace-pre-wrap">{message.text}</p>
                      <p
                        className={`text-xs mt-1 ${
                          message.sender === 'user' ? 'text-blue-100' : 'text-gray-500'
                        }`}
                      >
                        {formatTime(message.timestamp)}
                      </p>
                    </div>
                  </div>
                ))}
                {isLoading && (
                  <div className="flex justify-start">
                    <div className="bg-white border border-gray-200 rounded-lg px-4 py-2">
                      <div className="flex gap-1">
                        <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce"></div>
                        <div
                          className="w-2 h-2 bg-gray-400 rounded-full animate-bounce"
                          style={{ animationDelay: '0.1s' }}
                        ></div>
                        <div
                          className="w-2 h-2 bg-gray-400 rounded-full animate-bounce"
                          style={{ animationDelay: '0.2s' }}
                        ></div>
                      </div>
                    </div>
                  </div>
                )}
                <div ref={messagesEndRef} />
              </div>

              {/* Input de mensagem */}
              <div className="border-t border-gray-200 p-4 bg-white">
                {/* Checkbox para usar localização atual */}
                <div className="mb-3">
                  <label className="flex items-center gap-2 text-sm cursor-pointer hover:bg-gray-50 p-2 rounded-lg transition-colors">
                    <input
                      type="checkbox"
                      checked={useCurrentLocation}
                      onChange={handleToggleCurrentLocation}
                      className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500 focus:ring-2"
                    />
                    <span className="text-gray-700">
                      📍 Usar a minha localização atual como origem
                    </span>
                  </label>
                  {locationError && (
                    <p className="text-xs text-red-500 mt-1 ml-6">{locationError}</p>
                  )}
                  {useCurrentLocation && currentLocation && (
                    <p className="text-xs text-green-600 mt-1 ml-6">
                      ✓ Localização obtida: {currentLocation.name.substring(0, 50)}...
                    </p>
                  )}
                </div>

                <div className="flex gap-2">
                  <input
                    ref={inputRef}
                    type="text"
                    value={inputMessage}
                    onChange={(e) => setInputMessage(e.target.value)}
                    onKeyPress={handleKeyPress}
                    placeholder="Digite sua mensagem..."
                    className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    disabled={isLoading}
                  />
                  <button
                    onClick={handleSendMessage}
                    disabled={!inputMessage.trim() || isLoading}
                    className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors flex items-center justify-center"
                    aria-label="Enviar mensagem"
                  >
                    <Send size={20} />
                  </button>
                </div>
                <p className="text-xs text-gray-500 mt-2">
                  Pode definir origem, destino e paragens através do chat
                </p>
              </div>
            </>
          )}
        </div>
      )}
    </>
  )
}

export default ChatWidget
