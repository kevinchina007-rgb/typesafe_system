export type AircraftModel = {
  modelName: string
}

export const aircraftModelFromJson = (json: string): AircraftModel =>
  JSON.parse(json) as AircraftModel

export const aircraftModelToJson = (value: AircraftModel): string =>
  JSON.stringify(value)
