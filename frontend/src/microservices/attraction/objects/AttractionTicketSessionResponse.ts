export type AttractionTicketSessionResponse = {
  sessionId: string
  sessionName: string
  useDate: string
  startsAt: string
  endsAt: string
  capacity: number
  availableQuantity: number | null
  status: string
}
