package kr.spartacodingclub.payment.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import kr.spartacodingclub.payment.R
import kr.spartacodingclub.payment.databinding.ActivityMainBinding
import kr.spartacodingclub.payment.ui.login.LoginActivity
import kr.spartacodingclub.payment.ui.payment.PaymentActivity
import kr.spartacodingclub.payment.util.Extension.toast

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding()
        backPressed()

        viewModel.isLogout.observe(this) { isLogout ->
            if (isLogout) {
                toast(getString(R.string.user_information_all_deleted))
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()
            }
        }
    }

    private fun binding() {
        binding.btnGoPayment.setOnClickListener {
            val intent = Intent(this, PaymentActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogOut.setOnClickListener {
            viewModel.logout()
        }
    }

    private fun backPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT) {
                backPressedInvoke()
            }
        } else {
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backPressedInvoke()
                }
            })
        }
    }

    fun backPressedInvoke() {
        if (backPressedAt + TIME_INTERVAL > System.currentTimeMillis()) {
            finish()
        } else {
            backPressedAt = System.currentTimeMillis()
            toast(getString(R.string.message_back_pressed))
        }
    }

    companion object {
        private const val TAG = "MainActivity"

        private const val TIME_INTERVAL = 2000
        private var backPressedAt: Long = 0
    }
}