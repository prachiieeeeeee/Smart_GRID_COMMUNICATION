package smartgridcommunication.project.sgc

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class HomeActivity : AppCompatActivity() {

    private lateinit var inputText: EditText
    private lateinit var inputPassword: EditText
    private lateinit var outputText: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        inputText = findViewById(R.id.inputText)
        inputPassword = findViewById(R.id.inputPassword)
        outputText = findViewById(R.id.outputText)
        outputText.text = "Text to copy"
        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            goBack()
        }


        val encryptButton = findViewById<Button>(R.id.encrBtn)
        encryptButton.setOnClickListener {
            val text = inputText.text.toString()
            val password = inputPassword.text.toString()
            val encryptedText = encrypt(text, password)
            outputText.text = encryptedText
        }

        val decryptButton = findViewById<Button>(R.id.decBtn)
        decryptButton.setOnClickListener {
            val encryptedText = outputText.text.toString()
            val password = inputPassword.text.toString()
            val decryptedText = decrypt(encryptedText, password)
            outputText.text = decryptedText
        }

        outputText.setOnLongClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", outputText.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            true
        }
    }

    private fun goBack() {
        finish()
    }

    private fun encrypt(input: String, password: String): String {
        try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val key = generateKey(password)
            val ivSpec = IvParameterSpec(ByteArray(cipher.blockSize))

            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)

            val encrypted = cipher.doFinal(input.toByteArray(Charsets.UTF_8))
            return Base64.encodeToString(encrypted, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Failed to encrypt: " + e.message, e)
        }
    }

    private fun decrypt(input: String, password: String): String {
        try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val key = generateKey(password)
            val ivSpec = IvParameterSpec(ByteArray(cipher.blockSize))

            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)

            val encrypted = Base64.decode(input, Base64.DEFAULT)
            val decrypted = cipher.doFinal(encrypted)
            return String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Failed to decrypt: " + e.message, e)
        }
    }

    private fun generateKey(password: String): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
        val key = digest.digest(password.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(key, "AES")
    }
}
