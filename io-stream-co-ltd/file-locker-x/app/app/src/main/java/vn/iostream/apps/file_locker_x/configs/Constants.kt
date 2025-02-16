package vn.iostream.apps.file_locker_x.configs

class Constants {
    companion object {
        val Token = "275010a649c4d5690f10dc49b9418456"
        val Salt = Token.toByteArray(Charsets.UTF_8)
        val Iterations = 2048
        val KeyLength = 384
    }
}