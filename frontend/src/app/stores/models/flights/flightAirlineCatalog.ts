// 本文件定义航司目录数据，供航班筛选和展示使用。

export const flightAirlineNameByCode: Record<string, string> = {
  NL: '奶龙航空',
  LD: '科比航空',
  TF: '双子塔航空',
  YS: '雪豹航空',
  WX: '星际穿越航空',
  ZX: '祖国人航空',
  JN: 'SpaceX航空',
  PM: '无人驾驶航空',
  NM: '卡皮巴拉航空',
  MH: '万户航空',
  MU: '奶龙航空',
  '9C': '科比航空',
}

export const flightAirlineLogoPathByCode: Record<string, string> = {
  NL: '/images/airlines/NL.svg',
  LD: '/images/airlines/LD.svg',
  MH: '/images/airlines/MH.svg',
  TF: '/images/airlines/TF.svg',
  YS: '/images/airlines/YS.svg',
  WX: '/images/airlines/WX.svg',
  ZX: '/images/airlines/ZX.svg',
  JN: '/images/airlines/JN.svg',
  PM: '/images/airlines/PM.svg',
  NM: '/images/airlines/NM.svg',
  MU: '/images/airlines/MU.svg',
  '9C': '/images/airlines/9C.svg',
}

export function getFlightDetailsPlannerAirlineDisplayNameByCode(airlineCode: string | undefined, fallbackName = '航空公司'): string {
  const normalizedCode = airlineCode?.trim().toUpperCase()
  return normalizedCode ? flightAirlineNameByCode[normalizedCode] ?? fallbackName : fallbackName
}

export function getFlightDetailsPlannerAirlineLogoPathByCode(airlineCode: string | undefined, fallbackPath: string | null = null): string | null {
  const normalizedCode = airlineCode?.trim().toUpperCase()
  return normalizedCode ? flightAirlineLogoPathByCode[normalizedCode] ?? fallbackPath : fallbackPath
}
