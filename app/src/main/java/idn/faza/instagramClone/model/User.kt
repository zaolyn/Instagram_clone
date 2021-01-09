package idn.faza.instagramClone.model

// isi dari data class user harus persis sama dengan yang ada di realtime database firebase
data class User(
    var Bio:String ="",
    var email:String="",
    var fullname:String="",
    var image:String="",
    var uid:String="",
    var username:String=""
)