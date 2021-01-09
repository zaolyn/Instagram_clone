package idn.faza.instagramClone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import idn.faza.instagramClone.databinding.ActivityLoginBinding
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    //viewbinding untuk activity_login.xml
    private lateinit var binding: ActivityLoginBinding

    //buat variable dialog
    private lateinit var loginDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //deklarasi viewbinding dimulai
        val inflater = layoutInflater
        binding = ActivityLoginBinding.inflate(inflater)
        setContentView(binding.root)
        // deklarasi viewbinding selesai
        loginDialog = LoadingDialog(this)

        binding.run {
            textBuatAkun.setOnClickListener {
                //buat inten menuju ke register activity
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
            }

            btnLogin.setOnClickListener{
                loginuser()
            }
        }

    }

    // buat fungsi loginuser
    private fun loginuser() {
    val email = binding.inputEmail.text.toString()
    val password = binding.inputPass.text.toString()

        if(email.isEmpty()) {
            Toast.makeText(this, "Email Harus Diisi", Toast.LENGTH_SHORT).show()
            return
        }
        if(password.length < 8) {
            Toast.makeText(this, "Kata sandi minimal 8 karakter",Toast.LENGTH_SHORT).show()
            return
        }

        // jika email dan password sudah selesai
        // loginkefirebase
        val mAuth = FirebaseAuth.getInstance()
        loginDialog.startLoadingDialog()
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                // lanjut ke MainActivity
                if (task.isSuccessful) {
                    loginDialog.dissmissDialog()
                    val intent = Intent(this, MainActivity::class.java)
                    //addFlag menambahkan opsi pada intent
                    //flag FLAG_ACTIVITY_CLEAR_TASK digunakan untuk menonaktifkan tombol back
                    // flag FLAG_ACTIVITY_NEW_TASK digunakan untuk membuat activity baru
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    //mulai intent
                    startActivity(intent)
                    // tutup activity login dengan finish()
                    finish()
                } else { // jika tidak berhasil login
                    // tampilkan Toast pesan Error login
                    val message = task.exception.toString()
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    // sign out untuk keluar dari firebase
                    mAuth.signOut()
                    loginDialog.dissmissDialog()
                }
            }
    }

    override fun onStart() {
        super.onStart()

        if (FirebaseAuth.getInstance().currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
}