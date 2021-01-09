package idn.faza.instagramClone.fragments

import android.content.Intent
import android.os.Bundle
import android.renderscript.Sampler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.disklrucache.DiskLruCache
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import idn.faza.instagramClone.AccountSettingActivity
import idn.faza.instagramClone.R
import idn.faza.instagramClone.databinding.FragmentProfileBinding
import idn.faza.instagramClone.model.User
import kotlinx.android.synthetic.main.custom_bar.*


class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding

    // buat variable user info berisi database reference
    // database reference adalah bagian realtime database dari firebase
    private lateinit var databaseReference: DatabaseReference

    // buat variable user yang berisi model user dari struktur data di firebase
    private lateinit var user: User

   //onCreateView dipakai untuk menginisialisasi view yang ada pada layout fragment_profile
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       // inisialisasi viewBinding
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
     // onViewCard untuk memberi fungsi pada view di dalam layout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.edtProfile.setOnClickListener{
            val intent = Intent(view.context, AccountSettingActivity::class.java)
            startActivity(intent)
        }

        //cek adanya user yang login saat ini
        FirebaseAuth.getInstance().currentUser?.let { currentUser ->
            //  dapatkan database reference berdasarkan uid
            val uidUser = currentUser.uid
            databaseReference = FirebaseDatabase.getInstance()
                .reference
                .child("users")
                .child(uidUser)

            databaseReference.addValueEventListener(
                object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        user = snapshot.getValue(User::class.java) as User
                        binding.run {
                            titleProfile.text = user.username
                            textView2.text = user.Bio
                            bioData.text = user.fullname

                            if (user.image.isEmpty()) user.image =
                                "https://tanjungpinangkota.bawaslu.go.id/wp-content/uploads/2020/05/default-1.jpg"
                            Glide.with(this@ProfileFragment).load(user.image)
                                .circleCrop()
                                .into(imgProfile)
                        }
                    }
                    }

                }
            )
        }
    }
}
