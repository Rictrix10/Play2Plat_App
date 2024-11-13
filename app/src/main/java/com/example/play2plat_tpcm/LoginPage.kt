package com.example.play2plat_tpcm

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Environment
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import com.example.play2plat_tpcm.api.ApiManager
import com.example.play2plat_tpcm.api.UserLogin
import com.example.play2plat_tpcm.api.UserLoginResponse
import com.example.play2plat_tpcm.room.entities.User
import com.example.play2plat_tpcm.room.vm.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import com.example.play2plat_tpcm.api.User as User1

class LoginPage : AppCompatActivity() {

    private lateinit var etPassword: EditText
    private lateinit var ivTogglePasswordVisibility: ImageView
    private var isPasswordVisible: Boolean = false
    private lateinit var progressLoader: ProgressBar
    private lateinit var layoutSignIn: FrameLayout
    private lateinit var tvSignInText: TextView


    private val userViewModel: UserViewModel by viewModels()

    companion object {
        private const val REQUEST_WRITE_STORAGE = 112
        private const val PREFS_NAME = "user_data"
        private const val KEY_USER_LOGGED_IN = "user_logged_in"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_page)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val token = sharedPreferences.getString("jwt_token", null)

        // Se o token existir, configure-o no ApiManager
        if (token != null) {
            ApiManager.setJwtToken(token)
        }

        etPassword = findViewById(R.id.et_password)
        ivTogglePasswordVisibility = findViewById(R.id.iv_toggle_password_visibility)

        ivTogglePasswordVisibility.setOnClickListener {
            togglePasswordVisibility()
        }

        layoutSignIn = findViewById(R.id.layout_sign_in)
        tvSignInText = findViewById(R.id.tv_sign_in_text)
        progressLoader = findViewById(R.id.progress_loader)

        layoutSignIn.setOnClickListener {
            // Mostrar o ProgressBar e ocultar o TextView quando o processo de login inicia
            tvSignInText.visibility = View.GONE
            progressLoader.visibility = View.VISIBLE

            loginUser()
        }

        val signUpTextView: TextView = findViewById(R.id.sign_up)
        signUpTextView.setOnClickListener {
            // Navegar para a RegisterPage
            val intent = Intent(this, RegisterPage::class.java)
            startActivity(intent)
        }

        val forgotPasswordTextView: TextView = findViewById(R.id.forgot_password)
        forgotPasswordTextView.setOnClickListener {
            // Navegar para a ForgetPasswordActivity
            val intent = Intent(this, ForgetPasswordActivity::class.java)
            startActivity(intent)
        }

        userViewModel.getAllUsers().observe(this, Observer { users ->
            for (user in users) {
                Log.d("LoginPage", "User: ${user.id}, ${user.username}, ${user.email}, ${user.password}, ${user.avatar}")
            }
        })

        // Solicitar permissão de gravação
        val hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_STORAGE)
        }

        checkIfUserIsLoggedIn()
    }

    private fun resetSignInLayout() {
        tvSignInText.visibility = View.VISIBLE
        progressLoader.visibility = View.GONE
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            ivTogglePasswordVisibility.setImageResource(R.drawable.icon_eye_off)
        } else {
            etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            ivTogglePasswordVisibility.setImageResource(R.drawable.icon_eye)
        }
        isPasswordVisible = !isPasswordVisible
        etPassword.setSelection(etPassword.text.length)
    }

    private fun loginUser() {
        val username = findViewById<EditText>(R.id.et_username).text.toString()
        val password = etPassword.text.toString()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.please_fill_all_fields, Toast.LENGTH_SHORT).show()
            resetSignInLayout()
            return
        }

        if (!isNetworkAvailable()) {
            Toast.makeText(this, R.string.no_connection_login, Toast.LENGTH_SHORT).show()
            resetSignInLayout()
            return
        }

        // Ocultar texto e mostrar o spinner

        val userLogin = UserLogin(username, password)
        val API_KEY = "a75cfc9e4102e2830343787f2e0f3b939f877b8d7b1f2c1a9fdd07d0d3e0542c5beed6c0e5b80eb7cd8b57ab8cbcb9dbb3b4f8b06ad86ad4fe2b7b302d907d7e"
        ApiManager.apiService.loginUser(API_KEY, userLogin).enqueue(object : Callback<UserLoginResponse> {
            override fun onResponse(call: Call<UserLoginResponse>, response: Response<UserLoginResponse>) {
                // Reverter o estado do botão após a resposta


                if (response.isSuccessful) {
                    val userLoginResponse = response.body()
                    if (userLoginResponse != null) {
                        val user = userLoginResponse.user
                        val acessToken = userLoginResponse.accessToken
                        val refreshToken = userLoginResponse.refreshToken

                        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                        with(sharedPreferences.edit()) {
                            putString("jwt_token", acessToken)
                            putString("refresh_token", refreshToken)
                            apply()
                        }

                        ApiManager.setJwtToken(acessToken)
                        ApiManager.setRefreshToken(refreshToken)

                        saveUserData(user)
                        if (user.avatar != null) {
                            saveAvatarImage(user.avatar!!) { imagePath ->
                                checkAndSaveUserToRoom(user, imagePath)
                            }
                        } else {
                            checkAndSaveUserToRoom(user, null)
                        }

                        resetSignInLayout()
                        setUserLoggedIn()
                    } else {
                        Toast.makeText(this@LoginPage, R.string.error_response_from_server, Toast.LENGTH_SHORT).show()
                        resetSignInLayout()
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        441 -> getString(R.string.user_not_found)
                        442 -> getString(R.string.invalid_credentials)
                        else -> getString(R.string.login_failed)
                    }
                    Toast.makeText(this@LoginPage, errorMessage, Toast.LENGTH_SHORT).show()
                    resetSignInLayout()
                }
            }

            override fun onFailure(call: Call<UserLoginResponse>, t: Throwable) {
                // Reverter o estado do botão se a chamada falhar
                Toast.makeText(this@LoginPage, R.string.login_failed, Toast.LENGTH_SHORT).show()
                resetSignInLayout()
            }
        })
    }


    private fun saveUserData(user: User1) {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("user_id", user.id)
        editor.putInt("user_type_id", user.userTypeId)
        editor.apply()
    }

    private fun checkAndSaveUserToRoom(user: User1, avatarPath: String?) {
        userViewModel.getUserByIdUser(user.id).observe(this, Observer { existingUser ->
            if (existingUser == null) {
                // Se o usuário não existe no Room, insere-o
                val newUser = User(0, user.id, user.email, user.username, user.password, avatarPath, user.userTypeId)
                userViewModel.addUser(newUser)
            }
            // Redireciona para a próxima atividade
            Toast.makeText(this@LoginPage, R.string.login_successful, Toast.LENGTH_SHORT).show()
            val intent = Intent(this@LoginPage, MainActivity::class.java)
            startActivity(intent)
            finish()
        })
    }

    private fun saveAvatarImage(avatarUrl: String, callback: (String) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL(avatarUrl)
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val inputStream: InputStream = connection.inputStream
                val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
                val imagePath = saveImageToExternalStorage(bitmap)
                runOnUiThread {
                    callback(imagePath)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveImageToExternalStorage(bitmap: Bitmap): String {
        val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Play2Plat")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, "user_avatar.png")
        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return file.absolutePath
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun checkIfUserIsLoggedIn() {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean(KEY_USER_LOGGED_IN, false)
        if (isLoggedIn) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setUserLoggedIn() {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_USER_LOGGED_IN, true)
        editor.apply()
    }
}
