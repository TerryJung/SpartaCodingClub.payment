package kr.spartacodingclub.payment.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileMap
import kr.spartacodingclub.payment.R
import kr.spartacodingclub.payment.databinding.ActivityLoginBinding
import kr.spartacodingclub.payment.ui.MainActivity
import kr.spartacodingclub.payment.ui.signup.SignUpActivity
import kr.spartacodingclub.payment.util.Extension.toast

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        observe()

        // Naver Login
        binding.btnLoginNaver.setOnClickListener {
            NaverIdLoginSDK.authenticate(this, oauthLoginCallback)
        }

        // Kakao Login
        binding.btnLoginKakao.setOnClickListener {
            kakaoLogin()
        }

        // Google Login
        binding.btnLoginGoogle.setOnClickListener {
            googleLogin()
        }

        // Sign Up
        binding.btnSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observe() {
        viewModel.isSaved.observe(this) { isSaved ->
            if (isSaved) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }


    /**
     * 네이버 로그인 콜백
     */
    private val oauthLoginCallback = object : OAuthLoginCallback {
        override fun onSuccess() {
            // 네이버 로그인 인증이 성공했을 때 수행할 코드 추가
            Log.i(TAG, "getAccessToken ${NaverIdLoginSDK.getAccessToken()}")
            Log.i(TAG, "getRefreshToken ${NaverIdLoginSDK.getRefreshToken()}")
            NidOAuthLogin().getProfileMap(profileCallback)
        }
        override fun onFailure(httpStatus: Int, message: String) {
            val errorCode = NaverIdLoginSDK.getLastErrorCode().code
            val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
            toast(getString(R.string.login_failed))
            Log.e(TAG, "네이버 로그인 실패 : $errorCode, $errorDescription")
        }
        override fun onError(errorCode: Int, message: String) {
            onFailure(errorCode, message)
        }
    }

    val profileCallback = object : NidProfileCallback<NidProfileMap> {
        override fun onSuccess(response: NidProfileMap) {
            val name: String = response.profile?.get("name").toString()
            val email: String = response.profile?.get("email").toString()
            val mobile: String = response.profile?.get("mobile").toString()

            toast(getString(R.string.login_success))
            Log.i(TAG, "네이러 로그인 성공 : ${response.profile}")
            viewModel.saveLoginInfo(name, email, mobile)
        }
        override fun onFailure(httpStatus: Int, message: String) {
            val errorCode = NaverIdLoginSDK.getLastErrorCode().code
            val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
            toast(getString(R.string.login_failed))
            Log.e(TAG, "네이버 로그인 실패 : $errorCode, $errorDescription")
        }
        override fun onError(errorCode: Int, message: String) {
            onFailure(errorCode, message)
        }
    }


    /**
     * 카카오 로그인
     */
    private fun kakaoLogin() {
        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    Log.e(TAG, "카카오톡으로 로그인 실패 : ${error.message}")

                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }

                    // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                } else if (token != null) {
                    toast(getString(R.string.login_success))
                    Log.i(TAG, "카카오톡으로 로그인 성공 : ${token.accessToken}")
                    getKakaoUserInfo()
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }

    }

    // 카카오톡으로 로그인 할 수 없어 카카오계정으로 로그인할 경우 사용됨
    private val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            toast(getString(R.string.login_failed))
            Log.e(TAG, "카카오계정으로 로그인 실패 : ${error.message}")
        } else if (token != null) {
            toast(getString(R.string.login_success))
            Log.i(TAG, "카카오계정으로 로그인 성공 : ${token.accessToken}")
            getKakaoUserInfo()
        }
    }

    private fun getKakaoUserInfo() {
        // 사용자 정보 요청 (기본)
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                toast(getString(R.string.get_user_info_failed))
                Log.e(TAG, "사용자 정보 요청 실패 : ${error.message}")
            }
            else if (user != null) {
                val name = user.kakaoAccount?.profile?.nickname
                val email = user.kakaoAccount?.email
                val mobile = user.kakaoAccount?.phoneNumber
                toast(getString(R.string.get_user_info_success))
                Log.i(TAG, "사용자 정보 요청 성공 : $user")

                viewModel.saveLoginInfo(name, email, mobile)
            }
        }
    }


    /**
     * 구글 로그인
     */
    private fun googleLogin() {
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build(),
        )

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
        signInLauncher.launch(signInIntent)
    }

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser

            toast(getString(R.string.login_success))
            Log.d(TAG, "구글 로그인 성공 : ${user?.email}, ${user?.phoneNumber}, ${user?.displayName}")

            viewModel.saveLoginInfo(user?.displayName, user?.email, user?.phoneNumber)
        } else {
            toast(getString(R.string.login_failed))
            Log.w(TAG, "구글 로그인 실페 : ${response?.error?.message}")
        }
    }


    companion object {
        private const val TAG = "LoginActivity"
    }
}