package com.mguven.holysignal.ui

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.Observer
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.mguven.holysignal.FlowController
import com.mguven.holysignal.R
import com.mguven.holysignal.TheApplication
import com.mguven.holysignal.di.module.LoginActivityModule
import com.mguven.holysignal.exception.Exceptions
import com.mguven.holysignal.model.SignInResponseEntity
import com.mguven.holysignal.model.response.ResponseEntity
import com.mguven.holysignal.sociallogin.AuthStateManager
import com.mguven.holysignal.sociallogin.AuthenticationService
import com.mguven.holysignal.sociallogin.Configuration
import com.mguven.holysignal.util.DeviceUtil
import com.mguven.holysignal.viewmodel.PreferencesViewModel
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.loadingprogress.*
import net.openid.appauth.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class LoginActivity : AbstractBaseActivity() {

  companion object {
    private const val EXTRA_FAILED = "failed"
    private const val RC_AUTH = 100
    private const val FACEBOOK_TAG = "facebook"
    private const val TWITTER_TAG = "twitter"
    private const val GOOGLE_TAG = "google"
    private val FACEBOOK_PERMISSIONS = listOf("email", "public_profile")
  }

  @Inject
  lateinit var deviceUtil: DeviceUtil

  lateinit var callbackManager: CallbackManager

  private lateinit var preferencesViewModel: PreferencesViewModel
  private lateinit var mAuthStateManager: AuthStateManager
  private var mAuthService: AuthorizationService? = null
  private lateinit var mConfiguration: Configuration

  private val mClientId = AtomicReference<String>()
  private val mAuthRequest = AtomicReference<AuthorizationRequest>()
  private val mAuthIntent = AtomicReference<CustomTabsIntent>()
  private var mExecutor: ExecutorService? = null

  private var chosenProvider: String = ""
  private var twitterAccessToken: String = ""

  private var authServerObserver = object : io.reactivex.Observer<Boolean> {

    override fun onSubscribe(d: Disposable) {}

    override fun onNext(s: Boolean) {
      if (s) {
        startAuth()
      }
    }

    override fun onError(e: Throwable) {}

    override fun onComplete() {}

  }


  private fun inject() {
    (application as TheApplication)
        .applicationComponent
        .plus(LoginActivityModule(this))
        .inject(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)

    inject()
    mExecutor = Executors.newSingleThreadExecutor()
    mAuthStateManager = AuthStateManager.getInstance(this)
    if (intent.getBooleanExtra(EXTRA_FAILED, false)) {
      displayAuthCancelled()
    }
    callbackManager = CallbackManager.Factory.create()
    login_button.setReadPermissions(FACEBOOK_PERMISSIONS)
    login_button.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
      override fun onSuccess(result: LoginResult?) {
        //do nothing
      }

      override fun onCancel() {
        //do nothing
      }

      override fun onError(error: FacebookException?) {
        //do nothing
      }
    })

    preferencesViewModel = getViewModel(PreferencesViewModel::class.java)

    btnSignIn.setOnClickListener {
      if (deviceUtil.isEmailValid(tvEmail.editText?.text.toString())) {
        loading.visibility = View.VISIBLE
        preferencesViewModel.signIn(tvEmail.editText?.text.toString(),
            tvPassword.editText?.text.toString())
      } else {
        showErrorDialog(getString(R.string.sign_in_error))
      }
    }

    flFacebook.setOnClickListener {
      login_button.performClick()
    }

    btnSignUp.setOnClickListener {
      FlowController.launchSignUpActivity(this)
    }

    preferencesViewModel.memberShipData.observe(this, Observer<ResponseEntity<SignInResponseEntity>> { response ->
      loading.visibility = View.GONE
      if (response.status == 1) {
        FlowController.launchMainActivity(this)
        finish()
      } else {
        when (response.status) {
          Exceptions.SESSION_IS_DIFFERENT -> showYesNoDialog(getString(R.string.session_no_is_different),
              DialogInterface.OnClickListener { dialog, yes ->
                preferencesViewModel.updateSessionNo(tvEmail.editText?.text.toString(),
                    tvPassword.editText?.text.toString())
                dialog.dismiss()
              },
              DialogInterface.OnClickListener { dialog, no ->
                dialog.dismiss()
              })
          Exceptions.STORED_PHONE_IS_CHANGED -> showYesNoDialog(getString(R.string.phone_changed_warning),
              DialogInterface.OnClickListener { dialog, yes ->
                preferencesViewModel.updateSessionNo(tvEmail.editText?.text.toString(),
                    tvPassword.editText?.text.toString())
                dialog.dismiss()
              },
              DialogInterface.OnClickListener { dialog, no ->
                dialog.dismiss()
              })
          Exceptions.STORED_PHONE_CHANGED_MORE_THAN_TWO_TIMES_AND_IS_UNUSABLE_NOW -> {
            showErrorDialog(getString(R.string.phone_invalid))
          }
          else -> showErrorDialog(getString(R.string.sign_in_error))
        }
      }
    })

    btnForgotPassword.setOnClickListener {
      FlowController.launchForgotPasswordActivity(this)
    }

  }


  fun onAuthenticateClickListener(view: View) {
    chosenProvider = view.tag.toString()
    recreateAuthorizationService()
    if (chosenProvider == TWITTER_TAG) {
      getTwitterRequestToken()
    } else {
      startAuthProcess()
    }

  }

  private fun recreateAuthorizationService() {
    if (mAuthService != null) {
      mAuthService!!.dispose()
    }
    mAuthService = createAuthorizationService()
    mAuthRequest.set(null)
    mAuthIntent.set(null)
  }

  private fun createAuthorizationService(): AuthorizationService {
    val builder = AppAuthConfiguration.Builder()
    //builder.setConnectionBuilder(ConnectionBuilderForTesting.INSTANCE)
    return AuthorizationService(this, builder.build())
  }

  private fun startAuthProcess() {
    mConfiguration = Configuration.getInstance(this, chosenProvider)
    if (!mConfiguration.isValid) {
      return
    }
    mConfiguration.acceptConfiguration()
    mExecutor?.submit {
      initializeAppAuth()
    }
  }

  @WorkerThread
  private fun initializeAppAuth() {
    val config = AuthorizationServiceConfiguration(mConfiguration.authEndpointUri!!, mConfiguration.tokenEndpointUri!!)
    mAuthStateManager.replace(AuthState(config))
    initializeClient()
    return

  }

  @WorkerThread
  private fun initializeClient() {
    mClientId.set(mConfiguration.clientId)
    runOnUiThread { createAuthRequest() }
    return
  }

  private fun createAuthRequest() {
    val authRequestBuilder = AuthorizationRequest.Builder(
        mAuthStateManager.current.authorizationServiceConfiguration!!,
        mClientId.get(),
        ResponseTypeValues.CODE,
        mConfiguration.redirectUri!!
    ).setScope(mConfiguration.scope!!)
    if (chosenProvider == TWITTER_TAG) {
      val params = HashMap<String, String>()
      params["oauth_token"] = twitterAccessToken
      authRequestBuilder.setAdditionalParameters(params)
    }
    mAuthRequest.set(authRequestBuilder.build())
    authServerObserver.onNext(true)
  }

  @MainThread
  private fun startAuth() {
    mExecutor?.submit { doAuth() }
  }

  @WorkerThread
  private fun doAuth() {
    val intent = mAuthService?.getAuthorizationRequestIntent(mAuthRequest.get())
    startActivityForResult(intent, RC_AUTH)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    loading.visibility = View.VISIBLE

    if (resultCode == RESULT_CANCELED) {
      displayAuthCancelled()
    } else {
      if (chosenProvider == TWITTER_TAG) {
        if (data?.data != null) {
          val params = HashMap<String, String>()
          params["oauth_token"] = data.data!!.getQueryParameter("oauth_token")!!
          params["oauth_verifier"] = data.data!!.getQueryParameter("oauth_verifier")!!
          getTwitterAccessToken(generateOAuthAuthorizationHeader(params))
        }
      } else if (chosenProvider == GOOGLE_TAG) {
        val response = AuthorizationResponse.fromIntent(data!!)
        val ex = AuthorizationException.fromIntent(data)
        when {
          response?.authorizationCode != null -> {
            mAuthStateManager.updateAfterAuthorization(response, ex)
            exchangeAuthorizationCode(response)
          }
          ex != null -> return // Authorization flow failed
          else -> return // No authorization state retained - reauthorization required
        }
      } else {
        //facebook
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
      }
    }
  }

  @MainThread
  private fun exchangeAuthorizationCode(authorizationResponse: AuthorizationResponse) {
    performTokenRequest(
        authorizationResponse.createTokenExchangeRequest(),
        AuthorizationService.TokenResponseCallback { tokenResponse, authException ->
          this.handleCodeExchangeResponse(
              tokenResponse,
              authException
          )
        })
  }

  @MainThread
  private fun performTokenRequest(request: TokenRequest, callback: AuthorizationService.TokenResponseCallback) {
    val clientAuthentication: ClientAuthentication
    try {
      clientAuthentication = mAuthStateManager.current.clientAuthentication
    } catch (ex: ClientAuthentication.UnsupportedAuthenticationMethod) {
      // Token request cannot be made, client authentication for the token endpoint could not be constructed
      return
    }
    val finalRequest: TokenRequest = when (chosenProvider) {
      FACEBOOK_TAG, TWITTER_TAG -> {
        getFinalRequest(request)
      }
      else -> {
        request
      }
    }

    mAuthService!!.performTokenRequest(finalRequest, clientAuthentication, callback)
  }

  private fun getFinalRequest(request: TokenRequest): TokenRequest {
    val params = HashMap<String, String>()
    when (chosenProvider) {
      FACEBOOK_TAG -> params["client_secret"] = getString(R.string.facebookApiSecret)
      TWITTER_TAG -> params["client_secret"] = getString(R.string.twitterApiSecret)
    }
    return TokenRequest.Builder(request.configuration, request.clientId)
        .setGrantType(request.grantType)
        .setAuthorizationCode(request.authorizationCode)
        .setRedirectUri(request.redirectUri)
        .setCodeVerifier(request.codeVerifier)
        .setScope(request.scope)
        .setRefreshToken(request.refreshToken)
        .setAdditionalParameters(params)
        .build()
  }

  @WorkerThread
  private fun handleCodeExchangeResponse(tokenResponse: TokenResponse?, authException: AuthorizationException?) {
    if (authException != null) {
      // Log the error
      return
    }
    accessTokenTextView.text = tokenResponse!!.accessToken!!
    when (chosenProvider) {
      FACEBOOK_TAG -> facebookAuthorized(tokenResponse)
      TWITTER_TAG -> twitterAuthorized(tokenResponse)
      else -> googleAuthorized(tokenResponse)
    }
  }

  private fun googleAuthorized(tokenResponse: TokenResponse) {
    preferencesViewModel.signInWithGoogle(tokenResponse)
  }

  private fun twitterAuthorized(tokenResponse: TokenResponse) {

  }

  private fun facebookAuthorized(tokenResponse: TokenResponse) {

  }

  private fun displayAuthCancelled() {
    loading.visibility = View.GONE
    //Toast.makeText(this, "Authorization canceled", Toast.LENGTH_SHORT).show()
  }


  // Twitter OAuth1.0a Utilities
  private fun getTwitterRequestToken() {
    val oauthHeader = generateOAuthAuthorizationHeader(null)
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.twitter.com/")
        .build()
    retrofit.callbackExecutor()
    val service = retrofit.create(AuthenticationService::class.java)
    val call = service.getRequestToken(oauthHeader)
    call.enqueue(object : Callback<ResponseBody> {
      override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
        if (response.isSuccessful) {
          val responseBody = response.body()!!.string()
          val regex = "oauth_token=(.*)&oauth_token_secret".toRegex()
          twitterAccessToken = regex.find(responseBody)!!.value.replace("oauth_token=", "").replace("&oauth_token_secret", "")
          startAuthProcess()
        } else {
          // There was an error processing the authorization request params
        }
      }

      override fun onFailure(fail: Call<ResponseBody>, t: Throwable) {
        Toast.makeText(this@LoginActivity, "Network error: We couldn't connect to Twitter, try again.", Toast.LENGTH_SHORT).show()
      }
    })
  }

  private fun getTwitterAccessToken(header: String) {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.twitter.com/")
        .build()
    retrofit.callbackExecutor()
    val service = retrofit.create(AuthenticationService::class.java)
    val call = service.getAccessToken(header)
    call.enqueue(object : Callback<ResponseBody> {
      @SuppressLint("SetTextI18n")
      override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
        if (response.isSuccessful) {
          val responseBody = response.body()!!.string()
          val accessTokenRegex = "oauth_token=(.*)&oauth_token_secret".toRegex()
          val tokenSecretRegex = "oauth_token_secret=(.*)&user_id".toRegex()
          val accessToken = accessTokenRegex.find(responseBody)!!.value.replace("oauth_token=", "").replace("&oauth_token_secret", "")
          val tokenSecret = tokenSecretRegex.find(responseBody)!!.value.replace("oauth_token_secret=", "").replace("&user_id", "")
          accessTokenTextView.text = "Access Token: $accessToken & Secret : $tokenSecret"
        } else {
          // There was an error processing the token request params
        }
      }


      override fun onFailure(fail: Call<ResponseBody>, t: Throwable) {
        Toast.makeText(this@LoginActivity, "Network error: We couldn't connect to Twitter, try again.", Toast.LENGTH_SHORT).show()
      }
    })
  }

  private fun generateOAuthAuthorizationHeader(extraParams: HashMap<String, String>?): String {
    val timeStamp = System.currentTimeMillis() / 1000
    val nonce = UUID.randomUUID().toString().replace("-", "")
    val params = HashMap<String, String>()
    params["oauth_callback"] = "https://mywebsite.com/oauth"
    params["oauth_consumer_key"] = "TWITTER_ID"
    params["oauth_nonce"] = nonce
    params["oauth_signature_method"] = "HMAC-SHA1"
    params["oauth_timestamp"] = timeStamp.toString()
    params["oauth_version"] = "1.0"
    if (extraParams != null) {
      params.putAll(extraParams)
    }

    return generateOAuthHeader(params, "https://api.twitter.com/oauth/request_token")
  }

  private fun generateOAuthHeader(params: HashMap<String, String>, endpoint: String): String {
    val encodedParams = ArrayList<String>()
    params.forEach { (key, value) -> encodedParams.add(percentEncode(key) + "=" + percentEncode(value)) }
    params.toSortedMap()

    Collections.sort(encodedParams, String.CASE_INSENSITIVE_ORDER)

    val paramsString = encodedParams.joinToString("&")
    val baseString = "POST&${percentEncode(endpoint)}&${percentEncode(paramsString)}"
    val signingKey = percentEncode(getString(R.string.twitterApiSecret)) + "&"

    params["oauth_signature"] = calculateRFC2104HMAC(baseString, signingKey)

    val encodedHeaderValues = ArrayList<String>()
    params.forEach { (key, value) -> encodedHeaderValues.add(percentEncode(key) + "=" + "\"" + percentEncode(value) + "\"") }

    return "OAuth ${encodedHeaderValues.joinToString(", ")}"
  }

  private fun percentEncode(s: String?): String {
    if (s == null) {
      return ""
    }
    try {
      return URLEncoder.encode(s, "UTF-8")
          // OAuth encodes some characters differently:
          .replace("+", "%20").replace("*", "%2A")
          .replace("%7E", "~")
      // This could be done faster with more hand-crafted code.
    } catch (wow: UnsupportedEncodingException) {
      throw RuntimeException(wow.message, wow)
    }
  }

  private fun calculateRFC2104HMAC(data: String, key: String): String {
    val signingKey = SecretKeySpec(key.toByteArray(), "HmacSHA1")
    val mac = Mac.getInstance("HmacSHA1")
    mac.init(signingKey)
    return android.util.Base64.encodeToString(mac.doFinal(data.toByteArray()), android.util.Base64.DEFAULT)
  }

  private fun loadFacebookUserProfile(newAccessToken: AccessToken) {
    val request = GraphRequest.newMeRequest(newAccessToken) { objekt, response ->
      val firstName = objekt.getString("first_name")
      val lastName = objekt.getString("last_name")
      val email = objekt.getString("email")
      val id = objekt.getString("id")
      preferencesViewModel.signInWithFacebook(email, firstName, lastName)
    }

    val parameters = Bundle()
    parameters.putString("fields", "first_name, last_name, email, id")
    request.parameters = parameters
    request.executeAsync()
  }

  val tokenTracker = object : AccessTokenTracker() {
    override fun onCurrentAccessTokenChanged(oldAccessToken: AccessToken?, currentAccessToken: AccessToken?) {
      if (currentAccessToken == null) {
        cache.clear()
        LoginManager.getInstance().logOut()
        FlowController.launchMainActivity(this@LoginActivity, true)
      } else {
        loadFacebookUserProfile(currentAccessToken)
      }
    }
  }

}

