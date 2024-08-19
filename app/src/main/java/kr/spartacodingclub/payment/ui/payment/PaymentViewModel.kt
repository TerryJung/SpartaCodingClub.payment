package kr.spartacodingclub.payment.ui.payment

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.spartacodingclub.payment.model.Coupon
import kr.spartacodingclub.payment.model.UserInfo
import kr.spartacodingclub.payment.util.Constants.ADDRESS
import kr.spartacodingclub.payment.util.Constants.EMAIL
import kr.spartacodingclub.payment.util.Constants.PHONE
import kr.spartacodingclub.payment.util.Constants.POINT
import kr.spartacodingclub.payment.util.Constants.USER_NAME
import kr.spartacodingclub.payment.util.RegexUtil
import kr.spartacodingclub.payment.util.SharedPrefUtil
import kotlin.math.roundToInt

class PaymentViewModel(app: Application) : AndroidViewModel(app) {

    private val shared = SharedPrefUtil(app)

    private val price = MutableLiveData<Int>()
    val priceString = MutableLiveData<String>()
    val discount = MutableLiveData<Int>(0)
    val point = MutableLiveData<Int>(0)
    val total = MutableLiveData<Int>(0)
    val shipping = MutableLiveData<Int>(2500)

    val showProgress = MutableLiveData<Boolean>(true)

    private val _userInfo = MutableLiveData<UserInfo?>()
    val userInfo: LiveData<UserInfo?> = _userInfo

    private val couponList = arrayListOf<Coupon>(
        Coupon("30% 할인 쿠폰", 30, "정률"),
        Coupon("3000원 할인 쿠폰", 3000, "정액"),
    )

    init {
        viewModelScope.launch {
            _userInfo.postValue(
                UserInfo(
                    name = shared.getStringPreferences(USER_NAME, "홍길동") ?: "",
                    email = shared.getStringPreferences(EMAIL, "email@email.com"),
                    phone = shared.getStringPreferences(PHONE, "01000000000"),
                    address = shared.getStringPreferences(ADDRESS, "서울시 서초구 강남대로111"),
                    point = shared.getIntPreferences(POINT, 0)
                )
            )
        }
    }

    fun setPrice(amount: Int) {
        price.postValue(amount)
        total.postValue(amount.plus(shipping.value ?: 0))
    }

    fun setPriceString(amount: String) {
        priceString.postValue(amount)
    }

    fun setCoupon(value: String) {
        couponList.filter {
            value == it.name
        }.map {
            setTotal(coupon = CouponDiscountUseCases().getResult(price.value?:0,it.type, it.discount), point.value?:0)
        }
    }

    fun setPoint(value: String) {
        var discountValue = 0
        if (value.isNotEmpty() || value != "0") {
            discountValue = value.toInt()
            point.value = discountValue

            setTotal(coupon = discount.value ?: 0, point = discountValue)
        }
    }

    fun checkValidPoint(use: String): Boolean {
        val userPoint = _userInfo.value?.point ?: 0

        return RegexUtil.checkValidPoint(userPoint, use.toInt())
    }

    fun savePoint(point: Int) {
        Log.d("Save Point", "$point 포인트 사용")
        val userPoint = _userInfo.value?.point ?: shared.getIntPreferences(POINT, 0)
        shared.setIntPreferences(POINT, userPoint.minus(point))
    }

    private fun setTotal(coupon: Int, point: Int) {
        total.value = price.value?.minus(coupon)?.minus(point)?.plus(shipping.value ?: 0)
    }

    fun showProgress() {
        viewModelScope.launch {
            delay(2000)
            showProgress.postValue(false)
        }
    }
}