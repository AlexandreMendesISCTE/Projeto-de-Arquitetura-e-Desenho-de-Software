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

import { useState, useRef, useEffect } from 'react'
import { Send, X, Minimize2 } from 'lucide-react'
import { useRouteStore } from '../store/route.store'
import { useMapStore } from '../store/map.store'
import { sendMessageToN8N } from '../services/api/n8n.service'
import nominatimService from '../services/api/nominatim.service'
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
  const messagesEndRef = useRef<HTMLDivElement>(null)
  const inputRef = useRef<HTMLInputElement>(null)

  const { setOrigin, setDestination, addWaypoint, origin, destination, waypoints } = useRouteStore()
  const { setCenter, setWaitingForInput, waitingForInput } = useMapStore()

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
   * Processa resposta do n8n e atualiza o mapa conforme necessário
   */
  const processN8NResponse = async (response: string) => {
    // Detectar comandos de localização na resposta
    const lowerResponse = response.toLowerCase()

    // Verificar se a resposta contém coordenadas ou endereços
    if (lowerResponse.includes('origem') || lowerResponse.includes('partida') || lowerResponse.includes('onde começa')) {
      setWaitingForInput('origin')
      addBotMessage(response + '\n\n👆 Clique no mapa para definir a origem ou digite o nome do local.')
    } else if (lowerResponse.includes('destino') || lowerResponse.includes('chegada') || lowerResponse.includes('onde termina')) {
      setWaitingForInput('destination')
      addBotMessage(response + '\n\n👆 Clique no mapa para definir o destino ou digite o nome do local.')
    } else if (lowerResponse.includes('paragem') || lowerResponse.includes('parada') || lowerResponse.includes('onde parar')) {
      setWaitingForInput('waypoint')
      addBotMessage(response + '\n\n👆 Clique no mapa para adicionar uma paragem ou digite o nome do local.')
    } else {
      // Tentar extrair nome de localização da resposta e fazer geocoding
      await tryExtractAndSetLocation(response)
      addBotMessage(response)
    }
  }

  /**
   * Tenta extrair nome de localização da resposta e definir automaticamente
   */
  const tryExtractAndSetLocation = async (response: string) => {
    // Padrões comuns para identificar localizações
    const originPattern = /(?:origem|partida|começar em|início em):\s*([^,\.\n]+)/i
    const destinationPattern = /(?:destino|chegada|terminar em|fim em):\s*([^,\.\n]+)/i
    const waypointPattern = /(?:paragem|parada em):\s*([^,\.\n]+)/i

    let match: RegExpMatchArray | null = null
    let locationType: 'origin' | 'destination' | 'waypoint' | null = null

    // Verificar padrão de origem
    match = response.match(originPattern)
    if (match && match[1]) {
      locationType = 'origin'
    } else {
      // Verificar padrão de destino
      match = response.match(destinationPattern)
      if (match && match[1]) {
        locationType = 'destination'
      } else {
        // Verificar padrão de paragem
        match = response.match(waypointPattern)
        if (match && match[1]) {
          locationType = 'waypoint'
        }
      }
    }

    if (match && match[1] && locationType) {
      const locationName = match[1].trim()
      try {
        // Fazer geocoding do nome da localização
        const results = await nominatimService.search(locationName)
        if (results && results.length > 0) {
          const locationData = results[0]

          // Definir localização baseado no tipo detectado
          if (locationType === 'origin') {
            setOrigin(locationData)
            setCenter([locationData.lat, locationData.lng])
            addBotMessage(`✅ Origem definida: ${locationData.name || locationName}`)
          } else if (locationType === 'destination') {
            setDestination(locationData)
            setCenter([locationData.lat, locationData.lng])
            addBotMessage(`✅ Destino definido: ${locationData.name || locationName}`)
          } else if (locationType === 'waypoint') {
            addWaypoint(locationData)
            setCenter([locationData.lat, locationData.lng])
            addBotMessage(`✅ Paragem adicionada: ${locationData.name || locationName}`)
          }
        }
      } catch (error) {
        console.error('Erro ao fazer geocoding:', error)
      }
    }
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
          destination: destination ? { name: destination.name || '', lat: destination.lat, lng: destination.lng } : null,
          waypoints: waypoints.map((wp) => ({ name: wp.name || '', lat: wp.lat, lng: wp.lng })),
        },
        waitingForInput: waitingForInput,
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
          onClick={() => setIsOpen(true)}
          className="fixed bottom-6 right-6 w-16 h-16 rounded-full bg-blue-600 hover:bg-blue-700 shadow-lg hover:shadow-xl transition-all duration-300 flex items-center justify-center group"
          style={{ pointerEvents: 'auto', zIndex: 99999, position: 'fixed' }}
          aria-label="Abrir chat"
        >
          <img
            src={chatBotIcon}
            alt="Chat Bot"
            className="w-12 h-12 object-contain filter brightness-0 invert"
          />
          {/* Indicador de notificação */}
          <span className="absolute -top-1 -right-1 w-4 h-4 bg-red-500 rounded-full border-2 border-white"></span>
        </button>
      )}

      {/* Janela do chat */}
      {isOpen && (
        <div
          className={`fixed bottom-6 right-6 bg-white rounded-lg shadow-2xl flex flex-col transition-all duration-300 ${
            isMinimized ? 'w-80 h-12' : 'w-96 h-[600px]'
          }`}
          style={{ pointerEvents: 'auto', zIndex: 99999, position: 'fixed' }}
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
                        <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.1s' }}></div>
                        <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.2s' }}></div>
                      </div>
                    </div>
                  </div>
                )}
                <div ref={messagesEndRef} />
              </div>

              {/* Input de mensagem */}
              <div className="border-t border-gray-200 p-4 bg-white">
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

