package idn.faza.instagramClone

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import idn.faza.instagramClone.databinding.CustomBarBinding

class LoadingDialog (private val activity: Activity) { // tambahkan constructor atau variable activity

    //gunakan viewBinding untuk mengakses custom_bar.xml
    private val inflater =LayoutInflater.from(activity)
    private val binding = CustomBarBinding.inflate(inflater)

    //buat variable dialog tipe data AlertDialog
    private lateinit var dialog: AlertDialog

    //buat fungsi startLoadingDialog untuk memulai loading dialog
    fun startLoadingDialog() {
        if (binding.root.parent == null) {
            val alertDialog = AlertDialog.Builder(activity)
            alertDialog.setView(binding.root)
            alertDialog.setCancelable(false)
            dialog = alertDialog.create()
        }
        dialog.show()
    }

    //buat fungsi dismissDialog untuk menutup loading dialog
    fun dissmissDialog() {
        dialog.dismiss()
    }
}