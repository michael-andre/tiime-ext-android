package com.cubber.tiime.app.auth

import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import com.cubber.tiime.R
import com.cubber.tiime.api.ApiException
import com.cubber.tiime.app.MainActivity
import com.cubber.tiime.auth.AuthManager
import com.cubber.tiime.databinding.LoginActivityBinding
import com.cubber.tiime.model.ApiError
import com.cubber.tiime.utils.showErrorSnackbar
import com.wapplix.arch.UiModel
import com.wapplix.arch.getUiModel
import com.wapplix.arch.setOnEditorActionListener
import com.wapplix.binding.setContentViewBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginActivityBinding
    private lateinit var vm: VM

    private var authenticatorResponse: AccountAuthenticatorResponse? = null
    private var authenticatorResult: Bundle? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle(R.string.login)
        binding = setContentViewBinding(R.layout.login_activity)

        setSupportActionBar(binding.toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false)
            actionBar.setDisplayShowHomeEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_24dp)
        }

        binding.password.setOnEditorActionListener(
                onDone = { login(); true },
                onEnter = { login(); true }
        )

        if (savedInstanceState == null) {
            binding.username.requestFocus()
        }

        vm = getUiModel()

        authenticatorResponse = intent.getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE)
        authenticatorResponse?.onRequestContinued()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.login, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.password_recovery -> {
                var passwordRecoveryUrl = Uri.parse(PASSWORD_RECOVERY_URL)
                val login = binding.username.text.toString()
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(login).matches()) {
                    passwordRecoveryUrl = passwordRecoveryUrl.buildUpon().appendQueryParameter("login", login).build()
                }
                val i = Intent(Intent.ACTION_VIEW, passwordRecoveryUrl)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(i)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun login() {

        val login = binding.username.text.toString()
        if (TextUtils.isEmpty(login)) {
            binding.usernameLayout.error = getString(R.string.required)
            binding.username.requestFocus()
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(login).matches()) {
            binding.usernameLayout.error = getString(R.string.invalid_format)
            binding.username.requestFocus()
            return
        }
        binding.usernameLayout.error = null

        val password = binding.password.text.toString()
        if (TextUtils.isEmpty(password)) {
            binding.passwordLayout.error = getString(R.string.required)
            binding.password.requestFocus()
            return
        }
        binding.passwordLayout.error = null

        vm.login(login, password)
    }

    private fun handleError(ex: Throwable) {
        when ((ex as? ApiException)?.error?.code) {
            ApiError.CODE_INVALID_AUTHENTICATION -> {
                binding.passwordLayout.error = getString(R.string.invalid_credentials)
                binding.password.requestFocus()
                binding.password.selectAll()
            }
            else -> showErrorSnackbar(ex, R.string.login_failed)
        }
    }

    override fun finish() {
        if (authenticatorResponse != null) {
            if (authenticatorResult != null) {
                authenticatorResponse!!.onResult(authenticatorResult)
            } else {
                authenticatorResponse!!.onError(AccountManager.ERROR_CODE_CANCELED, "Login process was canceled")
            }
            authenticatorResponse = null
        }
        super.finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    class VM(application: Application) : UiModel<LoginActivity>(application) {

        internal fun login(login: String, password: String) {
            AuthManager.login(getApplication(), login, password)
                    .subscribe(
                            { result ->
                                onUi {
                                    authenticatorResult = result
                                    finish()
                                    if (isTaskRoot) {
                                        startActivity(MainActivity.newIntent(this))
                                    }
                                }
                            },
                            { e -> onUi { handleError(e) } }
                    )
        }

    }

    companion object {

        private val PASSWORD_RECOVERY_URL = "https://secure.tiime.fr/password.php"

        fun newIntent(context: Context, response: AccountAuthenticatorResponse? = null): Intent {
            val intent = Intent(context, LoginActivity::class.java)
            if (response != null) intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
            return intent
        }
    }
}