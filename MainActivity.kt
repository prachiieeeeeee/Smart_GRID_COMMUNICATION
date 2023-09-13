package smartgridcommunication.project.sgc

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.regex.Pattern
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val usernameField = findViewById<EditText>(R.id.username_field)
        val passwordField = findViewById<EditText>(R.id.password_field)
        val loginButton = findViewById<Button>(R.id.login_button)

        loginButton.setOnClickListener {

            val username = usernameField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate username
            val regex = "^[a-zA-Z0-9._-]*$"
            val pattern = Pattern.compile(regex)
            val matcher = pattern.matcher(username)

            if (!matcher.matches()) {
                Toast.makeText(this, "Username can only contain letters, numbers, dots, underscores, and dashes", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate password
            if (password.contains(Regex("[^a-zA-Z0-9 ]"))) {
                Toast.makeText(this, "Password can only contain letters, numbers, and spaces", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Perform login validation here
            val loginSuccessful = username == "admin" && password == "password"

            if (loginSuccessful) {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Incorrect username or password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "LoginDatabase"
        private const val TABLE_NAME = "LoginTable"
        private const val COL_USERNAME = "Username"
        private const val COL_PASSWORD = "Password"
        private const val USERNAME_PATTERN = "^[a-zA-Z0-9._-]+\$"
        private const val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
    }

    override fun onCreate(db: SQLiteDatabase) {
        try {
            val CREATE_TABLE = "CREATE TABLE $TABLE_NAME (id INTEGER PRIMARY KEY AUTOINCREMENT, $COL_USERNAME TEXT, $COL_PASSWORD TEXT)"
            db.execSQL(CREATE_TABLE)
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error creating database", e)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
            onCreate(db)
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error upgrading database", e)
        }
    }

    fun addUser(username: String, password: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COL_USERNAME, username)
        values.put(COL_PASSWORD, password)
        try {
            db.insertOrThrow(TABLE_NAME, null, values)
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error inserting user: $username", e)
        }
        db.close()
    }


    fun checkUser(username: String, password: String): Boolean {
        return try {
            val db = this.readableDatabase
            val columns = arrayOf(COL_USERNAME)
            val selection = "$COL_USERNAME = ? AND $COL_PASSWORD = ?"
            val selectionArgs = arrayOf(username, password)
            val cursor: Cursor = db.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null)
            val count = cursor.count
            cursor.close()
            db.close()
            count > 0
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error checking user: ${e.message}")
            false
        }
    }

    fun isValidUsername(username: String): Boolean {
        return try {
            val pattern = Pattern.compile(USERNAME_PATTERN)
            pattern.matcher(username).matches()
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error validating username: ${e.message}")
            false
        }
    }

    fun isValidPassword(password: String): Boolean {
        return try {
            val pattern = Pattern.compile(PASSWORD_PATTERN)
            pattern.matcher(password).matches()
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error validating password: ${e.message}")
            false
        }
    }
}
