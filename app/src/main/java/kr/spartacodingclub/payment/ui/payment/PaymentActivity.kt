package kr.spartacodingclub.payment.ui.payment

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.tosspayments.paymentsdk.view.PaymentMethod
import kr.spartacodingclub.payment.R
import kr.spartacodingclub.payment.databinding.ActivityPaymentBinding
import kr.spartacodingclub.payment.util.Extension.hideKeyboard
import kr.spartacodingclub.payment.util.Extension.toast

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private val viewModel: PaymentViewModel by viewModels()

    private var amount: Int = 19500
    private var point: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding()
    }

    private fun binding() {

        viewModel.setPrice(amount)
        viewModel.setPriceString(String.format(getString(R.string.product_price), amount))

        /**
         * 배송 메모
         */
        with(binding.layoutShipping.memo) {
            ArrayAdapter.createFromResource(
                context, R.array.memo_array, android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                setAdapter(adapter)
            }
        }

        /**
         * 쿠폰
         */
        with(binding.layoutCoupon.layoutCoupon) {
            ArrayAdapter.createFromResource(
                applicationContext, R.array.coupon_array, android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.layoutCoupon.coupon.setAdapter(adapter)
            }

            binding.layoutCoupon.btnApplyCoupon.setOnClickListener {
                if (editText?.text?.isNotEmpty() == true) {
                    viewModel.setCoupon(editText?.text.toString())
                } else {
                    toast("선택된 쿠폰이 없습니다")
                }
            }
        }

        /**
         * 포인트
         */
        with(binding.layoutCoupon.layoutPoint) {
            viewModel.userInfo.observe(this@PaymentActivity) {
                if (it?.point != null && it?.point < 5000 || amount < 10000) {
                    editText?.text = SpannableStringBuilder("포인트를 사용할 수 없습니다.")
                    editText?.isEnabled = false
                    binding.layoutCoupon.btnApplyPoint.isEnabled = false
                }
            }

            binding.layoutCoupon.btnApplyPoint.setOnClickListener {
                hideKeyboard()

                if (editText?.text?.isNotEmpty() == true) {
                    val isValid = viewModel.checkValidPoint(editText?.text.toString())

                    if (!isValid) {
                        error = context.getString(R.string.unavailable_to_use_point)
                    } else {
                        binding.layoutCoupon
                        viewModel.setPoint(editText?.text.toString())
                        point = editText?.text.toString().toInt()
                    }
                    isErrorEnabled = !isValid
                    binding.initPayment.isEnabled = isValid

                    editText?.doOnTextChanged { text, start, before, count ->
                        if (isErrorEnabled) {
                            error = null
                        }
                    }
                } else {
                    toast(context.getString(R.string.point_not_input))
                }
            }
        }



        viewModel.total.observe(this) { amount ->
            if (amount != 0) {

                binding.initPayment.setOnClickListener {
                    val intent = Intent(
                        RequestPaymentActivity.getIntent(
                            this@PaymentActivity,
                            amount = amount.toDouble(),
                            clientKey = TEST_KEY,
                            customerKey = TEST_CUSTOMER_KEY,
                            orderId = "orderId",
                            orderName = "orderName",
                            currency = PaymentMethod.Rendering.Currency.KRW,
                            countryCode = "KR",
                            variantKey = "variantKey",
                            point = point
                        )
                    )
                    startActivity(intent)
                }
            }
        }
    }

    companion object {
        private const val TEST_KEY = "test_ck_P9BRQmyarYDJ0PmaOmJaVJ07KzLN"
        private const val TEST_CUSTOMER_KEY = "test_sk_E92LAa5PVbNZJ1D5A5aZV7YmpXyJ"
    }
}