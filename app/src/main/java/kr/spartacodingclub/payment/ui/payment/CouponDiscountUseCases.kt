package kr.spartacodingclub.payment.ui.payment

import kotlin.math.roundToInt

class CouponDiscountUseCases {

    fun getResult(originalPrice:Int, type: String, discount:Int) : Int {
        when (type) {
            //percent
            "정률" -> { // 19499 * 30 / 100
                // 이런 비즈니스로직들이 정책
                val discountValue = originalPrice.times(discount)?.div(100.0)?.roundToInt() ?: 0
                return discountValue
            }
            //amount
            "정액" -> {
                return discount
            }
            else -> {
                return 0
            }
        }
    }
}