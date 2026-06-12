// 本文件定义 flight 模块的 `AircraftModel`，作为数据模型并提供 JSON 编解码。

export type AircraftModel = {
  modelName: string
}

export const aircraftModelFromJson = (json: string): AircraftModel =>
  JSON.parse(json) as AircraftModel

export const aircraftModelToJson = (value: AircraftModel): string =>
  JSON.stringify(value)
