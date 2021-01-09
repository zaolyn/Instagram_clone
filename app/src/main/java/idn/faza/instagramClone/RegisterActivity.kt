package idn.faza.instagramClone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import idn.faza.instagramClone.databinding.ActivityRegisterBinding
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    //buat variable dialog
    private lateinit var dialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inflater = layoutInflater
        binding = ActivityRegisterBinding.inflate(inflater)
        setContentView(binding.root)

        dialog = LoadingDialog(this)

        binding.run{
            //tambahkan setOnClickListener pada tombol btnmasuk
            btnSigin.setOnClickListener{
                finish()
            }

            //tambahkan setOnClickListener pada btnregister
            btnRegister.setOnClickListener{
                createAccount()
            }
        }
    }

    private fun showToast(pesan:String) {
        Toast.makeText(this, pesan, Toast.LENGTH_SHORT).show()
    }

    //buat fungsi createAcount()
    private fun createAccount() {
        binding.run {
            val fullName = fullName.text.toString()
            val emailUser = emailRegister.text.toString()
            val userName = userName.text.toString()
            val password = passRegister.text.toString()

            //cek semua input ada isinya atau tidak kosong is not empty
            if (fullName.isEmpty()) {
                showToast("Nama Lengkap Harus Diisi !")
                return
            }

            if (emailUser.isEmpty()) {
                showToast("Email tidak valid!")
                return
            }

            if (userName.isEmpty()) {
                showToast("Nama pengguna harus diisi")
                return
            }

            if (password.isEmpty()) {
                showToast("Kata sandi harus diisi !")
                return
            }
            if (!emailUser.isEmailValid()) {
                showToast("email tidak valid")
                return
            }
            if (password.count() <8){ // jika password kurang dari 8 karakter maka showtoast akan menampilkan
                showToast("Kata Sandi minimal 8 karakter")
                return
            }

            //munculkan loading sebelum menyimpan data ke firebase
            dialog.startLoadingDialog()

            //sambungkan ke firebase auth
            val mAuth = FirebaseAuth.getInstance()
            mAuth.createUserWithEmailAndPassword(emailUser, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        saveUserInfo(fullName, userName, emailUser)
                    } else {
                        //jika gagal membuat user maka tampilkan toast berisi errornya
                        val message = task.exception
                        showToast(message.toString())
                        mAuth.signOut()
                        //jika gagal loading ditutup menggunakan
                        dialog.dissmissDialog()
                    }
                }
        }
    }

    //buat fungsi saveUserInfo()
    private fun  saveUserInfo(fullName:String, userName:String, emailUser:String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val userRef = FirebaseDatabase.getInstance().reference.child("users")
        val userMap = HashMap<String, Any>()
        userMap["uid"] = currentUserId
        userMap["fullname"] = fullName
        userMap["username"] = userName
        userMap["email"] = emailUser
        userMap["Bio"] = ""
        userMap["image"] = ""

        userRef.child(currentUserId).setValue(userMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) { // jika berhasil update firebase
                    //jika sukses tutup dialog
                    dialog.dissmissDialog()
                    showToast("Akun Sudah Dibuat")
                    // buat intent yang menuju mainactivity
                    val intent = Intent(this, MainActivity::class.java)
                    // tambahkan flag activity clear task untuk nonaktifkan tombol back
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                } else {
                    //jika gagal ditutup dialog
                    dialog.dissmissDialog()
                    val message = task.exception.toString()
                    showToast(message)
                    FirebaseAuth.getInstance().signOut()
                }
            }
    }
}