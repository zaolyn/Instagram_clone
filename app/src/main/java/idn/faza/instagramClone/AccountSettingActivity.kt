package idn.faza.instagramClone

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.AdditionalUserInfo
import com.google.firebase.auth.EmailAuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import idn.faza.instagramClone.databinding.ActivityAccountSettingBinding
import idn.faza.instagramClone.databinding.ActivityRegisterBinding
import idn.faza.instagramClone.model.User
import kotlinx.android.synthetic.main.activity_login.*

class AccountSettingActivity : AppCompatActivity() {
    // viewbinding untuk activity_account_setting.xml
    private lateinit var binding: ActivityAccountSettingBinding

    // buat variable userInfo berisi database reference
    private lateinit var userInfo: DatabaseReference

   // buat variable user yang berisi model user untuk memetakan dari struktur data yang ada di firebase
    private lateinit var user: User

   // buat variable untuk mengakses storage firebase
    private lateinit var firebaseStorage: StorageReference

   //buat variable dialog untuk menampilkan dialog
    private lateinit var dialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //pengaturan view binding dimulai
        val inflater = layoutInflater
        binding = ActivityAccountSettingBinding.inflate(inflater)
        setContentView(binding.root)
        //pengaturan view binding selesai

        dialog = LoadingDialog(this)

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
        //tombol close diberi setOnClicklistener untuk menutupi activity
        binding.btnClose.setOnClickListener {
            finish()
        }

        // tombol centang diberi setOnClickListener untuk mengupdate info
        binding.btnAccept.setOnClickListener {
            updateUserInfo()
        }

        // jika ada user yang login
        FirebaseAuth.getInstance().currentUser?.let { currentUser ->
            // dapatkan UID dari User yang login
            val uidUser = currentUser.uid
            // dapatkan user info yang login berdasarkan UIDnya
            userInfo = FirebaseDatabase.getInstance()
                .reference
                // child users berisi nama folder dari user yang ada di firebase realtime database
                // nama ini harus persis
                .child("users")
                .child(uidUser)

            // aktifkan firebase storage buat folder bernama "imgprofile" untuk nama folder di firebase storage
            firebaseStorage = FirebaseStorage.getInstance().reference.child("imgprofile")

            // menggantikan tombol change image agar membuka crop gambar
            binding.btnChange.setOnClickListener{
                CropImage.activity().setAspectRatio(1, 1).start(this)
            }

            // jika ada user yang login maka tombol delete akun bisa di klik
            // tombol delete akun setOnclickL untuk menghapus akun
            binding.btnDelete.setOnClickListener {

                val password = binding.inputPassword.text.toString()
                val emailUser = currentUser.email.toString()

                if(password.isEmpty()){
                    Toast.makeText(this, "Masukan Ulang Password", Toast.LENGTH_SHORT).show()
                    binding.inputPassword.visibility = View.VISIBLE
                } else {
                    binding.inputPassword.visibility = View.GONE

                    val credential = EmailAuthProvider.getCredential(emailUser, password)
                    currentUser.reauthenticate(credential).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            currentUser.delete()
                            userInfo.removeValue()
                            FirebaseAuth.getInstance().signOut()

                            val intent = Intent(this, LoginActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Error :" + task.exception.toString(), Toast.LENGTH_SHORT).show()
                            binding.inputPassword.visibility = View.VISIBLE
                            binding.inputPassword.text.clear()
                        }
                    }
                }
            }
            // ambil data dari userInfo
            userInfo.addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            // jadikan data dari firebase menjadi data class User
                            user = snapshot.getValue(User::class.java) as User
                            binding.run {
                                // Masukkan data name, username, dan Bio ke dalam EditText
                                inputName.text = SpannableStringBuilder(user.fullname)
                                inputUsername.text = SpannableStringBuilder(user.username)
                                inputBio.text = SpannableStringBuilder(user.Bio)

                                var urlImage = user.image
                                if (urlImage.isEmpty()) urlImage = "https://tanjungpinangkota.bawaslu.go.id/wp-content/uploads/2020/05/default-1.jpg"
                                        // masukan gambar kedalam imageview
                                        Glide.with(this@AccountSettingActivity)
                                            .load(urlImage)
                                            .circleCrop()
                                            .into(imgProfile)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                }

            )
        }
    }
    // onActivityResult diguankan untuk menerima data dari activity lain
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // karena banyak jenis request code
        // maka untuk mengambil data dari activity cropimage kita gunakan request kode cropimage
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE
            && resultCode == Activity.RESULT_OK
            && data != null) {

            // ambil gambardari cropImage
            val resultUriImage = CropImage.getActivityResult(data).uri
            // mulai loading dialog
            dialog.startLoadingDialog()
            // buat url gambar di firebase
            val fileRef = firebaseStorage.child(user.uid + ".jpg")
            // upload gambar
            val uploadImage = fileRef.putFile(resultUriImage)

            val urlTask = uploadImage.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                fileRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    user.image = downloadUri.toString()
                    Glide.with(this@AccountSettingActivity)
                        .load(user.image)
                        .circleCrop()
                        .into(binding.imgProfile)
                    updateUserInfo()
                    dialog.dissmissDialog()
                    Toast.makeText(this, "sukses upload foto profile", Toast.LENGTH_SHORT).show()
                }else{
                    dialog.dissmissDialog()
                    Toast.makeText(this,"Gagal upload foto profil", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }


    // buat private fungsi UpdateUserInfo() untuk menyimpan info user ke dalam database
    // private fun itu fungsi yang hanya bisa diakses oleh fungsi lain di dalam class yang sama
    private fun updateUserInfo() {
        //akses semua input text yang ada di account setting activity
        binding.run {
            val fullName = inputName.text.toString()
            val userName = inputUsername.text.toString()
            val userBio = inputBio.text.toString()

            if (fullName.isEmpty()) {
                Toast.makeText(
                    this@AccountSettingActivity, "Nama harus diisi !", Toast.LENGTH_SHORT
                )
                    .show()
                return
            }

            if (userName.isEmpty()) {
                Toast.makeText(
                    this@AccountSettingActivity, "userName harus diisi !", Toast.LENGTH_SHORT
                )
                    .show()
                return
            }

            // buat userMap
            val userMap = HashMap<String, Any>()
            userMap["fullname"] = fullName
            userMap["username"] = userName
            userMap["Bio"] = userBio
            userMap["uid"] = user.uid
            userMap["image"] = user.image

            // update data yang ada pada firebase
            userInfo.updateChildren(userMap)
            Toast.makeText(
                this@AccountSettingActivity,
                "User Telah Diupdate",
                Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}