import type { AppLanguage } from '@/lib/mvp-types/index'

export function localizeBookingKind(kindValue: string, language: AppLanguage): string {
  const labels =
    language === 'zh'
      ? {
          hotel: '酒店',
          flight: '航班',
          Flight: '航班订单',
          Hotel: '酒店订单',
          Train: '火车票订单',
          Attraction: '景点门票订单',
          train: '火车票',
          attraction: '景点门票',
          HotelBooking: '酒店订单',
          FlightBooking: '航班订单',
          TrainBooking: '火车票订单',
          AttractionBooking: '景点门票订单',
          MixedBooking: '混合订单',
          PendingSelection: '待选择',
        }
      : {
          hotel: 'Hotel',
          flight: 'Flight',
          train: 'Train',
          attraction: 'Attraction',
          HotelBooking: 'Hotel booking',
          FlightBooking: 'Flight booking',
          TrainBooking: 'Train booking',
          AttractionBooking: 'Attraction booking',
          MixedBooking: 'Mixed booking',
          PendingSelection: 'Pending selection',
        }

  return labels[kindValue as keyof typeof labels] ?? kindValue
}

export function localizePaymentMethod(paymentMethodValue: string, language: AppLanguage): string {
  const labels =
    language === 'zh'
      ? {
          card: '银行卡',
          'bank-transfer': '银行转账',
          wallet: '钱包',
          Card: '银行卡',
          BankTransfer: '银行转账',
          Wallet: '钱包',
          LoyaltyPoints: '积分',
          alipay: '支付宝',
          'wechat-pay': '微信支付',
          'nailong-pay': '奶龙支付',
        }
      : {
          card: 'Card',
          'bank-transfer': 'Bank transfer',
          wallet: 'Wallet',
          Card: 'Card',
          BankTransfer: 'Bank transfer',
          Wallet: 'Wallet',
          LoyaltyPoints: 'Points',
          alipay: 'Alipay',
          'wechat-pay': 'WeChat Pay',
          'nailong-pay': 'NaiLong Pay',
        }

  return labels[paymentMethodValue as keyof typeof labels] ?? paymentMethodValue
}
