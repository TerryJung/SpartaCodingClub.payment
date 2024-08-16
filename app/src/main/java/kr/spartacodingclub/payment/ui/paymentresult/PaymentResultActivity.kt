package kr.spartacodingclub.payment.ui.paymentresult

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.spartacodingclub.payment.R
import kr.spartacodingclub.payment.databinding.ActivityPaymentResultBinding
import kr.spartacodingclub.payment.ui.MainActivity
import kr.spartacodingclub.payment.util.Extension.toast

class PaymentResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentResultBinding

    private lateinit var resultDataList: List<String>
    private var isSuccess: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isSuccess = intent?.getBooleanExtra(EXTRA_RESULT, false) == true
        resultDataList = intent?.getStringArrayListExtra(EXTRA_DATA).orEmpty()

        toast(if (isSuccess) getString(R.string.payment_success) else getString(R.string.payment_failed))

        bind()
    }

    private fun bind() {
        binding.result.text = resultDataList.toString()

        binding.btnNext.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }
    }

    companion object {
        private const val EXTRA_RESULT = "extraResult"
        private const val EXTRA_DATA = "extraData"

        fun getIntent(context: Context, result: Boolean, data: ArrayList<String>): Intent {
            return Intent(context, PaymentResultActivity::class.java).apply {
                putExtra(EXTRA_RESULT, result)
                putStringArrayListExtra(EXTRA_DATA, data)
            }
        }
    }
}