import type { ChangeEventHandler } from 'react'

export function useRoundTripSearchRows({
  departureAirport,
  arrivalAirport,
  departureDate,
  returnDate,
  onDepartureAirportChange,
  onArrivalAirportChange,
  onDepartureDateChange,
  onReturnDateChange,
}: {
  departureAirport: string
  arrivalAirport: string
  departureDate: string
  returnDate: string
  onDepartureAirportChange: (value: string) => void
  onArrivalAirportChange: (value: string) => void
  onDepartureDateChange: (value: string) => void
  onReturnDateChange: (value: string) => void
}) {
  const onOutboundDepartureChange: ChangeEventHandler<HTMLSelectElement> = event => {
    onDepartureAirportChange(event.target.value)
  }
  const onOutboundArrivalChange: ChangeEventHandler<HTMLSelectElement> = event => {
    onArrivalAirportChange(event.target.value)
  }
  const onReturnDepartureChange: ChangeEventHandler<HTMLSelectElement> = event => {
    onArrivalAirportChange(event.target.value)
  }
  const onReturnArrivalChange: ChangeEventHandler<HTMLSelectElement> = event => {
    onDepartureAirportChange(event.target.value)
  }

  return {
    outboundRow: {
      departureAirport,
      arrivalAirport,
      date: departureDate,
      onDepartureChange: onOutboundDepartureChange,
      onArrivalChange: onOutboundArrivalChange,
      onDateChange: onDepartureDateChange,
    },
    returnRow: {
      departureAirport: arrivalAirport,
      arrivalAirport: departureAirport,
      date: returnDate,
      onDepartureChange: onReturnDepartureChange,
      onArrivalChange: onReturnArrivalChange,
      onDateChange: onReturnDateChange,
    },
  }
}
